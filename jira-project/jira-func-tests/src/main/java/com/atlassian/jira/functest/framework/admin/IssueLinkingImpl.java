package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import net.sourceforge.jwebunit.WebTester;

/**
 * @since v4.0
 */
public class IssueLinkingImpl implements IssueLinking
{
    private final WebTester tester;

    private final Navigation navigation;

    private final FuncTestLogger logger;

    public IssueLinkingImpl(final WebTester tester, final Navigation navigation, final FuncTestLogger logger)
    {
        this.tester = tester;
        this.navigation = navigation;
        this.logger = logger;
    }

    @Override
    public void enable()
    {
        navigation.gotoAdminSection("linking");
        final XPathLocator locator = new XPathLocator(tester, "//input[@name='Activate']");
        if (locator.getNodes().length > 0)
        {
            logger.log("Activating issue linking");
            tester.submit("Activate");
        }
        else
        {
            logger.log("Issue linking already activated");
        }
    }

    @Override
    public void disable()
    {
        navigation.gotoAdminSection("linking");
        final XPathLocator locator = new XPathLocator(tester, "//input[@name='Deactivate']");
        if (locator.getNodes().length > 0)
        {
            logger.log("Deactivating issue linking");
            tester.submit("Deactivate");
        }
        else
        {
            logger.log("Issue linking already deactivated");
        }
    }

    @Override
    public void addIssueLink(final String name, final String outward, final String inward)
    {
        navigation.gotoAdminSection("linking");
        tester.setFormElement("name", name);
        tester.setFormElement("outward", outward);
        tester.setFormElement("inward", inward);
        tester.submit("Add");
    }

    @Override
    public void delete(final String name)
    {
        navigation.gotoAdminSection("linking");
        tester.clickLink("del_" + name);
        tester.submit("Delete");
    }

    @Override
    public boolean exists(final String name)
    {
        navigation.gotoAdminSection("linking");
        return tester.getDialog().isLinkPresent("del_" + name);
    }
}
