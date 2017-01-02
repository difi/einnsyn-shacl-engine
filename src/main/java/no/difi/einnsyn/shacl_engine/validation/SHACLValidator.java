package no.difi.einnsyn.shacl_engine.validation;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.Arkiv;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.backportedReasoner.FastRdfsForwardChainingSail;
import no.difi.einnsyn.shacl_engine.rules.Shape;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import no.difi.einnsyn.shacl_engine.violations.StrictModeStatementHandler;
import org.openrdf.IsolationLevels;
import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.memory.model.MemStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
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

    private static final ValueFactory vf = SimpleValueFactory.getInstance();

    private static Logger logger = LoggerFactory.getLogger(SHACLValidator.class);


    public SHACLValidator(Repository shaclRules, Repository ontology, boolean strictMode) {

        this.strictMode = strictMode;
        if (shaclRules == null) {
            return;
        }

        try (RepositoryConnection shapesConnection = shaclRules.getConnection()) {
            RepositoryResult<Statement> statements = shapesConnection.getStatements(null, RDF.TYPE, SHACL.Shape);

            shapes = QueryResults
                .stream(statements)
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

        if (shapes == null) {
            throw new NullPointerException("Shapes repository was null");
        }

        if (data == null) {
            throw new NullPointerException("Data repository was null");
        }

        Repository originalData = data;

        if (ontology != null) {
            logger.info("Inferencing");
            data = addInferencing(data, ontology);
            //data = addInferencingUsingJena(data, ontology);
        }

        final boolean[] failed = {false};

        try (RepositoryConnection dataConnection = data.getConnection()) {
            logger.info("Validating");

            shapes
                .forEach(shape -> shape.validate(dataConnection, (violation) -> {
                    if (violation.getSeverity().equals(SHACL.Violation)) {
                        failed[0] = true;
                    }
                    constraintViolationHandler.handle(violation);
                }));


            if (strictMode) {
                logger.info("Handle strict mode");

                // start by iterating over all the triples in the incoming data
                Iterations.stream(dataConnection.getStatements(null, null, null, false))

                    // cast to MemStatement
                    .map(statement -> ((MemStatement) statement))

                    // Abuse till snapshot for marking which triples have been validated.
                    // The validation methods run previously will have set tillSnapshot == Integer.MAX_VALUE -1
                    .filter(memStatement -> memStatement.getTillSnapshot() == Integer.MAX_VALUE)

                    .filter(memStatement -> !Arkiv.ONTOLOGY_GRAPH.equals(memStatement.getContext()))

                    // side effect to mark the validation as failed
                    .peek(memStatement -> failed[0] = true)

                    // notify the statement handler of the statements that caused the validation to fail
                    .forEach(strictModeStatementHandler::handle);

            }
        }

        return !failed[0];

    }


    private static Repository addInferencing(Repository data, Repository ontology) {
        MemoryStore baseSail = new MemoryStore();
        Repository inferencedRepository = new SailRepository(new FastRdfsForwardChainingSail(baseSail, ontology));

        inferencedRepository.initialize();

        try (RepositoryConnection inferencedConnection = inferencedRepository.getConnection()) {

            inferencedConnection.begin(IsolationLevels.READ_UNCOMMITTED);

            try (RepositoryConnection dataConnection = data.getConnection()) {
                dataConnection.begin(IsolationLevels.READ_UNCOMMITTED);

                RepositoryResult<Statement> statements = dataConnection.getStatements(null, null, null, false);

                String uuid = UUID.randomUUID().toString();

                while (statements.hasNext()) {
                    Statement next = statements.next();
                    Resource subject = next.getSubject();
                    IRI predicate = next.getPredicate();
                    Value object = next.getObject();

                    if (Arkiv.ONTOLOGY_GRAPH.equals(next.getContext())) {
                        continue;
                    }

                    if (subject instanceof BNode) {
                        subject = vf.createBNode(((BNode) subject).getID() + "_" + uuid);
                    }

                    if (object instanceof BNode) {
                        object = vf.createBNode(((BNode) object).getID() + "_" + uuid);
                    }


                    inferencedConnection.add(subject, predicate, object);
                }

                dataConnection.commit();
            }


            inferencedConnection.commit();


        }


        return inferencedRepository;
    }


}
