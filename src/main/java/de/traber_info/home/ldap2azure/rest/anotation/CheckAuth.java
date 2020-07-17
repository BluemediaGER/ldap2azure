package de.traber_info.home.ldap2azure.rest.anotation;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to bind the {@link de.traber_info.home.ldap2azure.rest.filter.AuthenticationFilter} to REST methods.
 *
 * @author Oliver Traber
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CheckAuth {}
