/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SummarySystemField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.web.util.AttachmentException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;

import static com.atlassian.jira.issue.IssueFieldConstants.SUMMARY;

public class CloneIssueDetails extends CreateIssueDetails
{
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final IssueLinkManager issueLinkManager;
    private final RemoteIssueLinkManager remoteIssueLinkManager;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final SubTaskManager subTaskManager;
    private final AttachmentManager attachmentManager;
    private final FieldManager fieldManager;
    private final IssueFactory issueFactory;

    private IssueLinkType cloneIssueLinkType;
    private String cloneIssueLinkTypeName;

    private MutableIssue issueObject;

    // The original issue that is to be cloned.
    private Issue originalIssue;

    // The clone parent of the clone subtasks.
    private GenericValue cloneParent;
    // Whether or not to clone issue links as well
    private boolean cloneLinks;
    // Whether or not to clone issue's sub-tasks
    private boolean cloneSubTasks;
    // Whether or not to clone issue's attachments
    private boolean cloneAttachments;
    // Map of old IssueId -> new IssueId for cloned issues
    private final Map<Long, Long> newIssueIdMap = new HashMap<Long, Long>();

    private static final String BROWSE_ISSUE_PAGE_PREFIX = "/browse/";

    public CloneIssueDetails(ApplicationProperties applicationProperties, PermissionManager permissionManager,
                             IssueLinkManager issueLinkManager, RemoteIssueLinkManager remoteIssueLinkManager, IssueLinkTypeManager issueLinkTypeManager, SubTaskManager subTaskManager,
                             AttachmentManager attachmentManager, FieldManager fieldManager, IssueCreationHelperBean issueCreationHelperBean,
                             IssueFactory issueFactory, IssueService issueService, final TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator)
    {
        super(issueFactory, issueCreationHelperBean, issueService, temporaryAttachmentsMonitorLocator);
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.issueLinkManager = issueLinkManager;
        this.remoteIssueLinkManager = remoteIssueLinkManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.subTaskManager = subTaskManager;
        this.attachmentManager = attachmentManager;
        this.fieldManager = fieldManager;
        this.issueFactory = issueFactory;
    }

    public String doDefault() throws Exception
    {
        this.cloneSubTasks = true;
        this.cloneLinks = false;
        this.cloneAttachments = false;

        try
        {
            setOriginalIssue(getIssueObject(getIssue()));

            copySummaryFieldFromOriginalIssueToHolder();
        }
        catch (IssueNotFoundException e)
        {
            // Error is added above
            return ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ERROR;
        }

        // Summary can be modified - require futher input
        return INPUT;
    }

    private void copySummaryFieldFromOriginalIssueToHolder() throws GenericEntityException
    {
        String summary = getOriginalIssue().getSummary();
        if (StringUtils.isNotBlank(summary))
        {
            //JRADEV-1972 CLONE - the space is ignored when reading from a properties file
            String clonePrefixProperties = applicationProperties.getDefaultBackedString(APKeys.JIRA_CLONE_PREFIX);
            String cloneSummary = StringUtils.isBlank(clonePrefixProperties) ? summary : StringUtils.join(new Object[]{clonePrefixProperties, summary}, " ");
            getFieldValuesHolder().put(SUMMARY, cloneSummary);
        }
    }

    public FieldScreenRenderLayoutItem getFieldScreenRenderLayoutItem(String fieldId)
    {
        return getFieldScreenRenderer().getFieldScreenRenderLayoutItem(fieldManager.getOrderableField(fieldId));
    }

    public Issue getIssueObject(GenericValue genericValue)
    {
        return issueFactory.getIssue(genericValue);
    }

    public MutableIssue getIssueObject()
    {
        if (issueObject == null)
        {
            issueObject = issueFactory.cloneIssue(getOriginalIssue());
        }

        return issueObject;
    }

