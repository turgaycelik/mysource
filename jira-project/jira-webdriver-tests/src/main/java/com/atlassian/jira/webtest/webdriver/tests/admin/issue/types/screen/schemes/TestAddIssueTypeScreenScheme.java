package com.atlassian.jira.webtest.webdriver.tests.admin.issue.types.screen.schemes;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.AddIssueTypeScreenSchemeDialog;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.screen.schemes.ViewIssueTypeScreenSchemesPage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <p>Responsible for testing the core functionality of the &quot;add issue type screen scheme dialog&quot;.</p>
 *
 * @since v5.0.2
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.SCHEMES, Category.ISSUE_TYPES, Category.SCREENS})
@Restore ("xml/blankprojects.xml")
public class TestAddIssueTypeScreenScheme extends BaseJiraWebTest
{
    @Test
    public void shouldBeAbleToAddAnIssueTypeScreenSchemeByOnlyDefiningANameForItAndSelectingTheDefaultScreenScheme()
    {
        final ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem expectedIssueTypeScreenSchemeItem =
                new ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem("A test issue type screen scheme", "");

        jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                openAddIssueTypeScreenSchemeDialog().
                setName(expectedIssueTypeScreenSchemeItem.getName()).
                setScreenScheme("Default Screen Scheme").
                submitSuccess();

        final ViewIssueTypeScreenSchemesPage viewIssueTypeScreenSchemesPage = jira.goTo(ViewIssueTypeScreenSchemesPage.class);

        final Iterable<ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem> actualIssueTypeScreenSchemes =
                viewIssueTypeScreenSchemesPage.getIssueTypeScreenSchemes();

        assertTrue(contains(actualIssueTypeScreenSchemes, expectedIssueTypeScreenSchemeItem));

    }

    @Test
    public void shouldBeAbleToAddAnIssueTypeScreenSchemeByDefiningBothItsNameAndDescriptionAndSelectingTheDefaultScreenScheme()
    {
        final ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem expectedIssueTypeScreenSchemeItem =
                new ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem
                        (
                                "A test issue type screen scheme", "description for the test issue type screen scheme"
                        );

        jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                openAddIssueTypeScreenSchemeDialog().
                setName(expectedIssueTypeScreenSchemeItem.getName()).
                setDescription(expectedIssueTypeScreenSchemeItem.getDescription()).
                setScreenScheme("Default Screen Scheme").
                submitSuccess();

        final ViewIssueTypeScreenSchemesPage viewIssueTypeScreenSchemesPage = jira.goTo(ViewIssueTypeScreenSchemesPage.class);
        final Iterable<ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem> actualIssueTypeScreenSchemes =
                viewIssueTypeScreenSchemesPage.getIssueTypeScreenSchemes();

        assertTrue(contains(actualIssueTypeScreenSchemes, expectedIssueTypeScreenSchemeItem));
    }

    /**
     * <p>
     *     Verifies that it is possible to select a screen scheme different fromt the &quot;Default Screen Scheme&quot;
     * </p>
     *
     * <p>
     *     <em>Initial data:</em>
     *     Contains a &quot;Test Screen Scheme&quot; so that we can select it from the dialog under test.
     * </p>
     */
    @Test
    @Restore ("xml/TestAddIssueTypeScreenScheme/existing-screen-scheme.xml")
    public void shouldBeAbleToAddAnIssueTypeScreenSchemeSelectingACustomScreenScheme()
    {
        final ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem expectedIssueTypeScreenSchemeItem =
                new ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem
                        (
                                "A test issue type screen scheme", "description for the test issue type screen scheme"
                        );

        jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                openAddIssueTypeScreenSchemeDialog().
                setName(expectedIssueTypeScreenSchemeItem.getName()).
                setDescription(expectedIssueTypeScreenSchemeItem.getDescription()).
                setScreenScheme("Test Screen Scheme").
                submitSuccess();

        final ViewIssueTypeScreenSchemesPage viewIssueTypeScreenSchemesPage = jira.goTo(ViewIssueTypeScreenSchemesPage.class);
        final Iterable<ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem> actualIssueTypeScreenSchemes =
                viewIssueTypeScreenSchemesPage.getIssueTypeScreenSchemes();

        assertTrue(contains(actualIssueTypeScreenSchemes, expectedIssueTypeScreenSchemeItem));

        final String actualDefaultScreenScheme = viewIssueTypeScreenSchemesPage.
                configure("A test issue type screen scheme").
                    getDefaultScreenScheme();

        assertEquals("Test Screen Scheme", actualDefaultScreenScheme);
    }

