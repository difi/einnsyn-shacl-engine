package no.difi.einnsyn.shacl_engine.rules.propertyconstraints;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.Shape;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationClassIn;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationDatatypeIn;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Checks that the o in s --p--> o is an instance of the class specified in the SHACL
 * constraint for the specified predicat (p)
 *
 * https://www.w3.org/TR/shacl/#AbstractClassPropertyConstraint
 *
 */
public class DatatypeInConstraint extends MinMaxConstraint {

    public final Map<IRI, IRI> datatypeIn = new HashMap<>();

    public DatatypeInConstraint(Resource subject, RepositoryConnection shapesConnection, boolean strictMode, Shape shape) {
        super(subject, shapesConnection, strictMode, shape);

        Resource head = SesameUtils.getExactlyOneResource(shapesConnection, subject, SHACL.datatypeIn);
        while(!head.equals(RDF.NIL)) {
            IRI headFirst = SesameUtils.getExactlyOneIri(shapesConnection, head, RDF.FIRST);
            datatypeIn.put(headFirst, headFirst);
            head = SesameUtils.getExactlyOneResource(shapesConnection, head, RDF.REST);
        }

    }

    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {

        super.validate(resource, list, constraintViolationHandler, dataGraphConnection);

        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> (statement.getObject() instanceof Literal))
            .filter(statement -> {
                IRI datatype = ((Literal) statement.getObject()).getDatatype();
               return !datatypeIn.containsKey(datatype);
            })

            .forEach(statement -> constraintViolationHandler.handle(
               new ConstraintViolationDatatypeIn(this, resource, "Incorrect datatype.", statement, ((Literal) statement.getObject()).getDatatype()))
            );

        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> !(statement.getObject() instanceof Literal))
            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationDatatypeIn(this, resource, "Object is a literal, expected IRI.", statement, null))
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
