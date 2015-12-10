/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager;
import com.atlassian.jira.issue.changehistory.metadata.renderer.HistoryMetadataRenderHelper;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.issue.worklog.TimeTrackingIssueUpdater;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.web.action.util.DiffViewRenderer;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.jira.issue.IssueFieldConstants.COMMENT;

/**
 * Provides the template with all required objects, including the issue details.
 */
public class IssueTemplateContext extends DefaultTemplateContext
{
    private static final Logger log = Logger.getLogger(IssueTemplateContext.class);

    // Text email formatting - the left pad size
    private static final Integer PADSIZE = 20;

    private final IssueEvent issueEvent;
    private final JiraDurationUtils jiraDurationUtils;
    private final HistoryMetadataManager historyMetadataManager;
    private final Issue issue;
    private final TemplateIssueFactory templateIssueFactory;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final EventTypeManager eventTypeManager;
    private final DiffViewRenderer diffViewRenderer;
    private final UserManager userManager;
    private final HistoryMetadataRenderHelper historyMetadataRenderHelper;

    public IssueTemplateContext(Locale locale, IssueEvent issueEvent, TemplateIssueFactory templateIssueFactory,
            FieldLayoutManager fieldLayoutManager, RendererManager rendererManager, UserManager userManager,
            JiraDurationUtils jiraDurationUtils, EventTypeManager eventTypeManager,
            DiffViewRenderer diffViewRenderer, WebResourceUrlProvider resourceUrlProvider,
            ApplicationProperties applicationProperties, I18nHelper.BeanFactory beanFactory,
            HistoryMetadataManager historyMetadataManager, HistoryMetadataRenderHelper historyMetadataRenderHelper)
    {
        super(locale, resourceUrlProvider, applicationProperties, beanFactory);

        this.issueEvent = issueEvent;
        this.jiraDurationUtils = jiraDurationUtils;
        this.issue = issueEvent.getIssue();
        this.templateIssueFactory = templateIssueFactory;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.eventTypeManager = eventTypeManager;
        this.userManager = userManager;
        this.diffViewRenderer = diffViewRenderer;
        this.historyMetadataManager = historyMetadataManager;
        this.historyMetadataRenderHelper = historyMetadataRenderHelper;
    }

    @Override
    public Map<String, Object> getTemplateParams()
    {
        // NOTE: if adding a parameter here please update the doc online at
        // https://developer.atlassian.com/display/JIRADEV/Velocity+Context+for+Email+Templates

        Map<String, Object> templateParams = super.getTemplateParams();

        // Pass this TemplateContext to the template - at present, used to retrieve a i18n formatted comment time worked.
        templateParams.put("context", this);

        // Pass the decorated issue object to the template
        templateParams.put("issue", templateIssueFactory.getTemplateIssue(issue));

        templateParams.put("params", issueEvent.getParams());
        templateParams.put("remoteUser", TemplateUser.getUser(issueEvent.getUser()));

        templateParams.putAll(getUtilParams());
        templateParams.putAll(getAttachmentParams());
        templateParams.putAll(getCommentParams());
        templateParams.putAll(getOriginalCommentParams());
        templateParams.putAll(getWorkLogParams());
        templateParams.putAll(getChangeLogParams());
        templateParams.putAll(getDiffParams());
        templateParams.put("security", issue.getSecurityLevel());
        templateParams.put("rendererManager", rendererManager);

        return templateParams;
    }

