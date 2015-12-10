package com.atlassian.jira.bc.security.login;

import com.atlassian.annotations.PublicApi;

/**
 * Represents all the properties necessary to render the login form or gadget.
 *
 * @since v4.2
 */
@PublicApi
public final class LoginProperties
{
    private final boolean allowCookies;
    private final boolean externalPasswordManagement;
    private final boolean externalUserManagement;
    private final boolean isPublicMode;
    private final boolean isElevatedSecurityCheckShown;
    private final boolean loginSucceeded;

    private final boolean loginError;
    private final boolean communicationError;
    private final boolean captchaFailure;
    private final boolean loginFailedByPermissions;

    private final String contactAdminLink;

    private LoginProperties(final boolean loginSucceeded, final boolean loginError, final boolean communicationError,
            final boolean allowCookies, final boolean externalPasswordManagement, final boolean externalUserManagement,
            final boolean publicMode, final boolean isElevatedSecurityCheckShown,
            final boolean captchaFailure, final boolean loginFailedByPermssions, String contactAdminLink)
    {
        this.loginSucceeded = loginSucceeded;
        this.loginError = loginError;
        this.communicationError = communicationError;
        this.allowCookies = allowCookies;
        this.externalPasswordManagement = externalPasswordManagement;
        this.externalUserManagement = externalUserManagement;
        this.isPublicMode = publicMode;
        this.isElevatedSecurityCheckShown = isElevatedSecurityCheckShown;
        this.captchaFailure = captchaFailure;
        this.loginFailedByPermissions = loginFailedByPermssions;
        this.contactAdminLink = contactAdminLink;
    }

    public boolean isLoginSucceeded()
    {
        return loginSucceeded;
    }

    public boolean isLoginError()
    {
        return loginError;
    }

    public boolean isCommunicationError()
    {
        return communicationError;
    }

    public boolean isAllowCookies()
    {
        return allowCookies;
    }

    /**
     * Returns true if JIRA is not able to manage user passwords, i.e. if there are no user directories
     * which allow updates to user passwords; false if otherwise.
     *
     * @return true if JIRA is not able to manage user passwords, i.e. if there are no user directories
     * which allow updates to user passwords; false if otherwise.
     */
    public boolean isExternalPasswordManagement()
    {
        return externalPasswordManagement;
    }

    public boolean isExternalUserManagement()
    {
        return externalUserManagement;
    }

    public boolean isPublicMode()
    {
        return isPublicMode;
    }

    public boolean isElevatedSecurityCheckShown()
    {
        return isElevatedSecurityCheckShown;
    }

    public boolean isCaptchaFailure()
    {
        return captchaFailure;
    }

    public boolean getLoginFailedByPermissions()
    {
        return loginFailedByPermissions;
    }

    public String getContactAdminLink()
    {
        return contactAdminLink;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private boolean allowCookies;
        private boolean externalPasswordManagement;
        private boolean externalUserManagement;
        private boolean isPublicMode;
        private boolean isElevatedSecurityCheckShown;
        private boolean loginSucceeded;
        private boolean captchaFailure;
        private boolean loginFailedByPermissions;
        private boolean loginError;
        private boolean communicationError;
        private String contactAdminLink;

        public Builder allowCookies(final boolean allowCookies)
        {
            this.allowCookies = allowCookies;
            return this;
        }

        public Builder externalPasswordManagement(final boolean externalPasswordManagement)
        {
            this.externalPasswordManagement = externalPasswordManagement;
            return this;
        }

        public Builder externalUserManagement(final boolean externalUserManagement)
        {
            this.externalUserManagement = externalUserManagement;
            return this;
        }

        public Builder isPublicMode(final boolean isPublicMode)
        {
            this.isPublicMode = isPublicMode;
            return this;
        }

        public Builder isElevatedSecurityCheckShown(final boolean isElevatedSecurityCheckShown)
        {
            this.isElevatedSecurityCheckShown = isElevatedSecurityCheckShown;
            return this;
        }

        public Builder loginSucceeded(final boolean loginSucceeded)
        {
            this.loginSucceeded = loginSucceeded;
            return this;
        }

        public Builder captchaFailure(final boolean captchaFailure)
        {
            this.captchaFailure = captchaFailure;
            return this;
        }

        public Builder loginFailedByPermissions(final boolean loginFailedByPermissions)
        {
            this.loginFailedByPermissions = loginFailedByPermissions;
            return this;
        }

        public Builder loginError(boolean loginError)
        {
            this.loginError = loginError;
            return this;
        }

        public Builder communicationError(boolean communicationError)
        {
            this.communicationError = communicationError;
            return this;
        }

        public Builder contactAdminLink(String contactAdminLink)
        {
            this.contactAdminLink = contactAdminLink;
            return this;
        }

        public LoginProperties build()
        {
            return new LoginProperties(loginSucceeded, loginError, communicationError, allowCookies,
                    externalPasswordManagement, externalUserManagement,
                    isPublicMode, isElevatedSecurityCheckShown, captchaFailure, loginFailedByPermissions,
                    contactAdminLink);
        }
    }
}
