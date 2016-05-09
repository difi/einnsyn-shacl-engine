package no.difi.einnsyn.shacle_engine.violations;

import no.difi.einnsyn.shacle_engine.rules.PropertyConstraint;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;

/**
 * Created by havardottestad on 06/05/16.
 */
public class ConstraintViolationDatatype extends ConstraintViolation {
    private final IRI actualDatatype;

    public ConstraintViolationDatatype(PropertyConstraint propertyConstraint, Resource resource, String s, IRI actualDatatype) {
        super(propertyConstraint, resource, s);
        this.actualDatatype = actualDatatype;


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
    public boolean equals(Object obj) {

        if(obj instanceof ConstraintViolationDatatype){

            ConstraintViolationDatatype obj1 = (ConstraintViolationDatatype) obj;

            if(!obj1.actualDatatype.equals(actualDatatype)) return false;
            if(!obj1.resource.equals(resource)) return false;



            return true;
        }else {
            return false;
        }


    }
}
