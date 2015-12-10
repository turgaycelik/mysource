package com.atlassian.jira.functest.framework.dashboard;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * A test object to help assert that a page has the correct portlet config in it
 *
 * @since v3.13
 */
public class DashboardPagePortletInfo
{
    private List<String> leftPortlets = new ArrayList<String>();
    private List<String> rightPortlets = new ArrayList<String>();

    public DashboardPagePortletInfo(final List<String> leftPortlets, final List<String> rightPortlets)
    {
        this.leftPortlets = leftPortlets;
        this.rightPortlets = rightPortlets;
    }

    public DashboardPagePortletInfo()
    {
    }

    public DashboardPagePortletInfo addToLeft(String portletName)
    {
        leftPortlets.add(portletName);
        return this;
    }

    public DashboardPagePortletInfo addToRight(String portletName)
    {
        rightPortlets.add(portletName);
        return this;
    }

    public List getLeftPortlets()
    {
        return leftPortlets;
    }

    public void setLeftPortlets(final List<String> leftPortlets)
    {
        this.leftPortlets = leftPortlets;
    }

    public List getRightPortlets()
    {
        return rightPortlets;
    }

    public void setRightPortlets(final List<String> rightPortlets)
    {
        this.rightPortlets = rightPortlets;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(ToStringStyle.MULTI_LINE_STYLE);
    }
}
