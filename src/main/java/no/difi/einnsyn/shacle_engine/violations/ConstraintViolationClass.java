package no.difi.einnsyn.shacle_engine.violations;

import no.difi.einnsyn.shacle_engine.rules.PropertyConstraint;
import org.openrdf.model.Resource;

/**
 * Created by veronika on 5/10/16.
 *
 *
 */
public class ConstraintViolationClass extends ConstraintViolation {
    public ConstraintViolationClass(PropertyConstraint propertyConstraint, Resource resource, String message) {
        super(propertyConstraint, resource, message);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ConstraintViolationClass) {

            ConstraintViolationClass classObject = (ConstraintViolationClass) obj;

            return classObject.message.equals(message) && classObject.resource.equals(resource);

        } else {
            return false;
        }
    }
}
