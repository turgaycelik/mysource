/*
 * WebWork, Web Application Framework
 *
 * Distributable under Apache license.
 * See terms of license at opensource.org
 */
package com.atlassian.jira.config.webwork;

import org.apache.commons.logging.LogFactory;
import webwork.action.Action;
import webwork.action.factory.ActionFactory;
import webwork.action.factory.ActionFactoryProxy;
import webwork.config.Configuration;

import java.util.Hashtable;
import java.util.Map;

/**
 * Aliasing action factory proxy. This will convert the given name to a classname of an Action implementation. By using
 * aliases you can give meaningful and easy-to-remember names to your action classes. Using aliases also allow you to
 * use the same action class with many result view mappings.
 * <p/>
 * If the configuration flag <code>"webwork.only.aliasing"</code> is set to "true", then the given name *must* be an
 * alias. Otherwise a security exception will be thrown. This is to ensure that only actions that you explicitly want
 * exposed for invocation can be accessed.
 * <p/>
 * However, actions can always access any other action through the ActionFactory regardless of this setting.
 *
 * @author Rickard \u00D6berg (rickard@middleware-company.com)
 * @version $Revision: 1.14 $
 */
public class JiraAliasingActionFactoryProxy extends ActionFactoryProxy
{
    // Attributes ----------------------------------------------------
    Map actionAliases = new Hashtable();
    boolean aliasingOnly;

    // Constructors --------------------------------------------------
    public JiraAliasingActionFactoryProxy(ActionFactory aFactory)
    {
        super(aFactory);

        try
        {
            aliasingOnly = Boolean.valueOf(Configuration.getString("webwork.aliasing.only")).booleanValue();
        }
        catch (IllegalArgumentException e)
        {
            // Ignore - hence default is false
        }
    }

    // ActionFactory overrides ---------------------------------------
    /**
     * Searches for the action from the configuration properties substituting the alias with the associated action and
     * then returns the matching action from the action factory proxy chain.  For the alias to match, it must be
     * specified with an <code>".action"</code> suffix.
     *
     * @param aName
     *
     * @return the action corresponding to the given alias
     *
     * @throws Exception
     */
    public Action getActionImpl(String aName)
            throws Exception
    {
        // Check cache
        String actionName = (String) actionAliases.get(aName);

        // Find class
        if (actionName == null)
        {
            try
            {
                // Get alias
                actionName = Configuration.getString(aName + "." +
                        Configuration.getString("webwork.action.extension"));
            }
            catch (IllegalArgumentException e)
            {
                // No alias for this name -> use as-is
                // Check if this is allowed
                LogFactory.getLog(getClass()).debug("Aliasing only:" + aliasingOnly);
                if (aliasingOnly)
                {
                    //TODO
                }
                actionName = aName;
            }
            actionAliases.put(aName, actionName);
        }

        return getNextFactory().getActionImpl(actionName);
    }

    public void flushCaches()
    {
        getNextFactory().flushCaches();
        actionAliases.clear();
    }
}
