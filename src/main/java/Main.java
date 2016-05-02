import validation.SHACLValidator;

import java.io.IOException;

/**
 * Created by veronika on 4/28/16.
 *
 *
 */
public class Main {

    public static void main(String[] args) {

        try {
            SHACLValidator.parseShapePropertyConstraints("shapesGraph.ttl", "dataGraph.ttl");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
