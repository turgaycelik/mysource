package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.admin.SendBulkMail;

/**
 *
 * @since v4.4
 */
public class DefaultSendBulkMail implements SendBulkMail
{
    private static final String SEND_BULK_MAIL_PAGE_LINK_ID = "send_email";
    private final Navigation navigation;

    public DefaultSendBulkMail(final Navigation navigation)
    {
        this.navigation = navigation;
    }

    @Override
    public SendBulkMail goTo()
    {
        navigation.gotoAdminSection(SEND_BULK_MAIL_PAGE_LINK_ID);
        return this;
    }
}
