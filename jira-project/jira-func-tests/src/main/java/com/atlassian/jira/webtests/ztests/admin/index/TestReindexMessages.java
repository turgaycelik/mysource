package com.atlassian.jira.webtests.ztests.admin.index;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.SystemTenantOnly;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.CUSTOM_FIELDS, Category.FIELDS, Category.INDEXING })
public class TestReindexMessages extends FuncTestCase
{
    public void testEditProjectFieldConfigScheme() throws Exception
    {
        administration.restoreData("TestEditProjectFieldConfigScheme.xml");

        changeFieldConfigSchemeAndAssertMessage(null, "System Default Field Configuration", false);
        changeFieldConfigSchemeAndAssertMessage(null, "Same as default scheme", false);
        changeFieldConfigSchemeAndAssertMessage("Same as default scheme", "Different scheme 2", true);
        changeFieldConfigSchemeAndAssertMessage("Different scheme 2", "Different scheme 1", true);
        changeFieldConfigSchemeAndAssertMessage(null, "Exhaustive 1", false);
        changeFieldConfigSchemeAndAssertMessage("Exhaustive 1", "Exhaustive 2 (same as default)", true);
        changeFieldConfigSchemeAndAssertMessage(null, "Exhaustive 2 copy with different default", false);
        changeFieldConfigSchemeAndAssertMessage(null, "System Default Field Configuration", false);
    }

    public void testVoting() throws Exception
    {
        administration.restoreBlankInstance();
        administration.generalConfiguration().disableVoting();
        assertNoVotingMessage();
        administration.generalConfiguration().enableVoting();
        assertVotingMessage();
        administration.reIndex();
        administration.generalConfiguration().disableVoting();
        assertNoVotingMessage();
    }

