package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * Default implementation of {@link com.atlassian.jira.functest.framework.admin.EditNotifications}.
 *
 * @since v4.4
 */
public class EditNotificationsImpl extends AbstractFuncTestUtil implements EditNotifications
{
    public EditNotificationsImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
    }

    @Override
    public int notificationSchemeId()
    {
        return Integer.parseInt(locators.id("notification-scheme-id")
                .getNode().getAttributes().getNamedItem("value").getNodeValue());
    }

    @Override
    public EditNotifications addNotificationsForEvent(int eventId, NotificationType notificationType)
    {
        goAndSelectType(eventId, notificationType);
        submitAdd();
        return this;
    }

    @Override
    public EditNotifications addNotificationsForEvent(int eventId, NotificationType notificationType, String paramValue)
    {
        goAndSelectType(eventId, notificationType);
        tester.setFormElement(notificationType.uiCode(), paramValue);
        submitAdd();
        return this;
    }

    private void goAndSelectType(int eventId, NotificationType notificationType)
    {
        tester.clickLink("add_" + eventId);
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        tester.checkRadioOption("type", notificationType.uiCode());
    }

    private void submitAdd()
    {
        tester.submit("Add");
    }
}