    protected void doValidation()
    {
        try
        {
            //calling getIssue() here may cause exceptions
            setOriginalIssue(getIssueObject(getIssue()));
        }
        catch (IssuePermissionException ipe)
        {
            return;
        }
        catch (IssueNotFoundException infe)
        {
            return;
        }

        // Initialise issue
        setPid(getOriginalIssue().getProject().getLong("id"));
        getIssueObject().setProject(getProject());
        setIssuetype(getOriginalIssue().getIssueTypeObject().getId());
        getIssueObject().setIssueType(getIssueTypeGV());

        // Validate summary
        SummarySystemField summaryField = (SummarySystemField) fieldManager.getOrderableField(IssueFieldConstants.SUMMARY);
        summaryField.populateFromParams(getFieldValuesHolder(), ActionContext.getContext().getParameters());
        summaryField.validateParams(this, this, this, getIssueObject(), getFieldScreenRenderLayoutItem(IssueFieldConstants.SUMMARY));
    }

    /**
     * This method is responsible for preparing details for cloning issue. Preparation consists of two parts such as
     * initializing some fields and actually cloning some fields from original issue.
     */
    private void prepareForCloningIssue()
    {

        setNotCloningFieldsToBlankByDefault();
        cloneSomeFieldsFromOriginalIssue();
    }

    private void setNotCloningFieldsToBlankByDefault()
    {
        getIssueObject().setCreated(null);
        getIssueObject().setUpdated(null);
        getIssueObject().setVotes(null);
        getIssueObject().setWatches(0L);
        getIssueObject().setStatus(null);
        getIssueObject().setWorkflowId(null);
        // Ensure that the 'time spent' and 'remaining estimated' are not cloned - JRA-7165
        // We need to copy the value of 'original estimate' to the value 'remaining estimate' as they must be kept in sync
        // until work is logged on an issue.
        getIssueObject().setEstimate(getOriginalIssue().getOriginalEstimate());
        getIssueObject().setTimeSpent(null);
        //JRA-18731: Cloning a resolved issue will result in an open issue.  The resolution date should be reset.
        getIssueObject().setResolutionDate(null);

        // If the user does not have permission to modify the reporter, initialise the reporter to be the remote user
        if (!isCanModifyReporter())
        {
            getIssueObject().setReporter(getLoggedInUser());
        }
    }

    /**
     * Please be informed that there ain't a 100% copy from original issue, but with modification.
     */
    private void cloneSomeFieldsFromOriginalIssue()
    {
        getIssueObject().setSummary((String) getFieldValuesHolder().get(SUMMARY));

        getIssueObject().setFixVersions(withoutArchivedVersions(getOriginalIssue().getFixVersions()));
        getIssueObject().setAffectedVersions(withoutArchivedVersions(getOriginalIssue().getAffectedVersions()));


        // Retrieve custom fields for the issue type and project of the clone issue (same as original issue)
        List<CustomField> customFields = getCustomFields(getOriginalIssue());

        for (final CustomField customField : customFields)
        {
            // Set the custom field value of the clone to the value set in the original issue
            Object value = customField.getValue(getOriginalIssue());
            if (value != null)
            {
                getIssueObject().setCustomFieldValue(customField, value);
            }
        }
    }

    private Collection withoutArchivedVersions(Collection<Version> versions)
    {
        List<Version> notArchivedVersions = new ArrayList<Version>();
        for (final Version version : versions)
        {
            if (!version.isArchived())
            {
                notArchivedVersions.add(version);
            }
        }
        return notArchivedVersions;
    }

    @RequiresXsrfCheck
    protected String doExecute()
    {
        try
        {
            prepareForCloningIssue();

            // Create the clone issue (without attachments)
            super.createIssue();
            if (super.hasAnyErrors())
            {
                return ERROR;
            }

            cloneIssue();

            return doPostCreationTasks();
        }
        catch (Exception e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.exception")+" " + e);
            return ERROR;
        }
    }

