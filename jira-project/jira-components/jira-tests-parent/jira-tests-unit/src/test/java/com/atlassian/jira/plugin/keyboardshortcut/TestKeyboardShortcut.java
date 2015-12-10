package com.atlassian.jira.plugin.keyboardshortcut;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.util.I18nHelper;

import org.junit.Test;

import static com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager.Context;
import static com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager.Operation;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class TestKeyboardShortcut
{
    @Test
    public void testPrettyShortcutOneLetter()
    {
        final I18nHelper mockI18nHelper = createMock(I18nHelper.class);

        replay(mockI18nHelper);
        final Set<List<String>> keys = new LinkedHashSet<List<String>>();
        keys.add(Arrays.asList("g"));
        KeyboardShortcut shortcut = new KeyboardShortcut("", Context.global, Operation.click, "", 10, keys, null, false);
        String prettyString = shortcut.getPrettyShortcut(mockI18nHelper);
        assertEquals("<kbd>g</kbd>", prettyString);

        verify(mockI18nHelper);
    }

    @Test
    public void testPrettyShortcutOneLetterThreeKeys()
    {
        final I18nHelper mockI18nHelper = createMock(I18nHelper.class);
        expect(mockI18nHelper.getText("common.words.or")).andReturn("or").times(2);

        replay(mockI18nHelper);
        final Set<List<String>> keys = new LinkedHashSet<List<String>>();
        keys.add(Arrays.asList("g"));
        keys.add(Arrays.asList("h"));
        keys.add(Arrays.asList("d"));
        KeyboardShortcut shortcut = new KeyboardShortcut("",Context.global, Operation.click, "", 10, keys, null, false);
        String prettyString = shortcut.getPrettyShortcut(mockI18nHelper);
        assertEquals("<kbd>g</kbd> or <kbd>h</kbd> or <kbd>d</kbd>", prettyString);

        verify(mockI18nHelper);
    }

    @Test
    public void testPrettyShortcutTwoLettersTwoKeys()
    {
        final I18nHelper mockI18nHelper = createMock(I18nHelper.class);

        expect(mockI18nHelper.getText("keyboard.shortcuts.two.keys", "<kbd>g</kbd>", "<kbd>h</kbd>")).
                andReturn("<kbd>g</kbd> then <kbd>h</kbd>");
        expect(mockI18nHelper.getText("common.words.or")).andReturn("or");
        expect(mockI18nHelper.getText("keyboard.shortcuts.two.keys", "<kbd>g</kbd>", "<kbd>d</kbd>")).
                andReturn("<kbd>g</kbd> then <kbd>d</kbd>");

        replay(mockI18nHelper);
        final Set<List<String>> keys = new LinkedHashSet<List<String>>();
        keys.add(Arrays.asList("g", "h"));
        keys.add(Arrays.asList("g", "d"));
        KeyboardShortcut shortcut = new KeyboardShortcut("",Context.global, Operation.click, "", 10, keys, null, false);
        String prettyString = shortcut.getPrettyShortcut(mockI18nHelper);
        assertEquals("<kbd>g</kbd> then <kbd>h</kbd> or <kbd>g</kbd> then <kbd>d</kbd>", prettyString);

        verify(mockI18nHelper);
    }

    @Test
    public void testPrettyShortcutThreeLetters()
    {
        final I18nHelper mockI18nHelper = createMock(I18nHelper.class);

        expect(mockI18nHelper.getText("keyboard.shortcuts.three.keys", "<kbd>g</kbd>", "<kbd>h</kbd>", "<kbd>c</kbd>")).
                andReturn("<kbd>g</kbd> then <kbd>h</kbd> then <kbd>c</kbd>");

        replay(mockI18nHelper);
        final Set<List<String>> keys = new LinkedHashSet<List<String>>();
        keys.add(Arrays.asList("g", "h", "c"));
        KeyboardShortcut shortcut = new KeyboardShortcut("",Context.global, Operation.click, "", 10, keys, null, false);
        String prettyString = shortcut.getPrettyShortcut(mockI18nHelper);
        assertEquals("<kbd>g</kbd> then <kbd>h</kbd> then <kbd>c</kbd>", prettyString);

        verify(mockI18nHelper);
    }
}
