package com.atlassian.jira.imports.project.handler;

import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParser;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParserImpl;
import com.atlassian.jira.util.collect.MapBuilder;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;

/**
 * @since v3.13
 */
public class TestCustomFieldMapperHandler
{
    private MapBuilder<String, CustomFieldValueParser> entities = MapBuilder.newBuilder();

    @Before
    public void setUp() throws Exception
    {
        entities.add(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, new CustomFieldValueParserImpl());
    }

    @Test
    public void testCustomFieldValueFlaggedAsInUse() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(33333)));

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("11111", "22222", "33333");
        final Mock mockCustomFieldValueParser = new Mock(CustomFieldValueParser.class);
        mockCustomFieldValueParser.setStrict(true);
        mockCustomFieldValueParser.expectAndReturn("parse", P.ANY_ARGS, externalCustomFieldValue);

        final MockControl mockCustomFieldMapperControl = MockClassControl.createControl(CustomFieldMapper.class);
        final CustomFieldMapper mockCustomFieldMapper = (CustomFieldMapper) mockCustomFieldMapperControl.getMock();
        mockCustomFieldMapper.flagValueAsRequired("22222", "33333");
        mockCustomFieldMapperControl.replay();

        entities.add(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, (CustomFieldValueParser) mockCustomFieldValueParser.proxy());
        CustomFieldMapperHandler customFieldMapperHandler = new CustomFieldMapperHandler(backupProject, mockCustomFieldMapper, entities.toMap());

        customFieldMapperHandler.handleEntity(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, Collections.EMPTY_MAP);

        mockCustomFieldMapperControl.verify();
        mockCustomFieldValueParser.verify();
    }

    @Test
    public void testCustomFieldValueIssueNotInProject() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(66666)));

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("11111", "22222", "33333");
        final Mock mockCustomFieldValueParser = new Mock(CustomFieldValueParser.class);
        mockCustomFieldValueParser.setStrict(true);
        mockCustomFieldValueParser.expectAndReturn("parse", P.ANY_ARGS, externalCustomFieldValue);

        final MockControl mockCustomFieldMapperControl = MockClassControl.createControl(CustomFieldMapper.class);
        final CustomFieldMapper mockCustomFieldMapper = (CustomFieldMapper) mockCustomFieldMapperControl.getMock();
        mockCustomFieldMapperControl.replay();

        entities.add(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, (CustomFieldValueParser) mockCustomFieldValueParser.proxy());
        CustomFieldMapperHandler customFieldMapperHandler = new CustomFieldMapperHandler(backupProject, mockCustomFieldMapper, entities.toMap());

        customFieldMapperHandler.handleEntity(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, Collections.EMPTY_MAP);

        mockCustomFieldMapperControl.verify();
        mockCustomFieldValueParser.verify();
    }

    @Test
    public void testCustomFieldValueWrongEntityType() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(66666)));

        final MockControl mockCustomFieldMapperControl = MockClassControl.createControl(CustomFieldMapper.class);
        final CustomFieldMapper mockCustomFieldMapper = (CustomFieldMapper) mockCustomFieldMapperControl.getMock();
        mockCustomFieldMapperControl.replay();

        CustomFieldMapperHandler customFieldMapperHandler = new CustomFieldMapperHandler(backupProject, mockCustomFieldMapper, entities.toMap());

        customFieldMapperHandler.handleEntity("BSENTITY", Collections.EMPTY_MAP);

        mockCustomFieldMapperControl.verify();
    }
}