    void cloneIssue() throws Exception
    {
        Issue cloneIssue = getIssueObject(super.getIssue());

        linkCloningIssueToOriginalOne(cloneIssue);

        cloneAttachmentsIfNeeded(cloneIssue);
        // JRA-17222 - We want to know all the issues being cloned, so we can choose to create links to the new
        // version of cloned issues.
        Set<Long> originalIssueIdSet = idsOfOriginalIssueIncludingSubTaskIfNeeded();
        cloneLinksIfNeeded(cloneIssue, originalIssueIdSet);

        if (isCloningSubTask())
        {
            linkCloningSubTaskToOriginalSubTaskParent();

        }
        else
        {
            setCloneParent(getIssue());

            cloneSubTasksIfNeeded(originalIssueIdSet);
        }
    }

    /**
     * Returns the set of original issues that are being cloned.
     * This will obviously always include the given "original issue", and may also include the subtasks of this issue.
     *
     * @return Set of ID's of the issue being cloned, and its subtasks if they are being cloned as well.
     */
    private Set<Long> idsOfOriginalIssueIncludingSubTaskIfNeeded()
    {
        Set<Long> originalIssues = new HashSet<Long>();
        originalIssues.add(originalIssue.getId());
        // Add subtasks if required
        if (subTaskManager.isSubTasksEnabled() && isCloneSubTasks())
        {
            for (final Issue subTask : originalIssue.getSubTaskObjects())
            {
                originalIssues.add(subTask.getId());
            }
        }
        return originalIssues;
    }

