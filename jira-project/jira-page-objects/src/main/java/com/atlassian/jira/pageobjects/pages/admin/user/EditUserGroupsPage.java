package com.atlassian.jira.pageobjects.pages.admin.user;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.Check;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Conditions.or;

/**
 * @since 4.4
 */
public class EditUserGroupsPage extends AbstractJiraPage
{

    private static final String URI = "/secure/admin/user/EditUserGroups.jspa?name=";

    private static String ERROR_SELECTOR = ".aui-message.error ul li";

    // TODO parsing into memory objects is bad. Use page elements. Use AuiMessages etc. to handle errors
    private Set<String> errors = new HashSet<String>();

    private final String username;

    @ElementBy(id = "userGroupPicker")
    protected PageElement userGroupPickerContainer;

    @ElementBy(name = "groupsToLeave")
    protected SelectElement joinedGroups;

    @ElementBy(name = "groupsToJoin")
    protected SelectElement availablegroups;

    @ElementBy(name = "join")
    protected PageElement join;

    @ElementBy(name = "leave")
    protected PageElement leave;

    @ElementBy(id = "return_link")
    protected PageElement returnLink;

    public EditUserGroupsPage(final String username)
    {
        this.username = username;
    }


    public String getUrl()
    {
        return URI + username;
    }


    @Override
    public TimedCondition isAt()
    {
        return and(userGroupPickerContainer.timed().isPresent(), hasAvailableGroupsSelectOrInfoMessage(),
                hasGroupsToLeaveSelectOrInfoMessage());
    }

    private TimedCondition hasAvailableGroupsSelectOrInfoMessage()
    {
        return or(availablegroups.timed().isPresent(),
                messageInUserPickerContainer().timed().hasText("User is a member of all groups."));
    }

    private TimedCondition hasGroupsToLeaveSelectOrInfoMessage()
    {
        return or(joinedGroups.timed().isPresent(),
                messageInUserPickerContainer().timed().hasText("User isn't a member of any groups."));
    }

    private PageElement messageInUserPickerContainer()
    {
        return userGroupPickerContainer.find(By.cssSelector(".aui-message.info"));
    }

    @Init
    public void parsePage()
    {
        if (Check.elementExists(ByJquery.$(ERROR_SELECTOR), driver))
        {
            for (WebElement el : driver.findElements(ByJquery.$(ERROR_SELECTOR)))
            {
                errors.add(el.getText());
            }
        }
    }

    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }

    public boolean hasError(String errorStr)
    {
        return errors.contains(errorStr);
    }

    public ViewUserPage returnToUserView()
    {
        returnLink.click();

        return pageBinder.bind(ViewUserPage.class, username);
    }

    public EditUserGroupsPage addTo(String... groups)
    {
        return addTo(Arrays.asList(groups));
    }

    public EditUserGroupsPage addTo(final List<String> groups)
    {
        for (String group : groups)
        {
            availablegroups.select(Options.text(group));
        }
        join.click();
        return pageBinder.bind(EditUserGroupsPage.class, username);
    }

    public EditUserGroupsPage removeFrom(String... groups)
    {
        return removeFrom(Arrays.asList(groups));
    }

    public EditUserGroupsPage removeFrom(final List<String> groups)
    {
        for (String group : groups)
        {
            joinedGroups.select(Options.text(group));
        }
        leave.click();
        return pageBinder.bind(EditUserGroupsPage.class, username);
    }

    // bacause of https://jira.atlassian.com/browse/JRA-28188 adding/removing does not stay on this page, hence
    // workaround methods:

    public <P extends Page> P addTo(List<String> groups, Class<P> targetPage, Object... arguments)
    {
        for (String group : groups)
        {
            availablegroups.select(Options.text(group));
        }
        join.click();
        Poller.waitUntilFalse("The inline dialog was supposed to disappear", userGroupPickerContainer.timed().isVisible());
        return pageBinder.bind(targetPage, arguments);
    }

    public <P extends Page> P removeFrom(List<String> groups, Class<P> targetPage, Object... arguments)
    {
        for (String group : groups)
        {
            joinedGroups.select(Options.text(group));
        }
        leave.click();
        Poller.waitUntilFalse("The inline dialog was supposed to disappear", userGroupPickerContainer.timed().isVisible());
        return pageBinder.bind(targetPage, arguments);
    }
}
