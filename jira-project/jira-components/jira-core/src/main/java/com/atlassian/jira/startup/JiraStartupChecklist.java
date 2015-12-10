package com.atlassian.jira.startup;


import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.jira.cluster.ClusterSafe;

/**
 * This class asserts that JIRA can start normally.
 * <p/>
 * Currently the checks include a number of database consistency checks, and checks that we have a valid jira.home.
 *
 * @since v4.0
 */
public class JiraStartupChecklist
{
    private static JiraStartupChecklist INSTANCE;

    /**
     * A reference to the JIRA startup state. The startup state encapsulates the startup checks that need to be
     * performed at a given state in JIRA's startup lifecycle (currently BOOTSTRAPPING->DELAYED->RUNNING).
     *
     * @see JiraStartupState
     */
    private final AtomicReference<JiraStartupState> startupState = new AtomicReference<JiraStartupState>();

    public JiraStartupChecklist()
    {
        // seed with the "bootstrapping" state
        startupState.set(new BootstrappingStartupState(startupState));
    }

    @ClusterSafe ("Program Artifact")
    public static synchronized JiraStartupChecklist getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new JiraStartupChecklist();
        }
        return INSTANCE;
    }


    /**
     * Returns true if JIRA started correctly. The first time this is called, it runs the checks, and then caches the
     * result.
     *
     * @return true if JIRA started correctly.
     */
    public static boolean startupOK()
    {
        return getInstance().startupState().isStartupChecksPassed();
    }

    /**
     * Returns the StartupCheck that failed, if any.
     *
     * @return the StartupCheck that failed, if any.
     */
    public static StartupCheck getFailedStartupCheck()
    {
        return getInstance().startupState().getFailedStartupCheck();
    }

    /**
     * Allows an external operation to declare that the startup failed, and give the message to be displayed in JIRA's
     * "JIRA is locked" web page.
     * <p/>
     * This is used in ComponentContainerLauncher, after the pre-startup checks have passed, but an error is thrown during
     * the actual startup.
     *
     * @param startupCheck The StartupCheck
     */
    public static void setFailedStartupCheck(StartupCheck startupCheck)
    {
        getInstance().startupState().setFailedStartupCheck(startupCheck);
    }

    /**
     * Called when JIRA is stopping and hence any startup check resources can be cleaned up
     */
    public static void stop()
    {
        getInstance().startupState().onJiraStopping();
    }

    /**
     * Returns the current startup state.
     *
     * @return a JiraStartupState
     */
    protected JiraStartupState startupState()
    {
        return startupState.get();
    }
}
