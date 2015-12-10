package com.atlassian.jira.webtests.ztests.jelly;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryItem;
import com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JELLY })
public class TestJellyWorkflowTransition extends FuncTestCase
{
    // this map is used to make assertions about the values in the top left issue detail section
    private final static Map<String, Object> ISSUE_DETAILS_FIELD_VALUE_MAP = new LinkedHashMap<String, Object>();
    // this map contains expected field values for all the fields shown on HSP-2
    private final static Map<String, Object> FIELD_VALUE_MAP = new LinkedHashMap<String, Object>();
    // this map is used to update each field to a new value one at a time
    private final static Map<String, FieldDetail> FIELD_UPDATE_MAP = new LinkedHashMap<String, FieldDetail>();

    static
    {
        ISSUE_DETAILS_FIELD_VALUE_MAP.put("Type", "Bug");
        ISSUE_DETAILS_FIELD_VALUE_MAP.put("Status", "Resolved");
        ISSUE_DETAILS_FIELD_VALUE_MAP.put("Priority", "Minor");
        ISSUE_DETAILS_FIELD_VALUE_MAP.put("Resolution", "Fixed");
        ISSUE_DETAILS_FIELD_VALUE_MAP.put("Assignee", ADMIN_FULLNAME);
        ISSUE_DETAILS_FIELD_VALUE_MAP.put("Reporter", ADMIN_FULLNAME);

        //summary doesn't have a label
        FIELD_VALUE_MAP.put("Values in all fields", null);
        FIELD_VALUE_MAP.put("Affects Version/s:", CollectionBuilder.newBuilder("New Version 1", "New Version 4").asList());
        FIELD_VALUE_MAP.put("Fix Version/s:", CollectionBuilder.newBuilder("New Version 1", "New Version 4").asList());
        FIELD_VALUE_MAP.put("Component/s:", "New Component 1");
        FIELD_VALUE_MAP.put("Environment", "My environment is MacOs");
        
        FIELD_VALUE_MAP.put("Free Text CF:", "This is some free text");
        FIELD_VALUE_MAP.put("Multi Checkbox:", "red");
        FIELD_VALUE_MAP.put("Multi select:", "monkey");
        FIELD_VALUE_MAP.put("Number CF:", "123,123");
        FIELD_VALUE_MAP.put("Project picker CF:", "homosapien");
        FIELD_VALUE_MAP.put("Radio Buttons CF:", "TV");
        FIELD_VALUE_MAP.put("Select CF:", "Nokia");
        FIELD_VALUE_MAP.put("Single Version CF:", "New Version 1");
        FIELD_VALUE_MAP.put("URL CF:", "http://www.msn.com/");
        FIELD_VALUE_MAP.put("Text CF:", "The sky is blue");
        FIELD_VALUE_MAP.put("Version CF:", "New Version 1");
        FIELD_VALUE_MAP.put("cascadingSelect:", CollectionBuilder.newBuilder("Fruit", "banana").asList());
        FIELD_VALUE_MAP.put("Description", "This is a really boring issue with an even lamer description.");
        //comment also doesn't have a label
        FIELD_VALUE_MAP.put("Yay! all fields have a value!", null);
        FIELD_VALUE_MAP.put("Group Picker:", "jira-administrators");
        FIELD_VALUE_MAP.put("Multi Group Picker CF:", CollectionBuilder.newBuilder("jira-administrators", "jira-developers").asList());
        FIELD_VALUE_MAP.put("Multi User CF:", CollectionBuilder.newBuilder(ADMIN_FULLNAME, FRED_FULLNAME).asList());
        FIELD_VALUE_MAP.put("user picker CF:", ADMIN_FULLNAME);
        FIELD_VALUE_MAP.put("Date picker cf:", "07/Apr/09");
        FIELD_VALUE_MAP.put("Date time:", "29/Apr/09 3:01 PM");

        FIELD_UPDATE_MAP.put("resolution", new FieldDetail("Resolution", "2", "Won't Fix", true));
        FIELD_UPDATE_MAP.put("fixVersions", new FieldDetail("Fix Version/s:", "10002", "New Version 5", false));
        FIELD_UPDATE_MAP.put("customfield_10000", new FieldDetail("cascadingSelect:", new String[] { "10012", "10016" }, new String[] { "Vegetable", "carrot" }, false, "customfield_10000:1"));
        FIELD_UPDATE_MAP.put("customfield_10001", new FieldDetail("Date time:", "14/Apr/09 1:01 PM", "14/Apr/09 1:01 PM", false));
        FIELD_UPDATE_MAP.put("customfield_10002", new FieldDetail("Group Picker:", "jira-users", "jira-users", false));
        FIELD_UPDATE_MAP.put("customfield_10003", new FieldDetail("Multi Checkbox:", "10001", "blue", false));
        FIELD_UPDATE_MAP.put("customfield_10004", new FieldDetail("Multi select:", "10004", "cat", false));
        FIELD_UPDATE_MAP.put("customfield_10005", new FieldDetail("Number CF:", "344", "344", false));
        FIELD_UPDATE_MAP.put("customfield_10006", new FieldDetail("Radio Buttons CF:", "10005", "Radio", false));
        FIELD_UPDATE_MAP.put("customfield_10007", new FieldDetail("Select CF:", "10008", "Apple", false));
        FIELD_UPDATE_MAP.put("customfield_10008", new FieldDetail("Text CF:", "The sea is blue too", "The sea is blue too", false));
        FIELD_UPDATE_MAP.put("customfield_10009", new FieldDetail("user picker CF:", FRED_USERNAME, FRED_FULLNAME, false));
        FIELD_UPDATE_MAP.put("customfield_10010", new FieldDetail("Date picker cf:", "1/Apr/09", "1/Apr/09", false));
        FIELD_UPDATE_MAP.put("customfield_10011", new FieldDetail("Free Text CF:", "Lorem ipsum", "Lorem ipsum", false));
        FIELD_UPDATE_MAP.put("customfield_10013", new FieldDetail("Multi Group Picker CF:", "jira-users", "jira-users", false));
        FIELD_UPDATE_MAP.put("customfield_10014", new FieldDetail("Multi User CF:", FRED_USERNAME, FRED_FULLNAME, false));
        FIELD_UPDATE_MAP.put("customfield_10015", new FieldDetail("Project picker CF:", "10001", "monkey", false));
        FIELD_UPDATE_MAP.put("customfield_10017", new FieldDetail("Single Version CF:", "10001", "New Version 4", false));
        FIELD_UPDATE_MAP.put("customfield_10018", new FieldDetail("URL CF:", "http://www.google.com", "http://www.google.com", false));
        FIELD_UPDATE_MAP.put("customfield_10019", new FieldDetail("Version CF:", "10002", "New Version 5", false));
        FIELD_UPDATE_MAP.put("versions", new FieldDetail("Affects Version/s:", "10002", "New Version 5", false));
        FIELD_UPDATE_MAP.put("components", new FieldDetail("Component/s:", "10002", "New Component 3", false));
        FIELD_UPDATE_MAP.put("description", new FieldDetail("Description", "A better description.", "A better description.", false));
        FIELD_UPDATE_MAP.put("environment", new FieldDetail("Environment", "Actually the environment is windows", "Actually the environment is windows", false));
        FIELD_UPDATE_MAP.put("priority", new FieldDetail("Priority", "2", "Critical", true));
    }

