package no.difi.einnsyn.shacl_engine.rules.propertyconstraints;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;
import no.difi.einnsyn.shacl_engine.violations.*;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;

import java.util.List;
import java.util.Optional;

/**
 * Created by havardottestad on 04/05/16.
 *
 *
 */
public class MinMax extends PropertyConstraint {

    protected Optional<Integer> minCount;
    protected Optional<Integer> maxCount;
    private long count;

    public MinMax(Resource object, RepositoryConnection shapes) {
        super(object, shapes);

        this.minCount = SesameUtils.getOptionalOneInteger(shapes, object, SHACL.minCount);
        this.maxCount = SesameUtils.getOptionalOneInteger(shapes, object, SHACL.maxCount);
    }

    public Optional<Integer> getMinCount() {
        return this.minCount;
    }
    public Optional<Integer> getMaxCount() {
        return this.maxCount;
    }
    public long getCount() { return this.count; }

    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {

        count = list.stream()

            .filter(statement -> statement.getPredicate().equals(predicate))
            .count();

        if (maxCount.isPresent()) {
            if (maxCount.get() < count) {
                constraintViolationHandler.handle(new ConstraintViolationMaxCount(this, resource, "was " + count));
            }
        }

        if (minCount.isPresent()) {
            if (minCount.get() > count) {
                constraintViolationHandler.handle(new ConstraintViolationMinCount(this, resource, "was " + count));
            }
        }
    }

    @Override
    public String toString() {
        return "PropertyConstraint {" +
            ", predicate=" + predicate +
            ", minCount=" + minCount +
            ", maxCount=" + maxCount +
            '}';
    }
}
