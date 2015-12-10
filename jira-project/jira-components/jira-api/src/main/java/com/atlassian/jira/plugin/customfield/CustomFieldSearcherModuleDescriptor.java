package com.atlassian.jira.plugin.customfield;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import webwork.action.Action;

import java.util.Map;
import java.util.Set;

@PublicApi
public interface CustomFieldSearcherModuleDescriptor extends JiraResourcedModuleDescriptor<CustomFieldSearcher>
{
    public String getSearchHtml(CustomField customField,
                                CustomFieldValueProvider provider,
                                SearchContext searchContext,
                                FieldValuesHolder fieldValuesHolder,
                                Map displayParameters,
                                Action action,
                                Map velocityParams);

    public String getViewHtml(CustomField customField,
                                CustomFieldValueProvider provider,
                                SearchContext searchContext,
                                FieldValuesHolder fieldValuesHolder,
                                Map displayParameters,
                                Action action,
                                Map velocityParams);

    public String getViewHtml(CustomField field, Object value);

    public String getStatHtml(CustomField field, Object value, String urlPrefix);

    public Set getValidCustomFieldKeys();
}
