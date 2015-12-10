package com.atlassian.jira.plugin.keyboardshortcut;

import com.atlassian.annotations.PublicApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a registry of keyboard shortcuts currently available in JIRA. Keyboard shortcuts can be provided via the
 * {@link KeyboardShortcut} plugin point. In order for keyboard shortcuts to remain consistent across a cluster, they
 * should be added or removed simultaneously across the cluster, e.g. upon startup, not at arbitrary times such as in
 * response to user input.
 *
 * @since v4.1
 */
@PublicApi
public interface KeyboardShortcutManager
{
    /**
     * Given a complete plugin module key, this method registers a keyboard shortcut with the manager, which will make
     * it available in the UI to end-users.
     *
     * @param pluginModuleKey the complete plugin module key for this keyboard shortcut
     * @param shortcut the shortcut to register
     */
    void registerShortcut(final String pluginModuleKey, final KeyboardShortcut shortcut);

    /**
     * Removes a keyboard shortcut by its complete plugin module key.
     *
     * @param pluginModuleKey the key to remove
     */
    void unregisterShortcut(final String pluginModuleKey);

    /**
     * Returns an ordered list of <em>active</em> <code>KeyboardShortcut</code>s, sorted using the plugin descriptor's
     * <code>order</code> attribute.
     * <p/>
     * A shortcut is active if it has not been overriden by another shortcut that uses the same key combination and has
     * a greater value in its <code>order</code> attribute (see the <a href="https://developer.atlassian.com/display/JIRADEV/Keyboard+Shortcut+Plugin+Module">keyboard shortcut plugin module documentation</a>).
     *
     * @return ordered list of all active keyboard shortcuts
     * @since 5.0.2
     *
     * @deprecated uses {@link #listActiveShortcutsUniquePerContext} under the hood
     */
    @Deprecated
    List<KeyboardShortcut> getActiveShortcuts();

    /**
     * Returns an ordered list of <em>active</em> <code>KeyboardShortcut</code>s, sorted using the plugin descriptor's
     * <code>order</code> attribute.
     * <p/>
     * A shortcut is considered active if
     * <ol>
     *     <li>All conditions attached to the keyboard shortcut are fulfilled, so that the current user is allowed to
     *         use it.
     *     </li>
     *     <li>It has not been overriden by another shortcut of the same {@link KeyboardShortcutManager.Context} that
     *         uses the same key combination and has a greater value in its <code>order</code> attribute (see the
     *         <a href="https://developer.atlassian.com/display/JIRADEV/Keyboard+Shortcut+Plugin+Module">keyboard
     *         shortcut plugin module documentation</a>). Thus this list can contain multiple shortcuts with the same
     *         key combination, which all belong to a different context.
     *     </li>
     * </ol>
     * <p/>
     * Be aware, that this operation is <em>potentially expensive</em>, depending on the number and type of conditions
     * attached to the keyboard shortcuts. So store the result in favour of recalculating it.
     *
     * @param userContext the context to be used when evaluating, whether the user is allowed to use the shortcut; not
     * <code>null </code>
     * @return ordered list of all active keyboard shortcuts, no duplicate shortcuts per {@link KeyboardShortcutManager.Context}; not <code>null</code>
     * @since 5.0.4
     */
    List<KeyboardShortcut> listActiveShortcutsUniquePerContext(Map<String, Object> userContext);

    /**
     * Returns an ordered list of all {@link com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcut}s available
     * using the plugin descriptor's 'order' attribute for sorting.  Implementations should take care to implement this
     * method as quickly as possible since it will be called very often.
     * <p/>
     * Note <b>this method returns all registered shortcuts</b>, even if they are not in effect (e.g. they have been
     * overridden). This means that this method may return duplicate shortcuts.
     *
     * @return ordered list of all registered keyboard shortcuts
     * @see #getActiveShortcuts()
     */
    List<KeyboardShortcut> getAllShortcuts();

