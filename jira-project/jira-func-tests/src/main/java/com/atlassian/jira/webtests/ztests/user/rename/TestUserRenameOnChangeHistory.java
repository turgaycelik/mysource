package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.table.HtmlTable;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.ISSUES, Category.ISSUE_NAVIGATOR})
public class TestUserRenameOnChangeHistory extends FuncTestCase
{
    public static final String BROWSE_ISSUE_WITH_HISTORY_URL = "browse/%s?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel";
    public static final String AUTHOR_ID = "changehistoryauthor_%d";
    public static final String CHANGE_TABLE_ID = "changehistory_%d";
    public static final String SINGLE_USER_FIELD_ID = "customfield_10400";
    public static final String MULTI_USER_FIELD_ID = "customfield_10401";
    public static final String SINGLE_USER_FIELD_NAME = "Single user";
    public static final String MULTI_USER_FIELD_NAME = "Multi user";
    public static final String NOTHING = "";
    public static final String CC_CAT_CHANGE = "Candy Chaos, Crazy Cat [ ID10101, cc ]";
    public static final String RENAMED_CC_CAT_CHANGE = "Candy Chaos, Crazy Cat [ ID10101, cc ]";
    public static final String BB_CRAZY_CANDY = "Betty Boop, Candy Chaos, Crazy Cat [ bb, ID10101, cc ]";
    public static final String ADAM_ANT_SYSTEM_FIELD_DISPLAY = "Adam Ant [ admin ]";
    public static final String CRAZY_CAT_SYSTEM_FIELD_DISPLAY = "Crazy Cat [ cc ]";
    public static final String CANDY_CHAOS_SYSTEM_FIELD_DISPLAY = "Candy Chaos [ ID10101 ]";
    public static final String BOB_BELCHER_SYSTEM_FIELD_DISPLAY = "Bob Belcher [ ID10001 ]";
    public static final String BETTY_BOOP_SYSTEM_FIELD_DISPLAY = "Betty Boop [ bb ]";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("user_rename_customfields.xml");
        administration.backdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
        administration.backdoor().darkFeatures().enableForSite("no.frother.assignee.field");

