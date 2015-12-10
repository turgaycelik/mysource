package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.MappingResult;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.util.ErrorCollection;
import webwork.action.ActionContext;

/**
 * Data object that is stored in the HTTP Session and used in various stages of the Project Import Wizard.
 *
 * @since v3.13
 */
public class ProjectImportBean
{
    private static final String SESSION_KEY = "project.import.bean";

    /**
     * This helper method is used to get the ProjectImportBean from this user's HTTP session.
     * <p>
     * If the bean does not already exist in the session, then an empty one is created, stored in the session and returned.
     * </p>
     * @return the ProjectImportBean from this user's HTTP session.
     */
    static ProjectImportBean getProjectImportBeanFromSession()
    {
        ProjectImportBean bean = (ProjectImportBean) ActionContext.getSession().get(ProjectImportBean.SESSION_KEY);
        if (bean == null)
        {
            bean = new ProjectImportBean();
            ActionContext.getSession().put(ProjectImportBean.SESSION_KEY, bean);
        }
        return bean;
    }

    /**
     * This helper method is used to clean the ProjectImportBean from this user's HTTP session.
     */
    static void clearFromSession()
    {
        final ProjectImportBean projectImportBean = (ProjectImportBean) ActionContext.getSession().remove(ProjectImportBean.SESSION_KEY);
        if (projectImportBean != null)
        {
            projectImportBean.deleteTempFiles();
        }
    }

    private ProjectImportOptionsImpl projectImportOptions;
    private BackupOverview backupOverview;
    private ProjectImportData projectImportData;
    private MappingResult mappingResult;
    private ProjectImportResults projectImportResults;
    private TaskProgressInformation taskProgressInformation;

    private ProjectImportBean()
    {}

    /**
     * ProjectImportOptions are the very first thing we collect from the users. This holds all the
     * data the the user has input from the screen, from both Step 1 and Step 2 of the import.
     *
     * @return the user inputted options
     */
    public ProjectImportOptionsImpl getProjectImportOptions()
    {
        return projectImportOptions;
    }

    /**
     * This should be called from the doExecute of Step 1 when we have collected the backup path to the XML backup
     * and the attachments.
     *
     * @param projectImportOptions contains the backup path and the attachments path
     */
    public void setProjectImportOptions(final ProjectImportOptionsImpl projectImportOptions)
    {
        this.projectImportOptions = projectImportOptions;
    }

    /**
     * This is the end result of the processing of Step 1. This is used to display the project select information
     * on the Step 2 screen (project select). This is how you get hold of a {@link com.atlassian.jira.imports.project.core.BackupProject}
     * which is required to move to the next step in the import.
     *
     * @return a BackupOverview containing all the "project" information about the projects that exist in the provided
     * backup file.
     */
    public BackupOverview getBackupOverview()
    {
        return backupOverview;
    }

    /**
     * This is set as the end result of processing Step 1. Once we have processed the backup XML file we store this
     * result here.
     *
     * NOTE: if this object exists then the user should be able to resume the import at Step 2.
     *
     * @param backupOverview contains all the project and system information gleaned from the backup XML.
     */
    public void setBackupOverview(final BackupOverview backupOverview)
    {
        this.backupOverview = backupOverview;
    }

    /**
     * This is the saved result of processing Step 2. This contains all the registered and required values that are
     * used in the selected project and the paths to the partitioned XML files that contain the projects data.
     *
     * This object is validated to create a {@link com.atlassian.jira.imports.project.core.MappingResult} which is used
     * to display the validation messages on the summary screen.
     *
     * This object can be used to create many instances of the MappingResult (which may be different if the user has
     * changed the state of the current instance of JIRA).
     *
     * This object is the NON-CHANGING data from Step 2.
     *
     * @return the projectImportData which can be used to create a mapping result
     */
    public ProjectImportData getProjectImportData()
    {
        return projectImportData;
    }

    /**
     * This is set as the result of moving from Step 2 to Step 3.
     *
     * @param projectImportData the NON-CHANGING data from Step 2.
     */
    public void setProjectImportData(final ProjectImportData projectImportData)
    {
        this.projectImportData = projectImportData;
    }

    /**
     * This is the result of mapping and validating the {@link com.atlassian.jira.imports.project.core.ProjectImportData}.
     * This is used to display Step 3 (pre-import summary screen). Everytime the pre-import summary screen is shown
     * this object will be recalculated.
     *
     * @return contains the results of mapping and validating the ProjectImportData
     */
    public MappingResult getMappingResult()
    {
        return mappingResult;
    }

    /**
     * This is set as the result of mapping and validating the ProjectImportData and should be set every time we
     * show the Step 3 (pre-import summary) screen.
     *
     * @param mappingResult contains the error and warning information that was generated from the mapping and validation
     */
    public void setMappingResult(final MappingResult mappingResult)
    {
        this.mappingResult = mappingResult;
    }

    /**
     * This is set once we have performed the actual import. This will let us know if the import was successful
     * or not and how much was created.
     * @return results of the import
     */
    public ProjectImportResults getProjectImportResults()
    {
        return projectImportResults;
    }

    /**
     * This should be set once the import has completed.
     * @param projectImportResults the results of how the import when
     */
    public void setProjectImportResults(final ProjectImportResults projectImportResults)
    {
        this.projectImportResults = projectImportResults;
    }

    public TaskProgressInformation getTaskProgressInformation()
    {
        if (taskProgressInformation == null)
        {
            taskProgressInformation = new TaskProgressInformation();
        }
        return taskProgressInformation;
    }

    /**
     * This is a convenience method that looks up the selected project if there is one. If the information has
     * not been set this will return null.
     *
     * @return the selected project, if there is one, null otherwise
     */
    public BackupProject getSelectedProject()
    {
        final ProjectImportOptionsImpl projectImportOptions = getProjectImportOptions();
        if (projectImportOptions != null)
        {
            final String selectedProjectKey = projectImportOptions.getSelectedProjectKey();
            if ((selectedProjectKey != null) && (getBackupOverview() != null))
            {
                return getBackupOverview().getProject(selectedProjectKey);
            }
        }
        return null;
    }

    private void deleteTempFiles()
    {
        if (projectImportData != null)
        {
            projectImportData.getTemporaryFiles().deleteTempFiles();
        }
    }

    /**
     * We want to try to make sure the temporary XML files are deleted, even if the session times out.
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        deleteTempFiles();
    }

    /**
     * Used to communicate errors and task id between task progress actions and their destinations. This does not
     * hold any REAL project import information, only action based errors.
     */
    static class TaskProgressInformation
    {
        private ErrorCollection errorCollection;
        private Long taskId;

        /**
         * Gets an ErrorCollection used by a long running task.
         *
         * @return ErrorCollection being populated by the long running task.
         */
        public ErrorCollection getErrorCollection()
        {
            return errorCollection;
        }

        /**
         * Saves an ErrorCollection that is being used by a long-running task.
         * @param errorCollection The ErrorCollection
         */
        public void setErrorCollection(final ErrorCollection errorCollection)
        {
            this.errorCollection = errorCollection;
        }

        /**
         * Gets the ID of the current long-running task for this Import Wizard.
         * @return the ID of the current long-running task for this Import Wizard.
         */
        public Long getTaskId()
        {
            return taskId;
        }

        /**
         * Set the ID of the long running task we have just started for this Project Import Wizard.
         * @param taskId The ID of the long running task we have just started for this Project Import Wizard.
         */
        public void setTaskId(final Long taskId)
        {
            this.taskId = taskId;
        }
    }
}
