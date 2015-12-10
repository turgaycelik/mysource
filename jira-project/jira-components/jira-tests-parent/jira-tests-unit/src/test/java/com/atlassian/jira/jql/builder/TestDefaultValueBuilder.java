package com.atlassian.jira.jql.builder;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.Operands;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.jql.builder.ValueBuilder}.
 *
 * @since v4.0
 */
public class TestDefaultValueBuilder
{
    @Test
    public void testConstructorBad() throws Exception
    {
        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        replay(builder);

        try
        {
            new DefaultValueBuilder(null, "name", Operator.EQUALS);
            fail("Exception expected.");
        }
        catch (IllegalArgumentException e) {}

        try
        {
            new DefaultValueBuilder(builder, null, Operator.EQUALS);
            fail("Exception expected.");
        }
        catch (IllegalArgumentException e) {}

        try
        {
            new DefaultValueBuilder(builder, "name", null);
            fail("Exception expected.");
        }
        catch (IllegalArgumentException e) {}

        verify(builder);
    }

    @Test
    public void testStringSingle() throws Exception
    {
        final String value = "value";
        final String name = "name";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addStringCondition(name, Operator.EQUALS, value)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.string(value));

        verify(builder);
    }

    @Test
    public void testStringVarArgs() throws Exception
    {
        final String value = "value";
        final String value2 = "value2";
        final String name = "name";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addStringCondition(name, Operator.EQUALS, value, value2)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.strings(value, value2));

        verify(builder);
    }

    @Test
    public void testStringCollection() throws Exception
    {
        final String value = "value";
        final String value2 = "value2";
        final String name = "name";
        final Collection<String> values = CollectionBuilder.newBuilder(value, value2).asLinkedList();


        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addStringCondition(name, Operator.EQUALS, values)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.strings(values));

        verify(builder);
    }

    @Test
    public void testNumberSingle() throws Exception
    {
        final long value = -7;
        final String name = "name";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addNumberCondition(name, Operator.EQUALS, value)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.number(value));

        verify(builder);
    }

    @Test
    public void testNumberVarArgs() throws Exception
    {
        final long value = 5;
        final long value2 = 6;
        final String name = "name";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addNumberCondition(name, Operator.EQUALS, value, value2)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.numbers(value, value2));

        verify(builder);
    }

    @Test
    public void testNumberCollection() throws Exception
    {
        final String name = "name";
        final Collection<Long> values = CollectionBuilder.newBuilder(6L, 7L).asLinkedList();


        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addNumberCondition(name, Operator.EQUALS, values)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.numbers(values));

        verify(builder);
    }

    @Test
    public void testOperandSingle() throws Exception
    {
        final Operand value = Operands.valueOf("3");
        final String name = "name";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addCondition(name, Operator.EQUALS, value)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.operand(value));

        verify(builder);
    }

    @Test
    public void testOperandVarArgs() throws Exception
    {
        final Operand value = Operands.valueOf("5");
        final Operand value2 = Operands.valueOf(6L);
        final String name = "name";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addCondition(name, Operator.EQUALS, value, value2)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.operands(value, value2));

        verify(builder);
    }

    @Test
    public void testOperandCollection() throws Exception
    {
        final String name = "name";
        final List<Operand> values = CollectionBuilder.newBuilder(Operands.valueOf(6L), Operands.valueOf("7L")).asLinkedList();

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addCondition(name, Operator.EQUALS, values)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.operands(values));

        verify(builder);
    }

    @Test
    public void testDateSingle() throws Exception
    {
        final Date date = new Date(56L);
        final String name = "name";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addDateCondition(name, Operator.LESS_THAN, date)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.LESS_THAN);
        assertSame(builder, defaultValueBuilder.date(date));

        verify(builder);
    }

    @Test
    public void testDateVarArgs() throws Exception
    {
        final Date value = new Date(347289347234L);
        final Date value2 = new Date(5878784545L);
        final String name = "name";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addDateCondition(name, Operator.NOT_IN, value, value2)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.NOT_IN);
        assertSame(builder, defaultValueBuilder.dates(value, value2));

        verify(builder);
    }

    @Test
    public void testDateCollection() throws Exception
    {
        final String name = "name";
        final List<Date> values = CollectionBuilder.newBuilder(new Date(7438243), new Date(2318283)).asLinkedList();

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addDateCondition(name, Operator.LESS_THAN, values)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.LESS_THAN);
        assertSame(builder, defaultValueBuilder.dates(values));

        verify(builder);
    }

    @Test
    public void testEmpty() throws Exception
    {
        final String name = "name";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addCondition(name, Operator.EQUALS, EmptyOperand.EMPTY)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.empty());

        verify(builder);
    }

    @Test
    public void testFunction() throws Exception
    {
        final String name = "name";
        final String funcName = "funcName";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.EQUALS, funcName)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.function(funcName));

        verify(builder);
    }

    @Test
    public void testFunctionVarArgs() throws Exception
    {
        final String name = "name";
        final String funcName = "funcName";

        String[] args = new String[] { "7", "8", "9" };

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.EQUALS, funcName, args)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.function(funcName, args));

        verify(builder);
    }

    @Test
    public void testFunctionCollection() throws Exception
    {
        final String name = "nam3";
        final String funcName = "asdhjajhds";

        Collection<String> args = CollectionBuilder.newBuilder("what", "is", "a", "builder").asCollection();

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.GREATER_THAN, funcName, args)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.GREATER_THAN);
        assertSame(builder, defaultValueBuilder.function(funcName, args));

        verify(builder);
    }

    @Test
    public void testFunctionStandardIssueTypes()
    {
        final String name = "fjewlkjrwlkrjelwkjre";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.GREATER_THAN, "standardIssueTypes")).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.GREATER_THAN);
        assertSame(builder, defaultValueBuilder.functionStandardIssueTypes());

        verify(builder);
    }

    @Test
    public void testFunctionSubTaskIssueTypes()
    {
        final String name = "adhdfhfhfdhjsdjhkfdkhjskjhdfjhskvbxvbksh475653874637";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.LESS_THAN, "subTaskIssueTypes")).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.LESS_THAN);
        assertSame(builder, defaultValueBuilder.functionSubTaskIssueTypes());

        verify(builder);
    }

    @Test
    public void testFunctionMembersOf()
    {
        final String name = "asdfgh";

        final JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);

        final String groupName = "group";
        expect(builder.addFunctionCondition(name, Operator.LESS_THAN, "membersOf", groupName)).andReturn(builder);
        final ValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.LESS_THAN);

        replay(builder);

        try
        {
            defaultValueBuilder.functionMembersOf(null);
            fail("Expected illegal argument exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        assertSame(builder, defaultValueBuilder.functionMembersOf(groupName));

        verify(builder);
    }

    @Test
    public void testFunctionCurrentUser()
    {
        final String name = "currentUser";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.GREATER_THAN_EQUALS, "currentUser")).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.GREATER_THAN_EQUALS);
        assertSame(builder, defaultValueBuilder.functionCurrentUser());

        verify(builder);
    }

    @Test
    public void testFunctionIssueHistory()
    {
        final String name = "issueHistory";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.GREATER_THAN_EQUALS, "issueHistory")).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.GREATER_THAN_EQUALS);
        assertSame(builder, defaultValueBuilder.functionIssueHistory());

        verify(builder);
    }

    @Test
    public void testFunctionUnreleasedVersions()
    {
        final String name = "meh";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.IN, "unreleasedVersions", new String[]{})).andReturn(builder);
        expect(builder.addFunctionCondition(name, Operator.IN, "unreleasedVersions", "JRA", "CONF")).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.IN);
        assertSame(builder, defaultValueBuilder.functionUnreleasedVersions());
        assertSame(builder, defaultValueBuilder.functionUnreleasedVersions("JRA", "CONF"));

        verify(builder);
    }
    
    @Test
    public void testFunctionReleasedVersions()
    {
        final String name = "pft";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.EQUALS, "releasedVersions", new String[]{})).andReturn(builder);
        expect(builder.addFunctionCondition(name, Operator.EQUALS, "releasedVersions", "JRA", "CONF")).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.functionReleasedVersions());
        assertSame(builder, defaultValueBuilder.functionReleasedVersions("JRA", "CONF"));

        verify(builder);
    }

    @Test
    public void testFunctionNow()
    {
        final String name = "now";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.EQUALS, "now")).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.functionNow());

        verify(builder);
    }

    @Test
    public void testFunctionWatachedIssues()
    {
        final String name = "watchedIssues";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.EQUALS, "watchedIssues")).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.functionWatchedIssues());

        verify(builder);
    }

    @Test
    public void testFunctionVotedIssues()
    {
        final String name = "votedIssues";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.EQUALS, "votedIssues")).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.functionVotedIssues());

        verify(builder);
    }

    @Test
    public void testFunctionLinkedIssues()
    {
        final String name = "linkedIssues";
        final String issueKey = "KEY1";
        final String linkType1 = "caused by";
        final String linkType2 = "related";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.EQUALS, "linkedIssues", Collections.singletonList(issueKey))).andReturn(builder);
        expect(builder.addFunctionCondition(name, Operator.EQUALS, "linkedIssues", CollectionBuilder.newBuilder(issueKey, linkType1, linkType2).asList())).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.functionLinkedIssues(issueKey));
        assertSame(builder, defaultValueBuilder.functionLinkedIssues(issueKey, linkType1, linkType2));

        verify(builder);

        try
        {
            defaultValueBuilder.functionLinkedIssues(null);
            fail("Expected an exception");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }

        try
        {
            defaultValueBuilder.functionLinkedIssues("blah", (String[]) null);
            fail("Expected an exception");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFunctionRemoteLinkByGlobalId()
    {
        final String clause = "issue";
        final String name = "issuesWithRemoteLinksByGlobalId";
        final String globalId1 = "a-global-id";
        final String globalId2 = "another-global-id";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(clause, Operator.EQUALS, name, globalId1, globalId2)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, clause, Operator.EQUALS);
        assertSame(builder, defaultValueBuilder.functionRemoteLinksByGlobalId(globalId1, globalId2));

        verify(builder);

        defaultValueBuilder.functionRemoteLinksByGlobalId(null);
        defaultValueBuilder.functionRemoteLinksByGlobalId();
    }

    @Test
    public void testFunctionCascaingOptionParent()
    {
        final String name = "cascadeOption";
        final String parent = "parent";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.IN, "cascadeOption", parent)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.IN);
        assertSame(builder, defaultValueBuilder.functionCascaingOption(parent));

        verify(builder);
    }

    @Test
    public void testFunctionCascaingOptionParentAndChild()
    {
        final String name = "cascadeOption";
        final String child = "child";
        final String parent = "parent";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.IN, "cascadeOption", parent, child)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.IN);
        assertSame(builder, defaultValueBuilder.functionCascaingOption(parent, child));

        verify(builder);
    }

    @Test
    public void testFunctionCascaingOptionParentOnly()
    {
        final String name = "cascadeOption";
        final String parent = "parent";
        final String child = "\"none\"";

        JqlClauseBuilder builder = createMock(JqlClauseBuilder.class);
        expect(builder.addFunctionCondition(name, Operator.IN, "cascadeOption", parent, child)).andReturn(builder);

        replay(builder);

        final DefaultValueBuilder defaultValueBuilder = new DefaultValueBuilder(builder, name, Operator.IN);
        assertSame(builder, defaultValueBuilder.functionCascaingOption(parent, child));

        verify(builder);
    }
}
