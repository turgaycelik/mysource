package com.atlassian.jira.scheme.distiller;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.scheme.distiller.DefaultSchemeDistillerService;
import com.atlassian.jira.bc.scheme.distiller.SchemeDistillerService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.scheme.AbstractSchemeTest;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class TestSchemeDistillerService extends AbstractSchemeTest
{
    private SchemeDistillerService schemeDistillerService;

    @Mock @AvailableInContainer
    private SchemeManager mockSchemeManager;
    @Mock @AvailableInContainer
    private SchemeDistiller mockSchemeDistiller;
    @Mock @AvailableInContainer
    private SchemeManagerFactory mockSchemeManagerFactory;
    @Mock @AvailableInContainer
    private SchemeFactory mockSchemeFactory;
    @Mock @AvailableInContainer
    private JiraAuthenticationContext authContext;
    @Mock @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setup() throws Exception
    {
        super.setUp();
        when(authContext.getI18nHelper()).thenReturn(new MockI18nBean());

    }

    @Test
    public void testPersistNewSchemesValidationHappyPath() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");

        when(mockSchemeDistiller.persistNewSchemeMappings(any(DistilledSchemeResult.class))).thenReturn(null);
        when(mockSchemeFactory.getSchemeWithEntitiesComparable(any(GenericValue.class))).thenReturn(testScheme1);

        when(mockSchemeManager.getScheme(testScheme1.getName())).thenReturn(null, new MockGenericValue("someScheme"));
        when(mockSchemeManagerFactory.getSchemeManager(any(String.class))).thenReturn(mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(mockSchemeDistiller, mockSchemeManagerFactory,
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), mockSchemeFactory);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertFalse("No errors occurred when persisting.", errorCollection.hasAnyErrors());
    }

    @Test
    public void testPersistNewSchemesValidationOriginalSchemeDeleted() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");

        when(mockSchemeManager.getScheme(testScheme1.getName())).thenReturn(null, null);
        when(mockSchemeManagerFactory.getSchemeManager(any(String.class))).thenReturn(mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, mockSchemeManagerFactory,
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertTrue("Errors occurred when persisting.", errorCollection.hasAnyErrors());
        assertTrue("Test the correct error message exists", errorCollection.getErrors().containsKey(testScheme1.getName()));
        Assert.assertEquals("Test the correct error message is shown",
                "Some of the original schemes (<strong>" + testScheme1.getName() +
                        "</strong>) have been modified/deleted since the merged scheme was generated. " +
                        "The merged scheme will not be saved and no project associations will be changed.",
                errorCollection.getErrors().get(testScheme1.getName()));
    }

    @Test
    public void testPersistNewSchemesValidationSchemeEntitiesHaveChanged() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");
        when(mockSchemeFactory.getSchemeWithEntitiesComparable(any(GenericValue.class))).thenReturn(testScheme3);

        when(mockSchemeManager.getScheme(testScheme1.getName())).thenReturn(null, new MockGenericValue("someScheme"));
        when(mockSchemeManagerFactory.getSchemeManager(any(String.class))).thenReturn(mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, mockSchemeManagerFactory,
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), mockSchemeFactory);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertTrue("Errors occurred when persisting.", errorCollection.hasAnyErrors());
        assertTrue("Test the correct error message exists", errorCollection.getErrors().containsKey(testScheme1.getName()));
        Assert.assertEquals("Test the correct error message is shown",
                "Some of the original schemes (<strong>" + testScheme1.getName() +
                        "</strong>) have been modified/deleted since the merged scheme was generated. " +
                        "The merged scheme will not be saved and no project associations will be changed.",
                errorCollection.getErrors().get(testScheme1.getName()));
    }

    @Test
    public void testPersistNewSchemesValidationSchemeNameHasChanged() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");
        when(mockSchemeFactory.getSchemeWithEntitiesComparable(any(GenericValue.class))).thenReturn(testScheme2);

        when(mockSchemeManager.getScheme(testScheme1.getName())).thenReturn(null, new MockGenericValue("someScheme"));
        when(mockSchemeManagerFactory.getSchemeManager(any(String.class))).thenReturn(mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, mockSchemeManagerFactory,
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), mockSchemeFactory);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertTrue("Errors occurred when persisting.", errorCollection.hasAnyErrors());
        assertTrue("Test the correct error message exists", errorCollection.getErrors().containsKey(testScheme1.getName()));
        Assert.assertEquals("Test the correct error message is shown",
                "Some of the original schemes (<strong>" + testScheme1.getName() +
                        "</strong>) have been modified/deleted since the merged scheme was generated. " +
                        "The merged scheme will not be saved and no project associations will be changed.",
                errorCollection.getErrors().get(testScheme1.getName()));
    }

    @Test
    public void testPersistNewSchemesValidationSchemeDescHasChanged() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");
        when(mockSchemeFactory.getSchemeWithEntitiesComparable(any(GenericValue.class))).thenReturn(testScheme2);

        when(mockSchemeManager.getScheme(testScheme1.getName())).thenReturn(null, new MockGenericValue("someScheme"));
        when(mockSchemeManagerFactory.getSchemeManager(any(String.class))).thenReturn(mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, mockSchemeManagerFactory,
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), mockSchemeFactory);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertTrue("Errors occurred when persisting.", errorCollection.hasAnyErrors());
        assertTrue("Test the correct error message exists", errorCollection.getErrors().containsKey(testScheme1.getName()));
        Assert.assertEquals("Test the correct error message is shown",
                "Some of the original schemes (<strong>" + testScheme1.getName() +
                        "</strong>) have been modified/deleted since the merged scheme was generated. " +
                        "The merged scheme will not be saved and no project associations will be changed.",
                errorCollection.getErrors().get(testScheme1.getName()));
    }

    @Test
    public void testIsNewSchemeNameValidNameDoesNotExist() throws GenericEntityException
    {
        when(mockSchemeManager.getScheme(any(String.class))).thenReturn(null);
        when(mockSchemeManagerFactory.getSchemeManager(any(String.class))).thenReturn(mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, (SchemeManagerFactory)mockSchemeManagerFactory, getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        schemeDistillerService.isValidNewSchemeName(null, "fieldName", "schemeThatDoesNotExist", "does not matter", errorCollection);

        assertFalse("The error collection is empty.", errorCollection.hasAnyErrors());
    }

    @Test
    public void testIsNewSchemeNameValidNameDoesExist() throws GenericEntityException
    {
        when(mockSchemeManager.getScheme(any(String.class))).thenReturn(new MockGenericValue("BS ENTITY"));
        when(mockSchemeManagerFactory.getSchemeManager(any(String.class))).thenReturn(mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, (SchemeManagerFactory)mockSchemeManagerFactory, getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        schemeDistillerService.isValidNewSchemeName(null, "fieldName", "schemeThatDoesNotExist", "does not matter", errorCollection);

        assertTrue("The error collection is empty.", errorCollection.hasAnyErrors());
        assertTrue("The error for the field we sent through is there.", errorCollection.getErrors().containsKey("fieldName"));
        assertEquals("A scheme with the name you entered already exists. Please enter a different scheme name.", errorCollection.getErrors().get("fieldName"));
    }

    private PermissionManager getPermissionManager(final boolean projectAdminPermission, final boolean adminPermission)
    {
        return new MockPermissionManager()
        {
            public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
            {
                return projectAdminPermission;
            }

            public boolean hasPermission(int permissionsId, com.atlassian.crowd.embedded.api.User u)
            {
                return adminPermission;
            }
        };
    }
}