    private static class FieldDetail
    {
        private String fieldName;
        private String[] fieldUpdateValue;
        private String[] fieldUpdateName;
        private boolean isIssueDetail;
        private String optionalId;

        private FieldDetail(final String fieldName, final String fieldUpdateValue, final String fieldUpdateName, final boolean issueDetail)
        {
            this.fieldName = fieldName;
            this.fieldUpdateValue = new String[] { fieldUpdateValue };
            this.fieldUpdateName = new String[] { fieldUpdateName };
            isIssueDetail = issueDetail;
        }

        private FieldDetail(final String fieldName, final String[] fieldUpdateValue, final String[] fieldUpdateName, final boolean issueDetail, final String optionalId)
        {
            this.fieldName = fieldName;
            this.fieldUpdateValue = fieldUpdateValue;
            this.fieldUpdateName = fieldUpdateName;
            isIssueDetail = issueDetail;
            this.optionalId = optionalId;
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public String[] getFieldUpdateName()
        {
            return fieldUpdateName;
        }

        @Override
        public String toString()
        {
            return "{fieldUpdateValue=" + (fieldUpdateValue == null ? null : Arrays.asList(fieldUpdateValue)) +
                    ", fieldUpdateName=" + (fieldUpdateName == null ? null : Arrays.asList(fieldUpdateName)) +
                    '}';
        }
    }

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestJellyWorkflowTransition.xml");
    }

    public void testTransitionIssueWithoutSettingResolution() throws Exception
    {
        boolean isOracle = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isOracle();

        final String invalidScript = "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\">\n"
                + "    <jira:TransitionWorkflow key=\"HSP-1\" user=\"admin\" workflowAction=\"Resolve Issue\"/>\n"
                + "</JiraJelly>";
        administration.runJellyScript(invalidScript);
        //Should have resolved issue with the default transition!
        tester.assertTextPresent("Jelly script completed successfully.");

        //check that the issue has been transitioned with the default Resolution
        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new IdLocator(tester, "resolution-val"), "Fixed");
        text.assertTextPresent(new IdLocator(tester, "status-val"), "Resolved");