    @Test
    public void shouldNotBeAbleToAddAnIssueTypeScreenSchemeGivenThatItsNameIsEmpty()
    {
        final ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem issueTypeScreenSchemeItemWithAnEmptyName =
                new ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem("", "description text");

        final String expectedErrorMessageForAnIssueTypeScreenSchemeWithAnEmptyName =
                "The issue type screen scheme name must not be empty.";

        final AddIssueTypeScreenSchemeDialog addIssueTypeScreenSchemeDialog =
                jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                openAddIssueTypeScreenSchemeDialog();

        addIssueTypeScreenSchemeDialog.
                setName(issueTypeScreenSchemeItemWithAnEmptyName.getName()).
                setDescription(issueTypeScreenSchemeItemWithAnEmptyName.getDescription()).
                submit();

        assertTrue(addIssueTypeScreenSchemeDialog.isOpen().now());
        assertTrue(addIssueTypeScreenSchemeDialog.hasFormErrors());

        final Map<String,String> addIssueTypeScreenSchemeDialogFormErrors =
                addIssueTypeScreenSchemeDialog.getFormErrors();

        assertEquals
                (
                        addIssueTypeScreenSchemeDialogFormErrors.get("schemeName"),
                        expectedErrorMessageForAnIssueTypeScreenSchemeWithAnEmptyName
                );
    }

    @Test
    public void shouldNotBeAbleToAddAnIssueTypeScreenSchemeGivenThatItsNameMatchesAnExistingIssueTyprScreenScheme()
    {
        final ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem defaultIssueTypeScreenScheme =
                new ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem
                        (
                                "Default Issue Type Screen Scheme", ""
                        );

        final String expectedErrorMessageForAnIssueTypeScreenWithADuplicateName =
                "A scheme with this name already exists.";

        final AddIssueTypeScreenSchemeDialog addIssueTypeScreenSchemeDialog =
                jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                openAddIssueTypeScreenSchemeDialog();

        addIssueTypeScreenSchemeDialog.
                setName(defaultIssueTypeScreenScheme.getName()).
                setDescription(defaultIssueTypeScreenScheme.getDescription()).
                submit();

        assertTrue(addIssueTypeScreenSchemeDialog.isOpen().now());
        assertTrue(addIssueTypeScreenSchemeDialog.hasFormErrors());

        final Map<String,String> addIssueTypeScreenSchemeDialogFormErrors =
                addIssueTypeScreenSchemeDialog.getFormErrors();

        assertEquals
                (
                        addIssueTypeScreenSchemeDialogFormErrors.get("schemeName"),
                        expectedErrorMessageForAnIssueTypeScreenWithADuplicateName
                );

    }

    @Test
    public void shouldBeAbleToAddAnIssueTypeScreenSchemeWhenTheLoggedInUserIsAJiraAdministrator()
    {
        final ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem expectedIssueTypeScreenSchemeItem =
                new ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem
                        (
                                "A test issue type screen scheme", "description for the test issue type screen scheme"
                        );

        jira.goTo(ViewIssueTypeScreenSchemesPage.class).
                openAddIssueTypeScreenSchemeDialog().
                    setName(expectedIssueTypeScreenSchemeItem.getName()).
                    setDescription(expectedIssueTypeScreenSchemeItem.getDescription()).
                    setScreenScheme("Default Issue Type Screen Scheme").
                submitSuccess();

        final ViewIssueTypeScreenSchemesPage viewIssueTypeScreenSchemesPage = jira.goTo(ViewIssueTypeScreenSchemesPage.class);
        final Iterable<ViewIssueTypeScreenSchemesPage.IssueTypeScreenSchemeItem> actualIssueTypeScreenSchemes =
                viewIssueTypeScreenSchemesPage.getIssueTypeScreenSchemes();

        assertTrue(contains(actualIssueTypeScreenSchemes, expectedIssueTypeScreenSchemeItem));
    }
}
