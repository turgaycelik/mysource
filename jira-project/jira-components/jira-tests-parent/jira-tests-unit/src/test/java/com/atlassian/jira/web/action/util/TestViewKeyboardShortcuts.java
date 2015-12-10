package com.atlassian.jira.web.action.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcut;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.security.JiraAuthenticationContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager.Context;
import static com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager.Operation;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestViewKeyboardShortcuts
{
    @Mock
    KeyboardShortcutManager keyboardShortcutManager;

    @Mock
    ComponentAccessor.Worker mockComponentAccessorWorker;

    @Mock
    JiraAuthenticationContext authenticationContext;

    private ViewKeyboardShortcuts viewKeyboardShortcuts;

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);

        ComponentAccessor.initialiseWorker(mockComponentAccessorWorker);
        when(mockComponentAccessorWorker.getComponent(JiraAuthenticationContext.class)).thenReturn(authenticationContext);

        viewKeyboardShortcuts = new ViewKeyboardShortcuts(keyboardShortcutManager, authenticationContext);
    }

    @Test
    public void testGetShortcutsForContext() throws Exception
    {
        final KeyboardShortcut globalShortcut = createKeyboardShortcut(Context.global, "g", "g");
        final KeyboardShortcut adminShortcut = createKeyboardShortcut(Context.admin, "g", "a");

        when(keyboardShortcutManager.listActiveShortcutsUniquePerContext(anyMap())).thenReturn(Lists.newArrayList(globalShortcut, adminShortcut));

        final Map<KeyboardShortcutManager.Context, List<KeyboardShortcut>> shortcutContexts = viewKeyboardShortcuts.getShortcutsForContext();
        assertThat(shortcutContexts.size(), is(notNullValue()));
        assertThatListHasOnlyOneItem(shortcutContexts.get(Context.global), globalShortcut);
        assertThatListHasOnlyOneItem(shortcutContexts.get(Context.admin), adminShortcut);
    }

    private KeyboardShortcut createKeyboardShortcut(final Context context, final String... keys)
    {
        return new KeyboardShortcut("", context, Operation.execute, "alert('shortcut');", 100, keys(keys), "i18n_key", false);
    }

    static Set<List<String>> keys(String... keys)
    {
        List<String> strings = Lists.newArrayList(keys);

        //noinspection unchecked
        return Sets.<List<String>>newHashSet(strings);
    }

    private static void assertThatListHasOnlyOneItem(List<KeyboardShortcut> globalShortcuts, KeyboardShortcut globalShortcut)
    {
        assertThat(globalShortcuts, is(notNullValue()));
        assertThat(globalShortcuts.size(), is(1));
        assertThat(globalShortcuts, hasItem(globalShortcut));
    }
}
