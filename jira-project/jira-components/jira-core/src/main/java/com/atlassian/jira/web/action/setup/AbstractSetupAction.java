/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import com.opensymphony.util.TextUtils;

public abstract class AbstractSetupAction extends JiraWebActionSupport
{
    protected static final String SETUP_ALREADY = "setupalready";
    private static final String SETUP_INSTANT_KEY = "instant-setup";
    private static final String SETUP_SESSION_ID_KEY = "setup-session-id";

    public static final String DEFAULT_GROUP_ADMINS = "jira-administrators";
    public static final String DEFAULT_GROUP_DEVELOPERS = "jira-developers";
    public static final String DEFAULT_GROUP_USERS = "jira-users";

    protected final FileFactory fileFactory;

    public AbstractSetupAction(FileFactory fileFactory)
    {
        this.fileFactory = fileFactory;
    }

    public boolean setupAlready()
    {
        return (getApplicationProperties().getString(APKeys.JIRA_SETUP) != null);
    }

    protected void validateFormPathParam(final String formElement, final String blankErrorMessage, final String nonUniqueErrorMessage, final String myPath, final Collection<String> otherPaths)
    {
        if (!TextUtils.stringSet(myPath))
        {
            addError(formElement, getText(blankErrorMessage));
        }
        else
        {
            for (String otherPath : otherPaths)
            {
                if (myPath.equals(otherPath))
                {
                    addError(formElement, getText(nonUniqueErrorMessage));
                }
            }
            validateSetupPath(myPath, formElement);
        }
    }

    protected void validateSetupPath(final String paramPath, final String formElement)
    {
        File attachmentDir = fileFactory.getFile(paramPath);

        if (!attachmentDir.isAbsolute())
        {
            addError(formElement, getText("setup.error.filepath.notabsolute"));
        }
        else if (!attachmentDir.exists())
        {
            try
            {
                if (!attachmentDir.mkdirs())
                {
                    addError(formElement, getText("setup.error.filepath.notexist"));
                }
            }
            catch (Exception e)
            {
                addError(formElement, getText("setup.error.filepath.notexist"));
            }
        }
        else if (!attachmentDir.isDirectory())
        {
            addError(formElement, getText("setup.error.filepath.notdir"));
        }
        else if (!attachmentDir.canWrite())
        {
            addError(formElement, getText("setup.error.filepath.notwriteable"));
        }
    }

    /**
     * @return boolean The user decision or false if not chosen yet
     */
    public boolean isInstantSetup()
    {
        Boolean on = (Boolean) getHttpSession().getAttribute(SETUP_INSTANT_KEY);

        return Boolean.TRUE == on;
    }

    /**
     * @return boolean Whether user made the decision already
     */
    public boolean isInstantSetupAlreadyChosen()
    {
        return getHttpSession().getAttribute(SETUP_INSTANT_KEY) != null;
    }

    public void setInstantSetup(boolean on)
    {
        getHttpSession().setAttribute(SETUP_INSTANT_KEY, on);
    }

    public String getSetupSessionId()
    {
        String sessionId = (String) getHttpSession().getAttribute(SETUP_SESSION_ID_KEY);

        if (sessionId == null)
        {
            sessionId = UUID.randomUUID().toString();
            getHttpSession().setAttribute(SETUP_SESSION_ID_KEY, sessionId);
        }

        return sessionId;
    }
}
