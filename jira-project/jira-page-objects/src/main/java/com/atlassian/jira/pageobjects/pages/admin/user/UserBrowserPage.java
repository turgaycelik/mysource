package com.atlassian.jira.pageobjects.pages.admin.user;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.webdriver.utils.Check;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.framework.elements.PageElements.transformTimed;
import static com.atlassian.jira.pageobjects.framework.util.JiraLocators.byCellType;
import static com.atlassian.webdriver.utils.by.ByDataAttribute.byTagAndData;

/**
 * Admin user browser.
 *
 * @since v4.4
 */
public class UserBrowserPage extends AbstractJiraAdminPage
{
    private static final String URI = "/secure/admin/user/UserBrowser.jspa";

    @Inject protected ExtendedElementFinder extendedFinder;

    private String MAX = "1000000";
    private String TEN = "10";
    private String TWENTY = "20";
    private String FIFTY = "50";
    private String ONE_HUNDRED = "100";

    private final Map<String, User> users;

    @FindBy(id="user-filter-submit")
    protected WebElement filterSubmit;

    @ElementBy (id = "create_user")
    protected PageElement addUserLink;

    @ElementBy (className = "results-count-total")
    protected PageElement numUsers;

    @ElementBy (cssSelector = ".count-pagination > .pagination")
    protected PageElement pagination;

    @FindBy (id = "user_browser_table")
    private WebElement userTableElement; // TODO remove as we remove the old API

    @ElementBy (id = "user_browser_table")
    protected PageElement userTable;

    @FindBy (name = "userNameFilter")
    protected WebElement filterUsersByUserName;

    @FindBy (name = "emailFilter")
    protected WebElement filterUsersByEmail;

    @ElementBy (name = "max")
    protected SelectElement filterUsersPerPage;

    @ElementBy(className = "user-row", within = "userTable")
    protected Iterable<PageElement> userRows;