    public boolean isDisplayCopyLink()
    {
        if (issueLinkManager.isLinkingEnabled())
        {
            // See if there are any links to clone
            if (givenIssueHasAnyCopyableLink(getOriginalIssue()))
            {
                return true;
            }
            else
            {
                // See if there are any links to copy on sub-tasks
                if (originalIssueHasSubTask())
                {
                    for (Issue subTask : getOriginalIssue().getSubTaskObjects())
                    {
                        if (givenIssueHasAnyCopyableLink(subTask))
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private void cloneLinksIfNeeded(Issue clone, Set<Long> originalIssueIdSet) throws CreateException
    {
        if (isCloneLinks() && issueLinkManager.isLinkingEnabled())
        {
            Collection<IssueLink> inwardLinks = issueLinkManager.getInwardLinks(originalIssue.getId());
            cloneInwardLinks(clone, originalIssueIdSet, inwardLinks);

            Collection<IssueLink> outwardLinks = issueLinkManager.getOutwardLinks(originalIssue.getId());
            cloneOutwardLinks(clone, originalIssueIdSet, outwardLinks);

            cloneRemoteIssueLinks(clone);
        }
    }

    private void cloneAttachmentsIfNeeded(Issue clone) throws CreateException
    {
        /*
         * Note, that Create Attachment permission is not checked,
         * the same way Link Issue permission is not checked for cloning links.
         */
        if (isCloneAttachments() && attachmentManager.attachmentsEnabled())
        {
            final List<Attachment> attachments = attachmentManager.getAttachments(originalIssue);
            final String remoteUserName = nullSafeLoggedUsername(super.getLoggedInApplicationUser());
            for (Attachment attachment : attachments)
            {
                File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
                if (attachmentFile.exists() && attachmentFile.canRead())
                {
                    try
                    {
                        attachmentManager.createAttachmentCopySourceFile(attachmentFile, attachment.getFilename(), attachment.getMimetype(), remoteUserName, clone, Collections.EMPTY_MAP, new Timestamp(System.currentTimeMillis()));
                    }
                    catch (AttachmentException e)
                    {
                        log.warn("Could not clone attachment with id '" + attachment.getId() + "' and file path '" + attachmentFile.getAbsolutePath() + "' for issue with id '" + clone.getId() + "' and key '" + clone.getKey() + "'.", e);
                    }
                }
                else
                {
                    log.warn("Could not clone attachment with id '" + attachment.getId() + "' and file path '" + attachmentFile.getAbsolutePath() + "' for issue with id '" + clone.getId() + "' and key '" + clone.getKey() + "', " +
                             "because the file path " + (attachmentFile.exists() ? "is not readable." : "does not exist."));
                }
            }
        }
    }

    public boolean isDisplayCopyAttachments()
    {
        if (attachmentManager.attachmentsEnabled())
        {
            if (givenIssueHasAnyAttachment(originalIssue))
            {
                // If an issue has attachments then we should allow to clone them
                return true;
            }
            else if (subTaskManager.isSubTasksEnabled())
            {
                // Otherwise need to check if at least one sub-task has an attachment
                if (originalIssueHasSubTask())
                {
                    for (Issue subTask : originalIssue.getSubTaskObjects())
                    {
                        if (givenIssueHasAnyAttachment(subTask))
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;

    }

    public boolean isDisplayCopySubTasks()
    {
        return subTaskManager.isSubTasksEnabled() && originalIssueHasSubTask();
    }

    private boolean givenIssueHasAnyCopyableLink(Issue issue)
    {
        Collection<IssueLink> inwardLinks = issueLinkManager.getInwardLinks(issue.getId());
        if (hasAnyCopyableLinkInGivenLinks(inwardLinks))
        {
            return true;
        }

        Collection<IssueLink> outwardLinks = issueLinkManager.getOutwardLinks(issue.getId());
        if (hasAnyCopyableLinkInGivenLinks(outwardLinks))
        {
            return true;
        }

        return false;
    }

    private boolean originalIssueHasSubTask()
    {
        return CollectionUtils.isNotEmpty(getOriginalIssue().getSubTaskObjects());
    }

    private boolean isCopyableLink(IssueLink checkingLink)
    {
        // Do not copy system links types and do not copy the cloners link type, as it is used to record the relationship between cloned issues
        // So if the cloners link type does not exists, or the link is not of cloners link type, and is not a system link, then copy it
        return !checkingLink.isSystemLink() &&
               (getCloneIssueLinkType() == null || givenLinkTypeIsNotSameAsCloneIssueLinkType(checkingLink));
    }

    public boolean isCloneLinks()
    {
        return cloneLinks;
    }

    public void setCloneLinks(boolean cloneLinks)
    {
        this.cloneLinks = cloneLinks;
    }

    public boolean isCloneSubTasks()
    {
        return cloneSubTasks;
    }

    public void setCloneSubTasks(boolean cloneSubTasks)
    {
        this.cloneSubTasks = cloneSubTasks;
    }

    public boolean isCloneAttachments()
    {
        return cloneAttachments;
    }

    public void setCloneAttachments(final boolean cloneAttachments)
    {
        this.cloneAttachments = cloneAttachments;
    }

    protected String doPostCreationTasks() throws Exception
    {
        if (getCloneParent() != null)
            // If an issue has been cloned - return view to the newly created issue clone.
            return inlineRedirectToIssueWithKey(getCloneParent().getString("key"));
        else
            // If a subtask has been cloned or an issue with no sub tasks - return view to the newly created clone.
            return inlineRedirectToIssueWithKey(getIssue().getString("key"));
    }

    // Clone sub-tasks if subtasks are enabled and exist for the original issue
    private void cloneSubTasksIfNeeded(Set<Long> originalIssueIdSet) throws Exception
    {
        if (subTaskManager.isSubTasksEnabled() && isCloneSubTasks())
        {
            // Iterate over all subtask links, retrieve subtasks and copy details for clone subtask
            for (Issue subTaskIssue : originalIssue.getSubTaskObjects())
            {

                setOriginalIssue(subTaskIssue);
                // Reset the issue object so we can populate the new subtask with appropriate values
                issueObject = null;
                // This needs to be here to trick the super action into making the subtask the current issue to create
                validationResult = null;

                copySummaryFieldFromOriginalIssueToHolder();

                prepareForCloningIssue();
                // JRA-15949. Set the parent id to the NEW parent. Otherwise we get the wrong parentId in the IssueEvent.
                getIssueObject().setParentId(cloneParent.getLong("id"));

                // Create the new issue
                super.createIssue();
                if (!super.hasAnyErrors())
                {
                    final Issue newSubTask = getIssueObject(getIssue());
                    // Record the mapping from old ID to new ID
                    newIssueIdMap.put(getOriginalIssue().getId(), newSubTask.getId());

                    cloneLinksIfNeeded(newSubTask, originalIssueIdSet);
                    // Link the clone subtask to the clone parent issue.
                    subTaskManager.createSubTaskIssueLink(cloneParent, getIssue(), getLoggedInUser());

                    cloneAttachmentsIfNeeded(newSubTask);
                }
                else
                {
                    for (Object message : super.getErrorMessages())
                    {
                        log.warn("Could not create subtask for issue: " + cloneParent.get("key") + ", validation error has occured: " + message);
                    }
                }
            }
        }
    }

    // ------ Getters & Setters & Helper Methods -----------------

    public Issue getOriginalIssue()
    {
        return originalIssue;
    }

    public void setOriginalIssue(Issue originalIssue)
    {
        this.originalIssue = originalIssue;
    }

    public GenericValue getCloneParent()
    {
        return cloneParent;
    }

    public void setCloneParent(GenericValue cloneParent)
    {
        this.cloneParent = cloneParent;
    }

    // Retrieve the issue link type specified by the clone link name in the properties file.
    // If the name is unset - issue linking of originals to clones is not required - returns null.
    // Otherwise, returns null if the issue link type with the specified name cannot be found in the system.
    public IssueLinkType getCloneIssueLinkType()
    {
        if (cloneIssueLinkType == null)
        {
            final Collection<IssueLinkType> cloneIssueLinkTypes = issueLinkTypeManager.getIssueLinkTypesByName(getCloneLinkTypeName());

            if (StringUtils.isBlank(getCloneLinkTypeName()))
            {
                // Issue linking is not required
                cloneIssueLinkType = null;
            }
            else if (CollectionUtils.isEmpty(cloneIssueLinkTypes))
            {
                log.warn("The clone link type '" + getCloneLinkTypeName() + "' does not exist. A link to the original issue will not be created.");
                cloneIssueLinkType = null;
            }
            else
            {
                for (final IssueLinkType issueLinkType : cloneIssueLinkTypes)
                {
                    if (issueLinkType.getName().equals(getCloneLinkTypeName()))
                    {
                        cloneIssueLinkType = issueLinkType;
                    }
                }
            }
        }

        return cloneIssueLinkType;
    }

    // Determines whether a warning should be displayed.
    // If the link type name is unset in the properties file - issue linking of originals to clones is not required - do not display warning.
    public boolean isDisplayCloneLinkWarning()
    {
        return (StringUtils.isNotBlank(getCloneLinkTypeName()) && getCloneIssueLinkType() == null);
    }

    // "Modify Reporter" permission required to create the clone with the original reporter set.
    public boolean isCanModifyReporter()
    {
        return permissionManager.hasPermission(Permissions.MODIFY_REPORTER, getIssueObject(), getLoggedInUser());
    }

    public String getCloneLinkTypeName()
    {
        if (cloneIssueLinkTypeName == null)
            cloneIssueLinkTypeName = applicationProperties.getDefaultBackedString(APKeys.JIRA_CLONE_LINKTYPE_NAME);

        return cloneIssueLinkTypeName;
    }

    @Override
    public GenericValue getProject()
    {
        return getProjectManager().getProject(getIssue());
    }

    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>();
        displayParams.put("theme", "aui");
        return displayParams;
    }

    private void linkCloningIssueToOriginalOne(final Issue cloneIssue) throws CreateException
    {
        // Record the mapping from old ID to new ID
        newIssueIdMap.put(getOriginalIssue().getId(), cloneIssue.getId());

        // Create link between the cloned issue and the original - sequence on links does not matter.
        final IssueLinkType cloneIssueLinkType = getCloneIssueLinkType();
        if (cloneIssueLinkType != null)
        {
            issueLinkManager.createIssueLink(getIssue().getLong("id"), getOriginalIssue().getId(), cloneIssueLinkType.getId(), null, getLoggedInUser());
        }
    }

    private boolean isCloningSubTask()
    {
        return originalIssue.isSubTask();
    }

    private void linkCloningSubTaskToOriginalSubTaskParent() throws CreateException
    {
        // Retrieve the parent of the original subtask
        Issue subTaskParent = originalIssue.getParentObject();

        // Link the clone subtask to the parent of the original subtask (by this stage the getIssue() method returns the newly cloned issue)
        subTaskManager.createSubTaskIssueLink(subTaskParent.getGenericValue(), getIssue(), getLoggedInUser());
    }

    private void cloneInwardLinks(Issue cloneIssue, Set<Long> originalIssueIdSet, Collection<IssueLink> givenLinks) throws CreateException
    {
        cloningGivenIssueLinks(cloneIssue, originalIssueIdSet, givenLinks, true);
    }

    private void cloneOutwardLinks(Issue cloneIssue, Set<Long> originalIssueIdSet, Collection<IssueLink> givenLinks) throws CreateException
    {
        cloningGivenIssueLinks(cloneIssue, originalIssueIdSet, givenLinks, false);
    }

    private void cloningGivenIssueLinks(Issue cloneIssue, Set<Long> originalIssueIdSet, Collection<IssueLink> givenLinks, boolean isCopyingInwardLinks) throws CreateException
    {
        for (final IssueLink issueLink : givenLinks)
        {
            if (isCopyableLink(issueLink))
            {
                // JRA-17222. Check if this link is from another Issue in the "clone set"
                Long workingIssueId = isCopyingInwardLinks ? issueLink.getSourceId() : issueLink.getDestinationId();

                if (originalIssueIdSet.contains(workingIssueId))
                {
                    // We want to create a link to the new cloned version of that issue, not the original
                    // This can return null if that issue is not cloned yet, but that is OK, we will create the link as an outward link after we clone the second one.
                    workingIssueId = newIssueIdMap.get(workingIssueId);
                }
                if (workingIssueId != null)
                {
                    if (isCopyingInwardLinks)
                    {
                        log.debug("Creating inward link to " + cloneIssue.getKey() + " (cloned from " + originalIssue.getKey() + ", link " + issueLink + ")");
                        issueLinkManager.createIssueLink(workingIssueId, cloneIssue.getId(), issueLink.getIssueLinkType().getId(), null, getLoggedInUser());
                    }
                    else
                    {
                        log.debug("Creating outward link from " + cloneIssue.getKey() + " (cloned from " + originalIssue.getKey() + ", link " + issueLink + ")");
                        issueLinkManager.createIssueLink(cloneIssue.getId(), workingIssueId, issueLink.getIssueLinkType().getId(), null, getLoggedInUser());
                    }
                }
            }
        }
    }

    private void cloneRemoteIssueLinks(Issue cloneIssue) throws CreateException
    {
        final List<RemoteIssueLink> originalLinks = remoteIssueLinkManager.getRemoteIssueLinksForIssue(originalIssue);
        for (final RemoteIssueLink originalLink : originalLinks)
        {
            final RemoteIssueLink link = new RemoteIssueLinkBuilder(originalLink).id(null).issueId(cloneIssue.getId()).build();
            remoteIssueLinkManager.createRemoteIssueLink(link, getLoggedInUser());
        }
    }

    private boolean hasAnyCopyableLinkInGivenLinks(Collection<IssueLink> givenLinks)
    {
        for (IssueLink checkingIssueLink : givenLinks)
        {
            if (isCopyableLink(checkingIssueLink))
            {
                return true;
            }
        }

        return false;
    }

    private boolean givenLinkTypeIsNotSameAsCloneIssueLinkType(final IssueLink checkingLink)
    {
        return getCloneIssueLinkType().getId().equals(checkingLink.getIssueLinkType().getId()) == false;
    }

    private boolean givenIssueHasAnyAttachment(Issue givenIssue)
    {
        return CollectionUtils.isNotEmpty(attachmentManager.getAttachments(givenIssue));
    }

    private String inlineRedirectToIssueWithKey(String issueKey)
    {
        return super.returnCompleteWithInlineRedirect(BROWSE_ISSUE_PAGE_PREFIX + issueKey);
    }

    private static String nullSafeLoggedUsername(ApplicationUser loggedUser)
    {
        return loggedUser == null ? null : loggedUser.getUsername();
    }

}
