package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a link between a JIRA issue and a remote object in a remote application.
 * Examples may include:
 * <ul>
 * <li>a JIRA issue on another JIRA server</li>
 * <li>a Confluence page</li>
 * <li>a ticket in a helpdesk system</li>
 * <li>a test case in a testing system</li>
 * </ul>
 *
 * @since v5.0
 * @see RemoteIssueLinkBuilder
 */
@PublicApi
public final class RemoteIssueLink
{

    public static String APPLICATION_TYPE_JIRA = "com.atlassian.jira";
    public static String APPLICATION_TYPE_CONFLUENCE = "com.atlassian.confluence";

    private final Long id;
    private final Long issueId;
    private final String globalId;
    private final String title;
    private final String summary;
    private final String url;
    private final String iconUrl;
    private final String iconTitle;
    private final String relationship;
    private final Boolean resolved;
    private final String statusName;
    private final String statusDescription;
    private final String statusIconUrl;
    private final String statusIconTitle;
    private final String statusIconLink;
    private final String statusCategoryKey;
    private final String statusCategoryColorName;
    private final String applicationType;
    private final String applicationName;

    public RemoteIssueLink(final Long id, final Long issueId, final String globalId, final String title, final String summary, final String url, final String iconUrl, final String iconTitle, final String relationship, final Boolean resolved, final String statusIconUrl, final String statusIconTitle, final String statusIconLink, final String applicationType, final String applicationName)
    {
        this(id, issueId, globalId, title, summary, url, iconUrl, iconTitle, relationship, resolved, statusIconUrl, statusIconTitle, statusIconLink, applicationType, applicationName, null, null, null, null);
    }

    public RemoteIssueLink(final Long id, final Long issueId, final String globalId, final String title, final String summary, final String url, final String iconUrl, final String iconTitle, final String relationship, final Boolean resolved, final String statusIconUrl, final String statusIconTitle, final String statusIconLink, final String applicationType, final String applicationName, final String statusName, final String statusDescription, final String statusCategoryKey, final String statusCategoryColorName)
    {
        this.id = id;
        this.issueId = issueId;
        this.globalId = globalId;
        this.title = title;
        this.summary = summary;
        this.url = url;
        this.iconUrl = iconUrl;
        this.iconTitle = iconTitle;
        this.relationship = relationship;
        this.resolved = resolved;
        this.statusName = statusName;
        this.statusDescription = statusDescription;
        this.statusIconUrl = statusIconUrl;
        this.statusIconTitle = statusIconTitle;
        this.statusIconLink = statusIconLink;
        this.statusCategoryKey = statusCategoryKey;
        this.statusCategoryColorName = statusCategoryColorName;
        this.applicationType = applicationType;
        this.applicationName = applicationName;
    }

    /**
     * Returns the id of the link. This is used to refer to a link when updating and deleting.
     * @return the id of the link
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Returns the id of the issue we are linking with.
     * @return the id of the issue we are linking with
     */
    public Long getIssueId()
    {
        return issueId;
    }

    /**
     * Returns a String which uniquely identifies the object in the remote application. This value must be unique
     * across all remote objects in all remote applications. The maximum length of this field is 255 characters.
     * <p>
     * For simplicity, this could simply be the {@link #getUrl() url}, although this is not wise if there is a chance
     * the url may change.
     * <p>
     * This value is not displayed in the JIRA user interface. We suggest including in this field any values that will
     * be needed by a renderer plugin, for example the application url and remote object identifier to make REST calls.
     * <p>
     * For example: "url=http://www.myapplication.com&ticketid=12345"
     *
     * @return a String which uniquely identifies the object in the remote application
     */
    public String getGlobalId()
    {
        return globalId;
    }

    /**
     * Returns a String to identify the remote object. This is shown as the text for the {@link #getUrl() url} in the
     * JIRA user interface.
     * <p>
     * For example: a JIRA issue key, a Confluence page title, a helpdesk ticket number
     *
     * @return a String to identify the remote object
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns a summary of the remote object. This is shown after the {@link #getTitle() title} in the JIRA user
     * interface.
     * <p>
     * For example: a JIRA issue summary, a helpdesk ticket title
     *
     * @return a summary of the remote object
     */
    public String getSummary()
    {
        return summary;
    }

