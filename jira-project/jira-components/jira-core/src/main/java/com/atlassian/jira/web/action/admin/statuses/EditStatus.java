/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.statuses;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.bc.config.StatusService;
import com.atlassian.jira.issue.status.SimpleStatus;
import com.atlassian.jira.issue.status.SimpleStatusImpl;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.ActionViewDataMappings;
import com.atlassian.jira.web.action.admin.constants.AbstractEditConstant;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.ofbiz.core.entity.GenericValue;

import static com.google.common.collect.Iterables.transform;

@WebSudoRequired
public class EditStatus extends AbstractEditConstant
{
    private Long statusCategory;

    private final StatusService statusService;
    private final ConstantsService constantsService;

    private static final String PLACEHOLDER_VALUE = "-1";
    private static final String CATEGORY_HELP_UTIL_KEY = "statuses";

    public EditStatus(StatusService statusService, final ConstantsService constantsService)
    {
        this.statusService = statusService;
        this.constantsService = constantsService;
    }

    protected String getConstantEntityName()
    {
        return "Status";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.status.lowercase");
    }

    protected String getIssueConstantField()
    {
        return "status";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getStatus(id);
    }

    protected String getRedirectPage()
    {
        return "ViewStatuses.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getStatuses();
    }

    @Override
    public String getIconurl()
    {
        if (null == iconurl || iconurl.isEmpty())
        {
            iconurl = getStatus().getIconUrl();
        }

        return iconurl;
    }

    @Override
    protected void doValidation()
    {
        ServiceResult validation = statusService.validateEditStatus(getLoggedInApplicationUser(), getStatus(), name, description, getIconurl(), getStatusCategoryObject());
        if(validation.isValid()){
            super.doValidation();
        } else {
            addErrorCollection(validation.getErrorCollection());
        }

        // check if placeholder for status category has been chosen
        if (statusCategory != null && PLACEHOLDER_VALUE.equals(String.valueOf(statusCategory)))
        {
            addError("statusCategory", getText("admin.errors.must.specify.status.category"));
        }
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshStatuses();
    }

    public Collection<StatusCategory> getStatusCategories()
    {
        return constantsService.getAllStatusCategories(getLoggedInUser()).getReturnedValue();
    }

    @RequiresXsrfCheck
    @Override
    protected String doExecute() throws Exception
    {
        ServiceOutcome<Status> outcome = statusService.editStatus(getLoggedInApplicationUser(), getStatus(), name, description, getIconurl(), getStatusCategoryObject());
        addErrorCollection(outcome.getErrorCollection());
        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return returnCompleteWithInlineRedirect(getRedirectPage());
        }
    }

    private StatusCategory getStatusCategoryObject()
    {
        StatusCategory category;

        if (getStatusLozengeEnabled())
        {
            category = constantsService.getStatusCategoryById(getLoggedInUser(), String.valueOf(statusCategory)).getReturnedValue();
        }
        else
        {
            category = null;
        }

        return category;
    }

    private Status getStatus()
    {
        return statusService.getStatusById(getLoggedInApplicationUser(), id);
    }

    @ActionViewData(key = "status")
    public SimpleStatus getStatusFormValues()
    {
        return new SimpleStatusImpl(getId(), getName(), getDescription(), getStatusCategoryObject(), getIconurl());
    }

    @ActionViewData(key = "isIconUrlFieldVisible")
    public boolean isIconUrlFieldVisible()
    {
        return !getStatusLozengeEnabled();
    }

    @ActionViewDataMappings ({"input","error"})
    public Map<String,Object> outputErrorsForSoy()
    {
        return MapBuilder.<String,Object>newBuilder()
                .add("errors", super.getErrors())
                .toMap();
    }

    @ActionViewData
    public String getToken()
    {
        return super.getXsrfToken();
    }

    @ActionViewData
    public HelpUtil.HelpPath getStatusCategoryHelpData()
    {
        return HelpUtil.getInstance().getHelpPath(CATEGORY_HELP_UTIL_KEY);
    }

    public Long getStatusCategory()
    {
        if(statusCategory == null)
        {
            Status s = getStatus();
            if(s != null && s.getStatusCategory() != null)
                statusCategory = getStatus().getStatusCategory().getId();
        }
        return statusCategory;
    }

    @ActionViewData
    public List<Map<String,Object>> getStatusCategoryOptions()
    {
        List<Map<String,Object>> options = Collections.emptyList();
        if (getStatusLozengeEnabled())
        {
            final StatusCategory undefinedCategory = constantsService.getDefaultStatusCategory(getLoggedInUser()).getReturnedValue();
            final boolean isCurrentUndefined = getStatus().getStatusCategory() != null ? getStatus().getStatusCategory().equals(undefinedCategory) : true;
            final Collection<StatusCategory> sc = constantsService.getUserVisibleStatusCategories(getLoggedInUser()).getReturnedValue();

            options = Lists.newArrayList(transform(sc, new Function<StatusCategory, Map<String, Object>>()
            {
                @Override
                public Map<String, Object> apply(@Nullable final StatusCategory input)
                {
                    MapBuilder<String,Object> builder = MapBuilder.<String, Object>newBuilder();
                    if (null != input)
                    {
                        builder.add("text", input.getTranslatedName(getI18nHelper()));
                        builder.add("value", input.getId());
                        builder.add("key", input.getKey());
                        builder.add("colorName", input.getColorName());
                        builder.add("sequence", input.getSequence());
                        builder.add("selected", null != input.getId() && input.getId().equals(getStatusCategory()));
                    }
                    return builder.toMap();
                }
            }));

            // status being edited has null / undefined category assigned and
            // category chosen is one of null | undefined | placeholder (take into account that statusCategory field may be set by getStatusCategory())
            if (isCurrentUndefined &&
                    (statusCategory == null || undefinedCategory.getId().equals(statusCategory) || PLACEHOLDER_VALUE.equals(String.valueOf(statusCategory))))
            {
                MapBuilder<String,Object> builder = MapBuilder.<String, Object>newBuilder();
                builder.add("text", getText("admin.issuesettings.statuses.status.category.please.select"));
                builder.add("value", PLACEHOLDER_VALUE);
                builder.add("key", "");
                builder.add("colorName", "");
                builder.add("sequence", "");
                builder.add("selected", true);
                builder.add("isPlaceholder", true);

                options.add(builder.toMap());
            }
        }
        return options;
    }

    public void setStatusCategory(final Long statusCategory)
    {
        this.statusCategory = statusCategory;
    }

    public boolean getStatusLozengeEnabled()
    {
        return constantsService.isStatusAsLozengeEnabled();
    }
}
