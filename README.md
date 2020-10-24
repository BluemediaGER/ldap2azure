# ldap2azure

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/3688138debf8442ea56998c5c3aca15b)](https://www.codacy.com/manual/BluemediaGER/ldap2azure?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BluemediaGER/ldap2azure&amp;utm_campaign=Badge_Grade) ![Maven Build](https://github.com/BluemediaGER/ldap2azure/workflows/Maven%20Build%20Test/badge.svg?branch=master&event=push)

ldap2azure is a simple tool for automatically synchronizing and updating users from any standard LDAP to an Azure Active Directory.  

ldap2azure ensures that all changes to LDAP objects are automatically synchronized to Azure AD. All properties of a user can be assembled from a mixture of fixed values and placeholders if required, making the synchronization very flexible.

## What ldap2azure can and cannot do

ldap2azure can create simple user objects for the use with a SAML 2.0 identity provider such as [Keycloak](https://www.keycloak.org/).  
ldaps2azure is **not** designed to synchronize complete directory structures including groups, group memberships and resources, or users including their password hashes. A synchronization into the other direction, i.e. from Azure into an LDAP, is also not intended.  
If your environment requires such functionality, or you use Microsoft Active Directory, take a look at [Azure AD Connect](https://www.codetwo.com/admins-blog/how-to-sync-on-premises-active-directory-to-azure-active-directory-with-azure-ad-connect/) instead.

## Configuration
  
The path of the configuration file is defined in the environment variable LDAP2AZURE_CONFIG. The specified folder must contain the configuration file with the name "config.json".
If the environment variable is not set, the configuration is searched as a fallback in the folder where the .jar file is located.  
Below is an example configuration that synchronizes users from an Samba 4 Active Directory:
```json
{
  "general": {
    "syncCronExpression": "0 0/30 * ? * * *",
    "debuggingEnabled": false
  },
  "msGraph": {
    "msGraphTenantSpecificAuthority": "https://login.microsoftonline.com/<your-tenant-id-here>/",
    "msGraphClientId": "<your-client-id-here>",
    "msGraphClientSecret": "<your-client-secret-here>",
    "usageLocation": "DE",
    "deleteBehavior": "SOFT"
  },
  "ldap": {
    "ldapUrl": "ldaps://dc1.example.com",
    "ldapBindUser": "CN=ServiceUser,CN=Users,DC=example,DC=com",
    "ldapBindPassword": "VerySecurePassword1234",
    "ldapSearchBase": "CN=Users,DC=example,DC=com",
    "ldapSearchFilter": "(&(objectClass=user)(memberof=CN=AzureSyncUser,CN=Groups,DC=example,DC=com))",
    "ldapAttributes": [
      {
        "attributeName": "givenName",
        "binary": false
      },
      {
        "attributeName": "sn",
        "binary": false
      },
      {
        "attributeName": "displayName",
        "binary": false
      },
      {
        "attributeName": "sAMAccountName",
        "binary": false
      },
      {
        "attributeName": "objectGUID",
        "binary": true
      }
    ],
    "ignoreSSLErrors": false
  },
  "userBuildPattern": {
    "givenNamePattern": "{givenName}",
    "surnamePattern": "{sn}",
    "displayNamePattern": "{displayName}",
    "onPremisesImmutableIdPattern": "{objectGUID}",
    "mailNicknamePattern": "{sAMAccountName}",
    "userPrincipalNamePattern": "{sAMAccountName}@example.com"
  },
  "autoLicensing": {
    "featureEnabled": true,
    "defaultLicenseSkuId": "19dceafd-77c1-4db5-b36b-3cc602144b04"
  },
  "web": {
    "featureEnabled": false,
    "port": 8080,
    "password": "VerySecurePassword1234"
  }
}
```  
All sections of the config file are explained in detail below.

### The "general" section
The "general" section contains all parameters that are not directly required for querying, processing and changing data.

| Key | Descripction | Example Value |
|:----|:-------------|:--------------|
| syncCronExpression | Cron expression, when the SyncJob should be executed | 0 0/30 * ? * * * |
| debuggingEnabled | If true enables an integrated H2 console on port 8082 to debug the internal database | false |
| databaseJDBCUrl | (optional) JDBC url for the use of an external database such as MySQL. If not set the internal H2 database is used. | mysql://user:password@localhost/ldap2azure?useSSL=false

### The "msGraph" section
The "msGraph" section contains all information required to connect to the Microsoft Graph API. The application under which ldap2azure runs must be a daemon application and have the Microsoft Graph permission 
"Directory.ReadWrite.All" and "User.ReadWrite.All".  
More about the creation of a Microsoft Graph daemon application can be found [here](https://docs.microsoft.com/en-us/azure/active-directory/develop/scenario-daemon-app-registration).  

| Key | Description | Example Value |
|:----|:------------|:--------------|
| msGraphTenantSpecificAuthority | Tenant specific OAuth 2.0 endpoint against which the application authenticates | https://login.microsoftonline.com/7a53db06-b010-4aef-ba79-337d9d2f29bb/ |
| msGraphClientId | ID of the Microsoft Graph Application that should be used | 641e2b36-0bde-46af-b896-545532c0ba03 |
| msGraphClientSecret | Secret, which was generated for the application in Azure AD | Vo:MfG.AHK[eIwO?QhpdQ5mz0p8cG3L- |
| usageLocation | Two character location code of the country new user accounts will be used in | DE |
| deleteBehavior | If set to HARD, users will be deleted completely if they are removed from the source ldap. If set to SOFT they will be moved to the recycling bin instead and will be deleted by Azure AD after 30 days | HARD |

### The "ldap" section
The "ldap" section contains all information on how and which data is retrieved from the source LDAP.  
If an LDAPS connection is used and the server only uses a self-signed certificate, this certificate must be added to the default Java Keystore for the connection to work correctly.
Alternatively, any certificate can be accepted in test environments. However, it is strongly recommended not to do this in production environments.  

| Key | Description | Example value |
|:----|:------------|:--------------|
| ldapUrl | Connection string used to connect to the source LDAP server | ldaps://dc1.example.com |
| ldapBindUser | Identity that is used to bind to the source LDAP server | CN=ServiceUser,CN=Users,DC=example,DC=com |
| ldapBindPassword | Password used to authenticate as the bind user | SomeSecurePassword1234 |
| ldapSearchBase | DN of the base container in which the users to be synchronized are located | CN=Users,DC=example,DC=com |
| ldapSearchFilter | LDAP filter to narrow down the objects to be synchronized | (&(objectClass=user)(memberof=CN=AzureSyncUser,CN=Groups,DC=example,DC=com)) |
| ignoreSSLErrors | If true, certificate errors are ignored for LDAPS connections | false |
| ldapAttributes | Array containing the LDAP attributes to be loaded, which can later be used in the pattern configuration | see below |

#### Ldap attribute
Defines an attribute in the source LDAP, which can later be used in the pattern configuration.

| Key | Description | Example value |
|:----|:------------|:--------------|
| attributeName | Name of the attribute in the source ldap | objectGUID |
| binary | Set whether the attribute is binary. If so, it will be converted to a Base64 encoded string | true |

### The "userBuildPattern" section
The "userBuildPattern" section contains the templates according to which the Azure user objects are created. Both fixed values and placeholders for values read from the LDAP can be used. A mixture of both is of course also possible.  
An exemplary configuration which uses placeholders combined with fixed values is shown in the example configuration file above.  

| Key | Description | Example value |
|:----|:------------|:--------------|
| givenNamePattern | Template for composing the given name of a user | {givenName} |
| surnamePattern | Template for composing the surname name of a user | {sn} |
| displayNamePattern | Template for composing the display name of a user | {displayName} |
| onPremisesImmutableIdPattern | Template for composing the on premises immutable id of a user. This value must be unique throughout your Azure AD. For a Federation with a SAML 2.0 Identity Provider, this value must be returned as NameID. | {objectGUID} |
| mailNicknamePattern | Template for composing the mail nickname of a user | {sAMAccountName} |
| userPrincipalNamePattern | Template for composing the user principal name of a user. This value must be unique throughout your Azure AD. | {sAMAccountName}@example.com |

### The "autoLicensing" section
ldap2azure offers the possibility to automatically assign a license to new users that are being synchronized. This feature is optional, but recommended for an automated workflow.  

| Key | Description | Example value |
|:----|:------------| :-------------|
| featureEnabled | Can be set true to enable the function or false to disable it | true |
| defaultLicenseSkuId | GUID of the license to be used by default. More can be read [here](https://docs.microsoft.com/en-us/azure/active-directory/users-groups-roles/licensing-service-plan-reference) | 05e9a617-0261-4cee-bb44-138d3ef5d965 |

### The "web" section
ldap2azure is equipped with a RESTful API and the possibility to provide a web frontend.  
Once the feature is enabled, ldap2azure will create a default api user the first time it is started. The credentials are displayed in the console or log.

To provide a frontend, it must be located in a folder called "web-frontend" in the same folder as the JAR file of ldap2azure. The "web-frontend" folder must also contain at least an index.html file. If this is the case, it is automatically mounted at application startup.

My personal implementation of a frontend can be found on my GitHub account: [ldap2azure-frontend](https://github.com/BluemediaGER/ldap2azure-frontend)  
The definition of the RESTful API is available at [https://bluemediager.github.io/ldap2azure](https://bluemediager.github.io/ldap2azure).

| Key | Description | Example value |
|:----|:------------| :-------------|
| featureEnabled | Can be set true to enable the function or false to disable it | true |
| port | Sets the port under which the API and the management interface can be reached | 8080 |
| password | Password, which is required to access the management interface | VerySecurePassword1234 |

## Logging
By default, logs are stored in the "log" subdirectory of the directory you are running ldap2azure from. The log files are automatically rotated every 24 hours and archived as .gz files. All logs older than 30 days are automatically deleted.
Log files are also deleted if the log folder exceeds 3 GB in size.  

The path for log files can be adjusted with the parameter "-DLOG_DIR=/path/to/logs"  
The name for log files can be adjusted with the parameter "-DLOG_NAME=myCustomLogName"

## Built With  
  
- [Maven](https://maven.apache.org/) - Dependency Management 
- [H2](https://h2database.com/) - Java SQL database (Used as in memory cache)
- [OrmLite](http://ormlite.com/) - Lightweight Object Relational Mapping Java Package
- [Jackson-Databind](https://github.com/FasterXML/jackson-databind) - FasterXML Jackson object mapper
- [Microsoft Graph SDK for Java](https://github.com/microsoftgraph/msgraph-sdk-java) - SDK for the Microsoft Graph API
- [MSAL4J](https://github.com/AzureAD/microsoft-authentication-library-for-java) - Microsoft Authentication Library (MSAL) for Java
- [Quartz](http://www.quartz-scheduler.org/) - Quartz Job Scheduler
- [LOGBack](http://logback.qos.ch/) - Java Logging Framework

## Contributors  
  
- [Oliver Traber](https://github.com/BluemediaGER)  

## License  
  
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for details
