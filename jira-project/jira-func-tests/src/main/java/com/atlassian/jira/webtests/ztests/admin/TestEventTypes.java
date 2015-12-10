/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestEventTypes extends JIRAWebTest
{
    private final String CUSTOM_EVENT_TYPE_NAME = "Custom Event Type Name";
    private final String CUSTOM_EVENT_TYPE_DESC = "Custom Event Type Description";

    private final String CREATE_EVENT_TYPE_NAME = "Issue Created";
    private final String COMMENT_EVENT_TYPE_NAME = "Issue Commented";
    private final String COMMENT_EDITED_EVENT_TYPE_NAME = "Issue Comment Edited";
    private final String REOPENED_EVENT_TYPE_NAME = "Issue Reopened";
    private final String WORKLOG_UPDATED_EVENT_TYPE_NAME = "Issue Worklog Updated";
    private final String WORKLOG_DELETED_EVENT_TYPE_NAME = "Issue Worklog Deleted"; 
    private final String GENERIC_EVENT_TYPE_NAME = "Generic Event";

    private final String[] eventOrder = new String[]{CREATE_EVENT_TYPE_NAME, COMMENT_EVENT_TYPE_NAME, COMMENT_EDITED_EVENT_TYPE_NAME,
                                                     REOPENED_EVENT_TYPE_NAME, WORKLOG_UPDATED_EVENT_TYPE_NAME,
                                                    WORKLOG_DELETED_EVENT_TYPE_NAME, GENERIC_EVENT_TYPE_NAME, CUSTOM_EVENT_TYPE_NAME};


    private final String CURRENT_ASSIGNEE_NOTIFICATION_TYPE = "Current Assignee";
    private final String CURRENT_ASSIGNEE_NOTIFICATION_CB_CODE = "Current_Assignee";

    private final String GENERIC_EVENT_TEMPLATE = "Generic Event";

    public TestEventTypes(String name)
    {
        super(name);
    }

    public void testAddEventType()
    {
        restoreBlankInstance();
        addEventType(CUSTOM_EVENT_TYPE_NAME, CUSTOM_EVENT_TYPE_DESC, GENERIC_EVENT_TEMPLATE);
    }

    public void testAddEventTypeWithErrors()
    {
        restoreBlankInstance();
        gotoAdmin();
        clickLink("eventtypes");
        setFormElement("name", "");
        submit("Add");
        assertTextPresent("You must specify an event name");
        assertTextPresent("You must select a default template to associate with this event");
        setFormElement("name", "Issue Created");
        submit("Add");
        assertTextPresent("An event with this name already exists");
        assertTextPresent("You must select a default template to associate with this event");
    }

    public void testDeleteEventType()
    {
        restoreBlankInstance();
        addEventType(CUSTOM_EVENT_TYPE_NAME, CUSTOM_EVENT_TYPE_DESC, GENERIC_EVENT_TEMPLATE);
        deleteEventType(CUSTOM_EVENT_TYPE_NAME);
        assertTextNotPresent(CUSTOM_EVENT_TYPE_NAME);
    }

    public void testEditEventType()
    {
        restoreBlankInstance();
        addEventType(CUSTOM_EVENT_TYPE_NAME, CUSTOM_EVENT_TYPE_DESC, GENERIC_EVENT_TEMPLATE);

        gotoAdmin();
        clickLink("eventtypes");
        clickLink("edit_" + CUSTOM_EVENT_TYPE_NAME);
        assertTextPresent("Update");

        final String NEW_NAME = "Custom Event Type New Name";
        final String NEW_DESC = "Custom Event Type New Description";
        final String NEW_TEMPLATE = "Issue Created";
        
        tester.setFormElement("name", NEW_NAME);
        tester.setFormElement("description", NEW_DESC);
        tester.selectOption("templateId", NEW_TEMPLATE);
        tester.submit("Update");

        checkEventTypeDetails(NEW_NAME, NEW_DESC, EVENT_TYPE_INACTIVE_STATUS, NEW_TEMPLATE, null, null);
    }

    public void testCreateNotificationAssociation()
    {
        restoreBlankInstance();
        // Add the event type
        addEventType(CUSTOM_EVENT_TYPE_NAME, CUSTOM_EVENT_TYPE_DESC, GENERIC_EVENT_TEMPLATE);
        String eventTypeId = getEventTypeIDWithName(CUSTOM_EVENT_TYPE_NAME);

        // Create the notification association
        gotoAdmin();
        clickLink("notification_schemes");
        clickLinkWithText("Default Notification Scheme");
        clickLink("add_" + eventTypeId);
        checkCheckbox("type", CURRENT_ASSIGNEE_NOTIFICATION_CB_CODE);
        submit("Add");

        // check the notification has been made
        checkNotificationForEvent(CUSTOM_EVENT_TYPE_NAME, CURRENT_ASSIGNEE_NOTIFICATION_TYPE, GENERIC_EVENT_TEMPLATE);

        checkEventTypeDetails(CUSTOM_EVENT_TYPE_NAME, CUSTOM_EVENT_TYPE_DESC, EVENT_TYPE_ACTIVE_STATUS, GENERIC_EVENT_TEMPLATE, "Default Notification Scheme", null);
    }

    public void testEventDefsOrder()
    {
        restoreBlankInstance();
        addEventType(CUSTOM_EVENT_TYPE_NAME, CUSTOM_EVENT_TYPE_DESC, GENERIC_EVENT_TEMPLATE);

        gotoAdmin();
        clickLink("eventtypes");

        assertTextSequence(eventOrder);
    }

    public void testNotificationsEventOrder()
    {
        restoreBlankInstance();
        addEventType(CUSTOM_EVENT_TYPE_NAME, CUSTOM_EVENT_TYPE_DESC, GENERIC_EVENT_TEMPLATE);
        gotoAdmin();
        clickLink("notification_schemes");
        clickLinkWithText("Default Notification Scheme");

        assertTextSequence(eventOrder);

        clickLink("add_1");
        assertTextSequence(eventOrder);
                
    }

    // JRA-10274: custom events with an apostrophe in their name do not display correctly due to a bug in the
    // I18nBean.getText(String) method.
    public void testEventWithApostropheDisplaysCorrectly()
    {
        restoreBlankInstance();

        gotoAdmin();
        tester.clickLink("eventtypes");
        tester.setFormElement("name", "This event isn't named nicely");
        tester.selectOption("templateId", "Generic Event");
        tester.submit("Add");
        tester.assertTextPresent("This event isn&#39;t named nicely");

        // check Notification Schemes page
        tester.clickLink("notification_schemes");
        tester.clickLinkWithText("Default Notification Scheme");
        tester.assertTextPresent("This event isn&#39;t named nicely");

        // check View Workflow Transition page

        // copy the current workflow
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira").textView().goTo();

        // add a new transition
        tester.clickLink("add_trans_3");
        tester.setFormElement("transitionName", "Test transition");
        tester.setFormElement("description", "");
        tester.submit("Add");
        tester.clickLinkWithText("Test transition");

        // edit the post functions to fire our new event
        tester.clickLinkWithText("Post Functions");
        tester.clickLinkWithText("Edit", 1);
        tester.selectOption("eventTypeId", "This event isn't named nicely");
        tester.submit("Update");
        tester.assertTextPresent("This event isn&#39;t named nicely");
    }

}
