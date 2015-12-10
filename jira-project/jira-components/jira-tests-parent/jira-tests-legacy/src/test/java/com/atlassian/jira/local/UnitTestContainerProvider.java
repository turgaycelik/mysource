package com.atlassian.jira.local;

import com.atlassian.jira.extension.ContainerProvider;
import com.atlassian.jira.permission.DefaultPermissionSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.PluginLoaderFactory;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.ThreadLocalCachingPermissionManager;
import com.atlassian.jira.security.WorkflowBasedPermissionManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugin.loaders.PluginLoader;
import com.atlassian.plugin.loaders.SinglePluginLoader;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import org.apache.log4j.Logger;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import java.util.List;

/**
 * A {@link ContainerProvider} which creates a custom PicoContainer for the unit tests.
 * <p/>
 * In particular, it replaces {@link WorkflowBasedPermissionManager} with {@link DefaultPermissionManager}, thus avoiding
 * having the permissions system depend on the workflow system. Think of it as mocking out a whole lot of stuff we don't
 * want to test :)
 */
public class UnitTestContainerProvider implements ContainerProvider
{
    private MutablePicoContainer container;
    private static final Logger log = Logger.getLogger(UnitTestContainerProvider.class);

    public MutablePicoContainer getContainer(final PicoContainer parent)
    {
        checkTheStateOfTheWorld();

        if (container == null)
        {
            buildContainer(parent);
        }
        return container;
    }

    private void buildContainer(final PicoContainer parent)
    {
        container = (MutablePicoContainer) parent;
        if (!ThreadLocalCachingPermissionManager.class.equals(container.getComponentAdapter(PermissionManager.class).getComponentImplementation()))
        {
            // Sanity check: the whole purpose of this class is to avoid use of WorkflowBasedPermissionManager. Check that it is actually being
            // used; if not, we might be able to get rid of UnitTestContainerProvider altogether
            throw new RuntimeException(
                "Something wrong with environment! Expected to find a WorkflowBasedPermissionManager instance for PermissionManager interface, but instead got a " + container.getComponentAdapter(
                    PermissionManager.class).getComponentImplementation());
        }

        // Throw out the WorkflowBasedPermissionManager
        container.removeComponent(PermissionManager.class);
        container.removeComponent(PermissionSchemeManager.class);
        container.removeComponent(PluginLoaderFactory.class);
        container.removeComponent(OsgiContainerManager.class);
        // .. and switch in the old DefaultPermissionManager (which WorkflowBasedPermissionManager extends).
        container.addComponent(PermissionManager.class, DefaultPermissionManager.class);
        container.addComponent(PermissionSchemeManager.class, DefaultPermissionSchemeManager.class);
        container.addComponent(PluginLoaderFactory.class, EmptyPluginLoaderFactory.class);
    }

    private void checkTheStateOfTheWorld()
    {
        if (!LegacyJiraMockTestCase.jiraMockTestCaseHasBeenRun)
        {
            // This means that we have been asked to bring up the PICO world however a JiraMockTestcase has not been run yet
            // This in turns that either a plain old TestCase is accessing PICO unintentionally or a JiraMockTestcase constructor
            // is accessing PICO.  Either way its bad news!
            log.fatal("A TestCase is accessing PICO too early!", new RuntimeException("PICO ACCESSED TOO EARLY!"));
        }
    }

    public static class EmptyPluginLoaderFactory implements PluginLoaderFactory
    {
        // need the system text renderer
        // warning - do not add more loaders here!  It _drastically_ increases the time it takes to run tests
        private static final List<PluginLoader> PLUGIN_LOADERS = CollectionBuilder.<PluginLoader>list(new SinglePluginLoader("system-renderers-plugin.xml"));

        public List<PluginLoader> getPluginLoaders()
        {
            return PLUGIN_LOADERS;

        }
    }
}
