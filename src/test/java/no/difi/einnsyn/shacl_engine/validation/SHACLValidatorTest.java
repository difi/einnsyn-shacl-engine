package no.difi.einnsyn.shacl_engine.validation;

import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolation;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationClass;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationDatatype;
import org.junit.Test;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;

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

        String dir = "simpleShacleViolationMin";


        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertFalse(shaclValidator.validate(
            getData(dir),
            violation -> {
            }
        ));

    }

    @Test
    public void validateSimpleViolationMax() throws Exception {
        String dir = "simpleShacleViolationMax";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertFalse(shaclValidator.validate(
            getData(dir),
            violation -> {
            }
        ));

    }

    @Test
    public void validateSimpleViolationMax5() throws Exception {
        String dir = "simpleShacleViolationMax5";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertFalse(shaclValidator.validate(
            getData(dir),
            violation -> {
            }
        ));

    }

    @Test
    public void validateSimplePassMax5() throws Exception {
        String dir = "simpleShaclePassMax5";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir),
            violation -> {
            }
        ));

    }


    @Test
    public void simpleShaclePassBasedOnClass() throws Exception {
        String dir = "simpleShaclePassBasedOnClass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir),
            violation -> {
            }
        ));

    }


    @Test
    public void simpleShacleViolationMinBig() throws Exception {
        String dir = "simpleShacleViolationMinBig";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir),
            violation -> {
            }
        ));

    }


    @Test
    public void simpleShacleMinDatatypePass() throws Exception {
        String dir = "simpleShacleMinDatatypePass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir),
            violation -> {
            }
        ));

    }

    @Test
    public void simpleShaclClassViolation() throws Exception {
        String dir = "simpleShaclClassViolation";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertFalse(shaclValidator.validate(
                getData(dir),
                violation -> {
                }
        ));

    }

    @Test
    public void simpleShaclClassPass() throws Exception {
        String dir = "simpleShaclClassPass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
                getData(dir),
                violation -> {
                }
        ));

    }

    @Test
    public void simpleShaclClassBlankNodePass() throws Exception {
        String dir = "simpleShaclClassBlankNodePass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        assertTrue(shaclValidator.validate(
            getData(dir),
            violation -> {
            }
        ));

    }

    @Test
    public void simpleShaclClassViolation2() throws Exception {
        String dir = "simpleShaclClassViolation2";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);



        List<ConstraintViolation> violations = new ArrayList<>();

        assertFalse(shaclValidator.validate(
                getData(dir),
                violation -> {
                    violations.add(violation);
                    System.out.println(violation);
                }
        ));

        SimpleValueFactory instance = SimpleValueFactory.getInstance();

        List<ConstraintViolation> expectedViolations = new ArrayList<>();
        expectedViolations.add(new ConstraintViolationClass(null, instance.createIRI("http://example.org/1"), "Object is a literal, expected IRI."));

        assertEquals("", expectedViolations.size(), violations.size());
        assertTrue("", violations.containsAll(expectedViolations));
        assertTrue("", expectedViolations.containsAll(violations));

    }

    @Test
    public void simpleShacleMinDatatypeViolation() throws Exception {
        String dir = "simpleShacleMinDatatypeViolation";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        List<ConstraintViolation> violations = new ArrayList<>();

        assertFalse(shaclValidator.validate(
            getData(dir),
            violation -> {
                violations.add(violation);
                System.out.println(violation);
            }
        ));

        SimpleValueFactory instance = SimpleValueFactory.getInstance();

        List<ConstraintViolation> expectedViolations = new ArrayList<>();
        expectedViolations.add(new ConstraintViolationDatatype(null, instance.createIRI("http://example.org/1"), null, RDF.LANGSTRING));

        assertTrue("", violations.containsAll(expectedViolations));
        assertTrue("", expectedViolations.containsAll(violations));


    }

    @Test
    public void simpleShacleMinDatatypeViolation2() throws Exception {
        String dir = "simpleShacleMinDatatypeViolation2";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), null);


        new ArrayList<String>().stream().collect(Collectors.toList());


        assertFalse(shaclValidator.validate(
            getData(dir),
            (error) -> {
            }

        ));

    }

    @Test
    public void simpleShacleMinInferencePass() throws Exception {
        String dir = "simpleShacleMinInferencePass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), getOntology(dir));


        new ArrayList<String>().stream().collect(Collectors.toList());


        assertTrue(shaclValidator.validate(
            getData(dir),
            (error) -> {
            }

        ));

    }

    @Test
    public void simpleShacleMinInferenceViolation() throws Exception {
        String dir = "simpleShacleMinInferenceViolation";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), getOntology(dir));


        new ArrayList<String>().stream().collect(Collectors.toList());


        assertFalse(shaclValidator.validate(
            getData(dir),
            (error) -> {
                System.err.println(error);
            }

        ));

    }

    @Test
    public void simpleShaclClassInferencePass() throws Exception {
        String dir = "simpleShaclClassInferencePass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacle(dir), getOntology(dir));


        new ArrayList<String>().stream().collect(Collectors.toList());


        assertTrue(shaclValidator.validate(
            getData(dir),
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