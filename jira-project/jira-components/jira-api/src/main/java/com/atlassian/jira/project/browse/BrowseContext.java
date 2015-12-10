package com.atlassian.jira.project.browse;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.query.Query;

import java.util.Map;

/**
 * The context of the Browse Project screen.  This is passed into tabs to help them render themselves.
 * It contains the current project and user, and for specific sub-classes more criteria.
 * It is also responsible for adding criteria to {@link com.atlassian.jira.issue.search.SearchRequest} to narrow their
 * focus to the current context.
 *
 * @since v4.0
 */
@PublicApi
public interface BrowseContext
{
    /**
     * The current projecet being browsed.
     *
     * @return The current project.
     */
    Project getProject();

    /**
     * The user browsing the project.
     *
     * @return The user browsing the project.
     */
    User getUser();

    /**
     * Creates a new {@link com.atlassian.query.Query} that narrows it down to the current search context.
     * E.g. Project, Component, Version
     *
     * @return A new Query that has a more refined search based on the current context.
     */
    Query createQuery();

    /**
     * Gets the URL query string for this context.  Used to create links to navigator but still in this context.
     *
     * @return the URL query string for this context.
     */
    String getQueryString();

    /**
     * Creates a map of the context-specific parameters and their associated domain objects.
     *
     * @return a mapping from parameter name to domain object.
     */
    Map<String, Object> createParameterMap();


    /**
     * Contains a context unique that can be used to identify this context.  This is useful when storing things in the
     * session on per context basis.
     *
     * @return a unique key for this context
     */
    String getContextKey();

}
