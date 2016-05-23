package no.difi.einnsyn.shacl_engine.violations;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.*;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by havardottestad on 06/05/16.
 *
 *
 */
public class ConstraintViolation {
    final PropertyConstraint propertyConstraint;
    final Resource resource;
    final String message;

    IRI validationResultsIri;
    ValueFactory factory = SimpleValueFactory.getInstance();
    ConstraintViolation(PropertyConstraint propertyConstraint, Resource resource, String message) {
        this.propertyConstraint = propertyConstraint;
        this.resource = resource;
        this.message = message;

        validationResultsIri = factory.createIRI("http://example.org/", UUID.randomUUID().toString());
    }

    public List<Statement> validationResults() {

        List<Statement> statements = new ArrayList<>();
        ValueFactory factory = SimpleValueFactory.getInstance();

        statements.add(factory.createStatement(validationResultsIri, RDF.TYPE, SHACL.ValidationResult));
        statements.add(factory.createStatement(validationResultsIri, SHACL.severity, SHACL.Violation));
        statements.add(factory.createStatement(validationResultsIri, SHACL.focusNode, resource));
        statements.add(factory.createStatement(validationResultsIri, SHACL.subject, resource));
        statements.add(factory.createStatement(validationResultsIri, SHACL.predicate, propertyConstraint.getPredicate()));

        Literal messageLiteral = factory.createLiteral(message);
        statements.add(factory.createStatement(validationResultsIri, SHACL.message, messageLiteral));

        return statements;
    }

    public JsonElement toJson() throws JsonLdError {

        Model model = new LinkedHashModel();
        StringWriter stringWriter = new StringWriter();

        List<Statement> statements = validationResults();

        ValueFactory factory = SimpleValueFactory.getInstance();

        statements.forEach(model::add);


        RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, stringWriter);

        writer.startRDF();
        model.forEach(writer::handleStatement);
        writer.endRDF();

        String jsonld = stringWriter.toString();

        try {
            Object jsonLdObject = JsonUtils.fromString(jsonld);

            String frameString = IOUtils.toString(ConstraintViolation.class.getClassLoader().getResourceAsStream("jsonLdFrame.json"));
            Object frame = JsonUtils.fromString(frameString);

            JsonLdOptions options = new JsonLdOptions();

            Map<String, Object> framedJsonLd = JsonLdProcessor.frame(jsonLdObject, frame, options);

            JsonElement parse = new JsonParser().parse(JsonUtils.toString(framedJsonLd));
            parse.getAsJsonObject().remove("@context");
            JsonArray asJsonArray = parse.getAsJsonObject().getAsJsonArray("@graph");

            if(asJsonArray.size() != 1) {
                throw new IllegalStateException("More than one result was generated for a constraint violation");
            }

            return asJsonArray.get(0);

        } catch (IOException e) {
            throw new IllegalStateException("There should never be an IOException when reading from a string");
        }
    }

    @Override
    public String toString() {

        return "ConstraintViolation{" +
            "propertyConstraint=" + propertyConstraint +
            ", resource=" + resource +
            ", message='" + message + '\'' +
            '}';
    }
}
