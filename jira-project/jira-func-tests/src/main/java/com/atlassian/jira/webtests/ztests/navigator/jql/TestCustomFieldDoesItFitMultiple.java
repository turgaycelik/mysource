package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestCustomFieldDoesItFitMultiple extends AbstractJqlFuncTest
{
    private static final ThreadLocal<AtomicBoolean> dataSetUp = new ThreadLocal<AtomicBoolean>() {
        @Override
        protected AtomicBoolean initialValue()
        {
            return new AtomicBoolean(false);
        }
    };
    private static final String TYPE_BUG = "1";
    private static final String TYPE_TASK = "3";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        if (!dataSetUp.get().getAndSet(true))
        {
            administration.restoreData("TestCustomFieldDoesItFitMultiple.xml");
        }
    }

    public void testCascadingSelect() throws Exception
    {
        final String customFieldId = "10000";

        final IssueNavigatorAssertions.FilterFormParam diffParent1 = createFilterFormParam("customfield_10000", "10097");
        final IssueNavigatorAssertions.FilterFormParam diffParent2 = createFilterFormParam("customfield_10000", "10101");

        final IssueNavigatorAssertions.FilterFormParam diffChild2 = createFilterFormParam("customfield_10000:1", "10099");
        final IssueNavigatorAssertions.FilterFormParam diffChild4 = createFilterFormParam("customfield_10000:1", "10105");

        // two contexts
        // For issuetype bug:
        // commonparent -> {commonchild, diffchild1}
        // diffparent1 -> {commonchild2, diffchild2}
        // For project twentyone:
        // commonparent -> {commonchild, diffchild3}
        // diffparent2 -> {commonchild2, diffchild4}

        assertFitsFilterForm("project = twentyone and CSF in cascadeOption(10101)", diffParent2);
        assertFitsFilterForm("project = twentyone and CSF in cascadeOption(10101, 10105)", diffParent2, diffChild4);
        assertFitsFilterForm("issuetype = bug and CSF in cascadeOption(10097)", diffParent1);
        assertFitsFilterForm("issuetype = bug and CSF in cascadeOption(10097, 10099)", diffParent1, diffChild2);

        assertFitsFilterForm("project = twentyone and issuetype = bug and CSF in cascadeOption(10101)", diffParent2);
        assertFitsFilterForm("project = twentyone and issuetype = bug and CSF in cascadeOption(10101, 10105)", diffParent2, diffChild4);

        assertFitsFilterForm("issuetype = bug and CSF in cascadeOption(10101)");
        assertFitsFilterForm("issuetype = bug and CSF in cascadeOption(10101, 10104)");
        assertFitsFilterForm("issuetype = bug and CSF in cascadeOption(10101, 10105)");

        assertFitsFilterForm("project = twentyone and CSF in cascadeOption(10097)");
        assertFitsFilterForm("project = twentyone and CSF in cascadeOption(10097, 10098)");
        assertFitsFilterForm("project = twentyone and CSF in cascadeOption(10097, 10099)");

        final IssueNavigatorAssertions.FilterFormParam formParam = createFilterFormParam("customfield_10000", "10100");

        String proj1 = "10031";
        String proj2 = "10020";
        String fieldConfigSchemeId = "10061";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("CSF = 10100", proj1, proj2, TYPE_BUG, TYPE_TASK);
        administration.customFields().removeConfigurationSchemeContextById(customFieldId, "10060");

        assertQueriesInContexts(contexts, queries, formParam, customFieldId, fieldConfigSchemeId);
    }

    public void testDatePicker() throws Exception
    {
        final String customFieldId = "10001";
        final String fieldConfigSchemeId = "10031";

        final IssueNavigatorAssertions.FilterFormParam afterAbs = createFilterFormParam("customfield_10001:after", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeAbs = createFilterFormParam("customfield_10001:before", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeRel = createFilterFormParam("customfield_10001:next", "1d");
        final IssueNavigatorAssertions.FilterFormParam afterRel = createFilterFormParam("customfield_10001:previous", "1d");
        final IssueNavigatorAssertions.FilterFormParam projParam = createFilterFormParam("pid", "10011");

        // all good combinations
        // note: need to add project to all these since the data has this custom field only configured with one project
        assertFitsFilterForm("project = TWO AND DP >= '2009-05-11' AND DP <= '2009-05-11'", projParam, afterAbs, beforeAbs);
        assertFitsFilterForm("project = TWO AND DP >= '2009-05-11' AND DP <= '1d'", projParam, afterAbs, beforeRel);
        assertFitsFilterForm("project = TWO AND DP >= '1d' AND DP <= '2009-05-11'", projParam, afterRel, beforeAbs);
        assertFitsFilterForm("project = TWO AND DP >= '1d' AND DP <= '1d'", projParam, afterRel, beforeRel);
        assertFitsFilterForm("project = TWO AND DP >= '2009-05-11' AND DP <= '2009-05-11' AND DP >= '1d'", projParam, afterAbs, beforeAbs, afterRel);
        assertFitsFilterForm("project = TWO AND DP >= '2009-05-11' AND DP <= '2009-05-11' AND DP <= '1d'", projParam, afterAbs, beforeAbs, beforeRel);
        assertFitsFilterForm("project = TWO AND DP >= '1d' AND DP <= '2009-05-11' AND DP >= '2009-05-11'", projParam, afterRel, beforeAbs, afterAbs);
        assertFitsFilterForm("project = TWO AND DP >= '1d' AND DP <= '1d' AND DP <= '2009-05-11'", projParam, afterRel, beforeRel, beforeAbs);
        assertFitsFilterForm("project = TWO AND DP >= '1d' AND DP <= '1d' AND DP >= '2009-05-11' AND DP <= '2009-05-11'", projParam, afterRel, beforeRel, afterAbs, beforeAbs);

        // can't combine with or
        assertTooComplex("project = TWO AND (DP >= '1d' OR DP <= '1d')");

        // can't combine same relational operator
        assertTooComplex("project = TWO AND DP >= '1d' AND DP >= '2d'");
        assertTooComplex("project = TWO AND DP <= '1d' AND DP <= '2d'");

        // cant use < or >
        assertTooComplex("project = TWO AND DP > '1d' AND DP <= '1d' AND DP >= '2009-05-11' AND DP <= '2009-05-11'");

        // if one of the operands is wrong it wont fit
        assertTooComplex("project = TWO AND DP >= 1234567890 AND DP <= '1d' AND DP >= '2009-05-11' AND DP <= '2009-05-11'");

        // fits even if split over multiple levels
        assertFitsFilterForm("(project = TWO AND DP <= '1d') AND (status = Open AND DP <= '2009-05-11')", projParam, createFilterFormParam("status", TYPE_BUG), beforeRel, beforeAbs);

        // no OR with project
        assertTooComplex("project = TWO OR DP <= '1d'");

        final String proj1 = "10011";
        final String proj2 = "10012";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("DP <= '1d'", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, beforeRel, customFieldId, fieldConfigSchemeId);
    }

    public void testDateTime() throws Exception
    {
        final String customFieldId = "10002";
        final String fieldConfigSchemeId = "10032";

        final IssueNavigatorAssertions.FilterFormParam afterAbs = createFilterFormParam("customfield_10002:after", "11/May/09 12:00 AM");
        final IssueNavigatorAssertions.FilterFormParam beforeAbs = createFilterFormParam("customfield_10002:before", "11/May/09 12:00 AM");
        final IssueNavigatorAssertions.FilterFormParam beforeRel = createFilterFormParam("customfield_10002:next", "1d");
        final IssueNavigatorAssertions.FilterFormParam afterRel = createFilterFormParam("customfield_10002:previous", "1d");
        final IssueNavigatorAssertions.FilterFormParam projParam = createFilterFormParam("pid", "10012");

        // all good combinations
        // note: need to add project to all these since the data has this custom field only configured with one project
        assertFitsFilterForm("project = THREE AND DT >= '2009-05-11' AND DT <= '2009-05-11'", projParam, afterAbs, beforeAbs);
        assertFitsFilterForm("project = THREE AND DT >= '2009-05-11' AND DT <= '1d'", projParam, afterAbs, beforeRel);
        assertFitsFilterForm("project = THREE AND DT >= '1d' AND DT <= '2009-05-11'", projParam, afterRel, beforeAbs);
        assertFitsFilterForm("project = THREE AND DT >= '1d' AND DT <= '1d'", projParam, afterRel, beforeRel);
        assertFitsFilterForm("project = THREE AND DT >= '2009-05-11' AND DT <= '2009-05-11' AND DT >= '1d'", projParam, afterAbs, beforeAbs, afterRel);
        assertFitsFilterForm("project = THREE AND DT >= '2009-05-11' AND DT <= '2009-05-11' AND DT <= '1d'", projParam, afterAbs, beforeAbs, beforeRel);
        assertFitsFilterForm("project = THREE AND DT >= '1d' AND DT <= '2009-05-11' AND DT >= '2009-05-11'", projParam, afterRel, beforeAbs, afterAbs);
        assertFitsFilterForm("project = THREE AND DT >= '1d' AND DT <= '1d' AND DT <= '2009-05-11'", projParam, afterRel, beforeRel, beforeAbs);
        assertFitsFilterForm("project = THREE AND DT >= '1d' AND DT <= '1d' AND DT >= '2009-05-11' AND DT <= '2009-05-11'", projParam, afterRel, beforeRel, afterAbs, beforeAbs);

        // can't combine with or
        assertTooComplex("project = THREE AND (DT >= '1d' OR DT <= '1d')");

        // can't combine same relational operator
        assertTooComplex("project = THREE AND DT >= '1d' AND DT >= '2d'");
        assertTooComplex("project = THREE AND DT <= '1d' AND DT <= '2d'");

        // cant use < or >
        assertTooComplex("project = THREE AND DT > '1d' AND DT <= '1d' AND DT >= '2009-05-11' AND DT <= '2009-05-11'");

        // if one of the operands is wrong it wont fit
        assertTooComplex("project = THREE AND DT >= 1234567890 AND DT <= '1d' AND DT >= '2009-05-11' AND DT <= '2009-05-11'");

        // fits even if split over multiple levels
        assertFitsFilterForm("(project = THREE AND DT <= '1d') AND (status = Open AND DT <= '2009-05-11')", projParam, createFilterFormParam("status", TYPE_BUG), beforeRel, beforeAbs);

        // no OR with project
        assertTooComplex("project = THREE OR DT <= '1d'");

        final String proj1 = "10012";
        final String proj2 = "10013";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("DT <= '1d'", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, beforeRel, customFieldId, fieldConfigSchemeId);
    }

    public void testFreeTextField() throws Exception
    {
        final String customFieldId = "10003";
        final String fieldConfigSchemeId = "10033";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10003", "qwerty");

        // can't combine with or
        assertTooComplex("project = FOUR OR FTF ~ 'qwerty'");

        // can't use the same clause more than once
        assertTooComplex("project = FOUR AND FTF ~ 'qwerty' AND FTF ~ 'qwerty2'");

        final String proj1 = "10013";
        final String proj2 = "10014";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("FTF ~ 'qwerty'", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testGroupPicker() throws Exception
    {
        final String customFieldId = "10004";
        final String fieldConfigSchemeId = "10034";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10004", "jira-users");

        // can't combine with or
        assertTooComplex("project = FIVE OR GP = 'jira-users'");

        // can't use the same clause more than once
        assertTooComplex("project = FIVE AND GP = 'jira-users' AND GP = 'jira-users'");

        final String proj1 = "10014";
        final String proj2 = "10015";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("GP = 'jira-users'", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testImportId() throws Exception
    {
        final String customFieldId = "10005";
        final String fieldConfigSchemeId = "10035";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10005", "123456");

        // can't combine with or
        assertTooComplex("project = SIX OR II = 123456");

        // can't use the same clause more than once
        assertTooComplex("project = SIX AND II = 123456 AND II = 123456");

        final String proj1 = "10015";
        final String proj2 = "10016";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("II = 123456", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testMultiCheckbox() throws Exception
    {
        final String customFieldId = "10006";
        final String fieldConfigSchemeId = "10036";
        final String proj1 = "10016";
        final String proj2 = "10017";

        final IssueNavigatorAssertions.FilterFormParam cfParam3a = createFilterFormParam("customfield_10006", "10031");
        final IssueNavigatorAssertions.FilterFormParam cfParam3b = createFilterFormParam("customfield_10006", "10110");
        final IssueNavigatorAssertions.FilterFormParam cfParam4 = createFilterFormParam("customfield_10006", "10032");
        final IssueNavigatorAssertions.FilterFormParam cfParam5 = createFilterFormParam("customfield_10006", "10111");
        final IssueNavigatorAssertions.FilterFormParam cfParamCombined1 = createFilterFormParam("customfield_10006", "10031", "10032");
        final IssueNavigatorAssertions.FilterFormParam cfParamCombined2 = createFilterFormParam("customfield_10006", "10110", "10111");
        final IssueNavigatorAssertions.FilterFormParam projParam = createFilterFormParam("pid", proj1);
        final IssueNavigatorAssertions.FilterFormParam typeParam = createFilterFormParam("type", TYPE_BUG);

        // one context for Project SEVEN one for Issue Type BUG
        // SEVEN: opt3, opt4
        // BUG: opt3, opt5
        assertFitsFilterForm("project = SEVEN AND MC = opt4", cfParam4, projParam);
        assertInvalidValue("project = SEVEN AND MC = opt5");
        assertFitsFilterForm("type = Bug AND MC = opt5", cfParam5, typeParam);
        assertInvalidValue("type = Bug AND MC = opt4");
        assertFitsFilterForm("project = SEVEN AND MC = opt3", projParam, cfParam3a);
        assertFitsFilterForm("project = SEVEN AND MC IN (opt3, opt4)", projParam, cfParamCombined1);
        assertInvalidValue("project = SEVEN AND MC IN (opt3, opt5)");
        assertFitsFilterForm("type = Bug AND MC = opt3", typeParam, cfParam3b);
        assertFitsFilterForm("type = Bug AND MC IN (opt3, opt5)", typeParam, cfParamCombined2);
        assertInvalidValue("type = Bug AND MC IN (opt3, opt4)");
        assertFitsFilterForm("project = SEVEN AND type = Bug AND MC = opt3", projParam, typeParam, cfParam3a);
        assertFitsFilterForm("project = SEVEN AND type = Bug AND MC IN (opt3, opt4)", projParam, typeParam, cfParamCombined1);
        assertInvalidValue("project = SEVEN AND type = Bug AND MC IN (opt3, opt5)");

        // can't combine with or
        assertTooComplex("project = SEVEN OR MC = opt4");

        // can't use the same clause more than once
        assertTooComplex("project = SEVEN AND MC = opt4 AND MC = opt4");
        assertTooComplex("project = SEVEN AND (MC = opt4 OR MC = opt4)");

        // remove other context before continuing
        administration.customFields().removeConfigurationSchemeContextById(customFieldId, "10070");
        
        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("MC = opt4", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam4, customFieldId, fieldConfigSchemeId);
    }

    public void testMultiGroupPicker() throws Exception
    {
        final String customFieldId = "10007";
        final String fieldConfigSchemeId = "10037";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10007", "jira-users");

        // can't combine with or
        assertTooComplex("project = EIGHT OR MGP = 'jira-users'");

        // can't use the same clause more than once
        assertTooComplex("project = EIGHT AND MGP = 'jira-users' AND MGP = 'jira-users'");

        final String proj1 = "10017";
        final String proj2 = "10018";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("MGP = 'jira-users'", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testMultiSelect() throws Exception
    {
        final String customFieldId = "10008";
        final String fieldConfigSchemeId = "10038";
        final String proj1 = "10018";
        final String proj2 = "10019";

        final IssueNavigatorAssertions.FilterFormParam projParam = createFilterFormParam("pid", proj1);
        final IssueNavigatorAssertions.FilterFormParam typeParam = createFilterFormParam("type", TYPE_BUG);
        final IssueNavigatorAssertions.FilterFormParam cfParam3a = createFilterFormParam("customfield_10008", "10033");
        final IssueNavigatorAssertions.FilterFormParam cfParam3b = createFilterFormParam("customfield_10008", "10060");
        final IssueNavigatorAssertions.FilterFormParam cfParam4 = createFilterFormParam("customfield_10008", "10034");
        final IssueNavigatorAssertions.FilterFormParam cfParam5 = createFilterFormParam("customfield_10008", "10061");
        final IssueNavigatorAssertions.FilterFormParam cfParamCombined1 = createFilterFormParam("customfield_10008", "10033", "10034");
        final IssueNavigatorAssertions.FilterFormParam cfParamCombined2 = createFilterFormParam("customfield_10008", "10060", "10061");

        // one context for Project NINE one for Issue Type BUG
        // SEVEN: select3, select4
        // BUG: select3, select5
        assertFitsFilterForm("project = NINE AND MS = select4", cfParam4, projParam);
        assertInvalidValue("project = NINE AND MS = select5");
        assertFitsFilterForm("type = Bug AND MS = select5", cfParam5, typeParam);
        assertInvalidValue("type = Bug AND MS = select4");

        assertFitsFilterForm("project = NINE AND MS = select3", projParam, cfParam3a);
        assertFitsFilterForm("project = NINE AND MS IN (select3, select4)", projParam, cfParamCombined1);
        assertInvalidValue("project = NINE AND MS IN (select3, select5)");
        assertFitsFilterForm("type = Bug AND MS = select3", typeParam, cfParam3b);
        assertFitsFilterForm("type = Bug AND MS IN (select3, select5)", typeParam, cfParamCombined2);
        assertInvalidValue("type = Bug AND MS IN (select3, select4)");
        assertFitsFilterForm("project = NINE AND type = Bug AND MS = select3", projParam, typeParam, cfParam3a);
        assertFitsFilterForm("project = NINE AND type = Bug AND MS IN (select3, select4)", projParam, typeParam, cfParamCombined1);
        assertInvalidValue("project = NINE AND type = Bug AND MS IN (select3, select5)");

        // can't combine with or
        assertTooComplex("project = NINE OR MS = select4");

        // can't use the same clause more than once
        assertTooComplex("project = NINE AND MS = select4 AND MS = select4");
        assertTooComplex("project = NINE AND (MS = select4 OR MS = select4)");

        // remove other context before continuing
        administration.customFields().removeConfigurationSchemeContextById(customFieldId, "10018");
        
        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("MS = select4", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam4, customFieldId, fieldConfigSchemeId);
    }

    public void testMultiUserPicker() throws Exception
    {
        final String customFieldId = "10009";
        final String fieldConfigSchemeId = "10039";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10009", ADMIN_USERNAME);

        // can't combine with or
        assertTooComplex("project = TEN OR MUP = admin");

        // can't use the same clause more than once
        assertTooComplex("project = TEN AND MUP = admin AND MUP = fred");

        final String proj1 = "10019";
        final String proj2 = "10020";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("MUP = admin", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testNumberField() throws Exception
    {
        final String customFieldId = "10010";
        final String fieldConfigSchemeId = "10040";
        final String proj1 = "10020";
        final String proj2 = "10021";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10010", "10");
        final IssueNavigatorAssertions.FilterFormParam cfParamMin = createFilterFormParam("customfield_10010:greaterThan", "10");
        final IssueNavigatorAssertions.FilterFormParam cfParamMax = createFilterFormParam("customfield_10010:lessThan", "20");
        final IssueNavigatorAssertions.FilterFormParam projParam = createFilterFormParam("pid", proj1);

        // can't combine with or
        assertTooComplex("project = ELEVEN OR NF = 10");

        // can't use the same clause more than once
        assertTooComplex("project = ELEVEN AND NF = 10 AND NF = 12");

        // switch to range searching
        final String oldSearcher = administration.customFields().setCustomFieldSearcher(customFieldId, "com.atlassian.jira.plugin.system.customfieldtypes:numberrange");
        
        // now multiple clauses with the proper operators will work
        assertFitsFilterForm("project = ELEVEN AND NF >= 10 AND NF <= 20", projParam, cfParamMin, cfParamMax);

        // can't combine with or
        assertTooComplex("project = ELEVEN AND (NF >= 10 OR NF <= 20)");

        // can't combine same relational operator
        assertTooComplex("project = ELEVEN AND NF >= 10 AND NF >= 20");
        assertTooComplex("project = ELEVEN AND NF <= 10 AND NF <= 20");

        // cant use < or >
        assertTooComplex("project = ELEVEN AND NF > 10 AND NF <= 20");

        // fits even if split over multiple levels
        assertFitsFilterForm("(project = ELEVEN AND NF >= 10) AND (status = Open AND NF <= 20)", projParam, createFilterFormParam("status", TYPE_BUG), cfParamMin, cfParamMax);

        // switch back
        administration.customFields().setCustomFieldSearcher(customFieldId, oldSearcher);

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("NF = 10", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testProjectPicker() throws Exception
    {
        final String customFieldId = "10011";
        final String fieldConfigSchemeId = "10041";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10011", "10012");

        // can't combine with or
        assertTooComplex("project = TWELVE OR PP = three");

        // can't use the same clause more than once
        assertTooComplex("project = TWELVE AND PP = three AND PP = four");

        final String proj1 = "10021";
        final String proj2 = "10022";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("PP = three", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testRadioButton() throws Exception
    {
        final String customFieldId = "10012";
        final String fieldConfigSchemeId = "10022";
        final String proj1 = "10022";
        final String proj2 = "10023";

        final IssueNavigatorAssertions.FilterFormParam projParam = createFilterFormParam("pid", proj1);
        final IssueNavigatorAssertions.FilterFormParam typeParam = createFilterFormParam("type", TYPE_BUG);
        final IssueNavigatorAssertions.FilterFormParam cfParam1a = createFilterFormParam("customfield_10012", "10006");
        final IssueNavigatorAssertions.FilterFormParam cfParam1b = createFilterFormParam("customfield_10012", "10080");
        final IssueNavigatorAssertions.FilterFormParam cfParam2 = createFilterFormParam("customfield_10012", "10007");
        final IssueNavigatorAssertions.FilterFormParam cfParam3 = createFilterFormParam("customfield_10012", "10035");

        // one context for Project THIRTEEN one for Issue Type BUG
        // THIRTEEN: rad1, rad2
        // BUG: rad1, rad3
        assertFitsFilterForm("project = THIRTEEN AND RB = rad2", cfParam2, projParam);
        assertInvalidValue("project = THIRTEEN AND RB = rad3");
        assertFitsFilterForm("type = Bug AND RB = rad3", cfParam3, typeParam);
        assertInvalidValue("type = Bug AND RB = rad2");

        assertFitsFilterForm("project = THIRTEEN AND RB = rad1", projParam, cfParam1a);
        assertFitsFilterForm("project = THIRTEEN AND RB IN (rad1, rad2)");
        assertInvalidValue("project = THIRTEEN AND RB IN (rad1, rad3)");
        assertFitsFilterForm("type = Bug AND RB = rad1", typeParam, cfParam1b);
        assertFitsFilterForm("type = Bug AND RB IN (rad1, rad3)");
        assertInvalidValue("type = Bug AND RB IN (rad1, rad2)");
        assertFitsFilterForm("project = THIRTEEN AND type = Bug AND RB = rad1", projParam, typeParam, cfParam1a);
        assertFitsFilterForm("project = THIRTEEN AND type = Bug AND RB IN (rad1, rad2)");
        assertInvalidValue("project = THIRTEEN AND type = Bug AND RB IN (rad1, rad3)");

        // can't combine with or
        assertTooComplex("project = THIRTEEN OR RB = rad2");

        // can't use the same clause more than once
        assertTooComplex("project = THIRTEEN AND RB = rad2 AND RB = rad2");
        assertTooComplex("project = THIRTEEN AND (RB = rad2 OR RB = rad2)");

        // remove other context before continuing
        administration.customFields().removeConfigurationSchemeContextById(customFieldId, "10042");

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("RB = rad2", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam2, customFieldId, fieldConfigSchemeId);
    }

    public void testReadOnlyTextField() throws Exception
    {
        final String customFieldId = "10013";
        final String fieldConfigSchemeId = "10043";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10013", "text");

        // can't combine with or
        assertTooComplex("project = FOURTEEN OR ROTF ~ text");

        // can't use the same clause more than once
        assertTooComplex("project = FOURTEEN AND ROTF ~ text AND ROTF ~ four");

        final String proj1 = "10023";
        final String proj2 = "10024";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("ROTF ~ text", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testSingleVersionPicker() throws Exception
    {
        final String customFieldId = "10015";
        final String fieldConfigSchemeId = "10045";
        final String proj1 = "10025";
        final String proj2 = "10029";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10015", "10010");
        final IssueNavigatorAssertions.FilterFormParam projParam = createFilterFormParam("pid", proj1);

        // can't combine with or
        assertTooComplex("project = SIXTEEN OR SVP = 10010");

        // can't use the same clause more than once
        assertTooComplex("project = SIXTEEN AND SVP = 10010 AND SVP = 10010");

        assertFitsFilterForm("project = SIXTEEN AND SVP = v1", projParam, cfParam);
        assertTooComplex("project = SIXTEEN AND SVP = 10010");
        assertTooComplex("project = SIXTEEN AND SVP != 10010");
        assertTooComplex("project = SIXTEEN AND SVP >= 10010");
        assertTooComplex("project = SIXTEEN AND SVP > 10010");
        assertTooComplex("project = SIXTEEN AND SVP <= 10010");
        assertTooComplex("project = SIXTEEN AND SVP < 10010");
        assertTooComplex("project = SIXTEEN AND SVP is EMPTY");
        assertTooComplex("project = SIXTEEN AND SVP is not EMPTY");
        assertFitsFilterForm("project = SIXTEEN AND SVP in (v1, v2)", projParam, createFilterFormParam("customfield_10015", "v1", "v2"));
        assertTooComplex("project = SIXTEEN AND SVP in (10010, 10011)");
        assertTooComplex("project = SIXTEEN AND SVP not in (10010, 10011)");
        assertTooComplex("project = SIXTEEN AND SVP in releasedVersions(SIXTEEN)");
        assertTooComplex("project = SIXTEEN AND SVP in releasedVersions()");
        assertTooComplex("project = SIXTEEN AND SVP not in releasedVersions()");
        assertTooComplex("project = SIXTEEN AND SVP in unreleasedVersions(SIXTEEN)");
        assertTooComplex("project = SIXTEEN AND SVP in unreleasedVersions()");
        assertTooComplex("project = SIXTEEN AND SVP not in unreleasedVersions()");

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueriesForVersion("SVP = v1", "10010", "10013", customFieldId, proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, null, customFieldId, fieldConfigSchemeId);
    }

    public void testTextField() throws Exception
    {
        final String customFieldId = "10016";
        final String fieldConfigSchemeId = "10046";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10016", "text");

        // can't combine with or
        assertTooComplex("project = SEVENTEEN OR TF ~ text");

        // can't use the same clause more than once
        assertTooComplex("project = SEVENTEEN AND TF ~ text AND TF ~ xxxx");

        final String proj1 = "10026";
        final String proj2 = "10027";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("TF ~ text", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testUserPicker() throws Exception
    {
        final String customFieldId = "10018";
        final String fieldConfigSchemeId = "10047";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10018", ADMIN_USERNAME);

        // can't combine with or
        assertTooComplex("project = EIGHTEEN OR UP = admin");

        // can't use the same clause more than once
        assertTooComplex("project = EIGHTEEN AND UP = admin AND UP = fred");

        final String proj1 = "10027";
        final String proj2 = "10028";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("UP = admin", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testUrlField() throws Exception
    {
        final String customFieldId = "10017";
        final String fieldConfigSchemeId = "10048";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10017", "http://www.atlassian.com");

        // can't combine with or
        assertTooComplex("project = NINETEEN OR URL = 'http://www.atlassian.com'");

        // can't use the same clause more than once
        assertTooComplex("project = NINETEEN AND URL = 'http://www.atlassian.com' AND URL = 'http://www.atlassian.com'");

        final String proj1 = "10028";
        final String proj2 = "10029";

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueries("URL = 'http://www.atlassian.com'", proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, cfParam, customFieldId, fieldConfigSchemeId);
    }

    public void testVersionPicker() throws Exception
    {
        final String customFieldId = "10019";
        final String fieldConfigSchemeId = "10049";
        final String proj1 = "10029";
        final String proj2 = "10025";

        final IssueNavigatorAssertions.FilterFormParam cfParam = createFilterFormParam("customfield_10019", "10013");
        final IssueNavigatorAssertions.FilterFormParam projParam = createFilterFormParam("pid", proj1);

        // can't combine with or
        assertTooComplex("project = TWENTY OR VP = 10013");

        // can't use the same clause more than once
        assertTooComplex("project = TWENTY AND VP = 10013 AND VP = 10013");

        assertFitsFilterForm("project = TWENTY AND VP = v1", projParam, cfParam);
        assertTooComplex("project = TWENTY AND VP = 10013");
        assertTooComplex("project = TWENTY AND VP != 10013");
        assertTooComplex("project = TWENTY AND VP >= 10013");
        assertTooComplex("project = TWENTY AND VP > 10013");
        assertTooComplex("project = TWENTY AND VP <= 10013");
        assertTooComplex("project = TWENTY AND VP < 10013");
        assertTooComplex("project = TWENTY AND VP is EMPTY");
        assertTooComplex("project = TWENTY AND VP is not EMPTY");
        assertFitsFilterForm("project = TWENTY AND VP in (v1, v2)", projParam, createFilterFormParam("customfield_10019", "10013", "10014"));
        assertTooComplex("project = TWENTY AND VP in (10013, 10014)");
        assertTooComplex("project = TWENTY AND VP not in (10013, 10014)");
        assertTooComplex("project = TWENTY AND VP in releasedVersions(TWENTY)");
        assertTooComplex("project = TWENTY AND VP in releasedVersions()");
        assertTooComplex("project = TWENTY AND VP not in releasedVersions()");
        assertTooComplex("project = TWENTY AND VP in unreleasedVersions(TWENTY)");
        assertTooComplex("project = TWENTY AND VP in unreleasedVersions()");
        assertTooComplex("project = TWENTY AND VP not in unreleasedVersions()");

        final List<CustomFieldContext> contexts = generateContexts(proj1, proj2, TYPE_BUG, TYPE_TASK);
        final List<Query> queries = generateQueriesForVersion("VP = v1", "10013", "10010", customFieldId, proj1, proj2, TYPE_BUG, TYPE_TASK);

        assertQueriesInContexts(contexts, queries, null, customFieldId, fieldConfigSchemeId);
    }

    private void assertQueriesInContexts(List<CustomFieldContext> contexts, List<Query> queries, IssueNavigatorAssertions.FilterFormParam cfParam, final String customFieldId, final String fieldConfigSchemeId)
    {
        for (CustomFieldContext context : contexts)
        {
            // setup the context
            final List<String> issueTypes = context.getIssueTypes();
            String[] issueTypeIds = issueTypes == null ? new String[0] : issueTypes.toArray(new String[issueTypes.size()]);
            final List<String> projects = context.getProjects();
            String[] projectIds = projects == null ? new String[0] : projects.toArray(new String[projects.size()]);

            log("");
            log("Setting up context: " + context);
            log("");
            administration.customFields().editConfigurationSchemeContextById(customFieldId, fieldConfigSchemeId, null, issueTypeIds, projectIds);

            for (Query query : queries)
            {
                final String jql = query.getJql();
                if (query.shouldFit(context))
                {
                    List<IssueNavigatorAssertions.FilterFormParam> params = new ArrayList<IssueNavigatorAssertions.FilterFormParam>();
                    if (cfParam != null)
                    {
                        params.add(cfParam);
                    }
                    params.addAll(query.getParams());
                    assertFitsFilterForm(jql, params.toArray(new IssueNavigatorAssertions.FilterFormParam[params.size()]));
                }
                else
                {
                    assertInvalidContext(jql);
                }
            }
        }
    }

    /**
     * For the purposes of our tests, we will configure a custom field with a fixed set of different contexts. These contexts
     * are:
     *
     * One Project, no Issue Types
     * Two Projects, no Issue Types
     * One Project, one Issue Type
     * Two Projects, one Issue Type
     * One Project, two Issue Types
     * Two Projects, two Issue Types
     * no Projects, one Issue Type
     * no Projects, two Issue Types
     *
     * @param proj1 the id of the first project to use
     * @param proj2 the id of the second project to use
     * @param type1 the id of the first issue type to use
     * @param type2 the id of the second issue type to use
     * @return the list enumerating the above contexts
     */
    private List<CustomFieldContext> generateContexts(String proj1, String proj2, String type1, String type2)
    {
        final List<CustomFieldContext> contexts = new ArrayList<CustomFieldContext>();
        contexts.add(new CustomFieldContext(Arrays.asList(proj1), null));
        contexts.add(new CustomFieldContext(Arrays.asList(proj1, proj2), null));
        contexts.add(new CustomFieldContext(Arrays.asList(proj1), Arrays.asList(type1)));
        contexts.add(new CustomFieldContext(Arrays.asList(proj1, proj2), Arrays.asList(type1)));
        contexts.add(new CustomFieldContext(Arrays.asList(proj1), Arrays.asList(type1, type2)));
        contexts.add(new CustomFieldContext(Arrays.asList(proj1, proj2), Arrays.asList(type1, type2)));
        contexts.add(new CustomFieldContext(null, Arrays.asList(type1)));
        contexts.add(new CustomFieldContext(null, Arrays.asList(type1, type2)));
        return contexts;
    }

    /**
     * Given the starting clause and the options for project and issue type operands, generates the full list of {@link Query}s
     * that should be tested for this custom field under different contexts.
     *
     * @param cfClause the starting clause for the custom field under test (will be a part of every query generated)
     * @param proj1 the first project operand - this is usually the "on" project which is in context
     * @param proj2 the second project operand - this is the "off" project which is only in context when combined with the other project
     * @param type1 the first issue type operand - this is usually the "on" type which is in context
     * @param type2 the second issue type operand - this is the "off" type which is only in context when combined with the other type
     * @return the list of queries to be tested
     */
    private List<Query> generateQueries(String cfClause, final String proj1, final String proj2, final String type1, final String type2)
    {
        final List<List<String>> projects = generateCombinationLists(proj1, proj2);
        final List<List<String>> issueTypes = generateCombinationLists(type1, type2);

        final List<Query> list = new ArrayList<Query>();
        for (List<String> issueType : issueTypes)
        {
            for (List<String> project : projects)
            {
                if (issueType != null || project != null)
                {
                    list.add(new Query(project, issueType, cfClause));
                }
            }
        }

        return list;
    }

    /**
     * Same as above, except since we are testing versions, some of the queries will not fit when combined with the base
     * clause, since it specifies a version by id which cannot be part of multiple projects. So for all queries generated,
     * the "shouldItFit" will return false if there are no projects (because versions need projects) or if the "off" project
     * operand is specified.
     *
     * @param cfClause the starting clause for the custom field under test (will be a part of every query generated)
     * @param version1
     * @param version2
     * @param customFieldId
     * @param proj1 the first project operand - this is usually the "on" project which is in context
     * @param proj2 the second project operand - this is the "off" project which is only in context when combined with the other project
     * @param type1 the first issue type operand - this is usually the "on" type which is in context
     * @param type2 the second issue type operand - this is the "off" type which is only in context when combined with the other type     @return the list of queries to be tested
     */
    private List<Query> generateQueriesForVersion(String cfClause, final String version1, final String version2, final String customFieldId, final String proj1, final String proj2, final String type1, final String type2)
    {
        final List<List<String>> projects = generateCombinationLists(proj1, proj2);
        final List<List<String>> issueTypes = generateCombinationLists(type1, type2);

        final List<Query> list = new ArrayList<Query>();
        for (final List<String> issueType : issueTypes)
        {
            for (final List<String> project : projects)
            {
                if (issueType != null || project != null)
                {
                    list.add(new Query(project, issueType, cfClause)
                    {
                        @Override
                        public List<IssueNavigatorAssertions.FilterFormParam> getParams()
                        {
                            final List<IssueNavigatorAssertions.FilterFormParam> paramList = super.getParams();
                            if (project != null)
                            {
                                if (project.contains(proj1) && project.contains(proj2))
                                {
                                    paramList.add(createFilterFormParam("customfield_" + customFieldId, version1, version2));
                                }
                                else if (project.contains(proj1))
                                {
                                    paramList.add(createFilterFormParam("customfield_" + customFieldId, version1));
                                }
                                else if (project.contains(proj2))
                                {
                                    paramList.add(createFilterFormParam("customfield_" + customFieldId, version2));
                                }
                            }
                            return paramList;
                        }
                    });
                }
            }
        }

        return list;
    }

    private List<List<String>> generateCombinationLists(final String s1, final String s2)
    {
        List<List<String>> projects = new ArrayList<List<String>>();
        projects.add(null);
        projects.add(Collections.singletonList(s1));
        projects.add(Collections.singletonList(s2));
        projects.add(Arrays.asList(s1, s2));
        return projects;
    }

    /**
     * Representation of a JQL query to run and check for fitness against.
     */
    private static class Query
    {
        private List<String> projects;
        private List<String> issueTypes;
        private String cfClause;

        /**
         * @param projects the project ids to specify in this query; null means dont include project clause
         * @param issueTypes the issue type ids to specify in this query; null means dont include issue type clause
         * @param cfClause the base clause of the custom field that we are testing
         */
        private Query(final List<String> projects, final List<String> issueTypes, final String cfClause)
        {
            this.projects = projects;
            this.issueTypes = issueTypes;
            this.cfClause = cfClause;
        }

        /**
         * @return the JQL string to execute for this query
         */
        public String getJql()
        {
            final StringBuilder sb = new StringBuilder(cfClause);
            addClauseToJql(sb, "project", projects);
            addClauseToJql(sb, "type", issueTypes);
            return sb.toString();
        }

        /**
         * @return the {@link com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions.FilterFormParam}s that would be expected (based on the criteria), should this query fit in the
         * filter form
         */
        public List<IssueNavigatorAssertions.FilterFormParam> getParams()
        {
            List<IssueNavigatorAssertions.FilterFormParam> params = new ArrayList<IssueNavigatorAssertions.FilterFormParam>();
            if (projects != null)
            {
                String[] values = new String[projects.size()];
                values = projects.toArray(values);
                params.add(createFilterFormParam("pid", values));
            }
            if (issueTypes != null)
            {
                String[] values = new String[issueTypes.size()];
                values = issueTypes.toArray(values);
                params.add(createFilterFormParam("type", values));
            }
            return params;
        }

        /**
         * @param context the context of the custom field that this query will be executed under
         * @return whether or not we expect this query to fit in the filter form. If the context specifies projects, then
         * the query must specify project clauses, and all the projects listed must be a part of the context. If the context
         * specifies issue types, the query must specify issue type clauses, and all the types listed must be a part of
         * the context. If the context specifies both, then both conditions must be true.
         */
        public boolean shouldFit(CustomFieldContext context)
        {
            boolean fit = true;
            if (context.getProjects() != null)
            {
                fit = this.projects != null && context.getProjects().containsAll(this.projects);
            }
            if (context.getIssueTypes() != null)
            {
                fit &= this.issueTypes != null && context.getIssueTypes().containsAll(this.issueTypes);
            }

            return fit;
        }

        private void addClauseToJql(final StringBuilder sb, String clauseName, final List<String> operands)
        {
            if (operands != null)
            {
                sb.append(" AND ").append(clauseName);
                if (operands.size() == 1)
                {
                    sb.append(" = ").append(operands.get(0));
                }
                else
                {
                    sb.append(" IN (").append(StringUtils.join(operands, ", ")).append(")");
                }
            }
        }

        @Override
        public String toString()
        {
            return getJql();
        }
    }

    /**
     * Represents a custom field context - a collection of projects and issue types associated with it.
     */
    private static class CustomFieldContext
    {
        private List<String> projects;
        private List<String> issueTypes;

        /**
         * @param projects the project ids associated with this context; null indicates Global (all projects)
         * @param issueTypes the issue type ids associated with this context; null indicates all issue types
         */
        private CustomFieldContext(final List<String> projects, final List<String> issueTypes)
        {
            this.projects = projects;
            this.issueTypes = issueTypes;
        }

        public List<String> getProjects()
        {
            return projects;
        }

        public List<String> getIssueTypes()
        {
            return issueTypes;
        }

        @Override
        public String toString()
        {
            return "CustomFieldContext{" +
                    "projects=" + projects +
                    ", issueTypes=" + issueTypes +
                    '}';
        }
    }
}
