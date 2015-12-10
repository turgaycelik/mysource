package com.atlassian.jira.jql.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.jql.parser.antlr.JqlLexer;
import com.atlassian.jira.jql.util.JqlStringSupportImpl;
import com.atlassian.jira.local.Junit3ListeningTestCase;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.Property;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.clause.WasClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.apache.commons.lang3.StringUtils;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.GREATER_THAN;
import static com.atlassian.query.operator.Operator.GREATER_THAN_EQUALS;
import static com.atlassian.query.operator.Operator.IN;
import static com.atlassian.query.operator.Operator.IS;
import static com.atlassian.query.operator.Operator.IS_NOT;
import static com.atlassian.query.operator.Operator.LESS_THAN;
import static com.atlassian.query.operator.Operator.LESS_THAN_EQUALS;
import static com.atlassian.query.operator.Operator.LIKE;
import static com.atlassian.query.operator.Operator.NOT_EQUALS;
import static com.atlassian.query.operator.Operator.NOT_IN;
import static com.atlassian.query.operator.Operator.NOT_LIKE;

/**
 * Test for {@link com.atlassian.jira.jql.parser.DefaultJqlQueryParser}.
 *
 * @since v4.0
 */
public class TestDefaultJqlQueryParser extends Junit3ListeningTestCase
{
    //Set of characters that are reserved but not currently used within JQL.
    private static final String illgealCharsString = "{}*/%+$#@?;";

    //Set of reserved words that the grammar currently parses.
    private static final Set<String> currentWords = CollectionBuilder.newBuilder("empty", "null", "and", "or", "not", "in", "is", "cf", "issue.property", "order", "by", "desc", "asc", "on", "before", "to", "after", "from", "was", "changed").asSet();

    private final TestSuite suite = new TestSuite();


    public static TestSuite suite()
    {
        final TestDefaultJqlQueryParser test = new TestDefaultJqlQueryParser();

        final BigInteger tooBigNumber = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);

        //Some tests to make sure that the white space is ignored during parsing.
        {
            final TerminalClauseImpl expectedClause = new TerminalClauseImpl("priority", EQUALS, "qwerty");
            test.add("priority = \"qwerty\"", expectedClause);
            test.add("priority=\"qwerty\"", expectedClause);
            test.add("priority=qwerty", expectedClause);
            test.add("  priority=qwerty  ", expectedClause);
            test.add("priority=     qwerty order      by priority, other", expectedClause, new OrderByImpl(new SearchSort("priority"), new SearchSort("other")));
        }

        //Checking minus after removing it from the reserved characters list.
        test.add("key = one-1", new TerminalClauseImpl("key", EQUALS, "one-1"));
        test.add("key in (one-1, -1)", new TerminalClauseImpl("key", IN, new MultiValueOperand(new SingleValueOperand("one-1"), new SingleValueOperand(-1L))));
        test.add("key in (one-1, 1-1)", new TerminalClauseImpl("key", IN, new MultiValueOperand(new SingleValueOperand("one-1"), new SingleValueOperand("1-1"))));
        test.add("-78a = a", new TerminalClauseImpl("-78a", EQUALS, new SingleValueOperand("a")));
        test.add("numberfield >= -29202", new TerminalClauseImpl("numberfield", GREATER_THAN_EQUALS, -29202));
        test.add("numberfield >= -29202-", new TerminalClauseImpl("numberfield", GREATER_THAN_EQUALS, "-29202-"));
        test.add("numberfield >= w-88 ", new TerminalClauseImpl("numberfield", GREATER_THAN_EQUALS, "w-88"));

        //Test to ensure that newline is accepted.
        test.add("newline = \"hello\nworld\"", new TerminalClauseImpl("newline", EQUALS, "hello\nworld"));
        test.add("newline = \"hello\\nworld\"", new TerminalClauseImpl("newline", EQUALS, "hello\nworld"));
        test.add("newline = 'hello\r'", new TerminalClauseImpl("newline", EQUALS, "hello\r"));
        test.add("newline = 'hello\\r'", new TerminalClauseImpl("newline", EQUALS, "hello\r"));
        test.add("newline = '\r'", new TerminalClauseImpl("newline", EQUALS, "\r"));
        test.add("newline = '\\r'", new TerminalClauseImpl("newline", EQUALS, "\r"));
        test.add("'new\nline' = 'b'", new TerminalClauseImpl("new\nline", EQUALS, "b"));
        test.add("'new\\nline' = 'b'", new TerminalClauseImpl("new\nline", EQUALS, "b"));
        test.add("'newline' = 'fun\rc'()", new TerminalClauseImpl("newline", EQUALS, new FunctionOperand("fun\rc")));
        test.add("'newline' = 'fun\\rc'()", new TerminalClauseImpl("newline", EQUALS, new FunctionOperand("fun\rc")));

        //Some tests for the other operators.
        test.add("coolness >= awesome", new TerminalClauseImpl("coolness", GREATER_THAN_EQUALS, "awesome"));
        test.add("coolness > awesome", new TerminalClauseImpl("coolness", GREATER_THAN, "awesome"));
        test.add("coolness < awesome", new TerminalClauseImpl("coolness", LESS_THAN, "awesome"));
        test.add("coolness <= awesome", new TerminalClauseImpl("coolness", LESS_THAN_EQUALS, "awesome"));
        test.add("coolness        !=       awesome order     by     coolness desc", new TerminalClauseImpl("coolness", NOT_EQUALS, "awesome"), new OrderByImpl(new SearchSort("coolness", SortOrder.DESC)));

        //Some tests for the in operator.
        test.add("language in (java, c, \"python2\")", new TerminalClauseImpl("language", IN, new MultiValueOperand("java", "c", "python2")));
        test.add("languagein   IN    (   java, c     , \"python2\")", new TerminalClauseImpl("languagein", IN, new MultiValueOperand("java", "c", "python2")));
        test.add("inlanguage in (java, c, \"python2\")", new TerminalClauseImpl("inlanguage", IN, new MultiValueOperand("java", "c", "python2")));
        test.add("pri in (java,c,\"python2\")", new TerminalClauseImpl("pri", IN, new MultiValueOperand("java", "c", "python2")));
        test.add("pri in(java)", new TerminalClauseImpl("pri", IN, new MultiValueOperand("java")));
        test.add("pri In(java)", new TerminalClauseImpl("pri", IN, new MultiValueOperand("java")));
        test.add("pri iN(java)", new TerminalClauseImpl("pri", IN, new MultiValueOperand("java")));

        //Some tests for the NOT in operator.
        test.add("language not in (java, c, \"python2\")", new TerminalClauseImpl("language", NOT_IN, new MultiValueOperand("java", "c", "python2")));
        test.add("languagein  NOT   IN    (   java, c     , \"python2\")", new TerminalClauseImpl("languagein", NOT_IN, new MultiValueOperand("java", "c", "python2")));
        test.add("inlanguage not in (java, c, \"python2\")", new TerminalClauseImpl("inlanguage", NOT_IN, new MultiValueOperand("java", "c", "python2")));
        test.add("pri NOT in (java,c,\"python2\")", new TerminalClauseImpl("pri", NOT_IN, new MultiValueOperand("java", "c", "python2")));
        test.add("pri not in(java)", new TerminalClauseImpl("pri", NOT_IN, new MultiValueOperand("java")));
        test.add("pri NoT In(java)", new TerminalClauseImpl("pri", NOT_IN, new MultiValueOperand("java")));
        test.add("pri nOT iN(java)", new TerminalClauseImpl("pri", NOT_IN, new MultiValueOperand("java")));

