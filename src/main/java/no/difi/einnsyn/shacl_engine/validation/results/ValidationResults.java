package no.difi.einnsyn.shacl_engine.validation.results;

import no.difi.einnsyn.SHACL;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;

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
        object;
    private String message;

    ValidationResults(IRI focusNode, IRI subject, IRI predicate, IRI object, String message) {
        this.severity = SHACL.Violation;
        this.focusNode = focusNode;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.message = message;
    }

    public ValidationResults(IRI severity, IRI focusNode, IRI subject, IRI predicate, IRI object, String message) {
        this.severity = severity;
        this.focusNode = focusNode;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.message = message;

        printValidationResults();
    }

    public Model buildValidationResults() {
        Model validationResults = new LinkedHashModel();

        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI resultSubject = factory.createIRI(SHACL.NS + "ExampleValidationResults");

        validationResults.add(resultSubject, RDF.TYPE, SHACL.ValidationResult);
        validationResults.add(resultSubject, SHACL.severity, this.severity);
        validationResults.add(resultSubject, SHACL.focusNode, this.focusNode);
        validationResults.add(resultSubject, SHACL.subject, this.subject);
        validationResults.add(resultSubject, SHACL.predicate, this.predicate);
        validationResults.add(resultSubject, SHACL.object, this.object);
        validationResults.add(resultSubject, SHACL.message, factory.createLiteral(this.message));

        return validationResults;
    }

    public void printValidationResults() {
        Model model = buildValidationResults();
        if (model != null) {
            model.forEach(System.out::println);
        }
    }

}
