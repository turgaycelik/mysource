package com.atlassian.jira.service.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.JiraKeyUtils;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.transform;

@PublicApi
public class ServiceUtils
{
    private static final String INVALID_ISSUEKEY_CHARS = "\n\r\t \"''`~,.:;<>()[]{}!@#$%^&*+=|\\/?";
    private static final Logger log = Logger.getLogger(ServiceUtils.class);

    private ServiceUtils()
    {
        //cannot create instance of class
    }


    /**
     * Given an actual key - return the issue that matches that key, or null if no issues match that key.
     *
     * @deprecated use {@link com.atlassian.jira.issue.IssueManager#getIssueObject(String)} instead
     */
    @Nullable
    @Deprecated
    public static GenericValue getIssue(final String key)
    {
        final Issue issueObject = getIssueObjectInternal(key);
        if (issueObject == null)
        {
            return null;
        }
        return issueObject.getGenericValue();
    }

    /**
     * Given an actual key - return the issue that matches that key, or null if no issues match that key.
     *
     * @deprecated Use {@link com.atlassian.jira.issue.IssueManager#getIssueObject(String)} instead. Since v6.1.
     */
    @Nullable
    @Deprecated
    public static Issue getIssueObject(final String key)
    {
        return getIssueObjectInternal(key);
    }

    /**
     * Loops through the string and returns the issue that is found within, or null if there is no issue matching.
     * <p/>
     * It finds any string matching XXX-### and then looks it up to see if it is a valid issue.  It will return the
     * first valid issue that exists.
     *
     * @param searchString the string to search through for issues
     * @return the issue that has been found, or null of one is not found.
     * @deprecated use {@link #findIssueObjectInString(String)} instead
     */
    @Deprecated
    @Nullable
    public static GenericValue findIssueInString(final String searchString)
    {
        final Issue issue = findIssueObjectInString(searchString);
        if (issue == null)
        {
            return null;
        }
        return issue.getGenericValue();
    }


    /**
     * Loops through the string and returns the issue that is found within, or null if there is no issue matching.
     * <p/>
     * It finds any string matching XXX-### and then looks it up to see if it is a valid issue.  It will return the
     * first valid issue that exists.
     *
     * @param searchString the string to search through for issues
     * @return the issue that has been found, or null of one is not found.
     */
    @Nullable
    public static Issue findIssueObjectInString(final String searchString)
    {
        final StringTokenizer tokenizer = new StringTokenizer(TextUtils.noNull(searchString).toUpperCase(), INVALID_ISSUEKEY_CHARS, false);
        String token;
        while (tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();

            if (JiraKeyUtils.validIssueKey(token))
            {
                final Issue issue = getIssueObjectInternal(token);
                if (issue != null)
                {
                    return issue;
                }
            }
        }
        return null;
    }

    /**
     *
     * @deprecated use {@link #findIssueObjectsInString(String)} instead
     */
    @Deprecated
    public static GenericValue[] findIssuesInString(final String searchString)
    {
        if (searchString == null)
        {
            return null;
        }

        final List<GenericValue> al = new ArrayList<GenericValue>();
        final StringTokenizer tokenizer = new StringTokenizer(searchString, INVALID_ISSUEKEY_CHARS, false);
        String token;
        while (tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();

            if (JiraKeyUtils.validIssueKey(token))
            {
                final GenericValue issue = ServiceUtils.getIssue(token);
                if (issue != null)
                {
                    al.add(issue);
                }
            }
        }
        return al.toArray(new GenericValue[al.size()]);
    }

    public static Iterable<Issue> findIssueObjectsInString(final String searchString)
    {
        if (searchString == null)
        {
            return Collections.emptyList();
        }

        final List<Issue> al = new ArrayList<Issue>();
        final StringTokenizer tokenizer = new StringTokenizer(searchString, INVALID_ISSUEKEY_CHARS, false);
        String token;
        while (tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();

            if (JiraKeyUtils.validIssueKey(token))
            {
                final Issue issue = ServiceUtils.getIssueObjectInternal(token);
                if (issue != null)
                {
                    al.add(issue);
                }
            }
        }
        return al;
    }

    /**
     * This method creates a map of parameters from a string.
     * <p/>
     * The format of the string is key=value, key=value, key=value.
     * <p/>
     * At the moment this is really only used for Handler parameters, but that whole area needs to be rewritten for JIRA
     * 2.0.
     */
    public static Map<String, String> getParameterMap(final String parameterString)
    {
        final Map<String, String> params = new HashMap<String, String>();

        if (parameterString != null)
        {
            final StringTokenizer st = new StringTokenizer(parameterString, ",");

            while (st.hasMoreTokens())
            {
                final String token = st.nextToken().trim();
                final int equalIdx = token.indexOf('=');

                if (equalIdx >= 0)
                {
                    final String paramName = token.substring(0, equalIdx);
                    String paramValue = null;

                    if (equalIdx + 1 < token.length())
                    {
                        paramValue = token.substring(equalIdx + 1);
                    }
                    params.put(paramName, paramValue);
                }
            }
        }
        return params;
    }

    public static String toParameterString(Map<String, String> params)
    {
        return Joiner.on(",").join(transform(params.entrySet(), new Function<Map.Entry<String, String>, String>()
        {
            @Override
            public String apply(Map.Entry<String, String> from)
            {
                return from.getKey() + "=" + from.getValue();
            }
        }));
    }

    private static MutableIssue getIssueObjectInternal(final String key)
    {
        return ComponentAccessor.getIssueManager().getIssueObject(key);
    }
}
