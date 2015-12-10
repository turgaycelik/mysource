package com.atlassian.jira.pageobjects.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default websudo in JIRA will be disabled before each test. Mark test method or class with this annotation to
 * enable web sudo for given test methods.
 *
 * @since v4.4
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ( { ElementType.TYPE, ElementType.METHOD })
public @interface EnableWebSudo
{
}