    /**
     * Returns a url to the object in the remote application.
     * <p>
     * For example: a confluence page url
     *
     * @return a url to the object in the remote application
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Returns a url for an icon to represent the remote object. The size of the icon should be 16x16 pixels.
     * <p>
     * For example: http://mycompany.com/jira/images/icons/bug.png
     *
     * @return a url for an icon to represent the remote object
     */
    public String getIconUrl()
    {
        return iconUrl;
    }

    /**
     * Returns the tool tip text for the {@link #getIconUrl() icon}. The {@link #getApplicationName() applicationName}
     * is prepended to this value to form the icon tool tip text.
     * <p>
     * For example: "Bug - A problem which impairs or prevents the functions of the product"
     *
     * @return the tool tip text for the {@link #getIconUrl() icon}
     */
    public String getIconTitle()
    {
        return iconTitle;
    }

    /**
     * Returns a String which describes the relationship between the JIRA issue and the remote object.
     * <p>
     * For example: "relates to", "is mentioned in", "links to". It could even be a simple noun, such as "Test Case".
     *
     * @return a String which describes the relationship between the JIRA issue and the remote object
     */
    public String getRelationship()
    {
        return relationship;
    }

    /**
     * Returns a Boolean to indicate whether the remote object is resolved or not. If the value is true, it is
     * "resolved", and if the value is false, it is "unresolved". This value will only make sense where the remote
     * object is something that is resolvable (eg. an issue, support ticket, etc). It will not make sense for things
     * like wiki pages. In that case, the value should be null.
     * <p>
     * If the value is true, the remote issue link be shown in a strikethrough font in the user interface.
     *
     * @return a Boolean to indicate whether the remote object is resolved or not
     */
    public Boolean isResolved()
    {
        return resolved;
    }

    /**
     * Returns a url for an icon representing the status of the remote object. The size of the icon should be 16x16
     * pixels. This icon is shown next to the {@link #getSummary() summary} in the JIRA user interface, and is right
     * justified. This icon does not have to strictly represent a "status", and may instead represent some secondary
     * information about the link.
     * <p>
     * For example: the status of a JIRA issue, the author of a test case
     *
     * @return a url for an icon representing the status of the remote object
     */
    public String getStatusIconUrl()
    {
        return statusIconUrl;
    }

    /**
     * Returns the tool tip text for the {@link #getStatusIconUrl() status icon}. If null, no tool tip text is shown.
     * This value is not used if the {@link #getStatusIconUrl() statusIconUrl} is null.
     *
     * @return the tool tip text for the {@link #getStatusIconUrl() status icon}
     */
    public String getStatusIconTitle()
    {
        return statusIconTitle;
    }

    /**
     * Returns a hyperlink for the {@link #getStatusIconUrl() status icon}. If null, no hyperlink is shown. This value
     * is not used if the {@link #getStatusIconUrl() statusIconUrl} is null.
     *
     * @return a hyperlink for the {@link #getStatusIconUrl() status icon}
     */

    public String getStatusIconLink()
    {
        return statusIconLink;
    }

    /**
     * Returns the type of remote application that we are linking with. This should be the product name, namespaced
     * with the product company. This value is not displayed in the JIRA user interface. Links are grouped based on the
     * applicationType and {@link #getApplicationName() applicationName} in the user interface.
     * <p>
     * Renderering plugins can register to render a certain type of application. It is not possible to use a plugin
     * to render links without an applicationType.
     * <p>
     * For example: "com.atlassian.confluence", "com.mycompany.myproduct"
     *
     * @return the type of remote application that we are linking with
     */
    public String getApplicationType()
    {
        return applicationType;
    }

    /**
     * Returns the human-readable name of the remote application instance that contains the remote object. Links are
     * grouped based on the {@link #getApplicationType() applicationType} and applicationName in the user interface.
     * The applicationName is prepended to the {@link #getIconTitle() iconTitle} to form the icon tool tip text.
     * <p>
     * For example: "My Company's Confluence", "HR JIRA", "Helpdesk JIRA"
     *
     * @return the human-readable name of the remote application instance that contains the remote object
     */
    public String getApplicationName()
    {
        return applicationName;
    }

    public String getStatusName()
    {
        return statusName;
    }

    public String getStatusDescription()
    {
        return statusDescription;
    }

    public String getStatusCategoryKey()
    {
        return statusCategoryKey;
    }

    public String getStatusCategoryColorName()
    {
        return statusCategoryColorName;
    }

    public boolean hasStatusCategory()
    {
        return StringUtils.isNotBlank(getStatusCategoryColorName()) || StringUtils.isNotBlank(getStatusCategoryKey());
    }
}
