package no.difi.einnsyn.shacle_engine.rules;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacle_engine.rules.propertyconstraints.Class;
import no.difi.einnsyn.shacle_engine.rules.propertyconstraints.Datatype;
import no.difi.einnsyn.shacle_engine.rules.propertyconstraints.MinMax;
import no.difi.einnsyn.shacle_engine.violations.*;
import org.apache.commons.lang.NotImplementedException;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleLiteral;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import java.util.List;
import java.util.Optional;

import static no.difi.einnsyn.SHACL.minCount;

/**
 * Created by havardottestad on 04/05/16.
 *
 *
 */
public abstract class PropertyConstraint {

    protected IRI predicate;

    protected PropertyConstraint(Resource object, RepositoryConnection shapes) {

        predicate = (IRI) shapes.getStatements(object, SHACL.predicate, null).next().getObject();

    }


    protected abstract void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection);


      protected IRI getExactlyOneIri(RepositoryConnection repoConnetion, Resource object, IRI property) {
        RepositoryResult<Statement> statements = repoConnetion.getStatements(object, property, null);
        Statement classPropertyStatement = statements.next();
        IRI object1 = (IRI) classPropertyStatement.getObject();

        if (statements.hasNext()) {
            throw new IllegalArgumentException("There may only be one class property per constraint.");
        }
        return object1;
    }

    protected Optional<Integer> getOptionalOneInteger(RepositoryConnection repoConnetion, Resource object, IRI property) {
        RepositoryResult<Statement> statements = repoConnetion.getStatements(object, property, null);
       if(statements.hasNext()){
           Statement classPropertyStatement = statements.next();
           Integer object1 = ((SimpleLiteral) classPropertyStatement.getObject()).intValue();

           if (statements.hasNext()) {
               throw new IllegalArgumentException("There may only be one class property per constraint.");
           }

           return Optional.of(object1);
       }

        return Optional.empty();

    }


    static class Factory {

        public static PropertyConstraint create(Resource object, RepositoryConnection shapesConnection) {

            // class
            if(shapesConnection.hasStatement(object, SHACL.class_property, null, true)) {
                return new Class(object, shapesConnection);
            }

            // datatype
            if(shapesConnection.hasStatement(object, SHACL.datatype, null, true)) {
                return new Datatype(object, shapesConnection);
            }

            // min max
            if(shapesConnection.hasStatement(object, SHACL.minCount, null, true) ||
                shapesConnection.hasStatement(object, SHACL.maxCount, null, true)) {
                return new MinMax(object, shapesConnection);
            }

            throw new NotImplementedException("Property constraint not implemented.");
        }
    }
}
