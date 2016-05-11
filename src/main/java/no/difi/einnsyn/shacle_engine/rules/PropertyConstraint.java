package no.difi.einnsyn.shacle_engine.rules;

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
public class PropertyConstraint {

    private IRI datatype;
    private IRI predicate;
    private IRI class_property;
    private Integer minCount;
    private Integer maxCount;

    PropertyConstraint(Resource object, RepositoryConnection shapes) {

        predicate = (IRI) shapes.getStatements(object, SHACL.predicate, null).next().getObject();

        RepositoryResult<Statement> statements = shapes.getStatements(object, null, null);
        Iterations.stream(statements).forEach(statement -> {
            if (statement.getPredicate().equals(SHACL.minCount)) {
                minCount = ((SimpleLiteral) statement.getObject()).intValue();
            }

            if (statement.getPredicate().equals(SHACL.maxCount)) {
                maxCount = ((SimpleLiteral) statement.getObject()).intValue();
            }

            if (statement.getPredicate().equals(SHACL.datatype)) {
                datatype = (IRI) statement.getObject();
            }

            if (statement.getPredicate().equals(SHACL.class_property)) {
                class_property = (IRI) statement.getObject();
            }
        });


    }


    void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                     RepositoryConnection dataGraphConnection) {


        long count = list.stream()

            .filter(statement -> statement.getPredicate().equals(predicate))
            .count();


        if (maxCount != null) {
            if (maxCount < count) {
                constraintViolationHandler.handle(new ConstraintViolationMaxCount(this, resource, "was " + count));
            }
        }

        if (minCount != null) {
            if (minCount > count) {
                constraintViolationHandler.handle(new ConstraintViolationMinCount(this, resource, "was " + count));

            }
        }

        if (datatype != null) {

            list.stream()

                .filter(statement -> statement.getPredicate().equals(predicate))
                .filter(statement -> !(statement.getObject() instanceof SimpleLiteral))
                .forEach(statement -> {

                    constraintViolationHandler.handle(new ConstraintViolationDatatype(this, resource, "Not a literal", null));

                });

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
                .forEach(statement -> {
                    constraintViolationHandler.handle(new ConstraintViolationDatatype(this, resource, "Mismatch for datatype", ((SimpleLiteral) statement.getObject()).getDatatype()));
                });
        }
        if (class_property != null) {

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
                .forEach(statement -> {
                    constraintViolationHandler.handle(new ConstraintViolationClass(this, resource, "Object is a literal, expected IRI."));
                });
        }




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
