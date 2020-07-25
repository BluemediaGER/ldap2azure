package de.traber_info.home.ldap2azure.rest.model.response;

/**
 * Generic response used to respond when various errors occur.
 */
public class GenericError {

    /** Error that occurred */
    public String error;

    /** Description that contains additional information about the error */
    public String message;

    /** Default constructor */
    public GenericError() {}

    /**
     * Create an new prefilled {@link GenericError}.
     * @param error Machine readable error code.
     * @param message Error message containing further details for manual review.
     */
    public GenericError(String error, String message) {
        this.error = error;
        this.message = message;
    }

}
