package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.Datatype;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import java.util.List;

/**
 * Created by havardottestad on 06/05/16.
 *
 *
 */
public class ConstraintViolationDatatype extends ConstraintViolation {
    private final IRI actualDatatype;
    private final IRI expectedDatatype;

    public ConstraintViolationDatatype(Datatype propertyConstraint, Resource resource, String s, IRI actualDatatype) {
        super(propertyConstraint, resource, s);
        this.actualDatatype = actualDatatype;
        this.expectedDatatype = propertyConstraint.getDatatype();
    }

    @Override
    public String toString() {

        return "ConstraintViolationDatatype{" +
            "propertyConstraint=" + propertyConstraint +
            ", resource=" + resource +
            ", message='" + message + '\'' +
            ", actualDatatype=" + actualDatatype +
            '}';
    }


    @Override
    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        statements.add(factory.createStatement(validationResultsIri, SHACLExt.actual, actualDatatype));
        statements.add(factory.createStatement(validationResultsIri, SHACLExt.expected, expectedDatatype));

        return statements;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ConstraintViolationDatatype) {

            ConstraintViolationDatatype datatypeObject = (ConstraintViolationDatatype) obj;

            return datatypeObject.actualDatatype.equals(actualDatatype) && datatypeObject.resource.equals(resource);

        } else {
            return false;
        }
    }
}
