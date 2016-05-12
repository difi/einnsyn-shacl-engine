package no.difi.einnsyn.shacl_engine.rules;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.Class;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.Datatype;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.MinMax;
import no.difi.einnsyn.shacl_engine.violations.*;
import org.apache.commons.lang.NotImplementedException;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleLiteral;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

import java.util.List;
import java.util.Optional;

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


    protected abstract void validate(Resource resource, List<Statement> list,
                                     ConstraintViolationHandler constraintViolationHandler,
                                     RepositoryConnection dataGraphConnection);


      protected IRI getExactlyOneIri(RepositoryConnection repositoryConnection, Resource object, IRI property) {
        RepositoryResult<Statement> statements = repositoryConnection.getStatements(object, property, null);
        Statement classPropertyStatement = statements.next();
        IRI classObject = (IRI) classPropertyStatement.getObject();

        if (statements.hasNext()) {
            throw new IllegalArgumentException("There may only be one value for property <" + property.toString() + ">");
        }
        return classObject;
    }

    protected Optional<Integer> getOptionalOneInteger(RepositoryConnection repositoryConnection, Resource object, IRI property) {
        RepositoryResult<Statement> statements = repositoryConnection.getStatements(object, property, null);

        if(statements.hasNext()) {
           Statement classPropertyStatement = statements.next();
           Integer classObject = ((SimpleLiteral) classPropertyStatement.getObject()).intValue();

           if(statements.hasNext()) {
               throw new IllegalArgumentException("There may only be one integer value for property <" + property.toString() + ">");
           }

           return Optional.of(classObject);
        }

        return Optional.empty();

    }

    static class Factory {

        static PropertyConstraint create(Resource object, RepositoryConnection shapesConnection) {

            if(shapesConnection.hasStatement(object, SHACL.class_property, null, true)) {
                return new Class(object, shapesConnection);
            }

            if(shapesConnection.hasStatement(object, SHACL.datatype, null, true)) {
                return new Datatype(object, shapesConnection);
            }

            if(shapesConnection.hasStatement(object, SHACL.minCount, null, true) ||
                shapesConnection.hasStatement(object, SHACL.maxCount, null, true)) {
                return new MinMax(object, shapesConnection);
            }

            // Throw exception for unhandled contraints.
            RepositoryResult<Statement> statements = shapesConnection.getStatements(object, null, null);
            Model model = QueryResults.asModel(statements);
            String shaclRuleAsTurtle = SesameUtils.modelToString(model, RDFFormat.TURTLE);

            throw new NotImplementedException("Property constraint not implemented. \n" + shaclRuleAsTurtle);
        }
    }
}