        // Some tests for the LIKE operator.
        test.add("pri ~ stuff", new TerminalClauseImpl("pri", LIKE, new SingleValueOperand("stuff")));
        test.add("pri~stuff", new TerminalClauseImpl("pri", LIKE, new SingleValueOperand("stuff")));
        test.add("pri ~ 12", new TerminalClauseImpl("pri", LIKE, new SingleValueOperand(12L)));
        test.add("pri~12", new TerminalClauseImpl("pri", LIKE, new SingleValueOperand(12L)));
        test.add("pri ~ (\"stuff\", 12)", new TerminalClauseImpl("pri", LIKE, new MultiValueOperand(CollectionBuilder.newBuilder(new SingleValueOperand("stuff"), new SingleValueOperand(12L)).asList())));

        // Some tests for the NOT_LIKE operator.
        test.add("pri !~ stuff", new TerminalClauseImpl("pri", NOT_LIKE, new SingleValueOperand("stuff")));
        test.add("pri!~stuff", new TerminalClauseImpl("pri", NOT_LIKE, new SingleValueOperand("stuff")));
        test.add("pri !~ 12", new TerminalClauseImpl("pri", NOT_LIKE, new SingleValueOperand(12L)));
        test.add("pri!~12", new TerminalClauseImpl("pri", NOT_LIKE, new SingleValueOperand(12L)));
        test.add("pri !~ (\"stuff\", 12)", new TerminalClauseImpl("pri", NOT_LIKE, new MultiValueOperand(CollectionBuilder.newBuilder(new SingleValueOperand("stuff"), new SingleValueOperand(12L)).asList())));

        // Some tests for the IS operator
        test.add("pri IS stuff", new TerminalClauseImpl("pri", IS, new SingleValueOperand("stuff")));
        test.add("pri is stuff", new TerminalClauseImpl("pri", IS, new SingleValueOperand("stuff")));
        test.add("pri IS EMPTY", new TerminalClauseImpl("pri", IS, new EmptyOperand()));

        // Some tests for the IS_NOT operator
        test.add("pri IS NOT stuff", new TerminalClauseImpl("pri", IS_NOT, new SingleValueOperand("stuff")));
        test.add("pri IS not stuff", new TerminalClauseImpl("pri", IS_NOT, new SingleValueOperand("stuff")));
        test.add("pri is Not stuff", new TerminalClauseImpl("pri", IS_NOT, new SingleValueOperand("stuff")));
        test.add("pri is not stuff", new TerminalClauseImpl("pri", IS_NOT, new SingleValueOperand("stuff")));


        //Test for the nested behaviour of in clause.
        {
            final List<Operand> nested = Arrays.asList(new MultiValueOperand("java"), new SingleValueOperand("duke"));
            test.add("pri iN((java), duke)", new TerminalClauseImpl("pri", IN, new MultiValueOperand(nested)));
        }

        //Test to make sure that numbers are returned correctly.
        test.add("priority = 12345", new TerminalClauseImpl("priority", EQUALS, 12345L));
        test.add("priority = -12345", new TerminalClauseImpl("priority", EQUALS, -12345L));
        test.add("priority = \"12a345\"", new TerminalClauseImpl("priority", EQUALS, "12a345"));
        test.add("priority = 12345a", new TerminalClauseImpl("priority", EQUALS, "12345a"));

        //Test custom field labels
        test.add("cf[12345] = 12345a", new TerminalClauseImpl("cf[12345]", EQUALS, "12345a"));
        test.add("Cf  [ 0005 ] = x", new TerminalClauseImpl("cf[5]", EQUALS, "x"));

        //Test properties
        test.add("issue.property[x] = x", new TerminalClauseImpl("issue.property", EQUALS, new SingleValueOperand("x"), property("x", null)));
        test.add("issue.property[issue.status] = resolved", new TerminalClauseImpl("issue.property", EQUALS, new SingleValueOperand("resolved"), property("issue.status", null)));
        test.add("ISSUE.property[\"issue.status\"] = resolved", new TerminalClauseImpl("ISSUE.property", EQUALS, new SingleValueOperand("resolved"), property("issue.status", null)));
        test.add("issue.property[\'issue.status\'] = resolved", new TerminalClauseImpl("issue.property", EQUALS, new SingleValueOperand("resolved"), property("issue.status", null)));
        test.add("issue.property     [\'issue.status\'] = resolved", new TerminalClauseImpl("issue.property", EQUALS, new SingleValueOperand("resolved"), property("issue.status", null)));
        test.add("issue.property[\'1@4s\'] = resolved", new TerminalClauseImpl("issue.property", EQUALS, new SingleValueOperand("resolved"), property("1@4s", null)));
        test.add("issue.property[1234] = resolved", new TerminalClauseImpl("issue.property", EQUALS, new SingleValueOperand("resolved"), property("1234", null)));
        test.add("issue.property[-1234] = resolved", new TerminalClauseImpl("issue.property", EQUALS, new SingleValueOperand("resolved"), property("-1234", null)));
        test.add("issue.property.x = y", new TerminalClauseImpl("issue.property.x", EQUALS, new SingleValueOperand("y")));
        // reserved characters inside
        test.add("issue.ProPeRty[\'-@.,@\'] = resolved", new TerminalClauseImpl("issue.ProPeRty", EQUALS, new SingleValueOperand("resolved"), property("-@.,@", null)));
        test.add("comment.prop[author] = filip", new TerminalClauseImpl("comment.prop", EQUALS, new SingleValueOperand("filip"), property("author", null)));
        test.add("comment.prop[author].author.name = filip", new TerminalClauseImpl("comment.prop", EQUALS, new SingleValueOperand("filip"), property("author", "author.name")));
        test.add("comment.prop[author].Author < filip", new TerminalClauseImpl("comment.prop", LESS_THAN, new SingleValueOperand("filip"), property("author", "Author")));

        //Make sure that a quoted number is actually returned as a string.
        test.add("priority = \"12345\"", new TerminalClauseImpl("priority", EQUALS, "12345"));

        //An invalid number should be returned as a string.
        test.add("priority=\"12a345\"", new TerminalClauseImpl("priority", EQUALS, "12a345"));
        //Should accept dot separated values as rhv
        test.add("version= 1.2.3", new TerminalClauseImpl("version", EQUALS, "1.2.3"));

        //Some tests to check the empty operand
        test.add("testfield = EMPTY", new TerminalClauseImpl("testfield", EQUALS, new EmptyOperand()));
        test.add("testfield = empty", new TerminalClauseImpl("testfield", EQUALS, new EmptyOperand()));
        test.add("testfield = NULL", new TerminalClauseImpl("testfield", EQUALS, new EmptyOperand()));
        test.add("testfield = null", new TerminalClauseImpl("testfield", EQUALS, new EmptyOperand()));
        test.add("testfield = \"null\"", new TerminalClauseImpl("testfield", EQUALS, "null"));
        test.add("testfield = \"NULL\"", new TerminalClauseImpl("testfield", EQUALS, "NULL"));
        test.add("testfield = \"EMPTY\"", new TerminalClauseImpl("testfield", EQUALS, "EMPTY"));
        test.add("testfield = \"empty\"", new TerminalClauseImpl("testfield", EQUALS, "empty"));

