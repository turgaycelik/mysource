package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.user.UserProfileUpdatedEvent;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang.StringUtils;

public class EditProfile extends JiraWebActionSupport
{
    private static final int MAX_LENGTH = 255;

    private final CrowdService crowdService;
    private final UserManager userManager;
    private final EventPublisher eventPublisher;

    private String username;
    private String fullName;
    private String email;

    private String password;

    public EditProfile(final CrowdService crowdService, final UserManager userManager, final EventPublisher eventPublisher)
    {
        this.crowdService = crowdService;
        this.userManager = userManager;
        this.eventPublisher = eventPublisher;
    }


    public String doDefault() throws Exception
    {
        final ApplicationUser current = getLoggedInApplicationUser();

        if (current == null || !current.getName().equals(username))
        {
            return ERROR;
        }
        if (!userManager.userCanUpdateOwnDetails(current))
        {
            addErrorMessage(getText("editprofile.not.allowed"));
            return ERROR;
        }

        fullName = current.getDisplayName();
        email = current.getEmailAddress();

        return super.doDefault();
    }

    protected void doValidation()
    {
        final ApplicationUser current = getLoggedInApplicationUser();
        if (current == null)
        {
            addErrorMessage("generic.notloggedin.title");
            return;
        }

        if (StringUtils.isBlank(fullName))
        {
            addError("fullName", getText("admin.errors.invalid.full.name.specified"));
        }
        else if (fullName.length() > MAX_LENGTH)
        {
            addError("fullName", getText("signup.error.full.name.greater.than.max.chars"));
        }
        if (StringUtils.isBlank(email))
        {
            addError("email", getText("admin.errors.invalid.email"));
        }
        else if (email.length() > MAX_LENGTH)
        {
            addError("email", getText("signup.error.email.greater.than.max.chars"));
        }

        if (detailsHaveChanged(current))
        {
            if (!validatePassword(current))
            {
                addError("password", getText("user.profile.password.mismatch"));
            }
        }

    }

    private boolean validatePassword(final ApplicationUser current)
    {
        if (password == null)
        {
            return false;
        }
        try
        {
            crowdService.authenticate(current.getUsername(), password);
        }
        catch (InactiveAccountException e)
        {
            return false;
        }
        catch (FailedAuthenticationException e)
        {
            return false;
        }
        return true;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final ApplicationUser current = getLoggedInApplicationUser();

        if (current == null || !current.getUsername().equals(username))
        {
            return ERROR;
        }
        if (!userManager.userCanUpdateOwnDetails(current))
        {
            addErrorMessage(getText("editprofile.not.allowed"));
            return ERROR;
        }
        if (detailsHaveChanged(current))
        {

            UserTemplate user = new UserTemplate(current.getDirectoryUser());
            user.setDisplayName(fullName);
            user.setEmailAddress(email);


            try
            {
                crowdService.updateUser(user);
                eventPublisher.publish(new UserProfileUpdatedEvent(current, getLoggedInApplicationUser()));
            }
            catch (OperationNotPermittedException e)
            {
                addErrorMessage(getText("admin.errors.cannot.edit.user.directory.read.only"));
            }
        }

        return returnComplete("ViewProfile.jspa");

    }

    private boolean detailsHaveChanged(ApplicationUser current)
    {
        return !eq(current.getDisplayName(), fullName) || !eq(current.getEmailAddress(), email);
    }

    private boolean eq(String s, String s1)
    {
        return StringUtils.defaultString(s).equals(StringUtils.defaultString(s1));
    }

    public String getFullName()
    {
        return fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getUsername()
    {
        return username;
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

    public boolean userCanUpdateOwnDetails()
    {
        return userManager.userCanUpdateOwnDetails(getLoggedInApplicationUser());
    }
}
