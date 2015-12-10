package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.LabelsField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.CustomFieldLabelsIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.CustomFieldLabelsSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.CustomFieldLabelsStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.query.LabelsClauseQueryFactory;
import com.atlassian.jira.jql.validator.LabelsValidator;
import com.atlassian.jira.jql.values.LabelsClauseValuesGenerator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Searcher for Label custom fields
 *
 * @since v4.2
 */
public class CustomFieldLabelsSearcher extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, CustomFieldStattable
{
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final LabelsClauseValuesGenerator labelsClauseValuesGenerator;
    private final JiraAuthenticationContext authenticationContext;
    private final CustomFieldInputHelper customFieldInputHelper;

    private CustomFieldSearcherInformation searcherInformation;
    private SearchRenderer searchRenderer;
    private SearchInputTransformer searchInputTransformer;
    private CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    public CustomFieldLabelsSearcher(final FieldVisibilityManager fieldVisibilityManager,
            final JqlOperandResolver jqlOperandResolver,
            final LabelsClauseValuesGenerator labelsClauseValuesGenerator, final JiraAuthenticationContext authenticationContext,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jqlOperandResolver = jqlOperandResolver;
        this.labelsClauseValuesGenerator = labelsClauseValuesGenerator;
        this.authenticationContext = authenticationContext;
        this.customFieldInputHelper = customFieldInputHelper;
    }

    public void init(final CustomField customField)
    {
        final ClauseNames names = customField.getClauseNames();
        final FieldIndexer fieldIndexer = new CustomFieldLabelsIndexer(fieldVisibilityManager, customField);
        final CustomFieldValueProvider customFieldValueProvider = new LabelCustomFieldValueProvider();

        searcherInformation = new CustomFieldSearcherInformation(customField.getId(), customField.getNameKey(), Arrays.asList(fieldIndexer), new AtomicReference<CustomField>(customField));
        searchRenderer = new CustomFieldRenderer(names, getDescriptor(), customField, customFieldValueProvider, fieldVisibilityManager);
        searchInputTransformer = new CustomFieldLabelsSearchInputTransformer(customField, customField.getId(), customFieldInputHelper, names);
        customFieldSearcherClauseHandler = new SimpleCustomFieldValueGeneratingClauseHandler(
                new LabelsValidator(jqlOperandResolver),
                new LabelsClauseQueryFactory(jqlOperandResolver, customField.getId() + CustomFieldLabelsIndexer.FOLDED_EXT),
                labelsClauseValuesGenerator,
                SystemSearchConstants.forLabels().getSupportedOperators(),
                SystemSearchConstants.forLabels().getDataType()
        );
    }

    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler()
    {
        return customFieldSearcherClauseHandler;
    }

    public SearcherInformation<CustomField> getSearchInformation()
    {
        return searcherInformation;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        return searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer()
    {
        return searchRenderer;
    }

    public StatisticsMapper getStatisticsMapper(final CustomField customField)
    {
        return new CustomFieldLabelsStatisticsMapper(customField, customFieldInputHelper, authenticationContext, false);
    }

    static class LabelCustomFieldValueProvider implements CustomFieldValueProvider
    {
        public Object getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
        {
            CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
            Collection customFieldValues = customFieldParams.getValuesForNullKey();
            if (customFieldValues != null)
            {
                return Joiner.on(LabelsField.SEPARATOR_CHAR).join(customFieldValues);
            }
            else
            {
                return "";
            }
        }

        public Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
        {
            CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
            return customField.getCustomFieldType().getValueFromCustomFieldParams(customFieldParams);
        }
    }

}
