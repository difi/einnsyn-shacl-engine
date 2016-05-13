package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;
import no.difi.einnsyn.shacl_engine.validation.results.ValidationResults;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;

/**
 * Created by havardottestad on 06/05/16.
 *
 *
 */
public class ConstraintViolationDatatype extends ConstraintViolation {
    private final IRI actualDatatype;

    public ConstraintViolationDatatype(PropertyConstraint propertyConstraint, Resource resource, String s, IRI actualDatatype) {
        super(propertyConstraint, resource, s);
        this.actualDatatype = actualDatatype;
    }

    @Override
    public String toString() {

        new ValidationResults((IRI) resource, propertyConstraint.getPredicate(), SHACLExt.none, actualDatatype, message);

        return "ConstraintViolationDatatype{" +
            "propertyConstraint=" + propertyConstraint +
            ", resource=" + resource +
            ", message='" + message + '\'' +
            ", actualDatatype=" + actualDatatype +
            '}';
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
