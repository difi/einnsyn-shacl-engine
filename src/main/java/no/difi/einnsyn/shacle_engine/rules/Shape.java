package no.difi.einnsyn.shacle_engine.rules;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.shacle_engine.utils.ConstraintViolationHandler;
import no.difi.einnsyn.shacle_engine.vocabulary.SHACL;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResult;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by havardottestad on 04/05/16.
 */
public class Shape {


    Resource scopeClass;
    List<Property> propertyList = new ArrayList<>();


    public Shape(Resource subject, RepositoryConnection shapesConnection) {
        scopeClass =  (Resource) shapesConnection.getStatements(subject, SHACL.scopeClass, null).next().getObject();

        //QueryResults.stream(shapesConnection.getStatements(null, null, null)).forEach(System.out::println);

        RepositoryResult<Statement> statements = shapesConnection.getStatements(subject, SHACL.property, null);

        while(statements.hasNext()){
            Statement statement = statements.next();
            Property property = new Property((Resource) statement.getObject(), shapesConnection);
            propertyList.add(property);
        }

//        Iterations.stream(statements)
//            .map(statement -> {
//                System.out.println(statement);
//                return statement;
//            })
//            .map(statement -> new Property((Resource) statement.getObject(), shapes));

    }

    public boolean validate(RepositoryConnection dataConnection, ConstraintViolationHandler constraintViolationHandler) {

        RepositoryResult<Statement> statements = dataConnection.getStatements(null, RDF.TYPE, scopeClass);

        boolean valid = true;

        while(statements.hasNext()){
            Statement next = statements.next();

            RepositoryResult<Statement> statements1 = dataConnection.getStatements(next.getSubject(), null, null);

            List<Statement> listOfStatements = new ArrayList<>();
            while (statements1.hasNext()){
                listOfStatements.add(statements1.next());
            }

            Optional<Boolean> validForProp = propertyList.stream().map(property -> property.validate(listOfStatements, constraintViolationHandler)).reduce((b1, b2) -> b1 && b2);


            if(validForProp.isPresent() && !validForProp.get()){
                valid = false;
            }


        }

        return valid;


    }
}
