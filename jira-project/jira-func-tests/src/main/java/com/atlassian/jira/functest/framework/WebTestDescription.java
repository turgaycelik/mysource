package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.suite.Category;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Represents basic information about a running web test
 *
 * @since v4.4
 */
public interface WebTestDescription
{

    /**
     * Display name of the test.
     *
     * @return name of the test
     */
    String name();

    /**
     * Test class name, or <code>null</code> if this is neither a single test, nor a suite constructed for a test class.
     *
     * @return test class name
     */
    String className();

    /**
     * Test method name, or <code>null</code> if this is not a single test.
     *
     * @return method name
     */
    String methodName();

    /**
     * Test class, or <code>null</code> if this is neither a single test, nor a suite constructed for a test class..
     *
     * @return test class
     */
    Class<?> testClass();

    /**
     * List of annotations of the test.
     *
     * @return test annotations
     */
    Iterable<Annotation> annotations();

    /**
     * Set of categories describing this test.
     *
     * @return categories of this test
     */
    Set<Category> categories();

    /**
     * Is it a single test?
     *
     * @return <code>true</code>, if this description describes a single test
     */
    boolean isTest();

    /**
     * Is it a test suite?
     *
     * @return <code>true</code>, if this description describes a test suite, i.e. when {@link #isTest()}
     * returns <code>false</code>
     */
    boolean isSuite();

    /**
     * Number of single, 'atomic' tests encapsulated by the described test.
     *
     * @return number of single tests within this test.
     */
    int testCount();

    /**
     * Descriptions of child tests.
     *
     * @return descriptions of child tests of the described test
     */
    Iterable<WebTestDescription> children();
}
