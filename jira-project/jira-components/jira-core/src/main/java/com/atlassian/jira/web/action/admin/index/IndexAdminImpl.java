/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.index;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import javax.servlet.ServletContext;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.Node;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.BackgroundIndexTaskContext;
import com.atlassian.jira.config.ForegroundIndexTaskContext;
import com.atlassian.jira.config.IndexTask;
import com.atlassian.jira.config.IndexTaskContext;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.index.ha.IndexRecoveryService;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.AlreadyExecutingException;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskMatcher;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.PathUtils;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import org.apache.commons.lang.StringUtils;

import webwork.action.ServletActionContext;

@WebSudoRequired
public class IndexAdminImpl extends ProjectActionSupport implements IndexAdmin
{
    private static final String PROGRESS = "progress";

    private final IndexLifecycleManager indexLifecycleManager;
    private final GlobalPermissionManager globalPermissionManager;
    private final TaskManager taskManager;
    private final JiraAuthenticationContext authenticationContext;
    private final FileFactory fileFactory;
    private final IndexRecoveryService indexRecoveryService;
    private final ClusterManager clusterManager;
    private final JiraHome jiraHome;
    private final BeanFactory i18nBeanFactory;

    private Long taskId;

    private TaskDescriptorBean<IndexCommandResult> currentTask;
    private long reindexTime;
    private String indexPath;
    private TaskDescriptor<IndexCommandResult> currentTaskDescriptor;
    private final TaskDescriptorBean.Factory taskBeanFactory;
    private final IndexPathManager indexPathManager;
    private IndexPathManager.Mode indexMode;
    private String indexingStrategy;
    private Boolean indexConsistent;
    private TaskDescriptor<?> taskInProgress;
    private String recoveryFilename;
    private String copyFromNodeId;
    private boolean copyRequested;

    // this constructor added for backwards-compatibility
    public IndexAdminImpl(IndexLifecycleManager indexLifecycleManager, final GlobalPermissionManager globalPermissionManager,
            final TaskManager taskManager, final JiraAuthenticationContext authenticationContext, final OutlookDateManager outlookDateManager,
            final IndexPathManager indexPathManager, FileFactory fileFactory)
    {
        this(ComponentAccessor.getProjectManager(), ComponentAccessor.getPermissionManager(), indexLifecycleManager,
                globalPermissionManager, taskManager, authenticationContext,
                ComponentAccessor.getComponent(TaskDescriptorBean.Factory.class), indexPathManager, fileFactory,
                ComponentAccessor.getComponent(IndexRecoveryService.class), ComponentAccessor.getComponent(ClusterManager.class),
                ComponentAccessor.getComponent(JiraHome.class), ComponentAccessor.getI18nHelperFactory());
    }

    public IndexAdminImpl(ProjectManager projectManager, PermissionManager permissionManager, IndexLifecycleManager indexLifecycleManager,
            final GlobalPermissionManager globalPermissionManager, final TaskManager taskManager, final JiraAuthenticationContext authenticationContext,
            final TaskDescriptorBean.Factory factory, final IndexPathManager indexPathManager, final FileFactory fileFactory,
            final IndexRecoveryService indexRecoveryService, final ClusterManager clusterManager, final JiraHome jiraHome, final BeanFactory i18nBeanFactory)
    {
        super(projectManager, permissionManager);
        this.indexLifecycleManager = indexLifecycleManager;
        this.globalPermissionManager = globalPermissionManager;
        this.taskManager = taskManager;
        this.authenticationContext = authenticationContext;
        taskBeanFactory = factory;
        this.indexPathManager = indexPathManager;
        this.fileFactory = fileFactory;
        this.clusterManager = clusterManager;
        this.jiraHome = jiraHome;
        this.i18nBeanFactory = i18nBeanFactory;
        indexMode = indexPathManager.getMode();
        indexPath = indexPathManager.getIndexRootPath();
        this.indexRecoveryService = indexRecoveryService;
    }

    public String getIndexPathOption()
    {
        return indexMode.toString();
    }

    public void setIndexPathOption(final String indexPathOption)
    {
        indexMode = IndexPathManager.Mode.valueOf(indexPathOption);
    }

