package com.atlassian.jira.pageobjects.pages.admin.user;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * @since 4.4
 */
public class ViewUserPage extends AbstractJiraPage
{

	private static final String URI = "/secure/admin/user/ViewUser.jspa?name=";

	@ElementBy(id = "username")
	protected PageElement username;

	@FindBy(className = "fn")
	protected WebElement fullname;

	@FindBy(className = "email")
	protected WebElement email;

	@FindBy(id = "loginCount")
	protected WebElement loginCount;

	@FindBy(id = "lastLogin")
	protected WebElement lastLogin;

	@FindBy(id = "previousLogin")
	protected WebElement previousLogin;

	@FindBy(id = "lastFailedLogin")
	protected WebElement lastFailedLogin;

	@FindBy(id = "currentFailedLoginCount")
	protected WebElement currentFailedLoginCount;

	@FindBy(id = "totalFailedLoginCount")
	protected WebElement totalFailedLoginCount;

	@ElementBy(id = "deleteuser_link")
	protected PageElement deleteUserLink;

	@FindBy(id = "editgroups_link")
	protected WebElement editUserLink;

	@ElementBy(linkText = "Edit Details")
	protected PageElement editUserDetailsLink;

	@ElementBy(linkText = "Set Password")
	protected PageElement setPasswordLink;

	// TODO: groups are currently image bullets, don't handle them. Make JIRA change to <ul>.

	protected final String name;

	public ViewUserPage(String username)
	{
		this.name = username;
	}

	@Override
	public String getUrl()
	{
		return URI + name;
	}

	@Override
	public TimedCondition isAt()
	{
		return deleteUserLink.timed().isPresent();
	}

	public DeleteUserPage gotoDeleteUserPage()
	{
		deleteUserLink.click();
		return pageBinder.bind(DeleteUserPage.class);
	}

	public EditUserPasswordPage setPassword()
	{
		String username = getUsername();
		setPasswordLink.click();
		return pageBinder.bind(EditUserPasswordPage.class, username);
	}

	public EditUserDetailsPage editDetails()
	{
		String username = getUsername();
		editUserDetailsLink.click();
		return pageBinder.bind(EditUserDetailsPage.class, username);
	}

	public Page viewProjectsRoles()
	{
		throw new UnsupportedOperationException("View project roles on ViewUSerPage is not supported");
	}

	public EditUserGroupsPage editGroups()
	{
		final String username = getUsername();
		editUserLink.click();
		return pageBinder.bind(EditUserGroupsPage.class, username);
	}

	public Page editProperties()
	{
		throw new UnsupportedOperationException("Edit properties on the ViewUserPage is not supported");
	}

	public Page viewPublicProfile()
	{
		throw new UnsupportedOperationException("View public profile on ViewUserPage is not supported");
	}

	public String getUsername()
	{
		return username.getText();
	}

	public String getFullname()
	{
		return fullname.getText();
	}

	public String getEmail()
	{
		return email.getText();
	}

	public String getLoginCount()
	{
		return loginCount.getText();
	}

	public String getLastLogin()
	{
		return lastLogin.getText();
	}

	public String getPreviousLogin()
	{
		return previousLogin.getText();
	}

	public String getLastFailedLogin()
	{
		return lastFailedLogin.getText();
	}

	public String getCurrentFailedLoginCount()
	{
		return currentFailedLoginCount.getText();
	}

	public String getTotalFailedLoginCount()
	{
		return totalFailedLoginCount.getText();
	}
}
