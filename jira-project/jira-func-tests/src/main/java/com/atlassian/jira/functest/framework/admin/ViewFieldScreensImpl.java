package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link ViewFieldScreens}.
 *
 * @since v4.2
 */
public class ViewFieldScreensImpl extends AbstractFuncTestUtil implements ViewFieldScreens
{
    private static final String CONFIGURE_LINK_PREFIX = "configure_fieldscreen_";

    private final Navigation navigation;

    private final ConfigureScreen configureScreen = new ConfigureScreenImpl();

    public ViewFieldScreensImpl(WebTester tester, JIRAEnvironmentData environmentData, Navigation navigation)
    {
        super(tester, environmentData, 2);
        this.navigation = notNull("navigation", navigation);
    }

    public ViewFieldScreens goTo()
    {
        navigation.gotoAdminSection("field_screens");
        return this;
    }

    @Override
    public ConfigureScreen configureScreen(String screenName)
    {
        tester.clickLink(CONFIGURE_LINK_PREFIX + screenName);
        return configureScreen;
    }
}
