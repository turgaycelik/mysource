package com.atlassian.jira.gadgets.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.gadgets.system.util.BonfireLicenseChecker;
import com.atlassian.jira.gadgets.system.util.GreenhopperLicenseChecker;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.PluginAccessor;

/**
 * Manages the state of the tasks in the admin gadget.
 *
 * @since v6.0
 */
public class AdminTaskManager
{
    private final AdminTaskUserPropertyManager adminTaskUserPropertyManager;
    private final ProjectManager projectManager;
    private final UserUtil userUtil;
    private final PluginAccessor pluginAccessor;
    private final SearchService searchService;
    private final FeatureManager featureManager;
    private final BonfireLicenseChecker bonfireLicenseChecker;
    private final GreenhopperLicenseChecker greenhopperLicenseChecker;
    private final IssueManager issueManager;

    public AdminTaskManager(final AdminTaskUserPropertyManager adminTaskUserPropertyManager, final ProjectManager projectManager,
            final UserUtil userUtil, final PluginAccessor pluginAccessor, final SearchService searchService,
            final FeatureManager featureManager, final BonfireLicenseChecker bonfireLicenseChecker,
            final GreenhopperLicenseChecker greenhopperLicenseChecker, final IssueManager issueManager)
    {
        this.adminTaskUserPropertyManager = adminTaskUserPropertyManager;
        this.projectManager = projectManager;
        this.userUtil = userUtil;
        this.pluginAccessor = pluginAccessor;
        this.searchService = searchService;
        this.featureManager = featureManager;
        this.bonfireLicenseChecker = bonfireLicenseChecker;
        this.greenhopperLicenseChecker = greenhopperLicenseChecker;
        this.issueManager = issueManager;
    }

    /**
     * Set the named task list for the supplied user to be dismissed or not.
     */
    public void setTaskListDimissed(final User user, final String taskListName, final boolean dismissed)
    {
        adminTaskUserPropertyManager.setTaskListDimissed(user, taskListName, dismissed);
    }

    /**
     * Is the named task list disabled for the specified user?
     */
    public boolean isTaskListDismissed(final User user, final String taskListName)
    {
        return adminTaskUserPropertyManager.isTaskListDismissed(user, taskListName);
    }

    /**
     * Mark a task for a user as done (or not).
     */
    public void setTaskMarkedAsCompleted(final User user, final String taskName, final boolean done)
    {
        adminTaskUserPropertyManager.setTaskMarkedAsCompleted(user, taskName, done);
    }

    /**
     * If the task has been manually marked as completed or incomplete then use the stored value.
     * Otherwise, actually check the task.
     */
    public boolean isTaskCompleted(final User user, final String taskName, final Condition condition)
    {
        if (adminTaskUserPropertyManager.hasTaskCompletedProperty(user, taskName))
        {
            return adminTaskUserPropertyManager.isTaskMarkedAsCompleted(user, taskName) || condition.isDone();
        }
        else
        {
            return condition.isDone();
        }
    }

    /**
     * Is the named task enabled?
     */
    public boolean isTaskEnabled(final Condition condition)
    {
        return condition.isEnabled();
    }

    /**
     * Get the admin task lists for the supplied user.
     */
    public AdminTaskLists getAdminTaskLists(final User user)
    {
        final DoMoreTaskList doMoreTasks = createDoMoreTaskList(user);
        final GettingStartedTaskList gettingStartedTasks = createGettingStartedTaskList(user);
        return new AdminTaskLists(gettingStartedTasks, doMoreTasks);
    }

    /**
     * Build the 'do more' task list for the specified user.
     */
    private DoMoreTaskList createDoMoreTaskList(final User user)
    {
        final AdminTask tryGreenHopper = new AdminTask(isTaskCompleted(user, "greenhopper", new TaskCondition()
        {
            @Override
            public boolean isDone()
            {
                return greenhopperLicenseChecker.greenhopperIsActiveAndLicensed();
            }
        }));

        final AdminTask tryBonfire = new AdminTask(isTaskCompleted(user, "bonfire", new TaskCondition()
        {
            @Override
            public boolean isDone()
            {
                return bonfireLicenseChecker.bonfireIsActiveAndLicensed();
            }
        }));

        boolean isDoMoreListDismissed = isTaskListDismissed(user, "domore");
        return new DoMoreTaskList(tryGreenHopper, tryBonfire, isDoMoreListDismissed);
    }

