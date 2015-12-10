package com.atlassian.jira.bc.customfield;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemBuilder;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.search.searchers.MockCustomFieldSearcher;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldContextConfigHelper;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldValidator;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultCustomFieldService
{
    public static final String DESCRIPTION = "This is a description";
    public static final String NAME = "This is a name";
    public static final long PROJECT_ID_1 = 1L;
    public static final long PROJECT_ID_2 = 2L;
    public static final String ISSUE_TYPE_ID_1 = "1";
    public static final String ISSUE_TYPE_ID_2 = "2";
    public static final String SEARCHER_KEY = "Searcher Key";

    private User user;
    private ApplicationUser applicationUser;
    private MockJiraServiceContext jiraServiceContext;

    @Mock
    private CustomField customField;
    @Mock
    private CustomFieldValidator customFieldValidator;
    @Mock
    private CustomFieldManager customFieldManager;
    @Mock
    private PermissionSchemeManager permissionSchemeManager;
    @Mock
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    @Mock
    private JiraContextTreeManager treeManager;
    @Mock
    private CustomFieldContextConfigHelper customFieldContextConfigHelper;
    @Mock
    private ReindexMessageManager reindexMessageManager;
    @Mock
    private GlobalPermissionManager permissionManager;
    @Mock
    private CustomFieldSearcher customFieldSearcher;
    @Mock
    private ConstantsManager constantsManager;
    @Mock
    private TranslationManager translationManager;
    @Mock
    private LocaleManager localeManager;
    @Mock
    private FieldScreenManager fieldScreenManager;
    @Mock
    private ManagedConfigurationItemService managedConfigurationItemService;

    private PicoContainer container;

    private static final String EXPECTED_MESSAGE = "This is a message we expect";
    private static final String CF_TYPE = "CF_TYPE";

    @Before
    public void setUp() throws Exception
    {
        applicationUser = new MockApplicationUser("TestUser");
        user = applicationUser.getDirectoryUser();


        final NoopI18nFactory noopI18nFactory = new NoopI18nFactory();
        jiraServiceContext = new MockJiraServiceContext(user, new NoopI18nHelper());

        MutablePicoContainer container = new DefaultPicoContainer();
        container.addComponent(I18nHelper.BeanFactory.class, noopI18nFactory);
        container.addComponent(CustomFieldValidator.class, customFieldValidator);
        container.addComponent(CustomFieldManager.class, customFieldManager);
        container.addComponent(PermissionSchemeManager.class, permissionSchemeManager);
        container.addComponent(IssueSecuritySchemeManager.class, issueSecuritySchemeManager);
        container.addComponent(JiraContextTreeManager.class, treeManager);
        container.addComponent(CustomFieldContextConfigHelper.class, customFieldContextConfigHelper);
        container.addComponent(ReindexMessageManager.class, reindexMessageManager);
        container.addComponent(GlobalPermissionManager.class, permissionManager);
        container.addComponent(ConstantsManager.class, constantsManager);
        container.addComponent(TranslationManager.class, translationManager);
        container.addComponent(LocaleManager.class, localeManager);
        container.addComponent(FieldScreenManager.class, fieldScreenManager);
        container.addComponent(ManagedConfigurationItemService.class, managedConfigurationItemService);

        this.container = container;
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getCustomFieldForEditConfigNoAdmin()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, applicationUser)).thenReturn(false);

        final ServiceOutcome<CustomField> customFieldForEditConfig
                = createService().getCustomFieldForEditConfig(applicationUser, "jack");

        final CustomField returnedValue = customFieldForEditConfig.getReturnedValue();
        assertThat(returnedValue, Matchers.nullValue());

        final ErrorCollection errorCollection = customFieldForEditConfig.getErrorCollection();
        assertThat(errorCollection.getReasons(), Matchers.contains(ErrorCollection.Reason.FORBIDDEN));
    }

    @Test
    public void getCustomFieldForEditConfigNoField()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, applicationUser)).thenReturn(true);

        final ServiceOutcome<CustomField> customFieldForEditConfig
                = createService().getCustomFieldForEditConfig(applicationUser, "jack");

        final CustomField returnedValue = customFieldForEditConfig.getReturnedValue();
        assertThat(returnedValue, Matchers.nullValue());

        final ErrorCollection errorCollection = customFieldForEditConfig.getErrorCollection();
        assertThat(errorCollection.getReasons(), Matchers.contains(ErrorCollection.Reason.NOT_FOUND));
    }

    @Test
    public void getCustomFieldForEditConfigLocked()
    {
        final String fieldId = "jack";
        final ManagedConfigurationItem value = new ManagedConfigurationItemBuilder()
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                .setItemId(fieldId).build();

        when(permissionManager.hasPermission(Permissions.ADMINISTER, applicationUser)).thenReturn(true);
        when(customFieldManager.getCustomFieldObject(fieldId)).thenReturn(customField);
        when(managedConfigurationItemService.getManagedCustomField(customField)).thenReturn(value);
        when(managedConfigurationItemService.doesUserHavePermission(applicationUser.getDirectoryUser(), value)).
                thenReturn(false);

        final ServiceOutcome<CustomField> customFieldForEditConfig
                = createService().getCustomFieldForEditConfig(applicationUser, fieldId);

        final CustomField returnedValue = customFieldForEditConfig.getReturnedValue();
        assertThat(returnedValue, Matchers.nullValue());

        final ErrorCollection errorCollection = customFieldForEditConfig.getErrorCollection();
        assertThat(errorCollection.getReasons(), Matchers.contains(ErrorCollection.Reason.FORBIDDEN));
    }

    @Test
    public void getCustomFieldForEditConfigGoodManaged()
    {
        final String fieldId = "jack";
        final ManagedConfigurationItem value = new ManagedConfigurationItemBuilder()
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                .setItemId(fieldId).build();

        when(permissionManager.hasPermission(Permissions.ADMINISTER, applicationUser)).thenReturn(true);
        when(customFieldManager.getCustomFieldObject(fieldId)).thenReturn(customField);
        when(managedConfigurationItemService.getManagedCustomField(customField)).thenReturn(value);
        when(managedConfigurationItemService.doesUserHavePermission(applicationUser.getDirectoryUser(), value)).
                thenReturn(true);

        final ServiceOutcome<CustomField> customFieldForEditConfig
                = createService().getCustomFieldForEditConfig(applicationUser, fieldId);

        final CustomField returnedValue = customFieldForEditConfig.getReturnedValue();
        assertThat(returnedValue, Matchers.equalTo(customField));
    }

    @Test
    public void getCustomFieldTypesForUserNoPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, applicationUser)).thenReturn(false);
        assertThat(createService().getCustomFieldTypesForUser(applicationUser), IsEmptyIterable.<CustomFieldType<?,?>>emptyIterable());
    }

    @Test
    public void getCustomFieldTypesForUser()
    {
        CustomFieldType<?, ?> type1 = createField("abc", null);
        CustomFieldType<?, ?> type2 = createField("def", ConfigurationItemAccessLevel.LOCKED);
        CustomFieldType<?, ?> type3 = createField("ghi", ConfigurationItemAccessLevel.SYS_ADMIN);

        when(managedConfigurationItemService.doesUserHavePermission(user, ConfigurationItemAccessLevel.SYS_ADMIN))
                .thenReturn(true);
        when(customFieldManager.getCustomFieldTypes()).thenReturn(Lists.newArrayList(type1, type2, type3));
        when(permissionManager.hasPermission(Permissions.ADMINISTER, applicationUser)).thenReturn(true);
        assertThat(createService().getCustomFieldTypesForUser(applicationUser), IsIterableContainingInOrder.<CustomFieldType<?, ?>>contains(type1, type3));
    }

    @Test
    public void testValidateDeleteNotAdmin()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        // Test User is not admin
        DefaultCustomFieldService defaultCustomFieldService = createService();

        // run validate
        defaultCustomFieldService.validateDelete(jiraServiceContext, new Long(1));
        // we expect the jira service context to contain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals(NoopI18nHelper.makeTranslation("admin.customfields.service.no.admin.permission"),
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteCustomFieldThatDoesntExist()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = createService();

        // run validate
        defaultCustomFieldService.validateDelete(jiraServiceContext, new Long(1));
        // we expect the jira service context to contiain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals(NoopI18nHelper.makeTranslation("admin.errors.customfields.invalid.custom.field"),
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteSuccess()
    {
        DefaultCustomFieldService defaultCustomFieldService = createService();

        defaultCustomFieldService = spy(defaultCustomFieldService);
        doNothing().when(defaultCustomFieldService)
                .validateNotUsedInPermissionSchemes(Mockito.any(JiraServiceContext.class), Mockito.anyLong(), Mockito.anyBoolean());

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldManager.getCustomFieldObject(1L)).thenReturn(customField);

        // Test User customfield is null
        // run validate
        defaultCustomFieldService.validateDelete(jiraServiceContext, new Long(1));
        // we expect the jira service context to contiain an error message
        jiraServiceContext.assertNoErrors();

        Mockito.verify(defaultCustomFieldService).validateNotUsedInPermissionSchemes(jiraServiceContext, 1L, false);
    }

    @Test
    public void testValidateDeleteUsedInPermissionSchemes()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldManager.getCustomFieldObject(1L)).thenReturn(customField);

        DefaultCustomFieldService defaultCustomFieldService = spy(createService());

        doAnswer(new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                final JiraServiceContext o = (JiraServiceContext) invocation.getArguments()[0];
                o.getErrorCollection().addErrorMessage("UsedInPermissionSchemes");
                return null;
            }
        }).when(defaultCustomFieldService).validateNotUsedInPermissionSchemes(Mockito.any(JiraServiceContext.class), anyLong(), anyBoolean());

        // run validate
        defaultCustomFieldService.validateDelete(jiraServiceContext, new Long(1));
        // we expect the jira service context to contiain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("UsedInPermissionSchemes", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUpdateWithNullCustomFieldId()
    {
        DefaultCustomFieldService defaultCustomFieldService = createService();
        try
        {
            defaultCustomFieldService.validateUpdate(null, null, null, null, null);
            fail("Should have failed with IllegalArgumentException.");
        }
        catch (IllegalArgumentException e)
        {
            //Expected error if the customFieldId is null
        }
    }

    @Test
    public void testValidateUpdateNotAdmin()
    {
        // Test User is not admin
        DefaultCustomFieldService defaultCustomFieldService = createService();

        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), null, null, null);
        // we expect the jira service context to contiain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals(NoopI18nHelper.makeTranslation("admin.customfields.service.no.admin.permission"),
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUpdateCustomFieldThatDoesntExist()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = createService();

        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), null, null, null);
        // we expect the jira service context to contiain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals(NoopI18nHelper.makeTranslation("admin.errors.customfields.invalid.custom.field"),
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUpdateWithNoName()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldManager.getCustomFieldObject(1L)).thenReturn(customField);

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = createService();

        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), null, null, null);
        // we expect the jira service context to contiain an error message
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals(NoopI18nHelper.makeTranslation("admin.errors.customfields.no.name"),
                jiraServiceContext.getErrorCollection().getErrors().get("name"));
    }

    @Test
    public void testValidateUpdateWithInvalidSearcher()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldManager.getCustomFieldObject(1L)).thenReturn(customField);

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = createService();
        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), "customFieldName", null, "InvalidSearcherKey");
        // we expect the jira service context to contiain an error message
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals(NoopI18nHelper.makeTranslation("admin.errors.customfields.invalid.searcher"),
                jiraServiceContext.getErrorCollection().getErrors().get("searcher"));
    }

    @Test
    public void testValidateUpdateWithValidSearcher()
    {
        final CustomFieldSearcher searcher = Mockito.mock(CustomFieldSearcher.class);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldManager.getCustomFieldObject(1L)).thenReturn(customField);
        when(customFieldManager.getCustomFieldSearcher("ValidSearcherKey")).thenReturn(searcher);

        final DefaultCustomFieldService defaultCustomFieldService = spy(createService());

        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), "customFieldName", null, "ValidSearcherKey");
        // we expect the jira service context to contiain an error message
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());

        verify(defaultCustomFieldService, never()).validateNotUsedInPermissionSchemes(Mockito.any(JiraServiceContext.class), anyLong(), anyBoolean());
    }

    @Test
    public void testValidateUpdateRemovingSearcher()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldManager.getCustomFieldObject(1L)).thenReturn(customField);

        final DefaultCustomFieldService defaultCustomFieldService = spy(createService());
        doNothing().when(defaultCustomFieldService).validateNotUsedInPermissionSchemes(any(JiraServiceContext.class), anyLong(), anyBoolean());

        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), "customFieldName", null, null);
        // we expect the jira service context to contiain an error message
        jiraServiceContext.assertNoErrors();

        verify(defaultCustomFieldService).validateNotUsedInPermissionSchemes(jiraServiceContext, 1L, true);
    }

    @Test
    public void testValidateNotUsedInPermissionSchemes()
    {
        Long customFieldID = 1L;
        final DefaultCustomFieldService defaultCustomFieldService = spy(createService());
        when(defaultCustomFieldService.getUsedPermissionSchemes(anyLong()))
                .thenReturn(Sets.<GenericValue>newHashSet());
        when(defaultCustomFieldService.getUsedIssueSecuritySchemes(anyLong()))
                .thenReturn(Sets.<GenericValue>newHashSet());

        MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        defaultCustomFieldService.validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldID, true);
        jiraServiceContext.assertNoErrors();
    }

    @Test
    public void testValidateNotUsedInPermissionSchemesWithPermissionsEnterprise()
    {
        Long customFieldID = 1L;
        final DefaultCustomFieldService service = spy(createService());

        doAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation)
            {
                HashSet<GenericValue> set = new HashSet<GenericValue>();
                MockGenericValue mockGenericValue = new MockGenericValue("PermissionScheme", EasyMap.build("name", "TestPermScheme"));
                set.add(mockGenericValue);
                return set;
            }
        }).when(service).getUsedPermissionSchemes(anyLong());

        doAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                HashSet<GenericValue> set = new HashSet<GenericValue>();
                MockGenericValue mockGenericValue = new MockGenericValue("IssueSecurityScheme", EasyMap.build("name", "IssueSecScheme"));
                set.add(mockGenericValue);
                return set;
            }
        }).when(service).getUsedIssueSecuritySchemes(anyLong());

        MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        // check for update
        service.validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldID, true);
        assertEquals(2, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        Iterator errorIterator = jiraServiceContext.getErrorCollection().getErrorMessages().iterator();
        assertEquals("Search Template cannot be set to 'None' because this custom field is used in the following Permission Scheme(s): TestPermScheme", errorIterator.next());
        assertEquals("Search Template cannot be set to 'None' because this custom field is used in the following Issue Level Security Scheme(s): IssueSecScheme", errorIterator.next());

        jiraServiceContext = new MockJiraServiceContext();
        // Check for delete
        service.validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldID, false);
        assertEquals(2, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        errorIterator = jiraServiceContext.getErrorCollection().getErrorMessages().iterator();
        assertEquals("Custom field cannot be deleted because it is used in the following Permission Scheme(s): TestPermScheme", errorIterator.next());
        assertEquals("Custom field cannot be deleted because it is used in the following Issue Level Security Scheme(s): IssueSecScheme", errorIterator.next());
    }

    @Test
    public void validateUserHasPermissionToAddCustomField()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        final ServiceOutcome<CreateValidationResult> outcome = createService().validateCreate(user, null);

        Assert.assertEquals(false, outcome.isValid());
        Assert.assertEquals(NoopI18nHelper.makeTranslation("admin.customfields.service.no.admin.permission"), outcome.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void assumeValidationOfCustomFieldTypeWorks()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcomeForNull = service.validateCreate(user, CustomFieldDefinition.builder().build());

        Assert.assertEquals(false, outcomeForNull.isValid());
        Assert.assertEquals(NoopI18nHelper.makeTranslation("admin.errors.customfields.no.field.type.specified"), outcomeForNull.getErrorCollection().getErrorMessages().iterator().next());

        final ServiceOutcome<CreateValidationResult> outcomeForEmpty = service.validateCreate(user, CustomFieldDefinition.builder().cfType("").build());

        Assert.assertEquals(false, outcomeForEmpty.isValid());
        Assert.assertEquals(NoopI18nHelper.makeTranslation("admin.errors.customfields.no.field.type.specified"), outcomeForEmpty.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void assumeValidationOfSearcherKeyWorks()
    {
        when(customFieldValidator.isValidType(anyString())).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        final SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();

        simpleErrorCollection.addErrorMessage(EXPECTED_MESSAGE);
        when(customFieldValidator.validateDetails(anyString(),anyString(),anyString())).thenReturn(simpleErrorCollection);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcomeForNull = service.validateCreate(user, CustomFieldDefinition.builder().cfType(CF_TYPE).build());

        Assert.assertEquals(false, outcomeForNull.isValid());
        Assert.assertEquals(EXPECTED_MESSAGE, outcomeForNull.getErrorCollection().getErrorMessages().iterator().next());

        final ServiceOutcome<CreateValidationResult> outcomeForEmpty = service.validateCreate(user, CustomFieldDefinition.builder().cfType(CF_TYPE).searcherKey("").build());

        Assert.assertEquals(false, outcomeForEmpty.isValid());
        Assert.assertEquals(EXPECTED_MESSAGE, outcomeForEmpty.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void validateCustomFieldDefinitionContainsProjects()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldValidator.validateDetails(anyString(), anyString(), anyString())).thenReturn(new SimpleErrorCollection());

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcome = service.validateCreate(user, CustomFieldDefinition.builder().cfType(CF_TYPE).searcherKey(SEARCHER_KEY).build());

        Assert.assertEquals(false, outcome.isValid());
        Assert.assertEquals(NoopI18nHelper.makeTranslation("admin.errors.must.select.project.for.non.global.contexts"), outcome.getErrorCollection().getErrors().get("projects"));
    }

    @Test
    public void validateCustomFieldInGlobalContext()
    {
        final JiraContextNode mockRootContextNode = Mockito.mock(JiraContextNode.class);
        final CustomFieldType mockCustomFieldType = Mockito.mock(CustomFieldType.class);
        final CustomFieldDefinition cfDefinition = CustomFieldDefinition.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .searcherKey(SEARCHER_KEY)
                .cfType(CF_TYPE)
                .isAllIssueTypes(true)
                .isGlobal(true).build();


        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldValidator.validateDetails(anyString(), anyString(), anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldManager.getCustomFieldSearcher(cfDefinition.getSearcherKey())).thenReturn(customFieldSearcher);
        when(treeManager.getRootNode()).thenReturn(mockRootContextNode);
        when(customFieldManager.getCustomFieldType(CF_TYPE)).thenReturn(mockCustomFieldType);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcome = service.validateCreate(user, cfDefinition);
        final CreateValidationResult createValidationResult = outcome.getReturnedValue();

        Assert.assertEquals(true, outcome.isValid());
        Assert.assertEquals(mockCustomFieldType, createValidationResult.getCustomFieldType());
        Assert.assertEquals(1, createValidationResult.getContextNodes().size());
        Assert.assertEquals(mockRootContextNode, createValidationResult.getContextNodes().iterator().next());
        Assert.assertEquals(DESCRIPTION, createValidationResult.getDescription());
        Assert.assertEquals(NAME, createValidationResult.getName());
        Assert.assertEquals(1,createValidationResult.getIssueTypes().size());
        Assert.assertNull(createValidationResult.getIssueTypes().iterator().next());
        Assert.assertEquals(customFieldSearcher, createValidationResult.getCustomFieldSearcher());
        Assert.assertEquals(user, createValidationResult.getUser());
    }

    @Test
    public void validateCustomFieldDefaultSearcherFlag()
    {
        final MockCustomFieldSearcher mockSearcher = new MockCustomFieldSearcher("searcherId");
        final JiraContextNode mockRootContextNode = Mockito.mock(JiraContextNode.class);
        final CustomFieldType mockCustomFieldType = Mockito.mock(CustomFieldType.class);

        final CustomFieldDefinition cfDefinition = CustomFieldDefinition.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .searcherKey(SEARCHER_KEY)
                .cfType(CF_TYPE)
                .isAllIssueTypes(true)
                .isGlobal(true)
                .searcherKey("badKey")
                .defaultSearcher().build();

        when(customFieldManager.getDefaultSearcher(mockCustomFieldType)).thenReturn(mockSearcher);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldValidator.validateDetails(anyString(), anyString(), eq(mockSearcher.getId()))).thenReturn(new SimpleErrorCollection());
        when(customFieldManager.getCustomFieldSearcher(mockSearcher.getId())).thenReturn(mockSearcher);
        when(treeManager.getRootNode()).thenReturn(mockRootContextNode);
        when(customFieldManager.getCustomFieldType(CF_TYPE)).thenReturn(mockCustomFieldType);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcome = service.validateCreate(user, cfDefinition);
        final CreateValidationResult createValidationResult = outcome.getReturnedValue();

        Assert.assertEquals(true, outcome.isValid());
        Assert.assertEquals(mockCustomFieldType, createValidationResult.getCustomFieldType());
        Assert.assertEquals(1, createValidationResult.getContextNodes().size());
        Assert.assertEquals(mockRootContextNode, createValidationResult.getContextNodes().iterator().next());
        Assert.assertEquals(DESCRIPTION, createValidationResult.getDescription());
        Assert.assertEquals(NAME, createValidationResult.getName());
        Assert.assertEquals(1, createValidationResult.getIssueTypes().size());
        Assert.assertNull(createValidationResult.getIssueTypes().iterator().next());
        Assert.assertEquals(mockSearcher, createValidationResult.getCustomFieldSearcher());
        Assert.assertEquals(user, createValidationResult.getUser());
    }

    @Test
    public void validateCustomFieldDefaultSearcherFlagWithoutDefault()
    {
        final MockCustomFieldSearcher mockSearcher = new MockCustomFieldSearcher("searcherId");
        final JiraContextNode mockRootContextNode = Mockito.mock(JiraContextNode.class);
        final CustomFieldType mockCustomFieldType = Mockito.mock(CustomFieldType.class);

        final CustomFieldDefinition cfDefinition = CustomFieldDefinition.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .searcherKey(SEARCHER_KEY)
                .cfType(CF_TYPE)
                .isAllIssueTypes(true)
                .isGlobal(true)
                .searcherKey("badKey")
                .defaultSearcher().build();

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldValidator.validateDetails(anyString(), anyString(), isNull(String.class))).thenReturn(new SimpleErrorCollection());
        when(customFieldManager.getCustomFieldSearcher(mockSearcher.getId())).thenReturn(mockSearcher);
        when(treeManager.getRootNode()).thenReturn(mockRootContextNode);
        when(customFieldManager.getCustomFieldType(CF_TYPE)).thenReturn(mockCustomFieldType);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcome = service.validateCreate(user, cfDefinition);
        final CreateValidationResult createValidationResult = outcome.getReturnedValue();

        Assert.assertEquals(true, outcome.isValid());
        Assert.assertEquals(mockCustomFieldType, createValidationResult.getCustomFieldType());
        Assert.assertEquals(1, createValidationResult.getContextNodes().size());
        Assert.assertEquals(mockRootContextNode, createValidationResult.getContextNodes().iterator().next());
        Assert.assertEquals(DESCRIPTION, createValidationResult.getDescription());
        Assert.assertEquals(NAME, createValidationResult.getName());
        Assert.assertEquals(1, createValidationResult.getIssueTypes().size());
        Assert.assertNull(createValidationResult.getIssueTypes().iterator().next());
        Assert.assertNull(createValidationResult.getCustomFieldSearcher());
        Assert.assertEquals(user, createValidationResult.getUser());
    }

    @Test
    public void validateCustomFieldIssueTypesFlagOverridesSelectedTypes()
    {
        final JiraContextNode mockRootContextNode = Mockito.mock(JiraContextNode.class);
        final CustomFieldType mockCustomFieldType = Mockito.mock(CustomFieldType.class);
        final CustomFieldDefinition cfDefinition = CustomFieldDefinition.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .searcherKey(SEARCHER_KEY)
                .cfType(CF_TYPE)
                .isAllIssueTypes(true)
                .isGlobal(true)
                .addIssueTypeId(ISSUE_TYPE_ID_1)
                .addIssueTypeId(ISSUE_TYPE_ID_2).build();


        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldValidator.validateDetails(anyString(), anyString(), anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldManager.getCustomFieldSearcher(cfDefinition.getSearcherKey())).thenReturn(customFieldSearcher);
        when(treeManager.getRootNode()).thenReturn(mockRootContextNode);
        when(customFieldManager.getCustomFieldType(CF_TYPE)).thenReturn(mockCustomFieldType);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcome = service.validateCreate(user, cfDefinition);
        final CreateValidationResult createValidationResult = outcome.getReturnedValue();

        Assert.assertEquals(true, outcome.isValid());
        Assert.assertEquals(mockCustomFieldType, createValidationResult.getCustomFieldType());
        Assert.assertEquals(1, createValidationResult.getContextNodes().size());
        Assert.assertEquals(mockRootContextNode, createValidationResult.getContextNodes().iterator().next());
        Assert.assertEquals(DESCRIPTION, createValidationResult.getDescription());
        Assert.assertEquals(NAME, createValidationResult.getName());
        Assert.assertEquals(1, createValidationResult.getIssueTypes().size());
        Assert.assertNull(createValidationResult.getIssueTypes().iterator().next());
        Assert.assertEquals(customFieldSearcher, createValidationResult.getCustomFieldSearcher());
        Assert.assertEquals(user, createValidationResult.getUser());
    }

    @Test
    public void validateCustomFieldGlobalContextOverridesProjects()
    {
        final JiraContextNode mockRootContextNode = Mockito.mock(JiraContextNode.class);
        final CustomFieldType mockCustomFieldType = Mockito.mock(CustomFieldType.class);
        final CustomFieldDefinition cfDefinition = CustomFieldDefinition.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .searcherKey(SEARCHER_KEY)
                .cfType(CF_TYPE)
                .isAllIssueTypes(true)
                .isGlobal(true)
                .addProjectId(PROJECT_ID_1)
                .addProjectId(PROJECT_ID_2).build();

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldValidator.validateDetails(anyString(), anyString(), anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldManager.getCustomFieldSearcher(cfDefinition.getSearcherKey())).thenReturn(customFieldSearcher);
        when(treeManager.getRootNode()).thenReturn(mockRootContextNode);
        when(customFieldManager.getCustomFieldType(CF_TYPE)).thenReturn(mockCustomFieldType);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcome = service.validateCreate(user, cfDefinition);
        final CreateValidationResult createValidationResult = outcome.getReturnedValue();

        Assert.assertEquals(true, outcome.isValid());
        Assert.assertEquals(mockCustomFieldType, createValidationResult.getCustomFieldType());
        Assert.assertEquals(1, createValidationResult.getContextNodes().size());
        Assert.assertEquals(mockRootContextNode, createValidationResult.getContextNodes().iterator().next());
        Assert.assertEquals(DESCRIPTION, createValidationResult.getDescription());
        Assert.assertEquals(NAME, createValidationResult.getName());
        Assert.assertEquals(1,createValidationResult.getIssueTypes().size());
        Assert.assertNull(createValidationResult.getIssueTypes().iterator().next());
        Assert.assertEquals(customFieldSearcher, createValidationResult.getCustomFieldSearcher());
        Assert.assertEquals(user, createValidationResult.getUser());
    }

    @Test
    public void validateCustomFieldInContextSelectedIssueTypes()
    {
        final JiraContextNode mockRootContextNode = Mockito.mock(JiraContextNode.class);
        final CustomFieldType mockCustomFieldType = Mockito.mock(CustomFieldType.class);
        final GenericValue genericValueForIssueType1 = Mockito.mock(GenericValue.class);
        final GenericValue genericValueForIssueType2 = Mockito.mock(GenericValue.class);

        final CustomFieldDefinition cfDefinition = CustomFieldDefinition.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .searcherKey(SEARCHER_KEY)
                .cfType(CF_TYPE)
                .isAllIssueTypes(false)
                .isGlobal(true)
                .addIssueTypeId(ISSUE_TYPE_ID_1)
                .addIssueTypeId(ISSUE_TYPE_ID_2).build();

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldValidator.validateDetails(anyString(), anyString(), anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldManager.getCustomFieldSearcher(cfDefinition.getSearcherKey())).thenReturn(customFieldSearcher);
        when(customFieldManager.getCustomFieldType(CF_TYPE)).thenReturn(mockCustomFieldType);
        when(treeManager.getRootNode()).thenReturn(mockRootContextNode);
        when(constantsManager.getIssueType(ISSUE_TYPE_ID_1)).thenReturn(genericValueForIssueType1);
        when(constantsManager.getIssueType(ISSUE_TYPE_ID_2)).thenReturn(genericValueForIssueType2);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcome = service.validateCreate(user, cfDefinition);
        final CreateValidationResult createValidationResult = outcome.getReturnedValue();

        Assert.assertEquals(true, outcome.isValid());
        Assert.assertEquals(mockCustomFieldType, createValidationResult.getCustomFieldType());
        Assert.assertEquals(1, createValidationResult.getContextNodes().size());
        Assert.assertEquals(mockRootContextNode, createValidationResult.getContextNodes().iterator().next());
        Assert.assertEquals(DESCRIPTION, createValidationResult.getDescription());
        Assert.assertEquals(NAME, createValidationResult.getName());
        Assert.assertEquals(2, createValidationResult.getIssueTypes().size());
        Assert.assertEquals(genericValueForIssueType1,createValidationResult.getIssueTypes().get(0));
        Assert.assertEquals(genericValueForIssueType2,createValidationResult.getIssueTypes().get(1));
        Assert.assertEquals(customFieldSearcher, createValidationResult.getCustomFieldSearcher());
        Assert.assertEquals(user, createValidationResult.getUser());
    }

    @Test
    public void validateCustomFieldInContextOfProjects()
    {
        final CustomFieldType mockCustomFieldType = Mockito.mock(CustomFieldType.class);
        final ProjectManager projectManager = Mockito.mock(ProjectManager.class);
        final Project project1 = Mockito.mock(Project.class);
        final Project project2 = Mockito.mock(Project.class);
        final CustomFieldDefinition cfDefinition = CustomFieldDefinition.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .searcherKey(SEARCHER_KEY)
                .cfType(CF_TYPE)
                .isAllIssueTypes(true)
                .isGlobal(false)
                .addProjectId(PROJECT_ID_1)
                .addProjectId(PROJECT_ID_2).build();

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldValidator.validateDetails(anyString(), anyString(), anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldManager.getCustomFieldType(CF_TYPE)).thenReturn(mockCustomFieldType);
        when(customFieldManager.getCustomFieldSearcher(cfDefinition.getSearcherKey())).thenReturn(customFieldSearcher);
        when(treeManager.getProjectManager()).thenReturn(projectManager);
        when(projectManager.getProjectObj(PROJECT_ID_1)).thenReturn(project1);
        when(projectManager.getProjectObj(PROJECT_ID_2)).thenReturn(project2);
        when(project1.getId()).thenReturn(PROJECT_ID_1);
        when(project2.getId()).thenReturn(PROJECT_ID_2);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcome = service.validateCreate(user, cfDefinition);
        final CreateValidationResult createValidationResult = outcome.getReturnedValue();

        Assert.assertEquals(true, outcome.isValid());
        Assert.assertEquals(mockCustomFieldType, createValidationResult.getCustomFieldType());
        Assert.assertEquals(2, createValidationResult.getContextNodes().size());
        Assert.assertEquals(new ProjectContext(project1,treeManager), createValidationResult.getContextNodes().get(0));
        Assert.assertEquals(new ProjectContext(project2,treeManager), createValidationResult.getContextNodes().get(1));
        Assert.assertEquals(DESCRIPTION, createValidationResult.getDescription());
        Assert.assertEquals(NAME, createValidationResult.getName());
        Assert.assertEquals(1,createValidationResult.getIssueTypes().size());
        Assert.assertNull(createValidationResult.getIssueTypes().iterator().next());
        Assert.assertEquals(customFieldSearcher, createValidationResult.getCustomFieldSearcher());
        Assert.assertEquals(user, createValidationResult.getUser());
    }

    @Test
    public void validateCustomFieldRespectSelectedTypes()
    {
        final CustomFieldType mockCustomFieldType = Mockito.mock(CustomFieldType.class);
        final ProjectManager projectManager = Mockito.mock(ProjectManager.class);
        final GenericValue genericValueForIssueType1 = Mockito.mock(GenericValue.class);
        final GenericValue genericValueForIssueType2 = Mockito.mock(GenericValue.class);
        final Project project1 = Mockito.mock(Project.class);
        final Project project2 = Mockito.mock(Project.class);

        final CustomFieldDefinition cfDefinition = CustomFieldDefinition.builder()
                .description(DESCRIPTION)
                .name(NAME)
                .searcherKey(SEARCHER_KEY)
                .cfType(CF_TYPE)
                .isAllIssueTypes(false)
                .isGlobal(false)
                .addIssueTypeId(ISSUE_TYPE_ID_1)
                .addIssueTypeId(ISSUE_TYPE_ID_2)
                .addProjectId(PROJECT_ID_1)
                .addProjectId(PROJECT_ID_2).build();

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(customFieldValidator.validateType(anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldValidator.validateDetails(anyString(), anyString(), anyString())).thenReturn(new SimpleErrorCollection());
        when(customFieldManager.getCustomFieldSearcher(cfDefinition.getSearcherKey())).thenReturn(customFieldSearcher);
        when(treeManager.getProjectManager()).thenReturn(projectManager);
        when(customFieldManager.getCustomFieldType(CF_TYPE)).thenReturn(mockCustomFieldType);
        when(constantsManager.getIssueType(ISSUE_TYPE_ID_1)).thenReturn(genericValueForIssueType1);
        when(constantsManager.getIssueType(ISSUE_TYPE_ID_2)).thenReturn(genericValueForIssueType2);
        when(projectManager.getProjectObj(PROJECT_ID_1)).thenReturn(project1);
        when(projectManager.getProjectObj(PROJECT_ID_2)).thenReturn(project2);
        when(project1.getId()).thenReturn(PROJECT_ID_1);
        when(project2.getId()).thenReturn(PROJECT_ID_2);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcome<CreateValidationResult> outcome = service.validateCreate(user, cfDefinition);

        Assert.assertEquals(true, outcome.isValid());
        Assert.assertEquals(mockCustomFieldType, outcome.getReturnedValue().getCustomFieldType());
        Assert.assertEquals(2, outcome.getReturnedValue().getContextNodes().size());
        Assert.assertEquals(new ProjectContext(project1,treeManager), outcome.getReturnedValue().getContextNodes().get(0));
        Assert.assertEquals(new ProjectContext(project2,treeManager), outcome.getReturnedValue().getContextNodes().get(1));
        Assert.assertEquals(DESCRIPTION, outcome.getReturnedValue().getDescription());
        Assert.assertEquals(NAME, outcome.getReturnedValue().getName());
        Assert.assertEquals(2, outcome.getReturnedValue().getIssueTypes().size());
        Assert.assertEquals(genericValueForIssueType1,outcome.getReturnedValue().getIssueTypes().get(0));
        Assert.assertEquals(genericValueForIssueType2,outcome.getReturnedValue().getIssueTypes().get(1));
        Assert.assertEquals(customFieldSearcher, outcome.getReturnedValue().getCustomFieldSearcher());
        Assert.assertEquals(user, outcome.getReturnedValue().getUser());
    }

    @Test
    public void assumeValidationOfNullCustomFieldWorksForAdd()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("customFieldId can not be null.");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcomeImpl<List<Long>> outcome = service.addToScreenTabs(user, null, null);
    }

    @Test
    public void assumeValidationOfNullCustomFieldWorksForRemove()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("customFieldId can not be null.");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcomeImpl<List<Long>> outcome = service.addToScreenTabs(user, null, null);
    }

    @Test
    public void assumeValidationOfEmptyTabsListWorksForAdd()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("List of tabs can not be null or empty.");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcomeImpl<List<Long>> outcome = service.addToScreenTabs(user, 1L, null);
    }

    @Test
    public void assumeValidationOfEmptyTabsListWorksForRemove()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("List of tabs can not be null or empty.");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcomeImpl<List<Long>> outcome = service.addToScreenTabs(user, 1L, null);
    }

    @Test
    public void assumeUserDoNotHavePermissionToAddCustomFieldToScreen()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        final DefaultCustomFieldService service = createService();
        final ServiceOutcomeImpl<List<Long>> outcome = service.addToScreenTabs(user, 1L, Lists.newArrayList(1L));

        Assert.assertEquals(false, outcome.isValid());
        Assert.assertEquals(NoopI18nHelper.makeTranslation("admin.customfields.service.no.admin.permission"), outcome.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void assumeUserCanAddCustomFieldToScreen()
    {
        final FieldScreenTab fieldScreenTab = Mockito.mock(FieldScreenTab.class);
        final FieldScreenTab fieldScreenTab2 = Mockito.mock(FieldScreenTab.class);
        final Long SCREEN_ID = 10000L;
        final Long TAB1_ID = 10001L;
        final Long TAB2_ID = 10002L;
        final Long CUSTOM_FIELD_ID = 10100L;
        final String CUSTOM_FIELD_STRING_ID = "customfield_" + CUSTOM_FIELD_ID;

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(fieldScreenManager.getFieldScreenTab(SCREEN_ID)).thenReturn(fieldScreenTab);
        when(fieldScreenTab.getId()).thenReturn(TAB1_ID);
        when(fieldScreenTab2.getId()).thenReturn(TAB2_ID);
        when(fieldScreenManager.getFieldScreenTabs(CUSTOM_FIELD_STRING_ID)).thenReturn(Lists.<FieldScreenTab>newArrayList(fieldScreenTab));

        final DefaultCustomFieldService service = createService();
        final ServiceOutcomeImpl<List<Long>> listServiceOutcome = service.addToScreenTabs(user, CUSTOM_FIELD_ID, Lists.newArrayList(SCREEN_ID));
        Mockito.verify(fieldScreenTab).addFieldScreenLayoutItem("customfield_"+CUSTOM_FIELD_ID);
        Assert.assertEquals(1, listServiceOutcome.getReturnedValue().size());
        Assert.assertEquals(TAB1_ID, listServiceOutcome.getReturnedValue().iterator().next());

    }

    @Test
    public void assumeUserCanRemoveCustomFieldToScreen()
    {
        final FieldScreenTab fieldScreenTab = Mockito.mock(FieldScreenTab.class);
        final FieldScreen fieldScreen = Mockito.mock(FieldScreen.class);

        final Long SCREEN_ID = 10000L;
        final Long CUSTOM_FIELD_ID = 10100L;
        final String CUSTOM_FIELD_STRING_ID = "customfield_" + CUSTOM_FIELD_ID;

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(fieldScreenManager.getFieldScreenTab(SCREEN_ID)).thenReturn(fieldScreenTab);
        when(fieldScreenTab.getFieldScreen()).thenReturn(fieldScreen);

        final DefaultCustomFieldService service = createService();
        service.removeFromScreenTabs(user, CUSTOM_FIELD_ID, Lists.newArrayList(SCREEN_ID));
        Mockito.verify(fieldScreen).removeFieldScreenLayoutItem(CUSTOM_FIELD_STRING_ID);
    }

    @Test
    public void getDefaultSearcher()
    {
        CustomFieldType<?,?> type = new MockCustomFieldType();
        final MockCustomFieldSearcher searcher = new MockCustomFieldSearcher("id");
        when(customFieldManager.getDefaultSearcher(type)).thenReturn(searcher);

        final DefaultCustomFieldService service = createService();
        assertSame(searcher, service.getDefaultSearcher(type));
    }

    private DefaultCustomFieldService createService()
    {
        return createObject(DefaultCustomFieldService.class);
    }

    private <T> T createObject(Class<T> klazz)
    {
        final DefaultPicoContainer picoContainer = new DefaultPicoContainer(container);
        picoContainer.addComponent(klazz);
        return klazz.cast(picoContainer.getComponent(klazz));
    }

    private CustomFieldType<?,?> createField(String name, ConfigurationItemAccessLevel configurationItemAccessLevel)
    {
        CustomFieldType<?, ?> type = new MockCustomFieldType(name, name);
        final CustomFieldTypeModuleDescriptor mock = Mockito.mock(CustomFieldTypeModuleDescriptor.class);
        when(mock.getManagedAccessLevel()).thenReturn(configurationItemAccessLevel);
        type.init(mock);
        return type;
    }
}
