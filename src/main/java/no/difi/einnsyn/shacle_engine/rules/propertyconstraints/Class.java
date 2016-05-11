package no.difi.einnsyn.shacle_engine.rules.propertyconstraints;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacle_engine.violations.*;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleLiteral;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import java.util.List;

/**
 * Created by havardottestad on 04/05/16.
 *
 *
 */
public class Class extends MinMax {

    private IRI class_property;

    public Class(Resource object, RepositoryConnection shapes) {
        super(object, shapes);

        this.class_property = getExactlyOneIri(shapes, object, SHACL.class_property);

    }



    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {

        list.stream()

            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> (statement.getObject() instanceof Resource) &&
                !(dataGraphConnection.hasStatement((Resource) statement.getObject(), RDF.TYPE, class_property, true)))

            .forEach(statement -> {
                constraintViolationHandler.handle(new ConstraintViolationClass(this, resource, "Incorrect class type."));
            });


        list.stream()

            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> !(statement.getObject() instanceof Resource))
            .forEach(statement -> constraintViolationHandler.handle(new ConstraintViolationClass(this, resource, "Object is a literal, expected IRI.")));
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
