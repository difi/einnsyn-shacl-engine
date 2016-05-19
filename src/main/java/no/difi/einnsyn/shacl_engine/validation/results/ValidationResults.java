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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

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

    private Repository buildValidationResults() {

        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();

        RepositoryConnection connection = repository.getConnection();

        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI resultSubject = factory.createIRI(SHACL.NS + "ExampleValidationResults" + this.focusNode.getLocalName());

        connection.add(resultSubject, RDF.TYPE, SHACL.ValidationResult);
        connection.add(resultSubject, SHACL.severity, this.severity);
        connection.add(resultSubject, SHACL.focusNode, this.focusNode);
        connection.add(resultSubject, SHACL.subject, this.subject);
        connection.add(resultSubject, SHACL.predicate, this.predicate);
        if (this.object != null) {
            connection.add(resultSubject, SHACL.object, this.object);
        }
        connection.add(resultSubject, SHACLExt.expected, this.expected);
        if(this.actual != null){
            connection.add(resultSubject, SHACLExt.actual, this.actual);
        }
        connection.add(resultSubject, SHACL.message, factory.createLiteral(this.message));

        return repository;
    }

    private void printValidationResults() {

        Repository repository = buildValidationResults();
        System.out.println(SesameUtils.repositoryToString(repository, RDFFormat.TURTLE));
    }
}
