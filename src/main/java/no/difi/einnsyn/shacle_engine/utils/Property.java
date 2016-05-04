package no.difi.einnsyn.shacle_engine.utils;

import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import no.difi.einnsyn.shacle_engine.vocabulary.SHACL;

import java.util.Map;

/**
 * Created by veronika on 4/29/16.
 *
 */
public class Property {

    private Value predicate;
    private Map<IRI, Value> constraints;

    public void setPredicate(Value predicate) {
        this.predicate = predicate;
    }

    public void setConstraints(Map<IRI, Value> constraints) {
        this.constraints = constraints;
    }

    public Value getPredicate() {
        return this.predicate;
    }

    public Map<IRI, Value> getConstraints() {
        return this.constraints;
    }

    public int getMinCount() {

        for (Map.Entry<IRI, Value> entry : constraints.entrySet()) {
            if (entry.getKey().equals(SHACL.minCount)) {
                return Integer.parseInt(entry.getValue().stringValue());
            }
        }
        return 0;
    }

    public int getMaxCount() {
        for (Map.Entry<IRI, Value> entry : constraints.entrySet()) {
            if (entry.getKey().equals(SHACL.maxCount)) {
                return Integer.parseInt(entry.getValue().stringValue());
            }
        }
        return 0;
    }

    public IRI getSeverity() {
        for (Map.Entry<IRI, Value> entry : constraints.entrySet()) {
            if (entry.getKey().equals(SHACL.severity)) {
                return (IRI) entry.getValue();
            }
        }
        return null;
    }

    public String getDatatype() {
        for (Map.Entry<IRI, Value> entry : constraints.entrySet()) {
            if (entry.getKey().equals(SHACL.datatype)) {
                return entry.getValue().stringValue();
            }
        }
        return null;
    }

    public void printInformation() {
        System.out.println("Predicate: " + this.predicate);
        System.out.println("Constraints: ");
        for (Map.Entry<IRI, Value> entry : constraints.entrySet()) {
            System.out.println(entry.getKey() + " // " + entry.getValue());
        }
    }
}