        // tests for quoted strings with characters that must be quoted
        test.add("priority = \"a big string ~ != foo and priority = haha \"", new TerminalClauseImpl("priority", EQUALS, "a big string ~ != foo and priority = haha "));
        test.add("priority = \"\"", new TerminalClauseImpl("priority", EQUALS, ""));

        //test for strange field names.
        test.add("prior\\'ty = testvalue", new TerminalClauseImpl("prior'ty", EQUALS, "testvalue"));
        test.add("priority\\ ty=testvalue", new TerminalClauseImpl("priority ty", EQUALS, "testvalue"));
        test.add("priority\u2ee5 > 6", new TerminalClauseImpl("priority\u2ee5", GREATER_THAN, 6));
        test.add("priori\\nty\\u2ee5 > 6", new TerminalClauseImpl("priori\nty\u2ee5", GREATER_THAN, 6));
        test.add("\"this is a strange field \" = google", new TerminalClauseImpl("this is a strange field ", EQUALS, "google"));
        test.add("\"don't\" = 'true'", new TerminalClauseImpl("don't", EQUALS, "true"));
        test.add("\"don\\\"t\" = 'false'", new TerminalClauseImpl("don\"t", EQUALS, "false"));
        test.add("\"don't\" = 'false'", new TerminalClauseImpl("don't", EQUALS, "false"));
        test.add("'don\\'t' = 'false' order by 'don\\'t' DEsc", new TerminalClauseImpl("don't", EQUALS, "false"), new OrderByImpl(new SearchSort("don't", SortOrder.DESC)));
        test.add("'don\"t' = 'false'", new TerminalClauseImpl("don\"t", EQUALS, "false"));
        test.add("'cf[1220]' = abc", new TerminalClauseImpl("cf[1220]", EQUALS, "abc"));
        test.add("'cf' = abc", new TerminalClauseImpl("cf", EQUALS, "abc"));
        test.add("10245948 = abc      order          by 10245948", new TerminalClauseImpl("10245948", EQUALS, "abc"), new OrderByImpl(new SearchSort("10245948")));
        test.add("-10245948 = abc", new TerminalClauseImpl("-10245948", EQUALS, "abc"));
        test.add("new\\nline = abc", new TerminalClauseImpl("new\nline", EQUALS, "abc"));
        test.add("some\\u0082control = abc  order by some\\u0082control", new TerminalClauseImpl("some\u0082control", EQUALS, "abc"), new OrderByImpl(new SearchSort("some\u0082control")));

        //test for strange field values.
        test.add("b = ''", new TerminalClauseImpl("b", EQUALS, ""));
        test.add("b = \\ ", new TerminalClauseImpl("b", EQUALS, " "));
        test.add("b = don\\'t\\ stop\\ me\\ now", new TerminalClauseImpl("b", EQUALS, "don't stop me now"));
        test.add("b = \u2ee5", new TerminalClauseImpl("b", EQUALS, "\u2ee5"));
        test.add("b = \\u2EE5jkdfskjfd", new TerminalClauseImpl("b", EQUALS, "\u2ee5jkdfskjfd"));
        test.add("b not in 'jack says, \"Hello World!\"'", new TerminalClauseImpl("b", NOT_IN, "jack says, \"Hello World!\""));
        test.add("b not in 'jack says, \\'Hello World!\\''", new TerminalClauseImpl("b", NOT_IN, "jack says, 'Hello World!'"));
        test.add("b not in \"jack says, 'Hello World!'\"", new TerminalClauseImpl("b", NOT_IN, "jack says, 'Hello World!'"));
        test.add("b not in \"jack says, \\\"Hello World!'\\\"\"", new TerminalClauseImpl("b", NOT_IN, "jack says, \"Hello World!'\""));
        test.add("b not in \"jack says, \\tnothing\"", new TerminalClauseImpl("b", NOT_IN, "jack says, \tnothing"));
        test.add("bad ~ wt\\u007f", new TerminalClauseImpl("bad", LIKE, "wt\u007f"));

        //tests for escaping.
        test.add("priority = \"a \\n new \\r line\"", new TerminalClauseImpl("priority", EQUALS, "a \n new \r line"));
        test.add("priority = \"Tab:\\t NewLine:\\n Carrage Return:\\r\"", new TerminalClauseImpl("priority", EQUALS, "Tab:\t NewLine:\n Carrage Return:\r"));
        test.add("priority = \"Quote:\\\" Single:\\' Back Slash:\\\\ Space:\\ \"", new TerminalClauseImpl("priority", EQUALS, "Quote:\" Single:' Back Slash:\\ Space: "));
        test.add("priority = \"Unicode: \\ufeeF1 Unicode2: \\u6EEF\"", new TerminalClauseImpl("priority", EQUALS, "Unicode: \ufeef1 Unicode2: \u6eef"));
        test.add("priority = 'Escape\" don\\'t'", new TerminalClauseImpl("priority", EQUALS, "Escape\" don't"));
        test.add("priority = \"Escape' don\\\"t\"", new TerminalClauseImpl("priority", EQUALS, "Escape' don\"t"));

        //Some tests for function calls.
        test.add("priority = higherThan(Major)", new TerminalClauseImpl("priority", EQUALS, new FunctionOperand("higherThan", Arrays.asList("Major"))));
        test.add("priority In     randomName(Major, Minor,      \"cool\", -65784)", new TerminalClauseImpl("priority", IN, new FunctionOperand("randomName", Arrays.asList("Major", "Minor", "cool", "-65784"))));
        test.add("priority    >=    randomName()", new TerminalClauseImpl("priority", GREATER_THAN_EQUALS, new FunctionOperand("randomName", Collections.<String>emptyList())));
        test.add(String.format("pri not in func(%d)", tooBigNumber), new TerminalClauseImpl("pri", NOT_IN, new FunctionOperand("func", Collections.singletonList(tooBigNumber.toString()))));
        test.add("pri not in fun(name\\u0082e)", new TerminalClauseImpl("pri", NOT_IN, new FunctionOperand("fun", "name\u0082e")));

