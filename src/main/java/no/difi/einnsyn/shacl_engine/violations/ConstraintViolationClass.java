package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.Class;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

import java.util.List;

/**
 * Created by veronika on 5/10/16.
 *
 *
 */
public class ConstraintViolationClass extends ConstraintViolation {

    private IRI class_property;

    public ConstraintViolationClass(Class propertyConstraint, Resource resource, String message) {
        super(propertyConstraint, resource, message);

        if (propertyConstraint.getClassProperty() != null) {
            this.class_property = propertyConstraint.getClassProperty();
        }
    }

    @Override
    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        statements.add(factory.createStatement(validationResultsIri, SHACL.object, class_property));

        return statements;
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
