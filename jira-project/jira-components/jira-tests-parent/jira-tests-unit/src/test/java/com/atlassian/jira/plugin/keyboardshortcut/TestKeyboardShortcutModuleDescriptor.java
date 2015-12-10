package com.atlassian.jira.plugin.keyboardshortcut;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestKeyboardShortcutModuleDescriptor
{
    private static final int ORDER = 10;
    private static final Set<List<String>> SHORTCUTS = Collections.singleton(Arrays.asList("g", "d"));
    private static final String PARAMETER = "#home_link";

    private final JiraAuthenticationContext mockAuthenticationContext = mock(JiraAuthenticationContext.class);
    private final KeyboardShortcutManager mockKeyboardShortcutManager = mock(KeyboardShortcutManager.class);
    private final ConditionDescriptorFactory mockConditionDescriptionFactory = mock(ConditionDescriptorFactory.class);

    private final Plugin mockPlugin = mock(Plugin.class);
    private final Condition mockCondition = mock(Condition.class);

    private KeyboardShortcutModuleDescriptor moduleDescriptor;

    @Before
    public void createModuleDescriptor() throws Exception
    {
        moduleDescriptor = new KeyboardShortcutModuleDescriptor(mockAuthenticationContext, mockKeyboardShortcutManager, ModuleFactory.LEGACY_MODULE_FACTORY, mockConditionDescriptionFactory);
    }

    @Test
    public void testInitAndEnabled() throws DocumentException
    {
        final Document document = createDescriptionWithShortcut("gd");
        final Element element = document.getRootElement();
        expectRetrieveCondition(element, mockCondition);

        moduleDescriptor.init(mockPlugin, element);
        moduleDescriptor.enabled();

        final int order = moduleDescriptor.getOrder();
        assertEquals(ORDER, order);

        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.global, keyboardShortcut.getContext());
        assertEquals(KeyboardShortcutManager.Operation.followLink, keyboardShortcut.getOperation());
        assertEquals(PARAMETER, keyboardShortcut.getParameter());
        assertEquals(SHORTCUTS, keyboardShortcut.getShortcuts());
        assertThat(moduleDescriptor.getCondition(), is(mockCondition));
    }

    @Test
    public void testInitAndEnabledAndEnableWithJsonString() throws DocumentException
    {
        final Document document = createDescriptionWithShortcut("\"return\"");
        final Element element = document.getRootElement();
        expectRetrieveCondition(element, mockCondition);

        moduleDescriptor.init(mockPlugin, element);
        moduleDescriptor.enabled();

        final int order = moduleDescriptor.getOrder();
        assertEquals(ORDER, order);
        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.global, keyboardShortcut.getContext());
        assertEquals(KeyboardShortcutManager.Operation.followLink, keyboardShortcut.getOperation());
        assertEquals(PARAMETER, keyboardShortcut.getParameter());
        final Set<List<String>> shortcuts = Collections.singleton(Arrays.asList("return"));
        assertEquals(shortcuts, keyboardShortcut.getShortcuts());
        assertThat(moduleDescriptor.getCondition(), is(mockCondition));
    }

    @Test
    public void testInitAndEnabledWithJsonArray() throws DocumentException
    {
        final Document document = createDescriptionWithShortcut("[\"j\", \"tab\"]");
        final Element element = document.getRootElement();
        expectRetrieveCondition(element, mockCondition);

        moduleDescriptor.init(mockPlugin, element);
        moduleDescriptor.enabled();

        final int order = moduleDescriptor.getOrder();
        assertEquals(ORDER, order);
        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.global, keyboardShortcut.getContext());
        assertEquals(KeyboardShortcutManager.Operation.followLink, keyboardShortcut.getOperation());
        assertEquals(PARAMETER, keyboardShortcut.getParameter());
        final Set<List<String>> shortcuts = Collections.singleton(Arrays.asList("j", "tab"));
        assertEquals(shortcuts, keyboardShortcut.getShortcuts());
        assertThat(moduleDescriptor.getCondition(), is(mockCondition));
    }

    @Test
    public void testInitAndEnabledWithContext() throws DocumentException
    {
        final Document document = createDescriptionWithContext("issueaction");
        final Element element = document.getRootElement();
        expectRetrieveCondition(element, mockCondition);

        moduleDescriptor.init(mockPlugin, element);
        moduleDescriptor.enabled();

        final int order = moduleDescriptor.getOrder();
        assertEquals(20, order);
        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.issueaction, keyboardShortcut.getContext());
        assertEquals(KeyboardShortcutManager.Operation.click, keyboardShortcut.getOperation());
        assertEquals("#create_link", keyboardShortcut.getParameter());
        final Set<List<String>> shortcuts = Collections.singleton(Arrays.asList("c"));
        assertEquals(shortcuts, keyboardShortcut.getShortcuts());
        assertThat(moduleDescriptor.getCondition(), is(mockCondition));
    }

    @Test
    public void testInitAndEnabledWithInvalidContextDefaultsToGlobal() throws DocumentException
    {
        final Document document = createDescriptionWithContext("booyah");
        final Element element = document.getRootElement();
        expectRetrieveCondition(element, mockCondition);

        moduleDescriptor.init(mockPlugin, element);
        moduleDescriptor.enabled();

        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.global, keyboardShortcut.getContext());
        assertThat(moduleDescriptor.getCondition(), is(mockCondition));
    }

    @Test
    public void testInitAndEnabledWithCondition() throws Exception
    {
        final Element element = createDescriptionWithConditionClass().getRootElement();
        expectRetrieveCondition(element, mockCondition);

        moduleDescriptor.init(mockPlugin, element);
        moduleDescriptor.enabled();

        final int order = moduleDescriptor.getOrder();
        assertEquals(ORDER, order);

        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.global, keyboardShortcut.getContext());
        assertEquals(KeyboardShortcutManager.Operation.followLink, keyboardShortcut.getOperation());
        assertEquals(PARAMETER, keyboardShortcut.getParameter());

        assertEquals(SHORTCUTS, keyboardShortcut.getShortcuts());
        assertThat(moduleDescriptor.getCondition(), is(mockCondition));
    }

    @Test
    public void testInitMissingShortcutTagFailure() throws DocumentException
    {
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <context>issue</context>\n"
                        + "        <operation type=\"click\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(mockPlugin, document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
    }

    @Test
    public void testInitEmptyShortcutTagFailure() throws DocumentException
    {
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut></shortcut>\n"
                        + "        <context>issue</context>\n"
                        + "        <operation type=\"click\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(mockPlugin, document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
    }

    @Test
    public void testInitInvalidOperationTypeFailure() throws DocumentException
    {
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>c</shortcut>\n"
                        + "        <context>global</context>\n"
                        + "        <operation type=\"booyah\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(mockPlugin, document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
    }

    @Test
    public void testInitMissingOperationTagFailure() throws DocumentException
    {
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>c</shortcut>\n"
                        + "        <context>issue</context>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(mockPlugin, document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
    }

    @Test
    public void testInitMissingI18nKeyForDescriptionFailure() throws DocumentException
    {
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description>Go to Dashboard</description>\n"
                        + "        <shortcut>c</shortcut>\n"
                        + "        <context>global</context>\n"
                        + "        <operation type=\"click\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(mockPlugin, document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
    }

    private void expectRetrieveCondition(@Nonnull final Element element, @Nonnull final Condition condition)
    {
        when(mockConditionDescriptionFactory.retrieveCondition(mockPlugin, element)).thenReturn(condition);
    }

    private Document createDescriptionWithShortcut(final String shortcut) throws DocumentException
    {
        return DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <order>" + ORDER + "</order>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>" + shortcut + "</shortcut>\n"
                        + "        <operation type=\"followLink\">" + PARAMETER + "</operation>\n"
                        + "    </keyboard-shortcut>");
    }

    private Document createDescriptionWithContext(final String context) throws DocumentException
    {
        return DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <order>20</order>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>" + "c" + "</shortcut>\n"
                        + "        <context>" + context + "</context>\n"
                        + "        <operation type=\"click\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");
    }

    private Document createDescriptionWithConditionClass() throws DocumentException
    {
        return DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "    <order>" + ORDER + "</order>\n"
                        + "    <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "    <shortcut>gd</shortcut>\n"
                        + "    <operation type=\"followLink\">" + PARAMETER + "</operation>\n"
                        + "    <condition class=\"com.my.Condition\"/>\n"
                        + "</keyboard-shortcut>");
    }
}
