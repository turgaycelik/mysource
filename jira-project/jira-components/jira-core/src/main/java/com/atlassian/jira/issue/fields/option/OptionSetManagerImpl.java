package com.atlassian.jira.issue.fields.option;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;

import java.util.Collection;

public class OptionSetManagerImpl implements OptionSetManager
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final OptionSetPersister optionSetPersister;
    private final ConstantsManager constantsManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public OptionSetManagerImpl(OptionSetPersister optionSetPersister, ConstantsManager constantsManager)
    {
        this.optionSetPersister = optionSetPersister;
        this.constantsManager = constantsManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public OptionSet getOptionsForConfig(FieldConfig config)
    {
        final OptionSet optionSet = optionSetPersister.getOptionSetByConfig(config);
        return optionSet;
    }

    public OptionSet createOptionSet(FieldConfig config, Collection optionIds)
    {
        return optionSetPersister.create(config, optionIds);
    }

    public OptionSet updateOptionSet(FieldConfig config, Collection optionIds)
    {
        return optionSetPersister.update(config, optionIds);
    }

    public void removeOptionSet(FieldConfig config)
    {
        optionSetPersister.update(config, null);
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods    
}