    /**
     * Returns the absolute path for the Default Index directory ([jira-home]/caches/index/)
     * This is used for read-only info added to the "Use Default Directory" option.
     *
     * @return the absolute path for the Default Index directory ([jira-home]/caches/index/)
     */
    public String getDefaultIndexPath()
    {
        return indexPathManager.getDefaultIndexRootPath();
    }

    public boolean getShowCustom()
    {
        // we need the second check (disabled && path not null) because they may have had a custom
        // path set...but then indexing got disabled somehow during startup.
        return indexMode == IndexPathManager.Mode.CUSTOM ||
                (indexMode == IndexPathManager.Mode.DISABLED && indexPath != null);
    }

    public String getIndexPath()
    {
        return indexPath;
    }

    private void validateIndexPath(final String path)
    {
        if (StringUtils.isBlank(path))
        {
            addError("indexPath", getText("admin.errors.you.must.specify.a.path"));
        }
        else
        {
            final File actualPath = fileFactory.getFile(path);

            if (!actualPath.isAbsolute())
            {
                addError("indexPath", getText("setup.error.filepath.notabsolute"));
            }
            else
            {
                if (!actualPath.exists()) // if doesn't exist, try to create it
                {
                    actualPath.mkdirs();
                }

                if (!actualPath.exists() || !actualPath.isDirectory())
                {
                    addError("indexPath", getText("admin.errors.path.entered.does.not.exist"));
                }
                else if (!actualPath.canRead() || !actualPath.canWrite())
                {
                    addError("indexPath", getText("admin.errors.path.entered.is.not.readable"));
                }
            }
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        final TaskDescriptor<IndexCommandResult> taskDescriptor = getCurrentTaskDescriptor();
        if (taskDescriptor != null)
        {
            return getRedirect(taskDescriptor.getProgressURL());
        }

        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doActivate() throws Exception
    {
        if (!isHasSystemAdminPermission())
        {
            addErrorMessage(getText("admin.errors.no.perm.to.activate"));
            return ERROR;
        }

        if (indexMode == IndexPathManager.Mode.DISABLED)
        {
            addErrorMessage(getText("admin.errors.you.must.specify.a.path"));
            return ERROR;
        }

        //Add a warning that an upgrade is in progress
        JohnsonEventContainer eventCont = null;

        final ServletContext ctx = ServletActionContext.getServletContext();
        if (ctx != null)
        {
            eventCont = JohnsonEventContainer.get(ctx);
        }
        // otherwise non-web action; assume something else (caller?) is responsible for notifying the user
        if (!isIndexing())
        {
            if (indexMode == IndexPathManager.Mode.CUSTOM)
            {
                validateIndexPath(indexPath);
            }

            if (!invalidInput())
            {
                updateIndexPathManager();
                final Callable<IndexCommandResult> activateCommand = new ActivateAsyncIndexerCommand(false, eventCont, indexLifecycleManager, log,
                        authenticationContext.getI18nHelper(), i18nBeanFactory);
                final String taskName = getText("admin.indexing.jira.indexing");
                try
                {
                    final TaskDescriptor<IndexCommandResult> taskDescriptor = taskManager.submitTask(activateCommand, taskName, new ForegroundIndexTaskContext());
                    return getRedirect(taskDescriptor.getProgressURL());
                }
                catch (final AlreadyExecutingException e)
                {
                    return getRedirect(e.getTaskDescriptor().getProgressURL());
                }
                catch (final RejectedExecutionException e)
                {
                    addErrorMessage(getText("common.tasks.rejected.execution.exception", e.getMessage()));
                    return ERROR;
                }
            }
            else
            {
                return ERROR;
            }
        }
        return getRedirect(getRedirectUrl());
    }

    private boolean isCustomPathChanged()
    {
        return (indexMode == IndexPathManager.Mode.CUSTOM) && (!indexPath.equals(indexPathManager.getIndexRootPath()));
    }

    private boolean isModeChanged()
    {
        return indexMode != indexPathManager.getMode();
    }

    @RequiresXsrfCheck
    public String doReindex() throws Exception
    {
        //Add a warning that an upgrade is in progress
        JohnsonEventContainer eventCont = null;

        if (isIndexing())
        {
            final ServletContext ctx = ServletActionContext.getServletContext();
            if (ctx != null)
            {
                eventCont = JohnsonEventContainer.get(ctx);
            }

            if (!runInBackground() && isTaskInProgress())
            {
                addErrorMessage(getCannotReindexInForegroundMessage());
                return ERROR;
            }

            Callable<IndexCommandResult> indexCallable = createDefaultReindexCommand(eventCont);
            if (isHasSystemAdminPermission())
            {
                if (isCustomPathChanged())
                {
                    validateIndexPath(indexPath);
                    if (invalidInput())
                    {
                        return ERROR;
                    }
                }

                if (isCustomPathChanged() || isModeChanged())
                {
                    updateIndexPathManager();
                    indexCallable = new ActivateAsyncIndexerCommand(true, eventCont, indexLifecycleManager, log,
                            authenticationContext.getI18nHelper(), i18nBeanFactory);
                }
            }

            final String taskName = getText("admin.indexing.jira.indexing");
            try
            {
                if (runInBackground() && !indexLifecycleManager.isIndexConsistent())
                {
                    // No need to addErrorMessage because it should show up on the refresh anyway
                    return ERROR;
                }
                // re direct to progress action command
                final IndexTaskContext context = runInBackground() ? new BackgroundIndexTaskContext() : new ForegroundIndexTaskContext();
                return getRedirect(taskManager.submitTask(indexCallable, taskName, context, runInBackground()).getProgressURL());
            }
            catch (final AlreadyExecutingException e)
            {
                return getRedirect(e.getTaskDescriptor().getProgressURL());
            }
            catch (final RejectedExecutionException e)
            {
                addErrorMessage(getText("common.tasks.rejected.execution.exception", e.getMessage()));
                return ERROR;
            }
        }
        return getRedirect(getRedirectUrl());
    }

    private Callable<IndexCommandResult> createDefaultReindexCommand(JohnsonEventContainer eventCont)
    {
        if (runInBackground())
        {
            return new ReIndexBackgroundIndexerCommand(indexLifecycleManager, log, authenticationContext.getI18nHelper(), i18nBeanFactory);
        }

        // assume "stop-the-world"
        return new ReIndexAsyncIndexerCommand(eventCont, indexLifecycleManager, log, authenticationContext.getI18nHelper(), i18nBeanFactory);
    }

    private Callable<IndexCommandResult> createIndexRecoveryCommand(final String fileName)
    {
        return new IndexRecoveryCommand(getLoggedInApplicationUser(), indexRecoveryService, log, authenticationContext.getI18nHelper(), fileName);
    }

    private boolean runInBackground()
    {
        return "background".equals(indexingStrategy);
    }

    private void updateIndexPathManager()
    {
        if (indexMode == IndexPathManager.Mode.DEFAULT)
        {
            indexPathManager.setUseDefaultDirectory();
        }
        else if (indexMode == IndexPathManager.Mode.CUSTOM)
        {
            indexPathManager.setIndexRootPath(indexPath);
        }
    }

    public String doProgress() throws ExecutionException, InterruptedException
    {
        if (taskId == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.id"));
            return ERROR;
        }
        currentTaskDescriptor = taskManager.getTask(taskId);
        if (currentTaskDescriptor == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.found"));
            return ERROR;
        }
        final TaskContext context = currentTaskDescriptor.getTaskContext();
        if (!(context instanceof IndexTaskContext))
        {
            addErrorMessage(getText("common.tasks.wrong.task.context", IndexTaskContext.class.getName(), context.getClass().getName()));
            return ERROR;
        }

        currentTask = taskBeanFactory.create(currentTaskDescriptor);
        if (currentTaskDescriptor.isFinished() && !taskManager.isCancelled(taskId))
        {
            final IndexCommandResult result = currentTaskDescriptor.getResult();
            if (result.isSuccessful())
            {
                reindexTime = result.getReindexTime();
            }
            else
            {
                addErrorCollection(result.getErrorCollection());
            }
        }
        return PROGRESS;
    }

    @RequiresXsrfCheck
    public String doCancel() throws ExecutionException, InterruptedException
    {
        if (taskId == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.id"));
            return ERROR;
        }
        currentTaskDescriptor = taskManager.getTask(taskId);
        if (currentTaskDescriptor == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.found"));
            return ERROR;
        }
        final TaskContext context = currentTaskDescriptor.getTaskContext();
        if (!(context instanceof IndexTaskContext))
        {
            addErrorMessage(getText("common.tasks.wrong.task.context", IndexTaskContext.class.getName(), context.getClass().getName()));
            return ERROR;
        }

        taskManager.cancelTask(taskId);

        // We need to get the task descriptor again because the one we got before is just a shallow copy of the real thing.
        currentTaskDescriptor = taskManager.getTask(taskId);
        currentTask = taskBeanFactory.create(currentTaskDescriptor);

        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doRecover() throws Exception
    {
        if (isIndexing())
        {
            if (!isHasSystemAdminPermission())
            {
                addErrorMessage(getText("admin.errors.index.recovery.unauthorised"));
                return ERROR;
            }
            if (isTaskInProgress())
            {
                addErrorMessage(getCannotReindexInForegroundMessage());
                return ERROR;
            }

            String filePath = getRecoveryFilePath();
            validateRecoveryFile(filePath);
            if (invalidInput())
            {
                return ERROR;
            }

            Callable<IndexCommandResult> indexCallable = createIndexRecoveryCommand(filePath);

            final String taskName = getText("admin.indexing.jira.indexing");
            try
            {
                // re direct to progress action command
                final IndexTaskContext context = new ForegroundIndexTaskContext();
                return getRedirect(taskManager.submitTask(indexCallable, taskName, context, runInBackground()).getProgressURL());
            }
            catch (final AlreadyExecutingException e)
            {
                return getRedirect(e.getTaskDescriptor().getProgressURL());
            }
            catch (final RejectedExecutionException e)
            {
                addErrorMessage(getText("common.tasks.rejected.execution.exception", e.getMessage()));
                return ERROR;
            }
        }
        return getRedirect(getRedirectUrl());

    }

    private void validateRecoveryFile(final String path)
    {
        if (StringUtils.isBlank(path))
        {
            addError("recoveryFilename", getText("admin.index.recovery.file.error.not.specified"));
        }
        else
        {
            final File actualFile = fileFactory.getFile(path);

                if (!actualFile.exists())
                {
                    addError("recoveryFilename", getText("admin.index.recovery.file.error.not.found"));
                }

                if (actualFile.exists() && actualFile.isDirectory())
                {
                    addError("recoveryFilename", getText("admin.index.recovery.file.error.not.zip"));
                }
                else if (!actualFile.canRead())
                {
                    addError("indexPath", getText("admin.errors.path.entered.is.not.readable"));
                }
                else if (!indexRecoveryService.validIndexZipFile(getLoggedInApplicationUser(), actualFile))
                {
                    addError("recoveryFilename", getText("admin.index.recovery.file.error.not.zip"));
                }
        }
    }

    @RequiresXsrfCheck
    public String doCopy() throws Exception
    {
        if (isIndexing())
        {
            if (!isHasSystemAdminPermission())
            {
                addErrorMessage(getText("admin.errors.index.copy.unauthorised"));
                return ERROR;
            }
            if (isTaskInProgress())
            {
                addErrorMessage(getCannotReindexInForegroundMessage());
                return ERROR;
            }

            validateCopyParams();
            if (invalidInput())
            {
                return ERROR;
            }
            clusterManager.requestCurrentIndexFromNode(copyFromNodeId);

            copyRequested = true;
        }
        return SUCCESS;

    }

    private void validateCopyParams()
    {
        if (StringUtils.isBlank(copyFromNodeId))
        {
            addError("copyFromNodeId", getText("admin.errors.index.copy.from.not.specified"));
        }
        if (invalidInput())
        {
            return;
        }
        if (copyFromNodeId.equals(clusterManager.getNodeId()))
        {
            addError("copyFromNodeId", getText("admin.errors.index.copy.to.from.same"));
        }
    }

    private String getRedirectUrl()
    {
        return "IndexAdmin.jspa";
    }

    public long getReindexTime()
    {
        return reindexTime;
    }

    public void setReindexTime(final long reindexTime)
    {
        this.reindexTime = reindexTime;
    }

    public boolean isAnyLiveTasks()
    {
        return !taskManager.getLiveTasks().isEmpty();
    }

    public boolean isHasSystemAdminPermission()
    {
        return globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, getLoggedInUser());
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(final Long taskId)
    {
        this.taskId = taskId;
    }

    public TaskDescriptorBean<IndexCommandResult> getOurTask()
    {
        return currentTask;
    }

    public TaskDescriptorBean<IndexCommandResult> getCurrentTask()
    {
        if (currentTask == null)
        {
            final TaskDescriptor<IndexCommandResult> taskDescriptor = getCurrentTaskDescriptor();
            if (taskDescriptor != null)
            {
                currentTask = taskBeanFactory.create(taskDescriptor);
            }
        }
        return currentTask;
    }

    public String getIndexingStrategy()
    {
        return indexingStrategy;
    }

    public void setIndexingStrategy(String indexingStrategy)
    {
        this.indexingStrategy = indexingStrategy;
    }

    public boolean isIndexConsistent()
    {
        if (indexConsistent == null)
        {
            indexConsistent = indexLifecycleManager.isIndexConsistent();
        }
        return indexConsistent;
    }

    private boolean isReindexInProgress()
    {
        return false;
    }

    private TaskDescriptor<IndexCommandResult> getCurrentTaskDescriptor()
    {
        if (currentTaskDescriptor == null)
        {
            currentTaskDescriptor = taskManager.getLiveTask(new IndexTaskContext());
        }

        return currentTaskDescriptor;
    }

    public String getDestinationURL()
    {
        return "/secure/admin/jira/IndexAdmin.jspa?reindexTime=" + reindexTime;
    }

    public boolean isTaskInProgress()
    {
        if (taskInProgress == null)
        {
            taskInProgress = taskManager.findFirstTask(new TaskMatcher()
            {
                @Override
                public boolean match(final TaskDescriptor<?> descriptor)
                {
                    return !descriptor.isFinished() && descriptor.getTaskContext() instanceof IndexTask;
                }
            });
        }
        return taskInProgress != null;
    }

    public String getCannotReindexInForegroundMessage()
    {
        return getText("admin.indexing.strategy.foreground.other.task.in.progress");
    }

    public String getRecoveryFilename()
    {
        return recoveryFilename;
    }

    public String getRecoveryFilePath()
    {
        if (StringUtils.isEmpty(recoveryFilename) || new File(recoveryFilename).isAbsolute())
        {
            return recoveryFilename;
        }
        return PathUtils.appendFileSeparator(getSnapshotDirectory()) + recoveryFilename;
    }

    public String getSnapshotInterval()
    {
        final Long snapshotInterval = indexRecoveryService.getSnapshotInterval(getLoggedInApplicationUser());
        if (snapshotInterval != null)
        {
            IndexRecoveryUtil.Interval interval = IndexRecoveryUtil.intervalFromMillis(snapshotInterval);
            return IndexRecoveryUtil.getIntervalOption(interval, getI18nHelper());
        }
        else
        {
            return "";
        }
    }

    public String getSnapshotDirectory()
    {
        return new File(jiraHome.getExportDirectory(), "indexsnapshots").getAbsolutePath();
    }

    public void setRecoveryFilename(final String recoveryFilename)
    {
        this.recoveryFilename = recoveryFilename;
    }

    public boolean recoveryEnabled()
    {
        return indexRecoveryService.isRecoveryEnabled(getLoggedInApplicationUser());
    }

    public String getCopyFromNodeId()
    {
        return copyFromNodeId;
    }

    public void setCopyFromNodeId(final String copyFromNodeId)
    {
        this.copyFromNodeId = copyFromNodeId;
    }

    public Collection<Node> getNodeList()
    {
        return Collections2.filter(clusterManager.findLiveNodes(), new Predicate<Node>()
        {
            String nodeId = clusterManager.getNodeId();
            @Override
            public boolean apply(final Node input)
            {
                return !input.getNodeId().equals(nodeId);
            }
        });
    }

    public boolean isClustered()
    {
        return clusterManager.isClustered();
    }

    public String getCurrentNodeId()
    {
        return clusterManager.getNodeId();
    }

    public boolean isCopyRequested()
    {
        return copyRequested;
    }
}