    private Map<String, Object> getDiffParams()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("diffutils", new DiffUtils(diffViewRenderer));
        return params;
    }

    private Map<String, Object> getOriginalCommentParams()
    {
        Map<String, Object> originalCommentParams = new HashMap<String, Object>();

        Comment originalComment = (Comment) issueEvent.getParams().get(CommentManager.EVENT_ORIGINAL_COMMENT_PARAMETER);

        if (originalComment != null)
        {
            // Try to generate a rendered value for the comment
            try
            {
                FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(COMMENT);
                String rendererType = (fieldLayoutItem == null) ? null : fieldLayoutItem.getRendererType();
                originalCommentParams.put("originalhtmlComment", rendererManager.getRenderedContent(rendererType, originalComment.getBody(), issue.getIssueRenderContext()));
            }
            catch (Exception e)
            {
                log.warn("Unable to produce rendered version of the comment for the issue " + issue.getKey() + ".", e);
                originalCommentParams.put("originalhtmlComment", JiraKeyUtils.linkBugKeys(originalComment.getBody()));
            }

            originalCommentParams.put("originalcomment", originalComment);

            final ApplicationUser author = originalComment.getAuthorApplicationUser();
            originalCommentParams.put("originalcommentauthor", author);

            if (originalComment.getGroupLevel() == null)
            {
                ProjectRole role = originalComment.getRoleLevel();
                if (role != null)
                {
                    originalCommentParams.put("originalroleVisibilityLevel", role.getName());
                }
            }
            else
            {
                originalCommentParams.put("originalgroupVisibilityLevel", originalComment.getGroupLevel());
            }
        }

        return originalCommentParams;
    }

    /**
     * Provide the template with some utility references
     *
     * @return Map  Utils required for templates.
     */
    public Map<String, Object> getUtilParams()
    {
        Map<String, Object> utilParams = new HashMap<String, Object>();

        utilParams.put("stringUtils", new StringUtils());
        // Text email formatting - used to leftPad strings in text emails.
        utilParams.put("padSize", PADSIZE);
        // Pass the "Time Spent" field id - used in changelog for worklogged
        utilParams.put("timeSpentFieldId", IssueFieldConstants.TIME_SPENT);

        return utilParams;
    }

    /**
     * Generate the attachment params for the template.
     *
     * @return Map  the attachment params for the template.
     */
    public Map<String, Object> getAttachmentParams()
    {
        Map<String, Object> attachmentParams = new HashMap<String, Object>();
        attachmentParams.put("attachments", issue.getAttachments());
        return attachmentParams;
    }

    /**
     * Generate the comment params for the template.
     *
     * @return Map  the comment params for the template.
     */
    public Map<String, Object> getCommentParams()
    {
        Map<String, Object> commentParams = new HashMap<String, Object>();

        Comment comment = issueEvent.getComment();

        if (comment != null)
        {
            // Try to generate a rendered value for the comment
            try
            {
                FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(COMMENT);
                String rendererType = (fieldLayoutItem == null) ? null : fieldLayoutItem.getRendererType();
                commentParams.put("htmlComment", rendererManager.getRenderedContent(rendererType, comment.getBody(), issue.getIssueRenderContext()));
            }
            catch (Exception e)
            {
                log.warn("Unable to produce rendered version of the comment for the issue " + issue.getKey() + ".", e);
                commentParams.put("htmlComment", JiraKeyUtils.linkBugKeys(comment.getBody()));
            }

            commentParams.put("comment", comment);
            commentParams.put("commentauthor", comment.getAuthorApplicationUser());

            if (comment.getGroupLevel() == null)
            {
                ProjectRole role = comment.getRoleLevel();
                if (role != null)
                {
                    commentParams.put("roleVisibilityLevel", role.getName());
                }
            }
            else
            {
                commentParams.put("groupVisibilityLevel", comment.getGroupLevel());
            }
        }

        return commentParams;
    }

    /**
     * Generate WorkLog params for the template
     *
     * @return the WorkLog params for the template.
     */
    private Map<String, Object> getWorkLogParams()
    {
        Map<String, Object> workLogParams = new HashMap<String, Object>();

        Worklog worklog = issueEvent.getWorklog();

        if (worklog != null)
        {
            workLogParams.put("worklog", worklog);

            // JRADEV-22203 - Comment in issueworklogged.vm is not rendered using wiki markup
            String worklogComment = worklog.getComment();
            if (worklogComment != null)
            {
                String worklogCommentHtml = rendererManager.getRenderedContent(AtlassianWikiRenderer.RENDERER_TYPE, worklogComment, issue.getIssueRenderContext());
                workLogParams.put("worklogCommentHtml", worklogCommentHtml);
            }

            if (worklog.getGroupLevel() == null)
            {
                ProjectRole role = worklog.getRoleLevel();
                if (role != null)
                {
                    workLogParams.put("roleVisibilityLevel", role.getName());
                }
            }
            else
            {
                workLogParams.put("groupVisibilityLevel", worklog.getGroupLevel());
            }

            Worklog originalWorklog = (Worklog) issueEvent.getParams().get(TimeTrackingIssueUpdater.EVENT_ORIGINAL_WORKLOG_PARAMETER);
            if (originalWorklog != null)
            {
                if (originalWorklog.getGroupLevel() == null)
                {
                    ProjectRole role = originalWorklog.getRoleLevel();
                    if (role != null)
                    {
                        workLogParams.put("originalroleVisibilityLevel", role.getName());
                    }
                }
                else
                {
                    workLogParams.put("originalgroupVisibilityLevel", originalWorklog.getGroupLevel());
                }

                // Setup flags that indicate which fields in the worklog have changed
                if(!worklog.getTimeSpent().equals(originalWorklog.getTimeSpent()))
                {
                    workLogParams.put("timeSpentUpdated", Boolean.TRUE);
                }
                if(!worklog.getStartDate().equals(originalWorklog.getStartDate()))
                {
                    workLogParams.put("startDateUpdated", Boolean.TRUE);
                }
                if(!StringUtils.equals(originalWorklog.getComment(), worklog.getComment()))
                {
                    workLogParams.put("commentUpdated", Boolean.TRUE);
                }
                if(worklogVisibilityUpdated(originalWorklog, worklog))
                {
                    workLogParams.put("visibilityUpdated", Boolean.TRUE);
                }

                workLogParams.put(TimeTrackingIssueUpdater.EVENT_ORIGINAL_WORKLOG_PARAMETER, originalWorklog);
            }
        }

        return workLogParams;
    }

    private boolean worklogVisibilityUpdated(Worklog originalWorklog, Worklog worklog)
    {
        String originalVisibility =
                CommentVisibility.getCommentLevelFromLevels(originalWorklog.getGroupLevel(), originalWorklog.getRoleLevelId());
        String newVisibility =
                CommentVisibility.getCommentLevelFromLevels(worklog.getGroupLevel(), worklog.getRoleLevelId());

        return !StringUtils.equals(originalVisibility, newVisibility);
    }

    /**
     * This is retrieved when the template is being generated so as it is properly formatted for the recipient.
     *
     * @return String      formatted comment time logged string
     */
    public String getCommentTimeLogged(I18nHelper i18n)
    {
        Worklog worklog = issueEvent.getWorklog();
        return getTimeLogged(worklog, i18n);
    }

    /**
     * This is retrieved when the template is being generated so as it is properly formatted for the recipient.
     *
     * @return String      formatted comment time logged string
     */
    public String getTimeLogged(Worklog worklog, I18nHelper i18n)
    {
        if (worklog != null && worklog.getTimeSpent() != null)
        {
            return jiraDurationUtils.getFormattedDuration(worklog.getTimeSpent(), i18n.getLocale());
        }
        else
        {
            return null;
        }
    }

    /**
     * This is retrieved when the template is being generated so as it is properly formatted for the recipient.
     *
     * @param i18n i18n bean
     * @return String formatted event type name
     */
    public String getEventTypeName(I18nHelper i18n) {
        final EventType eventType = eventTypeManager.getEventType(issueEvent.getEventTypeId());
        final String emailEventKey = "email." + eventType.getNameKey();
        final String i18nValue = i18n.getText(emailEventKey);

        // Check if a translation exists - otherwise just add the event name
        if (emailEventKey.equals(i18nValue))
        {
            return eventType.getName();
        }
        else
        {
            return i18nValue;
        }
    }

    /**
     * Generate the changelog params for the template.
     *
     * @return Map  the changelog params for the template.
     */
    public Map<String, Object> getChangeLogParams()
    {
        final Map<String, Object> commentParams = new HashMap<String, Object>();
        final GenericValue changelog = issueEvent.getChangeLog();

        if (changelog != null)
        {
            commentParams.put("changelog", changelog);
            commentParams.put("changelogauthor", userManager.getUserByKeyEvenWhenUnknown(changelog.getString("author")));
            commentParams.put("changelogmetadata", historyMetadataManager.getHistoryMetadata(changelog.getLong("id")).getHistoryMetadata());
            commentParams.put("metadataHelper", historyMetadataRenderHelper);
        }

        return commentParams;
    }
}
