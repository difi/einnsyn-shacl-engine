package no.difi.einnsyn.shacl_engine.rules.severity;

import no.difi.einnsyn.SHACL;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.List;

/**
 * Created by veronika on 5/26/16.
 *
 *
 */
public class Severity {

    private static ValueFactory factory = SimpleValueFactory.getInstance();

    public static Statement addDefaultSeverity(IRI subject) {
        return factory.createStatement(subject, SHACL.severity, SHACL.Violation);
    }

    public static Statement addInfoSeverity(IRI subject) {
        return factory.createStatement(subject, SHACL.severity, SHACL.Info);
    }

    public static Statement addWarningSeverity(IRI subject) {
        return factory.createStatement(subject, SHACL.severity, SHACL.Warning);
    }

    public static Statement addViolationSeverity(IRI subject) {
        return addDefaultSeverity(subject);
    }

    public static boolean checkForSeverity(List<Statement> statements) {
        final boolean[] severity = {true};
        statements.forEach(s -> {
            if(!s.getPredicate().equals(SHACL.severity)) {
                severity[0] = false;
            }
        });

        return severity[0];
    }
}
