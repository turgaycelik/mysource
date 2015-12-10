package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.navigation.FilterNavigation;
import com.atlassian.jira.functest.framework.navigation.IssueNavigation;
import com.atlassian.jira.functest.framework.navigation.IssueNavigationImpl;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigationImpl;
import com.atlassian.jira.functest.framework.navigation.ManageFiltersNavigation;
import com.atlassian.jira.functest.framework.page.WebTestPage;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.util.url.URLUtil;
import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/**
 * Implementation of {@link Navigation}
 *
 * @since v3.13
 */
public class NavigationImpl extends AbstractFuncTestUtil implements Navigation, FuncTestLogger
{
    public static final String BUTTON_NEXT = "Next";
    public static final String PROJECT_PLUGIN_PREFIX = "com.atlassian.jira.jira-projects-plugin:";

    private static final String PAGE_CUSTOM_FIELDS = "/secure/admin/ViewCustomFields.jspa";
    private static final String PAGE_LIST_WORKFLOWS = "/secure/admin/workflows/ListWorkflows.jspa";
    private static final String ADMIN_LINK_CLASSIC_HEADER = "admin_link";

    private final FilterNavigation manageFiltersNavigation;
    private final IssueNavigation issue;
    private final IssueNavigatorNavigation issueNavigator;
    private final Dashboard dashboard;
    private final FilterNavigation filterPickerPopup;
    private final UserProfile userProfile;
    private final HtmlPage page;
    private final Backdoor backdoor;
    private final Workflows workflows;

    private String lastPasswordUsed;

