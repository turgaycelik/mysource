package com.atlassian.jira.imports.project.populator;

import java.util.HashMap;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestSystemInfoPopulator
{
    SystemInfoPopulator systemInfoPopulator = new SystemInfoPopulator();

    /**
     * A simple test for populating Build Number.
     * @throws com.atlassian.jira.exception.ParseException ParseException
     */
    @Test
    public void testPopulateBuildNumber() throws ParseException
    {
        // We'll Use Mock Objects here to make Dylan happy ;)
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("setBuildNumber", P.args(P.eq("12345")));
        BackupOverviewBuilder backupOverviewBuilder = (BackupOverviewBuilder) mockBackupOverviewBuilder.proxy();

        // Send some OSPropertyEntry nodes
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "12", "entityName", "jira.properties", "propertyKey", "jira.version.patched"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "14", "entityName", "jira.properties", "propertyKey", "pineapple"));
        // Now send some OSPropertyString nodes (these are the values)
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyString", EasyMap.build("id", "12", "value", "12345"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyString", EasyMap.build("id", "14", "value", "54321"));

        mockBackupOverviewBuilder.verify();
    }

    /**
     * A simple test for populating Edition.
     * @throws com.atlassian.jira.exception.ParseException ParseException
     */
    @Test
    public void testPopulateEdition() throws ParseException
    {
        // Mock out the BackupOverviewBuilder:
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("setEdition", P.args(P.eq("nuclear")));
        BackupOverviewBuilder backupOverviewBuilder = (BackupOverviewBuilder) mockBackupOverviewBuilder.proxy();

        // Send some OSPropertyEntry nodes
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "12", "entityName", "jira.properties", "propertyKey", "jira.edition"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "14", "entityName", "jira.properties", "propertyKey", "pineapple"));
        // Now send some OSPropertyString nodes (these are the values)
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyString", EasyMap.build("id", "12", "value", "nuclear"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyString", EasyMap.build("id", "14", "value", "diesel"));

        mockBackupOverviewBuilder.verify();
    }

    /**
     * A simple test for populating Edition.
     * @throws com.atlassian.jira.exception.ParseException ParseException
     */
    @Test
    public void testUnassignedIssuesAllowed() throws ParseException
    {
        // Mock out the BackupOverviewBuilder:
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("setUnassignedIssuesAllowed", P.args(P.eq(Boolean.FALSE)));
        BackupOverviewBuilder backupOverviewBuilder = (BackupOverviewBuilder) mockBackupOverviewBuilder.proxy();

        // Send some OSPropertyEntry nodes
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "12", "entityName", "jira.properties", "propertyKey", "jira.option.allowunassigned"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "14", "entityName", "jira.properties", "propertyKey", "pineapple"));
        // Now send some OSPropertyString nodes (these are the values)
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyNumber", EasyMap.build("id", "12", "value", "0"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyNumber", EasyMap.build("id", "14", "value", "1"));

        mockBackupOverviewBuilder.verify();
    }

    /**
     * A comprehensive test covering both Edition and Build Number in a much more similar environment to real life.
     * @throws ParseException ParseException
     */
    @Test
    public void testSystemInfoPopulator() throws ParseException
    {
        // Mock out the BackupOverviewBuilder:
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("setEdition", P.args(P.eq("nuclear")));
        mockBackupOverviewBuilder.expectVoid("setBuildNumber", P.args(P.eq("99")));
        mockBackupOverviewBuilder.expectVoid("setUnassignedIssuesAllowed", P.args(P.eq(Boolean.TRUE)));
        BackupOverviewBuilder backupOverviewBuilder = (BackupOverviewBuilder) mockBackupOverviewBuilder.proxy();

        // Send some OSPropertyEntry nodes
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "12", "entityName", "jira.properties", "propertyKey", "jira.edition"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "60", "entityName", "jira.properties", "propertyKey", "jira.version.patched"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "61", "entityName", "jira.properties", "propertyKey", "jira.option.allowunassigned"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyEntry", EasyMap.build("id", "14", "entityName", "jira.properties", "propertyKey", "pineapple"));
        // Send some nodes of other entries
        systemInfoPopulator.populate(backupOverviewBuilder, "Random", EasyMap.build("id", "44", "entityName", "jira.properties", "propertyKey", "jira.edition"));
        systemInfoPopulator.populate(backupOverviewBuilder, "Random", EasyMap.build("id", "14", "value", "diesel"));
        systemInfoPopulator.populate(backupOverviewBuilder, "Random", EasyMap.build("id", "60", "value", "12345"));
        // Now send some OSPropertyString nodes (these are the values)
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyString", EasyMap.build("id", "12", "value", "nuclear"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyString", EasyMap.build("id", "14", "value", "diesel"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyString", EasyMap.build("id", "60", "value", "99"));
        systemInfoPopulator.populate(backupOverviewBuilder, "OSPropertyNumber", EasyMap.build("id", "61", "value", "1"));

        mockBackupOverviewBuilder.verify();
    }

    @Test
    public void testIllegalArgument() throws ParseException
    {
        try
        {
            systemInfoPopulator.populate(new BackupOverviewBuilderImpl(), null, new HashMap());
            fail("IllegalArgumentException expected.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
}
