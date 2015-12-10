package com.atlassian.jira.plugin.link.applinks;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

import static com.google.common.collect.Iterables.isEmpty;


/**
 * Condition to check whether there are any application links available for the given application type.
 *
 * @since v5.0
 */
public class HasApplicationLinkCondition implements Condition
{
    private final ApplicationLinkService applicationLinkService;

    private Class<ApplicationType> applicationTypeClass;

    public HasApplicationLinkCondition(@ComponentImport ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    @Override
    public void init(Map<String,String> params) throws PluginParseException
    {
        final String applicationType = params.get("applicationType");
        if (applicationType == null)
        {
            throw new PluginParseException("Parameter 'applicationType' must be defined for this condition");
        }

        try
        {
            applicationTypeClass = AppLinkUtils.getApplicationTypeClass(applicationType);
        }
        catch (IllegalArgumentException e)
        {
            throw new PluginParseException(e);
        }
    }

    public boolean shouldDisplay(Map<String,Object> context)
    {
        return !isEmpty(applicationLinkService.getApplicationLinks(applicationTypeClass));
    }
}
