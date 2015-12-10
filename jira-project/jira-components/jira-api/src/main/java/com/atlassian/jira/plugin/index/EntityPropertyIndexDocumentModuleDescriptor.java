package com.atlassian.jira.plugin.index;

import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.plugin.ModuleDescriptor;

/**
 * Module descriptor for indexing entity document properties
 *
 * @since v6.2
 */
public interface EntityPropertyIndexDocumentModuleDescriptor extends ModuleDescriptor<Void>
{
    IndexDocumentConfiguration getIndexDocumentConfiguration();

    /**
     * Initializes this EntityPropertyIndexDocumentModuleDescriptor.
     * This may write configuration to db if not previously stored.
     */
    void init();

}
