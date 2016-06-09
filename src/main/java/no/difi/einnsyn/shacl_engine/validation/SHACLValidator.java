package no.difi.einnsyn.shacl_engine.validation;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.Shape;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import no.difi.einnsyn.shacl_engine.violations.StrictModeStatementHandler;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.memory.model.MemStatement;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by veronika on 4/28/16.
 * Modified by havardottestad.
 * <p>
 * This class is the one that gets it all going. The constructor takes in a repository of SHACL shapes and a
 * repository containing the ontology which we will apply on our data graph before validating against the SHACL shapes.
 */
public class SHACLValidator {

    private boolean strictMode;
    List<Shape> shapes;
    private Repository ontology;

    public SHACLValidator(Repository shaclRules, Repository ontology, boolean strictMode) {

        this.strictMode = strictMode;
        if (shaclRules == null) {
            return;
        }

        try (RepositoryConnection shapesConnection = shaclRules.getConnection()) {
            RepositoryResult<Statement> statements = shapesConnection.getStatements(null, RDF.TYPE, SHACL.Shape);

            shapes = QueryResults.stream(statements)
                .map(statement -> new Shape(statement.getSubject(), shapesConnection, strictMode))
                .collect(Collectors.toList());
        }

        this.ontology = ontology;
    }

    public SHACLValidator(Repository shaclRules, Repository ontology) {
        if (shaclRules == null) {
            return;
        }

        try (RepositoryConnection shapesConnection = shaclRules.getConnection()) {
            RepositoryResult<Statement> statements = shapesConnection.getStatements(null, RDF.TYPE, SHACL.Shape);

            shapes = QueryResults.stream(statements)
                .map(statement -> new Shape(statement.getSubject(), shapesConnection, false))
                .collect(Collectors.toList());
        }

        this.ontology = ontology;

    }

    /**
     * Validate a given data graph against an ontology using Jena. The data graph is then validated against
     * a list of SHACL shapes to ensure that the data "fits" certain shapes (rules).
     * <p>
     * We are using Jena for inference, since Sesame will use ten times the time Jena uses to complete the task.
     * That was such an big difference between these two frameworks, so even that we're using Sesame in this project,
     * Jena will have to do with the inferencing job.
     *
     * @param data                       the given data graph
     * @param constraintViolationHandler an instance of the ConstraintViolationHandler
     * @return true if no constraint violations is found
     */
    public boolean validate(Repository data, ConstraintViolationHandler constraintViolationHandler) {
        return validate(data, constraintViolationHandler, statement -> {
        });
    }


    public boolean validate(Repository data, ConstraintViolationHandler constraintViolationHandler, StrictModeStatementHandler strictModeStatementHandler) {

        if (shapes == null || data == null) {
            return false;
        }

        Repository originalData = data;

        if (ontology != null) {
            //data = addInferencing(data, ontology);
            data = addInferencingUsingJena(data, ontology);
        }

        final boolean[] failed = {false};

        try (RepositoryConnection dataConnection = data.getConnection()) {

            shapes.stream()
                .forEach(shape -> shape.validate(dataConnection, (violation) -> {
                    if(violation.getSeverity().equals(SHACL.Violation)) {
                        failed[0] = true;
                    }
                    constraintViolationHandler.handle(violation);
                }));


            if (strictMode) {


                try (RepositoryConnection originalDataConnection = originalData.getConnection();) {
                    RepositoryResult<Statement> statements = originalDataConnection.getStatements(null, null, null);
                    while (statements.hasNext()) {
                        Statement next = statements.next();
                        RepositoryResult<Statement> statements1 = dataConnection.getStatements(next.getSubject(), next.getPredicate(), next.getObject());
                        if(statements1.hasNext()){
                            next = statements1.next();
                        }

                        if (next instanceof MemStatement) {
                            if (((MemStatement) next).getTillSnapshot() == Integer.MAX_VALUE) {
                                failed[0] = true;
                                strictModeStatementHandler.handle(next);
                            }
                        }


                    }
                }

            }

            return !failed[0];
        }
    }

    private static Repository addInferencingUsingJena(Repository data, Repository ontology) {

        long before = System.currentTimeMillis();

        Model dataJena = ModelFactory.createDefaultModel();
        Model ontologyJena = ModelFactory.createDefaultModel();

        dataJena.read(new ByteArrayInputStream(SesameUtils.repositoryToString(data, RDFFormat.NTRIPLES).getBytes()), "", "NTRIPLES");
        ontologyJena.read(new ByteArrayInputStream(SesameUtils.repositoryToString(ontology, RDFFormat.NTRIPLES).getBytes()), "", "NTRIPLES");

        Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();

        InfModel infModel = ModelFactory.createInfModel(reasoner, ontologyJena, dataJena);


        Model inferenced = ModelFactory.createDefaultModel();

        StmtIterator stmtIterator = infModel.listStatements();
        while (stmtIterator.hasNext()) {
            inferenced.add(stmtIterator.nextStatement());
        }

        StringWriter stringWriter = new StringWriter();
        inferenced.write(stringWriter, "NTRIPLES");


        Repository repository = SesameUtils.stringToRepository(stringWriter.toString(), RDFFormat.NTRIPLES);
        long after = System.currentTimeMillis();
        System.out.println("Reasoning took: " + (after - before));

        return repository;

    }

    private static Repository addInferencing(Repository data, Repository ontology) {
        Repository inferencedRepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
        inferencedRepository.initialize();

        long before = System.currentTimeMillis();

        try (RepositoryConnection inferencedConnection = inferencedRepository.getConnection()) {
            inferencedConnection.begin(IsolationLevels.NONE);

            try (RepositoryConnection dataConnection = data.getConnection()) {
                inferencedConnection.add(dataConnection.getStatements(null, null, null));
            }

            try (RepositoryConnection ontologyConnection = ontology.getConnection()) {
                inferencedConnection.add(ontologyConnection.getStatements(null, null, null));
            }
            inferencedConnection.commit();
        }

        long after = System.currentTimeMillis();
        System.out.println("Reasoning took: " + (after - before));

        return inferencedRepository;
    }


}
