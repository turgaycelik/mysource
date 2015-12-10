package com.atlassian.jira.web.component.webfragment;

import com.atlassian.core.util.WebRequestUtils;
import com.atlassian.jira.util.BrowserUtils;
import com.opensymphony.util.TextUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * This file provides specific context information for the system-navigation-bar.vm template
 */
public class SystemNavContextLayoutBean implements ContextLayoutBean
{
    private final HttpServletRequest request;
    private final String userAgent;


    public SystemNavContextLayoutBean(HttpServletRequest request)
    {
        this.request = request;
        this.userAgent = TextUtils.noNull(request.getHeader("USER-AGENT"));

    }

    public boolean isCaptureJavascript()
    {
        return WebRequestUtils.isGoodBrowser(request) && TextUtils.noNull(request.getHeader("USER-AGENT")).indexOf("Opera") == -1;
    }

    /**
     * Returns TRUE if the browser requires a Filter style opacity statement for PNGs
     *
     * <ul>
     *      <li> IE 5 upwards on Windows</li>
     * </ul>
     * @return
     */
    public boolean isFilterBasedPngOpacity()
    {
        return BrowserUtils.isFilterBasedPngOpacity(userAgent);
    }

    /**
     * Returns the browsers modifier key
     *
     * @return the browsers modifier key
     */
    public String getModifierKey()
    {
        return BrowserUtils.getModifierKey();
    }

    public String getSelectedSection()
    {
        return (String) request.getAttribute("jira.selected.section");
    }
}
