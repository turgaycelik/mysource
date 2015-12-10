package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.FieldRenderingContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelUtil;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Context provider for the details block
 *
 * @since v5.0
 */
public class DetailsBlockContextProvider implements ContextProvider
{
    private final PluginAccessor pluginAccessor;
    private final JiraAuthenticationContext authenticationContext;
    private final ProjectComponentManager projectComponentManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final LabelUtil labelUtil;
    private final FieldManager fieldManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final WorkflowManager workflowManager;
    private VersionManager versionManager;

    public DetailsBlockContextProvider(PluginAccessor pluginAccessor,
            JiraAuthenticationContext authenticationContext, ProjectComponentManager projectComponentManager,
            FieldVisibilityManager fieldVisibilityManager, LabelUtil labelUtil,
            final FieldManager fieldManager, FieldScreenRendererFactory fieldScreenRendererFactory,
            final IssueManager issueManager, final PermissionManager permissionManager,
            final WorkflowManager workflowManager, final VersionManager versionManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.authenticationContext = authenticationContext;
        this.projectComponentManager = projectComponentManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.labelUtil = labelUtil;
        this.fieldManager = fieldManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.workflowManager = workflowManager;
        this.versionManager = versionManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(JiraVelocityUtils.getDefaultVelocityParams(context, authenticationContext));

        final Issue issue = (Issue) context.get("issue");
        final Action action = (Action) context.get("action");
        final User user = authenticationContext.getLoggedInUser();
        paramsBuilder.add("i18n", authenticationContext.getI18nHelper());
        paramsBuilder.add("issue", issue);
        paramsBuilder.add("summaryComponent", this);
        paramsBuilder.add("fieldVisibility", fieldVisibilityManager);
        paramsBuilder.add("issueViews", getIssueViews());
        paramsBuilder.add("canEdit", issueManager.isEditable(issue, user));
        paramsBuilder.add("hasViewWorkflowPermission", permissionManager.hasPermission(Permissions.VIEW_WORKFLOW_READONLY, issue, user));
        final JiraWorkflow workflow = workflowManager.getWorkflow(issue);
        if (workflow != null)
        {
            paramsBuilder.add("workflowName", workflow.getName());
        }
        paramsBuilder.add("workflowUrl", WorkflowUrl.get(workflow, issue));
        final Long projectId = issue.getProjectObject().getId();
        paramsBuilder.add("projectHasComponents", !projectComponentManager.findAllForProject(projectId).isEmpty());
        if (!fieldVisibilityManager.isFieldHidden("components", issue))
        {
            final Collection<ProjectComponent> components = issue.getComponentObjects();
            if (components != null && !components.isEmpty())
            {
                paramsBuilder.add("components", components);
            }
        }
        paramsBuilder.add("projectHasVersions", !versionManager.getVersions(projectId).isEmpty());
        if (!fieldVisibilityManager.isFieldHidden("versions", issue))
        {
            final Collection<Version> versions = issue.getAffectedVersions();
            if (versions != null && !versions.isEmpty())
            {
                paramsBuilder.add("versions", versions);
            }
        }
        if (!fieldVisibilityManager.isFieldHidden("fixVersions", issue))
        {
            final Collection<Version> versions = issue.getFixVersions();
            if (versions != null && !versions.isEmpty())
            {
                paramsBuilder.add("fixVersions", versions);
            }
        }
        if (!fieldVisibilityManager.isFieldHidden(IssueFieldConstants.SECURITY, issue))
        {
            final GenericValue securityLevel = issue.getSecurityLevel();
            if (securityLevel != null)
            {
                paramsBuilder.add("securitylevel", securityLevel);
            }
        }
        if (!fieldVisibilityManager.isFieldHidden(IssueFieldConstants.LABELS, issue))
        {
            final Set<Label> labels = issue.getLabels();
            paramsBuilder.add("labelUtil", labelUtil);
            paramsBuilder.add("labels", labels);
            paramsBuilder.add("remoteUser", authenticationContext.getLoggedInUser());
        }

        if (!fieldVisibilityManager.isFieldHidden(IssueFieldConstants.ENVIRONMENT, issue) && StringUtils.isNotBlank(issue.getEnvironment()))
        {
            paramsBuilder.add("renderedEnvironmentHtml", getRenderedEnvironmentFieldValue(issue, action));
            paramsBuilder.add("environment", issue.getEnvironment());
        }
        final FieldScreenRenderer fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(issue, IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isStandardViewIssueCustomField());
        final List<FieldScreenRenderTab> fieldScreenRenderTabs = fieldScreenRenderer.getFieldScreenRenderTabs();

        paramsBuilder.add("tabs", getTabs(fieldScreenRenderTabs, issue, action));

        return paramsBuilder.toMap();
    }

