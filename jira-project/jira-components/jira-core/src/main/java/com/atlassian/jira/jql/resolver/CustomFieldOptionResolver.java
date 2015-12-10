package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Finds option Ids for custom fields
 *
 * @since v5.1
 */
public class CustomFieldOptionResolver implements NameResolver<Option>
{
    private final OptionsManager optionsManager;

    public CustomFieldOptionResolver(OptionsManager optionsManager)
    {
        this.optionsManager = optionsManager;
    }

    @Override
    public List<String> getIdsFromName(String name)
    {
        List<Option> options = optionsManager.findByOptionValue(name);
        List<String> ids = new ArrayList<String>();
        for (Option option : options)
        {
            ids.add(option.getOptionId().toString());
        }
        return ids;
    }

    @Override
    public boolean nameExists(String name)
    {
        List<Option> options = optionsManager.findByOptionValue(name);
        return !(options == null || options.isEmpty());
    }

    @Override
    public boolean idExists(Long id)
    {
        Option options = optionsManager.findByOptionId(id);
        return options != null;
    }

    @Override
    public Option get(Long id)
    {
        return optionsManager.findByOptionId(id);
    }

    @Override
    public Collection<Option> getAll()
    {
        return optionsManager.getAllOptions();
    }
}
