package com.atlassian.jira.functest.matcher;


import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Set;
import java.util.StringTokenizer;

import com.atlassian.jira.util.UriQueryParser;
import com.google.common.collect.Sets;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests whether the URL exactly matches. This is not a logical matcher.
 * This matcher is used to match generated URI's.
 * <p>
 * For example: (query parameters delimeter
 * <blockquote><pre>
 *     http://www.test.com/home/service?action=test&name=Jo&surname=Adam
 * </pre></blockquote><p>
 * is NOT the same as:
 * <blockquote><pre>
 *     http://www.test.com/home/service?action=test;name=Jo;surname=Adam
 * </pre></blockquote><p>
 * For example:
 * <blockquote><pre>
 *     http://www.test.com/home/service?action=test&name=Jo&surname=Adam;
 * </pre></blockquote><p>
 * is NOT the same as:
 * <blockquote><pre>
 *     http://www.test.com/home/service?action=test&name=Jo&surname=Adam;
 * </pre></blockquote><p> *
 * The ONLY exception: (Order of query parameters)
 * <blockquote><pre>
 *     http://www.test.com/home/service?action=test&name=Jo&surname=Adam
 * </pre></blockquote><p>
 * IS the same as:
 * <blockquote><pre>
 *     http://www.test.com/home/service?name=Jo&surname=Adam&action=test
 * </pre></blockquote><p>
 * @since 6.3
 */
public class URIMatcher extends TypeSafeMatcher<URI>
{
    final private URI expected;

    URIMatcher(URI expected)
    {
        this.expected = expected;
    }


    public static URIMatcher isSameURI(URI uri)
    {
        return new URIMatcher(uri);
    }

    @Override
    protected boolean matchesSafely(final URI receivedUriToMatch)
    {
        try
        {
            //If URL's are not exactly the same (params could be in different order)
            if(!expected.equals(receivedUriToMatch))
            {

                if (new URI(receivedUriToMatch.getScheme(),
                        receivedUriToMatch.getUserInfo(),
                        receivedUriToMatch.getHost(),
                        receivedUriToMatch.getPort(),
                        receivedUriToMatch.getPath(), null,
                        receivedUriToMatch.getFragment())
                        .equals(new URI(expected.getScheme(),
                                expected.getUserInfo(), expected.getHost(), expected.getPort(),
                                expected.getPath(), null,
                                expected.getFragment())))
                {
                    if (expected.getQuery() == null && receivedUriToMatch.getQuery() == null)
                    {
                        return true;
                    }
                    else if (expected.getQuery() != null && receivedUriToMatch.getQuery() != null)
                    {
                        if (receivedUriToMatch.getQuery().length() == expected.getQuery().length())
                        {
                            String uriToMatchQuery = receivedUriToMatch.getQuery();
                            String expectedQuery = expected.getQuery();
                            Set<String> uriToMatchParams = Sets.newHashSet(uriToMatchQuery.split("&"));
                            Set<String> expectedParams = Sets.newHashSet(expectedQuery.split("&"));
                            if(expectedParams.containsAll(uriToMatchParams))
                            {
                                return true;
                            }
                            else
                            {
                                //Could have used ; as parameter delimiter
                                uriToMatchParams = Sets.newHashSet(uriToMatchQuery.split(";"));
                                expectedParams = Sets.newHashSet(expectedQuery.split(";"));
                                return expectedParams.containsAll(uriToMatchParams);
                            }
                        }
                    }
                }
            }
            else
            {
                return true;
            }
        }
        catch (URISyntaxException ignoreException)
        {
            //fall through, when URISyntaxException occurs match should fail ( return false )
        }
        return false;
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("URI similar to (same base URI and same params + values ").appendText(expected.toString());
    }

}
