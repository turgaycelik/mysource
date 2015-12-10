package com.atlassian.jira.plugin.webwork;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.extension.Startable;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.seraph.util.PathMapper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This allows for the roles-required attribute to be used within plugins.
 *
 * @since v5.0
 */
public class WebworkPluginSecurityServiceHelper implements Startable
{
    private static final String ROLES_REQUIRED_ATTR = "roles-required";

    // This should be setup in the init from the params, but is extremely unlikely to change
    private static final String extension = "jspa";

    // Maps current action to roles required
    @ClusterSafe
    private final Map<String, String> rolesMap = new ConcurrentHashMap<String, String>();

    // used to check which actions match the current path
    // This class only uses the "get" method of the pathmapper, so initialise the map
    // that caches its results to a large value. The getAll method of the PathMapper is not used
    // by this class so make the second caching map small.
    private PathMapper actionMapper = new PathMapper();

    private final PluginAccessor pluginAccesor;

    private final EventPublisher eventPublisher;

    public WebworkPluginSecurityServiceHelper(PluginAccessor pluginAccesor, EventPublisher eventPublisher)
    {
        this.pluginAccesor = pluginAccesor;
        this.eventPublisher = eventPublisher;
    }

    @PluginEventListener
    public void onPluginModuleEnabled(final PluginModuleEnabledEvent event)
    {
        if (event.getModule() instanceof WebworkModuleDescriptor)
        {
            generatePathMaps();
        }
    }

    @PluginEventListener
    public void onPluginModuleDisabled(final PluginModuleDisabledEvent event)
    {
        if (event.getModule() instanceof WebworkModuleDescriptor)
        {
            generatePathMaps();
        }
    }

    /**
     * Generates the path -> required roles map for plugins
     * Uses the same method as the WebworkService
     */
    private void generatePathMaps()
    {
        // Clear the action mapper
        actionMapper = new PathMapper();
        // Clear the rolesMap
        rolesMap.clear();

        List<WebworkModuleDescriptor> webworkModuleDescriptors = pluginAccesor.getEnabledModuleDescriptorsByClass(WebworkModuleDescriptor.class);

        for (WebworkModuleDescriptor webworkModuleDescriptor : webworkModuleDescriptors)
        {
            Document pluginDocument = webworkModuleDescriptor.getWebworkDocument();
            final NodeList actions = pluginDocument.getElementsByTagName("action");

            final String rootRolesRequired = overrideRoles(null, pluginDocument.getDocumentElement());


            // Build list of views
            for (int i = 0; i < actions.getLength(); i++)
            {
                final Element action = (Element) actions.item(i);
                final String actionName = action.getAttribute("name");
                final String actionAlias = action.getAttribute("alias");
                final String actionRolesRequired = overrideRoles(rootRolesRequired, action);

                if (actionRolesRequired != null)
                {

                    if (actionAlias != null)
                    {
                        actionMapper.put(actionAlias, "/" + actionAlias + "." + extension);
                        rolesMap.put(actionAlias, actionRolesRequired);
                        actionMapper.put(actionAlias + "!*", "/" + actionAlias + "!*." + extension);
                        rolesMap.put(actionAlias + "!*", actionRolesRequired);
                    }

                    if (actionName != null)
                    {
                        actionMapper.put(actionName, "/" + actionName + "." + extension);
                        rolesMap.put(actionName, actionRolesRequired);
                        actionMapper.put(actionName + "!*", "/" + actionName + "!*." + extension);
                        rolesMap.put(actionName + "!*", actionRolesRequired);
                    }
                }
            }

        }
    }

    /**
     * Returns newRolesRequired if it isn't empty, and rolesRequired otherwise.
     */
    private String overrideRoles(final String rolesRequired, final Element action)
    {
        if (action.hasAttribute(ROLES_REQUIRED_ATTR))
        {
            return action.getAttribute(ROLES_REQUIRED_ATTR);
        }
        else
        {
            return rolesRequired;
        }
    }

    /**
     * This finds the required roles for a given URL.
     * Uses the same method as the WebworkService
     *
     * @param request
     * @return Set of required roles
     */
    public Set<String> getRequiredRoles(final HttpServletRequest request)
    {
        final Set<String> requiredRoles = new HashSet<String>();

        final String currentURL = request.getRequestURI();

        final int lastSlash = currentURL.lastIndexOf('/');
        String targetURL;

        // then check webwork mappings
        if (lastSlash > -1)
        {
            targetURL = currentURL.substring(lastSlash);
        }
        else
        {
            targetURL = currentURL;
        }

        final String actionMatch = actionMapper.get(targetURL);

        if (actionMatch != null)
        {
            final String rolesStr = rolesMap.get(actionMatch);

            final StringTokenizer st = new StringTokenizer(rolesStr, ", ");
            while (st.hasMoreTokens())
            {
                requiredRoles.add(st.nextToken());
            }
        }

        return Collections.unmodifiableSet(requiredRoles);
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
        generatePathMaps();
    }
}