        tester.assertLinkNotPresentWithText("Resolve Issue");
        assertions.assertLastChangeHistoryRecords("HSP-1", new ExpectedChangeHistoryRecord(CollectionBuilder.newBuilder(
                new ExpectedChangeHistoryItem("Status", "Open", "Resolved"),
                new ExpectedChangeHistoryItem("Resolution", null, "Fixed")).asList()));

        if(isOracle)
        {
            //TODO: Remove this sleep hack once http://jira.atlassian.com/browse/JRA-20274 has been resolved
            Thread.sleep(2000);
        }

        //now try transitioning an issue with a resolution set currently
        navigation.issue().viewIssue("HSP-2");
        tester.clickLink("action_id_711");
        tester.setWorkingForm("issue-workflow-transition");
        tester.selectOption("resolution", "Won't Fix");
        tester.submit("Transition");
        text.assertTextPresent(new IdLocator(tester, "resolution-val"), "Won't Fix");

        if(isOracle)
        {
            //TODO: Remove this sleep hack once http://jira.atlassian.com/browse/JRA-20274 has been resolved
            Thread.sleep(2000);
        }
        
        final String script = "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\">\n"
                + "    <jira:TransitionWorkflow key=\"HSP-2\" user=\"admin\" workflowAction=\"ResolveSomeMore\"/>\n"
                + "</JiraJelly>";
        administration.runJellyScript(script);
        tester.assertTextPresent("Jelly script completed successfully.");

