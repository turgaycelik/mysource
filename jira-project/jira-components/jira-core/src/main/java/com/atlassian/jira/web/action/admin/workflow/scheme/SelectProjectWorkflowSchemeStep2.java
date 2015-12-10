package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.ofbiz.OfBizStringFieldComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.migration.AssignableWorkflowSchemeMigrationHelper;
import com.atlassian.jira.workflow.migration.DraftWorkflowSchemeMigrationHelper;
import com.atlassian.jira.workflow.migration.MigrationHelperFactory;
import com.atlassian.jira.workflow.migration.WorkflowSchemeMigrationHelper;
import com.atlassian.jira.workflow.migration.WorkflowSchemeMigrationTaskAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

@WebSudoRequired
public class SelectProjectWorkflowSchemeStep2 extends SelectProjectWorkflowScheme
{
    private final WorkflowManager workflowManager;
    private Boolean haveIssuesToMigrate;
    private final SearchProvider searchProvider;
    private final static String ABORTED_MIGRATION_MESSAGE_KEY = "admin.workflowmigration.aborted.defaultworkflow";
    private static final String FAILURE_MIGRATION_MESSAGE_KEY = "admin.workflowmigration.withfailure.defaultworkflow";
    private final ConstantsManager constantsManager;
    private final MigrationHelperFactory migrationHelperFactory;

    private WorkflowSchemeHelper<?> workflowSchemeHelper;

    public SelectProjectWorkflowSchemeStep2(WorkflowSchemeMigrationTaskAccessor taskAccessor, final SearchProvider searchProvider, final TaskManager taskManager,
            final TaskDescriptorBean.Factory taskDescriptorFactory,
            final WorkflowManager workflowManager, final ConstantsManager constantsManager,
            final WorkflowSchemeManager workflowSchemeManager, final MigrationHelperFactory migrationHelperFactory)
    {
        super(taskAccessor, taskManager, workflowSchemeManager, taskDescriptorFactory);
        this.searchProvider = searchProvider;
        this.workflowManager = workflowManager;
        this.constantsManager = constantsManager;
        this.migrationHelperFactory = migrationHelperFactory;
    }

    @Override
    public String doDefault() throws Exception
    {
        List<Project> projects;
        if (isDraftMigration())
        {
            AssignableWorkflowScheme existingScheme = getExistingScheme();
            projects = getWorkflowSchemeManager().getProjectsUsing(existingScheme);
        }
        else
        {
            projects = Collections.singletonList(getProjectObject());
        }
        setProjects(projects);

        validate();

        if (invalidInput())
        {
            return ERROR;
        }

        if (getWorkflowSchemeHelper().doNothing())
        {
            return redirectUser();
        }

        return super.doDefault();
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        validate();

        if (invalidInput())
        {
            return ERROR;
        }

        if (getMigrationHelper().doQuickMigrate())
        {
            return redirectUser();
        }
        else
        {
            try
            {
                return getWorkflowSchemeHelper().waitForUpdatesToFinishAndExecute(new Callable<String>()
                {
                    @Override
                    public String call()
                    {
                        getWorkflowSchemeHelper().reload();

                        // setup manual migrations
                        addMigrationMappings();

                        if (invalidInput())
                        {
                            return getResult();
                        }

                        return getRedirect(getMigrationHelper().migrateAsync().getProgressURL());
                    }
                });
            }
            catch (final RejectedExecutionException e)
            {
                return ERROR;
            }
        }
    }

    private String redirectUser() throws Exception
    {
        return getRedirect(getRedirectURL());
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();
        getWorkflowSchemeHelper().validate();
    }

