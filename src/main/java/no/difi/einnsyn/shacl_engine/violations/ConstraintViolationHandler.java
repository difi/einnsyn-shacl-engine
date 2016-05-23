package no.difi.einnsyn.shacl_engine.violations;

import com.github.jsonldjava.core.JsonLdError;

/**
 * Created by havardottestad on 04/05/16.
 *
 *
 */
public interface ConstraintViolationHandler {

    void handle(ConstraintViolation violation) throws JsonLdError;
}
