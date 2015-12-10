package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidEmailAddressException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.directory.DirectoryPermissionException;
import com.atlassian.crowd.manager.login.ForgottenLoginManager;
import com.atlassian.crowd.manager.login.exception.InvalidResetPasswordTokenException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.token.ResetPasswordToken;

/**
 * We don't provide any of this functionality.  Just need to provide this guy because
 * Crowd Rest Plugin needs an implementation.
 *
 * @since v4.3
 */
public class NoopForgottenLoginManager implements ForgottenLoginManager
{

    @Override
    public void sendResetLink(Application application, String s)
            throws UserNotFoundException, InvalidEmailAddressException, ApplicationPermissionException
    {
    }

    @Override
    public boolean sendUsernames(Application application, String s) throws InvalidEmailAddressException
    {
        return false;
    }

    @Override
    public void sendResetLink(long l, String s)
            throws DirectoryNotFoundException, UserNotFoundException, InvalidEmailAddressException, OperationFailedException
    {
    }

    @Override
    public boolean isValidResetToken(long l, String s, String s1)
    {
        return false;
    }

    @Override
    public void resetUserCredential(long l, String s, PasswordCredential passwordCredential, String s1)
            throws DirectoryNotFoundException, UserNotFoundException, InvalidResetPasswordTokenException, OperationFailedException, InvalidCredentialException, DirectoryPermissionException
    {
    }

    @Override
    public ResetPasswordToken createAndStoreResetToken(long directoryId, String username)
    {
        return null;
    }
}
