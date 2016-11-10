package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.ClassInConstraint;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.DatatypeInConstraint;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;

import java.util.List;

/**
 * Created by sebastienmuller on 09/06/16.
 */
public class ConstraintViolationDatatypeIn extends ConstraintViolation {
    private final DatatypeInConstraint datatypeIn;
    private final IRI actual;

    public ConstraintViolationDatatypeIn(DatatypeInConstraint datatypeIn, Resource resource, String message, Statement statement, IRI actual) {
        super(datatypeIn, resource, message);
        this.datatypeIn = datatypeIn;
        this.actual = actual;

    }

    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        datatypeIn.datatypeIn.forEach((k, v) -> {
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.expected, k));
        });

        if(actual != null){
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.actual, actual));
        }


        statements.add(factory.createStatement(validationResultsIri, RDF.TYPE, SHACLExt.ConstraintViolationClass));

        return statements;
    }

}
