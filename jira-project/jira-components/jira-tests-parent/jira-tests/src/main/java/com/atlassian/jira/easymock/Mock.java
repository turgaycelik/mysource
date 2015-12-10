package com.atlassian.jira.easymock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a given test field as a mock.
 *
 * @since 4.4
 */
@Target (ElementType.FIELD)
@Retention (RetentionPolicy.RUNTIME)
public @interface Mock
{
    /**
     * Indicates the type of mock that is used.
     *
     * @return a MockType
     */
    MockType value() default MockType.NICE;
}
