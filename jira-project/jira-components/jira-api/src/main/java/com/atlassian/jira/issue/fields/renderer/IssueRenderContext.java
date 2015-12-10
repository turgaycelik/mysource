package com.atlassian.jira.issue.fields.renderer;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a context object used with the renderers.
 */
@PublicApi
public class IssueRenderContext
{
    /**
     * When {@link #getParam(Object) getParam(INLINE_PARAM)}==true then the renderer that is passed such context will
     * try to render the text as inline
     *
     * @since v5.2
     */

    public static final String INLINE_PARAM = "atlassian-renderer-inline-param";
    private Issue issue;
    private Map params;

    public IssueRenderContext(Issue issue)
    {
        this.issue = issue;
        params = new HashMap();
    }

    public Issue getIssue()
    {
        return issue;
    }

    public void setIssue(Issue issue)
    {
        this.issue = issue;
    }

    public Map getParams()
    {
        return params;
    }

    public void addParam(Object key, Object value)
    {
        params.put(key, value);
    }

    public Object getParam(Object key)
    {
        return params.get(key);
    }
}
