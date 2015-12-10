package com.atlassian.jira.webtests.ztests.navigator.jql;

import java.util.Collection;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;

import com.sun.jersey.api.client.UniformInterfaceException;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static org.junit.Assert.assertThat;

/**
 * Tests the JQL parser and how it handles reserved words.
 *
 * @since v6.2
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestJqlParsing extends FuncTestCase
{
    // copied from JqlStringSupportImpl.java. We have removed the strings that are valid in JQL.
    private static final String[] reservedWords = new String[] {
            "abort", "access", "add", "alias", "all", "alter", "any", "as",
            "audit", "avg", "begin", "between", "boolean", "break", "byte", "catch",
            "char", "character", "check", "checkpoint", "collate", "collation", "column", "commit", "connect", "continue",
            "count", "create", "current", "date", "decimal", "declare", "decrement", "default", "defaults", "define", "delete",
            "delimiter", "difference", "distinct", "divide", "do", "double", "drop", "else", "encoding",
            "end", "equals", "escape", "exclusive", "exec", "execute", "exists", "explain", "false", "fetch", "file", "field",
            "first", "float", "for", "function", "go", "goto", "grant", "greater", "group", "having",
            "identified", "if", "immediate", "increment", "index", "initial", "inner", "inout", "input", "insert",
            "int", "integer", "intersect", "intersection", "into", "isempty", "isnull", "join", "last", "left",
            "less", "like", "limit", "lock", "long", "max", "min", "minus", "mode", "modify",
            "modulo", "more", "multiply", "next", "noaudit", "notin", "nowait", "number", "object",
            "of", "option", "outer", "output", "power", "previous", "prior", "privileges",
            "public", "raise", "raw", "remainder", "rename", "resource", "return", "returns", "revoke", "right", "row",
            "rowid", "rownum", "rows", "select", "session", "set", "share", "size", "sqrt", "start", "strict",
            "string", "subtract", "sum", "synonym", "table", "then", "trans", "transaction", "trigger", "true",
            "uid", "union", "unique", "update", "user", "validate", "values", "view", "when", "whenever", "where",
            "while", "with"
    };

    public void testJqlParserErrors() throws Exception
    {
        backdoor.restoreBlankInstance();

        //Some unfinished string error messages.
        assertErrorMessage("comment ~ \'test", "The quoted string 'test' has not been completed. (line 1, character 11)");
        assertErrorMessage("comment ~ \'test\n", "The quoted string 'test ' has not been completed. (line 1, character 11)");
        assertErrorMessage("priority=\"test\nword", "Error in the JQL Query: The quoted string 'test word' has not been completed. (line 1, character 10)");
        assertErrorMessage("comment ~ \'", "Error in the JQL Query: The quoted string has not been completed. (line 1, character 11)");

        //Some illegal/unfinished escapes.
        String validSequence = "The valid escape sequences are \\', \\\", \\t, \\n, \\r, \\\\, '\\ ' and \\uXXXX.";
        assertErrorMessage("comme\\unt ~ \'test", "'\\un' is an illegal JQL escape sequence. " + validSequence + " (line 1, character 6)");
//        webdriver couldn't enter newline into the text area
//        assertErrorMessage("status =\n\n\n\n\n\n \\c", "'\\c' is an illegal JQL escape sequence. " + validSequence + " (line 7, character 2)");
//        assertErrorMessage("status =\n\n\n\n \\csss", "'\\c' is an illegal JQL escape sequence. " + validSequence + " (line 5, character 2)");
//        assertErrorMessage("status =\n\n\n\n\\", "The escape sequence has not been completed. " + validSequence + " (line 5, character 1)");
//        assertErrorMessage("status =\n\n\n\n\\u", "'\\u' is an illegal JQL escape sequence. " + validSequence + " (line 5, character 1)");
//        assertErrorMessage("status =\n\n\n\n'\\u'", "'\\u'' is an illegal JQL escape sequence. " + validSequence + " (line 5, character 2)");

        //Some reserved-characters
        assertErrorMessage("mine{www = qwwr", "The character '{' is a reserved JQL character. You must enclose it in a string or use the escape '\\u007b' instead. (line 1, character 5)");
        assertErrorMessage("minewww# = qwwr", "The character '#' is a reserved JQL character. You must enclose it in a string or use the escape '\\u0023' instead. (line 1, character 8)");
        assertErrorMessage("minewww = qwwr}", "The character '}' is a reserved JQL character. You must enclose it in a string or use the escape '\\u007d' instead. (line 1, character 15)");

        //Some illegal-characters - Some kind of encoding issue on bamboo causes some problems with these.
        assertErrorMessage("minewww =\u007fqwwr", "The character 'U+007F' on line 1 at position 10 must be escaped. Use the escape '\\u007f' instead. (line 1, character 10)");
        assertErrorMessage("mqwwr\u007f", "The character 'U+007F' on line 1 at position 6 must be escaped. Use the escape '\\u007f' instead. (line 1, character 6)");
        assertErrorMessage("mqwwr=\"\u007f this is broken\"", "Error in the JQL Query: The character 'U+007F' on line 1 at position 8 must be escaped. Use the escape '\\u007f' instead. (line 1, character 8)");

        //Test some illegal numbers.
        assertErrorMessage("aaaaa=-3482094382084092840932809482", "'-3482094382084092840932809482' is not a valid number. Numbers must be between -9,223,372,036,854,775,808 and 9,223,372,036,854,775,807. (line 1, character 7)");
        assertErrorMessage("cf[1002020202002020202020202020020202822]=a", "'1002020202002020202020202020020202822' is not a valid number. Numbers must be between -9,223,372,036,854,775,808 and 9,223,372,036,854,775,807. (line 1, character 4)");

        //Some sorting error messages.
        assertErrorMessage("a =b order", "Expecting 'by' before the end of the query.");
        assertErrorMessage("a =b order summary", "Expecting 'by' but got 'summary'. (line 1, character 12)");
        assertErrorMessage("a =b order by", "Expecting a field name at the end of the query.");
        assertErrorMessage("a =b order by asc", "Expecting a field name but got 'asc'. You must surround 'asc' in quotation marks to use it as a field name. (line 1, character 15)");
        assertErrorMessage("a =b order by ajsks blah", "Expecting either 'ASC' or 'DESC' but got 'blah'. (line 1, character 21)");

        //Check for empty field and function names.
        assertErrorMessage("a =b order by \"\" desc", "A field name cannot be empty. (line 1, character 15)");
        assertErrorMessage("'' = c", "A field name cannot be empty. (line 1, character 1)");
        //webdriver couldn't enter newline into the textarea
//        assertErrorMessage("summary ~ bad and \n\\ ~ cool", "A field name cannot be empty. (line 2, character 1)");
        assertErrorMessage("a = ''()", "A function name cannot be empty. (line 1, character 5)");
        assertErrorMessage("a = \"\"()", "A function name cannot be empty. (line 1, character 5)");
        //webdriver couldn't enter newline into the textarea
//        assertErrorMessage("a = \\ \n()", "A function name cannot be empty. (line 1, character 5)");

        //Check for some operator errors.
        assertErrorMessage("a", "Expecting operator before the end of the query. The valid operators are '=', '!=', '<', '>', '<=', '>=', '~', '!~', 'IN', 'NOT IN', 'IS' and 'IS NOT'.");
        assertErrorMessage("a blah >", "Expecting operator but got 'blah'. The valid operators are '=', '!=', '<', '>', '<=', '>=', '~', '!~', 'IN', 'NOT IN', 'IS' and 'IS NOT'. (line 1, character 3)");
        assertErrorMessage("a blah >", "Expecting operator but got 'blah'. The valid operators are '=', '!=', '<', '>', '<=', '>=', '~', '!~', 'IN', 'NOT IN', 'IS' and 'IS NOT'. (line 1, character 3)");
        assertErrorMessage("a =a and j not is empty", "Expecting 'IN' but got 'is'. (line 1, character 16)");
        assertErrorMessage("a =a and j is ! empty", "Expecting 'NOT' but got '!'. (line 1, character 15)");

        //Check out some CF id errors.
        assertErrorMessage("cf", "Expecting '[' before the end of the query.");
        assertErrorMessage("a = b and not (cf = 6)", "Expecting '[' but got '='. (line 1, character 19)");
        assertErrorMessage("cf[", "Expecting a custom field id (a positive number) before the end of the query.");
        assertErrorMessage("cf[ = 9", "Expecting a custom field id (a positive number) but got '='. (line 1, character 5)");
        assertErrorMessage("cf[] = 1", "Expecting a custom field id (a positive number) but got ']'. (line 1, character 4)");
        assertErrorMessage("cf[29829k] = ", "Expecting a custom field id (a positive number) but got '29829k'. (line 1, character 4)");
        assertErrorMessage("cf[-29829] = ", "Expecting a custom field id (a positive number) but got '-29829'. (line 1, character 4)");
        assertErrorMessage("cf[29829 = world", "Expecting ']' but got '='. (line 1, character 10)");
        assertErrorMessage("cf[29829", "Expecting ']' before the end of the query.");

        //Check out some function errors.
        assertErrorMessage("a = func(", "Expecting ')' before the end of the query.");
        assertErrorMessage("a = func(, and not (a = x)", "Expecting ')' but got ','. (line 1, character 10)");
        assertErrorMessage("b=func(bad", "Expecting ')' before the end of the query.");
        assertErrorMessage("bas=func(bad,)", "Function argument must be specified. (line 1, character 14)");
        assertErrorMessage("bas=func(bad,", "Expecting a function argument at the end of the query.");
        assertErrorMessage("k = func(!", "Expecting a function argument but got '!'. You must surround '!' in quotation marks to use it as an argument. (line 1, character 10)");
        assertErrorMessage("k = func(abc, jk!)", "Expecting ')' or ',' but got '!'. (line 1, character 17)");

        //Check some unmatched brackets.
        assertErrorMessage("(a=b and c = d", "Expecting ')' before the end of the query.");
        assertErrorMessage("a=b or (c=d and not l ~ kate", "Expecting ')' before the end of the query.");
        assertErrorMessage("b in ((12, (288)", "Expecting ')' before the end of the query.");
        assertErrorMessage("abc IN ((foo) abc = a", "Expecting ')' but got 'abc'. (line 1, character 15)");

        //Check some bad operands.
        assertErrorMessage("a=", "Expecting either a value, list or function before the end of the query.");
        assertErrorMessage("a=!", "Expecting either a value, list or function but got '!'. You must surround '!' in quotation marks to use it as a value. (line 1, character 3)");
        assertErrorMessage("a=b c=d", "Expecting either 'OR' or 'AND' but got 'c'. (line 1, character 5)");
        assertErrorMessage("a=b !c=d", "Expecting either 'OR' or 'AND' but got '!'. (line 1, character 5)");
        assertErrorMessage("a=(a,", "Expecting either a value, list or function before the end of the query.");
        assertErrorMessage("a=(a,", "Expecting either a value, list or function before the end of the query.");
        assertErrorMessage("a=(a,(,bad))", "Expecting either a value, list or function but got ','. You must surround ',' in quotation marks to use it as a value. (line 1, character 7)");
        assertErrorMessage("a = ((c,d), d, e, f, yyy) and", "Expecting a field name at the end of the query.");

        assertErrorMessage("comment !~ \"brenden\" and", "Expecting a field name at the end of the query.");
        assertErrorMessage("comment !~ \"brenden\" jack = jill", "Expecting either 'OR' or 'AND' but got 'jack'. (line 1, character 22)");
        assertErrorMessage("comment !~ \"brenden\" and cf[1000] = jill order by a desc b", "Error in the JQL Query: Expecting ',' but got 'b'. (line 1, character 58)");
        assertErrorMessage(", a = b", "Expecting a field name but got ','. You must surround ',' in quotation marks to use it as a field name. (line 1, character 1)");
        assertErrorMessage("order by a desc d", "Error in the JQL Query: Expecting ',' but got 'd'. (line 1, character 17)");
        assertErrorMessage("[brenden] = a", "Expecting a field name but got '['. Did you mean 'cf['? (line 1, character 1)");
        assertErrorMessage("a = brenden and [78] = q", "Expecting a field name but got '['. Did you mean 'cf['? (line 1, character 17)");

        //check some invalid change history searches
        //missing operand
        assertErrorMessage("a was", "Expecting either a value, list or function before the end of the query.");
        // operator issues
        assertErrorMessage("a not was \"Open\"", "Expecting 'IN' but got 'was'.");

        //JRADEV-6396 check that predicates cannot be used with normal searches
        assertErrorMessage("status = \"Open\" by admin", "The EQUALS operator does not support the use of the by \"admin\" predicate.");
        //JRADEV-7239 check that changed does not accept operands without predicates
        assertErrorMessage("fixVersion changed 4 by \"I can type complete rubbish here\"","The CHANGED operator does not support the use of values without predicates. You must prefix the 4 value with a suitable predicate.");
        //JRADEV-7244 check validation for from and to and all other fields too
        assertErrorMessage("fixVersion changed from 4 to \"I can type complete rubbish here\"","The value 'I can type complete rubbish here' does not exist for the field 'fixVersion'.");
        assertErrorMessage("status changed from fred","The value 'fred' does not exist for the field 'status'.");
        assertErrorMessage("status changed after \"20/12/10\"","Date value '20/12/10' for predicate 'after' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.");
        assertErrorMessage("status changed after currentUser()","A date for the predicate 'after' provided by the function 'currentUser' is not valid.");
        assertErrorMessage("status changed before (11, 12)","The BEFORE predicate must be supplied with only 1 date value.");
        assertErrorMessage("status changed during 11","The DURING predicate must be supplied with exactly 2 date values.");

        //JRA-27446 check that FunctionOperands in a ChangedClause or WasClause are being validated.
        assertErrorMessage("assignee changed by bogus()", "Unable to find JQL function 'bogus()'.");
        assertErrorMessage("assignee was bogus()", "Unable to find JQL function 'bogus()'.");
        assertErrorMessage("status was Reopened by currentUser() AFTER startOfDay(\"invalid\")", "Duration for function 'startOfDay' should have the format (+/-)n(yMwdm), e.g -1M for 1 month earlier.");
        assertErrorMessage("status was Reopened by currentUser() AFTER now(\"invalid\")", "Function 'now' expected '0' arguments but received '1'.");
        assertErrorMessage("status changed AFTER startOfDay(\"invalid\")", "Duration for function 'startOfDay' should have the format (+/-)n(yMwdm), e.g -1M for 1 month earlier.");
        assertErrorMessage("status changed AFTER now(\"invalid\")", "Function 'now' expected '0' arguments but received '1'.");

        //Make sure that reserved words are reported as such.
        for (final String reservedWord : reservedWords)
        {
            final String jqlQuery = String.format("summary ~ %s", reservedWord);
            final String errorMsg = String.format("Error in the JQL Query: '%s' is a reserved JQL word. You must surround it in quotation marks to use it in a query. (line 1, character 11)", reservedWord);
            assertErrorMessage(jqlQuery, errorMsg);
        }
    }

    private void assertErrorMessage(final String jql, final String message)
    {
        try {
            final SearchResult searchResult = backdoor.search().getSearch(new SearchRequest().jql(jql));
            throw new AssertionError("Expected to get UniformInterfaceException exception! "
                    + "Server should return 400 return code. Got: " + searchResult);
        }
        catch (UniformInterfaceException e) {
            final ErrorResponse response = e.getResponse().getEntity(ErrorResponse.class);
            final Matcher<Iterable<? super String>> containsErrorWithMessage = Matchers.hasItem(Matchers.containsString(message));
            assertThat(response.errorMessages, containsErrorWithMessage);
        }
    }

    public static class ErrorResponse {
        public Collection<String> errorMessages;

        @Override
        public String toString()
        {
            return "ErrorResponse{errorMessages=" + errorMessages + '}';
        }
    }
}
