/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.servlet.JiraCaptchaService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.octo.captcha.service.CaptchaServiceException;
import webwork.action.ActionContext;

import javax.servlet.http.HttpSession;

public class Signup extends JiraWebActionSupport
{
    private String fullname;
    private String username;
    private String email;
    private String password;
    private String confirm;
    private String captcha;
    private final ApplicationProperties applicationProperties;
    private final UserService userService;
    private final UserUtil userUtil;
    private final ExternalLinkUtil externalLinkUtil;
    private final JiraCaptchaService jiraCaptchaService;

    private UserService.CreateUserValidationResult result;
    private List<WebErrorMessage> passwordErrors;


    public Signup(final ApplicationProperties applicationProperties, final UserService userService, final UserUtil userUtil,
            final JiraCaptchaService jiraCaptchaService, ExternalLinkUtil externalLinkUtil)
    {
        this.applicationProperties = applicationProperties;
        this.userService = userService;
        this.userUtil = userUtil;
        this.jiraCaptchaService = jiraCaptchaService;
        this.externalLinkUtil = externalLinkUtil;
    }

    public String doDefault() throws Exception
    {
        if (!JiraUtils.isPublicMode())
        {
            return "modebreach";
        }

        if (getLoggedInUser() != null)
        {
            return "alreadyloggedin";
        }

        if (!userUtil.canActivateNumberOfUsers(1))
        {
            return "limitexceeded";
        }

        return super.doDefault();
    }

    protected void doValidation()
    {
        if (!JiraUtils.isPublicMode())
        {
            return;
        }

        if (getLoggedInUser() != null)
        {
            return;
        }

        if (!userUtil.canActivateNumberOfUsers(1))
        {
            return;
        }

        validateCaptcha();

        result = userService.validateCreateUserForSignup(
                        getLoggedInUser(), getUsername(), getPassword(), getConfirm(), getEmail(), getFullname());

        if (!result.isValid())
        {
            addErrorCollection(result.getErrorCollection());
        }
        passwordErrors = result.getPasswordErrors();
    }

    protected String doExecute() throws Exception
    {
        if (!JiraUtils.isPublicMode())
        {
            return "modebreach";
        }
        
        if (getLoggedInUser() != null)
        {
            return "alreadyloggedin";
        }

        if (!userUtil.canActivateNumberOfUsers(1))
        {
            return "limitexceeded";
        }

        try
        {
            User user = userService.createUserFromSignup(result);
            if (user == null)
            {
                addErrorMessage(getText("signup.error.duplicateuser"));
            }
        }
        catch (final CreateException e)
        {
            log.error("Error creating user from public sign up", e);
            return "systemerror";
        }

        return getResult();
    }

    public ExternalLinkUtil getExternalLinkUtils()
    {
        return externalLinkUtil;
    }

    private void validateCaptcha()
    {
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_CAPTCHA_ON_SIGNUP))
        {
            return;
        }
        //remember that we need an id to validate!
        HttpSession session = ActionContext.getRequest().getSession(false);
        if (session == null)
        {
            addErrorMessage(getText("session.timeout.message.title"));
            return;
        }
        String captchaId = session.getId();

        Boolean isResponseCorrect = null;
        try
        {
            isResponseCorrect = jiraCaptchaService.getImageCaptchaService().validateResponseForID(captchaId, captcha);
        }
        catch (CaptchaServiceException e)
        {
            addErrorMessage(getText("session.timeout.message.title"));
        }
        if (isResponseCorrect != null && !isResponseCorrect)
        {
            addError("captcha", getText("signup.error.captcha.incorrect"));
        }
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getUsername()
    {
        if (username != null)
        {
            return username.trim();
        }
        return null;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public List<WebErrorMessage> getPasswordErrors()
    {
        return passwordErrors;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public void setCaptcha(String captcha)
    {
        this.captcha = captcha;
    }
}
