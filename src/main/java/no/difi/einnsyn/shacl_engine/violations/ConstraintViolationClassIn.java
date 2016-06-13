package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.ClassInConstraint;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

/**
 * Created by sebastienmuller on 09/06/16.
 */
public class ConstraintViolationClassIn extends ConstraintViolation {
    public ConstraintViolationClassIn(ClassInConstraint classIn, Resource resource, String s, Statement statement) {
        super(classIn, resource, s);
    }
}
