package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * Default implementation of {@link com.atlassian.jira.functest.framework.admin.NotificationSchemes}.
 *
 * @since v4.4
 */
public class NotificationSchemesImpl extends AbstractFuncTestUtil implements NotificationSchemes
{
    private static final String MAIN_LINK_ID = "notification_schemes";
    private static final String ADD_NOTIFICATION_SCHEME_LINK_ID = "add-notification-scheme";

    private final EditNotifications editNotifications;

    public NotificationSchemesImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
        this.editNotifications = new EditNotificationsImpl(tester, environmentData, childLogIndentLevel());
    }

    @Override
    public NotificationSchemes goTo()
    {
        navigation().gotoAdminSection(MAIN_LINK_ID);
        return this;
    }

    @Override
    public EditNotifications addNotificationScheme(String name, String description)
    {
        tester.clickLink(ADD_NOTIFICATION_SCHEME_LINK_ID);
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        tester.setFormElement("name", name);
        tester.setFormElement("description", description);
        tester.submit("Add");
        return editNotifications;
    }

    @Override
    public EditNotifications editNotifications(int id)
    {
        tester.clickLink(id + "_edit");
        return editNotifications;
    }
}
