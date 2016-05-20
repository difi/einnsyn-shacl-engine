package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

import java.util.List;

/**
 * Created by veronika on 5/20/16.
 *
 *
 */
class ConstraintViolationWithStatement extends ConstraintViolation{

    private Statement failingStatement;

    ConstraintViolationWithStatement(PropertyConstraint propertyConstraint, Resource resource, String message, Statement failingStatement) {
        super(propertyConstraint, resource, message);
        this.failingStatement = failingStatement;
    }

    @Override
    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        statements.add(factory.createStatement(validationResultsIri, SHACL.object, failingStatement.getObject()));

        return statements;
    }
}
