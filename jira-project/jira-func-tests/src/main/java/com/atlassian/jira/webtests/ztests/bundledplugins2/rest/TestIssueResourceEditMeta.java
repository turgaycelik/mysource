package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.testkit.client.restclient.BeanClient;
import com.atlassian.jira.testkit.client.restclient.FieldMetaData;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueBean;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.IssuePickerBean;
import com.atlassian.jira.testkit.client.restclient.LabelSuggestionsBean;
import com.atlassian.jira.testkit.client.restclient.SectionBean;
import com.atlassian.jira.testkit.client.restclient.User;
import com.atlassian.jira.testkit.client.restclient.UserPickerResultBean;
import org.apache.commons.lang.StringUtils;

import java.util.EnumSet;
import java.util.List;

/**
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceEditMeta extends RestFuncTest
{
    private IssueClient issueClient;
    private BeanClient beanClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        beanClient = new BeanClient(getEnvironmentData());
    }

    private void importData()
    {
        administration.restoreData("TestIssueResourceEditMeta.xml");
    }

    public void testEditmetaViaExpand() throws Exception
    {
        importData();

        Issue issue = issueClient.get("TST-1");
        assertNull(issue.editmeta);

        issue = issueClient.get("TST-1", Issue.Expand.editmeta);
        assertNotNull(issue.editmeta);
        assertTrue(issue.editmeta.fields.containsKey("summary"));
        assertTrue(issue.editmeta.fields.containsKey("description"));
        assertTrue(issue.editmeta.fields.containsKey("timetracking"));
        assertTrue(issue.editmeta.fields.containsKey("issuetype"));
        assertTrue(issue.editmeta.fields.containsKey("labels"));
        assertTrue(issue.editmeta.fields.containsKey("assignee"));
        assertTrue(issue.editmeta.fields.containsKey("security"));
        assertTrue(issue.editmeta.fields.containsKey("resolution"));
        assertTrue(issue.editmeta.fields.containsKey("attachment"));
        assertTrue(issue.editmeta.fields.containsKey("comment"));
        assertTrue(issue.editmeta.fields.containsKey("worklog"));
        // etc

        issue = issueClient.getPartially("TST-1", EnumSet.of(Issue.Expand.editmeta), StringList.fromList("summary", "description"));
        assertNotNull(issue.editmeta);
        assertTrue(issue.editmeta.fields.containsKey("summary"));
        assertTrue(issue.editmeta.fields.containsKey("description"));
        assertEquals(2, issue.editmeta.fields.size());
    }

    public void testEditMetaAutoCompleteUrls() throws Exception
    {
        importData();

        Issue issue = issueClient.get("TST-1");
        assertNull(issue.editmeta);

        issue = issueClient.get("TST-1", Issue.Expand.editmeta);
        assertNotNull(issue.editmeta);
        FieldMetaData assignee = issue.editmeta.fields.get("assignee");
        assertTrue(StringUtils.isNotBlank(assignee.autoCompleteUrl));
        List<User> assigneeSuggestions = beanClient.getUsersFromUrl(assignee.autoCompleteUrl + "a");
        assertEquals(1, assigneeSuggestions.size());
        assertEquals("admin", assigneeSuggestions.get(0).displayName);

        FieldMetaData reporter = issue.editmeta.fields.get("reporter");
        assertTrue(StringUtils.isNotBlank(reporter.autoCompleteUrl));
        List<User> reporterSuggestions = beanClient.getUsersFromUrl(reporter.autoCompleteUrl + "a");
        assertEquals(1, reporterSuggestions.size());
        assertEquals("admin", reporterSuggestions.get(0).displayName);

        FieldMetaData issueLinks = issue.editmeta.fields.get("issuelinks");
        assertTrue(StringUtils.isNotBlank(issueLinks.autoCompleteUrl));
        IssuePickerBean issuePickerBean = beanClient.getIssueSuggestionsFromUrl(issueLinks.autoCompleteUrl + "TS");
        assertEquals(2, issuePickerBean.sections.get(0).issues.size());
        SectionBean sectionBean = issuePickerBean.sections.get(0);
        assertTrue(containsBeanWithKey(sectionBean.issues, "TST-2"));
        assertTrue(containsBeanWithKey(sectionBean.issues, "TST-3"));

        FieldMetaData labels = issue.editmeta.fields.get("labels");
        assertTrue(StringUtils.isNotBlank(labels.autoCompleteUrl));
        LabelSuggestionsBean labelSuggestionBean = beanClient.getLabelSuggestionsFromUrl(labels.autoCompleteUrl + "b");
        assertEquals(2, labelSuggestionBean.suggestions.size());
        assertEquals("bar", labelSuggestionBean.suggestions.get(0).label);
        assertEquals("bob", labelSuggestionBean.suggestions.get(1).label);

        FieldMetaData userPickerCF = issue.editmeta.fields.get("customfield_10010");
        assertTrue(StringUtils.isNotBlank(userPickerCF.autoCompleteUrl));
        UserPickerResultBean userPickerResultBean = beanClient.getUserPickResultsFromUrl(userPickerCF.autoCompleteUrl + "a");
        assertEquals(1, userPickerResultBean.users.size());
        assertEquals("admin", userPickerResultBean.users.get(0).name);

        FieldMetaData multiUserPickerCF = issue.editmeta.fields.get("customfield_10110");
        assertTrue(StringUtils.isNotBlank(multiUserPickerCF.autoCompleteUrl));
        UserPickerResultBean multiUserPickerResultBean = beanClient.getUserPickResultsFromUrl(multiUserPickerCF.autoCompleteUrl + "a");
        assertEquals(1, multiUserPickerResultBean.users.size());
        assertEquals("admin", multiUserPickerResultBean.users.get(0).name);

        FieldMetaData labelCF = issue.editmeta.fields.get("customfield_10210");
        assertTrue(StringUtils.isNotBlank(labelCF.autoCompleteUrl));
        LabelSuggestionsBean labelSuggestionsBean = beanClient.getLabelSuggestionsFromUrl(labelCF.autoCompleteUrl + "b");
        assertEquals(1, labelSuggestionsBean.suggestions.size());
        assertEquals("bob", labelSuggestionsBean.suggestions.get(0).label);


    }

    public void testFieldsFilteredByPermissions()
    {
        importData();

        Issue issue = issueClient.get("TST-1");
        assertNull(issue.editmeta);

        issue = issueClient.loginAs("fry").get("TST-1", Issue.Expand.editmeta);
        assertNotNull(issue.editmeta);
        assertTrue(issue.editmeta.fields.containsKey("summary"));
        assertTrue(issue.editmeta.fields.containsKey("description"));
        assertTrue(issue.editmeta.fields.containsKey("timetracking"));
        assertTrue(issue.editmeta.fields.containsKey("issuetype"));
        assertTrue(issue.editmeta.fields.containsKey("labels"));
        assertTrue(issue.editmeta.fields.containsKey("security"));
        assertTrue(issue.editmeta.fields.containsKey("resolution"));
        assertTrue(issue.editmeta.fields.containsKey("attachment"));
        assertFalse(issue.editmeta.fields.containsKey("comment"));
        assertFalse(issue.editmeta.fields.containsKey("worklog"));
        assertFalse(issue.editmeta.fields.containsKey("assignee"));

    }

    public void testNothingWhenNoEditPermissions()
    {
        importData();

        Issue issue = issueClient.loginAs("fry").get("PH-1", Issue.Expand.editmeta);
        assertNotNull(issue.editmeta);
        assertTrue(issue.editmeta.fields.isEmpty());
    }

    private boolean containsBeanWithKey(List<IssueBean> issueBeans, String key)
    {
        for (IssueBean issueBean : issueBeans)
        {
            if (issueBean.key.equals(key))
            {
                return true;
            }
        }
        return false;
    }
}
