package com.atlassian.jira.functest.framework.admin.user;

import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.page.AbstractWebTestPage;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DeleteUserPage extends AbstractWebTestPage
{
    public static final Pattern SHARED_FILTERS = Pattern.compile("(.*)Shared Filter:(.*)");
    public static final Pattern FAVORITED_FILTERS = Pattern.compile("(.*)Favourited Filter:(.*)");
    public static final Pattern SHARED_DASHBOARDS = Pattern.compile("(.*)Shared Dashboard:(.*)");
    public static final Pattern FAVORITED_DASHBOARDS =  Pattern.compile("(.*)Favourited Dashboard:(.*)");
    public static final Pattern ASSIGNED_ISSUES = Pattern.compile("(.*)Assigned Issue:(.*)");
    public static final Pattern REPORTED_ISSUES =  Pattern.compile("(.*)Reported Issue:(.*)");

    public static final String USER_CANNOT_DELETE_SYSADMIN = "As a user with JIRA Administrators permission, you cannot delete users with JIRA System Administrators permission.";
    public static final String USER_CANNOT_DELETE_SELF = "You cannot delete the currently logged in user.";

    private final String DELETE_USER_BASE_URL = "/secure/admin/user/DeleteUser!default.jspa";
    private final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

    public String getUserCannotBeDeleteMessage(String user)
    {
        return "Cannot delete user. '" + user + "' has associations in JIRA that cannot be removed automatically.";
    }

    public DeleteUserPage clickDeleteUser()
    {
        funcTestHelperFactory.getTester().submit("Delete");
        return this;
    }

    public static String generateDeleteQueryParameters(String user)
    {
        return "returnUrl=UserBrowser.jspa&name=" + user;
    }

    public static String generateDeleteQueryParametersWithXsrf(String user, HtmlPage page)
    {
        //addXsrfToken matches on the ? in the url but returning the string with the ? in it
        //duplicates the ? when gotoPageWithParams is called later since it introduces a second ?
        return page.addXsrfToken("?returnUrl=UserBrowser.jspa&name=" + user).replace("?","");
    }

    public DeleteUserPage confirmNoDeleteButtonPresent()
    {
        funcTestHelperFactory.getTester().assertSubmitButtonNotPresent("Delete");
        return this;
    }

    public String getNumberForPluginErrorNamed(Pattern errorPattern)
    {
        return getTextPrefixForSelector(errorPattern, ".user-errors a");

    }

    public String getUserDeletionError()
    {
        return getCssFieldText("#user-cannot-be-deleted");
    }

    public String getUserDeleteSelfError()
    {
        return getCssFieldText("#user-delete-self-error");
    }

    public String getNonAdminDeletingSysAdminErrorMessage()
    {
        return getCssFieldText("#user-nonadmin-error");
    }

    public String getNumberFromErrorFieldNamed(Pattern errorPattern)
    {
        return getTextPrefixForSelector(errorPattern, ".user-errors a");
    }

    public String getNumberFromWarningFieldNamed(Pattern warningPattern)
    {
        return getTextPrefixForSelector(warningPattern, ".user-warnings a");
    }

    public String getNumberFromWarningFieldNamedNoLink(Pattern warningPattern)
    {
        return getTextPrefixForSelector(warningPattern, ".user-warnings span");
    }

    public CssLocator getProjectLink()
    {
        return funcTestHelperFactory.getLocator().css("#user-projects-summary a");
    }

    public CssLocator getComponentLink()
    {
        return funcTestHelperFactory.getLocator().css("#user-components-summary a");
    }

    private String getTextPrefixForSelector(Pattern elemToMatch, String cssSelector)
    {
        CssLocator selectedCss = funcTestHelperFactory.getLocator().css(cssSelector);
        String prefixNumber = "0";
        for (Node node : selectedCss.getNodes())
        {
            NodeList children = node.getChildNodes();
            String elemTextContents = children.item(0).getNodeValue();
            if(elemToMatch.matcher(elemTextContents).matches())
            {
                Matcher m = DIGIT_PATTERN.matcher(elemTextContents);
                if(m.find())
                {
                    prefixNumber = m.group(0);
                }
            }
        }

        return prefixNumber;
    }

    private String getCssFieldText(String cssSelector)
    {
        CssLocator css = funcTestHelperFactory.getLocator().css(cssSelector);
        if( css.getNodes().length > 0)
        {
            return css.getNodes()[0].getNodeValue();
        }
        return "";
    }


    @Override
    public String baseUrl()
    {
        return DELETE_USER_BASE_URL;
    }
}
