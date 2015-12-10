/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AbstractTestIssueNavigatorView;
import com.meterware.httpunit.WebLink;
import org.xml.sax.SAXException;

import java.net.URL;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestIssueNavigatorFullContentView extends AbstractTestIssueNavigatorView
{
    private URL baseUrl;

    public TestIssueNavigatorFullContentView(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        baseUrl = getEnvironmentData().getBaseUrl();
    }

    public void tearDown()
    {
        baseUrl = null;
        super.tearDown();
    }

    public void testFullContentViewItem1()
    {
        goToView("status=Open");

        Item item = item1;
        verifyCommons(item);

        assertTextPresentBeforeText("Affects Version/s", "None");
        assertTextPresentBeforeText("Fix Version/s", "New Version 5");

        assertTextPresentBeforeText("duplicates", "HSP-10");
        assertTextPresentBeforeText("HSP-10", "Big 01");
        assertTextPresentBeforeText("Big 01", "Resolved");

        assertTextPresentBeforeText("CascadingSelectField", "value 01");
        assertTextPresentBeforeText("CascadingSelectField", "value 013");

        // links
        assertLinkPresentWithText("New Version 5");
        assertLinkPresent(new String[]{
                baseUrl + "/secure/IssueNavigator.jspa",
                "fixfor=10000",
                "reset=true",
                "mode=hide",
                "sorter/order=ASC",
                "sorter/field=priority",
                "pid=10000"});

        assertUserProfileLink(item.getAttribute(ATT_ASSIGNEE), "dev");

        assertLinkPresentWithText("HSP-10");
        assertLinkPresentWithUrl(baseUrl + "/browse/HSP-10", true);
        assertLinkNotPresentWithText("HSP-11");
        assertLinkNotPresentWithText("HSP-12");

        assertSingleVersionPicker("New Version 5", "10000");
        assertVersionPicker("New Version 2", "10003");
        assertVersionPicker("New Version 4", "10001");
    }

    public void testFullContentViewItem1DaysTimeFormat()
    {
        reconfigureTimetracking(FORMAT_DAYS);
        testFullContentViewItem1();
    }

    public void testFullContentViewItem1HoursTimeFormat()
    {
        reconfigureTimetracking(FORMAT_HOURS);
        testFullContentViewItem1();
    }


    protected void goToView(String jql)
    {
        tester.gotoPage("/sr/jira.issueviews:searchrequest-fullcontent/temp/SearchRequest.html?jqlQuery=" + jql + "&tempMax=1000");
    }

    public void testFullContentViewItem2()
    {
        goToView("status=\"In Progress\"");

        Item item = item2;
        verifyCommons(item);

        assertTextPresentBeforeText("Affects Version/s", "New Version 2");
        assertTextPresentBeforeText("Fix Version/s", "New Version 4");

        assertTextPresentBeforeText("duplicates", "HSP-10");
        assertTextPresentBeforeText("HSP-10", "Big 01");
        assertTextPresentBeforeText("Big 01", "Resolved");

        assertTextPresentBeforeText("CascadingSelectField", "value 05");

        assertTextPresentBeforeText("ProjectPickerField", "homosapien");
        assertLinkPresent(new String[]{baseUrl + "/secure/BrowseProject.jspa", "id=10000"});

        // links
        assertLinkPresentWithText("New Version 4");
        assertLinkPresent(new String[]{
                baseUrl + "/secure/IssueNavigator.jspa",
                "fixfor=10001",
                "reset=true",
                "mode=hide",
                "sorter/order=ASC",
                "sorter/field=priority",
                "pid=10000"});

        assertUserProfileLink(item.getAttribute(ATT_ASSIGNEE), ADMIN_USERNAME);

        assertLinkPresentWithText("HSP-10");
        assertLinkPresentWithUrl(baseUrl + "/browse/HSP-10", true);
        assertLinkNotPresentWithText("HSP-11");
        assertLinkNotPresentWithText("HSP-12");

        assertSingleVersionPicker("New Version 5", "10000");
        assertVersionPicker("New Version 5", "10000");
    }

    public void testFullContentViewItem2DaysTimeFormat()
    {
        reconfigureTimetracking(FORMAT_DAYS);
        testFullContentViewItem2();
    }

    public void testFullContentViewItem2HoursTimeFormat()
    {
        reconfigureTimetracking(FORMAT_HOURS);
        testFullContentViewItem2();
    }

    public void testFullContentViewItem3()
    {
        goToView("status=\"Resolved\"");

        Item item = item3;
        verifyCommons(item);

        assertTextPresentBeforeText("Affects Version/s", "New Version 4");
        assertTextPresentBeforeText("Fix Version/s", "New Version 5");

        assertTextPresentBeforeText("duplicates", "HSP-11");
        assertTextPresentBeforeText("HSP-11", "Minor Bug 01");
        assertTextPresentBeforeText("Minor Bug 01", "In Progress");
        assertTextPresentBeforeText("is duplicated by", "HSP-11");
        assertTextPresentBeforeText("HSP-11", "Minor Bug 01");
        assertTextPresentBeforeText("Minor Bug 01", "In Progress");
        assertTextPresentBeforeText("is duplicated by", "HSP-12");
        assertTextPresentBeforeText("HSP-12", "Feature 00");
        assertTextPresentBeforeText("Feature 00", "Open");

        assertTextPresentBeforeText("CascadingSelectField", "value 01");
        assertTextPresentBeforeText("value 01", "value 013");
        assertTextPresentBeforeText("GroupPickerField", "jira-developers");

        // links
        assertLinkPresentWithText("New Version 4");
        assertLinkPresent(new String[]{
                baseUrl + "/secure/IssueNavigator.jspa",
                "version=10001",
                "reset=true",
                "mode=hide",
                "sorter/order=ASC",
                "sorter/field=priority",
                "pid=10000"});
        assertLinkPresentWithText("New Version 5");
        assertLinkPresent(new String[]{
                baseUrl + "/secure/IssueNavigator.jspa",
                "fixfor=10000",
                "reset=true",
                "mode=hide",
                "sorter/order=ASC",
                "sorter/field=priority",
                "pid=10000"});

        assertUserProfileLink(item.getAttribute(ATT_ASSIGNEE), "dev");

        assertLinkNotPresentWithText("HSP-10");
        assertLinkPresentWithText("HSP-11");
        assertLinkPresentWithUrl(baseUrl + "/browse/HSP-11", true);
        assertLinkPresentWithText("HSP-12");
        assertLinkPresentWithUrl(baseUrl + "/browse/HSP-12", true);

        assertSingleVersionPicker("New Version 5", "10000");
        assertVersionPicker("New Version 2", "10003");
        assertVersionPicker("New Version 4", "10001");
    }

    public void testFullContentViewItem3DaysFormat()
    {
        reconfigureTimetracking(FORMAT_DAYS);
        testFullContentViewItem3();
    }

    public void testFullContentViewItem3HoursFormat()
    {
        reconfigureTimetracking(FORMAT_HOURS);
        testFullContentViewItem3();
    }

    //special test for JRA-11613
    public void testFullContentViewItem1NullPriority()
    {
        restoreData("TestSearchRequestViewsAndIssueViewsWithPriorityNull.xml");
        goToView("status=\"Resolved\"");

        assertTextNotPresent("Priority");
    }

    private void assertCustomField(Item item, final String title, final String cfName)
    {
        final CustomField customField = item.getCustomFieldByName(cfName);
        if (customField == null)
        {
            assertTextNotPresent(title);
        }
        else
        {
            assertTextPresent(title);
        }
    }

    private void assertAttribute(Item item, final String title, final String attName)
    {
        final String attribute = item.getAttribute(attName);
        if (attribute == null || attribute.length() == 0)
        {
            assertTextNotPresent(title);
        }
        else
        {
            assertTextPresent(title);
        }
    }

    private void assertProjectPickerField(Item item)
    {
        final String title = "ProjectPickerField";
        final CustomField customField = item.getCustomFieldByName(CF_PROJECT_PICKER_FIELD);
        if (customField == null)
        {
            assertTextNotPresent(title);
        }
        else
        {
            assertTextPresentBeforeText(title, "homosapien");
            assertLinkPresent(new String[]{baseUrl + "/secure/BrowseProject.jspa", "id=10000"});
        }
    }

    private void assertCustomFieldValues(Item item, final String title, final String cfName)
    {
        final CustomField customField = item.getCustomFieldByName(cfName);
        if (customField == null)
        {
            assertTextNotPresent(title);
        }
        else
        {
            final List<CustomField.Value> values = customField.getValues();
            if (values != null)
            {
                for (final CustomField.Value  value : values)
                {
                    assertTextPresentBeforeText(title, value.getValue());
                }
            }
        }
    }

    private void verifyCommons(Item item)
    {
        assertTextPresent(item.getAttribute(ATT_KEY));
        assertLinkPresentWithText(item.getAttribute(ATT_SUMMARY));
        assertLinkPresentWithUrl(baseUrl + "/browse/" + item.getAttribute(ATT_KEY), false);
        assertTextPresentBeforeText("Created", "Updated");
        assertTextPresentBeforeText("Status", item.getAttribute(ATT_STATUS));
        assertTextPresentBeforeText("Project", "homosapien");
        assertLinkPresentWithText("homosapien");
        assertLinkPresent(new String[]{baseUrl + "/secure/BrowseProject.jspa", "id=10000"});

        assertTextPresentBeforeText("Type", item.getAttribute(ATT_TYPE));
        assertTextPresentBeforeText("Priority", item.getAttribute(ATT_PRIORITY));
        assertTextPresentBeforeText("Reporter", item.getAttribute(ATT_REPORTER));
        assertTextPresentBeforeText("Assignee", item.getAttribute(ATT_ASSIGNEE));
        assertTextPresentBeforeText("Resolution", item.getAttribute(ATT_RESOLUTION));
        assertAttribute(item, "Description", ATT_DESCRIPTION);
        assertUserProfileLink(item.getAttribute(ATT_REPORTER), ADMIN_USERNAME);

        if (FORMAT_DAYS.equals(timeFormat))
        {
            assertTimeUnknownIfNotSet("Remaining Estimate", item.getAttribute(ATT_REMAINING_ESTIMATE_DAYS));
            assertTimeUnknownIfNotSet("Time Spent", item.getAttribute(ATT_TIMESPENT_DAYS));
            assertTimeUnknownIfNotSet("Original Estimate", item.getAttribute(ATT_TIMEORIGINALESTIMATE_DAYS));
        }
        else if (FORMAT_HOURS.equals(timeFormat))
        {
            assertTimeUnknownIfNotSet("Remaining Estimate", item.getAttribute(ATT_REMAINING_ESTIMATE_HOURS));
            assertTimeUnknownIfNotSet("Time Spent", item.getAttribute(ATT_TIMESPENT_HOURS));
            assertTimeUnknownIfNotSet("Original Estimate", item.getAttribute(ATT_TIMEORIGINALESTIMATE_HOURS));
        }
        else
        {
            assertTimeUnknownIfNotSet("Remaining Estimate", item.getAttribute(ATT_REMAINING_ESTIMATE));
            assertTimeUnknownIfNotSet("Time Spent", item.getAttribute(ATT_TIMESPENT));
            assertTimeUnknownIfNotSet("Original Estimate", item.getAttribute(ATT_TIMEORIGINALESTIMATE));
        }

        // components
        assertComponents(item);

        // comments
        assertComments(item);

        // attachments
        assertAttachments(item);

        // custom fields
        assertCustomField(item, "DateTimeField", CF_DATE_TIME_FIELD);
        assertCustomFieldValues(item, "MultiCheckboxesField", CF_MULTI_CHECKBOXES_FIELD);
        assertCustomFieldValues(item, "MultiSelectField", CF_MULTI_SELECT_FIELD);
//        assertCustomFieldValues(item, "NumberField", CF_NUMBER_FIELD);
        assertCustomFieldValues(item, "RadioButtonsField", CF_RADIO_BUTTONS_FIELD);
        assertCustomFieldValues(item, "SelectList", CF_SELECT_LIST);
        assertCustomFieldValues(item, "TextField255", CF_TEXT_FIELD255);
        assertCustomField(item, "DatePickerField", CF_DATE_PICKER_FIELD);
        assertCustomFieldValues(item, "FreeTextField", CF_FREE_TEXT_FIELD);

        assertProjectPickerField(item);

    }

    private void assertAttachments(Item item)
    {
        final List<String> attachments = item.getAttachments();
        if (attachments.isEmpty())
        {
            assertTextNotPresent("Attachments");
        }
        else
        {
            for (final String attachment : attachments)
            {
                assertTextPresentBeforeText("Attachments", attachment);
            }
        }
    }

    private void assertComments(Item item)
    {
        final List<Comment> comments = item.getComments();
        if (comments.isEmpty())
        {
            assertTextNotPresent("Comments");
        }
        else
        {
            for (final Comment comment : comments)
            {
                assertTextPresentBeforeText("Comments", comment.getValue());
            }
        }
    }

    private void assertComponents(Item item)
    {
        final List<String> components = item.getComponents();
        if (components.isEmpty())
        {
            assertTextPresentBeforeText("Component/s", "None");
        }
        else
        {
            for (final String component : components)
            {
                assertTextPresentBeforeText("Component/s", component);
            }
        }
    }

    private void assertTimeUnknownIfNotSet(String title, String attribute)
    {
        if (attribute == null)
        {
            assertTextPresentBeforeText(title, "Not Specified");
        }
        else
        {
            assertTextPresentBeforeText(title, attribute);
        }
    }

    private void assertUserProfileLink(String title, String user)
    {
        assertLinkPresentWithText(title);
        assertLinkPresent(new String[]{baseUrl + "/secure/ViewProfile.jspa", "name=" + user});
    }

    private void assertSingleVersionPicker(String title, String value)
    {
        assertCustonFieldLink(title, value, "10017");
    }

    private void assertVersionPicker(String title, String value)
    {
        assertCustonFieldLink(title, value, "10019");
    }

    private void assertCustonFieldLink(String title, String value, String customFieldId)
    {
        assertLinkPresentWithText(title);

        assertLinkPresent(new String[]{
                baseUrl + "/secure/IssueNavigator.jspa",
                "customfield_" + customFieldId + "=" + value,
                "reset=true",
                "mode=hide",
                "sorter/order=ASC",
                "sorter/field=priority",
                "pid=10000"});
    }

    private void assertLinkPresentWithUrl(String url, boolean exactMatch)
    {
        if (url != null)
        {
            try
            {
                WebLink[] links = getDialog().getResponse().getLinks();
                for (WebLink link : links)
                {
                    final String urlString = link.getURLString();
                    if (exactMatch)
                    {
                        if (url.equals(urlString))
                        {
                            return;
                        }
                    }
                    else
                    {
                        if (urlString != null && urlString.indexOf(url) >= 0)
                        {
                            return;
                        }
                    }
                }
                fail("Link '" + url + "' not found in response");
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void assertLinkPresent(String[] urlParts)
    {
        if (urlParts != null)
        {
            try
            {
                WebLink[] links = getDialog().getResponse().getLinks();
                assertTrue(
                        "Link with '" + urlParts + "' not found in response",
                        anyLinkContainsAllParts(links, urlParts));
            }
            catch (SAXException e)
            {
                // ignored
            }
        }
    }

    private boolean anyLinkContainsAllParts(WebLink[] links, String[] urlParts)
    {
        boolean result = false;
        for (WebLink link : links)
        {
            if (linkContainsAllParts(link.getURLString(), urlParts))
            {
                return true;
            }
        }
        return result;
    }

    private boolean linkContainsAllParts(String link, String[] urlParts)
    {
        boolean result = true;
        if (link != null)
        {
            for (String urlPart : urlParts)
            {
                if (link.indexOf(urlPart) < 0)
                {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Overriden to do nothing
     */
    protected void initFieldColumnMap()
    {
    }

    public void testTimeTracking() throws SAXException
    {
        activateSubTasks();
        subTaskify("HSP-12", "HSP-10");
        subTaskify("HSP-11", "HSP-10");

        displayAllIssues();
        goToView("");

        assertTextSequence(new String[] {
                " Remaining Estimate:", "1 day, 30 minutes", "Remaining Estimate:", "1 day",
                " Time Spent:", "3 hours, 20 minutes", "Time Spent:", "Not Specified",
                " Original Estimate:", "1 day", "Original Estimate:", "1 day"
        });
    }

}
