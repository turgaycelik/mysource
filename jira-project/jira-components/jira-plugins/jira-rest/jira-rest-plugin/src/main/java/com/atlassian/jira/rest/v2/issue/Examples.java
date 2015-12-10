package com.atlassian.jira.rest.v2.issue;

import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;

/**
 * This class holds constants that are
 *
 * @since v4.2
 */
public class Examples
{
    /**
     * The base URL for the example JIRA instance.
     */
    public static final String JIRA_BASE_URL = "http://www.example.com/jira";

    /**
     * The base URL for the REST API.
     */
    public static final String REST_BASE_URL = JIRA_BASE_URL + "/rest/api/2";

    /**
     * Creates a new URI consisting of the {@link #JIRA_BASE_URL} followed by the passed-in path segments.
     *
     * @param pathSegments the path segments to append
     * @return a URI
     * @throws IllegalArgumentException if the passed-in paths are not valid
     */
    public static URI jiraURI(String... pathSegments)
    {
        return createURI(JIRA_BASE_URL, pathSegments);
    }

    /**
     * Creates a new URI consisting of the {@link #REST_BASE_URL} followed by the passed-in path segments.
     *
     * @param pathSegments the path segments to append
     * @return a URI
     * @throws IllegalArgumentException if the passed-in paths are not valid
     */
    public static URI restURI(String... pathSegments)
    {
        String base = REST_BASE_URL;
        return createURI(base, pathSegments);
    }

    private static URI createURI(String base, String[] pathSegments)
    {
        String path = format("%s/%s", base, (pathSegments != null) ? StringUtils.join(pathSegments, '/') : "");
        try
        {
            return new URI(path);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Error parsing URI: " + path, e);
        }
    }

    /**
     * Prevents instantiation.
     */
    private Examples()
    {
        // empty
    }
}
