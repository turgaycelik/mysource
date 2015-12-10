package com.atlassian.jira.plugin.keyboardshortcut;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.ConditionalDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager.Context;

/**
 * Provides a plugin point to define keyboard shortcuts.  Shortcuts are made up of a set of shortcut keys for this
 * particular operation, the type of operation, a context in which the operation applies and a parameter for the
 * operation.  They are also ordered, which defines in which order they will appear on the help screen.
 *
 * @since v4.1
 */
public class KeyboardShortcutModuleDescriptor extends AbstractJiraModuleDescriptor<KeyboardShortcut>
        implements OrderableModuleDescriptor, ConditionalDescriptor
{
    private static final Logger log = Logger.getLogger(KeyboardShortcutModuleDescriptor.class);

    private static final Pattern JSON_VALUE_STRING = Pattern.compile("^\".*\"$");
    private static final Pattern JSON_VALUE_ARRAY = Pattern.compile("^\\[.*\\]$");

    private final KeyboardShortcutManager keyboardShortcutManager;
    private final ConditionDescriptorFactory conditionDescriptorFactory;

    private Element element;
    private String moduleKey;
    private Context context = Context.global;
    private KeyboardShortcutManager.Operation operation;
    private String operationParam;
    private Set<List<String>> shortcuts = new HashSet<List<String>>();
    private String descriptionKey;
    private int order;
    private boolean hidden = false;
    private Condition condition;

    public KeyboardShortcutModuleDescriptor(final JiraAuthenticationContext authenticationContext, final KeyboardShortcutManager keyboardShortcutManager, final ModuleFactory moduleFactory, final ConditionDescriptorFactory conditionDescriptorFactory)
    {
        super(authenticationContext, moduleFactory);
        this.keyboardShortcutManager = keyboardShortcutManager;
        this.conditionDescriptorFactory = conditionDescriptorFactory;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        final Attribute keyAttr = element.attribute("key");
        if (keyAttr != null && StringUtils.isNotEmpty(keyAttr.getText()))
        {
            moduleKey = keyAttr.getText();
        } else {
            throw new PluginParseException("The <shortcut> element did not provide a valid key attribute!");
        }

        final Attribute hiddenAttr = element.attribute("hidden");
        if(hiddenAttr != null && StringUtils.isNotEmpty(hiddenAttr.getText()))
        {
            hidden = Boolean.parseBoolean(hiddenAttr.getText());
        }
        final Element contextEl = element.element("context");
        if (contextEl != null)
        {
            final String contextString = StringUtils.trim(contextEl.getText());
            try
            {
                context = Context.valueOf(contextString);
            }
            catch (IllegalArgumentException e)
            {
                log.error("Invalid keyboard shortcut context of '" + contextString + "' defined.  Falling back to global context!");
                context = Context.global;
            }
        }

        final Element shortcutEl = element.element("shortcut");
        if (shortcutEl != null)
        {
            // For the moment, we're only going to allow a single shortcut to be defined in the <shortcut> element so
            // that we can (optionally) parse it as a JSON string or array of strings (previously, we interpreted it as
            // a space separated list of shortcuts).
            // If we require multiple shortcuts for a single <keyboard-shortcut> element, we'll add support for
            // multiple <shortcut> elements.
            List<String> shortcutCombination;
            try
            {
                shortcutCombination = parseShortcut(shortcutEl.getTextTrim());
            }
            catch (JSONException e)
            {
                throw new PluginParseException("The <shortcut> element did not provide a valid keyboard shortcut definition!");
            }
            if (shortcutCombination.isEmpty())
            {
                throw new PluginParseException("The <shortcut> element did not provide a valid keyboard shortcut definition!");
            }
            this.shortcuts.add(shortcutCombination);
        }
        else
        {
            throw new PluginParseException("<shortcut> is a required element for a keyboard shortcut plugin module!");
        }

        final Element operationEl = element.element("operation");
        if (operationEl != null)
        {
            final String operationType = operationEl.attribute("type").getText();
            try
            {
                this.operation = KeyboardShortcutManager.Operation.valueOf(operationType);
            }
            catch (IllegalArgumentException e)
            {
                throw new PluginParseException("Invalid operation type '" + operationType + "' provided!");
            }
            this.operationParam = StringUtils.trim(operationEl.getText());
        }
        else
        {
            throw new PluginParseException("<operation> is a required element for a keyboard shortcut plugin module!");
        }
        order = ModuleDescriptorXMLUtils.getOrder(element);

        final Element descriptionEl = element.element("description");
        if (descriptionEl != null && descriptionEl.attribute("key") != null)
        {
            descriptionKey = StringUtils.trim(descriptionEl.attributeValue("key"));
        }
        else
        {
            throw new PluginParseException("<description> i18n 'key' attribute is a required attribute for a keyboard shortcut plugin module!");
        }

        this.element = element;
    }

    @Override
    protected KeyboardShortcut createModule()
    {
        return new KeyboardShortcut(moduleKey, context, operation, operationParam, order, shortcuts, descriptionKey, hidden, condition);
    }

    @Override
    public void enabled()
    {
        super.enabled();

        // this was moved to the enabled() method because spring beans declared
        // by the plugin are not available for injection during the init() phase
        condition = conditionDescriptorFactory.retrieveCondition(plugin, element);

        keyboardShortcutManager.registerShortcut(getCompleteKey(), getModule());
    }

    @Override
    public void disabled()
    {
        super.disabled();
        keyboardShortcutManager.unregisterShortcut(getCompleteKey());
    }

    public int getOrder()
    {
        return order;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    @Override
    public Condition getCondition()
    {
        return condition;
    }

    private static List<String> parseShortcut(String shortcut) throws JSONException
    {
        if (JSON_VALUE_ARRAY.matcher(shortcut).matches())
        {
            List<String> result = new ArrayList<String>();
            JSONArray array = new JSONArray(shortcut);
            for (int i = 0; i < array.length(); i++) {
                result.add(array.getString(i));
            }
            return result;
        }
        else if (JSON_VALUE_STRING.matcher(shortcut).matches())
        {
            final String key = "shortcut";
            JSONObject json = new JSONObject(String.format("{ \"%s\": %s }", key, shortcut));
            return Arrays.asList(json.getString(key));
        }
        else
        {
            List<String> result = new ArrayList<String>();
            for (char c : shortcut.toCharArray())
            {
                result.add(String.valueOf(c));
            }
            return result;
        }
    }

}
