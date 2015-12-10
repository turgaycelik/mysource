package com.atlassian.jira.imports.project.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.util.MessageSet;

/**
 * A kitchen-sink-like object that passes back the project import data and any messages that validation may want to communicate.
 *
 * @since v3.13
 */
public class MappingResult implements Serializable
{
    private static final long serialVersionUID = -1301693049478752957L;

    private MessageSet issueTypeMessageSet;
    private MessageSet customFieldMessageSet;
    private MessageSet priorityMessageSet;
    private MessageSet resolutionMessageSet;
    private MessageSet statusMessageSet;
    private MessageSet projectRoleMessageSet;
    private MessageSet groupMessageSet;
    private MessageSet issueLinkTypeMessageSet;
    private MessageSet fileAttachmentMessageSet;
    private MessageSet userMessageSet;
    private MessageSet projectRoleActorMessageSet;
    private MessageSet issueSecurityLevelMessageSet;
    private Map<String, MessageSet> customFieldValueMessageSets;
    private List<ValidationMessage> systemFieldsMessageList;
    private List<ValidationMessage> customFieldsMessageList;

    public MappingResult()
    {
        customFieldValueMessageSets = new HashMap<String, MessageSet>();
    }

    /**
     * Returns <code>true</code> if all the validation has passed.
     * @return <code>true</code> if all the validation has passed.
     */
    public boolean canImport()
    {
        return messageListIsValid(systemFieldsMessageList) && messageListIsValid(customFieldsMessageList);
    }

    public boolean hasAnyCustomFieldValueErrors()
    {
        for (final MessageSet messageSet : customFieldValueMessageSets.values())
        {
            if (messageSet.hasAnyErrors())
            {
                return true;
            }
        }
        return false;
    }

    public MessageSet getIssueTypeMessageSet()
    {
        return issueTypeMessageSet;
    }

    public void setIssueTypeMessageSet(final MessageSet issueTypeMessageSet)
    {
        this.issueTypeMessageSet = issueTypeMessageSet;
    }

    public MessageSet getCustomFieldMessageSet()
    {
        return customFieldMessageSet;
    }

    public void setCustomFieldMessageSet(final MessageSet customFieldMessageSet)
    {
        this.customFieldMessageSet = customFieldMessageSet;
    }

    public MessageSet getPriorityMessageSet()
    {
        return priorityMessageSet;
    }

    public void setPriorityMessageSet(final MessageSet priorityMessageSet)
    {
        this.priorityMessageSet = priorityMessageSet;
    }

    public MessageSet getResolutionMessageSet()
    {
        return resolutionMessageSet;
    }

    public void setResolutionMessageSet(final MessageSet resolutionMessageSet)
    {
        this.resolutionMessageSet = resolutionMessageSet;
    }

    public MessageSet getStatusMessageSet()
    {
        return statusMessageSet;
    }

    public void setStatusMessageSet(final MessageSet statusMessageSet)
    {
        this.statusMessageSet = statusMessageSet;
    }

    public MessageSet getProjectRoleMessageSet()
    {
        return projectRoleMessageSet;
    }

    public MessageSet getGroupMessageSet()
    {
        return groupMessageSet;
    }

    public void setGroupMessageSet(final MessageSet groupMessageSet)
    {
        this.groupMessageSet = groupMessageSet;
    }

    public MessageSet getIssueLinkTypeMessageSet()
    {
        return issueLinkTypeMessageSet;
    }

    public void setIssueLinkTypeMessageSet(final MessageSet issueLinkTypeMessageSet)
    {
        this.issueLinkTypeMessageSet = issueLinkTypeMessageSet;
    }

    public void setProjectRoleMessageSet(final MessageSet projectRoleMessageSet)
    {
        this.projectRoleMessageSet = projectRoleMessageSet;
    }

    public void setProjectRoleActorMessageSet(final MessageSet projectRoleActorMessageSet)
    {
        this.projectRoleActorMessageSet = projectRoleActorMessageSet;
    }

    public MessageSet getProjectRoleActorMessageSet()
    {
        return projectRoleActorMessageSet;
    }

    public MessageSet getFileAttachmentMessageSet()
    {
        return fileAttachmentMessageSet;
    }

    public void setFileAttachmentMessageSet(final MessageSet fileAttachmentMessageSet)
    {
        this.fileAttachmentMessageSet = fileAttachmentMessageSet;
    }

    public MessageSet getUserMessageSet()
    {
        return userMessageSet;
    }

    public void setUserMessageSet(final MessageSet userMessageSet)
    {
        this.userMessageSet = userMessageSet;
    }

    public MessageSet getIssueSecurityLevelMessageSet()
    {
        return issueSecurityLevelMessageSet;
    }

    public void setIssueSecurityLevelMessageSet(final MessageSet issueSecurityLevelMessageSet)
    {
        this.issueSecurityLevelMessageSet = issueSecurityLevelMessageSet;
    }

    /**
     * Returns a Map keyed on the CustomField, with a MessageSet for each Custom Field containing any
     * @return a Map keyed on the CustomField, with a MessageSet for each Custom Field containing any
     */
    public Map<String, MessageSet> getCustomFieldValueMessageSets()
    {
        return customFieldValueMessageSets;
    }

    public void setCustomFieldValueMessageSets(final Map<String, MessageSet> customFieldValueMessageSets)
    {
        this.customFieldValueMessageSets = customFieldValueMessageSets;
    }

    public List<ValidationMessage> getSystemFieldsMessageList()
    {
        return systemFieldsMessageList;
    }

    public void setSystemFieldsMessageList(final List<ValidationMessage> messageList)
    {
        systemFieldsMessageList = messageList;
    }

    public List<ValidationMessage> getCustomFieldsMessageList()
    {
        return customFieldsMessageList;
    }

    public void setCustomFieldsMessageList(final List<ValidationMessage> customFieldsMessageList)
    {
        this.customFieldsMessageList = customFieldsMessageList;
    }

    public static class ValidationMessage implements Serializable
    {
        private final String displayName;
        private final MessageSet messageSet;

        public ValidationMessage(final String displayName, final MessageSet messageSet)
        {
            this.displayName = displayName;
            this.messageSet = messageSet;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public boolean isValidated()
        {
            return messageSet != null;
        }

        public MessageSet getMessageSet()
        {
            return messageSet;
        }
    }

    private boolean messageListIsValid(final List<ValidationMessage> messageList)
    {
        if (messageList == null)
        {
            return false;
        }
        for (final ValidationMessage validationMessage : messageList)
        {
            if (!validationMessage.isValidated() || validationMessage.getMessageSet().hasAnyErrors())
            {
                return false;
            }
        }
        return true;
    }

}