        //    KEY       USERNAME    NAME
        //    bb	    betty	    Betty Boop
        //    ID10001	bb	        Bob Belcher
        //    cc	    cat	        Crazy Cat
        //    ID10101	cc	        Candy Chaos
    }

    public void testCustomFieldChangeAuthorPreservedDespiteRename()
    {
        //Make some changes as Bob
        navigation.login("bb");
        navigation.issue().setFreeTextCustomField("COW-2", SINGLE_USER_FIELD_ID, "bb");
        navigation.issue().setFreeTextCustomField("COW-2", MULTI_USER_FIELD_ID, "cc, cat");

        assertChangeRecordedCorrectly("COW-2", 10800, "Bob Belcher", SINGLE_USER_FIELD_NAME, NOTHING, "Bob Belcher [ ID10001 ]");
        assertChangeRecordedCorrectly("COW-2", 10801, "Bob Belcher", MULTI_USER_FIELD_ID, NOTHING, CC_CAT_CHANGE);

        //Rename and re-assert
        navigation.login("admin");
        renameUser("bb", "belchyman");
        assertChangeRecordedCorrectly("COW-2", 10800, "Bob Belcher", SINGLE_USER_FIELD_NAME, NOTHING, "Bob Belcher [ ID10001 ]");
        assertChangeRecordedCorrectly("COW-2", 10801, "Bob Belcher", MULTI_USER_FIELD_ID, NOTHING, CC_CAT_CHANGE);

        //Make some changes as Betty
        navigation.login("betty");
        navigation.issue().setFreeTextCustomField("COW-3", SINGLE_USER_FIELD_ID, "cc");
        navigation.issue().setFreeTextCustomField("COW-3", MULTI_USER_FIELD_ID, "cc, cat");

        //Rename and re-assert
        assertChangeRecordedCorrectly("COW-3", 10802, "Betty Boop", SINGLE_USER_FIELD_NAME, NOTHING, "Candy Chaos [ ID10101 ]");
        assertChangeRecordedCorrectly("COW-3", 10803, "Betty Boop", MULTI_USER_FIELD_NAME, NOTHING, CC_CAT_CHANGE);

        navigation.login("admin");
        renameUser("betty", "bb");
        renameUser("cat", "crazy");
        renameUser("cc", "candy");

        navigation.issue().setFreeTextCustomField("COW-3", MULTI_USER_FIELD_ID, "bb, crazy, candy");
        assertChangeRecordedCorrectly("COW-3", 10802, "Betty Boop", SINGLE_USER_FIELD_NAME, NOTHING, "Candy Chaos [ ID10101 ]");
        assertChangeRecordedCorrectly("COW-3", 10803, "Betty Boop", MULTI_USER_FIELD_NAME, NOTHING, CC_CAT_CHANGE);
        assertChangeRecordedCorrectly("COW-3", 10804, "Adam Ant", MULTI_USER_FIELD_NAME, RENAMED_CC_CAT_CHANGE, BB_CRAZY_CANDY);

        // Check that changing a user to their renamed self doesn't register as a change
        renameUser("crazy", "meow");
        navigation.issue().setFreeTextCustomField("COW-3", SINGLE_USER_FIELD_ID, "candy");
        navigation.issue().setFreeTextCustomField("COW-3", MULTI_USER_FIELD_ID, "bb, meow, candy");
        assertFalse(locator.id(String.format(CHANGE_TABLE_ID, 10805)).exists());
    }

    public void testReporterAndAssigneeChangeAuthorPreservedDespiteRename()
    {
        backdoor.usersAndGroups().addUserToGroup("bb","jira-administrators");
        backdoor.usersAndGroups().addUserToGroup("betty","jira-administrators");

        // Make changes as Betty
        navigation.login("betty");
        navigation.issue().gotoEditIssue("COW-4");
        tester.setFormElement("assignee","cat");
        tester.setFormElement("reporter","cc");
        tester.submit("Update");

        Map<String,Change> expectedChanges = new HashMap<String, Change>();
        expectedChanges.put("Assignee", new Change(ADAM_ANT_SYSTEM_FIELD_DISPLAY, CRAZY_CAT_SYSTEM_FIELD_DISPLAY));
        expectedChanges.put("Reporter", new Change(ADAM_ANT_SYSTEM_FIELD_DISPLAY, CANDY_CHAOS_SYSTEM_FIELD_DISPLAY));
        assertChangeRecordedCorrectly("COW-4", 10800, "Betty Boop", expectedChanges);

        // Rename and re-assert
        navigation.login("admin");
        renameUser("bb", "belchyman");
        renameUser("betty", "bb");
        assertChangeRecordedCorrectly("COW-4", 10800, "Betty Boop", expectedChanges);
        renameUser("bb", "betty");
        renameUser("belchyman", "bb");

        // Make changes as Bob
        navigation.login("bb");
        navigation.issue().gotoEditIssue("COW-1");
        tester.setFormElement("assignee", "cat");
        tester.setFormElement("reporter","cc");
        tester.submit("Update");
        expectedChanges.clear();
        expectedChanges.put("Assignee",new Change(BETTY_BOOP_SYSTEM_FIELD_DISPLAY, CRAZY_CAT_SYSTEM_FIELD_DISPLAY));
        expectedChanges.put("Reporter",new Change(CRAZY_CAT_SYSTEM_FIELD_DISPLAY, CANDY_CHAOS_SYSTEM_FIELD_DISPLAY));
        assertChangeRecordedCorrectly("COW-1", 10801, "Bob Belcher", expectedChanges);

        // Rename and re-assert
        navigation.login("admin");
        renameUser("cat","crazy");
        renameUser("bb","belchyman");
        renameUser("cc", "candy");
        assertChangeRecordedCorrectly("COW-1", 10801, "Bob Belcher", expectedChanges);
        navigation.login("belchyman", "bb");

        navigation.issue().assignIssue("COW-4", null, "Bob Belcher");
        assertChangeRecordedCorrectly("COW-4", 10802, "Bob Belcher", "Assignee", CRAZY_CAT_SYSTEM_FIELD_DISPLAY, BOB_BELCHER_SYSTEM_FIELD_DISPLAY);

        navigation.issue().gotoEditIssue("COW-4");
        tester.setFormElement("reporter", "belchyman");
        tester.submit("Update");
        assertChangeRecordedCorrectly("COW-4", 10803, "Bob Belcher", "Reporter", CANDY_CHAOS_SYSTEM_FIELD_DISPLAY, BOB_BELCHER_SYSTEM_FIELD_DISPLAY);
    }

    private void assertChangeRecordedCorrectly(String issueKey, int changeId, String expectedAuthorName, String fieldName, String oldValue, String newValue)
    {
        Map<String,Change> expectedChanges = new HashMap<String, Change>();
        expectedChanges.put(fieldName, new Change(oldValue,newValue));
        assertChangeRecordedCorrectly(issueKey, changeId, expectedAuthorName, expectedChanges);
    }

    private void assertChangeRecordedCorrectly(String issueKey, int changeId, String expectedAuthorName, Map<String,Change> changesByFieldName)
    {
        navigation.gotoPage(String.format(BROWSE_ISSUE_WITH_HISTORY_URL, issueKey));
        text.assertTextPresent(locator.id(String.format(AUTHOR_ID, changeId)), expectedAuthorName);
        HtmlTable changesTable = page.getHtmlTable(String.format(CHANGE_TABLE_ID,changeId));
        for (HtmlTable.Row row: changesTable.getRows())
        {
            // Can't do this because only the first change table on the page has headers:
            // String field = row.getCellForHeading("Field");
            String field = row.getCellAsText(0);
            if (!changesByFieldName.containsKey(field)) continue;
            Change expectedChange = changesByFieldName.get(field);
            String actualOld = row.getCellAsText(1);
            String actualNew = row.getCellAsText(2);
            assertEquals(expectedChange.oldValue,actualOld);
            assertEquals(expectedChange.newValue,actualNew);
        }
    }

    private void renameUser(String from, String to)
    {
        navigation.gotoPage(String.format("secure/admin/user/EditUser!default.jspa?editName=%s", from));
        tester.setFormElement("username", to);
        tester.submit("Update");
    }

    private static class Change
    {
        public final String oldValue;
        public final String newValue;

        public Change(String oldValue, String newValue)
        {
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
}