    /**
     * Note: if you need to construct this for an old-style {@link com.atlassian.jira.webtests.JIRAWebTest}, you may
     * want to consider using {@link com.atlassian.jira.functest.framework.FuncTestHelperFactory} instead.
     *
     * @param tester the tester
     * @param environmentData the environment data
     */
    public NavigationImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
        manageFiltersNavigation = new ManageFiltersNavigation(tester, environmentData);
        filterPickerPopup = new FilterPickerPopupNavigation(tester);
        issue = new IssueNavigationImpl(tester, environmentData);
        issueNavigator = new IssueNavigatorNavigationImpl(tester, environmentData);
        dashboard = new DashboardImpl(tester, environmentData, this);
        userProfile = new UserProfileImpl(tester, environmentData, this);
        page = new HtmlPage(tester);
        workflows = new WorkflowsImpl(tester, environmentData, this);
        backdoor =  new Backdoor(environmentData);
    }

    public void login(final String userName)
    {
        login(userName, userName);
    }

    public void login(String userName, String userPassword)
    {
        login(userName, userPassword, false);
    }

    public void loginAttempt(String userName, String userPassword)
    {
        loginUsingForm(userName, userPassword, false, false);
    }

    public void login(final String userName, final String userPassword, final boolean useCookie)
    {
        doLogin(userName, userPassword, useCookie, true);
    }

    public void doLogin(final String userName, final String userPassword, final boolean useCookie, final boolean assertSuccess)
    {
        if (assertSuccess)
        {
            loginUsingForm(userName,userPassword,useCookie,assertSuccess);
        }
        else
        {
            loginUsingURLParameters(userName,userPassword,useCookie);
        }
    }

    public void loginUsingForm(final String userName, final String userPassword)
    {
        loginUsingForm(userName,userPassword,false,false);
    }

    public void loginUsingForm(final String userName, final String userPassword, final boolean useCookie, final boolean assertSuccess)
    {
        log("Logging in as '" + userName + "'");

        gotoDashboard();
        tester.beginAt("/login.jsp");
 	    tester.setFormElement("os_username", userName);
 	 	tester.setFormElement("os_password", userPassword);
 	 	if (useCookie)
 	 	{
 	 	    tester.checkCheckbox("os_cookie", "true");
 	 	}
 	 	tester.setWorkingForm("login-form");
 	 	tester.submit();

        if (assertSuccess)
        {
            assertLoginSuccess(userName);
        }

        lastPasswordUsed = userPassword;
    }

    public void loginUsingURLParameters(final String userName, final String userPassword, final boolean useCookie)
    {
        log("Logging in as '" + userName + "'");

        String dashboardWithLoginParamsURL = "secure/Dashboard.jspa?os_username=" + URLUtil.encode(userName) + "&os_password=" + URLUtil.encode(userPassword);

        if (useCookie)
        {
            dashboardWithLoginParamsURL += "&os_cookie=true";
        }

        tester.gotoPage(dashboardWithLoginParamsURL);

        lastPasswordUsed = userPassword;
    }

    private void assertLoginSuccess(String userName)
    {
        if (tester.getDialog().isTextInResponse("login-form-submit"))
        {
            Assert.fail("User '" + userName + "' failed to login.");
        }
    }

    public void logout()
    {
        String token;
        try
        {
            token = page.getXsrfToken();
        }
        catch (Exception e)
        {
            token = "";
        }

        // only do a new GET if we don't already have the XSRF token
        if (StringUtils.isBlank(token))
        {
            tester.gotoPage("/secure/ViewKeyboardShortcuts.jspa"); // quick to load and parse
            token = page.getXsrfToken();
        }

        log("Logging out");
        tester.beginAt("/secure/Logout!default.jspa?" + XsrfCheck.ATL_TOKEN + "=" + token);
        // check for confirm
        if (tester.getDialog().isTextInResponse("Confirm logout"))
        {
            tester.setWorkingForm("confirm-logout");
            tester.clickButton("confirm-logout-submit");
        }
    }

    public String getCurrentPage()
    {
        final String urlString = tester.getDialog().getResponse().getURL().toString();
        final String ctx = environmentData.getContext();
        if (ctx.length() > 0)
        {
            // return everything after the context
            return urlString.substring(urlString.indexOf(ctx) + ctx.length());
        }
        else
        {
            // return everything after the base url
            final String base = environmentData.getBaseUrl().toString();
            return urlString.substring(base.length());
        }
    }

    public void clickLink(final WebLink webLink)
    {
        String url = webLink.getURLString();
        if (!url.startsWith("/"))
        {
            // This is a relative URL, construct an absolute URL using the current page as context.
            url = makeAbsoluteUrl(url, tester.getDialog().getResponse().getURL());
        }
        if (url.startsWith(getEnvironmentData().getContext()))
        {
            url = url.substring(getEnvironmentData().getContext().length());
        }
        tester.gotoPage(url);
    }

    public void clickLinkWithExactText(String text)
    {
        final WebLink link = page.getLinksWithExactText(text)[0];
        clickLink(link);
    }

    public void clickLinkInTableCell(WebTable table, int row, int col, String linkText)
    {
        WebLink webLink = table.getTableCell(row, col).getLinkWith(linkText);
        clickLink(webLink);
    }

    public void clickLinkInTableCell(final String tableId, final int row, final int col, final String linkText)
    {
        clickLinkInTableCell(tester.getDialog().getWebTableBySummaryOrId(tableId), row, col, linkText);
    }

    public void jiraLog(String logMessage)
    {
        backdoor.getTestkit().logControl().info(logMessage);
    }

    public void gotoDashboard()
    {
        dashboard.navigateTo();
    }

    public Dashboard dashboard()
    {
        return dashboard;
    }

    public Workflows workflows()
    {
        return workflows;
    }

    public void clickOnNext()
    {
        tester.submit(BUTTON_NEXT);
    }

    /**
     * Goes to the admin section, or, if already in the admin section, does nothing.
     */
    public void gotoAdmin()
    {
        if (tester.getDialog().getResponse().getContentType().equals("text/html"))
        {
            HTMLElement element = null;

            try
            {
                element = tester.getDialog().getResponse().getElementWithID("adminMenu");
            }
            catch (SAXException e)
            {
                log("problem trying to find admin menu div, mustn't be on the admin menu");
            }

            if (element == null)
            {
                if (tester.getDialog().isLinkPresent(ADMIN_LINK_CLASSIC_HEADER))
                {
                    log("going to admin page via link");
                    tester.clickLink(ADMIN_LINK_CLASSIC_HEADER);
                }
                else
                {
                    log("going to admin page via URL");
                    tester.gotoPage("/secure/project/ViewProjects.jspa");
                }
            }
        }
        else // We are on an RSS Feed or a JSON Response or something
        {
            log("going to admin page via URL");
            tester.gotoPage("/secure/project/ViewProjects.jspa");
        }
    }

    public void webSudoAuthenticate(final String password)
    {
        if (locators.id("login-notyou").exists())
        {
            tester.setFormElement("webSudoPassword", password);
            tester.setWorkingForm("login-form");
            tester.submit();
        }
    }

    public void webSudoAuthenticateUsingLastPassword()
    {
        webSudoAuthenticate(lastPasswordUsed != null ? lastPasswordUsed : "admin");
    }

    public void disableWebSudo()
    {

    }

    public void gotoPage(String url)
    {
        tester.gotoPage(url);
    }

    @Override
    public <T extends WebTestPage> T gotoPage(Class<T> pageClass)
    {
        T pageObject = constructDefault(pageClass);
        pageObject.setContext(getFuncTestHelperFactory());
        gotoPage(pageObject.baseUrl());
        return pageObject;
    }

    @Override
    public <T extends WebTestPage> T gotoPageWithParams(Class<T> pageClass, String params)
    {
        T pageObject = constructDefault(pageClass);
        pageObject.setContext(getFuncTestHelperFactory());
        gotoPage(pageObject.baseUrl() + "?" + params);
        return pageObject;
    }

    private <T> T constructDefault(Class<T> pageClass)
    {
        Class[] params = {};
        try
        {
            Constructor<T> constructor = pageClass.getConstructor(params);
            return constructor.newInstance();
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void gotoResource(String resourcePath)
    {
        if (resourcePath.startsWith("/"))
        {
            resourcePath = resourcePath.substring(1);
        }
        resourcePath = makeAbsoluteUrl(resourcePath, tester.getDialog().getResponse().getURL());
        if (resourcePath.startsWith(getEnvironmentData().getContext()))
        {
            resourcePath = resourcePath.substring(getEnvironmentData().getContext().length());
        }
        tester.gotoPage(resourcePath);
    }

    public void gotoAdminSection(String linkId)
    {
        gotoAdmin();
        tester.clickLink(linkId);
    }

    public void gotoCustomFields()
    {
        tester.gotoPage(PAGE_CUSTOM_FIELDS);
    }

    public void gotoWorkflows()
    {
        tester.gotoPage(PAGE_LIST_WORKFLOWS);
    }

    public void runReport(Long projectId, String reportKey)
    {
        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + projectId + "&reportKey=" + reportKey);
    }

    public void runReport(Long projectId, String reportKey, Long filterId, String mapper)
    {
        tester.gotoPage("/secure/ConfigureReport.jspa?selectedProjectId=" + projectId + "&reportKey=" + reportKey + "&filterid=" + filterId + "&mapper=" + mapper);
    }

    public void browseProject(final String projectKey)
    {
        tester.gotoPage("/browse/" + projectKey);
    }

    public void browseProjectTabPanel(final String projectKey, final String tab)
    {
        browseProject(projectKey);
        clickLinkOrAssertSpanPresent(tab);
    }

    public void browseComponentTabPanel(final String projectKey, final String componentName, final String tab)
    {
        browseProjectTabPanel(projectKey, PROJECT_PLUGIN_PREFIX + "components-panel-panel");
        tester.clickLinkWithText(componentName);
        clickLinkOrAssertSpanPresent(tab);
    }

    public void browseVersionTabPanel(final String projectKey, final String versionName, final String tab)
    {
        browseProjectTabPanel(projectKey, PROJECT_PLUGIN_PREFIX + "versions-panel-panel");
        tester.clickLinkWithText(versionName);
        clickLinkOrAssertSpanPresent(tab);
    }

    public void browseComponentTabPanel(final String projectKey, final String componentName)
    {
        browseProjectTabPanel(projectKey, PROJECT_PLUGIN_PREFIX + "components-panel-panel");
        tester.clickLinkWithText(componentName);
    }

    public void browseVersionTabPanel(final String projectKey, final String versionName)
    {
        browseProjectTabPanel(projectKey, PROJECT_PLUGIN_PREFIX + "versions-panel-panel");
        tester.clickLinkWithText(versionName);
    }

    /**
     * Attempts to click a link, or if the link is not present, checks that a span with the same id is present instead,
     * which means that the link that is trying to be clicked is actually "active" and hence not a link.
     *
     * @param linkId the id of the link or span to look for
     */
    private void clickLinkOrAssertSpanPresent(final String linkId)
    {
        WebLink linkElement = null;
        try
        {
            linkElement = tester.getDialog().getResponse().getLinkWithID(linkId);
        }
        catch (SAXException e)
        {
            log("Can't find link with id: " + linkId + ", might already be on that tab panel");
        }
        if (linkElement != null)
        {
            tester.clickLink(linkId);
        }
        else
        {
            // if no link - a span should exist with the same id
            HTMLElement spanElement = null;
            try
            {
                spanElement = tester.getDialog().getResponse().getElementWithID(linkId);
            }
            catch (SAXException e)
            {
                log("Can't find span with id: " + linkId + ", this is bad.");
            }
            if (spanElement == null)
            {
                throw new IllegalStateException("Could not browse to project tab panel with link id '" + linkId + "' - tab did not exist.");
            }
        }
    }

    public FilterNavigation manageFilters()
    {
        return manageFiltersNavigation;
    }

    public FilterNavigation filterPickerPopup()
    {
        return filterPickerPopup;
    }

    public UserProfile userProfile()
    {
        return userProfile;
    }

    public IssueNavigation issue()
    {
        return issue;
    }

    public void gotoFullContentView(String jql)
    {
        gotoPage("/sr/jira.issueviews:searchrequest-fullcontent/temp/SearchRequest.html?tempMax=10000&jqlQuery=" + jql);
    }

    public void gotoXmlView(String jql)
    {
        gotoPage("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?tempMax=10000&jqlQuery=" + jql);
    }

    public IssueNavigatorNavigation issueNavigator()
    {
        return issueNavigator;
    }

    private String makeAbsoluteUrl(final String relativeUrl, final URL currentPage)
    {
        // Get the context from the current page
        String path = currentPage.getPath();
        // This will look like /jira/secure/admin/ViewStuff!default.jspa?freak=true so we strip off everything after the last slash.
        int pos = path.lastIndexOf('/');
        path = path.substring(0, pos + 1);
        return path + relativeUrl;
    }
}
