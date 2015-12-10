package com.atlassian.jira.rest.v1.keyboardshortcuts;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcut;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager.Context;
import static com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager.Operation;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class KeyboardShortcutResourceTest
{
    private static final KeyboardShortcut GLOBAL_SHORTCUT = createKeyboardShortcut(Context.global, 100, "g", "g");
    private static final KeyboardShortcut ADMIN_SHORTCUT = createKeyboardShortcut(Context.admin, 100, "g", "a");
    private static final KeyboardShortcut ISSUE_ACTION_SHORTCUT = createKeyboardShortcut(Context.issueaction, 100, "g", "i");
    private static final KeyboardShortcut CREATE_ISSUE_GLOBAL_SHORTCUT = createKeyboardShortcut(Context.global, 100, "c");
    private static final KeyboardShortcut CREATE_ISSUE_GREENHOPPER_SHORTCUT = createKeyboardShortcut(Context.greenhopper, 200, "c");

    @Mock
    KeyboardShortcutManager keyboardShortcutManager;

    @Mock
    ComponentAccessor.Worker mockComponentAccessorWorker;

    @Mock
    JiraAuthenticationContext mockJiraAuthenticationContext;

    private KeyboardShortcutResource keyboardShortcutResource;

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);

        ComponentAccessor.initialiseWorker(mockComponentAccessorWorker);
        when(mockComponentAccessorWorker.getComponent(JiraAuthenticationContext.class)).thenReturn(mockJiraAuthenticationContext);

        keyboardShortcutResource = new KeyboardShortcutResource(keyboardShortcutManager);
    }

    @Test
    public void testGetKeyboardShortcutsForNullContextNames() throws Exception
    {
        whenKeyboardShortcutManagerListActiveShortcutsPerContextThenReturn(GLOBAL_SHORTCUT, ADMIN_SHORTCUT);

        // null represents global context only
        final List<KeyboardShortcut> shortcuts = keyboardShortcutResource.getKeyboardShortcutsFor(null);
        assertThat(shortcuts.size(), equalTo(1));
        assertThat(shortcuts, hasItem(GLOBAL_SHORTCUT));
    }

    @Test
    public void testGetKeyboardShortcutsForEmptySetOfContextNames() throws Exception
    {
        whenKeyboardShortcutManagerListActiveShortcutsPerContextThenReturn(GLOBAL_SHORTCUT, ADMIN_SHORTCUT);

        // global context is always included
        final List<KeyboardShortcut> shortcuts = keyboardShortcutResource.getKeyboardShortcutsFor(Collections.<String>emptySet());
        assertThat(shortcuts.size(), equalTo(1));
        assertThat(shortcuts, hasItem(GLOBAL_SHORTCUT));
    }

    @Test
    public void testGetKeyboardShortcutsForUnknownContextName() throws Exception
    {
        whenKeyboardShortcutManagerListActiveShortcutsPerContextThenReturn(GLOBAL_SHORTCUT, ADMIN_SHORTCUT);

        // global context is always included
        final List<KeyboardShortcut> shortcuts = keyboardShortcutResource.getKeyboardShortcutsFor(Collections.singleton("you-dont-know-me"));
        assertThat(shortcuts.size(), equalTo(1));
        assertThat(shortcuts, hasItem(GLOBAL_SHORTCUT));
    }

    @Test
    public void testGetKeyboardShortcutsForGlobalContextNameOnly() throws Exception
    {
        whenKeyboardShortcutManagerListActiveShortcutsPerContextThenReturn(GLOBAL_SHORTCUT, ADMIN_SHORTCUT);

        final List<KeyboardShortcut> shortcuts = keyboardShortcutResource.getKeyboardShortcutsFor(Collections.singleton(Context.global.name()));
        assertThat(shortcuts.size(), equalTo(1));
        assertThat(shortcuts, hasItem(GLOBAL_SHORTCUT));
    }

    @Test
    public void testGetKeyboardShortcutsForAdminContextNameOnly() throws Exception
    {
        whenKeyboardShortcutManagerListActiveShortcutsPerContextThenReturn(GLOBAL_SHORTCUT, ADMIN_SHORTCUT);

        final List<KeyboardShortcut> shortcuts = keyboardShortcutResource.getKeyboardShortcutsFor(Collections.singleton(Context.admin.name()));
        assertThat(shortcuts.size(), equalTo(2));
        assertThat(shortcuts, hasItem(GLOBAL_SHORTCUT));
        assertThat(shortcuts, hasItem(ADMIN_SHORTCUT));
    }

    @Test
    public void testGetKeyboardShortcutsForMultipleContextNamesWithoutAdmin() throws Exception
    {
        whenKeyboardShortcutManagerListActiveShortcutsPerContextThenReturn(GLOBAL_SHORTCUT, ADMIN_SHORTCUT, ISSUE_ACTION_SHORTCUT);

        final List<KeyboardShortcut> shortcuts = keyboardShortcutResource.getKeyboardShortcutsFor(Sets.newHashSet(Context.issuenavigation.name(), Context.issueaction.name()));
        assertThat(shortcuts.size(), equalTo(2));
        assertThat(shortcuts, hasItem(GLOBAL_SHORTCUT));
        assertThat(shortcuts, hasItem(ISSUE_ACTION_SHORTCUT));
    }

    @Test
    public void testGetKeyboardShortcutsForMultipleContextEliminateDuplicates() throws Exception
    {
        // order is important, greenhopper has higher order - thus the greenhopper should win
        whenKeyboardShortcutManagerListActiveShortcutsPerContextThenReturn(CREATE_ISSUE_GLOBAL_SHORTCUT, CREATE_ISSUE_GREENHOPPER_SHORTCUT);

        final List<KeyboardShortcut> shortcuts = keyboardShortcutResource.getKeyboardShortcutsFor(Sets.newHashSet(Context.global.name(), Context.greenhopper.name()));
        assertThat(shortcuts.size(), equalTo(1));
        assertThat(shortcuts, hasItem(CREATE_ISSUE_GREENHOPPER_SHORTCUT));
    }

    private static KeyboardShortcut createKeyboardShortcut(final Context context, int order, final String... keys)
    {
        return new KeyboardShortcut("", context, Operation.execute, "alert('shortcut');", order, keys(keys), "i18n_key", false);
    }

    static Set<List<String>> keys(String... keys)
    {
        List<String> strings = Lists.newArrayList(keys);

        //noinspection unchecked
        return Sets.<List<String>>newHashSet(strings);
    }

    private void whenKeyboardShortcutManagerListActiveShortcutsPerContextThenReturn(final KeyboardShortcut... keyboardShortcuts)
    {
        when(keyboardShortcutManager.listActiveShortcutsUniquePerContext(anyMap())).thenReturn(Lists.newArrayList(keyboardShortcuts));
    }
}
