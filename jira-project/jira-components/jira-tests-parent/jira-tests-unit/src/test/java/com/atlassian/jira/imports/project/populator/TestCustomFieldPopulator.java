package com.atlassian.jira.imports.project.populator;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl;
import com.atlassian.jira.imports.project.parser.CustomFieldParser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestCustomFieldPopulator
{
    @Test
    public void testCustomFieldElement() throws ParseException
    {
        final Map attributes = EasyMap.build();

        final Mock mockCustomFieldParser = new Mock(CustomFieldParser.class);
        mockCustomFieldParser.setStrict(true);
        ExternalCustomField externalCustomField = new ExternalCustomField("10000", "TestCF", "text");
        mockCustomFieldParser.expectAndReturn("parseCustomField", P.ANY_ARGS, externalCustomField);
        CustomFieldPopulator CustomFieldPopulator = new CustomFieldPopulator()
        {
            CustomFieldParser getCustomFieldParser()
            {
                return (CustomFieldParser) mockCustomFieldParser.proxy();
            }
        };

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("addExternalCustomField", P.args(P.eq(externalCustomField)));

        CustomFieldPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "CustomField", attributes);

        mockBackupOverviewBuilder.verify();
        mockCustomFieldParser.verify();
    }

    @Test
    public void testCustomFieldElementNotParsable()
    {
        final Mock mockCustomFieldParser = new Mock(CustomFieldParser.class);
        mockCustomFieldParser.setStrict(true);
        mockCustomFieldParser.expectAndThrow("parseCustomField", P.ANY_ARGS, new ParseException("Hello world"));
        CustomFieldPopulator CustomFieldPopulator = new CustomFieldPopulator()
        {
            CustomFieldParser getCustomFieldParser()
            {
                return (CustomFieldParser) mockCustomFieldParser.proxy();
            }
        };
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);

        try
        {
            CustomFieldPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "CustomField", EasyMap.build());
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
        mockCustomFieldParser.verify();
    }

    @Test
    public void testConfigurationContextElement() throws ParseException
    {
        final Map attributes = EasyMap.build();

        final Mock mockCustomFieldParser = new Mock(CustomFieldParser.class);
        mockCustomFieldParser.setStrict(true);
        BackupOverviewBuilderImpl.ConfigurationContext configurationContext = new BackupOverviewBuilderImpl.ConfigurationContext("10000", "10001", "10002");
        mockCustomFieldParser.expectAndReturn("parseCustomFieldConfiguration", P.ANY_ARGS, configurationContext);
        CustomFieldPopulator CustomFieldPopulator = new CustomFieldPopulator()
        {
            CustomFieldParser getCustomFieldParser()
            {
                return (CustomFieldParser) mockCustomFieldParser.proxy();
            }
        };

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("addConfigurationContext", P.args(P.eq(configurationContext)));

        CustomFieldPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "ConfigurationContext", attributes);

        mockBackupOverviewBuilder.verify();
        mockCustomFieldParser.verify();
    }

    @Test
    public void testConfigurationContextElementNotParsable()
    {
        final Mock mockCustomFieldParser = new Mock(CustomFieldParser.class);
        mockCustomFieldParser.setStrict(true);
        mockCustomFieldParser.expectAndThrow("parseCustomFieldConfiguration", P.ANY_ARGS, new ParseException("Hello world"));
        CustomFieldPopulator CustomFieldPopulator = new CustomFieldPopulator()
        {
            CustomFieldParser getCustomFieldParser()
            {
                return (CustomFieldParser) mockCustomFieldParser.proxy();
            }
        };
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);

        try
        {
            CustomFieldPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "ConfigurationContext", EasyMap.build());
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
        mockCustomFieldParser.verify();
    }

    @Test
    public void testConfigurationContextElementNull() throws ParseException
    {
        final Mock mockCustomFieldParser = new Mock(CustomFieldParser.class);
        mockCustomFieldParser.setStrict(true);
        mockCustomFieldParser.expectAndReturn("parseCustomFieldConfiguration", P.ANY_ARGS, null);
        CustomFieldPopulator CustomFieldPopulator = new CustomFieldPopulator()
        {
            CustomFieldParser getCustomFieldParser()
            {
                return (CustomFieldParser) mockCustomFieldParser.proxy();
            }
        };
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectNotCalled("addConfigurationContext");

        CustomFieldPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "ConfigurationContext", EasyMap.build());
        mockBackupOverviewBuilder.verify();
        mockCustomFieldParser.verify();
    }

    @Test
    public void testFieldConfigSchemeIssueTypeElement() throws ParseException
    {
        final Map attributes = EasyMap.build();

        final Mock mockCustomFieldParser = new Mock(CustomFieldParser.class);
        mockCustomFieldParser.setStrict(true);
        BackupOverviewBuilderImpl.FieldConfigSchemeIssueType fieldConfigSchemeIssueType = new BackupOverviewBuilderImpl.FieldConfigSchemeIssueType("10000", "Bug");
        mockCustomFieldParser.expectAndReturn("parseFieldConfigSchemeIssueType", P.ANY_ARGS, fieldConfigSchemeIssueType);
        CustomFieldPopulator CustomFieldPopulator = new CustomFieldPopulator()
        {
            CustomFieldParser getCustomFieldParser()
            {
                return (CustomFieldParser) mockCustomFieldParser.proxy();
            }
        };

        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.setStrict(true);
        mockBackupOverviewBuilder.expectVoid("addFieldConfigSchemeIssueType", P.args(P.eq(fieldConfigSchemeIssueType)));

        CustomFieldPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "FieldConfigSchemeIssueType", attributes);

        mockBackupOverviewBuilder.verify();
        mockCustomFieldParser.verify();
    }

    @Test
    public void testFieldConfigSchemeIssueTypeElementNotParsable()
    {
        final Mock mockCustomFieldParser = new Mock(CustomFieldParser.class);
        mockCustomFieldParser.setStrict(true);
        mockCustomFieldParser.expectAndThrow("parseFieldConfigSchemeIssueType", P.ANY_ARGS, new ParseException("Hello world"));
        CustomFieldPopulator CustomFieldPopulator = new CustomFieldPopulator()
        {
            CustomFieldParser getCustomFieldParser()
            {
                return (CustomFieldParser) mockCustomFieldParser.proxy();
            }
        };
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);

        try
        {
            CustomFieldPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "FieldConfigSchemeIssueType", EasyMap.build());
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
        mockCustomFieldParser.verify();
    }

    @Test
    public void testNonCustomFieldElement() throws ParseException
    {
        CustomFieldPopulator CustomFieldPopulator = new CustomFieldPopulator();
        Mock mockBackupOverviewBuilder = new Mock(BackupOverviewBuilder.class);
        mockBackupOverviewBuilder.expectNotCalled("addExternalCustomField");

        CustomFieldPopulator.populate((BackupOverviewBuilder) mockBackupOverviewBuilder.proxy(), "NotCustomField", EasyMap.build("id", "10000", "customfieldtypekey", "textCF", "name", "TomCF"));
        mockBackupOverviewBuilder.verify();
    }
    
}
