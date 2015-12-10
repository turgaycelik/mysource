package com.atlassian.jira.webtest.webdriver.tests.admin.fields.configuration.schemes;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.AddFieldConfigurationSchemeDialog;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.ViewFieldConfigurationSchemesPage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Responsible for testing the core functionality of the &quot;add field configuration scheme dialog&quot;.
 *
 * @since v5.0.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.SCHEMES, Category.FIELDS })
@Restore ("xml/blankprojects.xml")
public class TestAddFieldConfigurationSchemeDialog extends BaseJiraWebTest
{
    @Test
    public void shouldBeAbleToAddAFieldConfigurationSchemeByOnlyDefiningANameForIt()
    {
        final ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem expectedFieldConfigurationSchemeItem =
                new ViewFieldConfigurationSchemesPage.
                        FieldConfigurationSchemeItem("A test field config. scheme", "");

                jira.goTo(ViewFieldConfigurationSchemesPage.class).
                        openAddFieldConfigurationSchemeDialog().
                        setName(expectedFieldConfigurationSchemeItem.getName()).
                        submitSuccess();

        final ViewFieldConfigurationSchemesPage ViewFieldConfigurationSchemesPage = jira.goTo(ViewFieldConfigurationSchemesPage.class);
        final Iterable<ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem> actualFieldConfigurationSchemes =
                ViewFieldConfigurationSchemesPage.getFieldConfigurationSchemes();

        assertTrue(contains(actualFieldConfigurationSchemes, expectedFieldConfigurationSchemeItem));
    }

    @Test
    public void shouldBeAbleToAddAFieldConfigurationSchemeByDefiningBothItsNameAndADescription()
    {
        final ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem expectedFieldConfigurationSchemeItem =
                new ViewFieldConfigurationSchemesPage.
                        FieldConfigurationSchemeItem
                        (
                                "A test field config. scheme",
                                "description for the test field configuration scheme"
                        );

                jira.goTo(ViewFieldConfigurationSchemesPage.class).
                        openAddFieldConfigurationSchemeDialog().
                        setName(expectedFieldConfigurationSchemeItem.getName()).
                        setDescription(expectedFieldConfigurationSchemeItem.getDescription()).
                        submitSuccess();

        final ViewFieldConfigurationSchemesPage ViewFieldConfigurationSchemesPage = jira.goTo(ViewFieldConfigurationSchemesPage.class);
        final Iterable<ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem> actualFieldConfigurations =
                ViewFieldConfigurationSchemesPage.getFieldConfigurationSchemes();

        assertTrue(contains(actualFieldConfigurations, expectedFieldConfigurationSchemeItem));
    }

    @Test
    public void shouldNotBeAbleToAddAFieldConfigurationSchemeGivenThatItsNameIsEmpty()
    {
        final ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem
                fieldConfigurationSchemeItemWithAnEmptyName =
                new ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem("", "description text");

        final String expectedErrorMessageForAFieldConfigurationWithAnEmptyName =
                "The field configuration scheme name must not be empty.";

        final AddFieldConfigurationSchemeDialog addFieldConfigurationDialog =
                jira.goTo(ViewFieldConfigurationSchemesPage.class).
                openAddFieldConfigurationSchemeDialog();

        addFieldConfigurationDialog.
                setName(fieldConfigurationSchemeItemWithAnEmptyName.getName()).
                setDescription(fieldConfigurationSchemeItemWithAnEmptyName.getDescription()).
                submit();

        assertTrue(addFieldConfigurationDialog.isOpen().now());
        assertTrue(addFieldConfigurationDialog.hasFormErrors());

        final Map<String, String> addFieldConfigurationDialogFormErrors =
                addFieldConfigurationDialog.getFormErrors();

        assertEquals
                (
                        addFieldConfigurationDialogFormErrors.get("fieldLayoutSchemeName"),
                        expectedErrorMessageForAFieldConfigurationWithAnEmptyName
                );
    }

    @Test
    @Restore ("xml/TestAddFieldConfigurationSchemeDialog/existing-config-scheme-validation.xml")
    public void shouldNotBeAbleToAddAFieldConfigurationSchemeGivenThatItsNameMatchesAnExistingFieldConfiguration()
    {
        final ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem defaultFieldConfiguration =
                new ViewFieldConfigurationSchemesPage.
                        FieldConfigurationSchemeItem("An existing config. scheme", "description text");

        final String expectedErrorMessageForAFieldConfigurationWithDuplicateName =
                "A field configuration scheme with this name already exists.";

        final AddFieldConfigurationSchemeDialog addFieldConfigurationDialog =
                jira.goTo(ViewFieldConfigurationSchemesPage.class).
                openAddFieldConfigurationSchemeDialog();

        addFieldConfigurationDialog.
                setName(defaultFieldConfiguration.getName()).
                setDescription(defaultFieldConfiguration.getDescription()).
                submit();

        assertTrue(addFieldConfigurationDialog.isOpen().now());
        assertTrue(addFieldConfigurationDialog.hasFormErrors());

        final Map<String,String> addFieldConfigurationDialogFormErrors =
                addFieldConfigurationDialog.getFormErrors();

        assertEquals
                (
                        addFieldConfigurationDialogFormErrors.get("fieldLayoutSchemeName"),
                        expectedErrorMessageForAFieldConfigurationWithDuplicateName
                );
    }

    @Test
    public void shouldBeAbleToAddAFieldConfigurationSchemeWhenTheLoggedInUserIsAJiraAdministrator()
    {
        final ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem expectedFieldConfigurationSchemeItem =
                new ViewFieldConfigurationSchemesPage.
                        FieldConfigurationSchemeItem("A test field config. scheme", "description text");

                jira.goTo(ViewFieldConfigurationSchemesPage.class).
                        openAddFieldConfigurationSchemeDialog().
                        setName(expectedFieldConfigurationSchemeItem.getName()).
                        setDescription(expectedFieldConfigurationSchemeItem.getDescription()).
                        submitSuccess();

        final ViewFieldConfigurationSchemesPage ViewFieldConfigurationSchemesPage = jira.goTo(ViewFieldConfigurationSchemesPage.class);
        final Iterable<ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem> actualFieldConfigurations =
                ViewFieldConfigurationSchemesPage.getFieldConfigurationSchemes();

        assertTrue(contains(actualFieldConfigurations, expectedFieldConfigurationSchemeItem));
    }
}
