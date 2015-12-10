package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.CustomFieldValue;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.google.common.collect.ImmutableList;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.google.common.collect.Lists.newArrayList;

@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestFilterActions extends JIRAWebTest
{
    private static final String NO_COLUMN_ORDER = "has no associated Column Order";
    private static final String REMOVE_COLUMN_ORDER = "Remove Filter's Column Order";
    private static final String LOGIN = "log in";

    public TestFilterActions(String name)
    {
        super(name);
    }

    private static final int numberOfIssues = 10;
    private static Collection<String> issues = newArrayList();

    private static final String COLUMN_NAME_VOTES = "Votes";
    private static final String COLUMN_NAME_CREATED = "Created";

    private static final String FILTER_NAME_WITH_VOTE_COL = "filter with a vote column";
    private static final String FILTER_NAME_WITHOUT_VOTE_COL = "filter without a vote column";

    private static final String CUSTOM_FIELD_NAME_SELECT = "Custom Field Select";
    private static final String CUSTOM_FIELD_NAME_RADIO = "Custom Field Radio";
    private static final String CUSTOM_FIELD_NAME_MULTI_SELECT = "Custom Field Multi Select";
    private static final String CUSTOM_FIELD_NAME_MULTI_CHECKBOX = "Custom Field Multi Checkbox";
    private static final String CUSTOM_FIELD_NAME_TEXT_FIELD = "Custom Field Text Fields";

    private static final String FILTER_PRIVATE = "/secure/IssueNavigator.jspa?requestId=10000";
    private static final String FILTER_PUBLIC = "/secure/IssueNavigator.jspa?requestId=10001";
    private static final String FILTER_DEV = "/secure/IssueNavigator.jspa?requestId=10030";
    private static final String FILTER_INVALID = "/secure/IssueNavigator.jspa?requestId=10005";

    private static final String DELETE_FILTER_DEV = "/secure/DeleteFilter.jspa?filterId=10030";
    private static final String DELETE_FILTER_DEV_URL_HACK = "/secure/DeleteFilter.jspa?filterId=10030&Delete=delete";
    private static final String DELETE_FILTER_PUBLIC = "/secure/DeleteFilter.jspa?filterId=10001";
    private static final String DELETE_FILTER_PUBLIC_URL_HACK = "/secure/DeleteFilter.jspa?filterId=10001&Delete=delete";

    private static final String ERROR_MSG_NO_PERM_TO_VIEW_FILTER = "The selected filter with id '10000' does not exist.";
    private static final String ERROR_MSG_FILTER_DOES_NOT_EXIST = "The selected filter with id '10005' does not exist.";
    private static final String ERROR_MSG_CANNOT_DELETE_FILTER = "You do not have permission to delete this filter or this filter may not exist.";

    public void testCustomFilterActions() throws IOException, SAXException
    {
        administration.restoreData("TestFilterActionsCustomFilterActions.xml");

        //build the issues collection which holds the issue keys thats in the above XML import file.
        //Originally added 20 (twice the numberOfIssues) issues manually which takes a long time
        for (int key = 1; key <= 2 * numberOfIssues; ++key)
        {
            issues.add(PROJECT_HOMOSAP_KEY + "-" + key);
        }

        filterActionsWithCustomFields();
    }

    public void testFilterResultVisibility()
    {
        administration.restoreData("TestFilterResultVisibility.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "dev");

        // logged out user - can only see public
        navigation.logout();
        assertTextPresentOnFilterView(FILTER_PRIVATE, ERROR_MSG_NO_PERM_TO_VIEW_FILTER, LOGIN);
        assertLinkPresentOnFilterView(FILTER_PUBLIC, "HSP-1");
        assertTextPresentOnFilterView(FILTER_DEV, "The selected filter with id '10030' does not exist.", LOGIN);
        assertTextPresentOnFilterView(FILTER_INVALID, ERROR_MSG_FILTER_DOES_NOT_EXIST, LOGIN);

        //dev user - can only see public
        navigation.login("dev");
        tester.beginAt(FILTER_PRIVATE);
        tester.assertElementNotPresent("issuetable");
        tester.assertLinkNotPresentWithText(LOGIN);
        assertTextPresentOnFilterView(FILTER_PUBLIC, "all public", "HSP-1");
        assertTextPresentOnFilterView(FILTER_DEV, "all dev", "HSP-1");
        beginAt(FILTER_INVALID);
        tester.assertElementNotPresent("issuetable");
        assertLinkNotPresentWithText(LOGIN);

        //admin user - can see all filter results
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertTextPresentOnFilterView(FILTER_PRIVATE, "all private", "HSP-1");
        assertTextPresentOnFilterView(FILTER_PUBLIC, "all public", "HSP-1");
        assertTextPresentOnFilterView(FILTER_DEV, "all dev", "HSP-1");
        tester.beginAt(FILTER_INVALID);
        tester.assertElementNotPresent("issuetable");
        assertLinkNotPresentWithText(LOGIN);
    }

    public void testDeleteFilterPermissions()
    {
        administration.restoreData("TestFilterResultVisibility.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "dev");
        navigation.login("dev");

        assertCannotDeleteFilter(DELETE_FILTER_DEV);
        assertCannotDeleteFilter(DELETE_FILTER_DEV_URL_HACK);
        
        assertCannotDeleteFilter(DELETE_FILTER_PUBLIC);
        assertCannotDeleteFilter(DELETE_FILTER_PUBLIC_URL_HACK);

        navigation.login(ADMIN_USERNAME);
    }

    private void assertCannotDeleteFilter(String filterUrl)
    {
        tester.gotoPage(page.addXsrfToken(filterUrl));
        text.assertTextPresent(locator.page(), ERROR_MSG_CANNOT_DELETE_FILTER);
    }

    private void assertLinkPresentOnFilterView(String filterURL, String expectedLink)
    {
        tester.beginAt(filterURL);
        tester.assertLinkPresentWithText(expectedLink);
    }

    private void assertTextPresentOnFilterView(String filterURL, String expectedMessage, String expectedLink)
    {
        tester.beginAt(filterURL);
        if (!expectedMessage.contains("all"))
        {
            tester.assertElementNotPresent("issuetable");
        }
        else
        {
            tester.assertElementPresent("issuetable");
        }
        tester.assertLinkPresentWithText(expectedLink);
    }

    private void filterActionsWithCustomFields() throws SAXException, IOException
    {
        final List<CustomFieldValue> cfList = ImmutableList.of
                (
                        new CustomFieldValue("10000", CUSTOM_FIELD_TYPE_RADIO, "10000"),
                        new CustomFieldValue("10001", CUSTOM_FIELD_TYPE_SELECT, "abc"),
                        new CustomFieldValue("10002", CUSTOM_FIELD_TYPE_MULTISELECT, "batman"),
                        new CustomFieldValue("10003", CUSTOM_FIELD_TYPE_CHECKBOX, "10009"),
                        new CustomFieldValue("10004", CUSTOM_FIELD_TYPE_TEXTFIELD, "'release 1'")
                );

        String[] customFieldNames = new String[]{CUSTOM_FIELD_NAME_RADIO, CUSTOM_FIELD_NAME_SELECT, CUSTOM_FIELD_NAME_TEXT_FIELD,
                CUSTOM_FIELD_NAME_MULTI_CHECKBOX, CUSTOM_FIELD_NAME_MULTI_SELECT};

        addFieldsToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, customFieldNames);

        int i = 0;
        for (String anIssue : issues)
        {
            i++;
            log("editing issues to include custom fields for issue: " + anIssue);
            if ((i % 2) == 0)
            {
                editIssueWithCustomFields(anIssue, cfList);
            }
        }

        removeFieldFromFieldScreen(DEFAULT_FIELD_SCREEN_NAME, customFieldNames);

        for (CustomFieldValue cfValue : cfList)
        {
            log("Checking portlets for " + cfValue.getCfType());
            createFilterWithFields(cfValue.getCfId(), cfValue.getCfValue(), cfValue.getCfType());

            sortIssues("issuekey", "ASC");

            assertIssuesPresent(numberOfIssues, 2);
        }
    }

    /*----------- Helper Methods -------------*/
    private void addColumnOrder(int filterId, String columnName, String columnVal)
    {
        gotoManageFilterColumnOrder(filterId);
        tester.selectOption("fieldId", columnVal);
        tester.submit("add");
        text.assertTextPresent(locator.page(), columnName);
    }

    private void configureColumnOrder(int filterId, String columnName, String operation)
    {
        gotoManageFilterColumnOrder(filterId);
        try
        {
            WebTable fieldTable = tester.getDialog().getResponse().getTableWithID(ISSUETABLE_ID);
            for (int i = 0; i < fieldTable.getColumnCount(); i++)
            {
                String field = fieldTable.getCellAsText(ISSUETABLE_HEADER_ROW, i);
                if (field.contains(columnName))
                {
                    TableCell linkCell = fieldTable.getTableCell(ISSUETABLE_EDIT_ROW, i);
                    WebLink link = linkCell.getLinkWithImageText(operation);
                    if (link == null)
                    {
                        fail("No link with image '" + operation + "'.");
                    }
                    link.click();
                    return;
                }
            }
            log("Field '" + columnName + "' not in table.");
        }
        catch (SAXException e)
        {
            fail("Cannot find table with id '" + ISSUETABLE_ID + "'.");
        }
        catch (IOException e)
        {
            fail("Could not click link with image '" + operation + "'.");
        }
    }


    public void gotoManageFilterColumnOrder(int filterId)
    {
        navigation.manageFilters().myFilters();
        // TODO move this to FilterNavigation
        tester.clickLink("colOrder_" + filterId);
    }

    private void createFilterWithFields(String fieldId, String fieldValue, String fieldType)
    {
        String jql = "cf[" + fieldId + "] = " + fieldValue;
        log.log("Creating filter with jql = " + jql);
        backdoor.filters().createFilter(jql, "jql", true);
    }

    private void assertIssuesPresent(int numberOfIssues, int gap)
    {
        log("checking if all issues are present in filter");

        int i = 0;
        for (String anIssue : issues)
        {
            i++;
            if (i < numberOfIssues && (i % gap) == 0)
            {
                tester.assertLinkPresentWithText(anIssue);
            }
        }
    }
}
