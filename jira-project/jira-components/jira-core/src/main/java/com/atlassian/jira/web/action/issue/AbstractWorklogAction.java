/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.opensymphony.util.TextUtils;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public abstract class AbstractWorklogAction extends AbstractIssueSelectAction
{
    private Long worklogId;
    private String timeLogged;
    private String startDate;
    private String workType;
    private String newEstimate;
    private String adjustmentAmount;
    private String commentLevel;
    private String comment;

    // how to adjust the timeestimate - allowed values are auto, new, manual & leave
    protected static final String ADJUST_ESTIMATE_AUTO = "auto";
    protected static final String ADJUST_ESTIMATE_NEW = "new";
    protected static final String ADJUST_ESTIMATE_MANUAL = "manual";
    // selected adjustment type - defaults to auto
    protected String adjustEstimate = ADJUST_ESTIMATE_AUTO;

    protected final WorklogService worklogService;
    protected final CommentService commentService;
    protected final ProjectRoleManager projectRoleManager;
    protected final JiraDurationUtils jiraDurationUtils;
    protected final OutlookDateManager outlookDateManager;
    protected final FieldVisibilityManager fieldVisibilityManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final UserUtil userUtil;
    private final FeatureManager featureManager;

    private CommentVisibility commentVisibility;

    public AbstractWorklogAction(WorklogService worklogService, CommentService commentService, ProjectRoleManager projectRoleManager,
            JiraDurationUtils jiraDurationUtils, OutlookDateManager outlookDateManager, final FieldVisibilityManager fieldVisibilityManager,
            final FieldLayoutManager fieldLayoutManager, final RendererManager rendererManager, UserUtil userUtil, final FeatureManager featureManager)
    {
        this.worklogService = worklogService;
        this.commentService = commentService;
        this.projectRoleManager = projectRoleManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.outlookDateManager = outlookDateManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.userUtil = userUtil;
        this.featureManager = featureManager;
    }

    public String getComment()
    {
        return comment;
    }

    public String getEstimate() throws Exception
    {
        final Long estimate = getIssueObject().getEstimate();
        return estimate == null ? null : jiraDurationUtils.getFormattedDuration(estimate);
    }

    public boolean isTimeTrackingFieldHidden(Issue issue)
    {
        return fieldVisibilityManager.isFieldHidden(IssueFieldConstants.TIMETRACKING, issue);
    }
    
    public String getTimeLogged()
    {
        return timeLogged;
    }

    public void setTimeLogged(String timeLogged)
    {
        this.timeLogged = timeLogged;
    }

    public String getWorkType()
    {
        return workType;
    }

    public void setWorkType(String workType)
    {
        this.workType = workType;
    }

    public String getNewEstimate()
    {
        return newEstimate;
    }

    public void setNewEstimate(String newEstimate)
    {
        this.newEstimate = newEstimate;
    }

    public String getAdjustmentAmount()
    {
        return adjustmentAmount;
    }

    public void setAdjustmentAmount(final String adjustmentAmount)
    {
        this.adjustmentAmount = adjustmentAmount;
    }

    public String getAdjustEstimate()
    {
        return adjustEstimate;
    }

    public void setAdjustEstimate(String adjustEstimate)
    {
        this.adjustEstimate = adjustEstimate;
    }

    public boolean isLevelSelected(String visibilityLevel)
    {
        return getCommentLevel() != null && getCommentLevel().equals(visibilityLevel);
    }

    public String getSelectedLevelName()
    {
        if(getCommentLevel() == null)
        {
            return getText("security.level.viewable.by.all");
        }

        final Collection<ProjectRole> roleLevels = getRoleLevels();
        for (ProjectRole roleLevel : roleLevels)
        {
            if(getCommentLevel().equals("role:" + roleLevel.getId().toString()))
            {
                return getText("security.level.restricted.to", TextUtils.htmlEncode(roleLevel.getName()));
            }
        }

        final Collection<String> groupLevels = getGroupLevels();
        for (String groupLevel : groupLevels)
        {
            if(getCommentLevel().equals("group:" + groupLevel))
            {
                return getText("security.level.restricted.to", TextUtils.htmlEncode(groupLevel));
            }
        }
        return getText("security.level.viewable.by.all");
    }

    public String getCommentLevel()
    {
        return commentLevel;
    }

    public void setCommentLevel(String commentLevel)
    {
        this.commentLevel = commentLevel;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Collection getGroupLevels()
    {
        Collection groups;
        if (getLoggedInUser() == null || !commentService.isGroupVisibilityEnabled())
        {
            groups = Collections.EMPTY_LIST;
        }
        else
        {
            groups = userUtil.getGroupNamesForUser(getLoggedInUser().getName());
        }
        return groups;
    }

    public Collection<ProjectRole> getRoleLevels()
    {
        Collection<ProjectRole> roleLevels;
        if (commentService.isProjectRoleVisibilityEnabled())
        {
            roleLevels = projectRoleManager.getProjectRoles(getLoggedInUser(), getIssueObject().getProjectObject());
        }
        else
        {
            roleLevels = Collections.emptyList();
        }
        return roleLevels;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    protected CommentVisibility getCommentVisibility()
    {
        if (commentVisibility == null)
        {
            commentVisibility = new CommentVisibility(commentLevel);
        }
        return commentVisibility;
    }

    protected Date getParsedStartDate()
    {
        try
        {
            return (getStartDate() == null) ? null : outlookDateManager.getOutlookDate(getLocale()).parseDateTimePicker(getStartDate());
        }
        catch (ParseException e)
        {
            // Its cool to let this be null the service validation will add the correct error
            return null;
        }
    }

    protected String getFormattedStartDate(Date date)
    {
        return outlookDateManager.getOutlookDate(getLocale()).formatDateTimePicker(date);
    }

    public Long getWorklogId()
    {
        return worklogId;
    }

    public void setWorklogId(Long worklogId)
    {
        this.worklogId = worklogId;
    }

    /**
     * Renders the input control for the Work Description (i.e. Comment) of the Log Work form. The control will be different
     * depending on which renderer is chosen for the Log Work field for this specific issue context.
     *
     * @return the HTML of the input control to be displayed on the form.
     */
    public String getWorkDescriptionEditHtml()
    {
        try
        {
            final FieldLayoutItem worklogFieldLayoutItem = fieldLayoutManager.getFieldLayout(getIssueObject()).getFieldLayoutItem(IssueFieldConstants.WORKLOG);
            final String rendererType = (worklogFieldLayoutItem != null) ? worklogFieldLayoutItem.getRendererType() : null;
            final JiraRendererModuleDescriptor rendererDescriptor = rendererManager.getRendererForType(rendererType).getDescriptor();
            final Map<Object,Object> rendererParams = MapBuilder.newBuilder().add("rows", "10").add("cols", "60").add("wrap", "virtual").add("class", "long-field").toMutableMap();

            return rendererDescriptor.getEditVM(getComment(), getIssueObject().getKey(), rendererType, "comment", "comment", rendererParams, false); 
        }
        catch (DataAccessException e)
        {
            log.error("Could not render edit template for work description", e);
            return "";
        }
    }

    public boolean isOnDemand()
    {
        return featureManager.isOnDemand();
    }
}
