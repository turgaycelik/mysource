package com.atlassian.jira.functest.framework.admin;

/**
 * Grouping of admin tasks related to the Attachments section
 *
 * @since v4.1
 */
public interface Attachments
{
    /**
     * Enables attachments and configures JIRA to use the default attachment path (<code>jira.home</code>/data/attachments).
     *
     * @see com.atlassian.jira.functest.framework.Administration#getCurrentAttachmentPath()
     */
    void enable();

    void enable(String maxAttachmentSize);

    void disable();

    void enableZipSupport();

    void disableZipSupport();

    String getCurrentAttachmentPath();
}
