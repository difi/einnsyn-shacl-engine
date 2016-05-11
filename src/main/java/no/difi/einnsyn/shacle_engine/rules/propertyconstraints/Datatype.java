package no.difi.einnsyn.shacle_engine.rules.propertyconstraints;

import no.difi.einnsyn.shacle_engine.rules.propertyconstraints.MinMax;

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
public class Datatype extends MinMax {

    private IRI datatype;

    public Datatype(Resource object, RepositoryConnection shapes) {
        super(object, shapes);

        this.datatype = getExactlyOneIri(shapes, object, SHACL.datatype);
    }


    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {

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
