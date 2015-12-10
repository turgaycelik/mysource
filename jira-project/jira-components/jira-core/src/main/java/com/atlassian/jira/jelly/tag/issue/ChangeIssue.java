/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.jelly.ActionTagSupport;
import com.atlassian.jira.jelly.tag.util.JellyTagUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;

public class ChangeIssue extends ActionTagSupport
{
    private static final Logger log = Logger.getLogger(com.atlassian.jira.jelly.tag.issue.ChangeIssue.class);
    private static final String KEY_ISSUE_KEY = "key";
    private static final String KEY_ISSUE_UPDATED_DATE = "updated";

    public ChangeIssue()
    {
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException
    {
        log.debug("ChangeIssue.doTag :" + toString());

        if (contextValidation(output))
        {
            // Pre-tag (data preparation + validation check)
            prePropertyValidation(output);

            boolean preTagValid = propertyValidation(output);
            if (preTagValid)
            {
                updateIssue(output);
            }
        }
    }

    protected void updateIssue(XMLOutput output) throws JellyTagException
    {
        log.debug("ChangeIssue.updateIssue");

        GenericValue issue = null;
        try
        {
            String key = getProperty(KEY_ISSUE_KEY);
            issue = ComponentAccessor.getIssueManager().getIssue(key);

            boolean shouldStore = modifyUpdateDate(issue);
            if (shouldStore)
            {
                issue.store();
                ComponentAccessor.getIssueIndexManager().reIndex(issue);
            }
        }
        catch (GenericEntityException e)
        {
            throw new JellyTagException(e);
        }
        catch (IndexException e)
        {
            log.error("Error while re-indexing issue '" + issue.getString("key") + "'. Seraching results may give incorrect results");
        }
    }

    //TODO Try to remove these dependancies on EE/Jira internals
    private boolean modifyUpdateDate(GenericValue issue) throws GenericEntityException
    {
        log.debug("ChangeIssue.modifyUpdateDate");

        String updatedDate = getProperty(KEY_ISSUE_UPDATED_DATE);
        if (updatedDate != null)
        {
            Timestamp updated = JellyTagUtils.parseDate(updatedDate);

            // Hack Directly to the Issue Entity via EE and change the creation date
            issue.set("updated", updated);
            return true;
        }
        else
        {
            log.debug("Update date not set, using todays date");
            return false;
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_ISSUE_KEY };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }

    public String[] getRequiredContextVariables()
    {
        return new String[0];
    }

    public void setProperty(String key, String value)
    {
        getProperties().put(key, value);
    }

    public void setProperty(String key, String[] value)
    {
        getProperties().put(key, value);
    }

    public String getProperty(String key)
    {
        return (String) getProperties().get(key);
    }
}
