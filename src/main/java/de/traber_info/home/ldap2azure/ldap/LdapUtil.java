package de.traber_info.home.ldap2azure.ldap;

import de.traber_info.home.ldap2azure.model.config.LdapAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Utility used to connect to and read objects from the source ldap server.
 *
 * @author Oliver Traber
 */
public class LdapUtil {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(LdapUtil.class.getName());

    /** DirContext used to connect to the target LDAP server */
    private DirContext dirContext;

    /** Search controls used to configure how an search takes place */
    private SearchControls searchCtls;

    /** Base DN which is searched for objects */
    private String searchBase;

    /** LDAP filter string used to filter for specific requirements */
    private String searchFilter;

    /**
     * Create a new instance of the LdapDirectory class.
     * @param ldapUrl Url of the LDAP server the directory context should be bind to.
     * @param bindUser User (full CN) the context should use for binding to the server.
     * @param bindPassword Password used for authenticating against the ldap server.
     * @param searchBase Base DN from which user objects should be read.
     * @param searchFilter LDAP-Filter to filter for LDAP-Objects that should be synced.
     * @param returnAttributes Array of LDAP-Attribute-Names that should be used to build Azure user attributes.
     * @param ignoreSSLValidation Set true to ignore all SSL-Errors and accept all certificates. Warning! Not recommended for production use.
     * @throws NamingException Thrown either if the creation of the DirContext fails, or if errors occur while retrieving the users.
     */
    public LdapUtil(String ldapUrl, String bindUser, String bindPassword, String searchBase, String searchFilter,
                    List<LdapAttribute> returnAttributes, boolean ignoreSSLValidation) throws NamingException {

        ArrayList<String> attributeNames = new ArrayList<>();
        ArrayList<String> binaryAttributes = new ArrayList<>();

        for (LdapAttribute attribute : returnAttributes) {
            attributeNames.add(attribute.getAttributeName());
            if (attribute.isBinary()) {
                binaryAttributes.add(attribute.getAttributeName());
            }
        }

        // Declare class variables
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(Context.PROVIDER_URL, ldapUrl);
        properties.put(Context.SECURITY_PRINCIPAL, bindUser);
        properties.put(Context.SECURITY_CREDENTIALS, bindPassword);
        properties.put(Context.SECURITY_AUTHENTICATION, "simple");
        // Specify attributes that are binary so they are loaded as byte[] instead of String
        properties.put("java.naming.ldap.attributes.binary", String.join(" ", binaryAttributes));
        if (ignoreSSLValidation) properties.put("java.naming.ldap.factory.socket", "de.traber_info.home.ldap2azure.ldap.AcceptAllSSLSocketFactory");

        this.searchBase = searchBase;
        this.searchFilter = searchFilter;

        dirContext = new InitialDirContext(properties);

        // initializing search controls
        searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(attributeNames.toArray(new String[0]));
    }

    /**
     * Get all objects from the ldap server that match the specified filter and are located in the specified base DN.
     * @return NamingEnumeration containing all matched objects.
     * @throws NamingException Thrown if errors occur while retrieving the objects.
     */
    public NamingEnumeration<SearchResult> search() throws NamingException {
        return dirContext.search(searchBase, searchFilter, searchCtls);
    }

    /**
     * Closes the LDAP connection.
     */
    public void close() {
        try {
            if(dirContext != null)
                dirContext.close();
        }
        catch (NamingException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

}
