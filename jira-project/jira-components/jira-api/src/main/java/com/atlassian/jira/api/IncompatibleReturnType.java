package com.atlassian.jira.api;

import com.atlassian.annotations.ExperimentalApi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method's return type has been changed in a way that is not binary-compatible (and possibly also not
 * source-compatible).
 * <p/>
 * This annotation should only be used when <b>only the return type has changed</b>, but the method signature has
 * remained unchanged.
 */
@Documented
@ExperimentalApi
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.SOURCE)
public @interface IncompatibleReturnType
{
    /**
     * @return the JIRA version when the incompatible change was made
     */
    String since();

    /**
     * @return the name of the previous return type
     */
    String was();
}
