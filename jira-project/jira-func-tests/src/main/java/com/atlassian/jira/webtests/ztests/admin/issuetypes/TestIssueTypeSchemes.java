package com.atlassian.jira.webtests.ztests.admin.issuetypes;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebTable;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import static com.atlassian.jira.functest.framework.util.dom.DomKit.getCollapsedText;

/**
 * Functional test issue type schemes
 *
 * @since v3.12
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.ISSUES, Category.SCHEMES, Category.ISSUE_TYPES })
public class TestIssueTypeSchemes extends JIRAWebTest
{
    private static final String DEFAULT_ISSUE_TYPE_SCHEME_NAME = "Default Issue Type Scheme";
    private static final String DEFAULT_ISSUE_TYPE_SCHEME_DESC = "Default issue type scheme is the list of global issue types. All newly created issue types will automatically be added to this scheme.";

    public void testIssueTypeSchemesSelectOrder() throws SAXException
    {
        administration.restoreData("TestIssueTypeSchemes_Order.xml");
        navigation.gotoAdmin();

        Long projectId = backdoor.project().getProjectId(PROJECT_HOMOSAP_KEY);
        tester.gotoPage("secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        tester.checkCheckbox("createType", "chooseScheme");

        final WebForm select = tester.getDialog().getResponse().getFormWithName("jiraform");
        final String[] values = select.getOptionValues("schemeId");
        assertEquals("10000", values[0]);
        assertEquals("10010", values[1]);
        assertEquals("10013", values[2]);
        assertEquals("10012", values[3]);
        assertEquals("10011", values[4]);
    }

    public TestIssueTypeSchemes(final String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestIssueTypeSchemes.xml");
    }

    public void testIssueTypeSchemesView()
    {
        gotoIssueTypeSchemes();
        assertCommonIssueTypeSchemesTable();
    }

    public void testIssueTypeSchemeCopy()
    {
        _testIssueTypeSchemeCopyValidation();
        _testIssueTypeSchemeCopy();
    }

    public void testIssueTypeSchemeEdit()
    {
        _testIssueTypeSchemeEditValidation();
    }

    public void testIssueTypeSchemeDelete()
    {
        //delete scheme with project association
        gotoIssueTypeSchemes();
        tester.clickLink("delete_10011");
        assertTextPresent("Delete Issue Type Scheme: Associated Issue Type Scheme");
        assertTextSequence(new String[] { "You are about to delete the Issue Type Scheme named", "Associated Issue Type Scheme" });
        assertTextPresent("There is one project (" + PROJECT_MONKEY + ") currently using this scheme. This project will revert to using the default global issue type scheme.");
        tester.submit("Delete");

        try
        {
            final WebTable issueTypeSchemesTable = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(3, issueTypeSchemesTable.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable, 0, new Object[] { "Name", "Options", "Projects", "Operations" });
            text.assertTextPresent(locator.css("#issuetypeschemes tr td [data-scheme-field=name]"), DEFAULT_ISSUE_TYPE_SCHEME_NAME);
            text.assertTextPresent(locator.css("#issuetypeschemes tr td [data-scheme-field=name]"), "Test Issue Type Scheme");
            text.assertTextNotPresent(locator.css("#issuetypeschemes tr td [data-scheme-field=name]"), "Associated Issue Type Scheme");
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }

        //delete scheme with no project association
        gotoIssueTypeSchemes();
        tester.clickLink("delete_10010");
        assertTextPresent("Delete Issue Type Scheme: Test Issue Type Scheme");
        assertTextSequence(new String[] { "You are about to delete the Issue Type Scheme named", "Test Issue Type Scheme" });
        assertTextPresent("There are no projects currently using this scheme.");
        tester.submit("Delete");

        try
        {
            final WebTable issueTypeSchemesTable1 = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(2, issueTypeSchemesTable1.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable1, 0, new Object[] { "Name", "Options", "Projects", "Operations" });
            text.assertTextPresent(locator.css("#issuetypeschemes tr td [data-scheme-field=name]"), DEFAULT_ISSUE_TYPE_SCHEME_NAME);
            text.assertTextNotPresent(locator.css("#issuetypeschemes tr td [data-scheme-field=name]"), "Test Issue Type Scheme");
            text.assertTextNotPresent(locator.css("#issuetypeschemes tr td [data-scheme-field=name]"), "Associated Issue Type Scheme");
        }
        catch (SAXException e1)
        {
            throw new RuntimeException(e1);
        }
    }

    public void testIssueTypeSchemeAssociateDefaultScheme()
    {
        gotoIssueTypeSchemes();
        tester.clickLink("associate_10000");
        assertTextPresent("Associate Issue Type Scheme");
        assertTextPresent("Only projects not currently associated with the default scheme are displayed.");
        assertOptionsEqual("projects", new String[] { PROJECT_MONKEY });
        tester.selectOption("projects", PROJECT_MONKEY);
        tester.submit("Associate");

        try
        {
            final WebTable issueTypeSchemesTable1 = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(4, issueTypeSchemesTable1.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable1, 0, new Object[] { "Name", "Options", "Projects", "Operations" });

            //assert that Associated Issue type Scheme no longer has the monkey projects as an association
            final Node[] nodes = locator.css("#issuetypeschemes tbody tr").getNodes();
            for (Node node : nodes)
            {
                final String schemeName = getCollapsedText(new CssLocator(node, "td [data-scheme-field=name]").getNode());
                if (schemeName.equals("Associated Issue Type Scheme"))
                {
                    final String projects = getCollapsedText(new CssLocator(node, "td[data-scheme-field=projects]").getNode());
                    assertEquals(projects, "No projects");
                }
            }
        }
        catch (SAXException e1)
        {
            throw new RuntimeException(e1);
        }

        //goto associate default issue type scheme to check that all projects are associated with it
        tester.clickLink("associate_10000");
        assertTextPresent("Associate Issue Type Scheme");
        assertTextSequence(new String[] { "No projects available to be associated with scheme", "Default Issue Type Scheme" });
        assertFormElementNotPresent("projects");
        assertSubmitButtonNotPresent("Associate");
    }

    public void testIssueTypeSchemeAssociateSchemeWithProjectAssociation()
    {
        //associate another project (ie. 2 selected now).
        gotoIssueTypeSchemes();
        tester.clickLink("associate_10011");
        assertTextPresent("Associate Issue Type Scheme");
        assertOptionsEqual("projects", new String[] { PROJECT_HOMOSAP, PROJECT_MONKEY });
        assertOptionSelected("projects", PROJECT_MONKEY);//assert that currently associated project is selected
        selectMultiOption("projects", PROJECT_MONKEY);
        selectMultiOption("projects", PROJECT_HOMOSAP);
        tester.submit("Associate");

        try
        {
            final WebTable issueTypeSchemesTable = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(4, issueTypeSchemesTable.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable, 0, new Object[] { "Name", "Options", "Projects", "Operations" });
            //check scheme is now associated to two projects
            final Node[] nodes = locator.css("#issuetypeschemes tbody tr").getNodes();
            for (Node node : nodes)
            {
                final String schemeName = getCollapsedText(new CssLocator(node, "td [data-scheme-field=name]").getNode());
                if (schemeName.equals("Associated Issue Type Scheme"))
                {
                    final String projects = getCollapsedText(new CssLocator(node, "td[data-scheme-field=projects]").getNode());
                    assertEquals(projects, "homosapien monkey");
                }
            }
        }
        catch (SAXException e1)
        {
            throw new RuntimeException(e1);
        }

        //remove one project association from the 2
        tester.clickLink("associate_10011");
        assertTextPresent("Associate Issue Type Scheme");
        assertOptionsEqual("projects", new String[] { PROJECT_HOMOSAP, PROJECT_MONKEY });
        assertOptionSelected("projects", PROJECT_MONKEY);//assert that currently associated project is selected
        assertOptionSelected("projects", PROJECT_HOMOSAP);//assert that currently associated project is selected
        tester.selectOption("projects", PROJECT_HOMOSAP);
        tester.submit("Associate");

        try
        {
            final WebTable issueTypeSchemesTable1 = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(4, issueTypeSchemesTable1.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable1, 0, new Object[] { "Name", "Options", "Projects", "Operations" });
            //assert that Associated Issue type Scheme is associated to homosapien only
            final Node[] nodes = locator.css("#issuetypeschemes tbody tr").getNodes();
            for (Node node : nodes)
            {
                final String schemeName = getCollapsedText(new CssLocator(node, "td [data-scheme-field=name]").getNode());
                if (schemeName.equals("Associated Issue Type Scheme"))
                {
                    final String projects = getCollapsedText(new CssLocator(node, "td[data-scheme-field=projects]").getNode());
                    assertEquals(projects, "homosapien");
                }
            }
        }
        catch (final SAXException e1)
        {
            throw new RuntimeException(e1);
        }
    }

    public void testIssueTypeSchemeAssociateSchemeWithNoProjectAssociation()
    {
        gotoIssueTypeSchemes();
        tester.clickLink("associate_10010");
        assertTextPresent("Associate Issue Type Scheme");
        assertOptionsEqual("projects", new String[] { PROJECT_HOMOSAP, PROJECT_MONKEY });
        selectMultiOption("projects", PROJECT_MONKEY);//take the monkey project association from the other scheme
        tester.submit("Associate");

        try
        {
            final WebTable issueTypeSchemesTable1 = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(4, issueTypeSchemesTable1.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable1, 0, new Object[] { "Name", "Options", "Projects", "Operations" });
            //assert that Associated Issue type Scheme no longer has the monkey projects as an association
            Node[] nodes = locator.css("#issuetypeschemes tbody tr").getNodes();
            for (Node node : nodes)
            {
                final String schemeName = getCollapsedText(new CssLocator(node, "td [data-scheme-field=name]").getNode());
                if (schemeName.equals("Associated Issue Type Scheme"))
                {
                    final String projects = getCollapsedText(new CssLocator(node, "td[data-scheme-field=projects]").getNode());
                    assertEquals(projects, "No projects");
                }
            }
            //assert that test issue type scheme is associated to monkey
            nodes = locator.css("#issuetypeschemes tbody tr").getNodes();
            for (Node node : nodes)
            {
                final String schemeName = getCollapsedText(new CssLocator(node, "td [data-scheme-field=name]").getNode());
                if (schemeName.equals("Test Issue Type Scheme"))
                {
                    final String projects = getCollapsedText(new CssLocator(node, "td[data-scheme-field=projects]").getNode());
                    assertEquals(projects, "monkey");
                }
            }
        }
        catch (final SAXException e1)
        {
            throw new RuntimeException(e1);
        }
    }

    private void _testIssueTypeSchemeCopyValidation()
    {
        gotoIssueTypeSchemes();
        tester.clickLink("copy_10000"); //Default issue type scheme
        tester.assertFormElementEquals("name", "Copy of " + DEFAULT_ISSUE_TYPE_SCHEME_NAME);
        tester.assertFormElementEquals("description", DEFAULT_ISSUE_TYPE_SCHEME_DESC);
        assertIssueTypeSchemeFormValidation();

        gotoIssueTypeSchemes();
        tester.clickLink("copy_10010"); //Test Issue Type Scheme
        tester.assertFormElementEquals("name", "Copy of Test Issue Type Scheme");
        tester.assertFormElementEquals("description", "Description for test issue type scheme");
        assertIssueTypeSchemeFormValidation();

        gotoIssueTypeSchemes();
        tester.clickLink("copy_10011"); //Associated Issue Type Scheme
        tester.assertFormElementEquals("name", "Copy of Associated Issue Type Scheme");
        tester.assertFormElementEquals("description", "Description for associated issue type scheme");
        assertIssueTypeSchemeFormValidation();
    }

    private void _testIssueTypeSchemeCopy()
    {
        gotoIssueTypeSchemes();
        assertCommonIssueTypeSchemesTable(4);

        tester.clickLink("copy_10000"); //Default issue type scheme
        text.assertTextPresent(locator.css("h2"),"Add Issue Type Scheme");
        tester.assertFormElementEquals("name", "Copy of " + DEFAULT_ISSUE_TYPE_SCHEME_NAME);
        tester.assertFormElementEquals("description", DEFAULT_ISSUE_TYPE_SCHEME_DESC);

        gotoIssueTypeSchemes();
        tester.clickLink("copy_10010"); //Test Issue Type Scheme
        text.assertTextPresent(locator.css("h2"),"Add Issue Type Scheme");
        tester.assertFormElementEquals("name", "Copy of Test Issue Type Scheme");
        tester.assertFormElementEquals("description", "Description for test issue type scheme");

        gotoIssueTypeSchemes();
        tester.clickLink("copy_10011"); //Associated Issue Type Scheme
        text.assertTextPresent(locator.css("h2"),"Add Issue Type Scheme");
        tester.assertFormElementEquals("name", "Copy of Associated Issue Type Scheme");
        tester.assertFormElementEquals("description", "Description for associated issue type scheme");
    }

    private void _testIssueTypeSchemeEditValidation()
    {
        gotoIssueTypeSchemes();
        tester.clickLink("edit_10000"); //Default issue type scheme
        tester.assertFormElementEquals("name", DEFAULT_ISSUE_TYPE_SCHEME_NAME);
        tester.assertFormElementEquals("description", DEFAULT_ISSUE_TYPE_SCHEME_DESC);
        assertIssueTypeSchemeFormValidation();

        gotoIssueTypeSchemes();
        tester.clickLink("edit_10010"); //Test Issue Type Scheme
        tester.assertFormElementEquals("name", "Test Issue Type Scheme");
        tester.assertFormElementEquals("description", "Description for test issue type scheme");
        assertIssueTypeSchemeFormValidation();

        gotoIssueTypeSchemes();
        clickLink("edit_10011"); //Associated Issue Type Scheme
        tester.assertFormElementEquals("name", "Associated Issue Type Scheme");
        tester.assertFormElementEquals("description", "Description for associated issue type scheme");
        assertIssueTypeSchemeFormValidation();
    }

    public void gotoIssueTypeSchemes()
    {
        navigation.gotoAdminSection("issue_type_schemes");
    }

    public void assertCommonIssueTypeSchemesTable()
    {
        assertCommonIssueTypeSchemesTable(4);
    }

    public void assertCommonIssueTypeSchemesTable(final int numberOfRows)
    {
        //assert that the issue type schemes appear as it is in the import file.
        try
        {
            final WebTable issueTypeSchemesTable = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(numberOfRows, issueTypeSchemesTable.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable, 0, new Object[] { "Name", "Options", "Projects", "Operations" });
            text.assertTextPresent(locator.css("#issuetypeschemes tr td [data-scheme-field=name]"), DEFAULT_ISSUE_TYPE_SCHEME_NAME);
            text.assertTextPresent(locator.css("#issuetypeschemes tr td [data-scheme-field=name]"), "Test Issue Type Scheme");
            text.assertTextPresent(locator.css("#issuetypeschemes tr td [data-scheme-field=name]"), "Associated Issue Type Scheme");
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertIssueTypeSchemeFormValidation()
    {
        assertTextNotPresent("You must select at least one option");
        assertTextNotPresent("You must enter a valid name.");
        tester.setFormElement("name", "");
        tester.setFormElement("description", "");

        tester.submit("save");
        assertTextPresent("You must select at least one option");
        assertTextPresent("You must enter a valid name.");

        tester.setFormElement("name", "test name");
        tester.submit("save");
        assertTextPresent("You must select at least one option");
        assertTextNotPresent("You must enter a valid name.");
    }
}
