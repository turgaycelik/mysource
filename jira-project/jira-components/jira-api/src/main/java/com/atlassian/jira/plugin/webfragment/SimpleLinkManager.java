package com.atlassian.jira.plugin.webfragment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import javax.annotation.Nonnull;

import java.util.List;

/**
 * A manager that looks after generating lists of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} and
 * {@link com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection}.
 *
 * @since v4.0
 * @deprecated since v6.3 - use {@link com.atlassian.plugin.web.api.DynamicWebInterfaceManager} directly if possible.
 */
public interface SimpleLinkManager
{
    /**
     * This determines whether a location should be loaded lazily if possible.
     *
     * @param location   The location to check for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return true if the loaction should be loaded lazily if possible, false otherwise
     */
    boolean shouldLocationBeLazy(String location, User remoteUser, JiraHelper jiraHelper);

    /**
     * This determines whether an individual section should be loaded lazily if possible.
     * DO NOT USE: This method only checks simple link factories and not the web-section. Nothing uses it now
     * and it should not be used in future!
     *
     * @param section The section to check for
     * @return true if the section should be loaded lazily if possible, false otherwise
     */
    boolean shouldSectionBeLazy(String section);

    /**
     * Gets a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} for the given section.
     *
     * @param section    The section to generate the list for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return The list of links for the given section
     */
    @Nonnull
    List<SimpleLink> getLinksForSection(@Nonnull String section, User remoteUser, @Nonnull JiraHelper jiraHelper);

    /**
     * Gets a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} for the given section.
     * Additionally it adds caching prefix to icon url, if addIconCachinPrefix is true
     *
     * @param section    The section to generate the list for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @param addIconCachingPrefix Diecides if relative icon URL will be automatically prefixed
     * @return The list of links for the given section
     */
    @Nonnull
    List<SimpleLink> getLinksForSection(@Nonnull String section, User remoteUser, @Nonnull JiraHelper jiraHelper, boolean addIconCachingPrefix);


    /**
     * Gets a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} for the given section, without
     * filtering links using the conditions specified for the links.  This will effectively return a list of links
     * without running any security checks.
     *
     * @param section    The section to generate the list for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return The list of links for the given section
     */
    @Nonnull
    List<SimpleLink> getLinksForSectionIgnoreConditions(@Nonnull String section, User remoteUser, @Nonnull JiraHelper jiraHelper);

    /**
     * Gets a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection} for the given location.
     *
     * @param location   The location to generate the list for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return The list of sections for the give location
     */
    @Nonnull
    List<SimpleLinkSection> getSectionsForLocation(@Nonnull String location, User remoteUser, @Nonnull JiraHelper jiraHelper);

    /**
     * Recursively search through our sections within sections within sections within ...
     * To find a section that contains a link that matches this URL
     *
     * @param URL           The URL for the action e.g. https://jdog.atlassian.com/secure/project/ViewProjects.jspa. We check if this URL contains a web-item's path which is usually something shorter like /secure/project/ViewProjects.jspa
     * @param topLevelSection The top level section from where to start searching!
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return              Returns null if no section found
     */
    SimpleLinkSection getSectionForURL (@Nonnull String topLevelSection, @Nonnull String URL, User remoteUser, JiraHelper jiraHelper);

    /**
     * Gets a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection} for the given location, that have either web-items or
     * other web-sections within them.
     *
     * @param location   The location to generate the list for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return The list of sections for the give location
     */
    @Nonnull
    List<SimpleLinkSection> getNotEmptySectionsForLocation(@Nonnull String location, User remoteUser, @Nonnull JiraHelper jiraHelper);
}
