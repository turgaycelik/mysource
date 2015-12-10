package com.atlassian.jira.web.util;

import java.util.EnumSet;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.sal.api.page.PageCapabilities;
import com.atlassian.sal.api.page.PageCapability;

import com.google.common.base.Enums;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class PageCapabilitiesImpl extends PageCapabilities
{
    public final static String DEFAULT_PARAM_NAME = "page_caps";
    public final static String DEFAULT_CLS_PREFIX = "CAPS_";
    public final static String DEFAULT_ATTR_NAME = "pageCaps";

    private final EnumSet<PageCapability> pageCaps;

    public static PageCapabilitiesImpl fromRequest(HttpServletRequest request)
    {
        return fromRequest(request, DEFAULT_PARAM_NAME);
    }

    public static PageCapabilitiesImpl fromRequest(HttpServletRequest request, String paramName)
    {
        return new PageCapabilitiesImpl(PageCapabilities.valueOf(request.getParameter(paramName)));
    }

    public PageCapabilitiesImpl(final EnumSet<PageCapability> pageCaps)
    {
        this.pageCaps = pageCaps;
    }

    public String getBodyCssClass()
    {
        return getBodyCssClass(DEFAULT_CLS_PREFIX);
    }

    public String getBodyCssClass(final String clsPrefix)
    {
        return Joiner.on(" ").join(Iterables.transform(pageCaps, new Function<PageCapability, String>()
        {
            @Override
            public String apply(final PageCapability pageCapability)
            {
                return clsPrefix + pageCapability.toString();
            }
        }));
    }

    public PageCapabilitiesImpl setRequestAttribute(HttpServletRequest request)
    {
        return setRequestAttribute(request, DEFAULT_ATTR_NAME);
    }

    public PageCapabilitiesImpl setRequestAttribute(HttpServletRequest request, String attributeName)
    {
        request.setAttribute(attributeName, PageCapabilities.toString(pageCaps));
        return this;
    }

    public EnumSet<PageCapability> getPageCaps()
    {
        return pageCaps;
    }
}
