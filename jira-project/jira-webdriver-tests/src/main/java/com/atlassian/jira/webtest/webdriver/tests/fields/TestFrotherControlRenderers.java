package com.atlassian.jira.webtest.webdriver.tests.fields;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.dialogs.JiraDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.CreateIssuePage;
import com.atlassian.jira.pageobjects.pages.admin.ViewFieldConfigurationsPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ActionTrigger;
import com.atlassian.jira.pageobjects.pages.viewissue.ResolveIssueDialog;
import com.atlassian.jira.webtest.webdriver.selenium.PseudoAssertThat;
import com.atlassian.jira.webtest.webdriver.selenium.PseudoSeleniumClient;
import com.atlassian.jira.webtest.webdriver.selenium.WebDriverNavigator;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.webtest.webdriver.selenium.PseudoAssertThat.DROP_DOWN_WAIT;

/**
 * JRADEV-2828: Tests that frother controls can be enabled & disabled for all components & version fields.
 */
@WebTest({Category.WEBDRIVER_TEST })
@Ignore("JRADEV-18417, JRADEV-18418 - Tests are incredibly slow and need to be revised for performance.")
public class TestFrotherControlRenderers extends BaseJiraWebTest
{
    public static final List<Field> ALL_FIELDS = Arrays.asList(Field.AFFECTS_VERSIONS, Field.COMPONENTS, Field.LABELS, Field.FIX_VERSIONS, Field.SINGLE_VERSION_CF, Field.MULTI_VERSION_CF);
    public static final List<Field> TOGGLEABLE_FIELDS = Arrays.asList(Field.AFFECTS_VERSIONS, Field.COMPONENTS, Field.FIX_VERSIONS, Field.MULTI_VERSION_CF);
    private static final String MULTI_VERSION_CF_ID = "customfield_10030";
    private static final String SINGLE_VERSION_CF_ID = "customfield_10031";

    private static class Field
    {
        static Field AFFECTS_VERSIONS = new Field("versions", true);
        static Field COMPONENTS = new Field("components", true);
        static Field LABELS = new Field("labels", true);
        static Field FIX_VERSIONS = new Field("fixVersions", true);

        static Field SINGLE_VERSION_CF = new Field(SINGLE_VERSION_CF_ID, false);
        static Field MULTI_VERSION_CF = new Field(MULTI_VERSION_CF_ID, true);

        public String fieldId;
        public boolean isFrotherControl;

        private Field(final String fieldId, final boolean frotherControl)
        {
            this.fieldId = fieldId;
            this.isFrotherControl = frotherControl;
        }
    }

    @Inject
    private AtlassianWebDriver driver;
    @Inject
    private PageElementFinder pageElementFinder;

    private PseudoSeleniumClient client;
    private PseudoAssertThat assertThat;
    private WebDriverNavigator navigator;

    @Before
    public void onSetUp()
    {
        client = new PseudoSeleniumClient(jira, pageElementFinder);
        assertThat = new PseudoAssertThat(driver);
        navigator = new WebDriverNavigator(jira, pageElementFinder);
        backdoor.restoreData("TestFrotherControlRenderers.xml");
        jira.quickLoginAsSysadmin();

    }

    @After
    public void tearDown() throws Exception
    {
        //reset all fields to default value
        for (Field field : TOGGLEABLE_FIELDS)
        {
            backdoor.fieldConfiguration().setFieldRenderer("Default Field Configuration", field.fieldId, "Autocomplete Renderer");
            field.isFrotherControl = true;
        }
    }

    public WebDriverNavigator getNavigator() {
        return navigator;
    }

    @Test
    public void testDefaultRenderersState(){
        //the data has them set as autocomplete so check first if the frother control renders in all the right places
        assertFieldRendererStates(ALL_FIELDS);
    }
    @Test
    public void testSwitchRenderersDisableFrother()
    {
        for (Field field : TOGGLEABLE_FIELDS)
        {
            backdoor.fieldConfiguration().setFieldRenderer("Default Field Configuration", field.fieldId, "Select List Renderer");
            field.isFrotherControl = false;
        }
        assertFieldRendererStates(ALL_FIELDS);
    }
    @Test
    public void testSwitchRenderersEnableFrother()
    {
        for (Field field : TOGGLEABLE_FIELDS)
        {
            backdoor.fieldConfiguration().setFieldRenderer("Default Field Configuration", field.fieldId, "Autocomplete Renderer");
            field.isFrotherControl = true;
        }
        assertFieldRendererStates(ALL_FIELDS);
    }

