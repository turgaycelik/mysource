package com.atlassian.jira.web.dispatcher;

import com.atlassian.jira.util.lang.Pair;
import com.atlassian.plugin.ModuleCompleteKey;
import org.apache.commons.lang.StringUtils;
import webwork.config.util.ActionInfo;

import static com.atlassian.jira.web.dispatcher.JiraWebworkViewDispatcher.isFromCore;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * This helper class allows us to interpret the soy template parameters better into something we can call.
 */
public class SoyTemplateAddress
{
    private String completeKey;
    private String templateName;

    public static SoyTemplateAddress address(ActionInfo.ViewInfo viewInfo)
    {
        final String defaultCompleteModuleKey = viewInfo.getActionInfo().getSource();
        final String viewTemplateValue = StringUtils.defaultString(viewInfo.getViewValue()).trim();
        return address(defaultCompleteModuleKey, viewTemplateValue);
    }

    public static SoyTemplateAddress address(String defaultCompleteModuleKey, String viewTemplateValue)
    {
        if (isEmpty(viewTemplateValue))
        {
            throw new IllegalArgumentException("Illegal Soy template name : Its not specified");
        }
        String templateName = StringUtils.substringAfter(viewTemplateValue, "/").trim();
        String pluginAndModule = StringUtils.substringBefore(viewTemplateValue, "/").trim();
        if (isEmpty(templateName) || isEmpty(pluginAndModule))
        {
            throw new IllegalArgumentException(String.format("Illegal Soy template name '%s'.   A valid name might be 'com.company.pluginKey:moduleKey/templateName' or ':moduleKey/templateName'", viewTemplateValue));
        }
        if (StringUtils.countMatches(viewTemplateValue,":") != 1)
        {
            throw new IllegalArgumentException(String.format("Illegal Soy template name '%s'.   A valid name might be 'com.company.pluginKey:moduleKey/templateName' or ':moduleKey/templateName'", viewTemplateValue));
        }


        /**
         See {@link com.atlassian.jira.plugin.webwork.WebworkModuleDescriptor} for details on how we setup the plugin key as source
         */
        final ModuleCompleteKey completeKey = isFromCore(defaultCompleteModuleKey) ? new ModuleCompleteKey("jira.webresources", "soy-templates") : new ModuleCompleteKey(defaultCompleteModuleKey);

        Pair<String, String> pair = splitAndTrim(pluginAndModule);
        // we must have a module name
        String moduleKey = pair.second();
        if (isEmpty(moduleKey))
        {
            throw new IllegalArgumentException(String.format("Illegal Soy template name '%s'.  A valid name might be 'com.company.pluginKey:moduleKey/templateName' or ':moduleKey/templateName'", viewTemplateValue));
        }
        final String pluginKey;

        if (pair.first().equals(".") || pair.first().equals(""))
        {
            pluginKey = completeKey.getPluginKey().trim();
        }
        else
        {
            pluginKey = pair.first().trim();
        }
        //
        // and that folks, is our module address
        final SoyTemplateAddress template = new SoyTemplateAddress();
        template.completeKey = pluginKey + ":" + moduleKey;
        template.templateName = templateName;
        return template;
    }


    private static Pair<String, String> splitAndTrim(final String pluginModule)
    {
        String[] split = pluginModule.split(":");
        String pluginKey = split.length < 1 ? "" : split[0].trim();
        String moduleKey = split.length < 2 ? "" : split[1].trim();
        return Pair.of(pluginKey, moduleKey);
    }

    public String getCompleteKey()
    {
        return completeKey;
    }

    public String getTemplateName()
    {
        return templateName;
    }
}
