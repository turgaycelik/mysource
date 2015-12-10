package com.atlassian.jira.util;

import javax.annotation.Nullable;

/**
 * Helper for getting User Contact information links
 *
 * @since v4.4
 */
public interface JiraContactHelper
{
    /**
     * <p/>
     * Get the full link text for the contact administration message as a snippet. This message is not puncuated or
     * capatilised and should be able to be inserted within a more complete message.
     * <p/>
     * <p/>
     * In English "Contact your Jira Administrators"
     * <p/>
     * <p/>
     * In some cases (e.g. empty base URL, or contact form turned off) this will just return a non-hyperlinked text
     * message equivalent to {@link #getAdministratorContactMessage(I18nHelper)}.
     *
     * @param baseUrl Base Url of the application. If <code>null</code>, non-hyperlinked text will be returned
     * @param i18nHelper i18NHelper
     * @return String containing HTML
     */
    String getAdministratorContactLinkHtml(@Nullable String baseUrl, I18nHelper i18nHelper);

    /**
     * Get the text for the contact administration message as a snippet. This message is not puncuated or capatilised
     * and should be able to be inserted within a more complete message. If you want a hyperlink then use {@link
     * #getAdministratorContactLinkHtml(String baseUrl, I18nHelper i18nHelper)}
     * <p/>
     * in English "contact your Jira Administrators"
     *
     * @param i18nHelper i18NHelper
     * @return String containing HTML
     */
    String getAdministratorContactMessage(I18nHelper i18nHelper);

    /**
     * Get the information whether Contact Administrator form can be used by users
     *
     * @since 6.1
     */
    boolean isAdministratorContactFormEnabled();

    /**
     * <p>Get link for the contact administrator page. This method <b>will not</b>
     * respect Contact Form's settings - it will always return a link</p>
     * <p>For example: <code>/jira/secure/ContactAdministrators!default.jspa</code></p>
     *
     * @param baseUrl Base Url of the application
     * @return @since 6.1
     */
    String getAdministratorContactLink(String baseUrl);

}
