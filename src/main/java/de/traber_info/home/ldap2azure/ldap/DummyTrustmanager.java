package de.traber_info.home.ldap2azure.ldap;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Dummy trust manager that accepts all X509 certificates without any checks.
 *
 * @author Oliver Traber
 */
public class DummyTrustmanager extends X509ExtendedTrustManager {

    public void checkClientTrusted(X509Certificate[] xcs, String string) {
        // do nothing
    }

    public void checkServerTrusted(X509Certificate[] xcs, String string) {
        // do nothing
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        // do nothing
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        // do nothing
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        // do nothing
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        // do nothing
    }

}
