package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.event.web.action.admin.UserAddedEvent;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.google.common.collect.ImmutableList;

/**
 * Responsible for handling the requests to add a new JIRA User.
 */
@WebSudoRequired
public class AddUser extends JiraWebActionSupport
{
    private String username;
    private String password;
    private String confirm;
    private String fullname;
    private String email;
    private Long directoryId;
    private boolean sendEmail = false;
    private UserService.CreateUserValidationResult result;
    private List<WebErrorMessage> passwordErrors = ImmutableList.of();

    private final UserService userService;
    private final UserUtil userUtil;
    private final UserManager userManager;
    private final WebInterfaceManager webInterfaceManager;
    private final EventPublisher eventPublisher;


    public AddUser(UserService userService, UserUtil userUtil, UserManager userManager, WebInterfaceManager webInterfaceManager, EventPublisher eventPublisher)
    {
        this.userService = userService;
        this.userUtil = userUtil;
        this.userManager = userManager;
        this.webInterfaceManager = webInterfaceManager;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Processes a request to render the input form to fill out the new user's details(username, password, full-name,
     * email ...)
     *
     * @return {@link #INPUT} the input form to fill out the new user's details(username, password, full-name, email
     *         ...)
     */
    @Override
    public String doDefault()
    {
        return INPUT;
    }

    protected void doValidation()
    {
        result = userService.validateCreateUserForAdmin(
                getLoggedInUser(),
                getUsername(),
                getPassword(),
                getConfirm(),
                getEmail(),
                getFullname(),
                getDirectoryId()
        );

        if (!result.isValid())
        {
            addErrorCollection(result.getErrorCollection());
        }
        passwordErrors = result.getPasswordErrors();
    }

    /**
     * Processes a request to create a user using the specified url parameters.
     *
     * @return if there are input error this will return {@link #ERROR}; otherwise, it will redirect to the View User
     *         page for the created user.
     */
    @Override
    @RequiresXsrfCheck
    protected String doExecute()
    {
        try
        {
            // send password if the user has not disabled when creating.
            if (sendEmail)
            {
                userService.createUserWithNotification(result);
            }
            else
            {
                userService.createUserNoNotification(result);
            }

            eventPublisher.publish(new UserAddedEvent(request.getParameterMap()));
        }
        catch (PermissionException e)
        {
            addError("username", getText("admin.errors.user.no.permission.to.create"));
        }
        catch (CreateException e)
        {
            addError("username", getText("admin.errors.user.cannot.create", e.getMessage()));
        }

        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return returnCompleteWithInlineRedirect("ViewUser.jspa?name=" + JiraUrlCodec.encode(username.toLowerCase()));
        }
    }

    public boolean hasReachedUserLimit()
    {
        return !userUtil.canActivateNumberOfUsers(1);
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = StringUtils.trim(username);
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = StringUtils.trim(email);
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        if (StringUtils.isEmpty(password))
        {
            this.password = null;
        }
        else
        {
            this.password = password;
        }
    }

    public List<WebErrorMessage> getPasswordErrors()
    {
        return passwordErrors;
    }

    public boolean hasPasswordWritableDirectory()
    {
        return userManager.hasPasswordWritableDirectory();
    }

    public boolean isSendEmail()
    {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail)
    {
        this.sendEmail = sendEmail;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        if (StringUtils.isEmpty(confirm))
        {
            this.confirm = null;
        }
        else
        {
            this.confirm = confirm;
        }
    }

    public Long getDirectoryId()
    {
        return directoryId;
    }

    public void setDirectoryId(Long directoryId)
    {
        this.directoryId = directoryId;
    }

    public List<Directory> getDirectories()
    {
        return userManager.getWritableDirectories();
    }

    public Map<Long, Boolean> getCanDirectoryUpdatePasswordMap()
    {
        final List<Directory> directories = getDirectories();
        final Map<Long, Boolean> result = new HashMap<Long, Boolean>(directories.size());
        for (final Directory directory : directories)
        {
            result.put(directory.getId(), userManager.canDirectoryUpdateUserPassword(directory));
        }
        return result;
    }

    public String getUserCountWebPanelHtml()
    {
        return getPanels("webpanels.admin.adduser.count");
    }

    public String getWebPanelHtml()
    {
        return getPanels("webpanels.admin.adduser");
    }

    private String getPanels(final String panelLocation)
    {
        final StringBuilder builder = new StringBuilder();
        final List<WebPanelModuleDescriptor> panels = webInterfaceManager.getDisplayableWebPanelDescriptors(panelLocation, Collections.<String, Object>emptyMap());
        for (final WebPanelModuleDescriptor panel : panels)
        {
            final Option<String> fragment = SafePluginPointAccess.call(new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    if (panel == null || panel.getModule() == null)
                    {
                        return null;
                    }
                    else
                    {
                        return panel.getModule().getHtml(Collections.<String, Object>emptyMap());
                    }

                }
            });

            if (fragment.isDefined())
            {
                builder.append(fragment.get());
            }

        }
        return builder.toString();
    }
}
