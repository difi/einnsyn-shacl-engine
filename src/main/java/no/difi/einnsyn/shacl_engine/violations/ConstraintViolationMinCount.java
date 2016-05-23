package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.MinMax;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;

import java.util.List;
import java.util.Optional;

/**
 * Created by havardottestad on 06/05/16.
 *
 *
 */
public class ConstraintViolationMinCount extends ConstraintViolation {

    private Optional<Integer> minCount;
    private long actual;

    public ConstraintViolationMinCount(MinMax propertyConstraint, Resource resource, String s) {
        super(propertyConstraint, resource, s);
        this.minCount = propertyConstraint.getMinCount();
        this.actual = propertyConstraint.getCount();
    }

    @Override
    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        if (minCount.isPresent()) {
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.expected, factory.createLiteral(minCount.get())));
        }
        statements.add(factory.createStatement(validationResultsIri, SHACLExt.actual, factory.createLiteral(actual)));
        statements.add(factory.createStatement(validationResultsIri, RDF.TYPE, SHACLExt.ConstraintViolationMinCount));

        return statements;
    }
}
