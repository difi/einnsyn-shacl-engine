package no.difi.einnsyn.shacle_engine.violations;

import no.difi.einnsyn.shacle_engine.rules.PropertyConstraint;
import org.openrdf.model.Resource;

/**
 * Created by havardottestad on 06/05/16.
 */
public class ConstraintViolation {
     final PropertyConstraint propertyConstraint;
     final Resource resource;
     final String message;

    public ConstraintViolation(PropertyConstraint propertyConstraint, Resource resource, String s) {
        this.propertyConstraint = propertyConstraint;
        this.resource = resource;
        this.message = s;

    }


    @Override
    public String toString() {
        return "ConstraintViolation{" +
            "propertyConstraint=" + propertyConstraint +
            ", resource=" + resource +
            ", message='" + message + '\'' +
            '}';
    }
}
