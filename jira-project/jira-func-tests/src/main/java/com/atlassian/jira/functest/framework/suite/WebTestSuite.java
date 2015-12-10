package com.atlassian.jira.functest.framework.suite;

import java.util.Set;

/**
 * Represents a specific web test suite (e.g. for func tests, selenium tests etc.)
 *
 * @since 4.3
 */
public interface WebTestSuite
{
    /**
     * Get web test package.
     *
     * @return package containing tests
     */
    String webTestPackage();

    /**
     * Set of categories to include in the target suite.
     *
     * @return categories to include in the suite
     */
    Set<Category> includes();

    /**
     * Set of categories to exclude from the target suite.
     *
     * @return categories to exclude from the suite
     */
    Set<Category> excludes();
}
