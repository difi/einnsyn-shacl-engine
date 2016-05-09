package no.difi.einnsyn.shacle_engine.rules;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacle_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by havardottestad on 04/05/16.
 */
public class Shape {


    private Resource scopeClass;
    private final List<PropertyConstraint> properties = new ArrayList<>();


    public Shape(Resource subject, RepositoryConnection shapesConnection) {
        scopeClass = (Resource) shapesConnection.getStatements(subject, SHACL.scopeClass, null).next().getObject();

        RepositoryResult<Statement> statements = shapesConnection.getStatements(subject, SHACL.property, null);

        Iterations.stream(statements)
            .map(statement -> new PropertyConstraint((Resource) statement.getObject(), shapesConnection))
            .forEach(properties::add);

    }

    class TempStatementsAndResource {
        Resource resource;
        List<Statement> list;

        public TempStatementsAndResource(Resource resource, List<Statement> list) {
            this.resource = resource;
            this.list = list;
        }
    }

    public boolean validate(RepositoryConnection dataConnection, ConstraintViolationHandler constraintViolationHandler) {

        RepositoryResult<Statement> statements = dataConnection.getStatements(null, RDF.TYPE, scopeClass);

        Optional<Boolean> reduce = Iterations.stream(statements)

            .map(statement ->
                new TempStatementsAndResource(
                    statement.getSubject(),
                    Iterations
                        .stream(dataConnection.getStatements(statement.getSubject(), null, null))
                        .collect(Collectors.toList())
                )
            )

            // validate every property in the properties (constraint list)
            .map(tempStatementsAndResource -> properties.stream()
                .map(property -> property.validate(tempStatementsAndResource.resource, tempStatementsAndResource.list, constraintViolationHandler))

                // .map(property -> property.validate(statement.getSubject(), dataConnection, constraintViolationHandler))
                .reduce((b1, b2) -> b1 && b2))

            // filter, map and reduce to get the result
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce((b1, b2) -> b1 && b2);

        if (reduce.isPresent()) {
            return reduce.get();
        }

        return true;


    }
}
