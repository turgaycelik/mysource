package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebForm;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the "Text Search" condition in Navigator.
 *
 * @since v3.13
 */
public class QuerySearchCondition implements NavigatorCondition
{
    private static final String QUERY_ELEMENT = "summary";
    
    private String queryString = "";

    public QuerySearchCondition()
    {
        this("");
    }

    public QuerySearchCondition(String queryString)
    {
        setQueryString(queryString);
    }

    public QuerySearchCondition(QuerySearchCondition condition)
    {
        this(condition.queryString);
    }

    public void setQueryString(String queryString)
    {
        this.queryString = StringUtils.isBlank(queryString) ? "" : queryString;
    }

    public void parseCondition(WebTester tester)
    {
        WebForm form = tester.getDialog().getForm();

        setQueryString(form.getParameterValue(QUERY_ELEMENT));

    }

    public void assertSettings(Document document)
    {
        Assert.assertEquals("Value not set correctly for element: " + QUERY_ELEMENT, queryString, document.getElementsByAttributeValue("name", QUERY_ELEMENT).get(0).val());
    }

    public NavigatorCondition copyCondition()
    {
        return new QuerySearchCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new QuerySearchCondition();
    }

    public String toString()
    {
        return "Query Search: '" + queryString;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        QuerySearchCondition that = (QuerySearchCondition) o;

        if (queryString != null ? !queryString.equals(that.queryString) : that.queryString != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (queryString != null ? queryString.hashCode() : 0);
        return result;
    }

    private boolean parseBoolean(final String bool)
    {
        return !StringUtils.isBlank(bool) && Boolean.valueOf(bool);
    }
}
