package com.atlassian.jira.imports.project.core;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalIssueImpl;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.populator.BackupOverviewPopulator;
import com.atlassian.jira.plugin.PluginVersion;
import com.atlassian.jira.plugin.PluginVersionImpl;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestBackupOverviewBuilderImpl
{
    private static final String TEST_ELEMENT = "TestElement";

    // Test that populate from element delegates to the the registered populators
    @Test
    public void testPopulateInformatonFromElement() throws ParseException
    {
        final MockControl mockBackupOverviewPopulatorControl = MockControl.createStrictControl(BackupOverviewPopulator.class);
        final BackupOverviewPopulator mockBackupOverviewPopulator = (BackupOverviewPopulator) mockBackupOverviewPopulatorControl.getMock();

        BackupOverviewBuilderImpl builder = new BackupOverviewBuilderImpl()
        {
            protected void registerOverviewPopulators()
            {
                try
                {
                    mockBackupOverviewPopulator.populate(this, TEST_ELEMENT, Collections.EMPTY_MAP);
                }
                catch (ParseException e)
                {
                    // no-op
                }
                mockBackupOverviewPopulatorControl.replay();
                registerOverviewPopulator(mockBackupOverviewPopulator);
            }
        };

        builder.populateInformationFromElement(TEST_ELEMENT, Collections.EMPTY_MAP);

        assertEquals(1, builder.getBackupOverview().getBackupSystemInformation().getEntityCount());

        mockBackupOverviewPopulatorControl.verify();
    }

    @Test
    public void testEdition()
    {
        BackupOverviewBuilderImpl builder = new BackupOverviewBuilderImpl()
        {
            protected void registerOverviewPopulators()
            {
                // no-op
            }
        };

        builder.setEdition("TestEdition");
        assertEquals("TestEdition", builder.getEdition());
    }
    
    @Test
    public void testBuildNumber()
    {
        BackupOverviewBuilderImpl builder = new BackupOverviewBuilderImpl()
        {
            protected void registerOverviewPopulators()
            {
                // no-op
            }
        };

        builder.setBuildNumber("123");
        assertEquals("123", builder.getBuildNumber());
    }

    @Test
    public void testPluginVersions()
    {
        BackupOverviewBuilderImpl builder = new BackupOverviewBuilderImpl()
        {
            protected void registerOverviewPopulators()
            {
                // no-op
            }
        };

        PluginVersion pluginVersion1 = new PluginVersionImpl("test.key.1", "test name 1", "0.1", new Date());
        PluginVersion pluginVersion2 = new PluginVersionImpl("test.key.2", "test name 2", "0.2", new Date());
        
        builder.addPluginVersion(pluginVersion1);
        builder.addPluginVersion(pluginVersion2);

        final List pluginVersions = builder.getPluginVersions();
        assertTrue(pluginVersions.contains(pluginVersion1));
        assertTrue(pluginVersions.contains(pluginVersion2));
    }

    @Test
    public void testBuildingBackupProjects()
    {
        final List customFieldConfigs = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("1", "1", "1"), "321"));
        BackupOverviewBuilderImpl builder = new BackupOverviewBuilderImpl()
        {
            protected void registerOverviewPopulators()
            {
                // no-op
            }

            List getCustomFieldConfigurations(final String projectId)
            {
                return customFieldConfigs;
            }
        };

        // Add 2 projects
        ExternalProject externalProject1 = new ExternalProject();
        externalProject1.setKey("TST1");
        externalProject1.setId("1");
        ExternalProject externalProject2 = new ExternalProject();
        externalProject2.setKey("TST2");
        externalProject2.setId("2");
        builder.addProject(externalProject1);
        builder.addProject(externalProject2);

        // Add some versions for the two projects
        ExternalVersion version1 = new ExternalVersion("Version1");
        version1.setProjectId("1");
        ExternalVersion version2 = new ExternalVersion("Version2");
        version2.setProjectId("2");
        builder.addVersion(version1);
        builder.addVersion(version2);

        // Add some components for the two projects
        ExternalComponent component1 = new ExternalComponent("Comp1");
        component1.setProjectId("1");
        ExternalComponent component2 = new ExternalComponent("Comp2");
        component2.setProjectId("2");
        builder.addComponent(component1);
        builder.addComponent(component2);

        // Add some issue id's
        final String issueId1 = "1";
        final String issueId2 = "2";
        final String issueId3 = "3";
        final String issueId4 = "4";
        ExternalIssue issue1 = new ExternalIssueImpl(null);
        issue1.setId(issueId1);
        issue1.setProject("1");
        issue1.setKey("TST1-1");
        ExternalIssue issue2 = new ExternalIssueImpl(null);
        issue2.setId(issueId2);
        issue2.setProject("2");
        issue2.setKey("TST2-1");
        ExternalIssue issue3 = new ExternalIssueImpl(null);
        issue3.setId(issueId3);
        issue3.setProject("1");
        issue3.setKey("TST1-2");
        ExternalIssue issue4 = new ExternalIssueImpl(null);
        issue4.setId(issueId4);
        issue4.setProject("2");
        issue4.setKey("TST2-2");
        builder.addIssue(issue1);
        builder.addIssue(issue2);
        builder.addIssue(issue3);
        builder.addIssue(issue4);

        BackupProjectImpl backupProject1 = new BackupProjectImpl(externalProject1, EasyList.build(version1), EasyList.build(component1), customFieldConfigs, EasyList.build(new Long(issueId1), new Long(issueId3)));
        BackupProjectImpl backupProject2 = new BackupProjectImpl(externalProject2, EasyList.build(version2), EasyList.build(component2), customFieldConfigs, EasyList.build(new Long(issueId2), new Long(issueId4)));

        final List projects = builder.getProjects();
        assertEquals(2, projects.size());
        if ("1".equals(((BackupProject)projects.get(0)).getProject().getId()))
        {
            assertEquals(backupProject1, projects.get(0));
        }
        else
        {
            assertEquals(backupProject2, projects.get(0));
        }

        if ("2".equals(((BackupProject)projects.get(1)).getProject().getId()))
        {
            assertEquals(backupProject2, projects.get(1));
        }
        else
        {
            assertEquals(backupProject1, projects.get(1));
        }

        // Make some assertions about the global issue id / key map
        final BackupSystemInformation information = builder.getBackupOverview().getBackupSystemInformation();
        assertEquals("TST1-1", information.getIssueKeyForId(issueId1));
        assertEquals("TST2-1", information.getIssueKeyForId(issueId2));
        assertEquals("TST1-2", information.getIssueKeyForId(issueId3));
        assertEquals("TST2-2", information.getIssueKeyForId(issueId4));
    }

    @Test
    public void testBuildingBackupProjectsNoVersionsComponentsCustomFieldConfigsOrIssuesIds()
    {
        BackupOverviewBuilderImpl builder = new BackupOverviewBuilderImpl()
        {
            protected void registerOverviewPopulators()
            {
                // no-op
            }
        };

        // Add 2 projects
        ExternalProject externalProject1 = new ExternalProject();
        externalProject1.setKey("TST1");
        externalProject1.setId("1");
        ExternalProject externalProject2 = new ExternalProject();
        externalProject2.setKey("TST2");
        externalProject2.setId("2");
        builder.addProject(externalProject1);
        builder.addProject(externalProject2);

        BackupProjectImpl backupProject1 = new BackupProjectImpl(externalProject1, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        BackupProjectImpl backupProject2 = new BackupProjectImpl(externalProject2, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final List projects = builder.getProjects();
        assertEquals(2, projects.size());
        if ("1".equals(((BackupProject)projects.get(0)).getProject().getId()))
        {
            assertEquals(backupProject1, projects.get(0));
        }
        else
        {
            assertEquals(backupProject2, projects.get(0));
        }

        if ("2".equals(((BackupProject)projects.get(1)).getProject().getId()))
        {
            assertEquals(backupProject2, projects.get(1));
        }
        else
        {
            assertEquals(backupProject1, projects.get(1));
        }
    }

    @Test
    public void testBuildingBackupOverview()
    {
        // Add 2 projects
        final ExternalProject externalProject1 = new ExternalProject();
        externalProject1.setKey("TST1");
        externalProject1.setId("1");
        final ExternalProject externalProject2 = new ExternalProject();
        externalProject2.setKey("TST2");
        externalProject2.setId("2");

        // Add some versions for the two projects
        final ExternalVersion version1 = new ExternalVersion("Version1");
        final ExternalVersion version2 = new ExternalVersion("Version2");

        // Add some components for the two projects
        final ExternalComponent component1 = new ExternalComponent("Comp1");
        final ExternalComponent component2 = new ExternalComponent("Comp2");

        // Add some issue id's
        final String issueId1 = "1";
        final String issueId2 = "2";
        final String issueId3 = "3";
        final String issueId4 = "4";

        final PluginVersionImpl pluginVersion1 = new PluginVersionImpl("test.key.1", "test1", "0.1", new Date());
        final PluginVersionImpl pluginVersion2 = new PluginVersionImpl("test.key.2", "test2", "0.2", new Date());

        final BackupProjectImpl backupProject1 = new BackupProjectImpl(externalProject1, EasyList.build(version1), EasyList.build(component1), Collections.EMPTY_LIST, EasyList.build(new Long(issueId1), new Long(issueId3)));
        final BackupProjectImpl backupProject2 = new BackupProjectImpl(externalProject2, EasyList.build(version2), EasyList.build(component2), Collections.EMPTY_LIST, EasyList.build(new Long(issueId2), new Long(issueId4)));

        BackupOverviewBuilderImpl builder = new BackupOverviewBuilderImpl()
        {
            protected void registerOverviewPopulators()
            {
                // no-op
            }

            List /*<BackupProject>*/ getProjects()
            {
                return EasyList.build(backupProject1, backupProject2);
            }

            List /*<PluginVersion>*/ getPluginVersions()
            {
                return EasyList.build(pluginVersion1, pluginVersion2);
            }

            String getBuildNumber()
            {
                return "123";
            }

            String getEdition()
            {
                return "TestEdition";
            }
        };

        BackupOverview backupOverview = builder.getBackupOverview();
        assertEquals(2, backupOverview.getProjects().size());
        if ("1".equals(((BackupProject)backupOverview.getProjects().get(0)).getProject().getId()))
        {
            assertEquals(backupProject1, backupOverview.getProjects().get(0));
        }
        else
        {
            assertEquals(backupProject2, backupOverview.getProjects().get(0));
        }

        if ("2".equals(((BackupProject)backupOverview.getProjects().get(1)).getProject().getId()))
        {
            assertEquals(backupProject2, backupOverview.getProjects().get(1));
        }
        else
        {
            assertEquals(backupProject1, backupOverview.getProjects().get(1));
        }

        assertEquals("123", backupOverview.getBackupSystemInformation().getBuildNumber());
        assertEquals("TestEdition", backupOverview.getBackupSystemInformation().getEdition());
        assertEquals(2, backupOverview.getBackupSystemInformation().getPluginVersions().size());
        assertTrue(backupOverview.getBackupSystemInformation().getPluginVersions().contains(pluginVersion1));
        assertTrue(backupOverview.getBackupSystemInformation().getPluginVersions().contains(pluginVersion2));
    }

    @Test
    public void testSimpleGetCustomFieldConfigurations()
    {
        BackupOverviewBuilderImpl builder = new BackupOverviewBuilderImpl();

        // Setup the custom field stuff
        final String configSchemeId = "123";
        final String customFieldId = "12345";
        final String projectId = "54321";

        final ExternalCustomField externalCustomField = new ExternalCustomField(customFieldId, "Custom Field Name", "CustomFieldTypeKey");
        builder.addExternalCustomField(externalCustomField);

        builder.addConfigurationContext(new BackupOverviewBuilderImpl.ConfigurationContext(configSchemeId, customFieldId, projectId));

        final String issueType = "4";
        builder.addFieldConfigSchemeIssueType(new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType(configSchemeId, issueType));

        final List customFieldConfigs = builder.getCustomFieldConfigurations(projectId);
        assertNotNull(customFieldConfigs);
        // Should be only one
        assertEquals(1, customFieldConfigs.size());
        ExternalCustomFieldConfiguration externalCustomFieldConfiguration = (ExternalCustomFieldConfiguration) customFieldConfigs.get(0);
        // should be our custom field
        assertEquals(externalCustomField, externalCustomFieldConfiguration.getCustomField());
        // should only contain 1 issue type
        assertEquals(1, externalCustomFieldConfiguration.getConstrainedIssueTypes().size());
        assertTrue(externalCustomFieldConfiguration.getConstrainedIssueTypes().contains(issueType));
        // should have the project set
        assertEquals(projectId, externalCustomFieldConfiguration.getConstrainedProjectId());
    }

    @Test
    public void testAdvancedGetCustomFieldConfigurations()
    {
        BackupOverviewBuilderImpl builder = new BackupOverviewBuilderImpl();

        String projectXId = "X";
        String projectYId = "Y";
        String projectZId = "Z";

        String issueTypeBugId = "Bug";
        String issueTypeImprovementId = "Improvement";
        String issueTypeTaskId = "Task";

        String cfTomId = "Tom";
        final ExternalCustomField cfTom = new ExternalCustomField(cfTomId, "Tom", "TomType");
        String cfDickId = "Dick";
        final ExternalCustomField cfDick = new ExternalCustomField(cfDickId, "Dick", "DickType");
        String cfHarryId = "Harry";
        final ExternalCustomField cfHarry = new ExternalCustomField(cfHarryId, "Harry", "HarryType");
        String cfMaryId = "Mary";
        final ExternalCustomField cfMary = new ExternalCustomField(cfMaryId, "Mary", "MaryType");

        // Setup the tom field and its configs
        builder.addExternalCustomField(cfTom);
        // First config for project X and issue type Bug & Task
        builder.addConfigurationContext(new BackupOverviewBuilderImpl.ConfigurationContext("1", cfTomId, projectXId));
        builder.addFieldConfigSchemeIssueType(new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType("1", issueTypeBugId));
        builder.addFieldConfigSchemeIssueType(new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType("1", issueTypeTaskId));

        // Second config for global with issue type Improvement & Task
        builder.addConfigurationContext(new BackupOverviewBuilderImpl.ConfigurationContext("2", cfTomId, null));
        builder.addFieldConfigSchemeIssueType(new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType("2", issueTypeTaskId));
        builder.addFieldConfigSchemeIssueType(new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType("2", issueTypeImprovementId));

        // Setup the dick field and its configs
        builder.addExternalCustomField(cfDick);
        // First config for project Y & Z with global issue types
        builder.addConfigurationContext(new BackupOverviewBuilderImpl.ConfigurationContext("3", cfDickId, projectYId));
        builder.addConfigurationContext(new BackupOverviewBuilderImpl.ConfigurationContext("3", cfDickId, projectZId));
        builder.addFieldConfigSchemeIssueType(new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType("3", null));
        // Second config global with issue type task
        builder.addConfigurationContext(new BackupOverviewBuilderImpl.ConfigurationContext("4", cfDickId, null));
        builder.addFieldConfigSchemeIssueType(new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType("4", issueTypeTaskId));

        // Setup the harry field and its configs
        builder.addExternalCustomField(cfHarry);
        // Only config for project Y and issue type Bug & Improvement
        builder.addConfigurationContext(new BackupOverviewBuilderImpl.ConfigurationContext("5", cfHarryId, projectYId));
        builder.addFieldConfigSchemeIssueType(new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType("5", issueTypeBugId));
        builder.addFieldConfigSchemeIssueType(new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType("5", issueTypeImprovementId));

        // Setup the mary field, it has no config
        builder.addExternalCustomField(cfMary);

        // Check the configs for the ProjectX
        final List projectXCustomFieldConfigs = builder.getCustomFieldConfigurations(projectXId);
        assertNotNull(projectXCustomFieldConfigs);
        // Should be 2
        assertEquals(2, projectXCustomFieldConfigs.size());
        // Test the first config
        ExternalCustomFieldConfiguration customFieldConfiguration = (ExternalCustomFieldConfiguration) projectXCustomFieldConfigs.get(0);
        assertCustomFieldConfiguration(customFieldConfiguration, cfTom, projectXId, EasyList.build(issueTypeBugId, issueTypeTaskId));
        // Test the second config
        customFieldConfiguration = (ExternalCustomFieldConfiguration) projectXCustomFieldConfigs.get(1);
        assertCustomFieldConfiguration(customFieldConfiguration, cfDick, null, EasyList.build(issueTypeTaskId));

        // Check the configs for the ProjectY
        final List projectYCustomFieldConfigs = builder.getCustomFieldConfigurations(projectYId);
        assertNotNull(projectYCustomFieldConfigs);
        // Should be 3
        assertEquals(3, projectYCustomFieldConfigs.size());
        // Test the first config
        customFieldConfiguration = (ExternalCustomFieldConfiguration) projectYCustomFieldConfigs.get(0);
        assertCustomFieldConfiguration(customFieldConfiguration, cfTom, null, EasyList.build(issueTypeImprovementId, issueTypeTaskId));
        // Test the second config
        customFieldConfiguration = (ExternalCustomFieldConfiguration) projectYCustomFieldConfigs.get(1);
        assertCustomFieldConfiguration(customFieldConfiguration, cfDick, projectYId, null);
        // Test the third config
        customFieldConfiguration = (ExternalCustomFieldConfiguration) projectYCustomFieldConfigs.get(2);
        assertCustomFieldConfiguration(customFieldConfiguration, cfHarry, projectYId, EasyList.build(issueTypeBugId, issueTypeImprovementId));

        // Check the configs for the ProjectZ
        final List projectZCustomFieldConfigs = builder.getCustomFieldConfigurations(projectZId);
        assertNotNull(projectZCustomFieldConfigs);
        // Should be 2
        assertEquals(2, projectZCustomFieldConfigs.size());
        // Test the first config
        customFieldConfiguration = (ExternalCustomFieldConfiguration) projectZCustomFieldConfigs.get(0);
        assertCustomFieldConfiguration(customFieldConfiguration, cfTom, null, EasyList.build(issueTypeImprovementId, issueTypeTaskId));
        // Test the second config
        customFieldConfiguration = (ExternalCustomFieldConfiguration) projectZCustomFieldConfigs.get(1);
        assertCustomFieldConfiguration(customFieldConfiguration, cfDick, projectZId, null);
    }

    private void assertCustomFieldConfiguration(ExternalCustomFieldConfiguration actualCustomFieldConfig, ExternalCustomField customField, String projectId, List /*<String>*/ issueTypes)
    {
        assertEquals(customField, actualCustomFieldConfig.getCustomField());
        assertEquals(projectId, actualCustomFieldConfig.getConstrainedProjectId());
        if (issueTypes == null)
        {
            assertTrue(actualCustomFieldConfig.isForAllIssueTypes());
            assertNull(actualCustomFieldConfig.getConstrainedIssueTypes());
        }
        else
        {
            assertEquals(issueTypes.size(), actualCustomFieldConfig.getConstrainedIssueTypes().size());
            assertTrue(actualCustomFieldConfig.getConstrainedIssueTypes().containsAll(issueTypes));
        }
    }

}
