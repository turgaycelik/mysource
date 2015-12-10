package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.VersionResolver;

/**
 * {@link SearchInputTransformer} for custom fields that allow to pick versions.
 */
public class VersionPickerCustomFieldSearchInputTransformer extends VersionSearchInputTransformer
{
    private final CustomFieldInputHelper customFieldInputHelper;
    private final CustomField customField;

    public VersionPickerCustomFieldSearchInputTransformer(
            final CustomField customField,
            final JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final VersionResolver versionResolver,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        super(customField.getClauseNames(), customField.getId(), operandResolver, fieldFlagOperandRegistry, versionResolver);
        this.customFieldInputHelper = customFieldInputHelper;
        this.customField = customField;
    }

    @Override
    protected String getClauseName(final User user, final ClauseNames clauseNames)
    {
        return customFieldInputHelper.getUniqueClauseName(user, clauseNames.getPrimaryName(), customField.getUntranslatedName());
    }
}
