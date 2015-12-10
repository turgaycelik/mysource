package com.atlassian.jira.plugin.issuelink;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;

import java.util.Map;

/**
 * Module descriptor for an issue link renderer that customises the way issue links are rendered.
 *
 * @since v5.0
 */
public interface IssueLinkRendererModuleDescriptor extends JiraResourcedModuleDescriptor<IssueLinkRenderer>
{
    /**
     * Returns <tt>true</tt> if the module descriptor is the default handler for issue links.
     *
     * @return <tt>true</tt> if the module descriptor is the default handler for issue links
     */
    boolean isDefaultHandler();

    /**
     * Returns <tt>true</tt> if the module descriptor can handle the application type. Only one module descriptor should
     * be capable of handling an application type. If there are more than one modules that can handle an application
     * type, only the first module (in loading order) will be used. If no descriptors handle an application type, then
     * the default issue link renderer module descriptor is used ({@see #isDefaultHandler}.
     *
     * @param applicationType application type to handle
     * @return <tt>true</tt> if the module descriptor can handle the application type
     */
    boolean handlesApplicationType(String applicationType);

    /**
     * Returns the initial HTML to place between the relationship text and the delete icon.
     * No remote calls should be made to construct the initial HTML.
     *
     * @param remoteIssueLink remote issue link
     * @return initial HTML that goes between the relationship text and the delete icon
     */
    String getInitialHtml(RemoteIssueLink remoteIssueLink);

    /**
     * Returns the final HTML to place between the relationship text and the delete icon.
     * Remote calls may be made to construct the final HTML.
     *
     * @param remoteIssueLink remote issue link
     * @return final HTML that goes between the relationship text and the delete icon
     */
    String getFinalHtml(RemoteIssueLink remoteIssueLink);
}