        //test for strange function names.
        test.add("a = func\\'  ()", new TerminalClauseImpl("a", EQUALS, new FunctionOperand("func'")));
        test.add("a = fu\\\"nc\\'()", new TerminalClauseImpl("a", EQUALS, new FunctionOperand("fu\"nc'")));
        test.add("a=function\\ name(  )", new TerminalClauseImpl("a", EQUALS, new FunctionOperand("function name")));
        test.add("a = \u2ee5()", new TerminalClauseImpl("a", EQUALS, new FunctionOperand("\u2ee5")));
        test.add("a = somereallystrangestring\\u2ee5()", new TerminalClauseImpl("a", EQUALS, new FunctionOperand("somereallystrangestring\u2ee5")));
        test.add("version <= \"affected\\ versions\"(   )", new TerminalClauseImpl("version", LESS_THAN_EQUALS, new FunctionOperand("affected versions")));
        test.add("version <= \"affected\\ versio'ns\"(   )", new TerminalClauseImpl("version", LESS_THAN_EQUALS, new FunctionOperand("affected versio'ns")));
        test.add("version <= \"affected versio\\\"ns\"(   )", new TerminalClauseImpl("version", LESS_THAN_EQUALS, new FunctionOperand("affected versio\"ns")));
        test.add("version <= 'my messed up versio\\'ns'     (   )", new TerminalClauseImpl("version", LESS_THAN_EQUALS, new FunctionOperand("my messed up versio'ns")));
        test.add("version <= 'my m\\nessed up\\ versio\"ns'     (   )", new TerminalClauseImpl("version", LESS_THAN_EQUALS, new FunctionOperand("my m\nessed up versio\"ns")));
        test.add("version <= 4759879855`(   )", new TerminalClauseImpl("version", LESS_THAN_EQUALS, new FunctionOperand("4759879855`")));
        test.add("version <= 4759879(   )", new TerminalClauseImpl("version", LESS_THAN_EQUALS, new FunctionOperand("4759879")));
        test.add("version = badname\\u0091", new TerminalClauseImpl("version", EQUALS, "badname\u0091"));


        //test some of the string breaks
        test.add("a=b&c=d", new AndClause(new TerminalClauseImpl("a", EQUALS, "b"), new TerminalClauseImpl("c", EQUALS, "d")));
        test.add("a=b&&c=d", new AndClause(new TerminalClauseImpl("a", EQUALS, "b"), new TerminalClauseImpl("c", EQUALS, "d")));
        test.add("a=b|c=d", new OrClause(new TerminalClauseImpl("a", EQUALS, "b"), new TerminalClauseImpl("c", EQUALS, "d")));
        test.add("a=b||c=d", new OrClause(new TerminalClauseImpl("a", EQUALS, "b"), new TerminalClauseImpl("c", EQUALS, "d")));
        test.add("a<b", new TerminalClauseImpl("a", LESS_THAN, "b"));
        test.add("a>b", new TerminalClauseImpl("a", GREATER_THAN, "b"));
        test.add("a~b", new TerminalClauseImpl("a", LIKE, "b"));

        {
            //Check the and operator.
            final Clause priorityEqualsMajor = new TerminalClauseImpl("priority", EQUALS, "major");
            final Clause fooGtBarFunc = new TerminalClauseImpl("foo", GREATER_THAN, new FunctionOperand("bar"));
            test.add("priority = major and foo > bar()", new AndClause(priorityEqualsMajor, fooGtBarFunc));
            test.add("priority = majorand and foo>bar()", new AndClause(new TerminalClauseImpl("priority", EQUALS, "majorand"), fooGtBarFunc));
            test.add("priority = major and foo > bar()", new AndClause(priorityEqualsMajor, fooGtBarFunc));
            test.add("priority != major    and      foo >      bar()", new AndClause(new TerminalClauseImpl("priority", NOT_EQUALS, "major"), fooGtBarFunc));
            test.add("priority != major    &&      foo >      bar()", new AndClause(new TerminalClauseImpl("priority", NOT_EQUALS, "major"), fooGtBarFunc));
            test.add("priority != andmajor    &      foo >      bar()", new AndClause(new TerminalClauseImpl("priority", NOT_EQUALS, "andmajor"), fooGtBarFunc));
            test.add("priority != andmajor    and      foo >      bar() order by priority     DESC,      foo",
                    new AndClause(new TerminalClauseImpl("priority", NOT_EQUALS, "andmajor"), fooGtBarFunc),
                    new OrderByImpl(new SearchSort("priority", SortOrder.DESC), new SearchSort("foo")));

            //Check the or operator.
            final Clause priorityEqualsMinor = new TerminalClauseImpl("priority", EQUALS, "minor");
            test.add("priority = major or foo > bar()", new OrClause(priorityEqualsMajor, fooGtBarFunc));
            test.add("priority = major or foo > bar()", new OrClause(priorityEqualsMajor, fooGtBarFunc));
            test.add("priority = major or foo > bar() or priority = minor", new OrClause(priorityEqualsMajor, fooGtBarFunc, priorityEqualsMinor));
            test.add("priority = major || foo > bar() | priority = minor", new OrClause(priorityEqualsMajor, fooGtBarFunc, priorityEqualsMinor));
            test.add("priority = major or foo > bar() || priority = minor", new OrClause(priorityEqualsMajor, fooGtBarFunc, priorityEqualsMinor));

            //Checks for operator precedence for and and or.
            final Clause fooGtBarFunc123 = new TerminalClauseImpl("foo", GREATER_THAN, new FunctionOperand("bar", Arrays.asList("1", "2", "3")));
            final Clause priorityFooAnd = new AndClause(priorityEqualsMajor, fooGtBarFunc123);
            final Clause bazNotEqual1234 = new TerminalClauseImpl("baz", NOT_EQUALS, 1234L);
            final Clause priorityBazAnd = new AndClause(priorityEqualsMinor, bazNotEqual1234);
            final Clause outerOr = new OrClause(priorityFooAnd, priorityBazAnd);
            test.add("priority = major and foo > bar(1,2,3) | priority = minor and baz != 1234", outerOr);
            test.add("priority =     major AND foo > bar(1,2,3) oR priority = minor and baz != 1234", outerOr);
            test.add("priority=major and foo>bar(1,2,3)|| priority=minor  and  baz!=1234", outerOr);
            test.add("priority = major AND foo > bar(1,2,3) Or priority = minor AND baz != 1234", outerOr);

            //Another test for the and operator.
            test.add("priority = major && foo > bar(1,2,3) & priority = minor and baz != 1234", new AndClause(priorityEqualsMajor, fooGtBarFunc123, priorityEqualsMinor, bazNotEqual1234));

            // use parentheses to overthrow precedence
            test.add("priority = major and (foo > bar(1,2,3) | priority = minor) and baz != 1234", new AndClause(priorityEqualsMajor, new OrClause(fooGtBarFunc123, priorityEqualsMinor), bazNotEqual1234));

            //make sure the precedence still works with the brackets.
            test.add("priority = major or (foo > bar(1,2,3) or priority = minor) && baz != 1234", new OrClause(priorityEqualsMajor, new AndClause(new OrClause(fooGtBarFunc123, priorityEqualsMinor), bazNotEqual1234)));

            //test for the not operator.
            test.add("not priority = major or foo > bar() or priority = minor", new OrClause(new NotClause(priorityEqualsMajor), fooGtBarFunc, priorityEqualsMinor));
            test.add("not priority = major or foo > bar() AnD priority=\"minor\"", new OrClause(new NotClause(priorityEqualsMajor), new AndClause(fooGtBarFunc, priorityEqualsMinor)));
            test.add("not priority = major or not foo > bar() AnD priority=\"minor\"", new OrClause(new NotClause(priorityEqualsMajor), new AndClause(new NotClause(fooGtBarFunc), priorityEqualsMinor)));
            test.add("not (priority = major or not foo > bar()) AnD priority=\"minor\"", new AndClause(new NotClause(new OrClause(priorityEqualsMajor, new NotClause(fooGtBarFunc))), priorityEqualsMinor));

            //check the '!' operator.
            test.add("! (priority = major or ! foo > bar()) AnD priority=\"minor\"", new AndClause(new NotClause(new OrClause(priorityEqualsMajor, new NotClause(fooGtBarFunc))), priorityEqualsMinor));

            //test that a double not also works.
            test.add("not ! (! priority = major or     foo >bar()) &         priority=\"minor\"", new AndClause(new NotClause(new NotClause(new OrClause(new NotClause(priorityEqualsMajor), fooGtBarFunc))), priorityEqualsMinor));
        }

