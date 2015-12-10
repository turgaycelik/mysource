package com.atlassian.jira.web.tags;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * This overrides the basic URLTag in webwork to provide JIRA specific behaviour
 *
 * @since v4.1
 */
public class URLTag extends webwork.view.taglib.URLTag
{

    private boolean atltoken = true;

    @Override
    public int doStartTag() throws JspException
    {
        int rc = super.doStartTag();
        if (atltoken)
        {
            // make the first parameter an atl_token
            addXsrfToken();
        }
        return rc;
    }

    /**
     * All URLs generated in JIRA now get a atl_token parameter to protect against XSRF
     */
    private void addXsrfToken()
    {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) pageContext.getRequest();
        addParameter("atl_token", getXsrfToken(httpServletRequest));
    }

    String getXsrfToken(final HttpServletRequest request)
    {
        return getXsrfTokenGenerator().generateToken(request);
    }

    XsrfTokenGenerator getXsrfTokenGenerator()
    {
        return ComponentAccessor.getComponentOfType(XsrfTokenGenerator.class);
    }

    /**
     * Defaults to true.  You must make and exception to not have the xsrf token
     * 
     * @return true if the XSRF token will be added to URL parameters
     */
    public boolean isAtltoken()
    {
        return atltoken;
    }

    /**
     * This controls whether the XSRF token will be added to the list of URL parameters
     * 
     * @param atltoken true if it should be added
     */
    public void setAtltoken(final boolean atltoken)
    {
        this.atltoken = atltoken;
    }
}
