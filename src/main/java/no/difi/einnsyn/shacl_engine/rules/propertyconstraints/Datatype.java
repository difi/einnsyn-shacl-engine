package no.difi.einnsyn.shacl_engine.rules.propertyconstraints;

import com.complexible.common.rdf.model.StardogValueFactory;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationDatatype;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleLiteral;
import org.openrdf.repository.RepositoryConnection;

import java.util.List;

/**
 * Created by havardottestad on 04/05/16.
 */
public class Datatype extends MinMax {

    private IRI datatype;

    public Datatype(Resource object, RepositoryConnection shapes) {
        super(object, shapes);

        this.datatype = getExactlyOneIri(shapes, object, SHACL.datatype);
    }

    public IRI getDatatype() {
        return this.datatype;
    }

    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {

        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> !(statement.getObject() instanceof SimpleLiteral))
            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationDatatype(this, resource, "Not a literal", null))
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
                    ((SimpleLiteral) statement.getObject()).getDatatype())
            ));


        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> statement.getObject() instanceof SimpleLiteral)
            .filter(statement -> datatype.equals(StardogValueFactory.XSD.DATE))
            .filter(statement -> statement.getObject().stringValue().contains("T"))
            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationDatatype(this, resource, "Datetime found in xsd:date field",
                    ((SimpleLiteral) statement.getObject()).getDatatype())
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
