package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Testing for "fitness" in the filter form of singular custom field clauses.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestCustomFieldDoesItFitSingle extends AbstractJqlFuncTest
{
    private static final ThreadLocal<AtomicBoolean> dataSetUp = new ThreadLocal<AtomicBoolean>() {
        @Override
        protected AtomicBoolean initialValue()
        {
            return new AtomicBoolean(false);
        }
    };

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        if (!dataSetUp.get().getAndSet(true))
        {
            administration.restoreData("TestCustomFieldDoesItFitSingle.xml");
        }
    }

    // todo enable when JRA-30298 is fixed
//    public void testCascadingSelect() throws Exception
//    {
//        final String customFieldId = "10000";
//        final String fieldName = "CSF";
//
//        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
//        matrix.put("= parent", new Result(createFilterFormParam("customfield_10000", "10000"), createFilterFormParam("customfield_10000:1", "")));
//        matrix.put("= 10000", new Result(createFilterFormParam("customfield_10000", "10000"), createFilterFormParam("customfield_10000:1", "")));
//        matrix.put("= child2", new Result(createFilterFormParam("customfield_10000", "10000"), createFilterFormParam("customfield_10000:1", "10010")));
//        matrix.put("= 10010", new Result(createFilterFormParam("customfield_10000", "10000"), createFilterFormParam("customfield_10000:1", "10010")));
//
//        // "child" is ambiguous so it will not fit, but its id will
//        matrix.put("= child", Result.tooComplex());
//        matrix.put("= 10001", new Result(createFilterFormParam("customfield_10000", "10000"), createFilterFormParam("customfield_10000:1", "10001")));
//
//        matrix.put("!= parent", Result.tooComplex());
//        addComplexEmpty(matrix);
//        matrix.put("in (parent, child2)", Result.tooComplex());
//        matrix.put("not in (parent, child2)", Result.tooComplex());
//
//        // cascadeOption() only fits if the names are not ambiguous
//        // if they are ambiguous, the ids can be used instead
//        // "none" as the 2nd argument never fits
//        final Map<String, Result> functionMatrix = new LinkedHashMap<String, Result>();
//        functionMatrix.put("in cascadeOption(parent)", new Result(false));
//        functionMatrix.put("in cascadeOption(10000)", new Result(createFilterFormParam("customfield_10000", "10000"), createFilterFormParam("customfield_10000:1", "")));
//        functionMatrix.put("in cascadeOption(parent, child2)", new Result(false));
//        functionMatrix.put("in cascadeOption(10000, child2)", new Result(createFilterFormParam("customfield_10000", "10000"), createFilterFormParam("customfield_10000:1", "10010")));
//        functionMatrix.put("in cascadeOption(parent, 10010)", new Result(createFilterFormParam("customfield_10000", "10000"), createFilterFormParam("customfield_10000:1", "10010")));
//        functionMatrix.put("in cascadeOption(parent, child)", new Result(false));
//        functionMatrix.put("in cascadeOption(parent, 10001)", new Result(createFilterFormParam("customfield_10000", "10000"), createFilterFormParam("customfield_10000:1", "10001")));
//        functionMatrix.put("in cascadeOption(parent, none)", new Result(false));
//        functionMatrix.put("in cascadeOption(10000, none)", new Result(false));
//
//        _testFitnessMatrix(fieldName, matrix);
//        _testFitnessMatrix(fieldName, functionMatrix);
//
//        // note: dont bother testing for the functions - we will cover this in the function's own test
//        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);
//
//        // remove global context for custom field
//        administration.customFields().removeGlobalContext(customFieldId);
//
//        matrix = new LinkedHashMap<String, Result>();
//        matrix.put("= parent", Result.tooComplex());
//        matrix.put("= 10030", Result.tooComplex());
//        matrix.put("= child2", Result.tooComplex());
//        matrix.put("= 10040", Result.tooComplex());
//        matrix.put("in cascadeOption(parent)", Result.tooComplex());
//        matrix.put("in cascadeOption(10030)", Result.tooComplex());
//        matrix.put("in cascadeOption(10030, child2)", Result.tooComplex());
//        matrix.put("in cascadeOption(parent, 10039)", Result.tooComplex());
//        matrix.put("in cascadeOption(parent, 10040)", Result.tooComplex());
//
//        _testFitnessMatrix(fieldName, matrix);
//    }

    public void testDatePicker() throws Exception
    {
        final String customFieldId = "10001";
        final String fieldName = "DP";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= '2009-05-11'", Result.tooComplex());
        matrix.put("!= '2009-05-11'", Result.tooComplex());
        matrix.put("<= '2009-05-11'", new Result(createFilterFormParam("customfield_10001:before", "11/May/09")));
        matrix.put(">= '2009-05-11'", new Result(createFilterFormParam("customfield_10001:after", "11/May/09")));
        matrix.put("< '2009-05-11'", Result.tooComplex());
        matrix.put("> '2009-05-11'", Result.tooComplex());
        matrix.put("<= '1d'", new Result(createFilterFormParam("customfield_10001:next", "1d")));
        matrix.put(">= '1d'", new Result(createFilterFormParam("customfield_10001:previous", "1d")));
        matrix.put("< '1d'", Result.tooComplex());
        matrix.put("> '1d'", Result.tooComplex());
        matrix.put("in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put("not in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testDateTime() throws Exception
    {
        final String customFieldId = "10002";
        final String fieldName = "DT";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= '2009-05-11 12:00'", Result.tooComplex());
        matrix.put("!= '2009-05-11 12:00'", Result.tooComplex());
        matrix.put("<= '2009-05-11 12:00'", new Result(createFilterFormParam("customfield_10002:before", "11/May/09 12:00 PM")));
        matrix.put("<= '2009-05-11'", new Result(createFilterFormParam("customfield_10002:before", "11/May/09 12:00 AM")));
        matrix.put(">= '2009-05-11 12:00'", new Result(createFilterFormParam("customfield_10002:after", "11/May/09 12:00 PM")));
        matrix.put(">= '2009-05-11'", new Result(createFilterFormParam("customfield_10002:after", "11/May/09 12:00 AM")));
        matrix.put("< '2009-05-11 12:00'", Result.tooComplex());
        matrix.put("> '2009-05-11 12:00'", Result.tooComplex());
        matrix.put("<= '1d'", new Result(createFilterFormParam("customfield_10002:next", "1d")));
        matrix.put(">= '1d'", new Result(createFilterFormParam("customfield_10002:previous", "1d")));
        matrix.put("< '1d'", Result.tooComplex());
        matrix.put("> '1d'", Result.tooComplex());
        matrix.put("in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put("not in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testFreeTextField() throws Exception
    {
        final String customFieldId = "10003";
        final String fieldName = "FTF";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("~ something", new Result(createFilterFormParam("customfield_" + customFieldId, "something")));
        matrix.put("!~ something", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testGroupPicker() throws Exception
    {
        final String customFieldId = "10004";
        final String fieldName = "GP";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 'jira-users'", new Result(createFilterFormParam("customfield_" + customFieldId, "jira-users")));
        matrix.put("!= 'jira-users'", Result.tooComplex());
        matrix.put("in ('jira-users', 'jira-administrators')", new Result(false));
        matrix.put("not in ('jira-users', 'jira-administrators')", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testImportId() throws Exception
    {
        final String customFieldId = "10005";
        final String fieldName = "II";

        // check exact number searching
        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 2", new Result(createFilterFormParam("customfield_" + customFieldId, "2")));
        matrix.put("!= 2", Result.tooComplex());
        matrix.put("in (2, 4)", Result.tooComplex());
        matrix.put("not in (2, 4)", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("< 2", Result.tooComplex());
        matrix.put("<= 2", Result.tooComplex());
        matrix.put("> 2", Result.tooComplex());
        matrix.put(">= 2", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);

        // switch to range searching
        administration.customFields().setCustomFieldSearcher(customFieldId, "com.atlassian.jira.plugin.system.customfieldtypes:numberrange");

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 2", Result.tooComplex());
        matrix.put("!= 2", Result.tooComplex());
        matrix.put("in (2, 4)", Result.tooComplex());
        matrix.put("not in (2, 4)", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("< 2", Result.tooComplex());
        matrix.put("<= 2", new Result(createFilterFormParam("customfield_10005:lessThan", "2")));
        matrix.put("> 2", Result.tooComplex());
        matrix.put(">= 2", new Result(createFilterFormParam("customfield_10005:greaterThan", "2")));

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testMultiCheckbox() throws Exception
    {
        final String customFieldId = "10006";
        final String fieldName = "MC";

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= opt1", new Result(createFilterFormParam("customfield_" + customFieldId, "10002")));
        matrix.put("!= opt1", Result.tooComplex());
        matrix.put("in (opt1, opt2)", new Result(createFilterFormParam("customfield_" + customFieldId, "10002", "10003")));
        matrix.put("not in (opt1, opt2)", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        // remove global context for custom field
        administration.customFields().removeGlobalContext(customFieldId);

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("= opt3", Result.invalid());
        matrix.put("in (opt3, opt4)", Result.invalid());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testMultiGroupPicker() throws Exception
    {
        final String customFieldId = "10007";
        final String fieldName = "MGP";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 'jira-users'", new Result(createFilterFormParam("customfield_" + customFieldId, "jira-users")));
        matrix.put("!= 'jira-users'", Result.tooComplex());
        matrix.put("in ('jira-users', 'jira-administrators')", new Result(false));
        matrix.put("not in ('jira-users', 'jira-administrators')", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testMultiSelect() throws Exception
    {
        final String customFieldId = "10008";
        final String fieldName = "MS";

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= select1", new Result(createFilterFormParam("customfield_" + customFieldId, "10004")));
        matrix.put("!= select1", Result.tooComplex());
        matrix.put("in (select1, select2)", new Result(createFilterFormParam("customfield_" + customFieldId, "10004", "10005")));
        matrix.put("not in (select1, select2)", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        // remove global context for custom field
        administration.customFields().removeGlobalContext(customFieldId);

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("= select3", Result.invalid());
        matrix.put("in (select3, select4)", Result.invalid());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testMultiUserPicker() throws Exception
    {
        final String customFieldId = "10009";
        final String fieldName = "MUP";

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= fred", new Result(createFilterFormParam("customfield_10009", FRED_USERNAME), createFilterFormParam("customfield_10009Select", "specificuser")));
        matrix.put("!= fred", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("in (fred, admin)", new Result(false));
        matrix.put("not in (fred, admin)", Result.tooComplex());
        matrix.put("in membersOf('jira-users')", new Result(createFilterFormParam("customfield_10009", "jira-users"), createFilterFormParam("customfield_10009Select", "specificgroup")));
        matrix.put("= currentUser()", new Result(createFilterFormParam("customfield_10009Select", "issue_current_user")));

        _testFitnessMatrix(fieldName, matrix);

        String oldSearcher = _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);
        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:userpickergroupsearcher", oldSearcher);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testNumberField() throws Exception
    {
        final String customFieldId = "10010";
        final String fieldName = "NF";

        // check exact number searching
        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10", new Result(createFilterFormParam("customfield_" + customFieldId, "10")));
        matrix.put("!= 10", Result.tooComplex());
        matrix.put("in (10, 20)", Result.tooComplex());
        matrix.put("not in (10, 20)", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("< 10", Result.tooComplex());
        matrix.put("<= 10", Result.tooComplex());
        matrix.put("> 10", Result.tooComplex());
        matrix.put(">= 10", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);

        // switch to range searching
        administration.customFields().setCustomFieldSearcher(customFieldId, "com.atlassian.jira.plugin.system.customfieldtypes:numberrange");

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10", Result.tooComplex());
        matrix.put("!= 10", Result.tooComplex());
        matrix.put("in (10, 20)", Result.tooComplex());
        matrix.put("not in (10, 20)", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("< 10", Result.tooComplex());
        matrix.put("<= 10", new Result(createFilterFormParam("customfield_10010:lessThan", "10")));
        matrix.put("> 10", Result.tooComplex());
        matrix.put(">= 10", new Result(createFilterFormParam("customfield_10010:greaterThan", "10")));

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testProjectPicker() throws Exception
    {
        final String customFieldId = "10011";
        final String fieldName = "PP";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= three", new Result(createFilterFormParam("customfield_" + customFieldId, "10012")));
        matrix.put("!= three", Result.tooComplex());
        matrix.put("in (three, four)", Result.tooComplex());
        matrix.put("not in (three, four)", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testRadioButton() throws Exception
    {
        final String customFieldId = "10012";
        final String fieldName = "RB";

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= rad1", new Result(createFilterFormParam("customfield_" + customFieldId, "10006")));
        matrix.put("!= rad1", Result.tooComplex());
        matrix.put("in (rad1, rad2)", new Result(createFilterFormParam("customfield_" + customFieldId, "10006", "10007")));
        matrix.put("not in (rad1, rad2)", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        // remove global context for custom field
        administration.customFields().removeGlobalContext(customFieldId);

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("= rad3", Result.invalid());
        matrix.put("in (rad3, rad4)", Result.invalid());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testReadOnlyTextField() throws Exception
    {
        final String customFieldId = "10013";
        final String fieldName = "ROTF";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("~ something", new Result(createFilterFormParam("customfield_" + customFieldId, "something")));
        matrix.put("!~ something", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testSelectList() throws Exception
    {
        final String customFieldId = "10014";
        final String fieldName = "SL";

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= select1", new Result(createFilterFormParam("customfield_" + customFieldId, "10008")));
        matrix.put("!= select1", Result.tooComplex());
        matrix.put("in (select1, select2)", new Result(createFilterFormParam("customfield_" + customFieldId, "10008", "10009")));
        matrix.put("not in (select1, select2)", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        // remove global context for custom field
        administration.customFields().removeGlobalContext(customFieldId);

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("= select3", Result.invalid());
        matrix.put("in (select3, select4)", Result.invalid());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);
    }

    //This should have been converted to a multi in 5.2
    public void testSingleVersionPicker() throws Exception
    {
        final String customFieldId = "10015";
        final String fieldName = "SVP";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= v1", new Result(createFilterFormParam("customfield_" + customFieldId, "v1")));
        matrix.put("!= v1", Result.tooComplex());
        addComplexRelational(matrix, "v1");
        addComplexEmpty(matrix);
        matrix.put("in (v1, v2)", new Result(createFilterFormParam("customfield_" + customFieldId, "v1"), createFilterFormParam("customfield_" + customFieldId, "v2")));
        matrix.put("not in (v1, v2)", Result.tooComplex());
        matrix.put("in releasedVersions(SIXTEEN)", Result.tooComplex());
        matrix.put("not in releasedVersions(SIXTEEN)", Result.tooComplex());
        matrix.put("in unreleasedVersions(SIXTEEN)", Result.tooComplex());
        matrix.put("not in unreleasedVersions(SIXTEEN)", Result.tooComplex());
        // JRA-20046 - archived versions should be too complex to fit in navigator
        matrix.put("= 10020 and project = HSP", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);

        // dont bother testing the others since none of these fit anyway
    }

    public void testSingleVersionPickerSearcherFitsIfMoreThanOneProjectIsInContext()
    {
        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= v1", new Result(createFilterFormParam("customfield_10015", "v1")));

        _testFitnessMatrix("SVP", matrix);
    }

    public void testTextField() throws Exception
    {
        final String customFieldId = "10016";
        final String fieldName = "TF";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("~ something", new Result(createFilterFormParam("customfield_" + customFieldId, "something")));
        matrix.put("!~ something", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testUrlField() throws Exception
    {
        final String customFieldId = "10017";
        final String fieldName = "URL";

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 'http://www.atlassian.com'", new Result(createFilterFormParam("customfield_" + customFieldId, "http://www.atlassian.com")));
        matrix.put("!= 'http://www.atlassian.com'", Result.tooComplex());
        matrix.put("in ('http://www.atlassian.com', 'http://www.jira.com')", Result.tooComplex());
        matrix.put("not in ('http://www.atlassian.com', 'http://www.jira.com')", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);

        _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testUserPicker() throws Exception
    {
        final String customFieldId = "10018";
        final String fieldName = "UP";

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= fred", new Result(createFilterFormParam("customfield_10018", FRED_USERNAME), createFilterFormParam("customfield_10018Select", "specificuser")));
        matrix.put("!= fred", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("in (fred, admin)", new Result(false));
        matrix.put("not in (fred, admin)", Result.tooComplex());
        matrix.put("in membersOf('jira-users')", new Result(createFilterFormParam("customfield_10018", "jira-users"), createFilterFormParam("customfield_10018Select", "specificgroup")));
        matrix.put("= currentUser()", new Result(createFilterFormParam("customfield_10018Select", "issue_current_user")));

        _testFitnessMatrix(fieldName, matrix);

        String oldSearcher = _testFieldDoesntExistWhenNoSearcher(customFieldId, fieldName, matrix);
        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:userpickergroupsearcher", oldSearcher);

        _testTooComplexWhenNoGlobalContext(customFieldId, fieldName, matrix);
    }

    public void testVersionPicker() throws Exception
    {
        final String customFieldId = "10019";
        final String fieldName = "VP";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= v1", new Result(createFilterFormParam("customfield_" + customFieldId, "v1")));
        matrix.put("!= v1", Result.tooComplex());
        addComplexRelational(matrix, "v1");
        addComplexEmpty(matrix);
        matrix.put("in (v1, v2)", new Result(false));
        matrix.put("not in (v1, v2)", Result.tooComplex());
        matrix.put("in releasedVersions(SIXTEEN)", Result.tooComplex());
        matrix.put("not in releasedVersions(SIXTEEN)", Result.tooComplex());
        matrix.put("in unreleasedVersions(SIXTEEN)", Result.tooComplex());
        matrix.put("not in unreleasedVersions(SIXTEEN)", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);

        // dont bother testing the others since none of these fit anyway
    }

    public void testVersionPickerSearcherFitsIfMoreThanOneProjectIsInContext()
    {
        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= v1", new Result(createFilterFormParam("customfield_10019", "v1")));

        _testFitnessMatrix("VP", matrix);
    }

    private void _testFitnessMatrix(final String fieldName, final Map<String, Result> matrix)
    {
        for (Map.Entry<String, Result> entry : matrix.entrySet())
        {
            final String jqlQuery = String.format("%s %s", fieldName, entry.getKey());
            Result result = entry.getValue();
            System.out.println("QUERY: " + jqlQuery);
            if (result.isTooComplex)
            {
                assertTooComplex(jqlQuery);
            }
            else if (result.isInvalid) {
                assertInvalidContext(jqlQuery);
            } else if (result.expectedFormData != null)
            {
                assertFitsFilterForm(jqlQuery, result.expectedFormData);
            }
            else
            {
                assertFitsFilterForm(jqlQuery);
            }
        }
    }

    private void _testTooComplexWhenNoGlobalContext(final String customFieldId, final String fieldName, final Map<String, Result> matrix)
    {
        // remove global context for custom field
        administration.customFields().removeGlobalContext(customFieldId);

        // all the previous tests that would have fit should now not fit
        for (Map.Entry<String, Result> entry : matrix.entrySet())
        {
            final String jqlQuery = String.format("%s %s", fieldName, entry.getKey());
            Result result = entry.getValue();
            if (!result.isTooComplex)
            {
                assertInvalidContext(jqlQuery);
            }
        }
    }

    private String _testFieldDoesntExistWhenNoSearcher(final String customFieldId, final String fieldName, final Map<String, Result> matrix)
    {
        // remove searcher id
        final String searcherId = administration.customFields().setCustomFieldSearcher(customFieldId, null);

        // all the previous tests that would have fit should now not fit
        for (Map.Entry<String, Result> entry : matrix.entrySet())
        {
            final String jqlQuery = String.format("%s %s", fieldName, entry.getKey());
            Result result = entry.getValue();
            if (!result.isTooComplex)
            {
                assertFieldDoesntExist(jqlQuery, fieldName);
            }
        }

        // replace searcher
        administration.customFields().setCustomFieldSearcher(customFieldId, searcherId);
        return searcherId;
    }

    private void assertFieldDoesntExist(final String jqlQuery, final String fieldName)
    {
        final String errorMsg = String.format("Field '%s' is not searchable, it is only sortable.", fieldName);
        assertSearchWithError(jqlQuery, errorMsg);
    }

    private void addComplexRelational(final Map<String, Result> matrix, final String lit)
    {
        matrix.put("< " + lit, Result.tooComplex());
        matrix.put("<= " + lit, Result.tooComplex());
        matrix.put("> " + lit, Result.tooComplex());
        matrix.put(">= " + lit, Result.tooComplex());
    }

    private void addComplexEmpty(final Map<String, Result> matrix)
    {
        matrix.put("is EMPTY", Result.tooComplex());
        matrix.put("is not EMPTY", Result.tooComplex());
    }

    private static class Result
    {
        private IssueNavigatorAssertions.FilterFormParam[] expectedFormData;
        private boolean isTooComplex;
        private boolean isInvalid;

        private static Result tooComplex()
        {
            final Result result = new Result();
            result.isTooComplex = true;
            result.isInvalid = false;
            return result;
        }

        private static Result invalid()
        {
            final Result result = new Result();
            result.isTooComplex = false;
            result.isInvalid = true;
            return result;
        }

        private Result()
        {
            this.expectedFormData = null;
            this.isTooComplex = true;
        }

        private Result(boolean tooComplex)
        {
            this.expectedFormData = null;
            this.isTooComplex = false;
        }

        private Result(final IssueNavigatorAssertions.FilterFormParam... expectedFormData)
        {
            this.expectedFormData = expectedFormData;
            this.isTooComplex = false;
        }
    }
}
