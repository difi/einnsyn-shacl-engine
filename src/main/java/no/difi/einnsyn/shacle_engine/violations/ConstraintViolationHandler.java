package no.difi.einnsyn.shacle_engine.violations;

/**
 * Created by havardottestad on 04/05/16.
 *
 *
 */
public interface ConstraintViolationHandler {

    void handle(ConstraintViolation violation);
}
