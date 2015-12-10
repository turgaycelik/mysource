package com.atlassian.jira.web.action.util;

import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcut;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.plugin.webfragment.DefaultWebFragmentContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays a help screen showing all the available keyboard shortcuts and descriptions telling users what a particular
 * shortcut will do.
 *
 * @since v4.1
 */
public class ViewKeyboardShortcuts extends JiraWebActionSupport
{
    private final KeyboardShortcutManager keyboardShortcutManager;
    private final JiraAuthenticationContext authenticationContext;

    private boolean keyboardShortcutsEnabled;

    public ViewKeyboardShortcuts(final KeyboardShortcutManager keyboardShortcutManager, final JiraAuthenticationContext authenticationContext)
    {
        this.keyboardShortcutManager = keyboardShortcutManager;
        this.authenticationContext = authenticationContext;
    }

    public Map<KeyboardShortcutManager.Context, List<KeyboardShortcut>> getShortcutsForContext()
    {
        final Map<String, Object> webContext = DefaultWebFragmentContext.get();
        final List<KeyboardShortcut> allShortcuts = keyboardShortcutManager.listActiveShortcutsUniquePerContext(webContext);
        final Map<KeyboardShortcutManager.Context, List<KeyboardShortcut>> ret = new LinkedHashMap<KeyboardShortcutManager.Context, List<KeyboardShortcut>>();
        for (KeyboardShortcutManager.Context context : KeyboardShortcutManager.Context.values())
        {
            ret.put(context, getShortcuts(context, allShortcuts));
        }
        return ret;
    }

    public String getContextName(final KeyboardShortcutManager.Context context)
    {
        return getText("keyboard.shortcut.context." + context.toString());
    }

    public I18nHelper getI18nHelper()
    {
        return authenticationContext.getI18nHelper();
    }

    public String[] getFormSubmitKeys()
    {
        final String modifierKeys = BrowserUtils.getModifierKey() + "+" + getText("common.forms.submit.accesskey");
        return modifierKeys.split("\\+");
    }

    private List<KeyboardShortcut> getShortcuts(final KeyboardShortcutManager.Context context, final List<KeyboardShortcut> shortcuts)
    {
        return new ArrayList<KeyboardShortcut>(CollectionUtil.filter(shortcuts, new Predicate<KeyboardShortcut>()
        {
            public boolean evaluate(final KeyboardShortcut input)
            {
                return context.equals(input.getContext());
            }
        }));
    }

    public boolean getKeyboardShortcutsEnabled()
    {
        return !getUserPreferences().getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED);
    }

    public void setKeyboardShortcutsEnabled(final boolean keyboardShortcutsEnabled)
    {
            this.keyboardShortcutsEnabled = keyboardShortcutsEnabled;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (getLoggedInUser() != null)
        {
            getUserPreferences().setBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED, !keyboardShortcutsEnabled);
        }

        if (isInlineDialogMode())
        {
            return returnComplete();
        }
        
        return super.doExecute();
    }

    public String doDefault() throws Exception
    {
        setKeyboardShortcutsEnabled(!getUserPreferences().getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED));
        return Action.INPUT;        
    }

    /**
     * Gets the available list of options for the Keyboard shortcut preference
     *
     * @return the available list of options for the keyboard shortcut Default preference
     */
    public Collection<TextOption> getKeyboardShortcutList()
    {
        final String enabledText = getText("preferences.keyboard.shortcuts.enabled");
        final String disabledText = getText("preferences.keyboard.shortcuts.disabled");

        return CollectionBuilder.list(new TextOption("true", enabledText),
                new TextOption("false", disabledText));
    }

    public boolean isUserLoggedIn()
    {
        return (getLoggedInUser() != null);
    }
}
