package no.difi.einnsyn.shacl_engine.validation;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.Shape;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by veronika on 4/28/16.
 */
public class SHACLValidator {

    private List<Shape> shapes;
    private Repository ontology;

    public SHACLValidator(Repository shaclRules, Repository ontology) {
        if (shaclRules == null) {
            return;
        }

        try (RepositoryConnection shapesConnection = shaclRules.getConnection()) {
            RepositoryResult<Statement> statements = shapesConnection.getStatements(null, RDF.TYPE, SHACL.Shape);

            shapes = QueryResults.stream(statements)
                .map(statement -> new Shape(statement.getSubject(), shapesConnection))
                .collect(Collectors.toList());
        }

        this.ontology = ontology;

    }

    public boolean validate(Repository data, ConstraintViolationHandler constraintViolationHandler) {

        if (shapes == null || data == null) {
            return false;
        }

        if (ontology != null) {
//            data = addInferencing(data, ontology);
            data = addInferencingUsingJena(data, ontology);

        }

        final boolean[] failed = {false};

        try (RepositoryConnection dataConnection = data.getConnection()) {

            shapes.stream()
                .forEach(shape -> shape.validate(dataConnection, (violation) -> {
                    failed[0] = true;
                    constraintViolationHandler.handle(violation);
                }));

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
        while(stmtIterator.hasNext()){
            inferenced.add(stmtIterator.nextStatement());
        }

        StringWriter stringWriter = new StringWriter();
        inferenced.write(stringWriter, "NTRIPLES");


        try {
            Repository repository = SesameUtils.stringToRepository(stringWriter.toString(), RDFFormat.NTRIPLES);
            long after = System.currentTimeMillis();
            System.out.println("Reasoning took: " + (after - before));

            return repository;
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }


        private static Repository addInferencing(Repository data, Repository ontology) {
        Repository inferencedRepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
        inferencedRepository.initialize();

        long before = System.currentTimeMillis();

        try (RepositoryConnection inferencedConnection = inferencedRepository.getConnection()) {

            inferencedConnection.begin();
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