    public void testTimeTracking() throws Exception
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        assertTimeTrackingMessage();
        administration.reIndex();
        administration.timeTracking().disable();
        assertNoTimeTrackingMessage();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        assertTimeTrackingMessage();
    }

    public void testAddCustomField() throws Exception
    {
        // when adding a custom field, we only will suggest a reindex if there are issues present in the context of the new field
        administration.restoreBlankInstance();

        administration.project().addProject("third project", "THIRD", ADMIN_USERNAME);

        // for a set of contexts (global, issue type only, project only, project and issue type)
        _testAddCustomFieldGlobalContext();
        _testAddCustomFieldSpecificContext(new String[] { }, new String[] { "4" }, "monkey", "Improvement");
        _testAddCustomFieldSpecificContext(new String[] { }, new String[] { "4", "1" }, "monkey", "Improvement");
        _testAddCustomFieldSpecificContext(new String[] { "10000" }, new String[] { }, "homosapien", "Bug");
        _testAddCustomFieldSpecificContext(new String[] { "10000", "10001" }, new String[] { }, "homosapien", "Bug");
        _testAddCustomFieldSpecificContext(new String[] { "10000" }, new String[] { "4" }, "homosapien", "Improvement");
        _testAddCustomFieldSpecificContext(new String[] { "10000", "10001" }, new String[] { "4", "1" }, "homosapien", "Improvement");
    }

    public void testIssueCountOverridesSecurity() throws Exception
    {
        // admin cant see project monkey, but can create issues. this test is checking whether the search to find issues
        // in context when adding the custom field overrides security correctly or not.
        administration.restoreData("TestReindexMessagesIssueCountOverridesSecurity.xml");

        // create custom field
        String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF");

        // no message
        assertNoCustomFieldMessage();

        // create issue in context
        navigation.issue().createIssue("monkey", "Bug", "Test");

        // re-add custom field
        administration.customFields().removeCustomField(customFieldId);
        administration.reIndex();

        administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF");

        // message
        assertCustomFieldMessage();
    }

    public void testRemoveCustomFieldOption() throws Exception
    {
        administration.restoreBlankInstance();

        final String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:select", "selectCF");
        final String numericCustomFieldId = customFieldId.split("_")[1];

        final String[] options = { BOB_USERNAME, "joe", "jill" };

        administration.customFields().addOptions(numericCustomFieldId, options);
        administration.reIndex();

        for (String option : new String[] {"10000", "10001"})
        {
            administration.customFields().removeOptions(numericCustomFieldId, option);

            assertCustomFieldMessage();
            administration.reIndex();
            assertNoCustomFieldMessage();
        }
    }

    public void testEditCustomFieldSearcher() throws Exception
    {
        administration.restoreBlankInstance();
        String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:float", "NumberCF", new String[] { }, new String[] { });
        String numericCustomFieldId = customFieldId.split("_")[1];

        // set to a different searcher - message
        administration.customFields().setCustomFieldSearcher(numericCustomFieldId, "com.atlassian.jira.plugin.system.customfieldtypes:numberrange");
        assertCustomFieldMessage();
        administration.reIndex();

        // set to no searcher - no message
        administration.customFields().setCustomFieldSearcher(numericCustomFieldId, null);
        assertNoCustomFieldMessage();
        administration.reIndex();

        // set to a searcher - message
        administration.customFields().setCustomFieldSearcher(numericCustomFieldId, "com.atlassian.jira.plugin.system.customfieldtypes:exactnumber");
        assertCustomFieldMessage();
        administration.reIndex();

        // set to same searcher - no message
        administration.customFields().setCustomFieldSearcher(numericCustomFieldId, "com.atlassian.jira.plugin.system.customfieldtypes:exactnumber");
        assertNoCustomFieldMessage();
    }

    public void testAddCustomFieldConfigurationContext() throws Exception
    {
        // if custom field already has global context, then no message will be expected
        // otherwise, we expect a message if the new context contains issues
        administration.restoreBlankInstance();

        // create custom field for global context
        String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF");
        String numericCustomFieldId = customFieldId.split("_")[1];
        administration.reIndex();

        // create issue in context
        String issue1 = navigation.issue().createIssue("monkey", "Bug", "Test");

        // add new configuration context (specific)
        administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "bla", new String[] {}, new String[] {"10000"});

        // no message
        assertNoCustomFieldMessage();

        // clean up
        navigation.issue().deleteIssue(issue1);
        administration.customFields().removeCustomField(customFieldId);
        administration.reIndex();

        // create custom field for specific context (monkey)
        customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF", new String[] { }, new String[] {"10001"});
        numericCustomFieldId = customFieldId.split("_")[1];

        // create issue in existing context
        issue1 = navigation.issue().createIssue("monkey", "Bug", "Test");

        // add new configuration context (homosapien)
        String schemeId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "bla", new String[] { }, new String[] { "10000" });

        // no message
        assertNoCustomFieldMessage();

        // clean up
        navigation.issue().deleteIssue(issue1);
        administration.customFields().removeConfigurationSchemeContextById(numericCustomFieldId, schemeId);
        administration.reIndex();

        // create issue for new context
        issue1 = navigation.issue().createIssue("homosapien", "Improvement", "Test");

        // add new configuration context (homosapien)
        schemeId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "bla", new String[] { }, new String[] { "10000" });

        // message
        assertCustomFieldMessage();

        // clean up
        navigation.issue().deleteIssue(issue1);
        administration.customFields().removeConfigurationSchemeContextById(numericCustomFieldId, schemeId);
        administration.reIndex();

        // create issue for existing context
        issue1 = navigation.issue().createIssue("monkey", "Bug", "Test");

        // add new configuration context (global)
        schemeId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "bla", new String[] { }, new String[] { });

        // message
        assertCustomFieldMessage();

        // clean up
        navigation.issue().deleteIssue(issue1);
        administration.customFields().removeConfigurationSchemeContextById(numericCustomFieldId, schemeId);
        administration.reIndex();
    }

    public void testEditCustomFieldConfigurationContext() throws Exception
    {
        // if field has global context before AND after edit; do not suggest reindex
        // if field has or used to have global context; we need to check for any issues before suggesting reindex
        // if field doesnt have global context before or after edit; check for issues in the "pre" and "post" edit contexts
        administration.restoreBlankInstance();

        // create custom field for global context
        String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF");
        String numericCustomFieldId = customFieldId.split("_")[1];

        // add Bug,Monkey context
        String bugContextId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Bug Context", new String[] { "1" }, new String[] { "10001" });

        // create issue in context
        String issue1 = navigation.issue().createIssue("monkey", "Bug", "Test");

        administration.reIndex();

        // edit Bug,Monkey context to be Improvement,Monkey context
        administration.customFields().editConfigurationSchemeContextById(numericCustomFieldId, bugContextId, "Improvement Context", new String[] {"4"}, new String[] {"10001"});

        // no message
        assertNoCustomFieldMessage();

        // cleanup and re-add global context
        navigation.issue().deleteIssue(issue1);
        administration.customFields().removeConfigurationSchemeContextById(numericCustomFieldId, bugContextId);
        administration.customFields().removeGlobalContext(numericCustomFieldId);
        String globalContextId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Global guy", new String[] { }, new String[] { });
        administration.reIndex();

        // edit global to be Bug context (with no issues around)
        administration.customFields().editConfigurationSchemeContextById(numericCustomFieldId, globalContextId, "Bug guy", new String [] {"1"}, new String[] {});

        // no message
        assertNoCustomFieldMessage();

        // cleanup and reset
        administration.customFields().removeConfigurationSchemeContextById(numericCustomFieldId, globalContextId);
        globalContextId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Global guy", new String[] { }, new String[] { });
        administration.reIndex();

        // add an issue
        issue1 = navigation.issue().createIssue("monkey", "Improvement", "Test");

        // edit global to be Bug context
        administration.customFields().editConfigurationSchemeContextById(numericCustomFieldId, globalContextId, "Bug guy", new String [] {"1"}, new String[] {});

        // message
        assertCustomFieldMessage();

        // cleanup and reset
        navigation.issue().deleteIssue(issue1);
        administration.customFields().removeConfigurationSchemeContextById(numericCustomFieldId, globalContextId);
        globalContextId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Bug guy", new String[] {"1" }, new String[] { });
        administration.reIndex();

        // add an issue
        issue1 = navigation.issue().createIssue("monkey", "Improvement", "Test");

        // edit Bug to be Global context
        administration.customFields().editConfigurationSchemeContextById(numericCustomFieldId, globalContextId, "Global guy", new String [] { }, new String[] {});

        // message
        assertCustomFieldMessage();

        // clean up
        navigation.issue().deleteIssue(issue1);
        administration.customFields().removeCustomField(customFieldId);
        administration.reIndex();

        // add a custom field with Bug context
        customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF", new String[] {"1"}, new String[] {});
        numericCustomFieldId = customFieldId.split("_")[1];
        administration.reIndex();

        // change Bug context to Improvement context with no issues around
        administration.customFields().editConfigurationSchemeContextByLabel(numericCustomFieldId, "Default Configuration Scheme for UserCF", null, new String[] {"4"}, new String[] {});
        
        // no message
        assertNoCustomFieldMessage();

        // add an issue for Improvement context (as its about to be changed to Bug)
        issue1 = navigation.issue().createIssue("monkey", "Improvement", "Test");

        // change Improvement to Bug context with issues around
        administration.customFields().editConfigurationSchemeContextByLabel(numericCustomFieldId, "Default Configuration Scheme for UserCF", null, new String[] {"1"}, new String[] {});

        // message
        assertCustomFieldMessage();

        // cleanup and remove issue
        navigation.issue().deleteIssue(issue1);
        administration.reIndex();

        // add an issue for Improvement context (as Bug is about to be changed to it)
        navigation.issue().createIssue("monkey", "Improvement", "Test");

        // change Bug context to Improvement context with issues around
        administration.customFields().editConfigurationSchemeContextByLabel(numericCustomFieldId, "Default Configuration Scheme for UserCF", null, new String[] {"4"}, new String[] {});

        // message
        assertCustomFieldMessage();
    }

    public void testRemoveCustomFieldConfigurationContext() throws Exception
    {
        /*
            Removing Non-Global:
            - Global exists -> no message required.
            - No global exists -> check for any issues in the context to be removed.
            Removing Global:
            - Other contexts -> check for any issues in global but not in remainder contexts.
            - No other contexts -> check for any issues.
         */
        administration.restoreBlankInstance();
        administration.project().addProject("third project", "THIRD", ADMIN_USERNAME);

        // create custom field for Monkey,Bug context
        String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF", new String[] {"1"}, new String[] {"10001"});
        String numericCustomFieldId = customFieldId.split("_")[1];

        // add global context
        String schemeId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Global Context", new String[] { }, new String[] {  });

        // create issue in Monkey,Bug context
        String issue1 = navigation.issue().createIssue("monkey", "Bug", "Test");

        administration.reIndex();

        // remove Monkey,Bug context
        administration.customFields().removeConfigurationSchemeContextByLabel(numericCustomFieldId, "Default Configuration Scheme for UserCF");

        // no message
        assertNoCustomFieldMessage();

        // cleanup, remove global and reset Monkey,Bug context
        administration.customFields().removeConfigurationSchemeContextById(numericCustomFieldId, schemeId);
        schemeId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Default Configuration Scheme for UserCF", new String[] {"1"}, new String[] {"10001"});
        administration.reIndex();

        // remove Monkey,Bug context
        administration.customFields().removeConfigurationSchemeContextById(numericCustomFieldId, schemeId);

        // message (because of issue)
        assertCustomFieldMessage();

        // cleanup and re-add Monkey,Bug context
        navigation.issue().deleteIssue(issue1);
        schemeId = administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Default Configuration Scheme for UserCF", new String[] {"1"}, new String[] {"10001"});
        administration.reIndex();

        // remove Monkey,Bug context
        administration.customFields().removeConfigurationSchemeContextById(numericCustomFieldId, schemeId);

        // no message (because no issue)
        assertNoCustomFieldMessage();

        // cleanup
        administration.customFields().removeCustomField(customFieldId);
        administration.reIndex();

        // create custom field with Global context
        customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF");
        numericCustomFieldId = customFieldId.split("_")[1];
        administration.reIndex();

        // remove Global context
        administration.customFields().removeGlobalContext(numericCustomFieldId);

        // no message (because no issues)
        assertNoCustomFieldMessage();

        // re-add global context
        administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Global Context", new String[] { }, new String[] {  });

        // add issue to some some context
        issue1 = navigation.issue().createIssue("monkey", "Bug", "Test");
        administration.reIndex();

        // remove global context
        administration.customFields().removeGlobalContext(numericCustomFieldId);

        // message (because issue exists)
        assertCustomFieldMessage();

        // cleanup, re-add global + another context
        navigation.issue().deleteIssue(issue1);
        administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Global Context", new String[] { }, new String[] {  });
        administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "BugMonkey Context", new String[] { "1" }, new String[] { "10001" });
        administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "ImprovementHomosapien Context", new String[] { "4" }, new String[] { "10000" });
        administration.reIndex();

        // remove global context
        administration.customFields().removeGlobalContext(numericCustomFieldId);

        // no message (because no issues outside of other context)
        assertNoCustomFieldMessage();

        // clean up & re-add global context
        administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Global Context", new String[] { }, new String[] {  });
        administration.reIndex();

        // add issue in BugMonkey context
        navigation.issue().createIssue("monkey", "Bug", "Test");

        // remove global context
        administration.customFields().removeGlobalContext(numericCustomFieldId);

        // no message (because no issues outside of other context)
        assertNoCustomFieldMessage();

        // clean up & re-add global context
        administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Global Context", new String[] { }, new String[] {  });
        administration.reIndex();

        // add issue in ImprovementHomosapien context
        navigation.issue().createIssue("homosapien", "Improvement", "Test");

        // remove global context
        administration.customFields().removeGlobalContext(numericCustomFieldId);

        // no message (because no issues outside of other context)
        assertNoCustomFieldMessage();

        // clean up & re-add global context
        administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "Global Context", new String[] { }, new String[] {  });
        administration.reIndex();

        // add issue in HomosapienBug context
        navigation.issue().createIssue("homosapien", "Bug", "Test");

        // remove global context
        administration.customFields().removeGlobalContext(numericCustomFieldId);

        // message (because issue was not covered by existing contexts)
        assertCustomFieldMessage();
    }

    public void testShowHideFieldsInFieldConfiguration1() throws Exception
    {
        administration.restoreData("TestShowHideFieldsInFieldConfiguration.xml");

        // show/hide a field in each field configuration when no issues are present
        _testShowHideFieldInFieldConfigurationNoEffect("Default Field Configuration");
        _testShowHideFieldInFieldConfigurationNoEffect("Field Config 1");
        _testShowHideFieldInFieldConfigurationNoEffect("Field Config 2");

        // add issue for each project
        navigation.issue().createIssue("homosapien", "Bug", "Test");
        navigation.issue().createIssue("monkey", "Bug", "Test");

        _testShowHideFieldInFieldConfigurationNoEffect("Default Field Configuration");
        _testShowHideFieldInFieldConfigurationHasEffect("Field Config 1");
        _testShowHideFieldInFieldConfigurationHasEffect("Field Config 2");
    }

    public void testShowHideFieldsInFieldConfiguration2() throws Exception
    {
        administration.restoreData("TestShowHideFieldsInFieldConfiguration.xml");

        // add issue for each project
        navigation.issue().createIssue("homosapien", "Bug", "Test");
        navigation.issue().createIssue("monkey", "Bug", "Test");

        // associate default scheme with a project and try again
        administration.project().associateFieldConfigurationScheme("homosapien", null);
        administration.reIndex();

        _testShowHideFieldInFieldConfigurationHasEffect("Default Field Configuration");
        _testShowHideFieldInFieldConfigurationNoEffect("Field Config 1");
        _testShowHideFieldInFieldConfigurationHasEffect("Field Config 2");
    }

    public void testShowHideFieldsInFieldConfiguration3() throws Exception
    {
        administration.restoreData("TestShowHideFieldsInFieldConfiguration.xml");

        // add issue for each project
        navigation.issue().createIssue("homosapien", "Bug", "Test");
        navigation.issue().createIssue("monkey", "Bug", "Test");

        // These tests were added for JRA-18160
        // Now associate a custom scheme that uses the Default Field Configuration for unmapped issue types.
        administration.project().associateFieldConfigurationScheme("homosapien", "Scheme X");
        administration.project().associateFieldConfigurationScheme("monkey", "Scheme X");
        administration.reIndex();

        _testShowHideFieldInFieldConfigurationHasEffect("Default Field Configuration");
        _testShowHideFieldInFieldConfigurationHasEffect("Field Config 1");
        _testShowHideFieldInFieldConfigurationNoEffect("Field Config 2");
    }

    public void testShowHideFieldsInFieldConfiguration4() throws Exception
    {
        administration.restoreData("TestShowHideFieldsInFieldConfiguration.xml");

        // add issue for each project
        navigation.issue().createIssue("homosapien", "Bug", "Test");
        navigation.issue().createIssue("monkey", "Bug", "Test");

        // Now associate a custom scheme that uses the Default Field Configuration for pariticular Issue Type.
        administration.project().associateFieldConfigurationScheme("homosapien", "Scheme Y");
        administration.project().associateFieldConfigurationScheme("monkey", "Scheme Y");
        administration.reIndex();

        _testShowHideFieldInFieldConfigurationHasEffect("Default Field Configuration");
        _testShowHideFieldInFieldConfigurationNoEffect("Field Config 1");
        _testShowHideFieldInFieldConfigurationHasEffect("Field Config 2");
    }

    public void testAddFieldConfigurationAssociationToScheme() throws Exception
    {
        administration.restoreData("TestAddFieldConfigurationAssociationToScheme.xml");

        administration.project().addProject("third project", "THIRD", ADMIN_USERNAME);

        // non-associated scheme should not generate messages
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 2").addAssociation("1", "Field Config 1");
        assertNoFieldConfigurationMessage();

        // check adding different configs with no issues
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("1", "Field Config 1");
        assertNoFieldConfigurationMessage();
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("3", "Field Config 2");
        assertNoFieldConfigurationMessage();

        // cleanup added associations
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").removeAssociation("1");
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").removeAssociation("3");
        administration.reIndex();

        // add issue in project which doesnt use scheme
        navigation.issue().createIssue("third project", "Bug", "Test");

        // add association that is equivalent
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("1", "Default Field Configuration");

        // no message
        assertNoFieldConfigurationMessage();

        // add association that is not equivalent
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("3", "Field Config 1");

        // no message
        assertNoFieldConfigurationMessage();

        // add issue in project which uses scheme
        navigation.issue().createIssue("homosapien", "Improvement", "Test");

        // add association for issue type with issue
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("4", "Field Config 2");

        // message
        assertFieldConfigurationMessage();
    }

    public void testEditFieldConfigurationAssociationInScheme() throws Exception
    {
        administration.restoreData("TestEditFieldConfigurationAssociationToScheme.xml");

        administration.project().addProject("third project", "THIRD", ADMIN_USERNAME);

        // non-associated scheme should not generate messages
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 2").editAssociation("1", "Field Config 2");
        assertNoFieldConfigurationMessage();

        // check editing different configs with no issues
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("1", "Field Config 1");
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("3", "Field Config 2");
        administration.reIndex();
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").editAssociation("1", "Field Config 2");
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").editAssociation("3", "Field Config 1");
        assertNoFieldConfigurationMessage();

        // cleanup added associations
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").removeAssociation("1");
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").removeAssociation("3");
        administration.reIndex();

        // add issue in project not using scheme
        navigation.issue().createIssue("third project", "Bug", "Test");

        // edit to association that is equivalent
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").editAssociation(null, "Copy of Default Field Configuration");

        // no message
        assertNoFieldConfigurationMessage();

        // add association that is not equivalent, then edit it
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("3", "Field Config 1");
        administration.reIndex();
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").editAssociation("3", "Field Config 2");

        // no message
        assertNoFieldConfigurationMessage();

        // add issue in project using scheme
        navigation.issue().createIssue("homosapien", "Improvement", "Test");

        // add association for issue type with issue, then edit it
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("4", "Field Config 2");
        administration.reIndex();
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").editAssociation("4", "Default Field Configuration");

        // message
        assertFieldConfigurationMessage();

        // cleanup
        administration.reIndex();

        // edit the association to be equivalent (but with issues)
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").editAssociation("4", "Copy of Default Field Configuration");

        // no message
        assertNoFieldConfigurationMessage();
    }

    public void testRemoveFieldConfigurationAssociationFromScheme() throws Exception
    {
        administration.restoreData("TestEditFieldConfigurationAssociationToScheme.xml");

        // non-associated scheme should not generate messages
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 2").removeAssociation("1");
        assertNoFieldConfigurationMessage();

        // check removing different configs with no issues
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("1", "Field Config 1");
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("3", "Field Config 2");
        administration.reIndex();
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").removeAssociation("1");
        assertNoFieldConfigurationMessage();
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").removeAssociation("3");
        assertNoFieldConfigurationMessage();

        // cleanup & re-add association that is equivalent to default
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("1", "Copy of Default Field Configuration");
        administration.reIndex();

        // add issue
        navigation.issue().createIssue("homosapien", "Bug", "Test");

        // remove association that is equivalent
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").removeAssociation("1");

        // no message
        assertNoFieldConfigurationMessage();

        // add association that is different, then remove it
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").addAssociation("1", "Field Config 2");
        administration.reIndex();
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Scheme 1").removeAssociation("1");

        // message
        assertFieldConfigurationMessage();
    }

    //#JRA-20533
    public void testProjectAdminCannotSee() throws Exception
    {
        administration.restoreBlankInstance();
        administration.roles().addProjectRoleForUser(PROJECT_MONKEY, "Administrators",FRED_USERNAME);
        administration.generalConfiguration().disableVoting();
        assertNoVotingMessage();
        administration.generalConfiguration().enableVoting();
        assertVotingMessage();
        navigation.login(FRED_USERNAME);
        navigation.gotoAdmin();
        assertNoVotingMessage();
    }

    @SystemTenantOnly
    public void testEnableDisablePlugins() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();
        navigation.gotoAdminSection("upm-admin-link");

        // disabling any plugin should not generate a message
        administration.plugins().disablePluginModule("com.atlassian.jira.plugin.system.issueoperations", "com.atlassian.jira.plugin.system.issueoperations:log-work");
        assertNoPluginsMessage();
        administration.plugins().disablePluginModule("com.atlassian.jira.plugin.system.customfieldtypes", "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect");
        assertNoPluginsMessage();
        administration.plugins().disablePluginModule("com.atlassian.jira.plugin.system.customfieldtypes", "com.atlassian.jira.plugin.system.customfieldtypes:numberrange");
        assertNoPluginsMessage();

        // enabling a plugin which is not a custom field type or searcher should have no impact
        administration.plugins().enablePluginModule("com.atlassian.jira.plugin.system.issueoperations", "com.atlassian.jira.plugin.system.issueoperations:log-work");
        assertNoPluginsMessage();

        // enabling a plugin which is a custom field type or searcher should generate a message
        administration.plugins().enablePluginModule("com.atlassian.jira.plugin.system.customfieldtypes", "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect");
        assertPluginsMessage();
        administration.reIndex();

        administration.plugins().enablePluginModule("com.atlassian.jira.plugin.system.customfieldtypes", "com.atlassian.jira.plugin.system.customfieldtypes:numberrange");
        assertPluginsMessage();
        administration.reIndex();

        // disabling an entire plugin should not generate a message
        administration.plugins().disablePlugin("com.atlassian.jira.plugin.system.issueoperations");
        assertNoPluginsMessage();
        administration.plugins().disablePlugin("com.atlassian.jira.plugin.system.customfieldtypes");
        assertNoPluginsMessage();

        // enabling an entire plugin should generate a message if it contains custom field types or searchers
        administration.plugins().enablePlugin("com.atlassian.jira.plugin.system.issueoperations");
        assertNoPluginsMessage();
        administration.plugins().enablePlugin("com.atlassian.jira.plugin.system.customfieldtypes");
        assertPluginsMessage();
    }

    private void _testShowHideFieldInFieldConfigurationNoEffect(final String fieldConfigurationName)
    {
        administration.fieldConfigurations().fieldConfiguration(fieldConfigurationName).hideFields("Affects Version/s");
        assertNoFieldConfigurationMessage();
        administration.fieldConfigurations().fieldConfiguration(fieldConfigurationName).showFields("Affects Version/s");
        assertNoFieldConfigurationMessage();
    }

    private void _testShowHideFieldInFieldConfigurationHasEffect(final String fieldConfigurationName)
    {
        administration.fieldConfigurations().fieldConfiguration(fieldConfigurationName).hideFields("Affects Version/s");
        assertFieldConfigurationMessage();
        administration.reIndex();
        administration.fieldConfigurations().fieldConfiguration(fieldConfigurationName).showFields("Affects Version/s");
        assertFieldConfigurationMessage();
        administration.reIndex();
    }

    private void _testAddCustomFieldGlobalContext()
    {
        // create custom field
        String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF");

        // no message
        assertNoCustomFieldMessage();

        // create issue in context
        String issue = navigation.issue().createIssue("monkey", "Bug", "Test");

        // re-add custom field
        administration.customFields().removeCustomField(customFieldId);
        customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF");

        // message
        assertCustomFieldMessage();

        // clean up
        navigation.issue().deleteIssue(issue);
        administration.customFields().removeCustomField(customFieldId);
        administration.reIndex();
    }

    private void _testAddCustomFieldSpecificContext(final String[] projects, final String[] issueTypes, final String projectForIssue, final String issueTypeForIssue)
    {
        // create custom field
        String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF", issueTypes, projects);

        // no message
        assertNoCustomFieldMessage();

        // create issue not in context
        String issue1 = navigation.issue().createIssue("third project", "Task", "Test");

        // re-add custom field
        administration.customFields().removeCustomField(customFieldId);
        customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF", issueTypes, projects);

        // no message
        assertNoCustomFieldMessage();

        // create issue in context
        String issue2 = navigation.issue().createIssue(projectForIssue, issueTypeForIssue, "Test");

        // re-add custom field
        administration.customFields().removeCustomField(customFieldId);
        customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF", issueTypes, projects);

        // message
        assertCustomFieldMessage();

        // clean up
        navigation.issue().deleteIssue(issue1);
        navigation.issue().deleteIssue(issue2);
        administration.customFields().removeCustomField(customFieldId);
        administration.reIndex();
    }

    private void changeFieldConfigSchemeAndAssertMessage(final String previousFieldConfigurationSchemeName, final String newFieldConfigurationSchemeName, final boolean shouldMessageBeDisplayed)
    {
        administration.project().associateFieldConfigurationScheme("monkey", newFieldConfigurationSchemeName);
        if (shouldMessageBeDisplayed)
        {
            // message will not be displayed if there are no issues
            assertNoFieldConfigurationMessage();

            // reset old scheme
            administration.project().associateFieldConfigurationScheme("monkey", previousFieldConfigurationSchemeName);
            administration.reIndex();

            // add issue
            String issue = navigation.issue().createIssue("monkey", "Bug", "Test");

            // change will now generate message
            administration.project().associateFieldConfigurationScheme("monkey", newFieldConfigurationSchemeName);
            assertFieldConfigurationMessage();

            // delete issue
            navigation.issue().deleteIssue(issue);

            // clear the message
            administration.reIndex();
        }
        else
        {
            assertNoFieldConfigurationMessage();
        }
    }

    private void assertVotingMessage()
    {
        assertMessage("Voting");
    }

    private void assertNoVotingMessage()
    {
        assertNoMessage("Voting");
    }

    private void assertTimeTrackingMessage()
    {
        assertMessage("Time Tracking");
    }

    private void assertNoTimeTrackingMessage()
    {
        assertNoMessage("Time Tracking");
    }

    private void assertFieldConfigurationMessage()
    {
        assertMessage("Field Configuration");
    }

    private void assertNoFieldConfigurationMessage()
    {
        assertNoMessage("Field Configuration");
    }

    private void assertCustomFieldMessage()
    {
        assertMessage("Custom Fields");
    }

    private void assertNoCustomFieldMessage()
    {
        assertNoMessage("Custom Fields");
    }

    private void assertPluginsMessage()
    {
        assertMessage("Plugins");
    }

    private void assertNoPluginsMessage()
    {
        assertNoMessage("Plugins");
    }

    private void assertMessage(final String section)
    {
        assertions.getTextAssertions().assertTextPresent(new CssLocator(tester, ".aui-message.info"),
                ADMIN_FULLNAME + " made configuration changes in section '" + section + "'");
    }

    private void assertNoMessage(final String section)
    {
        assertions.getTextAssertions().assertTextNotPresent(new CssLocator(tester, ".aui-message.info"),
                ADMIN_FULLNAME + " made configuration changes in section '" + section + "'");
    }
}
