package com.atlassian.jira.issue.fields.option;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import javax.annotation.Nonnull;

import java.util.Collection;

@PublicApi
public interface OptionSetManager
{
    // -------------------------------------------------------------------------------------------------- Public Methods
    OptionSet getOptionsForConfig(@Nonnull FieldConfig config);

    OptionSet createOptionSet(@Nonnull FieldConfig config, Collection optionIds);

    OptionSet updateOptionSet(@Nonnull FieldConfig config, Collection optionIds);

    void removeOptionSet(@Nonnull FieldConfig config);
}