        //Tests to make sure illegal characters can be escaped.
        for (int i = 0; i < illgealCharsString.length(); i++)
        {
            final char currentChar = illgealCharsString.charAt(i);
            final String fieldName = String.format("test%cdfjd", currentChar);
            test.add(String.format("'%s' = 'good'", fieldName), new TerminalClauseImpl(fieldName, EQUALS, "good"));
        }

        //Make sure the reserved words can be escaped.
        for (final String reservedWord : JqlStringSupportImpl.RESERVED_WORDS)
        {
            test.add(String.format("priority NOT IN ('%s')", reservedWord), new TerminalClauseImpl("priority", NOT_IN, new MultiValueOperand(reservedWord)));
        }

        //We want to be able to add sort when there is no where clause.
        test.add("order by crap", null, new OrderByImpl(new SearchSort("crap")));
        test.add("order by crap  DESC", null, new OrderByImpl(new SearchSort("crap", SortOrder.DESC)));
        test.add("order by crap  ASC", null, new OrderByImpl(new SearchSort("crap", SortOrder.ASC)));
        test.add("order by cf[12345]  ASC", null, new OrderByImpl(new SearchSort("cf[12345]", SortOrder.ASC)));

        test.add("", null, null);
        test.add("       ", null, null);

        //Some tests for invalid parsing input.
        {
            test.addException("Tabs and Spaces String", "  \t  \n\r\f   ", JqlParseErrorMessages.illegalCharacter('\f', 2, 1));
            test.addException("Bare field", "foo", JqlParseErrorMessages.badOperator(createEOFToken(1, 3)));
            test.addException("Missing operand", "foo=", JqlParseErrorMessages.badOperand(createEOFToken(1, 4)));
            test.addException("Just an operator", "=", JqlParseErrorMessages.badFieldName(createToken("=", 1, 0)));
            test.addException("Just a two character operator", "!=", JqlParseErrorMessages.badFieldName(createToken("!=", 1, 0)));
            test.addException("Missing operator in first subclause of otherwise OK composite logical clause", "foo bar and 78foo = bar", JqlParseErrorMessages.badOperator(createToken("bar", 1, 4)));
            test.addException("Two logical operators only", "and and", JqlParseErrorMessages.badFieldName(createToken("and", 1, 0)));
            test.addException("missing logical operator", "a=b a=b", JqlParseErrorMessages.needLogicalOperator(createToken("a", 1, 4)));
            test.addException("Legal subclause with missing second subclause", "foo=bar and", JqlParseErrorMessages.badFieldName(createEOFToken(1, 11)));
            test.addException("Legal subclause with malformed second subclause", "foo=bar and and", JqlParseErrorMessages.badFieldName(createToken("and", 1, 12)));
            test.addException("Incomplete second subclause", "foo=bar and 78foo", JqlParseErrorMessages.badOperator(createEOFToken(1, 17)));
            test.addException("Incomplete second subclause2", "foo=bar and 78foo=", JqlParseErrorMessages.badOperand(createEOFToken(1, 18)));
            test.addException("Incomplete second subclause with newline", "foo=bar and \n78foo", JqlParseErrorMessages.badOperator(createEOFToken(2, 5)));
            test.addException("Middle subclause invalid", "foo=bar and 78foo brenden and a=b", JqlParseErrorMessages.badOperator(createToken("brenden", 1, 18)));
            test.addException("Middle subclause invalid with newline", "foo=bar and \n78foo brenden and a=b", JqlParseErrorMessages.badOperator(createToken("brenden", 2, 6)));
            test.addException("Second subclause missing with not", "foo=bar and not", JqlParseErrorMessages.badFieldName(createEOFToken(1, 15)));
            test.addException("Second subclause incomplete with not", "foo=bar and not foo =", JqlParseErrorMessages.badOperand(createEOFToken(1, 21)));
            test.addException("Just not", "not", JqlParseErrorMessages.badFieldName(createEOFToken(1, 3)));
            test.addException("two nots", "not not", JqlParseErrorMessages.badFieldName(createEOFToken(1, 7)));
            test.addException("not instead of field", "a=b and not not=b", JqlParseErrorMessages.badFieldName(createToken("=", 1, 15)));
            test.addException("missing logical operator (not instead)", "a=b not a=b", JqlParseErrorMessages.needLogicalOperator(createToken("not", 1, 4)));
            test.addException("just one paren", "(", JqlParseErrorMessages.badFieldName(createEOFToken(1, 1)));
            test.addException("empty list", "abc = ()", JqlParseErrorMessages.badOperand(createToken(")", 1, 7)));
            test.addException("empty list with like", "abc ~ ()", JqlParseErrorMessages.badOperand(createToken(")", 1, 7)));

            test.addException("complex nested parens don't match", "abc in ((fee, fie, foe, fum), 787, (34, (45))", JqlParseErrorMessages.expectedText(createEOFToken(1, 45), ")"));
            test.addException("Random crap on the end", "priority = 12345=== not p jfkff fjfjfj", JqlParseErrorMessages.needLogicalOperator(createToken("=", 1, 16)));
            test.addException("Random crap on the end2", "priority = 12345 \njfkff fjfjfj", JqlParseErrorMessages.needLogicalOperator(createToken("jfkff", 2, 0)));
            test.addException("Random crap on the end3", "priority=a jfkff=fjfjfj", JqlParseErrorMessages.needLogicalOperator(createToken("jfkff", 1, 11)));
            test.addException("Random crap on the end4", "priority=12345 jfkff=fjfjfj", JqlParseErrorMessages.needLogicalOperator(createToken("jfkff", 1, 15)));
            test.addException("Random crap on the end5", "a=b ,b", JqlParseErrorMessages.needLogicalOperator(createToken(",", 1, 4)));
            test.addException("Random crap on the end6", "a=b,b", JqlParseErrorMessages.needLogicalOperator(createToken(",", 1, 3)));

            //Come parens tests.
            test.addException("backwards parens", "abc = )foo(", JqlParseErrorMessages.badOperand(createToken(")", 1, 6)));
            test.addException("unmatched parens (open only)", "(a in a", JqlParseErrorMessages.expectedText(createEOFToken(1, 7), ")"));
            test.addException("unmatched parens (open only)", "abc in (foo", JqlParseErrorMessages.expectedText(createEOFToken(1, 11), ")"));
            test.addException("unmatched parens (open open)", "abc in (foo(", JqlParseErrorMessages.expectedText(createEOFToken(1, 12), ")"));
            test.addException("unmatched parens (double open)", "abc IN ((foo) abc = a", JqlParseErrorMessages.expectedText(createToken("abc", 1, 14), ")"));
            test.addException("unmatched parens (open close close)", "abc in (foo))", JqlParseErrorMessages.needLogicalOperator(createToken(")", 1, 12)));
            test.addException("unmatched parens (open open)", "abc in ((foo), a", JqlParseErrorMessages.expectedText(createEOFToken(1, 16), ")"));
            test.addException("unmatched parens (open open close)", "(abc  =        b or not (j=k and p=l)", JqlParseErrorMessages.expectedText(createEOFToken(1, 37), ")"));

            //Some CF tests.
            test.addException("Unclosed CF label", "cf[1234 = x", JqlParseErrorMessages.expectedText(createToken("=", 1, 8), "]"));
            test.addException("Unopened CF label", "cf1234] = x", JqlParseErrorMessages.badOperator(createToken("]", 1, 6)));
            test.addException("Non-numeric id in CF label", "cf[z123] = x", JqlParseErrorMessages.badCustomFieldId(createToken("z123", 1, 3)));
            test.addException("Negative numeric id in CF label", "cf[-123] = x", JqlParseErrorMessages.badCustomFieldId(createToken("-123", 1, 3)));
            test.addException("Too big numeric id in CF label", String.format("cf[%d] = x", tooBigNumber), JqlParseErrorMessages.illegalNumber(tooBigNumber.toString(), 1, 3));
            test.addException("Empty id in CF label", "cf[] = x", JqlParseErrorMessages.badCustomFieldId(createToken("]", 1, 3)));
            test.addException("Missing CF in CF label", "[54] = x", JqlParseErrorMessages.badFieldName(createToken("[", 1, 0, JqlLexer.LBRACKET)));
            test.addException("Custom Field (cf) without brackets", "cf = brenden", JqlParseErrorMessages.expectedText(createToken("=", 1, 3), "["));

            //Some property tests.
            test.addException("Unclosed property label", "issue.property[x = x", JqlParseErrorMessages.expectedText(createToken("=", 1, 17), "]"));
            test.addException("Unopened property label", "issue.property1234] = x", JqlParseErrorMessages.badOperator(createToken("]", 1, 18)));
            test.addException("Property without operand", "issue.property[x.1] = ", JqlParseErrorMessages.badOperand(createEOFToken(1, 22)));
            test.addException("Property without operator", "issue.property[x.1] unresolved", JqlParseErrorMessages.badOperator(createToken("unresolved", 1, 20)));
            test.addException("Missing property in property label", "[issue.status] = x", JqlParseErrorMessages.badFieldName(createToken("[", 1, 0, JqlLexer.LBRACKET)));
            test.addException("Empty property in property label", "issue.property[] = x", JqlParseErrorMessages.badPropertyArgument(createToken("]", 1, 15)));
            test.addException("Two dots in label", "comment.prop[author]..Author = Test", JqlParseErrorMessages.badPropertyArgument(createToken("..Author", 1, 20)));

            //Lets test some bad function arguments.
            test.addException("Bad function argument", "q = func( [", JqlParseErrorMessages.badFunctionArgument(createToken("[", 1, 10)));
            test.addException("Bad function argument2", "q = func(a, [)", JqlParseErrorMessages.badFunctionArgument(createToken("[", 1, 12)));
            test.addException("Bad function argument3", "q = func(a, [", JqlParseErrorMessages.badFunctionArgument(createToken("[", 1, 12)));
            test.addException("Bad function argument4", "q = func(a, )", JqlParseErrorMessages.emptyFunctionArgument(createToken(")", 1, 12)));
            test.addException("Bad function argument5", "q = func(a, ", JqlParseErrorMessages.badFunctionArgument(createEOFToken(1, 12)));
            test.addException("Bad function argument6", "q = func(a", JqlParseErrorMessages.expectedText(createEOFToken(1, 10), ")"));
            test.addException("Bad function argument7", "q = func(", JqlParseErrorMessages.expectedText(createEOFToken(1, 9), ")"));
            test.addException("Bad function argument8", "q = func(good, jack!, \"really good\")", JqlParseErrorMessages.expectedText(createToken("!", 1, 19), ")", ","));

            test.addException("No such operator", "pri notin (x)", JqlParseErrorMessages.reservedWord("notin", 1, 4));
            test.addException("No such operator2", "pri isnot empty", JqlParseErrorMessages.badOperator(createToken("isnot", 1, 4)));
            test.addException("No such operator3", "pri ^ empty", JqlParseErrorMessages.reservedCharacter('^', 1, 4));
            test.addException("No such operator4", "pri is", JqlParseErrorMessages.badOperand(createEOFToken(1, 6)));
            test.addException("No such operator5", "pri not is empty", JqlParseErrorMessages.expectedText(createToken("is", 1, 8), "IN"));
            test.addException("Bang does not work for 'not in'", "pri ! in (test)", JqlParseErrorMessages.badOperator(createToken("!", 1, 4)));
            test.addException("Bang does not work for 'not empty'", "pri is ! empty", JqlParseErrorMessages.expectedText(createToken("!", 1, 7), "NOT"));

            test.addException("Unmatched string quote", "priority = \"\"\"", JqlParseErrorMessages.unfinishedString(null, 1, 13));
            test.addException("Unmatched string quote2", "priority = \"", JqlParseErrorMessages.unfinishedString(null, 1, 11));
            test.addException("Unmatched string quote3", "priority = '''", JqlParseErrorMessages.unfinishedString(null, 1, 13));
            test.addException("Unmatched string quote4", "priority = '", JqlParseErrorMessages.unfinishedString(null, 1, 11));

            test.addException("Unescaped single quote", "what = hrejw'ewjrhejkw", JqlParseErrorMessages.unfinishedString("ewjrhejkw", 1, 12));
            test.addException("Unescaped quote", "wh\"at = hrejwewjrhejkw", JqlParseErrorMessages.unfinishedString("at = hrejwewjrhejkw", 1, 2));

            test.addException("Empty Field Name", "'' = bad", JqlParseErrorMessages.emptyFieldName(1, 0));
            test.addException("Empty Field Name2", "\"\" = bad", JqlParseErrorMessages.emptyFieldName(1, 0));
            test.addException("Empty Escaped Field Name", "\\     < 38", JqlParseErrorMessages.emptyFieldName(1, 0));
            test.addException("Empty Function Name", "a \n= ''()", JqlParseErrorMessages.emptyFunctionName(2, 2));
            test.addException("Empty Function Name2", "b = \"\"()", JqlParseErrorMessages.emptyFunctionName(1, 4));

            test.addException("Unterminated escape", "test = case\\", JqlParseErrorMessages.illegalEsacpe(null, 1, 11));
            test.addException("Illegal escape", "test = case\\k", JqlParseErrorMessages.illegalEsacpe("\\k", 1, 11));
            test.addException("Unterminated illegal unicode", "test = case\\u", JqlParseErrorMessages.illegalEsacpe("\\u", 1, 11));
            test.addException("Unterminated illegal unicode2", "test = case\\u278q", JqlParseErrorMessages.illegalEsacpe("\\u278q", 1, 11));
            test.addException("Unterminated illegal unicode3", "test = case\\u27", JqlParseErrorMessages.illegalEsacpe("\\u27", 1, 11));
            test.addException("Unterminated illegal unicode4", "test = case\\u-998", JqlParseErrorMessages.illegalEsacpe("\\u-", 1, 11));
            test.addException("Unterminated illegal unicode5", "test = case order by \\u-998", JqlParseErrorMessages.illegalEsacpe("\\u-", 1, 21));
            test.addException("Unterminated illegal unicode6", "test = case\\uzzzz", JqlParseErrorMessages.illegalEsacpe("\\uz", 1, 11));
            test.addException("Unterminated illegal unicode7", "test = case\\u278qzzzz", JqlParseErrorMessages.illegalEsacpe("\\u278q", 1, 11));
            test.addException("Unterminated illegal unicode8", "test = case\\u27zzzzz", JqlParseErrorMessages.illegalEsacpe("\\u27z", 1, 11));
            test.addException("Unterminated illegal unicode9", "tecase\\u-998zzzzz", JqlParseErrorMessages.illegalEsacpe("\\u-", 1, 6));
            test.addException("Unterminated illegal unicod10", "test = case order by \\u-998zzzzz", JqlParseErrorMessages.illegalEsacpe("\\u-", 1, 21));

            test.addException("Number too big", String.format("priority = %d", tooBigNumber), JqlParseErrorMessages.illegalNumber(tooBigNumber.toString(), 1, 11));
            test.addException("Number too big 2", String.format("priority in  (%d, 2)", tooBigNumber), JqlParseErrorMessages.illegalNumber(tooBigNumber.toString(), 1, 14));

            test.addException("Control Character String", "c = q\uffff", JqlParseErrorMessages.illegalCharacter('\uffff', 1, 5));
            test.addException("Control Character String 2", "c = c\ufdd0", JqlParseErrorMessages.illegalCharacter('\ufdd0', 1, 5));
            test.addException("Control Character String 3", "c = aa\ufdd0kkk", JqlParseErrorMessages.illegalCharacter('\ufdd0', 1, 6));
            test.addException("Control Character String 4", "\u0000iuyiuyiu", JqlParseErrorMessages.illegalCharacter('\u0000', 1, 0));
            test.addException("Control Character String 5", "aa = bb order by \u0001", JqlParseErrorMessages.illegalCharacter('\u0001', 1, 17));

            test.addException("Control Character Quoted", "control = \"char\ufffe\"", JqlParseErrorMessages.illegalCharacter('\ufffe', 1, 15));
            test.addException("Control Character Quoted 2", "control = \"char\ufdef\"", JqlParseErrorMessages.illegalCharacter('\ufdef', 1, 15));
            test.addException("Control Character Quoted 3", "\"\u001f\"", JqlParseErrorMessages.illegalCharacter('\u001f', 1, 1));
            test.addException("Control Character Quoted 4", "control = \"char\b\"", JqlParseErrorMessages.illegalCharacter('\b', 1, 15));
            test.addException("Control Character Quoted 5", "control = a order by \"char\b\"", JqlParseErrorMessages.illegalCharacter('\b', 1, 26));

            test.addException("Control Character Single Quoted", "control\n = 'char\u0002'", JqlParseErrorMessages.illegalCharacter('\u0002', 2, 8));
            test.addException("Control Character Single Quoted 2", "control = '\ufdd5'", JqlParseErrorMessages.illegalCharacter('\ufdd5', 1, 11));
            test.addException("Control Character Single Quoted 3", "control = '\ufdef'", JqlParseErrorMessages.illegalCharacter('\ufdef', 1, 11));
            test.addException("Control Character Single Quoted 4", "control = 'char\t'", JqlParseErrorMessages.illegalCharacter('\t', 1, 15));
            test.addException("Control Character Single Quoted 5", "control = a order by 'char\t'", JqlParseErrorMessages.illegalCharacter('\t', 1, 26));

            test.addException("Control Character Field Name", "cont\nrol = ''", JqlParseErrorMessages.badOperator(createToken("rol", 2, 0)));
            test.addException("Control Character Function Name", "control = f\run()", JqlParseErrorMessages.needLogicalOperator(createToken("un", 1, 12)));
            test.addException("Control Character Function Argument", "control = fun('\uffff')", JqlParseErrorMessages.illegalCharacter('\uffff', 1, 15));

            test.addException("Empty Order By", "a = b order by", JqlParseErrorMessages.badFieldName(createEOFToken(1, 14)));
            test.addException("Empty Order By2", "a = b order", JqlParseErrorMessages.expectedText(createEOFToken(1, 11), "by"));
            test.addException("Empty Order By3", "a = b order bad", JqlParseErrorMessages.expectedText(createToken("bad", 1, 12), "by"));
            test.addException("Unfinished Order By", "a = b order BY abc desc, ", JqlParseErrorMessages.badFieldName(createEOFToken(1, 25)));
            test.addException("Unfinished Order By 2", "a = b order BY abc desc, ]", JqlParseErrorMessages.badFieldName(createToken("]", 1, 25)));
            test.addException("Bad Order", "order BY abc s", JqlParseErrorMessages.badSortOrder(createToken("s", 1, 13)));
            test.addException("Bad Name Order", "order BY desc", JqlParseErrorMessages.badFieldName(createToken("desc", 1, 9)));
            test.addException("Order by Extra", "order BY era desc extra", JqlParseErrorMessages.expectedText(createToken("extra", 1, 18), ","));

            test.addException("Test newline in value", "query = hello\nworld", JqlParseErrorMessages.needLogicalOperator(createToken("world", 2, 0)));

            //Tests to make sure illegal characters cause an error.
            for (int i = 0; i < illgealCharsString.length(); i++)
            {
                final char currentChar = illgealCharsString.charAt(i);
                test.addException(String.format("Error with unescaped '%c'.", currentChar), String.format("test%cdfjd = 'bad'", currentChar), JqlParseErrorMessages.reservedCharacter(currentChar, 1, 4));
            }

            //Make sure that unquoted reserved words cause an error.
            final Set<String> words = new HashSet<String>(JqlStringSupportImpl.RESERVED_WORDS);
            words.removeAll(currentWords);
            for (final String reservedWord : words)
            {
                test.addException(String.format("Unquoted reserved word '%s'.", reservedWord), String.format("priority NOT IN %s('arg', 10)", reservedWord), JqlParseErrorMessages.reservedWord(reservedWord, 1, 16));
            }
        }

