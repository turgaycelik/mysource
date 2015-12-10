package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class represents the collection of links in to and out of a particular issue.
 *
 * Sort order is determined by the application property "jira.view.issue.links.sort.order"
 */
@PublicApi
public interface LinkCollection
{
    /**
     * Returns a set of link types, {@link IssueLinkType} objects.
     *
     * @return a set of {@link IssueLinkType} objects
     */
    public Set<IssueLinkType> getLinkTypes();

    /**
     * Looks up and returns a "sort.order" sorted list of all outward linked issues by given link name. May return
     * null.
     *
     * @param linkName link name to lookup issues by
     * @return a sorted list of browsable outward linked issues or null (sorry)
     */
    @Nullable public List<Issue> getOutwardIssues(String linkName);

    /**
     * Looks up and returns a "sort.order" sorted list of all inward linked issues by given link name. May return
     * null.
     *
     * @param linkName link name to lookup issues by
     * @return a sorted list of browsable inward linked issues
     */
    @Nullable public List<Issue> getInwardIssues(String linkName);

    /**
     * Returns a collection of issues that contains both inward and outward linking issues. The returned collection is
     * sorted by "sort.order" and does not contain duplicates.
     *
     * @return a collection of all linked issues
     */
    public Collection<Issue> getAllIssues();

    /**
     * Determine whether there are links visible to this user - do not display link panel if not
     *
     * @return true if there are any outward or inward links, false otherwise
     *
     * @deprecated No longer relevant because of introduction of Remote Issue Links. Since v5.0.
     */
    public boolean isDisplayLinkPanel();
}