    private void addMigrationMappings()
    {
        final Map params = ActionContext.getParameters();

        List<GenericValue> typesNeedingMigration = getMigrationHelper().getTypesNeedingMigration();
        for (final GenericValue issueType : typesNeedingMigration)
        {
            Collection<GenericValue>  statusesNeedingMigration = getMigrationHelper().getStatusesNeedingMigration(issueType);
            for (final GenericValue status : statusesNeedingMigration)
            {
                final String[] paramValue = (String[]) params.get(getSelectListName(issueType, status));
                if ((paramValue == null) || (paramValue.length != 1))
                {
                    addErrorMessage(getText("admin.errors.workflows.specify.mapping", issueType.getString("name"), status.getString("name")));
                }
                else
                {
                    getMigrationHelper().addMapping(issueType, status, constantsManager.getStatus(paramValue[0]));
                }
            }
        }
    }

    public long getNumAffectedIssues(final GenericValue issueType) throws SearchException
    {
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        final JqlClauseBuilder whereBuilder = queryBuilder.where().defaultAnd();
        whereBuilder.issueType(issueType.getString("id"));
        whereBuilder.project().inNumbers(getProjectIds());
        final List<String> statuses = new ArrayList<String>();
        Collection<GenericValue> statusesNeedingMigration = getMigrationHelper().getStatusesNeedingMigration(issueType);
        for (final GenericValue status : statusesNeedingMigration)
        {
            statuses.add(status.getString("id"));
        }
        if (!statuses.isEmpty())
        {
            whereBuilder.status().inStrings(statuses);
        }
        return searchProvider.searchCountOverrideSecurity(queryBuilder.buildQuery(), getLoggedInUser());
    }

    public long getTotalAffectedIssues(final GenericValue issueType) throws SearchException
    {
        final JqlClauseBuilder queryBuilder = JqlQueryBuilder.newBuilder().where();
        queryBuilder.issueType(issueType.getString("id"));
        queryBuilder.and();
        queryBuilder.project().inNumbers(getProjectIds());
        return searchProvider.searchCountOverrideSecurity(queryBuilder.buildQuery(), getLoggedInUser());
    }

    public JiraWorkflow getTargetWorkflow(final GenericValue issueType) throws WorkflowException
    {
        return workflowManager.getWorkflowFromScheme(getWorkflowScheme(), issueType.getString("id"));
    }

    public JiraWorkflow getExistingWorkflow(final GenericValue issueType) throws GenericEntityException, WorkflowException
    {
        return workflowManager.getWorkflowFromScheme(getExistingScheme(), issueType.getString("id"));
    }

    public Collection getTargetStatuses(final GenericValue issueType) throws WorkflowException, GenericEntityException
    {
        return getTargetWorkflow(issueType).getLinkedStatuses();
    }

    public String getSelectListName(final GenericValue issueType, final GenericValue status)
    {
        return "mapping_" + issueType.getString("id") + "_" + status.getString("id");
    }

    public boolean isHaveIssuesToMigrate() throws GenericEntityException
    {
        if (haveIssuesToMigrate == null)
        {
            haveIssuesToMigrate = getMigrationHelper().isHaveIssuesToMigrate();
        }

        return haveIssuesToMigrate;
    }

    public Collection getStatusesNeedingMigration(final GenericValue issueType)
    {
        final List<GenericValue> statuses = new ArrayList<GenericValue>(getMigrationHelper().getStatusesNeedingMigration(issueType));
        Collections.sort(statuses, new OfBizStringFieldComparator("sequence"));
        return statuses;
    }

    public static String getAbortedMigrationMessageKey()
    {
        return ABORTED_MIGRATION_MESSAGE_KEY;
    }

    public static String getFailureMigrationMessageKey()
    {
        return FAILURE_MIGRATION_MESSAGE_KEY;
    }

    private String getSchemeName()
    {
        final WorkflowScheme scheme = getWorkflowScheme();
        if (scheme == null)
        {
            return getText("admin.common.words.default");
        }
        else
        {
            return scheme.getName();
        }
    }

    public WorkflowSchemeMigrationHelper<?> getMigrationHelper()
    {
        return getWorkflowSchemeHelper().getMigrationHelper();
    }

    public WorkflowScheme getWorkflowScheme()
    {
        return getWorkflowSchemeHelper().getWorkflowScheme();
    }

