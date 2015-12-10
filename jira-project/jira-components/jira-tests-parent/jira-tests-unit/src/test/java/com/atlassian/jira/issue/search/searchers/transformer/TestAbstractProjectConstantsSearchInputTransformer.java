package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.MockFunctionOperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.search.MockSearchContext;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.noVersions;
import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.version;
import static java.lang.String.valueOf;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestAbstractProjectConstantsSearchInputTransformer
{
    public static final String FUNCTION = "function";

    @Mock
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;

    @Mock
    private NameResolver<Version> testNameResolver;
    private MockUser user = new MockUser("fred");

    @Mock
    private NavigatorStructureChecker<Version> testChecker;

    private MockJqlOperandResolver jqlOperandResolver;

    @Before
    public void setup()
    {
        new MockComponentWorker().init();
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        jqlOperandResolver.addHandler(FUNCTION, new MockFunctionOperandHandler(1L));
    }

    @Test
    public void testPopulateFromParams()
    {
        final String url = "url";
        class ParamsTransformer extends TestTransformer
        {
            private List<List<String>> parts = Lists.newLinkedList();

            ParamsTransformer()
            {
                super(url);
            }

            @Nonnull
            @Override
            VersionSearchInput parseInputParam(final String[] parts)
            {
                this.parts.add(Arrays.asList(parts));
                return version(parts[0]);
            }
        }

        final ParamsTransformer transformer = new ParamsTransformer();
        final ActionParams actionParams = new ActionParamsImpl();
        actionParams.put(url, new String[]{"a:bc", "a:", "k::", "lm:cdeg:ss"});
        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();

        transformer.populateFromParams(user, fieldValuesHolder, actionParams);

        List<List<String>> expectedPairs = Lists.newArrayList();
        expectedPairs.add(list("a", "bc"));
        expectedPairs.add(list("a", ""));
        expectedPairs.add(list("k", ":"));
        expectedPairs.add(list("lm", "cdeg:ss"));
        assertEquals(expectedPairs, transformer.parts);

        assertFvh(url, fieldValuesHolder, version("a"), version("k"), version("lm"));
    }

    @Test
    public void testPopulateFromQueryEmpty()
    {
        final Query builder = JqlQueryBuilder.newClauseBuilder().affectedVersion().isEmpty().buildQuery();
        assertPopulateFromQuery(builder, noVersions());
    }

    @Test
    public void testPopulateFromQueryFunction()
    {
        final Query builder = JqlQueryBuilder.newClauseBuilder().affectedVersion().eqFunc("something").buildQuery();
        assertPopulateFromQuery(builder, VersionSearchInput.allReleased());
    }

    @Test
    public void testPopulateFromQuerySingleStringNotNumber()
    {
        final String versionName = "something";
        final Query builder = JqlQueryBuilder.newClauseBuilder().affectedVersion().eq(versionName).buildQuery();
        assertPopulateFromQuery(builder, VersionSearchInput.version(versionName));
    }

    @Test
    public void testPopulateFromQuerySingleStringIdDoesNotExist()
    {
        final long versionId = 10;
        final Query builder = JqlQueryBuilder.newClauseBuilder().affectedVersion().eq(valueOf(versionId)).buildQuery();
        assertPopulateFromQuery(builder, version(valueOf(versionId)));
    }

    @Test
    public void testPopulateFromQuerySingleStringIdExists()
    {
        final long versionId = 10;
        final String versionName = "Ten";
        when(testNameResolver.get(versionId)).thenReturn(new MockVersion(versionId, versionName));

        final Query builder = JqlQueryBuilder.newClauseBuilder().affectedVersion().eq(valueOf(versionId)).buildQuery();
        assertPopulateFromQuery(builder, version(valueOf(versionName)));
    }

    @Test
    public void testPopulateFromQuerySingleStringIdExistsAsWellAsName()
    {
        final long versionId = 10;
        final String versionName = "Ten";
        when(testNameResolver.get(versionId)).thenReturn(new MockVersion(versionId, versionName));
        when(testNameResolver.nameExists(valueOf(versionId))).thenReturn(true);

        final Query builder = JqlQueryBuilder.newClauseBuilder().affectedVersion().eq(valueOf(versionId)).buildQuery();
        assertPopulateFromQuery(builder, version(valueOf(versionId)), version(versionName));
    }

    @Test
    public void testPopulateFromQuerySingleLongIdDoesntExist()
    {
        final long versionId = 10;
        final Query builder = JqlQueryBuilder.newClauseBuilder().affectedVersion().eq(versionId).buildQuery();
        assertPopulateFromQuery(builder, version(valueOf(versionId)));
    }

    @Test
    public void testPopulateFromQuerySingleLongIdExists()
    {
        final long versionId = 10;
        final String versionName = "Ten";
        when(testNameResolver.get(versionId)).thenReturn(new MockVersion(versionId, versionName));

        final Query builder = JqlQueryBuilder.newClauseBuilder().affectedVersion().eq(versionId).buildQuery();
        assertPopulateFromQuery(builder, version(valueOf(versionName)));
    }

    @Test
    public void testPopulateFromQueryMultiple()
    {
        final long versionId = 10;
        final String versionName = "Ten";
        when(testNameResolver.get(versionId)).thenReturn(new MockVersion(versionId, versionName));

        final MultiValueOperand multiValueOperand = new MultiValueOperand(new SingleValueOperand(versionId), EmptyOperand.EMPTY, new FunctionOperand("example"));
        final Query builder = JqlQueryBuilder.newClauseBuilder()
                .addClause(new TerminalClauseImpl("affectedVersion", Operator.IN, multiValueOperand)).buildQuery();

        assertPopulateFromQuery(builder, version(valueOf(versionName)), noVersions(), VersionSearchInput.allReleased());
    }

    @Test
    public void testGetSearchClauseEmpty()
    {
        assertGetSearchClause(null);
    }

    @Test
    public void testGetSearchClauseSingleNotList()
    {
        final String versionName = "version";

        final Query builder = JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().eq(versionName).buildQuery();

        assertGetSearchClause(builder.getWhereClause(), version(versionName));
    }

    @Test
    public void testGetSearchClauseSingleList()
    {
        final String version = "version";

        final Query builder = JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().in(version, version).buildQuery();
        assertGetSearchClause(builder.getWhereClause(), version(version + ":" + version));
    }

    @Test
    public void testGetSearchClauseMultiple()
    {
        final String version = "version";

        final Query builder = JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().in(version, version).buildQuery();
        assertGetSearchClause(builder.getWhereClause(), version(version), version(version));
    }

    @Test
    public void testDoesItFitCheckerFails()
    {
        final Query builder = JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().isEmpty().buildQuery();

        final TestTransformer testTransformer = new TestTransformer();
        assertFalse(testTransformer.doRelevantClausesFitFilterForm(null, builder, new MockSearchContext()));
    }

    @Test
    public void testDoesItFitCheckerPassesFunction()
    {
        assertDoesItFitSuccess(JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().in().function(FUNCTION).buildQuery());
    }

    @Test
    public void testDoesItFitCheckerPassesEmpty()
    {
        assertDoesItFitSuccess(JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().isEmpty().buildQuery());
    }

    @Test
    public void testDoesItFitCheckerPassesSingle()
    {
        assertDoesItFitSuccess(JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().eq(valueOf(true)).buildQuery());
    }

    @Test
    public void testDoesItFitCheckerFailsSingle()
    {
        assertDoesItFitFail(JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().eq(valueOf(false)).buildQuery());
    }

    @Test
    public void testDoesItFitCheckerPassesMultiple()
    {
        MultiValueOperand operand = new MultiValueOperand(function(), EmptyOperand.EMPTY, single(true));

        assertDoesItFitSuccess(JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().eq(operand).buildQuery());
    }

    @Test
    public void testDoesItFitCheckerFailsMultiple()
    {
        MultiValueOperand operand = new MultiValueOperand(function(), EmptyOperand.EMPTY, single(true), single(false));

        assertDoesItFitFail(JqlQueryBuilder.newClauseBuilder()
                .affectedVersion().eq(operand).buildQuery());
    }

    private static SingleValueOperand single(final boolean good)
    {
        return new SingleValueOperand(valueOf(good));
    }

    private static FunctionOperand function()
    {
        return new FunctionOperand(FUNCTION);
    }

    private void assertDoesItFitSuccess(final Query query)
    {
        assertDoesItFit(query, true);
    }

    private void assertDoesItFitFail(final Query query)
    {
        assertDoesItFit(query, false);
    }

    private void assertDoesItFit(final Query query, boolean expected)
    {
        when(testChecker.checkSearchRequest(Mockito.any(Query.class))).thenReturn(true);
        final TestTransformer testTransformer = new TestTransformer();
        assertEquals(expected, testTransformer.doRelevantClausesFitFilterForm(null, query, new MockSearchContext()));
    }

    private void assertGetSearchClause(final Clause expectedClause, final VersionSearchInput...actualInput)
    {
        final String url = "url";
        final TestTransformer transformer = new TestTransformer(url);
        final FieldValuesHolderImpl fieldValuesHolder = new FieldValuesHolderImpl();

        if (actualInput.length > 0)
        {
            fieldValuesHolder.put(url, Arrays.asList(actualInput));
        }

        assertEquals(expectedClause, transformer.getSearchClause(user, fieldValuesHolder));
    }

    private void assertPopulateFromQuery(final Query query, final VersionSearchInput...input)
    {
        final String url = "url";
        final TestTransformer transformer = new TestTransformer(url);
        final FieldValuesHolderImpl fvh = new FieldValuesHolderImpl();
        transformer.populateFromQuery(user, fvh, query, new MockSearchContext());
        assertFvh(url, fvh, input);
    }

    private static void assertFvh(final String url, final FieldValuesHolder fieldValuesHolder, final VersionSearchInput...versionSearchInputs)
    {
        @SuppressWarnings ("unchecked")
        final Collection<VersionSearchInput> actualVersions = (Collection<VersionSearchInput>)fieldValuesHolder.get(url);
        assertEquals(list(versionSearchInputs), actualVersions);
    }

    private class TestTransformer extends AbstractProjectConstantsSearchInputTransformer<Version, VersionSearchInput>
    {
        TestTransformer()
        {
            this("dontCare");
        }

        TestTransformer(final String urlParameterName)
        {
            super(SystemSearchConstants.forAffectedVersion().getJqlClauseNames(),
                    urlParameterName, jqlOperandResolver, testNameResolver, testChecker);
        }

        @Nonnull
        @Override
        VersionSearchInput parseInputParam(final String[] parts)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Nonnull
        @Override
        VersionSearchInput inputValue(final String value)
        {
            return VersionSearchInput.version(value);
        }

        @Nonnull
        @Override
        VersionSearchInput noValueInput()
        {
            return noVersions();
        }

        @Override
        void parseFunctionOperand(final FunctionOperand operand, final Set<VersionSearchInput> values)
        {
            values.add(VersionSearchInput.allReleased());
        }

        @Nonnull
        @Override
        Operand parseInputValue(final VersionSearchInput value)
        {
            if (value.equals(noVersions()))
            {
                return EmptyOperand.EMPTY;
            }
            else
            {
                final String[] split = value.getValue().split(":");
                if (split.length > 1)
                {
                    return new MultiValueOperand(split);
                }
                else
                {
                    return new SingleValueOperand(value.getValue());
                }
            }
        }

        @Override
        boolean checkClauseValuesForBasic(final QueryLiteral literal)
        {
            return Boolean.parseBoolean(literal.asString());
        }
    }

    private static <T> List<T> list(T...elements)
    {
        return Lists.newLinkedList(Arrays.asList(elements));
    }
}