    private Collection<IssueViewModuleDescriptor> getIssueViews()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(IssueViewModuleDescriptor.class);
    }


    /**
     * Gets the HTML that shows the environment field. This includes divs and a javascript enabled hide/show toggle
     * button.
     *
     * @param issue what issue they want to do it to
     * @param action what they want to do
     * @return the HTML that shows the environment field.
     */
    private String getRenderedEnvironmentFieldValue(final Issue issue, final Action action)
    {
        final OrderableField environmentField = fieldManager.getOrderableField(IssueFieldConstants.ENVIRONMENT);
        final FieldScreenRenderer renderer = fieldScreenRendererFactory.getFieldScreenRenderer(issue, IssueOperations.VIEW_ISSUE_OPERATION);
        final FieldLayoutItem fieldLayoutItem = renderer.getFieldScreenRenderLayoutItem(environmentField).getFieldLayoutItem();

        // JRA-16224 Cannot call getViewHtml() on FieldScreenRenderLayoutItem, because it will return "" if Environment is not included in the Screen Layout.
        return environmentField.getViewHtml(fieldLayoutItem, action, issue);
    }

    private List<SimpleTab> getTabs(List<FieldScreenRenderTab> fieldScreenRenderTabs, Issue issue, Action action)
    {
        CollectionBuilder<SimpleTab> tabs = CollectionBuilder.newBuilder();
        for (FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderTabs)
        {
            tabs.add(new SimpleTab(fieldScreenRenderTab, issue, action));
        }
        return tabs.asList();
    }

    public static class SimpleTab
    {
        final private String name;
        final private List<SimpleField> fields;

        public SimpleTab(FieldScreenRenderTab tab, Issue issue, Action action)
        {
            name = tab.getName();
            final List<FieldScreenRenderLayoutItem> layoutItems = tab.getFieldScreenRenderLayoutItems();
            CollectionBuilder<SimpleField> fieldBuilder = CollectionBuilder.newBuilder();
            for (FieldScreenRenderLayoutItem layoutItem : layoutItems)
            {
                fieldBuilder.add(new SimpleField(layoutItem, issue, action));
            }

            fields = fieldBuilder.asList();
        }

        public String getName()
        {
            return name;
        }

        public List<SimpleField> getFields()
        {
            return fields;
        }
    }

    public static class SimpleField
    {
        private String id = null;
        private String fieldType = null;
        private String fieldTypeCompleteKey = null;
        private String name = null;
        private String styleClass = null;
        private boolean isShowField = false;
        private String fieldHtml = null;

        public SimpleField(FieldScreenRenderLayoutItem layoutItem, Issue issue, Action action)
        {
            final CustomField orderableField = (CustomField) layoutItem.getOrderableField();
            
            CustomFieldTypeModuleDescriptor descriptor = orderableField.getCustomFieldType().getDescriptor();
            Object value = orderableField.getValue(issue);
            isShowField = value != null && descriptor.isViewTemplateExists();

            if (isShowField)
            {
                id = orderableField.getId();
                name = orderableField.getName();
                styleClass = "type-" + descriptor.getKey();
                fieldType = descriptor.getKey();
                fieldTypeCompleteKey = descriptor.getCompleteKey();
                if (styleClass.equals("type-textarea") && ((String) value).length() > 255)
                {
                    styleClass += " twixified";
                }

                fieldHtml = orderableField.getViewHtml(layoutItem.getFieldLayoutItem(), action, issue, MapBuilder.build(FieldRenderingContext.ISSUE_VIEW, Boolean.TRUE));
            }
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public String getStyleClass()
        {
            return styleClass;
        }

        public boolean isShowField()
        {
            return isShowField;
        }

        public String getFieldHtml()
        {
            return fieldHtml;
        }

        public String getFieldType()
        {
            return fieldType;
        }

        public String getFieldTypeCompleteKey()
        {
            return fieldTypeCompleteKey;
        }
    }

    private static class WorkflowUrl
    {
        private static String get(JiraWorkflow workflow, Issue issue)
        {
            if (workflow != null)
            {
                UrlBuilder url = new UrlBuilder(false).
                        addPath("/").addPath("browse").addPath(issue.getKey()).
                        addParameter("workflowName", workflow.getName());

                StepDescriptor step = workflow.getLinkedStep(issue.getStatusObject());
                if (step != null)
                {
                    url.addParameter("stepId", Integer.toString(step.getId()));
                }

                return url.asUrlString();
            }
            return "";
        }
    }
}