    private WorkflowSchemeHelper getWorkflowSchemeHelper()
    {
        if (workflowSchemeHelper == null)
        {
            workflowSchemeHelper = isDraftMigration() ? new DraftWorkflowSchemeHelper()
                                                      : new AssignableWorkflowSchemeHelper();
        }

        return workflowSchemeHelper;
    }

    private abstract class WorkflowSchemeHelper<T extends WorkflowScheme>
    {
        private T workflowScheme;
        private WorkflowSchemeMigrationHelper<T> migrationHelper;

        WorkflowSchemeHelper()
        {
            reload();
        }

        T getWorkflowScheme()
        {
            return workflowScheme;
        }

        WorkflowSchemeMigrationHelper<T> getMigrationHelper()
        {
            return migrationHelper;
        }

        void reload()
        {
            try
            {
                workflowScheme = doGetWorkflowScheme();

                if (workflowScheme != null)
                {
                    migrationHelper = doGetMigrationHelper();
                }
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }

        abstract void validate();
        abstract T doGetWorkflowScheme() throws GenericEntityException;
        abstract WorkflowSchemeMigrationHelper<T> doGetMigrationHelper() throws GenericEntityException;
        abstract boolean doNothing() throws GenericEntityException;
        abstract String waitForUpdatesToFinishAndExecute(Callable<String> task) throws Exception;
    }

    private class AssignableWorkflowSchemeHelper extends WorkflowSchemeHelper<AssignableWorkflowScheme>
    {
        @Override
        AssignableWorkflowScheme doGetWorkflowScheme()
        {
            if (getSchemeId() == null)
            {
                return getWorkflowSchemeManager().getDefaultWorkflowScheme();
            }

            return getWorkflowSchemeManager().getWorkflowSchemeObj(getSchemeId());
        }

        @Override
        AssignableWorkflowSchemeMigrationHelper doGetMigrationHelper() throws GenericEntityException
        {
            return migrationHelperFactory.createMigrationHelper(getProjectObject(), getWorkflowScheme());
        }

        @Override
        boolean doNothing() throws GenericEntityException
        {
            // if they're not swapping scheme at all - do nothing
            AssignableWorkflowScheme existingScheme = getExistingScheme();
            WorkflowScheme targetScheme = getWorkflowScheme();
            return ((targetScheme == null) && (existingScheme == null)) || ((targetScheme != null) && targetScheme.equals(existingScheme));
        }

        @Override
        void validate()
        {
        }

        @Override
        String waitForUpdatesToFinishAndExecute(Callable<String> task) throws Exception
        {
            return getWorkflowSchemeManager().waitForUpdatesToFinishAndExecute(getWorkflowScheme(), task);
        }
    }

    private class DraftWorkflowSchemeHelper extends WorkflowSchemeHelper<DraftWorkflowScheme>
    {
        @Override
        DraftWorkflowScheme doGetWorkflowScheme() throws GenericEntityException
        {
            AssignableWorkflowScheme existingScheme = getExistingScheme();
            return getWorkflowSchemeManager().getDraftForParent(existingScheme);
        }

        @Override
        DraftWorkflowSchemeMigrationHelper doGetMigrationHelper() throws GenericEntityException
        {
            return migrationHelperFactory.createMigrationHelper(getProjectObject(), getProjects(), getWorkflowScheme());
        }

        @Override
        boolean doNothing() throws GenericEntityException
        {
            return false;
        }

        @Override
        void validate()
        {
            if (getWorkflowScheme() == null)
            {
                addErrorMessage(getText("admin.errors.workflows.scheme.no.draft"));
            }
        }

        @Override
        String waitForUpdatesToFinishAndExecute(Callable<String> task) throws Exception
        {
            // We lock the parent scheme in case of drafts.
            return getWorkflowSchemeManager().waitForUpdatesToFinishAndExecute(getExistingScheme(), task);
        }
    }
}
