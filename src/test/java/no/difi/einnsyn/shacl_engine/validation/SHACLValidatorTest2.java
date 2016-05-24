package no.difi.einnsyn.shacl_engine.validation;

import no.difi.einnsyn.sesameutils.SesameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class SHACLValidatorTest2 {

    String path;

    public SHACLValidatorTest2(String path) {
        this.path = path;
    }

    final static String TEST_DATA = "testData/";

    @Parameterized.Parameters(name = "{0}")
    public static Collection<String> data() {


        List<String> ret = new ArrayList<>();

        String file = SHACLValidatorTest.class.getClassLoader().getResource(TEST_DATA).getFile();

        File file1 = new File(file);
        for (File file2 : file1.listFiles()) {
            ret.add(file2.getAbsolutePath().split(TEST_DATA)[1]);
        }

        return ret;
    }


    @Test
    public void test() throws IOException {

        boolean ranAtLeastOneTest = false;

        ranAtLeastOneTest = runTestsOnAllFiles(ranAtLeastOneTest, false);

        ranAtLeastOneTest = runTestsOnAllFiles(ranAtLeastOneTest, true);

        assertTrue("There must be at least one pass or fail data.ttl file to test against.", ranAtLeastOneTest);

    }

    private boolean runTestsOnAllFiles(boolean ranAtLeastOneTest, boolean shouldPass) throws IOException {
        for (int i = 0; i < 100; i++) {
            String index = i + "";
            if (i == 0) {
                index = "";
            }

            String state = "fail";
            if(shouldPass){
                state = "pass";
            }

            String realPath = path +"/"+state + index;

            if (getData(realPath) != null) {
                System.out.println("Testing: " + realPath);
                SHACLValidator shaclValidator = new SHACLValidator(getShacle(path), getOntology(realPath));

                assertEquals("", shouldPass, shaclValidator.validate(getData(realPath), (violation) -> {
                    System.out.println(violation);
                }));

                ranAtLeastOneTest = true;

            }
        }
        return ranAtLeastOneTest;
    }


    private Repository getShacle(String simpleShacleViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(TEST_DATA + simpleShacleViolation + "/shacl.ttl");

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }


    private Repository getOntology(String simpleShacleViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(TEST_DATA + simpleShacleViolation + "/ontology.ttl");
        if (resourceAsStream == null) {
            return null;
        }

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }

    private Repository getData(String simpleShacleViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(TEST_DATA + simpleShacleViolation + "/data.ttl");

        if (resourceAsStream == null) {
            return null;
        }
        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }

}
