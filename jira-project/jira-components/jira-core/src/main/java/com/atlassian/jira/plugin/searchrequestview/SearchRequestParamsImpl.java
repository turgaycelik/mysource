package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.jira.plugin.issueview.IssueViewFieldParams;
import com.atlassian.jira.web.bean.PagerFilter;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implmentation of @see com.atlassian.jira.plugin.searchrequestview.SearchRequestParams.
 */
public class SearchRequestParamsImpl implements SearchRequestParams 
{
    private Map sessionParamsMap;
    private PagerFilter pagerFilter;
    private IssueViewFieldParams issueViewFieldParams;
    private String userAgent;
    private boolean returnMax;

    public SearchRequestParamsImpl(HttpSession session)
    {
        sessionParamsMap = convertSessionToMap(session);
        pagerFilter = PagerFilter.getUnlimitedFilter();
    }

    public SearchRequestParamsImpl(HttpSession session, PagerFilter pagerFilter)
    {
        this(session, pagerFilter, null, null);
    }

    public SearchRequestParamsImpl(Map params, PagerFilter pagerFilter)
    {
        this.sessionParamsMap = new HashMap();
        if (params != null)
        {
            this.sessionParamsMap.putAll(params);
        }
        this.pagerFilter = pagerFilter;
    }

    public SearchRequestParamsImpl(HttpSession session, PagerFilter pagerFilter, IssueViewFieldParams issueViewFieldParams)
    {
        this(session, pagerFilter, null, issueViewFieldParams);
    }


    public SearchRequestParamsImpl(HttpSession session, PagerFilter pagerFilter, Map otherParams, IssueViewFieldParams issueViewFieldParams)
    {
        this(session, pagerFilter, otherParams, issueViewFieldParams, null);
    }

    public SearchRequestParamsImpl(HttpSession session, PagerFilter pagerFilter, Map otherParams, IssueViewFieldParams issueViewFieldParams, String userAgent)
    {
        this.issueViewFieldParams = issueViewFieldParams;
        this.sessionParamsMap = convertSessionToMap(session);
        if (otherParams != null && !otherParams.isEmpty())
        {
            this.sessionParamsMap.putAll(otherParams);
        }
        this.pagerFilter = pagerFilter;
        this.userAgent = userAgent;
    }

    private Map convertSessionToMap(HttpSession session)
    {
        Map map = new HashMap();

        Enumeration attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements())
        {
            String attributeName =  (String)attributeNames.nextElement();
            map.put(attributeName, session.getAttribute(attributeName));
        }
        return map;
    }

    public Map getSession()
    {
        return Collections.unmodifiableMap(sessionParamsMap);
    }

    public PagerFilter getPagerFilter()
    {
        return pagerFilter;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public IssueViewFieldParams getIssueViewFieldParams()
    {
        return issueViewFieldParams;
    }

    public void setReturnMax(boolean returnMax)
    {
        this.returnMax = returnMax;
    }

    public boolean isReturnMax()
    {
        return returnMax;
    }
}
