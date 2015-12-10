package com.atlassian.jira.bc.user;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

/**
 * Pass through package-protected constraints
 */
public class UserValidationResultBuilder
{
    private ErrorCollection errorCollection = new SimpleErrorCollection();
    private String username = "charlie";
    private String password = "$ecure";
    private String email = "charlie@atlassian.com";
    private String fullname = "Charlie of Atlassian";

    public UserValidationResultBuilder setErrorCollection(final ErrorCollection errorCollection)
    {
        this.errorCollection = errorCollection;
        return this;
    }

    public UserValidationResultBuilder setUsername(final String username)
    {
        this.username = username;
        return this;
    }

    public UserValidationResultBuilder setPassword(final String password)
    {
        this.password = password;
        return this;
    }

    public UserValidationResultBuilder setEmail(final String email)
    {
        this.email = email;
        return this;
    }

    public UserValidationResultBuilder setFullname(final String fullname)
    {
        this.fullname = fullname;
        return this;
    }

    public UserValidationResultBuilder addError(final String field, final String message) {
        errorCollection.addError(field, message);
        return this;
    }

    public UserService.CreateUserValidationResult buildUserCreateErr() {
        return new UserService.CreateUserValidationResult(errorCollection);
    }

    public UserService.CreateUserValidationResult buildUserCreate() {
        return new UserService.CreateUserValidationResult(username, password, email, fullname);
    }

    public UserService.UpdateUserValidationResult buildUserUpdateErr()
    {
        return new UserService.UpdateUserValidationResult(errorCollection);
    }

    public UserService.UpdateUserValidationResult buildUserUpdate(final ApplicationUser user)
    {
        return new UserService.UpdateUserValidationResult(user);
    }

    public UserService.DeleteUserValidationResult buildUserDelete()
    {
        ApplicationUser applicationUser = new MockApplicationUser(username);
        return new UserService.DeleteUserValidationResult(applicationUser);
    }

    public UserService.DeleteUserValidationResult buildUserDeleteErr()
    {
        return new UserService.DeleteUserValidationResult(errorCollection);
    }
}
