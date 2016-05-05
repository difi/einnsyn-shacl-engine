package no.difi.einnsyn.shacle_engine.rules;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacle_engine.utils.ConstraintViolationHandler;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleLiteral;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import java.util.List;

/**
 * Created by havardottestad on 04/05/16.
 */
public class Property {

    private IRI predicate;
    private Integer minCount;
    private Integer maxCount;

    Property(Resource object, RepositoryConnection shapes) {

        predicate = (IRI) shapes.getStatements(object, SHACL.predicate, null).next().getObject();

        RepositoryResult<Statement> statements = shapes.getStatements(object, null, null);
        Iterations.stream(statements).forEach(statement -> {
            if(statement.getPredicate().equals(SHACL.minCount)){
                minCount = ((SimpleLiteral)statement.getObject()).intValue();
            }

            if(statement.getPredicate().equals(SHACL.maxCount)){
                maxCount = ((SimpleLiteral)statement.getObject()).intValue();
            }
        });


    }




    boolean validate(Resource subject, RepositoryConnection dataConnection, ConstraintViolationHandler constraintViolationHandler) {


        long count = Iterations
            .stream(dataConnection.getStatements(subject, predicate, null))
            .count();


        if(maxCount != null){
            if (maxCount < count) return false;
        }

        if(minCount != null){
            if (minCount > count) return false;
        }

        return true;

    }


    @Override
    public String toString() {
        return "Property{" +
            "predicate=" + predicate +
            ", minCount=" + minCount +
            ", maxCount=" + maxCount +
            '}';
    }

    public boolean validate(List<Statement> list, ConstraintViolationHandler constraintViolationHandler) {

        long count = list.stream().filter(statement -> statement.getPredicate().equals(predicate)).count();


        if(maxCount != null){
            if (maxCount < count) return false;
        }

        if(minCount != null){
            if (minCount > count) return false;
        }

        return true;
    }
}
