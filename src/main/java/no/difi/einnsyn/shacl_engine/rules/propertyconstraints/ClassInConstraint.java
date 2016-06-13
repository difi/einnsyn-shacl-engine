package no.difi.einnsyn.shacl_engine.rules.propertyconstraints;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationClassIn;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks that the o in s --p--> o is an instance of the class specified in the SHACL
 * constraint for the specified predicat (p)
 *
 * https://www.w3.org/TR/shacl/#AbstractClassPropertyConstraint
 *
 */
public class ClassInConstraint extends MinMaxConstraint {

    private final Map<IRI, IRI> classIn = new HashMap<>();

    public ClassInConstraint(Resource subject, RepositoryConnection shapesConnection, boolean strictMode) {
        super(subject, shapesConnection, strictMode);

        Resource classInHead = SesameUtils.getExactlyOneResource(shapesConnection, subject, SHACL.classIn);
        while(!classInHead.equals(RDF.NIL)) {
            IRI headFirst = SesameUtils.getExactlyOneIri(shapesConnection, classInHead, RDF.FIRST);
            classIn.put(headFirst, headFirst);
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

                RepositoryResult<Statement> types = dataGraphConnection.getStatements(object, RDF.TYPE, null, true);
                while(types.hasNext()){
                    IRI type = (IRI) types.next().getObject();
                    if(classIn.containsKey(type)){
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
