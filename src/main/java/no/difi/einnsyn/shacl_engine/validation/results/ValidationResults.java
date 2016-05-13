package no.difi.einnsyn.shacl_engine.validation.results;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.sesameutils.SesameUtils;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;

/**
 * Created by veronika on 5/13/16.
 *
 * A class for constructing the validation results graph from a validation between
 * SHACL constraints and the data graph.
 */
public class ValidationResults {

    private IRI severity,
        focusNode,
        subject,
        predicate,
        object,
        actual,
        expected;

    private String message;

    private ValueFactory factory = SimpleValueFactory.getInstance();

    // All the stuffs
    public ValidationResults(IRI severity, IRI focusNode, IRI subject, IRI predicate, IRI object,
                             IRI expected, IRI actual, String message) {

        this.severity = severity;
        this.focusNode = focusNode;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.expected = expected;
        this.actual = actual;
        this.message = message;

    }

    // Without severity, default is Violation
    public ValidationResults(IRI subject, IRI predicate, IRI object,
                             IRI expected, IRI actual, String message) {

        this.severity = SHACL.Violation;
        this.subject = subject;
        this.focusNode = subject;
        this.predicate = predicate;
        this.object = object;
        this.expected = expected;
        this.actual = actual;
        this.message = message;

    }

    // Without object, for when there is use of min/max
    public ValidationResults(IRI subject, IRI predicate,
                             IRI expected, IRI actual, String message) {

        this.severity = SHACL.Violation;
        this.subject = subject;
        this.focusNode = subject;
        this.predicate = predicate;
        this.expected = expected;
        this.actual = actual;
        this.message = message;

        printValidationResults();

    }

    private Model buildValidationResults() {
        Model validationResults = new LinkedHashModel();

        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI resultSubject = factory.createIRI(SHACL.NS + "ExampleValidationResults" + this.focusNode.getLocalName());

        validationResults.add(resultSubject, RDF.TYPE, SHACL.ValidationResult);
        validationResults.add(resultSubject, SHACL.severity, this.severity);
        validationResults.add(resultSubject, SHACL.focusNode, this.focusNode);
        validationResults.add(resultSubject, SHACL.subject, this.subject);
        validationResults.add(resultSubject, SHACL.predicate, this.predicate);
        if (this.object != null) {
            validationResults.add(resultSubject, SHACL.object, this.object);
        }
        validationResults.add(resultSubject, SHACLExt.expected, this.expected);
        validationResults.add(resultSubject, SHACLExt.actual, this.actual);
        validationResults.add(resultSubject, SHACL.message, factory.createLiteral(this.message));

        return validationResults;
    }

    private void printValidationResults() {
        Model model = buildValidationResults();
        if (model != null) {
            System.out.println(SesameUtils.modelToString(model, RDFFormat.TURTLE));
        }
    }

}
