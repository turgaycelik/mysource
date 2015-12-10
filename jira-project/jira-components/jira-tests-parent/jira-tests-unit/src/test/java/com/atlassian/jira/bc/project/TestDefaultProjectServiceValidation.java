package com.atlassian.jira.bc.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.ClearStatics;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestDefaultProjectServiceValidation
{
    @Rule
    public TestRule initMocks = MockitoMocksInContainer.forTest(this);
    @Rule
    public ClearStatics clearStatics = new ClearStatics();
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private ProjectKeyStore projectKeyStore;
    @Mock
    private ProjectManager projectManager;
    @Mock
    @AvailableInContainer
    private JiraProperties jiraProperties;
    @AvailableInContainer
    private final I18nHelper.BeanFactory i18nFactory = new NoopI18nFactory();
    @AvailableInContainer
    private final ApplicationProperties applicationProperties = new MockApplicationProperties();
    private ProjectService projectService;
    private ErrorCollection errorCollection;
    private static final String TEST_USER = "test user";
    private static final Long ASSIGNEETYPE_PROJECTLEAD = AssigneeTypes.PROJECT_LEAD;
    private JiraServiceContext serviceContext;

    @Before
    public void setUp() throws Exception
    {
        final MockUserManager userManager = new MockUserManager();
        final User user = new MockUser(TEST_USER);
        userManager.addUser(user);
        Mockito.when(projectKeyStore.getProjectId(Mockito.anyString())).thenReturn(null);
        projectService = new DefaultProjectService(jiraAuthenticationContext, projectManager, applicationProperties, null, null, null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null, userManager, null, projectKeyStore);
        Mockito.when(jiraProperties.getProperty("os.name")).thenReturn("other");
        errorCollection = new SimpleErrorCollection();
        serviceContext = constructServiceContext(errorCollection);
    }

    @Test
    public void testIsValidRequiredProjectDataHappyPath()
    {
        assertTrue("Expected validation to pass", projectService.isValidRequiredProjectData(serviceContext, "project", "PRJ", TEST_USER));
        assertFalse("Expected not errors", errorCollection.hasAnyErrors());
    }

    @Test
    public void testIsValidRequiredProjectDataNameEmpty()
    {
        assertFalse(projectService.isValidRequiredProjectData(serviceContext, "", "PRJ", TEST_USER));
        assertErrorPresent("projectName", "admin.errors.must.specify.a.valid.project.name{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataNullName()
    {
        assertFalse(projectService.isValidRequiredProjectData(serviceContext, null, "PRJ", TEST_USER));
        assertErrorPresent("projectName", "admin.errors.must.specify.a.valid.project.name{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataDuplicateName()
    {
        Mockito.when(projectManager.getProjectObjByName("project")).thenReturn(new MockProject());

        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", "PRJ", TEST_USER));
        assertErrorPresent("projectName", "admin.errors.project.with.that.name.already.exists{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataKeyIsEmptyString()
    {
        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", "", TEST_USER));
        assertErrorPresent("projectKey", "admin.errors.must.specify.unique.project.key{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataKeyIsNull()
    {
        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", null, TEST_USER));
        assertErrorPresent("projectKey", "admin.errors.must.specify.unique.project.key{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataInvalidKeyDefaultPattern()
    {
        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", "!NV4L!D K3Y", TEST_USER));
        assertErrorPresent("projectKey", "admin.errors.must.specify.unique.project.key{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataInvalidKeyCustomPattern()
    {
        applicationProperties.setText(APKeys.JIRA_PROJECTKEY_WARNING, "Test Project Key Regex Warning Message");
        applicationProperties.setText(APKeys.JIRA_PROJECTKEY_PATTERN, "wrong");

        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", "AA", TEST_USER));
        assertErrorPresent("projectKey", "Test Project Key Regex Warning Message{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataInvalidKeyReservedWord()
    {

        Mockito.when(jiraProperties.getProperty("os.name")).thenReturn("windows");
        applicationProperties.setString(APKeys.JIRA_PROJECTKEY_RESERVEDWORDS_LIST, "CON CORD");
        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", "CON", TEST_USER));
        assertErrorPresent("projectKey", "admin.errors.project.keyword.invalid{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataDuplicateKey()
    {
        Mockito.when(projectManager.getProjectObjByKey("PRJ")).thenReturn(new MockProject());
        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", "PRJ", TEST_USER));
        assertErrorPresent("projectKey", "admin.errors.project.with.that.key.already.exists{[null]}");
    }

    @Test
    public void testIsValidRequiredProjectDataNoLeadEmpty()
    {
        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", "PRJ", ""));
        assertErrorPresent("projectLead", "admin.errors.must.specify.project.lead{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataNoLeadEmptyNull()
    {
        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", "PRJ", null));
        assertErrorPresent("projectLead", "admin.errors.must.specify.project.lead{[]}");
    }

    @Test
    public void testIsValidRequiredProjectDataNonExistentLead()
    {
        assertFalse("Expected validation to fail", projectService.isValidRequiredProjectData(serviceContext, "project", "PRJ", "stranger"));
        assertErrorPresent("projectLead", "admin.errors.not.a.valid.user{[]}");
    }

    @Test
    public void testIsValidAllProjectDataHappyPath()
    {
        assertTrue("Expected validation to pass", projectService.isValidAllProjectData(serviceContext, "project", "PRJ", TEST_USER, "http://www.example.com", ASSIGNEETYPE_PROJECTLEAD));
        assertFalse("Unexpected errors", errorCollection.hasAnyErrors());
    }

    @Test
    public void testIsValidAllProjectDataHappyPathNonRequiredFieldsLeftBlank()
    {
        assertTrue("Expected validation to pass", projectService.isValidAllProjectData(serviceContext, "project", "PRJ", TEST_USER, null, null));
        assertFalse("Unexpected errors", errorCollection.hasAnyErrors());
    }

    @Test
    public void testIsValidAllProjectDataInvalidURL()
    {
        assertFalse("Expected validation to fail", projectService.isValidAllProjectData(serviceContext, "project", "PRJ", TEST_USER, "not - a - valid - url", null));
        assertErrorPresent("projectUrl", "admin.errors.url.specified.is.not.valid{[]}");
    }

    @Test
    public void testIsValidAllProjectDataInvalidAssigneeType()
    {
        assertFalse(projectService.isValidAllProjectData(serviceContext, "project", "PRJ", TEST_USER, null, 500l));
        assertErrorMessage("admin.errors.invalid.default.assignee{[]}");
    }

    private JiraServiceContext constructServiceContext(final ErrorCollection errorCollection)
    {
        return new JiraServiceContextImpl((ApplicationUser) null, errorCollection);
    }

    private void assertErrorPresent(final String errorKey, final String value)
    {
        assertTrue("Expected some errors", errorCollection.hasAnyErrors());
        assertThat(errorCollection.getErrors().keySet(), Matchers.<String>hasSize(1));
        assertThat(errorCollection.getErrors(), Matchers.hasEntry(errorKey, value));
    }

    private void assertErrorMessage(final String expectedError)
    {
        assertTrue("Expected some errors", errorCollection.hasAnyErrors());
        assertThat(errorCollection.getErrorMessages(), Matchers.containsInAnyOrder(expectedError));
    }
}
