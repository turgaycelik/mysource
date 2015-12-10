package com.atlassian.jira.functest.framework.suite;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JIRA web test.
 *
 * @since 4.4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.METHOD })
public @interface WebTest
{

    /**
     * Optional list of categories describing this test.
     *
     * @return categories describing this test
     */
    Category[] value();
}
