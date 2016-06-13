package no.difi.einnsyn.shacl_engine.rules.propertyconstraints;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationDatatype;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleLiteral;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;

import java.util.List;

/**
 * Checks that the datatype of o in s --p--> o is a literal and has the datatype
 * specified in the SHACL constraint for the specified predicat (p)
 *
 * https://www.w3.org/TR/shacl/#AbstractDatatypePropertyConstraint
 *
 */
public class DatatypeConstraint extends MinMaxConstraint {

    private final IRI datatype;

    public DatatypeConstraint(Resource object, RepositoryConnection shapes, boolean strictMode) {
        super(object, shapes, strictMode);

        this.datatype = SesameUtils.getExactlyOneIri(shapes, object, SHACL.datatype);
    }

    public IRI getDatatype() {
        return this.datatype;
    }

    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {

        super.validate(resource, list, constraintViolationHandler, dataGraphConnection);


        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> !(statement.getObject() instanceof SimpleLiteral))
            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationDatatype(this, resource, "Not a literal", statement, null))
            );


        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> {
                if (statement.getObject() instanceof SimpleLiteral) {
                    if (!((SimpleLiteral) statement.getObject()).getDatatype().equals(datatype)) {
                        return true;
                    }
                }
                return false;
            })
            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationDatatype(this, resource, "Mismatch for datatype",
                    statement, ((SimpleLiteral) statement.getObject()).getDatatype())
            ));


        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> statement.getObject() instanceof SimpleLiteral)
            .filter(statement -> datatype.equals(XMLSchema.DATE))
            .filter(statement -> statement.getObject().stringValue().contains("T"))
            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationDatatype(this, resource, "Datetime found in xsd:date field",
                    statement, ((SimpleLiteral) statement.getObject()).getDatatype())
            ));
    }

    @Override
    public String toString() {
        return "PropertyConstraint{" +
            "datatype=" + datatype +
            ", predicate=" + predicate +
            ", minCount=" + minCount +
            ", maxCount=" + maxCount +
            '}';
    }
}
