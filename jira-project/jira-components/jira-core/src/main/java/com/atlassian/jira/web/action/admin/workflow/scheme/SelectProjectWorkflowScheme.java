package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.fugue.Iterables;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.AbstractSelectProjectScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.migration.WorkflowSchemeMigrationTaskAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@WebSudoRequired
public class SelectProjectWorkflowScheme extends AbstractSelectProjectScheme
{
    private final WorkflowSchemeMigrationTaskAccessor taskAccessor;
    private final TaskManager taskManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final TaskDescriptorBean.Factory taskBeanFactory;

    private TaskDescriptorBean<WorkflowMigrationResult> currentActivateTask;
    private List<Project> projects;
    private boolean draftMigration;

    public SelectProjectWorkflowScheme(WorkflowSchemeMigrationTaskAccessor taskAccessor, TaskManager taskManager,
            WorkflowSchemeManager workflowSchemeManager, TaskDescriptorBean.Factory taskBeanFactory)
    {
        this.workflowSchemeManager = workflowSchemeManager;
        this.taskBeanFactory = taskBeanFactory;
        this.taskAccessor = taskAccessor;
        this.taskManager = taskManager;
    }

    @Override
    public String doDefault() throws Exception
    {
        if (hasPermission())
        {
            return super.doDefault();
        }
        else
        {
            return "securitybreach";
        }
    }

    @Override
    protected void doValidation()
    {
    }

    @Override
    public SchemeManager getSchemeManager()
    {
        return workflowSchemeManager;
    }

    public WorkflowSchemeManager getWorkflowSchemeManager()
    {
        return workflowSchemeManager;
    }

    @Override
    public String getProjectReturnUrl()
    {
        return "/plugins/servlet/project-config/" + getProjectObject().getKey() + "/workflows";
    }

    @Override
    public String getReturnUrlForCancelLink()
    {
        String url;
        if (getProjectObject() != null)
        {
            url = getProjectReturnUrl();
        }
        else
        {
            // cannot use getSchemeId() as that points to the draft that we have since destroyed
            Long schemeId = getWorkflowSchemeManager().getWorkflowSchemeObj(Iterables.first(getProjects()).get()).getId();
            url = "/secure/admin/EditWorkflowScheme.jspa?schemeId=" + schemeId;
        }
        return url;
    }

    @Override
    public String getRedirectURL()
    {
        String url = getReturnUrlForCancelLink();
        if (isDraftMigration())
        {
            url += "#draftMigrationSuccess";
        }
        return url;
    }

    TaskManager getTaskManager()
    {
        return taskManager;
    }

    public boolean isAnyLiveTasks()
    {
        return !taskManager.getLiveTasks().isEmpty();
    }

    void initTaskDescriptorBean(TaskDescriptor<WorkflowMigrationResult> taskDescriptor)
    {
        currentActivateTask = taskBeanFactory.create(taskDescriptor);
    }

    /**
     * Return the {@link com.atlassian.jira.web.bean.TaskDescriptorBean} associated with the task that is currently
     * migrating the workflow for the current project. The method can be told search for such a task if necessary. This
     * feature is useful when the action needs to see if there are currently any tasks migrating the current project.
     *
     * @param searchForTask When true the method will attempt to find a task that is currently migrating our project.
     *  When false will simply return the bean configured with {@link #initTaskDescriptorBean(com.atlassian.jira.task.TaskDescriptor)}
     *  or null is no bean was configured. 
     *
     * @return a task or null if none can be found.
     */

    TaskDescriptorBean<WorkflowMigrationResult> getCurrentTask(boolean searchForTask)
    {
        if (currentActivateTask == null && searchForTask)
        {
            TaskDescriptor<WorkflowMigrationResult> taskDescriptor = null;
            if (isDraftMigration())
            {
                final DraftWorkflowScheme draft = workflowSchemeManager.getDraft(getSchemeId());
                if (draft != null)
                {
                    taskDescriptor = taskAccessor.getActiveByProjects(draft, false);
                }
            }
            else
            {
                final Project projectObject = getProjectObject();
                if (projectObject != null)
                {
                    taskDescriptor = taskAccessor.getActive(projectObject);
                }
                if (taskDescriptor == null)
                {
                    AssignableWorkflowScheme targetScheme = (getSchemeId() == null)
                                                            ? workflowSchemeManager.getDefaultWorkflowScheme()
                                                            : workflowSchemeManager.getWorkflowSchemeObj(getSchemeId());

                    DraftWorkflowScheme draft = workflowSchemeManager.getDraftForParent(targetScheme);
                    if (draft != null)
                    {
                        taskDescriptor = taskAccessor.getActive(draft);
                    }
                }
            }
            if (taskDescriptor != null)
            {
                initTaskDescriptorBean(taskDescriptor);
            }
        }
        return currentActivateTask;
    }

    public TaskDescriptorBean getCurrentTask()
    {
        return getCurrentTask(true);
    }

    public boolean isDraftMigration()
    {
        return draftMigration;
    }

    public void setDraftMigration(boolean draftMigration)
    {
        this.draftMigration = draftMigration;
    }

    AssignableWorkflowScheme getExistingScheme()
    {
        if (isDraftMigration())
        {
            return workflowSchemeManager.getParentForDraft(getSchemeId());
        }
        else
        {
            return workflowSchemeManager.getWorkflowSchemeObj(getProjectObject());
        }
    }

    public List<Project> getProjects()
    {
        return projects != null ? projects : Collections.<Project>emptyList();
    }

    public void setProjects(List<Project> projects)
    {
        this.projects = projects;
    }

    public String getProjectIdsParameter()
    {
        return getProjectIdsParameter(getProjects());
    }

    List<Long> getProjectIds()
    {
        return getProjectIds(getProjects());
    }

    public static List<Long> getProjectIds(List<Project> projects)
    {
        return Lists.transform(projects, new Function<Project, Long>()
        {
            @Override
            public Long apply(Project project)
            {
                return project.getId();
            }
        });
    }

    private static List<String> getProjectIdStrings(List<Project> projects)
    {
        return Lists.transform(getProjectIds(projects), Functions.toStringFunction());
    }

    public static String getProjectIdsParameter(List<Project> projects)
    {
        return StringUtils.join(getProjectIdStrings(projects), ",");
    }

    public void setProjectIdsParameter(String projectIdsStr)
    {
        List<String> projectIds = Arrays.asList(projectIdsStr.split(","));

        List<Project> projects = Lists.transform(projectIds, new Function<String, Project>()
        {
            @Override
            public Project apply(String projectId)
            {
                return getProjectManager().getProjectObj(Long.valueOf(projectId));
            }
        });

        setProjects(projects);
    }
}
