package no.difi.einnsyn.shacle_engine.validation;

import no.difi.einnsyn.sesameutils.SesameUtils;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by havardottestad on 04/05/16.
 */
public class SHACLValidatorTest {

    @Test
    public void validateNull() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator();
        assertFalse(shaclValidator.validate(null, null, null));

    }


    @Test
    public void validateEmptyRule() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator();

        assertTrue("Empty rules set and empty data should validate", shaclValidator.validate(
            SesameUtils.stringToRepository("", RDFFormat.TURTLE),
            SesameUtils.stringToRepository("", RDFFormat.TURTLE),
            ()->{}
        ));

    }


    @Test
    public void validateSimpleViolationMin() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator();

        String dir = "simpleShacleViolationMin";

        assertFalse(shaclValidator.validate(
            getShacle(dir),
            getData(dir),
            ()->{}
        ));

    }

    @Test
    public void validateSimpleViolationMax() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator();

        String dir = "simpleShacleViolationMax";

        assertFalse(shaclValidator.validate(
            getShacle(dir),
            getData(dir),
            ()->{}
        ));

    }

    @Test
    public void validateSimpleViolationMax5() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator();

        String dir = "simpleShacleViolationMax5";

        assertFalse(shaclValidator.validate(
            getShacle(dir),
            getData(dir),
            ()->{}
        ));

    }

    @Test
    public void validateSimplePassMax5() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator();

        String dir = "simpleShaclePassMax5";

        assertTrue(shaclValidator.validate(
            getShacle(dir),
            getData(dir),
            ()->{}
        ));

    }


    @Test
    public void simpleShaclePassBasedOnClass() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator();

        String dir = "simpleShaclePassBasedOnClass";

        assertTrue(shaclValidator.validate(
            getShacle(dir),
            getData(dir),
            ()->{}
        ));

    }
    private Repository getData(String simpleShacleViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(simpleShacleViolation + "/data.ttl");

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }

    private Repository getShacle(String simpleShacleViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(simpleShacleViolation + "/shacle.ttl");

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }

}