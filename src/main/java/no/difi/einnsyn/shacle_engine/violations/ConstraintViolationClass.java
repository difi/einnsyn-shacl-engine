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

            ConstraintViolationClass obj1 = (ConstraintViolationClass) obj;

            if (!obj1.message.equals(message)) {
                return false;
            }
            if (!obj1.resource.equals(resource)) {
                return false;
            }

            return true;

        } else {

            return false;
        }

    }
}
