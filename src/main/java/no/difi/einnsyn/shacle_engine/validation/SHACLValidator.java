package no.difi.einnsyn.shacle_engine.validation;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacle_engine.rules.Shape;
import no.difi.einnsyn.shacle_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by veronika on 4/28/16.
 *
 *
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
            data = addInferencing(data, ontology);
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

    private static Repository addInferencing(Repository data, Repository ontology) {
        Repository inferencedRepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
        inferencedRepository.initialize();

        try (RepositoryConnection inferencedConnection = inferencedRepository.getConnection()) {
            
            try (RepositoryConnection dataConnection = data.getConnection()) {
                inferencedConnection.add(dataConnection.getStatements(null, null, null));
            }

            try (RepositoryConnection ontologyConnection = ontology.getConnection()) {
                inferencedConnection.add(ontologyConnection.getStatements(null, null, null));
            }
        }

        data = inferencedRepository;
        return data;
    }

}