    //unfortunately we have 2 jsps rendering the default field config and custom field configs so we
    //just neet to make sure that the custom one shows renderers as well.
    @Test
    public void testCustomFieldConfigAlsoShowsRenderers()
    {
        jira.visit(ViewFieldConfigurationsPage.class).openAddFieldConfigurationDialog().setName("Some random field configuration").submitSuccess();

        assertThat.textPresent("Autocomplete Renderer");
    }

    private void assertFieldRendererStates(final List<Field> allFields)
    {
        //check edit issue
        getNavigator().issue().editIssue("MKY-1");
        assertFrotherControlEnabledForAllFields(allFields);

        //check bulk edit
        jira.visit(AdvancedSearch.class).enterQuery("project=MKY").submit().toolsMenu().open().bulkChange().selectAllIssues().chooseOperation().editIssues();
        assertFrotherControlEnabledForAllFields(allFields);

        //check bulk transition
        jira.visit(AdvancedSearch.class).enterQuery("project=MKY").submit().toolsMenu().open().bulkChange().selectAllIssues().chooseOperation().transitionIssues();
        client.check("name=wftransition", "jira_5_5");
        client.click("id=Next", true);
        assertFrotherControlEnabledForAllFields(allFields);

        //check resolve screen in dialog
        ResolveIssueDialog dialog = getNavigator().issue().viewIssue("MKY-1").openResolveIssue(ActionTrigger.MENU);
        assertFrotherControlEnabledForAllFields(allFields, JiraDialog.DIALOG_CLASS);
        dialog.close();

        //check create issue
        CreateIssuePage createIssuePage = jira.visit(CreateIssuePage.class);
        createIssuePage.getProjectQuickSearch().clearQuery().query("monkey").getActiveSuggestion().click();
        createIssuePage.getIssueTypeQuickSearch().clearQuery().query("Bug").getActiveSuggestion().click();
        createIssuePage.submit();
        assertFrotherControlEnabledForAllFields(allFields);

        //The searchers for these fields should NEVER display the frother control!
        jira.visit(AdvancedSearch.class).enterQuery("project=MKY").submit().switchToSimple().expandAllNavigatorSections();
        assertFrotherControlNotShown(null, "searcher-fixfor");
        assertFrotherControlNotShown(null, "searcher-component");
        assertFrotherControlNotShown(null, "searcher-version");
        assertFrotherControlNotShown(null, "searcher-labels");
        assertFrotherControlNotShown(null, MULTI_VERSION_CF_ID);
        assertFrotherControlNotShown(null, SINGLE_VERSION_CF_ID);
    }

    private void assertFrotherControlEnabledForAllFields(List<Field> fields)
    {
        assertFrotherControlEnabledForAllFields(fields, null);
    }

    private void assertFrotherControlEnabledForAllFields(List<Field> fields, String domContext)
    {
        for (Field field : fields)
        {
            if (field.isFrotherControl)
            {
                assertFrotherControlShown(domContext, field.fieldId);
            }
            else
            {
                assertFrotherControlNotShown(domContext, field.fieldId);
            }
        }
    }

    private void assertFrotherControlNotShown(String domContext, String fieldId)
    {
        final String selector = getSelector(domContext, fieldId);
        assertThat.elementNotPresentByTimeout(selector + "-textarea", DROP_DOWN_WAIT);
        assertThat.visibleByTimeout(selector, DROP_DOWN_WAIT);
    }

    private void assertFrotherControlShown(String domContext, String fieldId)
    {
        final String selector = getSelector(domContext, fieldId);
        assertThat.elementPresentByTimeout(selector + "-textarea", DROP_DOWN_WAIT);
        assertThat.notVisibleByTimeout(selector, DROP_DOWN_WAIT);
    }

    private String getSelector(final String domContext, final String fieldId)
    {
        return domContext != null ? "jquery=" + domContext + " #" + fieldId : "jquery=#" + fieldId;
    }
}
