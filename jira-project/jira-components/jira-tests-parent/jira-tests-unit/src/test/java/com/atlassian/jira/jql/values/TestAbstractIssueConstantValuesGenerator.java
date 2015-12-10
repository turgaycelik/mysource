package com.atlassian.jira.jql.values;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.MockIssueConstant;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @since v4.0
 */
public class TestAbstractIssueConstantValuesGenerator
{
    private Fixture valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testGetPossibleValuesHappyPath() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "", 5);

        assertThat(possibleValues.getResults(), contains(
                new ClauseValuesGenerator.Result(type1.getName()),
                new ClauseValuesGenerator.Result(type2.getName()),
                new ClauseValuesGenerator.Result(type3.getName()),
                new ClauseValuesGenerator.Result(type4.getName())));
    }

    @Test
    public void testGetPossibleValuesDoesMatchFullValue() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Aa it", 5);

        assertThat(possibleValues.getResults(), contains(
                new ClauseValuesGenerator.Result(type1.getName())));
    }

    @Test
    public void testGetPossibleValuesMatchNone() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Z", 5);

        assertThat(possibleValues.getResults(), hasSize(0));
    }

    @Test
    public void testGetPossibleValuesMatchSome() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "a", 5);

        assertThat(possibleValues.getResults(), contains(
                new ClauseValuesGenerator.Result(type1.getName()),
                new ClauseValuesGenerator.Result(type2.getName())));
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "Aa it blah");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "Aa it", 5);

        assertThat(possibleValues.getResults(), contains(
                new ClauseValuesGenerator.Result(type1.getName()),
                new ClauseValuesGenerator.Result(type2.getName())));
    }

    @Test
    public void testGetPossibleValuesMatchToLimit() throws Exception
    {
        final MockIssueConstant type1 = new MockIssueConstant("1", "Aa it");
        final MockIssueConstant type2 = new MockIssueConstant("2", "A it");
        final MockIssueConstant type3 = new MockIssueConstant("3", "B it");
        final MockIssueConstant type4 = new MockIssueConstant("4", "C it");

        valuesGenerator = createGenerator(type4, type3, type2, type1);

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "type", "", 3);

        assertThat(possibleValues.getResults(), contains(
                new ClauseValuesGenerator.Result(type1.getName()),
                new ClauseValuesGenerator.Result(type2.getName()),
                new ClauseValuesGenerator.Result(type3.getName())));
    }

    private static Fixture createGenerator(IssueConstant... allConstants)
    {
        return new Fixture(allConstants)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };
    }

    static class Fixture extends AbstractIssueConstantValuesGenerator
    {
        private final List<IssueConstant> allConstants;

        Fixture(IssueConstant... allConstants)
        {
            this.allConstants = Arrays.asList(allConstants);
        }

        protected List<IssueConstant> getAllConstants()
        {
            return allConstants;
        }
    }
}
