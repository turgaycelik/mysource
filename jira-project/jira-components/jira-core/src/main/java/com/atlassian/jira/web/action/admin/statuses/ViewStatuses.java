package com.atlassian.jira.web.action.admin.statuses;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.bc.config.StatusService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.status.SimpleStatus;
import com.atlassian.jira.issue.status.SimpleStatusImpl;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.ActionViewDataMappings;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class ViewStatuses extends AbstractViewConstants
{
    @VisibleForTesting
    static final String NEW_STATUS_DEFAULT_ICON = "/images/icons/statuses/generic.png";

    public static final String STATUS_ENTITY_NAME = "Status";
    private static final String CATEGORY_HELP_UTIL_KEY = "statuses";
    private final StatusService statusService;
    private final ConstantsService constantsService;
    private final WorkflowManager workflowManager;

    private Long statusCategory;
    private String id;

    @Deprecated
    /**
     * @deprecated This method is only here for backwards compatibility with JIM - use
     * {@link #ViewStatuses(TranslationManager, StatusManager, WorkflowManager, StatusCategoryManager)}
     */
    public ViewStatuses(final TranslationManager translationManager, final StatusManager statusManager,
                        final WorkflowManager workflowManager)
    {
        super(translationManager);
        this.workflowManager = workflowManager;
        setIconurl(NEW_STATUS_DEFAULT_ICON);
        this.statusService = ComponentAccessor.getComponent(StatusService.class);
        this.constantsService = ComponentAccessor.getComponent(ConstantsService.class);
    }

    /**
     * Default constructor for depedency injection
     * @param translationManager
     * @param statusService
     * @param constantsService
     * @param i18nHelper dummy parameter to make this constructor the longest
     */
    public ViewStatuses(final TranslationManager translationManager, final StatusService statusService,
            final ConstantsService constantsService, final I18nHelper i18nHelper,
            final WorkflowManager workflowManager)
    {
        super(translationManager);
        this.statusService = statusService;
        this.constantsService = constantsService;
        this.workflowManager = workflowManager;
        setIconurl(NEW_STATUS_DEFAULT_ICON);
    }

    protected String getConstantEntityName()
    {
        return STATUS_ENTITY_NAME;
    }

    protected String getNiceConstantName()
    {
        return "status";
    }

    protected String getIssueConstantField()
    {
        return getText("admin.issue.constant.status.lowercase");
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

    protected void clearCaches()
    {
        getConstantsManager().refreshStatuses();
    }

    public Long getStatusCategory()
    {
        return statusCategory;
    }

    public void setStatusCategory(final Long statusCategory)
    {
        this.statusCategory = statusCategory;
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    @RequiresXsrfCheck
    public String doAddStatus() throws Exception
    {
        ServiceResult validationResult = statusService.validateCreateStatus(getLoggedInApplicationUser(), name, description, getIconurl(), getStatusCategoryObject());
        addErrorCollection(validationResult.getErrorCollection());
        if(hasAnyErrors()){
            return ERROR;
        }
        return super.doAddConstant();
    }

    @ActionViewData(key = "status")
    public SimpleStatus getStatusFormValues()
    {
        return new SimpleStatusImpl(null, getName(), getDescription(), getStatusCategoryObject(), getIconurl());
    }

    @ActionViewData
    public Collection<SimpleStatus> getStatuses()
    {
        List<JiraWorkflow> workflows = workflowManager.getWorkflowsIncludingDrafts();
        return Collections2.transform(getConstantsManager().getStatusObjects(), new StatusMappingFunction(workflows));
    }

    @ActionViewData(key = "isTranslatable")
    public boolean isTranslatable()
    {
        return super.isTranslatable();
    }

    @ActionViewData(key = "isIconUrlFieldVisible")
    public boolean isIconUrlFieldVisible()
    {
        return !getStatusLozengeEnabled();
    }

    @ActionViewDataMappings ({"success","error"})
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

    @ActionViewData(key = "isNew")
    public boolean isNew()
    {
        return true;
    }

    protected String redirectToView()
    {
        return returnCompleteWithInlineRedirect("ViewStatuses.jspa");
    }

    protected String getDefaultPropertyName()
    {
        return APKeys.JIRA_CONSTANT_DEFAULT_STATUS;
    }

    @Override
    protected GenericValue addConstant() throws GenericEntityException
    {
        final StatusCategory statusCategoryObject = getStatusCategoryObject();
        final ServiceOutcome<Status> status = statusService.createStatus(getLoggedInApplicationUser(), name, description, getIconurl(), statusCategoryObject);
        return status.getReturnedValue().getGenericValue();
    }

    private StatusCategory getStatusCategoryObject()
    {
        final StatusCategory statusCategoryObject;
        if(getStatusLozengeEnabled())
        {
            statusCategoryObject = constantsService.getStatusCategoryById(getLoggedInUser(), String.valueOf(statusCategory)).getReturnedValue();
        }
        else
        {
            statusCategoryObject = constantsService.getDefaultStatusCategory(getLoggedInUser()).getReturnedValue();
        }
        return statusCategoryObject;
    }

    public boolean getStatusLozengeEnabled()
    {
        return constantsService.isStatusAsLozengeEnabled();
    }

    @ActionViewData
    public Collection<Map<String,Object>> getStatusCategoryOptions()
    {
        Collection<Map<String,Object>> options = Collections.emptyList();
        if (getStatusLozengeEnabled())
        {
            Collection<StatusCategory> sc = constantsService.getUserVisibleStatusCategories(getLoggedInUser()).getReturnedValue();
            options = Collections2.transform(sc, new Function<StatusCategory, Map<String, Object>>()
            {
                @Override
                public Map<String, Object> apply(@Nullable final StatusCategory input)
                {
                    MapBuilder<String,Object> builder = MapBuilder.<String,Object>newBuilder();
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
            });
        }
        return options;
    }

    public String doMoveUp()
    {
        statusService.moveStatusUp(getLoggedInApplicationUser(), up);
        return redirectToView();
    }

    public String doMoveDown()
    {
        statusService.moveStatusDown(getLoggedInApplicationUser(), down);
        return redirectToView();
    }

    public class SimpleStatusMap extends SimpleStatusImpl implements SimpleStatus
    {
        private final Collection<String> workflows;
        private final boolean isActive;

        public SimpleStatusMap(final Status status, final Set<JiraWorkflow> workflows)
        {
            super(status);
            this.workflows = ImmutableSet.copyOf(Iterables.transform(workflows, GET_WORKFLOW_NAME));
            this.isActive = (null != workflows) && workflows.size() > 0;
        }

        public Collection<String> getWorkflows()
        {
            return workflows;
        }

        public boolean getIsActive()
        {
            return isActive;
        }
    }

    private static final Function<JiraWorkflow, String> GET_WORKFLOW_NAME = new Function<JiraWorkflow, String>()
    {
        @Override
        public String apply(@Nullable final JiraWorkflow input)
        {
            return input.getName();
        }
    };

    private class StatusMappingFunction implements Function<Status, SimpleStatus>
    {
        private final Map<JiraWorkflow, Set<String>> workflowToStatusIds;

        private StatusMappingFunction(final List<JiraWorkflow> allWorkflows)
        {
            Map<JiraWorkflow, Set<String>> workflowToStatusIds = new HashMap<JiraWorkflow, Set<String>>(allWorkflows.size());
            for (JiraWorkflow workflow : allWorkflows)
            {
                workflowToStatusIds.put(workflow, workflow.getLinkedStatusIds());
            }
            this.workflowToStatusIds = ImmutableMap.copyOf(workflowToStatusIds);
        }

        @Override
        public SimpleStatus apply(final Status status)
        {
            return new SimpleStatusMap(status, getWorkflows(status));
        }

        public Set<JiraWorkflow> getWorkflows(final Status status)
        {
            return Maps.filterEntries(workflowToStatusIds, containsStatus(status)).keySet();
        }
    }

    public static Predicate<Map.Entry<JiraWorkflow, Set<String>>> containsStatus(final Status status)
    {
        return new Predicate<Map.Entry<JiraWorkflow, Set<String>>>()
        {
            @Override
            public boolean apply(final Map.Entry<JiraWorkflow, Set<String>> input)
            {
                return input.getValue().contains(status.getId());
            }
        };
    }

}
