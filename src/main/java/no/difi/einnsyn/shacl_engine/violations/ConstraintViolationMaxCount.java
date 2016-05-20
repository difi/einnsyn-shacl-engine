package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.MinMax;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

import java.util.List;
import java.util.Optional;

/**
 * Created by havardottestad on 06/05/16.
 *
 *
 */
public class ConstraintViolationMaxCount extends ConstraintViolation {

    private Optional<Integer> maxCount;
    private long actual;

    public ConstraintViolationMaxCount(MinMax propertyConstraint, Resource resource, String s) {
        super(propertyConstraint, resource, s);
        this.maxCount = propertyConstraint.getMaxCount();
        this.actual = propertyConstraint.getCount();
    }

    @Override
    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        if (maxCount.isPresent()) {
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.expected, factory.createLiteral(maxCount.get())));
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.actual, factory.createLiteral(actual)));
        }

        return statements;
    }
}
