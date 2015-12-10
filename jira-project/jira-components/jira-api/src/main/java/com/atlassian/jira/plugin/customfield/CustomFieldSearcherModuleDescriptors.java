package com.atlassian.jira.plugin.customfield;

import com.atlassian.annotations.PublicApi;
import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;

/**
 * Provides different methods to access the {@link CustomFieldSearcher}s registered in the system.
 */
@PublicApi
public interface CustomFieldSearcherModuleDescriptors
{
    /**
     * Retrieve a custom field searcher by its type key.
     * The key is the "full plugin module key". That is, the plugin key for the
     * plugin it comes from, a colon separator, and then the module key.
     *
     * @param completeModuleKey Type identifier constructed from plugin XML.
     * @return An {@link Option} with the {@link CustomFieldSearcher}
     */
    Option<CustomFieldSearcher> getCustomFieldSearcher(String completeModuleKey);
}
