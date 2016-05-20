package no.difi.einnsyn.shacl_engine.validation;

import com.github.jsonldjava.core.JsonLdError;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;
import no.difi.einnsyn.shacl_engine.rules.Shape;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.Datatype;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolation;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationClass;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationDatatype;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.Class;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by havardottestad on 04/05/16.
 *
 *
 */
public class SHACLValidatorTest {

    @Test
    public void validateNull() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator(null, null);
        assertFalse(shaclValidator.validate(null, null));

    }


    @Test
    public void validateEmptyRule() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator(SesameUtils.stringToRepository("", RDFFormat.TURTLE), null);

        assertTrue("Empty rules set and empty data should validate", shaclValidator.validate(
            SesameUtils.stringToRepository("", RDFFormat.TURTLE),
            violation -> {
            }
        ));

    }


    @Test
    public void validateSimpleViolationMin() throws Exception {

        String dir = "testData/minCount1";


        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            violation -> {
            }
        ));

    }

    @Test
    public void validateSimpleViolationMax5() throws Exception {
        String dir = "testData/maxCount5";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            violation -> {
            }
        ));

    }

    @Test
    public void validateSimplePassMax5() throws Exception {
        String dir = "testData/maxCount5";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir+"/pass"),
            violation -> {
            }
        ));

    }


    @Test
    public void simpleShaclePassBasedOnClass() throws Exception {
        String dir = "testData/validateAgainstScopeClass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir+"/pass"),
            violation -> {
            }
        ));

    }


    @Test
    public void simpleShacleViolationMinBig() throws Exception {
        String dir = "testData/bigFile";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir+"/pass"),
            violation -> {
            }
        ));

    }


    @Test
    public void simpleShacleMinDatatypePass() throws Exception {
        String dir = "testData/datatypeStringVsLangString";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir+"/pass"),
            violation -> {
            }
        ));

    }

    @Test
    public void simpleShaclClassViolation() throws Exception {
        String dir = "testData/simpleClass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertFalse(shaclValidator.validate(
                getData(dir+"/fail"),
                violation -> {
                }
        ));

    }

    @Test
    public void simpleShaclClassPass() throws Exception {
        String dir = "testData/simpleClass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
                getData(dir+"/pass"),
                violation -> {
                }
        ));

    }

    @Test
    public void simpleShaclClassBlankNodePass() throws Exception {
        String dir = "testData/classBlankNode";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir+"/pass"),
            violation -> {
            }
        ));

    }

    @Test
    public void simpleShaclClassViolation2() throws Exception {
        String dir = "testData/simpleClass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);
        Shape shape = shaclValidator.shapes.get(0);
        
        List<ConstraintViolation> violations = new ArrayList<>();

        assertFalse(shaclValidator.validate(
                getData(dir+"/fail2"),
                violation -> {
                    violations.add(violation);
                    System.out.println(violation);

                    JsonElement jsonElement = null;
                    try {
                        jsonElement = violation.toJson();
                    } catch (JsonLdError jsonLdError) {
                        assertTrue(jsonLdError.getMessage(), false);
                    }

                    JsonObject asJsonObject = jsonElement.getAsJsonObject();

                    String message = asJsonObject.get("message").getAsString();
                    assertEquals("", "Object is a literal, expected IRI.", message);

                    assertEquals("",
                        "{\"@id\":\"http://example.org/1377d24e-8c36-4ecf-b43e-4a5cdbe9a256\",\"@type\":\"shacl:ValidationResult\",\"focusNode\":\"http://example.org/1\",\"message\":\"Object is a literal, expected IRI.\",\"object\":\"http://www.arkivverket.no/standarder/noark5/arkivstruktur/Journalpoststatus\",\"predicate\":\"http://www.arkivverket.no/standarder/noark5/arkivstruktur/journalpoststatus\",\"severity\":\"shacl:Violation\",\"subject\":\"http://example.org/1\"}".replaceAll("\\{\"@id\":(.*?),",""),
                        jsonElement.toString().replaceAll("\\{\"@id\":(.*?),","")
                    );

                    System.out.println(jsonElement);
                }
        ));

        SimpleValueFactory instance = SimpleValueFactory.getInstance();

        List<PropertyConstraint> properties = (List<PropertyConstraint>) ReflectionTestUtils.getField(shape, "properties");

        List<ConstraintViolation> expectedViolations = new ArrayList<>();
        expectedViolations.add(new ConstraintViolationClass(((Class) properties.get(0)), instance.createIRI("http://example.org/1"), "Object is a literal, expected IRI."));

        assertEquals("", expectedViolations.size(), violations.size());
        assertTrue("", violations.containsAll(expectedViolations));
        assertTrue("", expectedViolations.containsAll(violations));
    }

    @Test
    public void simpleShacleDatetimeInDateViolation() throws Exception {
        String dir = "testData/datetimeIndate";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);

        List<ConstraintViolation> violations = new ArrayList<>();

        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            System.out::println
        ));

        assertFalse(shaclValidator.validate(
            getData(dir),
            violation -> {
                violations.add(violation);
                System.out.println(violation);

                JsonElement jsonElement = null;
                try {
                    jsonElement = violation.toJson();
                } catch (JsonLdError jsonLdError) {
                    assertTrue(jsonLdError.getMessage(), false);
                }

                JsonObject asJsonObject = jsonElement.getAsJsonObject();

                String message = asJsonObject.get("message").getAsString();
                assertEquals("", "Datetime found in xsd:date field", message);


                System.out.println(jsonElement);
            }
        ));
    }

    @Test
    public void simpleShacleMinDatatypeViolation() throws Exception {
        String dir = "testData/datatypeStringVsLangString";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        List<ConstraintViolation> violations = new ArrayList<>();

        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            violation -> {
                violations.add(violation);
                System.out.println(violation);
            }
        ));

        SimpleValueFactory instance = SimpleValueFactory.getInstance();

        Shape shape = shaclValidator.shapes.get(0);

        List<PropertyConstraint> properties = (List<PropertyConstraint>) ReflectionTestUtils.getField(shape, "properties");

        List<ConstraintViolation> expectedViolations = new ArrayList<>();
        expectedViolations.add(new ConstraintViolationDatatype(((Datatype) properties.get(0)), instance.createIRI("http://example.org/1"), null, RDF.LANGSTRING));

        assertTrue("", violations.containsAll(expectedViolations));
        assertTrue("", expectedViolations.containsAll(violations));

        ConstraintViolation constraintViolation = violations.get(0);
        List<Statement> statements = constraintViolation.validationResults();
        assertEquals("Every violation should have exactly 1 focusNode", statements.stream().filter(s -> s.getPredicate().equals(SHACL.focusNode)).count(), 1);
        assertEquals("Every violation should have exactly 1 actual", statements.stream()
            .filter(s -> s.getPredicate().equals(SHACLExt.actual))
            .filter(s -> s.getObject().equals(RDF.LANGSTRING))
            .count(), 1);
        assertEquals("Every violation should have exactly 1 expected", statements.stream()
            .filter(s -> s.getPredicate().equals(SHACLExt.expected))
            .filter(s -> s.getObject().equals(XMLSchema.STRING))
            .count(), 1);

        JsonElement jsonElement = constraintViolation.toJson();

        System.out.println(jsonElement);
    }

    @Test
    public void simpleShacleMinDatatypeViolation2() throws Exception {
        String dir = "testData/datatypeStringVsLangString";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        new ArrayList<String>().stream().collect(Collectors.toList());


        assertFalse(shaclValidator.validate(
            getData(dir+"/fail2"),
            (error) -> {
            }

        ));

    }

    @Test
    public void simpleShacleMinInferencePass() throws Exception {
        String dir = "testData/minCountWithInference";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), getOntology(dir+"/pass"));


        new ArrayList<String>().stream().collect(Collectors.toList());


        assertTrue(shaclValidator.validate(
            getData(dir+"/pass"),
            (error) -> {
            }

        ));

    }

    @Test
    public void simpleShacleMinInferenceViolation() throws Exception {
        String dir = "testData/minCountWithInference";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), getOntology(dir+"/fail"));


        new ArrayList<String>().stream().collect(Collectors.toList());


        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            (error) -> {
                System.err.println(error);
            }

        ));

    }

    @Test
    public void simpleShaclClassInferencePass() throws Exception {
        String dir = "testData/classInference";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), getOntology(dir+"/pass"));


        new ArrayList<String>().stream().collect(Collectors.toList());


        assertTrue(shaclValidator.validate(
            getData(dir+"/pass"),
            System.err::println
        ));

    }

    private Repository getData(String simpleShacleViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(simpleShacleViolation + "/data.ttl");

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }

    private Repository getShacle(String simpleShacleViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(simpleShacleViolation + "/shacl.ttl");

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }

    private Repository getOntology(String simpleShacleViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(simpleShacleViolation + "/ontology.ttl");

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }
}