    /**
     * Register that the keyboard shortcuts for the context should be included by {@link #includeShortcuts()} (in
     * addition to the {@link Context#global global} shortcuts, which are always included).
     *
     * @param context the context whose keyboard shorcuts should be included (in addition to {@link Context#global global}).
     */
    void requireShortcutsForContext(Context context);

    /**
     * Returns a URL to be used to include the currently registered keyboard shortcuts in a page.  The URL should point
     * to the Keyboard Shortcut REST resource. Generally this URL will look something like:
     * <code>/rest/api/1.0/shortcuts/500/f1e3b05a6e6db41e5b77fb8afd7ddde9/shortcuts.js</code>
     * <p/>
     * It has been suggested that webresources (with say the systemcounter) would be a good fit for this, however the
     * problem is that the systemcounter would have to be updated everytime a plugin is enabled/disabled (so everytime
     * JIRA is started up), which would have some adverse side effects: <ul> <li>The system counter would get really
     * large eventually</li> <li>All webresources would no longer be cached after a  restart of JIRA and would have to
     * be downloaded again.</li> </ul> That's why this uses a md5 checksum of the shortcuts in the URL instead of the
     * web-resources approach.
     * <p/>
     * It does not contain the applications context path.  Also, the hash included at the end of the URL should be a
     * hash of all keyboard shortcuts currently available such that the resource can be cached indefinitely.  If a new
     * shortcut is registered (or an old shortcut unregistered), then the hash should obviously change.
     *
     * @return URL pointing to a REST resource to included keyboard shortcuts. The URL will be correctly
     * URL-escaped, but it is not HTML escaped.
     * @see #requireShortcutsForContext(com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager.Context)
     */
    String includeShortcuts();

    /**
     * Returns if the user has keyboard shortcuts enabled. If the user is not logged in this will default to true
     *
     * @return boolean indicating if the user has keyboard shortcuts enabled. Defaults to true if the user is not
     * logged in.
     */
    boolean isKeyboardShortcutsEnabled();

    /**
     * Defines the context under which a keyboard shortcut is valid.  Some shortcuts will only be availble on the view
     * issue screen.  'global' is the default context if none is specified.
     */
    static enum Context
    {
        global, issuenavigation, issueaction, greenhopper, admin;

        private static final Map<String, Context> stringToEnum = new HashMap<String, Context>();
        static
        {
            for (Context context : values())
            {
                stringToEnum.put(context.toString(), context);
            }
        }
        
        public static Context fromString(String contextName)
        {
            return stringToEnum.get(contextName);
        }

    }

    /**
     * Defines the different operations that can be taken using a {@link com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcut}.
     * Operations are always coupled with a parameter which will generally be a jQuery selector or URL.  The parameter
     * can however also be more generic when coupled with the 'execute' operation where a generic javascript function
     * can be specified as well.
     */
    static enum Operation
    {
        /**
         * Clicks an element identified by a jQuery selector
         */
        click,
        /**
         * Changes the window.location to go to a specified location
         */
        goTo,
        /**
         * Sets the window.location to the href value of the link specified by the jQuery selector.
         */
        followLink,
        /**
         * Scrolls the window to a specific element and focuses that element using a jQuery selector
         */
        moveToAndFocus,

        /**
         * Scrolls the window to an element and clicks that elment using a jQuery selector
         */
        moveToAndClick,

        /**
         * Scrolls to and adds <em>focused</em> class to the next item in the jQuery collection
         */
        moveToNextItem,

        /**
         * Scrolls to and adds <em>focused</em> class to the previous item in the jQuery collection
         */
        moveToPrevItem,

        /**
         * Executes a javascript functions specified by the operation parameter
         */
        execute,

        /**
         * Evaluates javascript on page load. Execute above will execute the javascript provided when the keyboard
         * shortcut is pressed.
         */
        evaluate
    }
}