        //also check that the issue really HAS been transitioned!  The resolution should have stayed the same!
        navigation.issue().viewIssue("HSP-2");
        text.assertTextPresent(new IdLocator(tester, "resolution-val"), "Won't Fix");
        tester.assertLinkNotPresentWithText("Resolve Issue");
        tester.assertLinkPresentWithText("ResolveSomeMore");
        assertions.assertLastChangeHistoryRecords("HSP-2", new ExpectedChangeHistoryRecord(CollectionBuilder.newBuilder(
                new ExpectedChangeHistoryItem("Status", "Resolved", "Resolved")).asList()));
    }

    public void testTransitionIssueWithoutResolutionFieldPresent() throws Exception 
    {
        boolean isOracle = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isOracle();

        //first remove the resolution field from the 'Resolve' Screen
        backdoor.screens().removeFieldFromScreen("Resolve Issue Screen", "Resolution");
        text.assertTextNotPresent(new TableLocator(tester, "field_table"), "Resolution");

        if(isOracle)
        {
            //TODO: Remove this sleep hack once http://jira.atlassian.com/browse/JRA-20274 has been resolved
            Thread.sleep(2000);
        }

        //now transition an issue that isn't resolved yet via Jelly, and don't set the resolution!
        final String invalidScript = "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\">\n"
                + "    <jira:TransitionWorkflow key=\"HSP-1\" user=\"admin\" workflowAction=\"Resolve Issue\"/>\n"
                + "</JiraJelly>";
        administration.runJellyScript(invalidScript);
        tester.assertTextPresent("Jelly script completed successfully.");

        //The issue has been transitioned, but no resolution will be set.
        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new IdLocator(tester, "resolution-val"), "Unresolved");
        tester.assertLinkNotPresentWithText("Resolve Issue");
        assertions.assertLastChangeHistoryRecords("HSP-1", new ExpectedChangeHistoryRecord(CollectionBuilder.newBuilder(
                new ExpectedChangeHistoryItem("Status", "Open", "Resolved")).asList()));

        if(isOracle)
        {
            //TODO: Remove this sleep hack once http://jira.atlassian.com/browse/JRA-20274 has been resolved
            Thread.sleep(2000);
        }

        //now try the same with an issue that has already been resolved.  This should work fine!
        final String validScript = "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\">\n"
                + "    <jira:TransitionWorkflow key=\"HSP-2\" user=\"admin\" workflowAction=\"ResolveSomeMore\"/>\n"
                + "</JiraJelly>";
        administration.runJellyScript(validScript);
        tester.assertTextPresent("Jelly script completed successfully.");

        //also check that the issue really HAS been transitioned!
        navigation.issue().viewIssue("HSP-2");
        text.assertTextPresent(new IdLocator(tester, "resolution-val"), "Fixed");
        tester.assertLinkNotPresentWithText("Resolve Issue");
        assertions.assertLastChangeHistoryRecords("HSP-1", new ExpectedChangeHistoryRecord(CollectionBuilder.newBuilder(
                new ExpectedChangeHistoryItem("Status", "Open", "Resolved")).asList()));
    }

    public void testTransitionIssueChangingAllFieldValues()
    {
        assertFieldValues("HSP-2", ISSUE_DETAILS_FIELD_VALUE_MAP, FIELD_VALUE_MAP);

        final String validScriptPrefix = "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\">\n"
                + "    <jira:TransitionWorkflow key=\"HSP-2\" user=\"admin\" workflowAction=\"ResolveSomeMore\" resolution=\"Won't Fix\"";

        final String validScriptPostfix = "\n</jira:TransitionWorkflow></JiraJelly >";

        final Map<String, Object> issueDetails = new LinkedHashMap<String, Object>(ISSUE_DETAILS_FIELD_VALUE_MAP);
        final Map<String, Object> fieldDetails = new LinkedHashMap<String, Object>(FIELD_VALUE_MAP);
        for (Map.Entry<String, FieldDetail> fieldUpdateEntry : FIELD_UPDATE_MAP.entrySet())
        {
            String fieldId = fieldUpdateEntry.getKey();
            FieldDetail fieldDetail = fieldUpdateEntry.getValue();
            log("Updating field '" + fieldId + "' with '" + fieldDetail);


            String script;
            if (fieldDetail.optionalId == null)
            {
                if (fieldId.startsWith("customfield_"))
                {
                    script = new StringBuilder().
                            append(validScriptPrefix).append(">\n").
                                    append("<jira:AddCustomFieldValue id=\"").append(fieldId).append("\" value=\"").append(fieldDetail.fieldUpdateValue[0]).append("\"/>").
                            append(validScriptPostfix).toString();
                }
                else
                {
                    script = new StringBuilder().
                            append(validScriptPrefix).append(" ").append(fieldId).append("=\"").append(fieldDetail.fieldUpdateValue[0]).append("\">").
                            append(validScriptPostfix).toString();
                }
            }
            else
            {
                //cascading selects are a bitch!
                script = new StringBuilder().
                        append(validScriptPrefix).append(">\n").
                        append("<jira:AddCustomFieldValue id=\"").append(fieldId).append("\" value=\"").append(fieldDetail.fieldUpdateValue[0]).append("\"/>\n").
                        append("<jira:AddCustomFieldValue id=\"").append(fieldId).append("\" value=\"").append(fieldDetail.fieldUpdateValue[1]).append("\" key=\"1\"/>").
                        append(validScriptPostfix).toString();
            }
            administration.runJellyScript(script);
            tester.assertTextPresent("Jelly script completed successfully.");

            navigation.issue().viewIssue("HSP-2");
            if (fieldDetail.isIssueDetail)
            {
                issueDetails.put(fieldDetail.getFieldName(), Arrays.asList(fieldDetail.getFieldUpdateName()));
            }
            else
            {
                fieldDetails.put(fieldDetail.getFieldName(), Arrays.asList(fieldDetail.getFieldUpdateName()));
            }
            assertFieldValues("HSP-2", issueDetails, fieldDetails);
        }
    }

    public void testTransitionIssueRemovingAllFieldValues()
    {

    }

    private void assertFieldValues(String issueKey, Map<String, Object> issueDetails, Map<String, Object> fieldDetails)
    {
        navigation.issue().viewIssue(issueKey);
        final List<String> issueDetailAssertions = getTextAssertions(issueDetails);
        text.assertTextSequence(new WebPageLocator(tester), issueDetailAssertions.toArray(new String[issueDetailAssertions.size()]));

        final List<String> textAssertions = getTextAssertions(fieldDetails);
        //assert all fields have the correct value!
        text.assertTextSequence(new WebPageLocator(tester), textAssertions.toArray(new String[textAssertions.size()]));
    }

    private List<String> getTextAssertions(final Map<String, Object> fieldValueMap)
    {
        final List<String> textAssertions = new ArrayList<String>();
        for (Map.Entry<String, Object> fieldEntry : fieldValueMap.entrySet())
        {
            textAssertions.add(fieldEntry.getKey());
            if (fieldEntry.getValue() != null)
            {
                if (fieldEntry.getValue() instanceof Collection<?>)
                {
                    //noinspection unchecked
                    textAssertions.addAll((Collection<? extends String>) fieldEntry.getValue());
                }
                else
                {
                    textAssertions.add((String) fieldEntry.getValue());
                }
            }
        }
        return textAssertions;
    }

}
