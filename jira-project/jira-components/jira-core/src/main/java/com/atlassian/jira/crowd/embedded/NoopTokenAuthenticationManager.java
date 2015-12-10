package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationAccessDeniedException;
import com.atlassian.crowd.manager.authentication.TokenAuthenticationManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.authentication.ApplicationAuthenticationContext;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.model.token.Token;
import com.atlassian.crowd.model.token.TokenLifetime;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.Date;
import java.util.List;

/**
 * We don't provide any of this functionality.  Just need to provide this guy because
 * Crowd Rest Plugin needs an implementation.
 *
 * @since v4.3
 */
public class NoopTokenAuthenticationManager implements TokenAuthenticationManager
{
    @Override
    public Token authenticateApplication(ApplicationAuthenticationContext applicationAuthenticationContext)
            throws InvalidAuthenticationException
    {
        return null;
    }

    @Override
    public Token authenticateUser(UserAuthenticationContext userAuthenticationContext, TokenLifetime tokenLifetime)
            throws InvalidAuthenticationException, OperationFailedException, InactiveAccountException, ApplicationAccessDeniedException, ExpiredCredentialException, ApplicationNotFoundException
    {
        return null;
    }

    @Override
    public Token authenticateUser(UserAuthenticationContext userAuthenticationContext)
            throws InvalidAuthenticationException, OperationFailedException, InactiveAccountException, ApplicationAccessDeniedException, ExpiredCredentialException, ApplicationNotFoundException
    {
        return null;
    }

    @Override
    public Token authenticateUserWithoutValidatingPassword(UserAuthenticationContext userAuthenticationContext)
            throws InvalidAuthenticationException, OperationFailedException, InactiveAccountException, ApplicationAccessDeniedException, ApplicationNotFoundException
    {
        return null;
    }

    @Override
    public Token validateApplicationToken(String s, ValidationFactor[] validationFactors) throws InvalidTokenException
    {
        return null;
    }

    @Override
    public Token validateUserToken(String s, ValidationFactor[] validationFactors, String s1)
            throws InvalidTokenException, ApplicationAccessDeniedException, OperationFailedException
    {
        return null;
    }

    @Override
    public void invalidateToken(String s)
    {
    }

    @Override
    public void removeExpiredTokens()
    {
    }

    @Override
    public User findUserByToken(String s, String s1)
            throws InvalidTokenException, OperationFailedException, ApplicationNotFoundException
    {
        return null;
    }

    @Override
    public Token findUserTokenByKey(String s, String s2)
            throws InvalidTokenException, ApplicationAccessDeniedException, OperationFailedException, ApplicationNotFoundException
    {
        return null;
    }

    @Override
    public List<Application> findAuthorisedApplications(User user, String s)
            throws OperationFailedException, DirectoryNotFoundException, ApplicationNotFoundException
    {
        return null;
    }

    @Override
    public void invalidateTokensForUser(String s, String s2, String s3)
            throws UserNotFoundException, ApplicationNotFoundException
    {
    }

    @Override
    public Date getTokenExpiryTime(Token token)
    {
        return null;
    }
}
