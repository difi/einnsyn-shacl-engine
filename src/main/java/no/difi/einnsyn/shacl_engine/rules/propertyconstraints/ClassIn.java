package no.difi.einnsyn.shacl_engine.rules.propertyconstraints;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationClassIn;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks that the o in s --p--> o is an instance of the class specified in the SHACL
 * constraint for the specified predicat (p)
 *
 * https://www.w3.org/TR/shacl/#AbstractClassPropertyConstraint
 *
 */
public class ClassIn extends MinMaxConstraint {

    private List<IRI> classIn = new ArrayList<>();

    public ClassIn(Resource subject, RepositoryConnection shapesConnection, IRI severity, boolean strictMode) {
        super(subject, shapesConnection, severity, strictMode);

        Resource classInHead = SesameUtils.getExactlyOneResource(shapesConnection, subject, SHACL.classIn);
        while(!classInHead.equals(RDF.NIL)) {
            IRI headFirst = SesameUtils.getExactlyOneIri(shapesConnection, classInHead, RDF.FIRST);
            classIn.add(headFirst);
            classInHead = SesameUtils.getExactlyOneResource(shapesConnection, classInHead, RDF.REST);
        }

    }

    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {

        super.validate(resource, list, constraintViolationHandler, dataGraphConnection);

        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> (statement.getObject() instanceof Resource))
            .filter(statement -> {
                Resource object = (Resource) statement.getObject();
                boolean ok = false;
                for(IRI possibleClass : classIn) {
                    if(dataGraphConnection.hasStatement(object, RDF.TYPE, possibleClass, true)) {
                        ok = true;
                        break;
                    }
                }
                return !ok;
            })

            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationClassIn(this, resource, "Incorrect class type.", statement))
            );

        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> !(statement.getObject() instanceof Resource))
            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationClassIn(this, resource, "Object is a literal, expected IRI.", statement))
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
