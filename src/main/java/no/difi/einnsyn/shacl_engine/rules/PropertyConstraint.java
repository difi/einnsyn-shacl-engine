package no.difi.einnsyn.shacl_engine.rules;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.ClassConstraint;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.ClassInConstraint;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.DatatypeConstraint;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.MinMaxConstraint;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
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

    protected final boolean strictMode;
    protected IRI predicate;
    protected IRI severity = SHACL.Violation;

    protected PropertyConstraint(Resource subject, RepositoryConnection shapesConnection, IRI severity, boolean strictMode) {
        this.strictMode = strictMode;
        predicate = SesameUtils.getExactlyOneIri(shapesConnection, subject, SHACL.predicate);
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

        static PropertyConstraint create(Resource object, RepositoryConnection shapesConnection, IRI severity, boolean strictMode) {

            if (shapesConnection.hasStatement(object, SHACL.class_property, null, true)) {
                return new ClassConstraint(object, shapesConnection, severity, strictMode);
            }

            if (shapesConnection.hasStatement(object, SHACL.datatype, null, true)) {
                return new DatatypeConstraint(object, shapesConnection, severity, strictMode);
            }

            if (shapesConnection.hasStatement(object, SHACL.classIn, null, true)) {
                return new ClassInConstraint(object, shapesConnection, severity, strictMode);
            }

            if (shapesConnection.hasStatement(object, SHACL.minCount, null, true) ||
                shapesConnection.hasStatement(object, SHACL.maxCount, null, true)) {

                return new MinMaxConstraint(object, shapesConnection, severity, strictMode);
            }

            // Throw exception for unhandled contraints.
            RepositoryResult<Statement> statements = shapesConnection.getStatements(object, null, null);
            Model model = QueryResults.asModel(statements);

            String shaclRuleAsTurtle = SesameUtils.modelToString(model, RDFFormat.TURTLE);

            throw new UnsupportedOperationException("Property constraint not implemented. \n" + shaclRuleAsTurtle);
        }

    }
}
