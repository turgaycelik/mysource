package com.atlassian.jira.webtest.webdriver.tests.admin.issue.types.screen.schemes.configure;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.ViewIssueTypeScreenSchemesPage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.configure.ConfigureIssueTypeScreenSchemePage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static com.google.common.collect.Iterables.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <p>Responsible for testing the core functionality of the &quot;add issue type to screen scheme association
 * dialog&quot;.</p>
 *
 * <p>This dialog adds an association between an issue type and a screen scheme for a given issue type screen
 * scheme.</p>
 *
 * @since v5.0.2
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.SCHEMES, Category.SCREENS, Category.ISSUE_TYPES })
@Restore ("xml/blankprojects.xml")
public class TestAddIssueTypeToScreenSchemeAssociation extends BaseJiraWebTest
{
    @Test
    public void shouldBeAbleToAssociateAnIssueTypeToTheDefaultScreenSchemeWhenThereAreStillUnmappedIssueTypes()
    {
        final ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                expectedIssueTypeToScreenSchemeAssociation =
                new ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                        (
                                "Bug", "Default Screen Scheme"
                        );

        final ConfigureIssueTypeScreenSchemePage configureIssueTypeScreenSchemePage =
                jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                        configure("Default Issue Type Screen Scheme").
                        openAssociateIssueTypeToScreenSchemeDialog().
                        setIssueType(expectedIssueTypeToScreenSchemeAssociation.getIssueType()).
                        setScreenScheme(expectedIssueTypeToScreenSchemeAssociation.getScreenScheme()).
                        submitSuccess();

        final Iterable<ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem>
                actualIssueTypeToFieldConfigurationAssociationItems =
                configureIssueTypeScreenSchemePage.getIssueTypeToScreenSchemeAssociations();

        assertTrue
                (
                        contains
                                (
                                        actualIssueTypeToFieldConfigurationAssociationItems,
                                        expectedIssueTypeToScreenSchemeAssociation
                                )
                );
    }

    /**
     * <p>
     *     Verifies that it is possible to select a screen scheme different fromt the &quot;Default Screen Scheme&quot;
     * </p>
     * <p>
     *     <em>Initial data:</em>
     *     Contains a &quot;Test Screen Scheme&quot; so that we can select it from the dialog under test.
     * </p>
     */
    @Test
    @Restore ("xml/TestAddIssueTypeToScreenSchemeAssociation/existing-screen-scheme.xml")
    public void shouldBeAbleToAssociateAnIssueTypeToACustomScreenSchemeWhenThereAreStillUnmappedIssueTypes()
    {
        final ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                expectedIssueTypeToScreenSchemeAssociation =
                new ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                        (
                                "Improvement", "Test Screen Scheme"
                        );

        final ConfigureIssueTypeScreenSchemePage configureIssueTypeScreenSchemePage =
                jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                        configure("Default Issue Type Screen Scheme").
                        openAssociateIssueTypeToScreenSchemeDialog().
                        setIssueType(expectedIssueTypeToScreenSchemeAssociation.getIssueType()).
                        setScreenScheme(expectedIssueTypeToScreenSchemeAssociation.getScreenScheme()).
                        submitSuccess();

        final Iterable<ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem>
                actualIssueTypeToFieldConfigurationAssociationItems =
                configureIssueTypeScreenSchemePage.getIssueTypeToScreenSchemeAssociations();

        assertTrue
                (
                        contains
                                (
                                        actualIssueTypeToFieldConfigurationAssociationItems,
                                        expectedIssueTypeToScreenSchemeAssociation
                                )
                );
    }

    @Test
    public void shouldNotBeAbleToAssociateAnIssueTypeToAScreenSchemeWhenTheIssueTypeHasAlreadyBeenMapped()
    {
        final ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                existingIssueTypeToScreenSchemeAssociationItem =
                new ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                        (
                                "Task", "Default Screen Scheme"
                        );

        final ConfigureIssueTypeScreenSchemePage configureIssueTypeScreenSchemePage =
                jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                        configure("Default Issue Type Screen Scheme").
                        openAssociateIssueTypeToScreenSchemeDialog().
                        setIssueType(existingIssueTypeToScreenSchemeAssociationItem.getIssueType()).
                        setScreenScheme(existingIssueTypeToScreenSchemeAssociationItem.getScreenScheme()).
                        submitSuccess();

        final Iterable<String> selectableIssueTypes = configureIssueTypeScreenSchemePage.
                openAssociateIssueTypeToScreenSchemeDialog().
                getSelectableIssueTypes();

        assertFalse
                (
                        contains
                                (
                                        selectableIssueTypes,
                                        "Task"
                                )
                );
    }

    @Test
    public void shouldNotBeAbleToAssociateAnIssueTypeToAScreenSchemeWhenAllIssueTypesHaveAlreadyBeenMapped()
    {
        final Iterable<ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem>
                associationItemsForAllExistingIssueTypes =
                ImmutableList.of(
                        new ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                                (
                                        "Bug", "Default Field Screen Scheme"
                                ),
                        new ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                                (
                                        "Task", "Default Field Screen Scheme"
                                ),
                        new ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                                (
                                        "Improvement", "Default Field Screen Scheme"
                                ),
                        new ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                                (
                                        "New Feature", "Default Field Screen Scheme"
                                )
                );

        for (final ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                issueTypeToFieldConfigurationAssociationItem : associationItemsForAllExistingIssueTypes)
        {
            jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                    configure("Default Issue Type Screen Scheme").
                    openAssociateIssueTypeToScreenSchemeDialog().
                    setIssueType(issueTypeToFieldConfigurationAssociationItem.getIssueType()).
                    setScreenScheme(issueTypeToFieldConfigurationAssociationItem.getScreenScheme()).
                    submitSuccess();
        }

        assertTrue
                (
                        jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                                configure("Default Issue Type Screen Scheme").
                                isAddingAnIssueTypeToScreenSchemeAssociationDisabled()
                );
    }

        @Test
        public void shouldBeAbleToAssociateAnIssueTypeToAScreenSchemeWhenTheLoggedInUserIsAJiraAdministrator()
        {
            final ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                    expectedIssueTypeToScreenSchemeAssociationItem =
                    new ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem
                            (
                                    "Bug", "Default Screen Scheme"
                            );

            final ConfigureIssueTypeScreenSchemePage configureIssueTypeScreenSchemePage =
                    jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                            configure("Default Issue Type Screen Scheme").
                            openAssociateIssueTypeToScreenSchemeDialog().
                            setIssueType(expectedIssueTypeToScreenSchemeAssociationItem.getIssueType()).
                            setScreenScheme(expectedIssueTypeToScreenSchemeAssociationItem.getScreenScheme()).
                            submitSuccess();

            final Iterable<ConfigureIssueTypeScreenSchemePage.IssueTypeToScreenSchemeAssociationItem>
                    actualIssueTypeToFieldConfigurationAssociationItems =
                    configureIssueTypeScreenSchemePage.getIssueTypeToScreenSchemeAssociations();

            assertTrue
                    (
                            contains
                                    (
                                            actualIssueTypeToFieldConfigurationAssociationItems,
                                            expectedIssueTypeToScreenSchemeAssociationItem
                                    )
                    );
        }
}
