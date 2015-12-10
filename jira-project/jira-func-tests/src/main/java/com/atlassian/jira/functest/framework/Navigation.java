package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.navigation.FilterNavigation;
import com.atlassian.jira.functest.framework.navigation.IssueNavigation;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.page.WebTestPage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;

/**
 * An interface for navigating around JIRA
 *
 * @since v3.13
 */
public interface Navigation
{
    /**
     * This assumes the standard JIRA test behaviour of having the user name the same value as the password
     *
     * @param userName the user name and password to login as
     */
    void login(String userName);

    /**
     * Login the given user.
     * <p>
     * Note that this will assert that the login suceeded.
     *
     * @param userName the user name and password to login as
     *
     * @see #loginAttempt(String, String)
     */
    void login(String userName, String userPassword);

    void login(String userName, String userPassword, boolean useCookie);

    /**
     * Login by adding parameters to the Dashboard URL
     *
     * e.g. http://localhost:8090/jira/secure/Dashboard.jspa?os_username=admin&os_password=admin
     *
     * @param userName
     * @param userPassword
     * @param useCookie
     */
    void loginUsingURLParameters(String userName, String userPassword, boolean useCookie);

    /**
     * Login by navigating to the Dashboard and filling out the login form and submitting it
     *
     * @param userName
     * @param userPassword
     * @param useCookie
     * @param assertSuccess
     */
    void loginUsingForm(String userName, String userPassword, boolean useCookie, final boolean assertSuccess);

    void loginUsingForm(String userName, String userPassword);

    /**
     * Attempts to login the given user without asserting success.
     *
     * @param userName
     * @param userPassword
     */
    void loginAttempt(String userName, String userPassword);

    /**
     * Logout current user.
     *
     */
    void logout();

    /**
     * Returns the relative path of the current page.
     * E.g. if currently at http://web.com:9999/jira/browse/HSP-1, this method will return "/browse/HSP-1".
     * Also caters for situations when no context path is set, e.g. http://standalone.com/browse/HSP-1.
     *
     * Use this method with {@link net.sourceforge.jwebunit.WebTester#gotoPage}.
     *
     * @return the relative path of the current page.
     */
    String getCurrentPage();

    /**
     * Follows the URL in the given WebLink.
     *
     * @param webLink WebLink
     */
    public void clickLink(final WebLink webLink);

    /**
     * Finds the first link on the page with the exact text and follows the URL.
     *
     * Note: This is not the same as {@link WebTester#clickLinkWithText(String)} as it does an exact text match, but
     * also no onclick events are fired. It simply follows the URL specified by the link tag.
     *
     * @param text the text of the link to match
     */
    void clickLinkWithExactText(String text);

    /**
     * Click on a link in a particular table cell with the given text.
     *
     * @param tableId The id of the table whose link we're clicking
     * @param row The table row that contains the link
     * @param col The table column that contains the link
     * @param linkText The text of the link
     */
    void clickLinkInTableCell(String tableId, int row, int col, String linkText);

    /**
     * Click on a link in a particular table cell with the given text.
     *
     * @param table The table whose link we're clicking
     * @param row The table row that contains the link
     * @param col The table column that contains the link
     * @param linkText The text of the link
     */
    void clickLinkInTableCell(WebTable table, int row, int col, String linkText);

    /**
     * Writes the specified message to the atlassian-jira.log on the server side.  Works because we have a
     * magic servlet ready for this log messages.
     *
     * @param logMessage the message to log
     */
    void jiraLog(String logMessage);

    /**
     * Navigates to the given relative URL.
     *
     * <p> Note that the "base URL" is automatically prepended to the given URL including the context.
     * eg: If the URL on your dev machine is "http://localhost:8091/jira/secure/Signup!default.jspa",
     * then you would supply "secure/Signup!default.jspa" as the URL parameter to this method.
     *
     * @param url URL to navigate to.
     */
    void gotoPage(String url);

    <T extends WebTestPage> T gotoPage(Class<T> pageClass);

    <T extends WebTestPage> T gotoPageWithParams(Class<T> pageClass, String params);

    /**
     * <p>
     * Navigates to the relative resource path.
     *
     * <p>
     * Note that the This method will prepend current location
     * to the resource path, e.g. if the current location is "http://localhost:8091/jira/secure/Signup!default.jspa",
     * and given <tt>resource</tt> is "Signout.jspa, the resulting URL
     * will be "http://localhost:8091/jira/secure/Signout.jspa".
     *
     * @param resourcePath path to navigate to.
     */
    void gotoResource(String resourcePath);

