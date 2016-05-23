package no.difi.einnsyn.shacl_engine.rules.propertyconstraints;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.violations.*;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;

import java.util.List;

/**
 * Checks that the o in s --p--> o is an instance of the class specified in the SHACL
 * constraint for the specified predicat (p)
 *
 * https://www.w3.org/TR/shacl/#AbstractClassPropertyConstraint
 *
 */
public class Class extends MinMax {

    private IRI class_property;

    public Class(Resource object, RepositoryConnection shapes) {
        super(object, shapes);

        this.class_property = SesameUtils.getExactlyOneIri(shapes, object, SHACL.class_property);
    }

    public IRI getClassProperty() {
        return this.class_property;
    }

    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {

        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> (statement.getObject() instanceof Resource) &&
                !(dataGraphConnection.hasStatement((Resource) statement.getObject(), RDF.TYPE, class_property, true)))

            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationClass(this, resource, "Incorrect class type.", statement))
            );

        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> !(statement.getObject() instanceof Resource))
            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationClass(this, resource, "Object is a literal, expected IRI.", statement))
            );
    }

    @Override
    public String toString() {
        return "PropertyConstraint{" +
            ", predicate=" + predicate +
            ", minCount=" + minCount +
            ", maxCount=" + maxCount +
            '}';
    }
}
