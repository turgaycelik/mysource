package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Element;

/**
 * @since v4.0
 */
public class ResolutionsImpl extends AbstractFuncTestUtil implements Resolutions
{
    public ResolutionsImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    public String addResolution(final String name)
    {
        getFuncTestHelperFactory().getNavigation().gotoAdminSection("resolutions");
        tester.setWorkingForm("jiraform");
        tester.setFormElement("name", name);
        tester.submit("Add");

        // get the id of the newly added resolution
        XPathLocator locator = new XPathLocator(tester, "//tr/td/b[contains(.,'" + name + "')]");
        if (locator.getNodes().length != 1)
        {
            Assert.fail("Could not find the newly created resolution '" + name + "'");
        }

        final Element rowParent = DomKit.getFirstParentByTag((Element) locator.getNodes()[0], "tr");
        locator = new XPathLocator(rowParent, ".//a[contains(.,'Edit')]/@href");
        if (locator.getNodes().length != 1)
        {
            Assert.fail("Could not find edit link for the newly created resolution '" + name + "'");
        }

        // href should look something like this -- EditResolution!default.jspa?id=6
        final String href = locator.getNodes()[0].getNodeValue();
        return href.substring(href.indexOf("=") + 1);
    }
}
