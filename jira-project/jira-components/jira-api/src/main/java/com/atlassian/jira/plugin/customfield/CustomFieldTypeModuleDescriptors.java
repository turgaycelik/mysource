package com.atlassian.jira.plugin.customfield;

import com.atlassian.annotations.PublicApi;
import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.customfields.CustomFieldType;

import java.util.List;
import javax.annotation.Nonnull;

/**
 * Provides different methods to access the {@link CustomFieldType}s registered in the system.
 */
@PublicApi
public interface CustomFieldTypeModuleDescriptors
{
    /**
     * Retrieve all {@link CustomFieldType}s registered in the system.
     * @return a list of {@link CustomFieldType}s
     */
    @Nonnull
    List<CustomFieldType<?,?>> getCustomFieldTypes();

    /**
     * Retrieve a custom field type by its type key.
     * The key is the "full plugin module key". That is, the plugin key for the
     * plugin it comes from, a colon separator, and then the module key.
     *
     * @param completeModuleKey Type identifier constructed from plugin XML.
     * @return An {@link Option} with the {@link CustomFieldType} for the given key
     */
    Option<CustomFieldType> getCustomFieldType(String completeModuleKey);
}
