package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.fugue.Option;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.util.profiling.UtilTimerStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This class is used to safely invoke methods in the IssueTabPanel and IssueTabPanel2 interfaces. When any of the
 * invoked methods throws an exception, this invoker will just log the exception and instead return an action that will
 * display a pretty error, so as not to kill the whole "Activity" block on the view issue page.
 *
 * @since v5.0
 */
public final class IssueTabPanelInvokerImpl implements IssueTabPanelInvoker
{
    private static final Logger log = LoggerFactory.getLogger(IssueTabPanelInvokerImpl.class);

    private final I18nHelper.BeanFactory i18Factory;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public IssueTabPanelInvokerImpl(I18nHelper.BeanFactory i18Factory, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.i18Factory = i18Factory;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public boolean invokeShowPanel(final ShowPanelRequest request, final IssueTabPanelModuleDescriptor descriptor)
    {
        final String timerName = "Calling showPanel for " + descriptor;
        UtilTimerStack.push(timerName);
        final Option<Boolean> showPanel = SafePluginPointAccess.call(new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                final IssueTabPanel3 issueTabPanel = descriptor.getModule();
                try
                {
                    return issueTabPanel.showPanel(request);
                }
                catch (AbstractMethodError e)
                {
                    // This could happen because we changed the getActions(Issue,User) in a binary-incompatible way, but the JVM
                    // only checks for binary compatibility of interface methods when the method is called. Hence we can get an
                    // AbstractMethodError here if the issue tab panel was compiled against a pre-Embedded Crowd version of
                    // JIRA.
                    log.error(formatBinaryCompatibilityMessage(issueTabPanel, "showPanel"), e);

                    // Show the tab anyway, and aim to show error in the tab
                    return true;
                }
            }
        });
        UtilTimerStack.pop(timerName);
        if (showPanel.isEmpty()) // non-fatal exception in plugin code
        {
            log.error("Exception thrown while trying to call showPanel() for " + descriptor);

            // Show the tab anyway, and aim to show error in the tab
            return true;
        }
        else
        {
            return showPanel.get();
        }
    }

    @Override
    public List<IssueAction> invokeGetActions(final GetActionsRequest request, final IssueTabPanelModuleDescriptor descriptor)
    {
        final String timerName = "Calling getActions in" + descriptor;
        UtilTimerStack.push(timerName);
        final Option<List<IssueAction>> actions = SafePluginPointAccess.call(new Callable<List<IssueAction>>()
        {
            @Override
            public List<IssueAction> call() throws Exception
            {
                final IssueTabPanel3 issueTabPanel = descriptor.getModule();

                try
                {
                    return issueTabPanel.getActions(request);
                }
                catch (AbstractMethodError e)
                {
                    // This could happen because we changed the getActions(Issue,User) in a binary-incompatible way, but the JVM
                    // only checks for binary compatibility of interface methods when the method is called. Hence we can get an
                    // AbstractMethodError here if the issue tab panel was compiled against a pre-Embedded Crowd version of
                    // JIRA.
                    log.error(formatBinaryCompatibilityMessage(issueTabPanel, "getActions"), e);

                    return Collections.<IssueAction>singletonList(new RenderingErrorAction(descriptor));
                }
            }
        });
        UtilTimerStack.pop(timerName);
        if (actions.isEmpty()) // non-fatal exception in plugin code
        {
            log.error("Exception thrown while trying to call getActions() for " + descriptor);
        }
        return actions.getOrElse(Collections.<IssueAction>singletonList(new RenderingErrorAction(descriptor)));
    }

    /*
     * Returns the binary compatibility message.
     */
    private String formatBinaryCompatibilityMessage(IssueTabPanel3 issueTabPanel, String methodName)
    {
        return String.format("%s does not implement IssueTabPanel.%s(com.atlassian.jira.issues.Issue, com.atlassian.crowd.embedded.api.User)."
                + " This likely means the plugin is not compatible with this version of JIRA.", methodName, issueTabPanel.getClass().getName());
    }

    /**
     * Displays an i18n'ed message like "Error rendering "blah blah". Please contact your administrator.".
     */
    class RenderingErrorAction implements IssueAction
    {
        private final IssueTabPanelModuleDescriptor moduleDescriptor;

        public RenderingErrorAction(IssueTabPanelModuleDescriptor moduleDescriptor)
        {
            this.moduleDescriptor = moduleDescriptor;
        }

        /**
         * @return an error message
         */
        @Override
        public String getHtml()
        {
            return i18Factory.getInstance(jiraAuthenticationContext.getUser()).getText("modulewebcomponent.exception", moduleDescriptor.getCompleteKey());
        }

        /**
         * @return the current date
         */
        @Override
        public Date getTimePerformed()
        {
            return new java.sql.Date(System.currentTimeMillis());
        }

        /**
         * @return true
         */
        @Override
        public boolean isDisplayActionAllTab()
        {
            return true;
        }
    }
}
