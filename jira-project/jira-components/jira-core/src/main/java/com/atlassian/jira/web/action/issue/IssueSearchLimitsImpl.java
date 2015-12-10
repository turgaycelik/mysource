package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.config.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.config.properties.APKeys.JIRA_SEARCH_VIEWS_DEFAULT_MAX;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Utility methods that allow you to get the limits that are imposed by JIRA configuration properties.
 *
 * @since v4.3
 */
public class IssueSearchLimitsImpl implements IssueSearchLimits
{
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IssueSearchLimitsImpl.class);

    /**
     * The JIRA application properties.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * Creates a new SearchLimits instance.
     *
     * @param applicationProperties an ApplicationProperties
     */
    public IssueSearchLimitsImpl(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Returns the maximum number of search results that this JIRA instance is configured to allow, by reading it from
     * the <pre>jira-application.properties</pre> file. If there is a problem reading the configured value, this method
     * returns {@value #DEFAULT_MAX_RESULTS}.
     *
     * @return an int containing the maximum number of search results to return
     */
    @Override
    public int getMaxResults()
    {
        try
        {
            final String defaultMax = applicationProperties.getDefaultBackedString(JIRA_SEARCH_VIEWS_DEFAULT_MAX);
            if (isBlank(defaultMax))
            {
                return DEFAULT_MAX_RESULTS;
            }

            return Integer.valueOf(defaultMax);
        }
        catch (NumberFormatException e)
        {
            LOGGER.warn("Cannot get issue navigator restriction clause for: {} key={}", applicationProperties.getDefaultBackedString(JIRA_SEARCH_VIEWS_DEFAULT_MAX), JIRA_SEARCH_VIEWS_DEFAULT_MAX);
            return DEFAULT_MAX_RESULTS;
        }
    }
}
