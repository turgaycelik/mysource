package com.atlassian.jira.junit.rules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate mocks in your unit test with this annotation to have them available via
 * {@link com.atlassian.jira.component.ComponentAccessor} in your unit tests.
 *
 * @since 5.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AvailableInContainer
{
    Class<?> interfaceClass() default Object.class;
    boolean instantiateMe() default false;
}
