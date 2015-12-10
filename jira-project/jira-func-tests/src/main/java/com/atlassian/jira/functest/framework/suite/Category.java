package com.atlassian.jira.functest.framework.suite;

import java.util.EnumSet;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static java.util.Arrays.asList;

/**
 * Enumeration of possible categories of JIRA web tests.
 *
 * @since v4.4
 */
public enum Category
{
    /**
     * Generic category applied to all func tests.
     *
     */
    FUNC_TEST,

    /**
     * Generic category applied to all Selenium tests. NOTE: those tests should be steadily migrated to WebDriver suite.
     */
    SELENIUM_TEST,

    /**
     * Generic category applied to all WebDriver tests.
     *
     */
    WEBDRIVER_TEST,

    /**
     * Marks tests that can only be run in the TPM builds.
     *
     */
    TPM,

    /**
     * Platform compatibility tests.
     *
     */
    PLATFORM_COMPATIBILITY,

    /**
     * Tests for visual regression.
     *
     */
    VISUAL_REGRESSION,

    /**
     * Tests for visual regression during setup steps.
     *
     */
    VISUAL_REGRESSION_SETUP,

    /**
     * QUnit runner.
     *
     */
    QUNIT,

    /**
     * Marks all tests that require 'dev-mode' plugins installed in JIRA.
     *
     */
    DEV_MODE,

    /**
     * Marks all tests that require the JIRA reference plugin installed in JIRA.
     *
     */
    REFERENCE_PLUGIN,

    /**
     * Requires ignite plugin to run.
     *
     */
    IGNITE,

    /**
     * Performance test
     *
     */
    PERFORMANCE,

    /**
     * Tests for plugins reloadability (involve slow data restore)
     *
     */
    RELOADABLE_PLUGINS,

    /**
     * We have some tests that we want to run against other databases: MS SQL, Oracle, Postgres, etc. In theory
     * we might want a different Category for each database (along with each OS-type [Linux, Windows]) but in practice
     * we'll start with just lumping them all together under "DATABASE" and refine that later if we need to.
     */
    DATABASE,

    /**
     * Browser exclusions
     */
    IE_INCOMPATIBLE,


    /**
     * Tests that are run for infrastructure/maintenance reasons and should not be run as part of the normal suite.
     */
    INFRASTRUCTURE,
//    /**
//     * Test that must be run first
//     *
//     */
//    RUN_FIRST,

    // 'functional' test categories (must be passed defined with the 'functional' flag set to true)
    ACTIVITY_STREAMS(true),
    ADMINISTRATION(true),
    API(true),
    APP_LINKS(true),
    ATTACHMENTS(true),
    BROWSE_PROJECT(true),
    BROWSING(true),
    BULK_OPERATIONS(true),
    CHARTING(true),
    CLONE_ISSUE(true),
    COMMENTS(true),
    COMPONENTS_AND_VERSIONS(true),
    CUSTOM_FIELDS(true),
    DASHBOARDS(true),
    EMAIL(true),
    FIELDS(true),
    FILTERS(true),
    GADGETS(true),
    HTTP(true),
    I18N(true),
    IMPORT_EXPORT(true),
    INDEXING(true),
    ISSUE_NAVIGATOR(true),
    ISSUES(true),
    ISSUE_LINKS(true),
    JELLY(true),
    JQL(true),
    LDAP(true),
    LICENSING(true),
    MOVE_ISSUE(true),
    PERMISSIONS(true),
    PLUGINS(true),
    PROJECT_IMPORT(true),
    PROJECTS(true),
    QUARTZ(true),
    REPORTS(true),
    RENAME_USER(true),
    REST(true),
    ROLES(true),
    SCHEMES(true),
    SECURITY(true),
    SETUP(true),
    SLOW_IMPORT(true),
    SUB_TASKS(true),
    TIME_TRACKING(true),
    TIME_ZONES(true),
    UPGRADE_TASKS(true),
    USERS_AND_GROUPS(true),
    WORKFLOW(true),
    WORKLOGS(true),
    CHANGE_HISTORY(true),
    ACTIVE_OBJECTS(true),
    ISSUE_TYPES(true),
    SCREENS(true),
    WELCOME_PLUGIN(true),
    ENTITY_PROPERTIES(true),
    /**
     * Setup test that need pristine JIRA - execured in separate jobs on CI.
     * Are not included in harness.
     */
    SETUP_PRISTINE(true);
    // Add more here if you need to


    public static Category forString(String constName)
    {
        notBlank("constName", constName);
        for (Category category : values())
        {
            if (category.name().equalsIgnoreCase(constName))
            {
                return category;
            }
        }
        throw new IllegalArgumentException("No corresponding Category constant for value \"" + constName + "\"");
    }

    public static Set<Category> fromAnnotation(WebTest webTest)
    {
        if (webTest == null || webTest.value().length == 0)
        {
            // EnumSet can't be created from empty collection - we get IllegalArgumentException - awesome!
            return EnumSet.noneOf(Category.class);
        }
        else
        {
            // in order to get enum set we create a list - WIN!
            return EnumSet.copyOf(asList(webTest.value()));
        }
    }



    private final boolean isFunctional;

    Category()
    {
        // non-functional by default
        this(false);
    }

    Category(boolean functional)
    {
        this.isFunctional = functional;
    }


    /**
     * Whether or not this category marks a functional area of the tested application.
     *
     * @return <code>true</code>, if this is a functional category
     */
    public boolean isFunctional()
    {
        return isFunctional;
    }
}