    /**
     * Goto the current user's dashboard. This is a quick way to call <code>dashboard().navigateTo()</code>.
     */
    void gotoDashboard();

    /**
     * Navigates to the admin section
     */
    void gotoAdmin();

    /**
     * Checks if we have been redirected to the websudo login form and logs in.
     * @param password
     */
    public void webSudoAuthenticate (final String password);

    /**
     * Checks if we have been redirected to the websudo login form and logs in using the last password
     */
    public void webSudoAuthenticateUsingLastPassword ();

    /**
     * Historically this connected to a REST endpoint in the jira-functest-plugin to disable WebSudo
     * Currently this does nothing
     */
    public void disableWebSudo ();

    /**
     * Navigates to a particular page of the admin section
     *
     * @param linkId the link id of the admin section
     */
    void gotoAdminSection(String linkId);

    /**
     * Navigates to the 'Custom Fields' page in the administration section
     *
     */
    void gotoCustomFields();


    /**
     * Navigates to the workflows table in the admin section
     * 
     */
    void gotoWorkflows();

    /**
     * Run  the given report for the given project
     *
     * @param projectId The id of the project
     * @param reportKey The key of the report.
     */
    void runReport(Long projectId, String reportKey);


    /**
     * Run the given report for the given project using the given mapper
     *
     * @param projectId The id of the project
     * @param reportKey The key of the report
     * @param filterId The filter id used for the report
     * @param mapper The mapper used for the report (e.g. "labels")
     */
    void runReport(Long projectId, String reportKey, Long filterId, String mapper);

    /**
     * Navigates to the project's Browse page
     *
     * @param projectKey the key of the project e.g. "HSP"
     */
    void browseProject(String projectKey);

    /**
     * Navigates to the project tab panel for the specified project.
     *
     * @param projectKey the key of the project e.g. "HSP"
     * @param tab the key of the project tab panel e.g. "summary", "issues", "changelog", etc.
     */
    void browseProjectTabPanel(String projectKey, String tab);

    /**
     * Navigates to the component tab panel for the specified component.
     *
     * @param projectKey the key of the project e.g. "HSP"
     * @param componentName the name of the component e.g. "New Component 1"
     * @param tab the key of the project tab panel e.g. "summary", "issues", "changelog", etc.
     */
    void browseComponentTabPanel(String projectKey, String componentName, String tab);

    /**
     * Navigates to the component tab panel for the specified component.
     *
     * @param projectKey the key of the project e.g. "HSP"
     * @param versionName the name of the version e.g. "New Version 1"
     * @param tab the key of the project tab panel e.g. "summary", "issues", "changelog", etc.
     */
    void browseVersionTabPanel(String projectKey, String versionName, String tab);

    /**
     * Navigates to Browse Component page of the specified component
     *
     * @param projectKey the key of the project e.g. "HSP"
     * @param componentName the name of the component e.g. "New Component 1"
     */
    void browseComponentTabPanel(String projectKey, String componentName);

    /**
     * Navigates to Browse Version page of the specified component
     *
     * @param projectKey the key of the project e.g. "HSP"
     * @param versionName the name of the version e.g. "New Version 1"
     */
    void browseVersionTabPanel(String projectKey, String versionName);

    /**
     * Go to the 'Manage filters' screen.
     * Access API to navigate through issue filters.
     *
     * @return filter navigation
     */
    FilterNavigation manageFilters();

    /**
     * Access API to navigate through issues.
     *
     * @return issue navigation
     * @see IssueNavigation
     */
    IssueNavigation issue();

    void gotoFullContentView(String jql);
    void gotoXmlView(String jql);

    /**
     * Access API to navigate through issue navigator.
     *
     * @return issue navigator navigation
     * @see IssueNavigatorNavigation
     */
    IssueNavigatorNavigation issueNavigator();

    FilterNavigation filterPickerPopup();

    UserProfile userProfile();

    /**
     * Navigates to the dashboard, returning it.
     *
     * @return the Dashboard.
     */
    Dashboard dashboard();

    Workflows workflows();

    /**
     * Clicks on the 'Next' button (useful for bulk edits and such)
     */
    void clickOnNext();

}
