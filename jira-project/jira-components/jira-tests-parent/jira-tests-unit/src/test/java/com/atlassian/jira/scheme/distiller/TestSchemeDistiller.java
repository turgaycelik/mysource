package com.atlassian.jira.scheme.distiller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.scheme.AbstractSchemeTest;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestSchemeDistiller extends AbstractSchemeTest
{
    private SchemeDistiller schemeDistiller;


    @Mock
    @AvailableInContainer
    private SchemeManager mockSchemeManager;
    @Mock @AvailableInContainer
    private SchemeDistiller mockSchemeDistiller;
    @Mock @AvailableInContainer
    private SchemeManagerFactory mockSchemeManagerFactory;
    @Mock @AvailableInContainer
    private SchemeFactory mockSchemeFactory;
    @Mock @AvailableInContainer
    private PermissionSchemeManager mockPermissionSchemeManager;
    @Mock @AvailableInContainer
    private NotificationSchemeManager mockNotificationSchemeManager;
    @Mock @AvailableInContainer
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setup() throws Exception
    {
        super.setUp();
        when(authContext.getI18nHelper()).thenReturn(new MockI18nBean());

    }

    @Test
    public void testSchemeSmoosherPermissionSchemes() throws GenericEntityException
    {
        when(mockPermissionSchemeManager.getScheme(any(Long.class))).thenReturn(null);
        when(mockPermissionSchemeManager.getProjects(any(GenericValue.class))).thenReturn(Collections.EMPTY_LIST);

        when(mockSchemeManagerFactory.getSchemeManager(eq(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER))).thenReturn(mockPermissionSchemeManager);

        schemeDistiller = new SchemeDistillerImpl(mockSchemeManagerFactory, null, null);

        doSmooshTest(getSchemesForType(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER));
    }

    public void testSchemeSmoosherNotificationSchemes() throws GenericEntityException
    {
        when(mockNotificationSchemeManager.getScheme(any(Long.class))).thenReturn(null);
        when(mockNotificationSchemeManager.getProjects(any(GenericValue.class))).thenReturn(Collections.EMPTY_LIST);

        when(mockSchemeManagerFactory.getSchemeManager(eq(SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER))).thenReturn(mockNotificationSchemeManager);

        schemeDistiller = new SchemeDistillerImpl(mockSchemeManagerFactory, null, null);

        doSmooshTest(getSchemesForType(SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER));
    }

    private void doTestPersistNewSchemeMappingsForSchemes(String defaultSchemeName, boolean notification) throws Exception
    {

        // Get the deps that we need
        ProjectFactory projectFactory = ComponentAccessor.getProjectFactory();
        SchemeManagerFactory schemeManagerFactory = ComponentManager.getComponentInstanceOfType(SchemeManagerFactory.class);
        SchemeFactory schemeFactory = (SchemeFactory) ComponentManager.getComponentInstanceOfType(SchemeFactory.class);

        SchemeManager schemeManager = notification ? schemeManagerFactory.getSchemeManager(SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER)
                : schemeManagerFactory.getSchemeManager(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER);

        // Create a working SchemeDistiller
        schemeDistiller = new SchemeDistillerImpl(schemeManagerFactory, null, null);

        // Create a project and get the Object representation of it.
        GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST", "name", "project 1"));
        Project project1 = projectFactory.getProject(projectGV);

        // Create a second project and get the Object representation of it.
        GenericValue projectGV2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2), "key", "TST2", "name", "project 2"));
        Project project2 = ComponentAccessor.getProjectFactory().getProject(projectGV2);

        // Create a third project and get the Object representation of it.
        GenericValue projectGV3 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(3), "key", "TST3", "name", "project 3"));
        Project project3 = ComponentAccessor.getProjectFactory().getProject(projectGV3);

        // Get the defaultNotification scheme and copy it twice
        GenericValue defaultSchemeGV = schemeManager.getScheme(defaultSchemeName);
        GenericValue copiedNotificationScheme1 = schemeManager.copyScheme(defaultSchemeGV);
        GenericValue copiedNotificationScheme2 = schemeManager.copyScheme(defaultSchemeGV);

        // Get the object representations of the schemes we just created
        Scheme defaultScheme = schemeFactory.getSchemeWithEntitiesComparable(defaultSchemeGV);
        Scheme scheme1 = schemeFactory.getSchemeWithEntitiesComparable(copiedNotificationScheme1);
        Scheme scheme2 = schemeFactory.getSchemeWithEntitiesComparable(copiedNotificationScheme2);

        // Associate one project with one scheme so that we have three different project associated with 3 different
        // schemes.
        schemeManager.addSchemeToProject(project1, defaultScheme);
        schemeManager.addSchemeToProject(project2, scheme1);
        schemeManager.addSchemeToProject(project3, scheme2);

        // Smoosh the schemes, this should smoosh down to 1 scheme
        DistilledSchemeResults results = schemeDistiller.distillSchemes(EasyList.build(defaultScheme, scheme1, scheme2));

        assertEquals(1, results.getDistilledSchemeResults().size());
        assertEquals(0, results.getUnDistilledSchemes().size());

        // Test that we can persist the result
        Scheme scheme = schemeDistiller.persistNewSchemeMappings((DistilledSchemeResult) new ArrayList(results.getDistilledSchemeResults()).get(0));
        assertNotNull(scheme);

        // get the associated projects for the new scheme
        List projects = schemeManager.getProjects(scheme);

        // The new scheme should now be associated with the 3 projects
        assertEquals(3, projects.size());

        // Make sure that the three projects are the 3 that we created.
        List projectNames = new ArrayList();
        for (Iterator iterator = projects.iterator(); iterator.hasNext();)
        {
            Project project = (Project) iterator.next();
            projectNames.add(project.getName());
        }
        assertTrue(projectNames.contains(project1.getName()));
        assertTrue(projectNames.contains(project2.getName()));
        assertTrue(projectNames.contains(project3.getName()));
    }

    private void doSmooshTest(Collection schemes)
    {
        DistilledSchemeResults distilledSchemeResults = schemeDistiller.distillSchemes(schemes);

        // We should get 2 results, one that has smooshed the testScheme1 & 2 and a result with an unsmooshed scheme
        // for testScheme3.
        assertEquals(1, distilledSchemeResults.getDistilledSchemeResults().size());
        assertEquals(1, distilledSchemeResults.getUnDistilledSchemes().size());

        // Check that the unSmooshed scheme is the one it should be, testScheme3
        assertTrue(testScheme3.containsSameEntities((Scheme) new ArrayList(distilledSchemeResults.getUnDistilledSchemes()).get(0)));


        DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) new ArrayList(distilledSchemeResults.getDistilledSchemeResults()).get(0);
        assertEquals(2, distilledSchemeResult.getOriginalSchemes().size());
        assertTrue(testScheme1.containsSameEntities(distilledSchemeResult.getResultingScheme()));
    }

}
