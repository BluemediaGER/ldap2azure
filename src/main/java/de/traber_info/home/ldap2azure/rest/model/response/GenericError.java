package de.traber_info.home.ldap2azure.rest.model.response;

/**
 * Generic response used to respond when various errors occur.
 */
public class GenericError {

    /** Error that occurred */
    public String error;

    /** Description that contains additional information about the error */
    public String message;

}