    /**
     * Create the 'getting started' task list for the supplied user.
     */
    private GettingStartedTaskList createGettingStartedTaskList(final User user)
    {
        final AdminTask createProject = new AdminTask(isTaskCompleted(user, "createproject", new TaskCondition()
        {
            @Override
            public boolean isDone()
            {
                return !projectManager.getProjectObjects().isEmpty();
            }
        }));

        Condition createIssueCondition = new TaskCondition()
        {
            @Override
            public boolean isDone()
            {
                return hasAtLeastOneIssueBeenCreated(user);
            }

            @Override
            public boolean isEnabled()
            {
                return !projectManager.getProjectObjects().isEmpty();
            }
        };

        final AdminTask createIssue = new AdminTask(
                isTaskCompleted(user, "createissue", createIssueCondition),
                isTaskEnabled(createIssueCondition));

        final AdminTask createUser = new AdminTask(isTaskCompleted(user, "createuser", new TaskCondition()
        {
            @Override
            public boolean isDone()
            {
                // By default, OnDemand has both an admin and a sysadmin whereas BTF only has 1 user
                if (featureManager.isEnabled(CoreFeatures.ON_DEMAND))
                {
                    return userUtil.getActiveUserCount() > 2;
                }
                else
                {
                    return userUtil.getActiveUserCount() > 1;
                }
            }
        }));

        final AdminTask lookAndFeel = new AdminTask(isTaskCompleted(user, "lookandfeel", new TaskCondition()
        {
            @Override
            public boolean isDone()
            {
                return adminTaskUserPropertyManager.isLookAndFeelUpdated(user);
            }
        }));

        boolean isGettingStartedListDismissed = isTaskListDismissed(user, "gettingstarted");
        return new GettingStartedTaskList(createProject, createIssue, createUser, lookAndFeel, isGettingStartedListDismissed);
    }

    private boolean hasAtLeastOneIssueBeenCreated(final User user)
    {
        return issueManager.getIssueCount() > 0;
    }

    /**
     * Admin task lists.
     */
    @XmlRootElement
    public static class AdminTaskLists
    {
        @XmlElement
        GettingStartedTaskList gettingStarted;

        @XmlElement
        DoMoreTaskList doMore;

        public AdminTaskLists(GettingStartedTaskList gettingStarted, DoMoreTaskList doMore)
        {
            this.gettingStarted = gettingStarted;
            this.doMore = doMore;
        }
    }

    /**
     * The getting started task list.
     */
    @XmlRootElement
    public static class GettingStartedTaskList
    {
        @XmlElement
        AdminTask createProject;

        @XmlElement
        AdminTask createIssue;

        @XmlElement
        AdminTask createUser;

        @XmlElement
        AdminTask lookAndFeel;

        @XmlElement
        boolean isCompleted;

        @XmlElement
        boolean isDismissed;

        public GettingStartedTaskList(AdminTask createProject, AdminTask createIssue, AdminTask createUser, AdminTask lookAndFeel, boolean isDismissed)
        {
            this.createProject = createProject;
            this.createIssue = createIssue;
            this.createUser = createUser;
            this.lookAndFeel = lookAndFeel;
            this.isCompleted = createProject.isCompleted && createIssue.isCompleted && createUser.isCompleted && lookAndFeel.isCompleted;
            this.isDismissed = isDismissed;
        }
    }

    /**
     * The 'do more' task list.
     */
    @XmlRootElement
    public static class DoMoreTaskList
    {
        @XmlElement
        AdminTask tryGreenHopper;

        @XmlElement
        AdminTask tryBonfire;

        @XmlElement
        boolean isCompleted;

        @XmlElement
        boolean isDismissed;

        public DoMoreTaskList(AdminTask tryGreenHopper, AdminTask tryBonfire, boolean isDismissed)
        {
            this.tryGreenHopper = tryGreenHopper;
            this.tryBonfire = tryBonfire;
            this.isCompleted = tryGreenHopper.isCompleted && tryBonfire.isCompleted;
            this.isDismissed = isDismissed;
        }
    }

    /**
     * An Admin taks that can be completed.
     */
    @XmlRootElement
    public static class AdminTask
    {
        @XmlElement
        boolean isCompleted;

        @XmlElement
        boolean isEnabled;

        public AdminTask(boolean isCompleted)
        {
            this.isCompleted = isCompleted;
            this.isEnabled = true;
        }

        public AdminTask(boolean isCompleted, boolean isEnabled)
        {
            this.isCompleted = isCompleted;
            this.isEnabled = isEnabled;
        }
    }

    /**
     * A condition for a task indicating if something has been done or not and also an indicator that
     * the task is enabled to be done.
     */
    private static interface Condition
    {
        /**
         * Is the task this condition is applied to completed?
         * @return true if completed otherwise, false.
         */
        boolean isDone();

        /**
         * Is the task this condition is applied to enabled?
         * @return true if enabled otherwise, false.
         */
        boolean isEnabled();
    }

    /**
     * Base class for conditions.  Default state is that the task
     * is enabled.
     */
    private static abstract class TaskCondition implements Condition
    {
        /**
         * {@inheritDoc}
         */
        public abstract boolean isDone();

        /**
         * {@inheritDoc}
         * Default behaviour is that a task is enabled.
         */
        public boolean isEnabled()
        {
            return true;
        }
    }
}
