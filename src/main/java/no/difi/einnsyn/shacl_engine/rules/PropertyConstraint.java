package no.difi.einnsyn.shacl_engine.rules;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.Class;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.Datatype;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.MinMax;
import no.difi.einnsyn.shacl_engine.violations.*;
import org.openrdf.model.*;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import java.util.List;

/**
 * Created by havardottestad on 04/05/16.
 * Modified by veronika.
 *
 */
public abstract class PropertyConstraint {

    protected IRI predicate;
    protected IRI severity;

    protected PropertyConstraint(Resource object, RepositoryConnection shapes, IRI severity) {
        predicate = (IRI) shapes.getStatements(object, SHACL.predicate, null).next().getObject();
        this.severity = severity;
    }

    protected abstract void validate(Resource resource, List<Statement> list,
                                     ConstraintViolationHandler constraintViolationHandler,
                                     RepositoryConnection dataGraphConnection);

    public IRI getPredicate() {
        return this.predicate;
    }
    public IRI getSeverity() {
        return this.severity;
    }

    static class Factory {

        static PropertyConstraint create(Resource object, RepositoryConnection shapesConnection) {

            if(shapesConnection.hasStatement(object, SHACL.class_property, null, true)) {
                return new Class(object, shapesConnection, getSeverity(object, shapesConnection));
            }

            if(shapesConnection.hasStatement(object, SHACL.datatype, null, true)) {
                return new Datatype(object, shapesConnection, getSeverity(object, shapesConnection));
            }

            if(shapesConnection.hasStatement(object, SHACL.minCount, null, true) ||
                shapesConnection.hasStatement(object, SHACL.maxCount, null, true)) {

                return new MinMax(object, shapesConnection, getSeverity(object, shapesConnection));
            }

            // Throw exception for unhandled contraints.
            RepositoryResult<Statement> statements = shapesConnection.getStatements(object, null, null);
            Model model = QueryResults.asModel(statements);

            String shaclRuleAsTurtle = SesameUtils.modelToString(model, RDFFormat.TURTLE);

            throw new UnsupportedOperationException("Property constraint not implemented. \n" + shaclRuleAsTurtle);
        }

        static IRI getSeverity(Resource object, RepositoryConnection shapesConnection) {
            if (shapesConnection.hasStatement(object, SHACL.severity, null, true)) {
                RepositoryResult<Statement> severityStatement = shapesConnection.getStatements(object, SHACL.severity, null);
                return (IRI) severityStatement.next().getObject();
            }

            return SHACL.Violation;
        }
    }
}
