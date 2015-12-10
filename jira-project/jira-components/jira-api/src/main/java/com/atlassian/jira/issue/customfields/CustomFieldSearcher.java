package com.atlassian.jira.issue.customfields;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;

/**
 * The custom field searcher interface defines an {@link IssueSearcher} that's usable by {@link CustomField} objects.
 * Since JIRA 4.0, searchers have changed a lot with the introduction of JQL. For examples of usage you might want to
 * check out {@link com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher} and its
 * inheritors.
 *
 * @since JIRA 3.0
 */
@PublicSpi
@PublicApi
public interface CustomFieldSearcher extends IssueSearcher<CustomField>
{
    /**
     * Initializes the searcher. This is called the first time this searcher is retrieved from the plugin manager.
     * You can expect that JIRA will be initialized when this method is invoked.
     *
     * @param customFieldSearcherModuleDescriptor the module descriptor that defines this searcher.
     */
    void init(CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor);

    /**
     * @return the module descriptor that defined this searcher.
     */
    CustomFieldSearcherModuleDescriptor getDescriptor();

    /**
     * Provides an object that can be used to handle the clauses that this searcher generates.
     *
     * @return an object that can be used to handle the clauses that this searcher generates, can be null.
     */
    CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler();
}
