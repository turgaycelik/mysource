package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.allReleased;
import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.allUnreleased;
import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.noVersions;
import static com.atlassian.jira.issue.search.searchers.transformer.VersionSearchInput.version;
import static com.atlassian.jira.plugin.jql.function.AllReleasedVersionsFunction.FUNCTION_RELEASED_VERSIONS;
import static com.atlassian.jira.plugin.jql.function.AllUnreleasedVersionsFunction.FUNCTION_UNRELEASED_VERSIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestVersionSearchInputTransformer
{
    @Mock
    private VersionResolver nameResolver;
    @Mock
    private VersionManager versionManager;
    @Mock
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;

    private VersionSearchInputTransformer transformer;

    @Before
    public void setUp()
    {
        SimpleFieldSearchConstantsWithEmpty fixVersion = SystemSearchConstants.forFixForVersion();
        transformer = new VersionSearchInputTransformer(fixVersion.getJqlClauseNames(), fixVersion.getUrlParameter(),
                MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, nameResolver);
    }

    @Test
    public void parseInputParam()
    {
        final String version = "version";

        assertEquals(noVersions(), transformer.parseInputParam(new String[]{VersionManager.NO_VERSIONS}));
        assertEquals(allReleased(), transformer.parseInputParam(new String[]{VersionManager.ALL_RELEASED_VERSIONS}));
        assertEquals(allUnreleased(), transformer.parseInputParam(new String[]{VersionManager.ALL_UNRELEASED_VERSIONS}));
        assertEquals(version(version), transformer.parseInputParam(new String[] { "id", version }));
        assertEquals(version(version), transformer.parseInputParam(new String[] { version }));
    }

    @Test
    public void noValueInput()
    {
        assertEquals(VersionSearchInput.noVersions(), transformer.noValueInput());
    }

    @Test
    public void inputValue()
    {
        final String version = "string";
        assertEquals(VersionSearchInput.version(version), transformer.inputValue(version));
    }

    @Test
    public void parseFunctionReleased()
    {
        assertParseFunction(FUNCTION_RELEASED_VERSIONS, VersionSearchInput.allReleased());
    }

    @Test
    public void parseFunctionUnReleased()
    {
        assertParseFunction(FUNCTION_UNRELEASED_VERSIONS, VersionSearchInput.allUnreleased());
    }

    @Test
    public void parseInputValueAllUnreleased()
    {
        assertEquals(new FunctionOperand(FUNCTION_UNRELEASED_VERSIONS), transformer.parseInputValue(VersionSearchInput.allUnreleased()));
    }

    @Test
    public void parseInputValueAllReleased()
    {
        assertEquals(new FunctionOperand(FUNCTION_RELEASED_VERSIONS), transformer.parseInputValue(VersionSearchInput.allReleased()));
    }

    @Test
    public void parseInputValueNoVersion()
    {
        assertEquals(EmptyOperand.EMPTY, transformer.parseInputValue(VersionSearchInput.noVersions()));
    }

    @Test
    public void parseInputValueVersion()
    {
        final String version = "version";
        assertEquals(new SingleValueOperand(version), transformer.parseInputValue(VersionSearchInput.version(version)));
    }

    @Test
    public void checkClauseValuesForBasicEmpty()
    {
        assertFalse(transformer.checkClauseValuesForBasic(literal()));
    }

    @Test
    public void checkClauseValuesForBasicStringNoValues()
    {
        assertFalse(transformer.checkClauseValuesForBasic(literal("novalues")));
    }

    @Test
    public void checkClauseValuesForBasicLongNoValues()
    {
        assertFalse(transformer.checkClauseValuesForBasic(literal(10101L)));
    }

    @Test
    public void checkClauseValuesForBasicStringAllArchived()
    {
        assertCheckBasicForVersion(literal("something"), false, createVersion(1, true), createVersion(2, true));
    }

    @Test
    public void checkClauseValuesForBasicLongAllArchived()
    {
        assertCheckBasicForVersion(literal(171718L), false, createVersion(1, true));
    }

    @Test
    public void checkClauseValuesForBasicStringSomeNotArchived()
    {
        assertCheckBasicForVersion(literal("something"), true, createVersion(1, true), createVersion(2, false));
    }

    @Test
    public void checkClauseValuesForBasicLongSomeNotArchived()
    {
        assertCheckBasicForVersion(literal(171718L), true, createVersion(1, false));
    }

    @Test
    public void checkClauseValuesForBasicLongButIsId()
    {
        final long id = 171718L;
        when(nameResolver.idExists(id)).thenReturn(true);
        assertFalse(transformer.checkClauseValuesForBasic(literal(id)));
    }

    private void assertCheckBasicForVersion(QueryLiteral input, boolean expected, Version...versions)
    {
        final List<Version> versionsList = Arrays.asList(versions);
        final List<Long> ids = ids(versionsList);

        when(nameResolver.getIdsFromName(input.asString())).thenReturn(strings(ids));

        for (Version version : versionsList)
        {
            if (version != null)
            {
                when(nameResolver.get(version.getId())).thenReturn(version);
            }
        }

        assertEquals(expected, transformer.checkClauseValuesForBasic(input));
    }

    private void assertParseFunction(final String functionName, final VersionSearchInput expectedSearchInput)
    {
        Set<VersionSearchInput> inputs = Sets.newHashSet();
        transformer.parseFunctionOperand(new FunctionOperand(functionName), inputs);
        assertEquals(Collections.singleton(expectedSearchInput), inputs);
    }

    private Version createVersion(long id, boolean arch)
    {
        final MockVersion version = new MockVersion(id, String.valueOf(id));
        version.setArchived(arch);
        return version;
    }

    private QueryLiteral literal(final String value) {return new QueryLiteral(new SingleValueOperand(value), value);}
    private QueryLiteral literal(final long value) {return new QueryLiteral(new SingleValueOperand(value), value);}
    private QueryLiteral literal() {return new QueryLiteral(EmptyOperand.EMPTY);}

    private List<String> strings(List<?> values)
    {
        return Lists.transform(values, Functions.toStringFunction());
    }

    private List<Long> ids(List<? extends Version> version)
    {
        return Lists.transform(version, new Function<Version, Long>()
        {
            @Override
            public Long apply(final Version input)
            {
                return input.getId();
            }
        });
    }
}
