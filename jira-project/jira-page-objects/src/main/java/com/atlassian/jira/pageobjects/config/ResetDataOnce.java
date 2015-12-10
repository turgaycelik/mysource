package com.atlassian.jira.pageobjects.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p/>
 * Annotate your test class with this annotation if you want to reset data in JIRA once per the test suite
 * represented by the class.
 *
 * <p/>
 * Only applicable to the test class.
 *
 * @since 5.2
 * @see com.atlassian.jira.pageobjects.config.junit4.rule.RestoreDataClassRule
 */
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface ResetDataOnce
{
}
