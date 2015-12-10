package com.atlassian.sal.jira.search;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextFactory;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.search.SearchMatch;
import com.atlassian.sal.api.search.SearchResults;
import com.atlassian.sal.api.search.parameter.SearchParameter;
import com.atlassian.sal.api.search.query.SearchQuery;
import com.atlassian.sal.api.search.query.SearchQueryParser;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.core.message.DefaultMessage;
import com.atlassian.sal.core.search.BasicResourceType;
import com.atlassian.sal.core.search.BasicSearchMatch;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Implementation of the JIRA search provider... now with JQL!
 */
public class JiraSearchProvider implements com.atlassian.sal.api.search.SearchProvider
{
    private static final Logger log = Logger.getLogger(JiraSearchProvider.class);
    private final SearchProvider searchProvider;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchQueryParser searchQueryParser;
    private final ApplicationProperties applicationProperties;
    private final SearchContextFactory searchContextFactory;
    private final UserManager userManager;

    public JiraSearchProvider(SearchProvider searchProvider, IssueManager issueManager,
            JiraAuthenticationContext authenticationContext, SearchQueryParser searchQueryParser,
            ApplicationProperties applicationProperties, SearchContextFactory searchContextFactory, final UserManager userManager)
    {
        this.searchProvider = searchProvider;
        this.issueManager = issueManager;
        this.authenticationContext = authenticationContext;
        this.searchQueryParser = searchQueryParser;
        this.applicationProperties = applicationProperties;
        this.searchContextFactory = searchContextFactory;
        this.userManager = userManager;
    }

    public SearchResults search(String username, String searchString)
    {
        return internalSearch(getUser(username), username, searchString);
    }

    @Override
    public SearchResults search(final UserKey userKey, final String searchString)
    {
        return internalSearch(getUser(userKey), (userKey == null)?null:userKey.getStringValue(), searchString);
    }

    private SearchResults internalSearch(final ApplicationUser remoteUser, final String remoteUsername, final String searchString)
    {
        final SearchQuery searchQuery = searchQueryParser.parse(searchString);
        final int maxHits = searchQuery.getParameter(SearchParameter.MAXHITS, Integer.MAX_VALUE);

        final ApplicationUser oldAuthenticationContextUser = getAuthenticationContextUser();
        try
        {
            setAuthenticationContextUser(remoteUser);
            if (remoteUser == null)
            {
                log.info("User '" + remoteUsername + "' not found. Running anonymous search...");
            }
            // See if the search String contains a JIRA issue
            final Collection<Issue> issues = getIssuesFromQuery(searchQuery.getSearchString());
            final Query query = createQuery(searchQuery);
            return performSearch(maxHits, query, issues, remoteUser);
        }
        finally
        {
            // restore original user (who is hopefully null)
            setAuthenticationContextUser(oldAuthenticationContextUser);
        }
    }

    private SearchResults performSearch(int maxHits, Query query, Collection<Issue> issues, ApplicationUser remoteUser)
    {
        try
        {
            final long startTime = System.currentTimeMillis();
            final PagerFilter pagerFilter = new PagerFilter();

            pagerFilter.setMax(maxHits);
            final com.atlassian.jira.issue.search.SearchResults searchResults =
                searchProvider.search(query, remoteUser, pagerFilter);
            issues.addAll(searchResults.getIssues());
            final int numResults = searchResults.getTotal() - searchResults.getIssues().size() + issues.size();

            List<Issue> trimmedResults = new ArrayList<Issue>(issues);
            if (trimmedResults.size() > maxHits)
            {
                trimmedResults = trimmedResults.subList(0, maxHits);
            }

            return new SearchResults(transformResults(trimmedResults), numResults,
                System.currentTimeMillis() - startTime);
        }
        catch (final SearchException e)
        {
            log.error("Error executing search", e);
            final ArrayList<Message> errors = new ArrayList<Message>();
            errors.add(new DefaultMessage(e.getMessage()));
            return new SearchResults(errors);
        }
        finally
        {
            DefaultIndexManager.flushThreadLocalSearchers();
        }
    }

    private List<SearchMatch> transformResults(final Collection<Issue> issues)
    {
        final List<SearchMatch> matches = new ArrayList<SearchMatch>();
        for (Issue issue : issues)
        {
            matches.add(new BasicSearchMatch(applicationProperties.getBaseUrl(UrlMode.CANONICAL) + "/browse/" + issue.getKey(),
                "[" + issue.getKey() + "] " + issue.getSummary(), issue.getDescription(),
                new BasicResourceType(applicationProperties, issue.getIssueTypeObject().getId())));
        }
        return matches;
    }

    private Query createQuery(SearchQuery query)
    {
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        JqlClauseBuilder clause = builder.where();

        // Project first
        final String projectKey = query.getParameter(SearchParameter.PROJECT);
        if (projectKey != null)
        {
            clause.project(projectKey).and();
        }

        // Split... I don't know if we should do anything like honour quotes
        String[] rawTerms = query.getSearchString().trim().split("\\s");
        List<String> terms = new ArrayList<String>();
        // Make sure there's no blanks
        for (String term : rawTerms)
        {
            if (term.length() > 0)
            {
                terms.add(term);
            }
        }

        // Add all terms, anded together
        for (int i = 0; i < terms.size(); i++)
        {
            if (i > 0)
            {
                clause.and();
            }
            // Search if the summary or the description contains the word
            final String term = terms.get(i);
            clause.sub().summary(term).or().description(term).or().comment(term).endsub();
        }

        return builder.buildQuery();
    }

    Collection<String> getIssueKeysFromQuery(String query)
    {
        return JiraKeyUtils.getIssueKeysFromString(query);
    }

    private Collection<Issue> getIssuesFromQuery(String query)
    {
        final Collection<String> issueKeys = getIssueKeysFromQuery(query);
        // Need to ensure issue order is maintained, while also ensuring uniqueness, hence LinkedHashSet
        final Collection<Issue> issues = new LinkedHashSet<Issue>();
        for (final String issueKey : issueKeys)
        {
            final Issue issue = getIssueByKey(issueKey);
            if (issue != null)
            {
                issues.add(issue);
            }
        }
        return issues;
    }

    private Issue getIssueByKey(String issueKey)
    {
        try
        {
            return issueManager.getIssueObject(issueKey);
        }
        catch (final DataAccessException dae)
        {
            // Not found
            return null;
        }
    }

    SearchContext getSearchContext()
    {
        return searchContextFactory.create();
    }

    private ApplicationUser getUser(final String username)
    {
        return userManager.getUserByName(username);
    }

    private ApplicationUser getUser(final UserKey userKey)
    {
        if (userKey != null)
        {
            return userManager.getUserByKey(userKey.getStringValue());
        }
        return null;
    }

    private void setAuthenticationContextUser(final ApplicationUser remoteUser)
    {
        authenticationContext.setLoggedInUser(remoteUser);
    }

    private ApplicationUser getAuthenticationContextUser()
    {
        return authenticationContext.getUser();
    }

}
