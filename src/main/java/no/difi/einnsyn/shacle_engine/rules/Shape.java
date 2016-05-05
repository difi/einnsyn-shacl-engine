package no.difi.einnsyn.shacle_engine.rules;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacle_engine.utils.ConstraintViolationHandler;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by havardottestad on 04/05/16.
 */
public class Shape {


    private Resource scopeClass;
    private final List<Property> propertyList = new ArrayList<>();


    public Shape(Resource subject, RepositoryConnection shapesConnection) {
        scopeClass = (Resource) shapesConnection.getStatements(subject, SHACL.scopeClass, null).next().getObject();

        RepositoryResult<Statement> statements = shapesConnection.getStatements(subject, SHACL.property, null);

        Iterations.stream(statements)
            .map(statement -> new Property((Resource) statement.getObject(), shapesConnection))
            .forEach(propertyList::add);

    }

    public boolean validate(RepositoryConnection dataConnection, ConstraintViolationHandler constraintViolationHandler) {

        RepositoryResult<Statement> statements = dataConnection.getStatements(null, RDF.TYPE, scopeClass);

        Optional<Boolean> reduce = Iterations.stream(statements)

            // get list of statements for each instance of the class
            .map(statement -> {
                RepositoryResult<Statement> statements1 = dataConnection.getStatements(statement.getSubject(), null, null);

                List<Statement> listOfStatements = new ArrayList<>();
                while (statements1.hasNext()) {
                    listOfStatements.add(statements1.next());
                }
                return listOfStatements;
            })

            // validate every property in the propertyList (constraint list)
            // by running the validate method with each list of statements for the instance
            .map(list -> propertyList.stream()
                .map(property -> property.validate(list, constraintViolationHandler))
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
