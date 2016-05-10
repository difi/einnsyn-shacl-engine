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


//    boolean validate(Resource subject, RepositoryConnection dataConnection, ConstraintViolationHandler constraintViolationHandler) {
//
//
//        long count = Iterations
//            .stream(dataConnection.getStatements(subject, predicate, null))
//            .count();
//
//
//        if(maxCount != null){
//            if (maxCount < count) return false;
//        }
//
//        if(minCount != null){
//            if (minCount > count) return false;
//        }
//
//        return true;
//
//    }


    public boolean validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler) {

        boolean pass = true;

        final boolean[] datatypeViolation = {false};
        final boolean[] classViolation = {false};

        long count = list.stream()

            .filter(statement -> statement.getPredicate().equals(predicate))

            .map(statement -> {
                if (datatype != null) {
                    if (statement.getObject() instanceof SimpleLiteral) {
                        if (!((SimpleLiteral) statement.getObject()).getDatatype().equals(datatype)) {
                            constraintViolationHandler.handle(new ConstraintViolationDatatype(this, resource, "Mismatch for datatype", ((SimpleLiteral) statement.getObject()).getDatatype()));
                            datatypeViolation[0] = true;
                        }
                    } else {
                        datatypeViolation[0] = true;
                        constraintViolationHandler.handle(new ConstraintViolationDatatype(this, resource, "Not a literal", null));

                    }

                }
                return statement;
            })
                .map(statement -> {
                    if (class_property != null) {

                        if (statement.getObject() instanceof Resource) {
                            boolean classPropertyOfIncomingObject = dataGraphConnection.hasStatement((Resource) statement.getObject(), RDF.TYPE, class_property, true);

                            if (!classPropertyOfIncomingObject) {
                                classViolation[0] = true;
                                constraintViolationHandler.handle(new ConstraintViolationClass(this, resource, "Incorrect class type."));
                            }

                        }
                        else {
                            classViolation[0] = true;
                            constraintViolationHandler.handle(new ConstraintViolationClass(this, resource, "Object is a literal, expected IRI."));
                        }

                    }
                    return statement;
                })
            .count();

        if (classViolation[0]) {
            pass = false;
        }

        if (datatypeViolation[0]) {
            pass = false;
        }

        if (maxCount != null) {
            if (maxCount < count) {
                pass = false;
                constraintViolationHandler.handle(new ConstraintViolationMaxCount(this, resource, "was "+count));
            }
        }

        if (minCount != null) {
            if (minCount > count) {
                constraintViolationHandler.handle(new ConstraintViolationMinCount(this, resource, "was "+count));

                pass = false;
            }
        }

        return pass;
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
