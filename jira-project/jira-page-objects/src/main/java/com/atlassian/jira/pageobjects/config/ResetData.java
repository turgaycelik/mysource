package com.atlassian.jira.pageobjects.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p/>
 * Annotate your test class or test method with this annotation if you want the data on the tested JIRA
 * instance to be reset to blank state before given test class/test method executes.
 *
 * <p/>
 * If a class is annotated with this annotation, the restore will happen for each test method in the class.
 * If a test method is annotated - the restore will happen before that particular test only.
 *
 * <p/>
 * NOTE: This annotation cannot be mixed with other &#64;Restore annotations either on method, or class level. That means
 * that for given test there must only ever be one &#64;Reset or &#64;Restore-style annotation, or an error will be
 * raised. This is to encourage developers to explicitly choose between the restore-once, or restore-for-each-test
 * approach for each test suite.
 *
 * @see ResetDataOnce
 * @since 5.2
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResetData
{
}