        //Some lexical errors
        {
            //This test may no longer be valid when we expand the queries.
            test.addException("Lexical errors at end", "f\n = \n \n abc *", JqlParseErrorMessages.reservedCharacter('*', 4, 5));
            test.addException("Lexical errors in middle", "f *= abc", JqlParseErrorMessages.reservedCharacter('*', 1, 2));
        }

        //Tests for reserved characters.
        for (int i = 0; i < illgealCharsString.length(); i++)
        {
            final char currentChar = illgealCharsString.charAt(i);
            test.addException("Reserved character " + currentChar, "bbain = " + currentChar, JqlParseErrorMessages.reservedCharacter(currentChar, 1, 8));
        }

        test.add(JqlParserIsValidCheckTest.class);

        // was clause change history queries
        test.add("status was open", new WasClauseImpl("status", Operator.WAS, new SingleValueOperand("open"), null));
        test.addException("Incomplete was clause", "status was", JqlParseErrorMessages.badOperand(createEOFToken(1, 10)));

        return test.suite;
    }

    private static com.atlassian.fugue.Option<Property> property(final String property, final String objectReferences)
    {
        final Splitter splitter = Splitter.on('.').omitEmptyStrings();
        final ArrayList<String> objReferences = StringUtils.isEmpty(objectReferences) ? Lists.<String>newArrayList() : Lists.newArrayList(splitter.split(objectReferences));
        return com.atlassian.fugue.Option.some(new Property(Lists.newArrayList(splitter.split(property)), objReferences));
    }

    private static Token createEOFToken(int line, int position)
    {
        return createToken(null, line, position, Token.EOF);
    }

    private static Token createToken(String text, int line, int position)
    {
        return createToken(text, line, position, 10);
    }

    private static Token createToken(String text, int line, int position, final int type)
    {
        final CommonToken token = new CommonToken(type);
        token.setLine(line);
        token.setCharPositionInLine(position);
        token.setText(text);
        return token;
    }

    private void add(Class<? extends TestCase> testClass)
    {
        suite.addTestSuite(testClass);
    }

    private void add(String jql, Clause expectedClause)
    {
        add(jql, expectedClause, new OrderByImpl());
    }

    private void add(String jql, Clause expectedClause, final OrderBy orderBy)
    {
        suite.addTest(new JqlParserTest(jql, expectedClause, (orderBy == null) ? new OrderByImpl() : orderBy));
    }

    private void addException(final String description, final String jql, final JqlParseErrorMessage errorMessage)
    {
        suite.addTest(new JqlParserErrorTest(description, jql, errorMessage));
    }

    /**
     * @since v4.0
     */
    private static class JqlParserTest extends Junit3ListeningTestCase
    {
        private final String inputJql;
        private final Clause expectedClause;
        private final OrderBy orderBy;

        public JqlParserTest(final String inputJql, final Clause expectedClause, final OrderBy orderBy)
        {
            super("Generated test for input: '" + inputJql + "'");
            this.expectedClause = expectedClause;
            this.inputJql = inputJql;
            this.orderBy = orderBy;
        }

        public String getInputJql()
        {
            return inputJql;
        }

        public Clause getExpectedClause()
        {
            return expectedClause;
        }

        @Override
        protected void runTest() throws Throwable
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            final Query query = parser.parseQuery(inputJql);
            final Clause whereClause = query.getWhereClause();
            assertEquals(inputJql, query.getQueryString());
            assertEquals(expectedClause, whereClause);
            assertEquals(orderBy, query.getOrderByClause());
        }
    }

    /**
     * @since v4.0
     */
    private static class JqlParserErrorTest extends Junit3ListeningTestCase
    {
        private final String desc;
        private final String inputJql;
        private final JqlParseErrorMessage parseError;

        public JqlParserErrorTest(final String desc, final String inputJql, final JqlParseErrorMessage parseError)
        {
            super(desc);
            this.desc = desc;
            this.inputJql = inputJql;
            this.parseError = parseError;
        }

        public String getInputJql()
        {
            return inputJql;
        }

        @Override
        protected void runTest()
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            try
            {
                final Query query = parser.parseQuery(inputJql);
                fail(desc + ": Expected exception on input '" + inputJql + "' but got query '" + query + "'");
            }
            catch (final JqlParseException e)
            {
                JqlParseErrorMessage actual = e.getParseErrorMessage();
                assertEquals(parseError, actual);
            }
        }
    }

    public static class JqlParserIsValidCheckTest extends Junit3ListeningTestCase
    {
        public void testIsValidFieldNameGood() throws Exception
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            final CollectionBuilder<String> goodBuilder = createGoodList().addAll("cf[4784]", "cf [4784]", "cf[000001]", "8543859843095843098540938398493");
            assertGood(goodBuilder.asList(), new Function<String, Boolean>()
            {
                public Boolean get(final String input)
                {
                    return parser.isValidFieldName(input);
                }
            });
        }

        public void testIsValidFieldNameBad() throws Exception
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            assertBad(createBadList().asList(), new Function<String, Boolean>()
            {
                public Boolean get(final String input)
                {
                    return parser.isValidFieldName(input);
                }
            });
        }

        public void testIsValidFunctionArgumentGood() throws Exception
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            final CollectionBuilder<String> goodBuilder = createGoodList().add("8543859843095843098540938398493");
            assertGood(goodBuilder.asList(), new Function<String, Boolean>()
            {
                public Boolean get(final String input)
                {
                    return parser.isValidFunctionArgument(input);
                }
            });
        }

        public void testIsValidFunctionArgumentBad() throws Exception
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            final CollectionBuilder<String> badBuilder = createBadList().addAll("cf[9202]");
            assertBad(badBuilder.asList(), new Function<String, Boolean>()
            {
                public Boolean get(final String input)
                {
                    return parser.isValidFunctionArgument(input);
                }
            });
        }

        public void testIsValidFunctionNameGood() throws Exception
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            final CollectionBuilder<String> goodBuilder = createGoodList().add("8543859843095843098540938398493");
            assertGood(goodBuilder.asList(), new Function<String, Boolean>()
            {
                public Boolean get(final String input)
                {
                    return parser.isValidFunctionName(input);
                }
            });
        }

        public void testIsValidFunctionNameBad() throws Exception
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            final CollectionBuilder<String> badBuilder = createBadList().addAll("cf[27483]");
            assertBad(badBuilder.asList(), new Function<String, Boolean>()
            {
                public Boolean get(final String input)
                {
                    return parser.isValidFunctionName(input);
                }
            });
        }

        public void testIsValidValueGood() throws Exception
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            assertGood(createGoodList().asList(), new Function<String, Boolean>()
            {
                public Boolean get(final String input)
                {
                    return parser.isValidValue(input);
                }
            });
        }

        public void testIsValidValueBad() throws Exception
        {
            final DefaultJqlQueryParser parser = new DefaultJqlQueryParser();
            final CollectionBuilder<String> badBuilder = createBadList().addAll("cf[1]", "8543859843095843098540938398493");
            assertBad(badBuilder.asList(), new Function<String, Boolean>()
            {
                public Boolean get(final String input)
                {
                    return parser.isValidValue(input);
                }
            });
        }

        private static CollectionBuilder<String> createGoodList()
        {
            return CollectionBuilder.newBuilder("nicename", "102748", "niceunicodename\u3737");
        }

        private static CollectionBuilder<String> createBadList()
        {
            final CollectionBuilder<String> builder = CollectionBuilder.newBuilder("namewith\\bslash", "\\namewithbcontrol",
                    "bad name spaces", "badnamewithescapes\n", "badnamewithescapes\\u5775", "'badnamewithsquote",
                    "\"badnamewithquote", "badescape\\k", "badquote\"name", "badsinglequote\'name", "", " ", "cf[ 38", "cf[aaa]",
                    "cf[-1232]");

            for (int i = 0; i < illgealCharsString.length(); i++)
            {
                builder.add(String.format("String%cwithillegalchar", illgealCharsString.charAt(i)));
            }

            builder.addAll(JqlStringSupportImpl.RESERVED_WORDS);

            return builder;
        }

        private static void assertBad(final List<String> values, final Function<String, Boolean> function)
        {
            for (final String value : values)
            {
                assertFalse(String.format("Expected string '%s' to be invalid.", value), function.get(value));
            }
        }

        private static void assertGood(final List<String> values, final Function<String, Boolean> function)
        {
            for (final String value : values)
            {
                assertTrue(String.format("Expected string '%s' to be valid.", value), function.get(value));
            }
        }
    }
}