    public UserBrowserPage()
    {
        users = new HashMap<String, User>();
    }

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("user_browser_table")).timed().isPresent();
    }

    @Init
    public void init()
    {
        getUsers();
    }

    public boolean hasUser(User user)
    {
        return users.containsKey(user.getUserName());
    }

    /**
     * When editing a users groups from this page, EditUserGroups always returns back to
     * UserBrowser unless there was an error.
     *
     * @param user user to edit
     * @return edit user groups page instance
     * @deprecated use {@link #findRow(com.atlassian.jira.pageobjects.global.User)} instead
     */
    public EditUserGroupsPage editUserGroups(User user)
    {

        if (hasUser(user))
        {
            String editGroupsId = "editgroups_" + user.getUserName();

            driver.findElement(By.id(editGroupsId)).click();

            return pageBinder.bind(EditUserGroupsPage.class, user.getUserName());
        }
        else
        {
            throw new IllegalStateException("User: " + user.getUserName() + " was not found.");
        }

    }

    /**
     *
     *
     * @param user blah
     * @return blah
     * @deprecated use {@link #findRow(com.atlassian.jira.pageobjects.global.User)} instead
     */
    @Deprecated
    public Set<String> getUsersGroups(User user)
    {

        if (hasUser(user))
        {
            Set<String> groups = new HashSet<String>();
            PageElement groupCol = userTable.findAll(ByJquery.$("#" + user.getUserName()).parents("tr.vcard").find("td")).get(4);

            for (PageElement groupEl : groupCol.findAll(By.tagName("a")))
            {
                groups.add(groupEl.getText());
            }

            return groups;
        }
        else
        {
            throw new IllegalStateException("User: " + user.getUserName() + " was not found.");
        }
    }

    /**
     *
     *
     * @param user blah
     * @return blah
     * @deprecated use {@link #findRow(com.atlassian.jira.pageobjects.global.User)} instead
     */
    public ViewUserPage gotoViewUserPage(User user)
    {
        if (hasUser(user))
        {
            User actualUser = users.get(user.getUserName());
            WebElement userEmailLink = driver.findElement(By.id(actualUser.getUserName()));
            userEmailLink.click();
            return pageBinder.bind(ViewUserPage.class, user.getUserName());
        }
        else
        {
            throw new IllegalStateException("The user: " + user + " was not found on the page");
        }
    }

    public int getNumberOfUsers()
    {
        return Integer.valueOf(numUsers.getText());
    }

    public UserBrowserPage gotoResultPage(int page)
    {
        pagination.find(By.linkText(String.valueOf(page))).click();

        return pageBinder.bind(UserBrowserPage.class);
    }

    /**
     * Navigates to the addUserPage by activating the add User link
     *
     * @return add user page
     */
    public AddUserPage gotoAddUserPage()
    {
        addUserLink.click();
        return pageBinder.bind(AddUserPage.class);
    }

    /**
     * Takes User object and fills out the addUserPage form and creates the user.
     * @param user the user to create
     * @param sendPasswordEmail sets the send email tick box to on or off
     * @return the user browser page which should have the new user added to the count.
     */
    public UserBrowserPage addUser(User user, boolean sendPasswordEmail)
    {
        AddUserPage addUserPage = gotoAddUserPage();
        return addUserPage.addUser(user.getUserName(), user.getPassword(), user.getFullName(), user.getEmail(), sendPasswordEmail).createUser();
    }

    public UserBrowserPage filterByUserName(String username)
    {
        filterUsersByUserName.sendKeys(username);
        filterSubmit.click();

        return pageBinder.bind(UserBrowserPage.class);
    }

    public UserBrowserPage filterByEmail(String email)
    {
        filterUsersByEmail.sendKeys(email);
        filterSubmit.click();

        return pageBinder.bind(UserBrowserPage.class);
    }

    public UserBrowserPage setUserFilterToShowAllUsers()
    {
        filterUsersPerPage.select(Options.value(MAX));
        filterSubmit.click();

        return pageBinder.bind(UserBrowserPage.class);
    }

    public UserBrowserPage setUserFilterTo10Users()
    {
        filterUsersPerPage.select(Options.value(TEN));
        filterSubmit.click();

        return pageBinder.bind(UserBrowserPage.class);
    }

    // TODO remove once moved to the new API
    private void getUsers()
    {
        users.clear();

        List<WebElement> rows = userTableElement.findElements(By.tagName("tr"));

        for (WebElement row : rows)
        {
            // Check it's not the headings (th) tags.
            if (Check.elementExists(By.tagName("td"), row))
            {
                List<WebElement> cols = row.findElements(By.tagName("td"));

                String username = cols.get(0).getText();
                String email = cols.get(1).getText();
                String fullName = cols.get(2).getText();

//                Set<Group> groups = new HashSet<Group>();
//
//                for (WebElement group : cols.get(4).findElements(By.tagName("a")))
//                {
//                    groups.add(new Group(group.getText()));
//                }

                users.put(username, new User(username, fullName, email, null));
            }
        }

    }


    // NEW API meant to replace most of the above - using the row pattern

    public Iterable<UserRow> getUserRows()
    {
        return Iterables.transform(userRows, PageElements.bind(pageBinder, UserRow.class));
    }

    public TimedQuery<Iterable<UserRow>> getUserRowsTimed()
    {
        return transformTimed(timeouts, pageBinder,
                extendedFinder.within(userTable).newQuery(By.className("user-row")).supplier(),
                UserRow.class);
    }

    public boolean hasRow(User user)
    {
        return userTable.find(byTagAndData(PageElements.TR, "user", user.getUserName())).isPresent();
    }

    /**
     * Find row for given user
     *
     * @param user user to find
     * @return user row
     * @throws java.util.NoSuchElementException if no user row found for given user (use
     * {@link #hasRow(com.atlassian.jira.pageobjects.global.User)} to check)
     */
    public UserRow findRow(final User user)
    {
        return Iterables.find(getUserRows(), new Predicate<UserRow>()
        {
            @Override
            public boolean apply(@Nullable UserRow input)
            {
                return input.getUsername().equals(user.getUserName());
            }
        });
    }

    public UserRow findRow(final String userName)
    {
        return Iterables.find(getUserRows(), new Predicate<UserRow>()
        {
            @Override
            public boolean apply(@Nullable UserRow input)
            {
                return input.getUsername().equals(userName);
            }
        });
    }

    @Override
    public String linkId()
    {
        return "user_browser";
    }

    public static class FilterForm
    {
        // this should encapsulate the user filter form
    }

    /**
     * Encapsulates single row in the user browser.
     *
     */
    public static final class UserRow
    {
        @Inject private PageBinder pageBinder;

        private final PageElement usernameCell;
        private final PageElement loginDetailsCell;
        private final PageElement operationsCell;
        private final PageElement userDirectory;
        private final List<PageElement> userGroupLinks;

        public UserRow(PageElement rowElement)
        {
            this.usernameCell = rowElement.find(byCellType("username"));
            this.loginDetailsCell = rowElement.find(byCellType("login-details"));
            this.operationsCell = rowElement.find(byCellType("operations"));
            this.userDirectory = rowElement.find(byCellType("user-directory"));
            this.userGroupLinks = rowElement.find(byCellType("user-groups")).findAll(By.tagName("a"));
        }

        public String getUsername()
        {
            return usernameCell.getText();
        }

        public boolean canResetLoginCount()
        {
            return getResetLoginCountButton().isPresent();
        }

        public UserBrowserPage resetLoginCount()
        {
            // doesn't work
            getResetLoginCountButton().click();
            return pageBinder.bind(UserBrowserPage.class);
        }

        public ViewUserPage view()
        {
            final String username = getUsername();
            usernameCell.find(By.tagName("a")).click();
            return pageBinder.bind(ViewUserPage.class, username);
        }

        public EditUserGroupsPage editGroups()
        {
            final String username = getUsername();
            operationsCell.find(By.className("editgroups_link")).click();
            return pageBinder.bind(EditUserGroupsPage.class, username);
        }

        // TODO other operations


        // TODO expose cells


        public Iterable<PageElement> getUserGroupLinks()
        {
            return userGroupLinks;
        }

        public Iterable<String> getUserGroupNames()
        {
            return Iterables.transform(getUserGroupLinks(), new Function<PageElement, String>()
            {
                @Override
                public String apply(@Nullable final PageElement pageElement)
                {
                    return pageElement == null ? null : pageElement.getText();
                }
            });
        }

        public PageElement getUserDirectory()
        {
            return userDirectory;
        }

        private PageElement getResetLoginCountButton()
        {
            return loginDetailsCell.find(byTagAndData("a", "link-type", "reset-login-type"));
        }
    }
}
