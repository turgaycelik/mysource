package com.atlassian.jira.web.action.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.user.UserPasswordActionHelper;
import org.apache.commons.lang.StringUtils;
import webwork.action.ResultException;

/**
 * Handles the requests to reset a password for a specific user. The link to this action will come in an email sent from
 * as a result of the execution of {@link ForgotLoginDetails#doExecute()}
 * @since v4.1
 */
public class ResetPassword extends JiraWebActionSupport
{
    private final UserUtil userUtil;
    private final PasswordPolicyManager passwordPolicyManager;

    private String token;
    private String os_username;
    private String password;
    private String confirm;
    private User userInPlay;
    private boolean tokenTimedOut;
    private boolean tokenInvalid;
    private final List<WebErrorMessage> passwordErrors = new ArrayList<WebErrorMessage>();

    public ResetPassword(final UserUtil userUtil, final PasswordPolicyManager passwordPolicyManager)
    {
        this.userUtil = userUtil;
        this.passwordPolicyManager = passwordPolicyManager;
    }

    /**
     * Handles the request to render the Reset Password form.
     * @return The name of the view to be rendered. If there are any validation errors
     * {@link webwork.action.Action#INPUT}; Otherwise, {@link webwork.action.Action#ERROR} is returned.
     */
    @Override
    public String doDefault()
    {
        validateUserAndToken();
        if (hasAnyErrors())
        {
            return ERROR;
        }
        return INPUT;
    }

    @Override
    protected void validate() throws ResultException
    {
        validateUserAndToken();
        if (!tokenInvalid && !tokenTimedOut)
        {
            validateNewPasswords();
        }
    }

    /**
     * Handles the request to set a new password for the user in play from the Reset Password form.
     * @return The name of the view to be rendered. If there are any input errors, and the token is invalid or expired
     * {@link webwork.action.Action#ERROR}; otherwise, {@link webwork.action.Action#SUCCESS} is returned.
     */
    @Override
    protected String doExecute()
    {
        if (tokenInvalid || tokenTimedOut || invalidInput())
        {
            return ERROR;
        }
        new UserPasswordActionHelper(this, userUtil).setPassword(userInPlay, password);
        if (invalidInput())
        {
            return ERROR;
        }
        return SUCCESS;
    }

    private void validateUserAndToken()
    {
        userInPlay = userUtil.getUser(os_username);
        if (userInPlay == null)
        {
            addErrorMessage(getText("resetpassword.error.unknown.user"));
        }
        else
        {
            final UserUtil.PasswordResetTokenValidation validation = userUtil.validatePasswordResetToken(userInPlay, token);
            if (validation.getStatus() == UserUtil.PasswordResetTokenValidation.Status.EXPIRED)
            {
                tokenTimedOut = true;
                addErrorMessage(getText("resetpassword.error.token.timedout"));
            }
            else if (validation.getStatus() == UserUtil.PasswordResetTokenValidation.Status.UNEQUAL)
            {
                tokenInvalid = true;
                addErrorMessage(getText("resetpassword.error.invalid.token"));
            }
        }
    }

    private void validateNewPasswords()
    {
        if (StringUtils.isBlank(password) || StringUtils.isBlank(confirm))
        {
            addErrorMessage(getText("resetpassword.error.password.blank"));
        }
        else if (!nvl(password, "").equals(confirm))
        {
            addErrorMessage(getText("resetpassword.error.password.mustmatch"));
        }
        else
        {
            final Collection<WebErrorMessage> messages = passwordPolicyManager.checkPolicy(userUtil.getUserByName(os_username), null, password);
            if (!messages.isEmpty())
            {
                addError("password", getText("changepassword.new.password.rejected"));
                for (WebErrorMessage message : messages)
                {
                    passwordErrors.add(message);
                }
            }
        }
    }

    private String nvl(final String str, final String defaultStr)
    {
        return str == null ? defaultStr : str;
    }


    public String getToken()
    {
        return token;
    }

    public void setToken(final String token)
    {
        this.token = token;
    }

    public String getOs_username()
    {
        return os_username;
    }

    public void setOs_username(final String os_username)
    {
        this.os_username = os_username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }

    public List<WebErrorMessage> getPasswordErrors()
    {
        return passwordErrors;
    }


    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(final String confirm)
    {
        this.confirm = confirm;
    }

    public boolean isTokenTimedOut()
    {
        return tokenTimedOut;
    }

    public boolean isTokenInvalid()
    {
        return tokenInvalid;
    }
}
