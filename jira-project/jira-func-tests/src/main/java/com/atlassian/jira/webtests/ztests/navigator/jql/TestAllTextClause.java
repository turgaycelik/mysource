package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests the "text" clause in JQL, which searches across all system text fields and custom free text fields that the user
 * can see.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestAllTextClause extends AbstractJqlFuncTest
{
    public void testCorrectness() throws Exception
    {
        administration.restoreData("TestAllTextClauseCorrectness.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        // no results to start out with
        assertSearchWithResults("text ~ something");

        final String issue1 = navigation.issue().createIssue("homosapien", "Bug", "this summary has something in it");
        final String issue2 = navigation.issue().createIssue("homosapien", "Bug", "this summary has bananas in it");
        final String issue3 = navigation.issue().createIssue("homosapien", "Bug", "this summary 3");
        final String issue4 = navigation.issue().createIssue("homosapien", "Bug", "this summary 4");
        assertSearchWithResults("text ~ something", issue1);

        // try some range queries
        assertSearchWithResults("text ~ \"[bananaa TO bananaz]\"", issue2);
        assertSearchWithResults("text ~ \"{ha TO haz}\"", issue2, issue1);

        // ensure it is and based searching
        assertSearchWithResults("text ~ \"summary something\"", issue1);
        assertSearchWithResults("text ~ \"bananas something\"");
        assertSearchWithResults("text ~ \"this summary has in it\"", issue2, issue1);

        navigation.issue().setDescription(issue1, "cheese");
        assertSearchWithResults("text ~ cheese", issue1);

        navigation.issue().setEnvironment(issue2, "toast");
        assertSearchWithResults("text ~ toast", issue2);

        navigation.issue().addComment(issue3, "monkey", null);
        assertSearchWithResults("text ~ monkey", issue3);

        // add a custom field with a free text searcher
        final String customFieldId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:textarea", "MyText");
        final String numericCustomFieldId = customFieldId.split("_", 2)[1];
        navigation.issue().setFreeTextCustomField(issue4, customFieldId, "gojira");
        assertSearchWithResults("text ~ gojira", issue4);

        // change custom field configuration so that it is only in project MKY, which we can't see
        administration.customFields().editConfigurationSchemeContextByLabel(numericCustomFieldId, "Default Configuration Scheme for MyText", null, null, new String[] {"10001"});
        assertSearchWithResults("text ~ gojira");

        // re-add a global context to the custom field
        administration.customFields().editConfigurationSchemeContextByLabel(numericCustomFieldId, "Default Configuration Scheme for MyText", null, new String[] {}, new String[] {});
        assertSearchWithResults("text ~ gojira", issue4);

        // add a URL field - check that it is not included
        final String urlCFId = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:url", "MyURL");
        navigation.issue().setFreeTextCustomField(issue4, urlCFId, "http://www.atlassian.com");
        assertSearchWithResults("text ~ 'http://www.atlassian.com'");

        // set search term on multiple issues to assert that merging results is done correctly
        navigation.issue().addComment(issue4, "something", null);
        assertSearchWithResults("text ~ something", issue4, issue1);

        navigation.issue().setDescription(issue2, "something");
        assertSearchWithResults("text ~ something", issue4, issue2, issue1);

        navigation.issue().setEnvironment(issue3, "something");
        assertSearchWithResults("text ~ something", issue4, issue3, issue2, issue1);

        // start hiding stuff

        // hide description in one field config, should still be searched
        administration.fieldConfigurations().fieldConfiguration("A Config").hideFields("Description");
        assertSearchWithResults("text ~ cheese", issue1);

        // hide description in second field config, should be ignored
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Description");
        assertSearchWithResults("text ~ cheese");

        // hide environment in one field config, should still be searched
        administration.fieldConfigurations().fieldConfiguration("A Config").hideFields("Environment");
        assertSearchWithResults("text ~ toast", issue2);

        // hide environment in second field config, should be ignored
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("Environment");
        assertSearchWithResults("text ~ toast");

        // hide MyText in one field config, should still be searched
        administration.fieldConfigurations().fieldConfiguration("A Config").hideFields("MyText");
        assertSearchWithResults("text ~ gojira", issue4);

        // hide MyText in second field config, should be ignored
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields("MyText");
        assertSearchWithResults("text ~ gojira");
    }

    public void testErrorMessagesWithInvalidRangeQuery() throws Exception
    {
        administration.restoreData("TestAllTextClauseCorrectness.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        // try some range queries
        assertSearchWithIncorrectRangeFreeTextJql("[wrong");
        assertSearchWithIncorrectRangeFreeTextJql("[wrong]");
        assertSearchWithIncorrectRangeFreeTextJql("[wrong TO]");
        assertSearchWithIncorrectRangeFreeTextJql("[TO wrong]");
        assertSearchWithIncorrectRangeFreeTextJql("[TO wrong");

        assertSearchWithIncorrectRangeFreeTextJql("{wrong");
        assertSearchWithIncorrectRangeFreeTextJql("{wrong}");
        assertSearchWithIncorrectRangeFreeTextJql("{wrong TO}");
        assertSearchWithIncorrectRangeFreeTextJql("{TO wrong}");
        assertSearchWithIncorrectRangeFreeTextJql("{TO wrong");

        assertSearchWithIncorrectRangeFreeTextJql("[wrong TO query}");
        assertSearchWithIncorrectRangeFreeTextJql("{wrong TO query]");
    }

    private void assertSearchWithIncorrectRangeFreeTextJql(final String rangeQuery) {
        assertSearchWithError(String.format("text ~ \"%s\"", rangeQuery),
                String.format("The text query '%s' for field 'text' is not valid: probably your range query is incorrect.", rangeQuery));
    }
}
