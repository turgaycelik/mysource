/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.core.util.FileUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.jelly.JiraDynaBeanTagSupport;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.util.JellyTagUtils;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.web.util.AttachmentException;
import com.google.common.collect.ImmutableList;
import com.opensymphony.util.TextUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class AttachFile extends JiraDynaBeanTagSupport
{
    private static final Logger log = Logger.getLogger(AttachFile.class);
    public static final String OPTION_SKIP = "skip"; // If an attachment with the same filename already exists do NOT import the new one
    public static final String OPTION_OVERRIDE = "override"; // If an attachment with the same filename already exists replace it with the new one
    public static final String OPTION_ADD = "add"; // If an attachment with the same filename already extits add the new one (i.e. no check
    private static final List<String> availableOptions = ImmutableList.of(OPTION_SKIP, OPTION_OVERRIDE, OPTION_ADD);

    // for existing attachments is done
    private static final String KEY_ISSUE_KEY = "key";
    private static final String KEY_FILEPATH = "filepath";
    private static final String KEY_OPTION = "option";
    private static final String KEY_CREATED_DATE = "created";

    private final AttachmentManager attachmentManager = ComponentAccessor.getAttachmentManager();
    private static final FastDateFormat TMP_FOLDER_FORMATTER = FastDateFormat.getInstance("yyyyMMddhhmmssSSS");

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        String issueKey = getKey();
        String filepath = getFilepath();
        String option = getOption();
        String created = getCreated();
        Timestamp createdDate;
        // parse the created date if set
        if (TextUtils.stringSet(created))
        {
            createdDate = JellyTagUtils.parseDate(created);
        }
        else
        {
            createdDate = null;
        }

        String username = (String) getContext().getVariable(JellyTagConstants.USERNAME);
        User user = null;

        if (!TextUtils.stringSet(issueKey))
            throw new MissingAttributeException(KEY_ISSUE_KEY);

        if (!TextUtils.stringSet(filepath))
            throw new MissingAttributeException(KEY_FILEPATH);

        File originalFile = new File(getFilepath());

        final JiraProperties jiraSystemProperties = JiraSystemProperties.getInstance();
        String FS = jiraSystemProperties.getProperty("file.separator");
        String TMP_DIR = jiraSystemProperties.getProperty("java.io.tmpdir");
        String TMP_DIR_NAME = TMP_FOLDER_FORMATTER.format(new Date());
        File tmpFile = new File(TMP_DIR + FS + TMP_DIR_NAME + FS + originalFile.getName());

        // As the file to be attached is moved in the createAttachment method - a copy of the original file is created.
        // The original file remains unchanged and all operations are performed on the copy in a "tmp" dir - this dir
        // is deleted when all operations are completed.
        try
        {
            FileUtils.copyFile(originalFile, tmpFile);
        }
        catch (IOException e)
        {
            log.warn("Unable to make temporary copy of file.", e);
            throw new JellyTagException("Unable to make temporary copy of file.", e);
        }

        if (username != null)
        {
            user = UserUtils.getUser(username);
            if (user == null)
            {
                throw new JellyTagException("The user '" + username + "' cannot be found.");
            }
        }
        // If option not specified - add the attachment
        if (!TextUtils.stringSet(option))
            option = OPTION_ADD;

        try
        {
            MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);

            if (issue != null)
            {
                if (OPTION_ADD.equals(option))
                {
                    attachFile(tmpFile, user, issue, createdDate);
                }
                else if (OPTION_OVERRIDE.equals(option))
                {
                    Long attachmentId = getAttachmentId(issue, tmpFile.getName());
                    if (attachmentId != null)
                    {
                        try
                        {
                            log.debug("Found existing attachment with filename '" + tmpFile.getName() + "'. Removing it to import the new attachment.");

                            // Remove the attachment
                            attachmentManager.deleteAttachment(attachmentManager.getAttachment(attachmentId));
                        }
                        catch (Exception e)
                        {
                            throw new JellyTagException("Error occured while removing attachment.", e);
                        }
                    }
                    attachFile(tmpFile, user, issue, createdDate);
                }
                else if (OPTION_SKIP.equals(option))
                {
                    // Check if the attachments is there
                    if (getAttachmentId(issue, tmpFile.getName()) != null)
                    {
                        // Attachment with the same filename exists, do not attach the new one
                        log.debug("Found existing attachment with filename '" + tmpFile.getName() + "'. Will not import the new attachment.");
                    }
                    else
                    {
                        attachFile(tmpFile, user, issue, createdDate);
                    }
                }
                else
                {
                    throw new JellyTagException("Invalid value for the 'option' attribute. Must be one of the following: " + availableOptions);
                }

                // Finally - remove the tmp dir
                if (tmpFile.getParentFile().isDirectory() && tmpFile.getParentFile().getName().equals(TMP_DIR_NAME))
                    tmpFile.getParentFile().delete();
            }
            else
            {
                log.warn("Unable to find an issue with key '" + issueKey + "' not adding the attachment.");
                throw new JellyTagException("Unable to find an issue with key '" + issueKey + "' not adding the attachment.");
            }
        }
        catch (GenericEntityException e)
        {
            log.error(e, e);
        }
    }

    private void attachFile(File file, User user, Issue issue, Timestamp createdDate) throws GenericEntityException, JellyTagException
    {
        try
        {
            if (createdDate == null)
            {
                attachmentManager.createAttachment(file, file.getName(), "application/octet-stream", user, issue);
            }
            else
            {
                attachmentManager.createAttachment(file, file.getName(), "application/octet-stream", user, issue, Collections.EMPTY_MAP, createdDate);
            }
        }
        catch (AttachmentException e)
        {
            throw new JellyTagException("Error occured while attaching file.", e);
        }
    }

    public String[] getRequiredProperties()
    {
        return new String[]{KEY_ISSUE_KEY, KEY_FILEPATH};
    }

    private Long getAttachmentId(Issue issue, String compareFilename)
    {
        // Check if the attachment with the same filename exists
        List<Attachment> attachments = attachmentManager.getAttachments(issue);

        // Look for the attachments of the same name
        for (Attachment attachment : attachments)
        {
            String filename = attachment.getFilename();
            if (filename.equals(compareFilename))
            {
                return attachment.getId();
            }
        }

        return null;
    }

    public String getKey()
    {
        return (String) getProperties().get(KEY_ISSUE_KEY);
    }

    public String getFilepath()
    {
        return (String) getProperties().get(KEY_FILEPATH);
    }

    public String getOption()
    {
        return (String) getProperties().get(KEY_OPTION);
    }

    public String getCreated()
    {
        return (String) getProperties().get(KEY_CREATED_DATE);
    }

    public File getTempDirName(String name)
    {
        final JiraProperties jiraSystemProperties = JiraSystemProperties.getInstance();
        String FS = jiraSystemProperties.getProperty("file.separator");
        String TMP_DIR = jiraSystemProperties.getProperty("java.io.tmpdir");
        String TMP_DIR_NAME = TMP_FOLDER_FORMATTER.format(new Date());
        return new File(TMP_DIR + FS + TMP_DIR_NAME + FS + name);
    }
}
