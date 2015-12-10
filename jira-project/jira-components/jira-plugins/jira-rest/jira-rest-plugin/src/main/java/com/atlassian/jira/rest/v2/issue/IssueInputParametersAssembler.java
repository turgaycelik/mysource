package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.FieldOperationHolder;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Assembles an IssueInputParametersExt from an IssueFields.
 *
 * @since v5.0
 */
public class IssueInputParametersAssembler
{
    public interface Result {
        ErrorCollection getErrors();
        IssueInputParameters getParameters();
        String getParentIdorKey();
    }

    private final IssueService issueService;
    private final FieldManager fieldManager;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final VersionBeanFactory versionBeanFactory;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ContextUriInfo contextUriInfo;
    private final JiraBaseUrls baseUrls;
    private final ProjectBeanFactory projectBeanFactory;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final WorkflowManager workflowManager;
    private final IssueFactory issueFactory;
    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public IssueInputParametersAssembler(IssueService issueService, FieldManager fieldManager, ProjectManager projectManager, ConstantsManager constantsManager,
            FieldLayoutManager fieldLayoutManager, JiraAuthenticationContext jiraAuthenticationContext, PermissionManager permissionManager,
            VersionBeanFactory versionBeanFactory, VelocityRequestContextFactory velocityRequestContextFactory, ContextUriInfo contextUriInfo,
            JiraBaseUrls baseUrls, ProjectBeanFactory projectBeanFactory, FieldScreenRendererFactory fieldScreenRendererFactory, WorkflowManager workflowManager,
            IssueFactory issueFactory, IssueSecurityLevelManager issueSecurityLevelManager)
    {
        this.issueService = issueService;
        this.fieldManager = fieldManager;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.versionBeanFactory = versionBeanFactory;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.contextUriInfo = contextUriInfo;
        this.baseUrls = baseUrls;
        this.projectBeanFactory = projectBeanFactory;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.workflowManager = workflowManager;
        this.issueFactory = issueFactory;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    Result makeUpdateAssembler(IssueUpdateBean request, Issue issue)
    {
        IssueInputParametersBuilder assembler = newAssembler();
        assembler.buildForEdit(request, issue);
        return assembler;
    }

    Result makeTransitionAssember(IssueUpdateBean updateBean, Issue issue)
    {
       IssueInputParametersBuilder assembler = newAssembler();
        assembler.buildForTransition(updateBean, issue);
        return assembler;
    }

    Result makeCreateAssembler(IssueUpdateBean request)
    {
        IssueInputParametersBuilder assembler = newAssembler();
        assembler.buildForCreate(request);
        return assembler;
    }

    List<Result> makeCreateAssemblers(IssuesUpdateBean requests)
    {
        final List<Result> results = Lists.newArrayListWithCapacity(requests.getIssueUpdates().size());
        for (IssueUpdateBean request : requests.getIssueUpdates())
        {
           results.add(makeCreateAssembler(request));
        }
        return results;
    }

    private IssueInputParametersBuilder newAssembler()
    {
        return new IssueInputParametersBuilder(issueService, fieldManager, projectManager, constantsManager,fieldLayoutManager,
                jiraAuthenticationContext, versionBeanFactory, velocityRequestContextFactory, contextUriInfo, baseUrls,
                permissionManager, projectBeanFactory, fieldScreenRendererFactory, workflowManager, issueFactory, issueSecurityLevelManager);
    }

    /**
     * Assembles an IssueInputParametersExt from an IssueFields.
     *
     * @since v5.0
     */
    private static class IssueInputParametersBuilder implements Result
    {
        private final FieldManager fieldManager;
        private final ProjectManager projectManager;
        private final ConstantsManager constantsManager;
        private final FieldLayoutManager fieldLayoutManager;
        private final JiraAuthenticationContext jiraAuthenticationContext;
        private final VersionBeanFactory versionBeanFactory;
        private final VelocityRequestContextFactory velocityRequestContextFactory;
        private final ContextUriInfo contextUriInfo;
        private final JiraBaseUrls baseUrls;
        private final PermissionManager permissionManager;
        private final ProjectBeanFactory projectBeanFactory;
        private final FieldScreenRendererFactory fieldScreenRendererFactory;
        private final WorkflowManager workflowManager;
        private final IssueFactory issueFactory;
        private final IssueSecurityLevelManager issueSecurityLevelManager;

        private final ErrorCollection errors;
        private final IssueInputParameters parameters;
        private final Map<String, List<FieldOperation>> updates = new LinkedHashMap<String, List<FieldOperation>>();
        private String parentIdorKey;

        public IssueInputParametersBuilder(IssueService issueService, FieldManager fieldManager, ProjectManager projectManager,
                ConstantsManager constantsManager, FieldLayoutManager fieldLayoutManager, JiraAuthenticationContext jiraAuthenticationContext,
                VersionBeanFactory versionBeanFactory, VelocityRequestContextFactory velocityRequestContextFactory, ContextUriInfo contextUriInfo,
                JiraBaseUrls baseUrls, PermissionManager permissionManager, ProjectBeanFactory projectBeanFactory,
                FieldScreenRendererFactory fieldScreenRendererFactory, WorkflowManager workflowManager, IssueFactory issueFactory,
                IssueSecurityLevelManager issueSecurityLevelManager)
        {
            this.fieldManager = fieldManager;
            this.projectManager = projectManager;
            this.constantsManager = constantsManager;
            this.fieldLayoutManager = fieldLayoutManager;
            this.jiraAuthenticationContext = jiraAuthenticationContext;
            this.versionBeanFactory = versionBeanFactory;
            this.velocityRequestContextFactory = velocityRequestContextFactory;
            this.contextUriInfo = contextUriInfo;
            this.baseUrls = baseUrls;
            this.permissionManager = permissionManager;
            this.projectBeanFactory = projectBeanFactory;
            this.fieldScreenRendererFactory = fieldScreenRendererFactory;
            this.workflowManager = workflowManager;
            this.issueFactory = issueFactory;
            this.issueSecurityLevelManager = issueSecurityLevelManager;

            this.parameters = issueService.newIssueInputParameters();
            this.parameters.setRetainExistingValuesWhenParameterNotProvided(true, false);

            this.errors = new SimpleErrorCollection();
        }

        public ErrorCollection getErrors()
        {
            return errors;
        }

        public IssueInputParameters getParameters()
        {
            return parameters;
        }

        public String getParentIdorKey()
        {
            return parentIdorKey;
        }

        void buildForEdit(IssueUpdateBean request, Issue issue)
        {
            build(request, getValidFieldsForEdit(issue),true);
            this.parameters.setRetainExistingValuesWhenParameterNotProvided(true, true);
            if (!errors.hasAnyErrors())
            {
                finalizeIssueInputParams(issue, issue);
            }
        }

        void buildForTransition(IssueUpdateBean updateBean, Issue issue)
        {
            build(updateBean, getValidFieldsForTransition(issue, updateBean.getTransition().getId()),false);
            if (!errors.hasAnyErrors())
            {
                finalizeIssueInputParams(issue, issue);
            }
        }

        private void build(IssueUpdateBean request, Set<String> validFieldIds, boolean mustHaveFields)
        {
            Map<String, Object> fields = request.fields();
            Map<String, List<FieldOperation>> update = request.update();

            if (mustHaveFields && fields == null && update == null)
            {
                errors.addErrorMessage("one of 'fields' or 'update' required", ErrorCollection.Reason.VALIDATION_FAILED);
                return;
            }

            if (fields != null) {
                handleFields(fields, validFieldIds);
            }
            if (update != null) {
                handleUpdate(update, validFieldIds);
            }

            parameters.setHistoryMetadata(request.getHistoryMetadata());
        }

        public void buildForCreate(IssueUpdateBean request)
        {
            // Need the issue context before we go any further.
            initProjectAndIssueType(request.fields());
            Long projectId = parameters.getProjectId();
            if (projectId == null)
            {
                errors.addError("project", "project is required", ErrorCollection.Reason.VALIDATION_FAILED);
                return;
            }
            String issueTypeId = parameters.getIssueTypeId();
            if (issueTypeId == null)
            {
                errors.addError("issuetype", "issue type is required", ErrorCollection.Reason.VALIDATION_FAILED);
                return;
            }
            IssueContextImpl issueCtx = new IssueContextImpl(projectId, issueTypeId);
            if (issueCtx.getProjectObject() == null)
            {
                errors.addError("project", "valid project is required", ErrorCollection.Reason.VALIDATION_FAILED);
                return;
            }
            if (issueCtx.getIssueTypeObject() == null)
            {
                errors.addError("issuetype", "valid issue type is required", ErrorCollection.Reason.VALIDATION_FAILED);
                return;
            }

            parameters.setApplyDefaultValuesWhenParameterNotProvided(true);
            build(request, getValidFieldsForCreate(issueCtx), true);
            if (!errors.hasAnyErrors())
            {
                finalizeIssueInputParams(issueCtx, null);
            }
        }

        private void initProjectAndIssueType(Map<String, Object> fields)
        {
            for (Map.Entry<String, Object> e : fields.entrySet())
            {
                String fieldId = e.getKey();
                Object value = e.getValue();
                JsonData data = new JsonData(value);

                if ("project".equals(fieldId)) {
                    parameters.setProjectId(parseProject(fieldId, data, errors));
                }
                if ("issuetype".equals(fieldId)) {
                    parameters.setIssueTypeId(parseIssueType(fieldId, data, errors));
                }
            }
        }

        private void handleFields(Map<String, Object> fields, Set<String> validFieldIds)
        {
            for (Map.Entry<String, Object> e : fields.entrySet())
            {
                String fieldId = e.getKey();
                Object value = e.getValue();
                JsonData data = new JsonData(value);


                // this "parent" fields aren't really "fields", look for them especially
                if ("parent".equals(fieldId))
                {
                    String id = data.asObjectWithProperty("id", fieldId, errors);
                    if (id != null)
                    {
                        parentIdorKey = id;
                    }
                    else
                    {
                        parentIdorKey = data.asObjectWithProperty("key", fieldId, errors);
                    }
                }
                // fields that don't support set-verbs (yet)
                else if ("project".equals(fieldId)) { parameters.setProjectId(parseProject(fieldId, data, errors)); }
                else if ("issuetype".equals(fieldId)) { parameters.setIssueTypeId(parseIssueType(fieldId, data, errors)); }
                else {
                    if (validFieldIds.contains(fieldId))
                    {
                        addSetOperation(updates, fieldId, value);
                    }
                    else
                    {
                        errors.addError(fieldId, "Field '" + fieldId + "' cannot be set. It is not on the appropriate screen, or unknown.");
                    }
                }
            }
        }

        private Long parseProject(String fieldId, JsonData data, ErrorCollection errors)
        {
            ResourceRef project = data.convertValue(fieldId, ResourceRef.class, errors);
            if (project == null)
            {
                return null;
            }

            if (project.id() != null)
            {
                try
                {
                    Project byId = projectManager.getProjectObj(Long.parseLong(project.id()));
                    if (byId != null)
                    {
                        return byId.getId();
                    }
                }
                catch (NumberFormatException e)
                {
                    errors.addError(fieldId, "invalid id: " + project.id());
                }
            }
            else if (project.key() != null)
            {

                Project byKey = projectManager.getProjectObjByKey(project.key());
                if (byKey != null)
                {
                    return byKey.getId();
                }
            }

            errors.addError(fieldId, "Could not find project by id or key.");
            return null;
        }

        private String parseIssueType(String fieldId, JsonData data, ErrorCollection errors)
        {
            ResourceRef issuetype = data.convertValue(fieldId, ResourceRef.class, errors);
            if (issuetype == null)
            {
                return null;
            }

            if (issuetype.id() != null)
            {
                return issuetype.id();
            }
            else if (issuetype.name() != null)
            {
                String name = issuetype.name();
                for (IssueType it : constantsManager.getAllIssueTypeObjects())
                {
                    if (name.equals(it.getNameTranslation()) || (name.equals(it.getName()))) {
                        return it.getId();
                    }
                }
            }

            errors.addError(fieldId, "Could not find issuetype by id or name.");
            return null;
        }

        private void handleUpdate(Map<String, List<FieldOperation>> update, Set<String> validFieldIds)
        {
            for (String fieldId : update.keySet())
            {
                if (validFieldIds.contains(fieldId))
                {
                    if (updates.containsKey(fieldId))
                    {
                        errors.addErrorMessage("Field '" + fieldId + "' cannot appear in both 'fields' and 'update'");
                    }
                    else
                    {
                        updates.put(fieldId, update.get(fieldId));
                    }
                }
                else
                {
                    errors.addError(fieldId, "Field '" + fieldId + "' cannot be set. It is not on the appropriate screen, or unknown.");
                }
            }

        }

        private void finalizeIssueInputParams(IssueContext issueCtx ,Issue issue)
        {
            for (Map.Entry<String, List<FieldOperation>> entry : updates.entrySet())
            {
                String fieldId = entry.getKey();
                List<FieldOperation> operations = entry.getValue();

                Field field = fieldManager.getField(fieldId);

                if (field == null)
                {
                    String msg = "Field with id '" + fieldId + "' does not exist";
                    if (issue != null) {
                        msg += " for issue '" + issue.getKey() + "'";
                    }
                    errors.addErrorMessage(msg);
                }
                else if (field instanceof RestFieldOperations)
                {
                    final RestFieldOperationsHandler operationsHandler = ((RestFieldOperations) field).getRestFieldOperation();
                    final Set<String> supportedOperations = operationsHandler.getSupportedOperations();
                    List<FieldOperationHolder> operationsHolders = new ArrayList<FieldOperationHolder>();
                    for (FieldOperation operation : operations)
                    {
                        String operationName = operation.getOperation();
                        if (!supportedOperations.contains(operationName))
                        {
                            errors.addErrorMessage("Field with id' " + fieldId + "' and name '" + field.getName() + "' does not support operation '" + operationName + "' Supported operation(s) are: '" + StringUtils.join(supportedOperations.iterator(), ",") + "'");
                        }
                        else
                        {
                            FieldOperationHolder fieldOperationHolder = new FieldOperationHolder(operationName, new JsonData(operation.getValue()));
                            operationsHolders.add(fieldOperationHolder);
                        }
                    }
                    if (!operationsHolders.isEmpty())
                    {
                        ErrorCollection errorCollection = operationsHandler.updateIssueInputParameters(issueCtx, issue, fieldId, parameters, operationsHolders);
                        errors.getErrorMessages().addAll(errorCollection.getErrorMessages());
                        errors.getErrors().putAll(errorCollection.getErrors());
                    }

                }
                else {
                    // TODO eventually all fields will be rest-aware?
                }
            }
        }

        private void addSetOperation(Map<String, List<FieldOperation>> updates, String fieldId, Object value)
        {
            Field field = fieldManager.getField(fieldId);
            if (field instanceof RestFieldOperations)
            {
                final RestFieldOperations restField = (RestFieldOperations) field;
                if (restField.getRestFieldOperation().getSupportedOperations().contains("set"))
                {
                    FieldOperation op = new FieldOperation();
                    op.init("set", value);
                    updates.put(fieldId, Arrays.asList(op));
                }
                else {
                    errors.addError(fieldId, "Field does not support update '" + fieldId + "'");
                }
            }
            else {
                errors.addError(fieldId, "Field does not support update '" + fieldId + "'");
            }
        }

        private Set<String> getValidFieldsForEdit(Issue issue)
        {
            EditMetaFieldBeanBuilder builder = new EditMetaFieldBeanBuilder(fieldLayoutManager, issue.getProjectObject(), issue, issue.getIssueTypeObject(),
                    jiraAuthenticationContext.getLoggedInUser(), versionBeanFactory, velocityRequestContextFactory, contextUriInfo, baseUrls, permissionManager, fieldScreenRendererFactory,
                    fieldManager);

            Set<String> strings = new HashSet<String>();
            strings.add(IssueFieldConstants.COMMENT);
            strings.addAll(builder.build().keySet());
            return strings;
        }

        private Set<String> getValidFieldsForCreate(IssueContext issueCtx)
        {
            final MutableIssue nullIssue = issueFactory.getIssue();
            Project projectObject = issueCtx.getProjectObject();
            if (projectObject != null && issueCtx.getIssueTypeObject() != null)
            {
                nullIssue.setProjectObject(projectObject);
                nullIssue.setIssueTypeObject(issueCtx.getIssueTypeObject());
                CreateMetaFieldBeanBuilder builder = new CreateMetaFieldBeanBuilder(fieldLayoutManager, issueCtx.getProjectObject(),
                        nullIssue, issueCtx.getIssueTypeObject(), jiraAuthenticationContext.getLoggedInUser(), versionBeanFactory,
                        velocityRequestContextFactory, contextUriInfo, baseUrls, permissionManager, fieldScreenRendererFactory,
                        jiraAuthenticationContext, fieldManager,
                        new DefaultFieldMetaBeanHelper(projectObject, issueCtx.getIssueTypeObject(), issueSecurityLevelManager));

                return builder.build().keySet();
            }
            else
            {
                //noinspection unchecked
                return Collections.EMPTY_SET;
            }
        }

        private Set<String> getValidFieldsForTransition(Issue issue, String actionId)
        {
            ActionDescriptor actionDescriptor = getActionDescriptor(issue, actionId);
            if (issue == null)
            {
                errors.addErrorMessage("getValidFieldsForTransition - Issue is null");
            }
            if (actionDescriptor == null)
            {
                errors.addErrorMessage("Transition id' " + actionId + "' is not valid for this issue.");
            }
            TransitionMetaFieldBeanBuilder builder = new TransitionMetaFieldBeanBuilder(fieldScreenRendererFactory, fieldLayoutManager,
                    actionDescriptor, issue, jiraAuthenticationContext.getLoggedInUser(), versionBeanFactory, velocityRequestContextFactory, contextUriInfo, baseUrls);

            Set<String> strings = new HashSet<String>();
            strings.add(IssueFieldConstants.COMMENT);
            strings.addAll(builder.build().keySet());
            return strings;
        }

        private ActionDescriptor getActionDescriptor(Issue issue, String actionId)
        {
            final JiraWorkflow workflow = workflowManager.getWorkflow(issue);
            if (workflow == null)
            {
                return null;
            }

            final WorkflowDescriptor descriptor = workflow.getDescriptor();
            if (descriptor == null)
            {
                return null;
            }
            try
            {
                return descriptor.getAction(Integer.valueOf(actionId));
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }

}
