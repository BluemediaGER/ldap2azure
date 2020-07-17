package de.traber_info.home.ldap2azure.rest.service;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.rest.exception.BadRequestException;
import de.traber_info.home.ldap2azure.rest.exception.NotFoundException;
import de.traber_info.home.ldap2azure.rest.model.object.ApiKey;
import de.traber_info.home.ldap2azure.rest.model.object.ApiSession;
import de.traber_info.home.ldap2azure.util.ConfigUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service used to manage {@link ApiSession} objects and validate the web consoles password.
 *
 * @author Oliver Traber
 */
public class AuthenticationService {

    /**
     * Set how long a session is valid after last api action.
     */
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    /**
     * Validate the password send by the client.
     * @param password Password that was send by the client
     * @return true if the supplied password is correct, otherwise false.
     */
    public static boolean validatePassword(String password) {
        if (password == null) return false;
        return ConfigUtil.getConfig().getWebConfig().getPassword().equals(password);
    }

    /**
     * Issue an new {@link ApiSession} and persist it in the database.
     * @return Session key used by the client to identify itself.
     */
    public static String issueSession() {
        ApiSession session = new ApiSession();
        H2Helper.getApiSessionDao().persist(session);
        return session.getSessionKey();
    }

    /**
     * Check if an session key is valid and allowed to perform REST actions.
     * @param sessionKey Session key that should be validated.
     * @return true if the session key is valid, otherwise false.
     */
    public static boolean validateSession(String sessionKey) {
        if (sessionKey == null) return false;
        ApiSession session = H2Helper.getApiSessionDao().getByAttributeMatch("sessionKey", sessionKey);
        if (session == null) return false;
        if (session.getLastAccessTime().plusMinutes(SESSION_TIMEOUT_MINUTES).isBefore(LocalDateTime.now())) {
            H2Helper.getApiSessionDao().delete(session);
            return false;
        }
        session.resetLasAccessTime();
        H2Helper.getApiSessionDao().update(session);
        return true;
    }

    /**
     * Invalidate the given session key and remove the corresponding {@link ApiSession} from the database.
     * @param sessionKey Session key of the session that should be invalidated.
     */
    public static void invalidateSession(String sessionKey) {
        ApiSession session = H2Helper.getApiSessionDao().getByAttributeMatch("sessionKey", sessionKey);
        if (session != null) {
            H2Helper.getApiSessionDao().delete(session);
        }
    }

    /**
     * Create an new {@link ApiKey} and persist it in the database.
     * @return Generated {@link ApiKey}.
     */
    public static ApiKey createApiKey(String keyName) {
        ApiKey checkKey = H2Helper.getApiKeyDao().getByAttributeMatch("keyName", keyName);
        if (checkKey != null) throw new BadRequestException("keyname_already_existing");
        ApiKey key = new ApiKey(keyName);
        H2Helper.getApiKeyDao().persist(key);
        return key;
    }

    /**
     * Get a list of all {@link ApiKey} currently saved in the database.
     * @return List of {@link ApiKey} currently saved in the database.
     */
    public static List<ApiKey> getApiKeys() {
        return H2Helper.getApiKeyDao().getAll();
    }

    /**
     * Get the authentication key from the {@link ApiKey} using the given id.
     * @param keyId Id of the {@link ApiKey} the authentication key should be got from.
     * @return Authentication key corresponding to the given {@link ApiKey}.
     */
    public static String getApiKeySecret(String keyId) {
        ApiKey key = H2Helper.getApiKeyDao().getByAttributeMatch("id", keyId);
        if (key == null) throw new NotFoundException("keyid_not_existing");
        return key.getAuthenticationKey();
    }

    /**
     * Check if an api key is valid and allowed to perform REST actions.
     * @param authenticationHeader Authentication HTTP header containing the api key that should be validated.
     * @return true if the api key is valid, otherwise false.
     */
    public static boolean validateApiKey(String authenticationHeader) {
        if (authenticationHeader == null) return false;
        String authenticationKey = authenticationHeader.replace("Bearer " , "");
        ApiKey apiKey = H2Helper.getApiKeyDao().getByAttributeMatch("authenticationKey", authenticationKey);
        if (apiKey == null) return false;
        apiKey.resetLastAccessTime();
        H2Helper.getApiKeyDao().update(apiKey);
        return true;
    }

    /**
     * Delete the {@link ApiKey} with the given id from the database.
     * @param keyId Id of the {@link ApiKey} that should be deleted.
     */
    public static void deleteApiKey(String keyId) {
        ApiKey key = H2Helper.getApiKeyDao().getByAttributeMatch("id", keyId);
        if (key == null) throw new NotFoundException("keyid_not_existing");
        H2Helper.getApiKeyDao().delete(key);
    }

}
