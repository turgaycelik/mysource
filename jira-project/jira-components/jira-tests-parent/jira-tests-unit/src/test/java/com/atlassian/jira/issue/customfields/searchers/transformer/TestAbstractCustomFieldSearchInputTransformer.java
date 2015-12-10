package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.VersionIndexInfoResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractCustomFieldSearchInputTransformer
{
    @Mock private NameResolver<Version> versionResolver;
    @Mock private SearchContext searchContext;
    @Mock private CustomFieldInputHelper customFieldInputHelper;
    @Mock private VersionManager versionManager;

    @After
    public void tearDown() throws Exception
    {
        versionResolver = null;
        searchContext = null;
        customFieldInputHelper = null;
        versionManager = null;
    }

    @Test
    public void testPopulateFromParamsDelegates() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final ActionParamsImpl actionParams = new ActionParamsImpl();

        final CustomField field = mock(CustomField.class);

        AbstractCustomFieldSearchInputTransformer transformer = new Fixture(field, "cf[1000]", customFieldInputHelper);

        transformer.populateFromParams(null, valuesHolder, actionParams);

        verify(field).populateFromParams(valuesHolder, actionParams.getKeysAndValues());
    }

    @Test
    public void testPopulateFromSearchRequestParamsAreNull() throws Exception
    {
        final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer(
                "clauseName", new ClauseNames("clauseName"), mock(CustomField.class), mock(VersionIndexInfoResolver.class),
                mock(JqlOperandResolver.class), mock(FieldFlagOperandRegistry.class), versionResolver, customFieldInputHelper,
                versionManager)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
            {
                return null;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, valuesHolder, null, searchContext);
        assertThat(valuesHolder.entrySet(), hasSize(0));
    }

    @Test
    public void testPopulateFromSearchRequestParamsAreEmpty() throws Exception
    {
        final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer(
                "clauseName", new ClauseNames("clauseName"), mock(CustomField.class), mock(VersionIndexInfoResolver.class),
                mock(JqlOperandResolver.class), mock(FieldFlagOperandRegistry.class), versionResolver, customFieldInputHelper,
                versionManager)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
            {
                return new CustomFieldParamsImpl();
            }
        };


        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, valuesHolder, null, searchContext);
        assertThat(valuesHolder.entrySet(), hasSize(0));
    }

    @Test
    public void testPopulateFromSearchRequestParamsAreNotEmpty() throws Exception
    {
        final String searcherId = "searcherId";

        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(Collections.singleton("Hi"));

        final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer(
                searcherId, new ClauseNames("clauseName"), mock(CustomField.class), mock(VersionIndexInfoResolver.class),
                mock(JqlOperandResolver.class), mock(FieldFlagOperandRegistry.class), versionResolver,
                customFieldInputHelper, versionManager)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
            {
                return customFieldParams;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, valuesHolder, null, searchContext);
        assertEquals(customFieldParams, valuesHolder.get(searcherId));
    }

    @Test
    public void testValidateParamsValuesDoesntContain() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final CustomField field = mock(CustomField.class);

        AbstractCustomFieldSearchInputTransformer transformer = new Fixture(field, "cf[1000]", customFieldInputHelper);

        transformer.validateParams(null, null, valuesHolder, null, null);
    }

    @Test
    public void testValidateParamsValuesContainsSearcherNoRelevantConfig() throws Exception
    {
        final String searcherId = "searcherId";
        final SearchContext searchContext = mock(SearchContext.class);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(searcherId, Collections.singletonList("BLAH"));
        valuesHolder.put(searcherId, customFieldParams);
        final CustomField field = mock(CustomField.class);

        AbstractCustomFieldSearchInputTransformer transformer = new Fixture(field, "cf[1000]", customFieldInputHelper);

        transformer.validateParams(null, searchContext, valuesHolder, null, null);
    }

    @Test
    public void testValidateParamsValuesContainsSearcherRelevantConfig() throws Exception
    {
        final String searcherId = "cf[1000]";
        final SearchContext searchContext = mock(SearchContext.class);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(searcherId, Collections.singletonList("BLAH"));
        valuesHolder.put(searcherId, customFieldParams);

        final FieldConfig fieldConfig = mock(FieldConfig.class);
        final ErrorCollection errors = new SimpleErrorCollection();
        final CustomFieldType<?,?> customFieldType = mock(CustomFieldType.class);
        final CustomField field = mock(CustomField.class);

        when(field.getReleventConfig(searchContext)).thenReturn(fieldConfig);
        when(field.getCustomFieldType()).thenReturn(customFieldType);

        AbstractCustomFieldSearchInputTransformer transformer = new Fixture(field, "cf[1000]", customFieldInputHelper);
        transformer.validateParams(null, searchContext, valuesHolder, null, errors);

        verify(customFieldType).validateFromParams(customFieldParams, errors, fieldConfig);
    }

    @Test
    public void testValidateParamsValuesContainsSearcherNullConfig() throws Exception
    {
        final String searcherId = "cf[1000]";
        final SearchContext searchContext = mock(SearchContext.class);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(searcherId, Collections.singletonList("BLAH"));
        valuesHolder.put(searcherId, customFieldParams);
        final CustomField field = mock(CustomField.class);

        AbstractCustomFieldSearchInputTransformer transformer = new Fixture(field, "cf[1000]", customFieldInputHelper);

        transformer.validateParams(null, searchContext, valuesHolder, null, null);
    }

    static class Fixture extends AbstractCustomFieldSearchInputTransformer
    {
        public Fixture(CustomField field, String urlParameterName, final CustomFieldInputHelper customFieldInputHelper)
        {
            super(field, urlParameterName, customFieldInputHelper);
        }

        public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
        {
            return false;
        }

        protected Clause getClauseFromParams(final User user, final CustomFieldParams customFieldParams)
        {
            return null;
        }

        protected CustomFieldParams getParamsFromSearchRequest(final User user, final Query query, final SearchContext searchContext)
        {
            return null;
        }

    }
}
