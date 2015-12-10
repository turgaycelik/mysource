package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.mapper.IssueLinkTypeMapper;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeImpl;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestIssueLinkTypeMapperValidatorImpl
{
    @Mock IssueLinkTypeManager issueLinkTypeManager;
    @Mock ApplicationProperties applicationProperties;
    @Mock SubTaskManager subTaskManager;

    IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();

    @After
    public void tearDown()
    {
        issueLinkTypeManager = null;
        applicationProperties = null;
        subTaskManager = null;
        issueLinkTypeMapper = null;
    }

    @Test
    public void testValidateMappingsNoneRequired()
    {
        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(null, null, null);
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidateMappingsNotMappedMissingLinkType()
    {
        withIssueLinking();
        when(issueLinkTypeManager.getIssueLinkTypesByName("Related To")).thenReturn(ImmutableList.<IssueLinkType>of());
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' is required for the import but does not exist in the current JIRA instance.");
    }

    @Test
    public void testValidateMappingsLinksNotEnabled()
    {
        when(issueLinkTypeManager.getIssueLinkTypesByName("Related To")).thenReturn(ImmutableList.of(
                issueLinkType("56", "Related To", null) ));
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "Issue Linking must be enabled because there are issue links in the project to import.");

        verifyZeroInteractions(subTaskManager);
    }

    @Test
    public void testValidateMappingsNotMappedSubtasksNotEnabled()
    {
        withIssueLinking();
        when(issueLinkTypeManager.getIssueLinkTypesByName("Related To")).thenReturn(ImmutableList.of(
                issueLinkType("56", "Related To", "jira_subtask")));
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "jira_subtask");

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The project to import includes subtasks, but subtasks are disabled in the current system.");
    }

    @Test
    public void testValidateMappingsNotMappedOldTypeHasStyleSubtaskNotNew()
    {
        withIssueLinking();
        withSubTasks();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "jira_subtask");
        when(issueLinkTypeManager.getIssueLinkTypesByName("Related To")).thenReturn(ImmutableList.of(
                issueLinkType("56", "Related To", null)));

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has style 'jira_subtask' in the backup, but has no style in the current system.");
    }

    @Test
    public void testValidateMappingsNotMappedOldTypeHasNoStyleNewDoes()
    {
        withIssueLinking();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);

        when(issueLinkTypeManager.getIssueLinkTypesByName("Related To")).thenReturn(ImmutableList.of(
                issueLinkType("56", "Related To", "Styling")));

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has no style value in the backup, but has style 'Styling' in the current system.");

        verifyZeroInteractions(subTaskManager);
    }

    @Test
    public void testValidateMappingsNotMappedDifferentStyles()
    {
        withIssueLinking();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "Rum n Raisin");

        when(issueLinkTypeManager.getIssueLinkTypesByName("Related To")).thenReturn(ImmutableList.of(
                issueLinkType("56", "Related To", "Cookies n Cream")));

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has style 'Rum n Raisin' in the backup, but has style 'Cookies n Cream' in the current system.");
    }

    @Test
    public void testValidateMappingsNotMappedMappingMissing()
    {
        withIssueLinking();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);

        when(issueLinkTypeManager.getIssueLinkTypesByName("Related To")).thenReturn(ImmutableList.of(
                issueLinkType("56", "Related To", null)));

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' is required for the import but has not been mapped.");

        verifyZeroInteractions(subTaskManager);
    }

    @Test
    public void testValidateMappingsMappedToNonexistantLinkType()
    {
        withIssueLinking();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);
        // We make an invalid mapping - this link type does not exist in the current system.
        issueLinkTypeMapper.mapValue("12", "78");

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' was mapped to an IssuelinkType ID (78) that does not exist.");

        verifyZeroInteractions(subTaskManager);
    }

    @Test
    public void testValidateMappingsMappedSubtasksNotEnabled()
    {
        withIssueLinking();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "jira_subtask");
        issueLinkTypeMapper.mapValue("12", "56");

        when(issueLinkTypeManager.getIssueLinkType(56L)).thenReturn(issueLinkType("56", "Related To", "jira_subtask"));

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The project to import includes subtasks, but subtasks are disabled in the current system.");
    }

    @Test
    public void testValidateMappingsMappedOldTypeHasStyleSubtaskNotNew()
    {
        withIssueLinking();
        withSubTasks();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "jira_subtask");
        issueLinkTypeMapper.mapValue("12", "56");

        when(issueLinkTypeManager.getIssueLinkType(56L)).thenReturn(issueLinkType("56", "Related To", null));

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has style 'jira_subtask' in the backup, but has no style in the current system.");
    }

    @Test
    public void testValidateMappingsMappedOldTypeHasNoStyleNewDoes()
    {
        withIssueLinking();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);
        issueLinkTypeMapper.mapValue("12", "56");

        when(issueLinkTypeManager.getIssueLinkType(56L)).thenReturn(issueLinkType("56", "Related To", "Styling"));

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has no style value in the backup, but has style 'Styling' in the current system.");

        verifyZeroInteractions(subTaskManager);
    }

    @Test
    public void testValidateMappingsMappedDifferentStyles()
    {
        withIssueLinking();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "Rum n Raisin");
        issueLinkTypeMapper.mapValue("12", "56");

        when(issueLinkTypeManager.getIssueLinkType(56L)).thenReturn(issueLinkType("56", "Related To", "Cookies n Cream"));

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has style 'Rum n Raisin' in the backup, but has style 'Cookies n Cream' in the current system.");
    }

    @Test
    public void testValidateMappingsHappyPath()
    {
        withIssueLinking();

        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);
        issueLinkTypeMapper.mapValue("12", "56");

        when(issueLinkTypeManager.getIssueLinkType(56L)).thenReturn(issueLinkType("56", "Related To", null));

        final IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = fixture();
        final MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject(), issueLinkTypeMapper);
        assertNoMessages(messageSet);

        verifyZeroInteractions(subTaskManager);
    }



    private void withIssueLinking()
    {
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).thenReturn(true);
    }

    private void withSubTasks()
    {
        when(subTaskManager.isSubTasksEnabled()).thenReturn(true);
    }

    private IssueLinkTypeMapperValidatorImpl fixture()
    {
        return new IssueLinkTypeMapperValidatorImpl(issueLinkTypeManager, subTaskManager, applicationProperties);
    }

    private static IssueLinkType issueLinkType(final String id, final String linkname, final String style)
    {
        final FieldMap fields = FieldMap.build(
                "id", id,
                "linkname", linkname,
                "style", style );
        return new IssueLinkTypeImpl(new MockGenericValue("IssueLinkType", fields));
    }

    private static BackupProject backupProject()
    {
        return new BackupProjectImpl(
                new ExternalProject(),
                ImmutableList.<ExternalVersion>of(),
                ImmutableList.<ExternalComponent>of(),
                ImmutableList.<ExternalCustomFieldConfiguration>of(),
                ImmutableList.<Long>of());
    }
}
