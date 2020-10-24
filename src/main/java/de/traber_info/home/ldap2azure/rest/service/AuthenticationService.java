package de.traber_info.home.ldap2azure.rest.service;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.rest.exception.BadRequestException;
import de.traber_info.home.ldap2azure.rest.exception.NotFoundException;
import de.traber_info.home.ldap2azure.rest.model.object.ApiKey;
import de.traber_info.home.ldap2azure.rest.model.object.ApiSession;
import de.traber_info.home.ldap2azure.rest.model.object.ApiUser;
import de.traber_info.home.ldap2azure.rest.model.request.ApiKeyCreateRequest;
import de.traber_info.home.ldap2azure.rest.model.request.ApiUserCreateRequest;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Service used to manage all actions needed for authorisation and access management.
 *
 * @author Oliver Traber
 */
public class AuthenticationService {

    /**
     * Set how long a session is valid after last api action.
     */
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    /**
     * Validate the credentials send by the client.
     * @param username Username that was send by the client.
     * @param password Password that was send by the client
     * @return true if the supplied credentials are correct, otherwise false.
     */
    public static boolean validateCredentials(String username, String password) {
        ApiUser user = H2Helper.getApiUserDao().getByAttributeMatch("username", username);
        if (user == null) return false;
        if (user.validatePassword(password)) {
            user.resetLastLoginTime();
            H2Helper.getApiUserDao().update(user);
            return true;
        }
        return false;
    }

    /**
     * Issue an new {@link ApiSession} and persist it in the database.
     * @return Session key used by the client to identify itself.
     */
    public static String issueSession(String username) {
        ApiUser user = H2Helper.getApiUserDao().getByAttributeMatch("username", username);
        ApiSession session = new ApiSession(user.getId());
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
    public static ApiKey createApiKey(ApiKeyCreateRequest request) {
        ApiKey checkKey = H2Helper.getApiKeyDao().getByAttributeMatch("keyName", request.getKeyName());
        if (checkKey != null) throw new BadRequestException("keyname_already_existing");
        ApiKey key = new ApiKey(request.getKeyName(), request.getPermission());
        H2Helper.getApiKeyDao().persist(key);
        return key;
    }

    /**
     * Get an {@link ApiKey} by it's secret key.
     * @return Retrieved {@link ApiKey}.
     */
    public static ApiKey getApiKey(String authenticationHeader) {
        String authenticationKey = authenticationHeader.replace("Bearer " , "");
        return H2Helper.getApiKeyDao().getByAttributeMatch("authenticationKey", authenticationKey);
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

    /**
     * Create an new {@link ApiUser} and persist it to the database.
     * @param request {@link ApiUserCreateRequest} containing the details for the new user.
     * @return Newly created {@link ApiUser}.
     */
    public static ApiUser createApiUser(ApiUserCreateRequest request) {
        ApiUser user = H2Helper.getApiUserDao().getByAttributeMatch("username", request.getUsername());
        if (user != null) throw new BadRequestException("username_already_existing");
        ApiUser newUser = new ApiUser(request.getUsername(), request.getPassword(), request.getPermission());
        H2Helper.getApiUserDao().persist(newUser);
        return newUser;
    }

    /**
     * Get an {@link ApiUser} by the key of an {@link ApiSession}.
     * @param sessionKey Session key of an {@link ApiSession}.
     * @return Parent {@link ApiUser} of the given {@link ApiSession}.
     */
    public static ApiUser getApiUserBySession(String sessionKey) {
        ApiSession session = H2Helper.getApiSessionDao().getByAttributeMatch("sessionKey", sessionKey);
        return H2Helper.getApiUserDao().getByAttributeMatch("id", session.getParentApiUserId());
    }

    /**
     * Update the password of an logged in {@link ApiUser} using his session key.
     * @param sessionCookie Key of the users {@link ApiSession}.
     * @param password New password that should be set.
     * @return {@link ApiUser} if the change was successful.
     */
    public static ApiUser updateApiUserPassword(String sessionCookie, String password) {
        ApiUser user = getApiUserBySession(sessionCookie);
        user.updatePassword(password);
        H2Helper.getApiUserDao().update(user);
        return user;
    }

    /**
     * Update the password of an logged in {@link ApiUser} using his id.
     * @param userId Id of the user the password should be changed for.
     * @param password New password that should be set.
     * @return {@link ApiUser} if the change was successful.
     */
    public static ApiUser updateApiUserPasswordById(String userId, String password) {
        ApiUser user = H2Helper.getApiUserDao().getByAttributeMatch("id", userId);
        if (user == null) throw new NotFoundException("user_not_existing");
        user.updatePassword(password);
        H2Helper.getApiUserDao().update(user);
        return user;
    }

    /**
     * Delete an {@link ApiUser} from the database.
     * @param sessCookie Session key to prevent an user from deleting himself.
     * @param userId Id of the {@link ApiUser} that should be deleted.
     * @return {@link Response} containing an empty array and status 200 if the operation was successful.
     */
    public static Response deleteApiUser(Cookie sessCookie, String userId) {
        if (sessCookie != null) {
            ApiUser sessUser = AuthenticationService.getApiUserBySession(sessCookie.getValue());
            if (userId.equals(sessUser.getId())) throw new BadRequestException("cant_delete_own_user");
        }
        ApiUser user = H2Helper.getApiUserDao().getByAttributeMatch("id", userId);
        if (user == null) throw new NotFoundException("user_not_existing");
        try {
            H2Helper.getApiSessionDao().deleteAllByParentApiUserId(user.getId());
        } catch (SQLException ex) {
            // Do nothing
        }
        H2Helper.getApiUserDao().delete(user);
        return Response.ok().entity("[]").build();
    }

}
