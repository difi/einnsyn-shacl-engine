package no.difi.einnsyn.shacle_engine.violations;

import no.difi.einnsyn.shacle_engine.rules.PropertyConstraint;
import org.openrdf.model.Resource;

/**
 * Created by havardottestad on 06/05/16.
 *
 *
 */
public class ConstraintViolationMinCount extends ConstraintViolation{
    public ConstraintViolationMinCount(PropertyConstraint propertyConstraint, Resource resource, String s) {
        super(propertyConstraint, resource, s);
    }
}
