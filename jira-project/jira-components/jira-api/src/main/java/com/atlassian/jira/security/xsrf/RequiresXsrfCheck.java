package com.atlassian.jira.security.xsrf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation used to indicate that a method needs XSRF protection checking
 *
 * @since v4.1
 */
@Retention (value= RetentionPolicy.RUNTIME)
@Target (value= ElementType.METHOD)
public @interface RequiresXsrfCheck {
}
