package com.atlassian.jira.bc.user;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

/**
 * Provides helper methods for creating *Result objects for test.
 * Allows to hide *Result constructors.
 *
 * @since v4.0
 */
public final class UserServiceResultHelper
{
    public final static UserService.CreateUserValidationResult getCreateUserValidationResult()
    {
        return new UserService.CreateUserValidationResult(new SimpleErrorCollection());
    }

    public final static UserService.CreateUserValidationResult getCreateUserValidationResult(ErrorCollection errorCollection)
    {
        return new UserService.CreateUserValidationResult(errorCollection);
    }
    
    public final static UserService.CreateUserValidationResult getCreateUserValidationResult(
            final String username,
            final String password,
            final String email,
            final String fullname)
    {
        return new UserService.CreateUserValidationResult(
                username,
                password,
                email,
                fullname);
    }

    public final static UserService.DeleteUserValidationResult getDeleteUserValidationResult()
    {
        return new UserService.DeleteUserValidationResult(new SimpleErrorCollection());
    }
}
