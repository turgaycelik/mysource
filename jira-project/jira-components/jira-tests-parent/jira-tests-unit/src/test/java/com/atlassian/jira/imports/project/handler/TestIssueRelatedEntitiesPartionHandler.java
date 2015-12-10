package com.atlassian.jira.imports.project.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyImpl;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.parser.EntityPropertyParser;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.model.MockModelEntity;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test of IssueRelatedEntitiesPartionHandler.
 * <p/>
 * The name of this test deliberately repeats the typo in the name of the production class, so that IDEs can easily jump
 * between the two.
 *
 * @since v3.13
 */
public class TestIssueRelatedEntitiesPartionHandler
{
    private static final List<ExternalComponent> NO_COMPONENTS = emptyList();
    private static final List<ExternalCustomFieldConfiguration> NO_CUSTOM_FIELD_CONFIGURATIONS = emptyList();
    private static final List<ExternalVersion> NO_VERSIONS = emptyList();
    private static final String NL = System.getProperty("line.separator");
    public static final long CREATED_TS_OFFSET = 2000l;
    public static final long UPDATED_TS_OFFSET = 3000l;
    @Mock
    private DelegatorInterface mockDelegatorInterface;

    /**
     * Creates an immutable map from the given keys and values. Required because Guava only goes up to five key-value
     * pairs.
     *
     * @param keysAndValues alternating keys and values, must be an even length array
     * @param <T> the type of key and value
     * @return a non-null map
     */
    private static <T> Map<T, T> immutableMapOf(final T... keysAndValues)
    {
        assertEquals("Expected an even length but was " + keysAndValues.length, 0, keysAndValues.length % 2);
        final Map<T, T> map = new LinkedHashMap<T, T>();
        for (int i = 0; i < keysAndValues.length - 1; i += 2)
        {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        when(mockDelegatorInterface.getDelegatorName()).thenReturn("default");
    }

    @Test
    public void testChangeItemGroupParseExceptions()
    {
        // Set up a mock ChangeGroup
        // <ChangeGroup id="10033" issue="10000" author="admin" created="2008-01-24 16:03:24.325"/>
        final MockModelEntity mockChangeGroupModelEntity = new MockModelEntity("ChangeGroup");
        mockChangeGroupModelEntity.setFieldNames(asList("id", "issue", "author", "created"));
        // <ChangeItem id="10000" group="10000" fieldtype="jira" field="Project" oldvalue="10000" oldstring="homosapien" newvalue="10001" newstring="monkey"/>
        final MockModelEntity mockChangeItemModelEntity = new MockModelEntity("ChangeItem");
        mockChangeItemModelEntity.setFieldNames(
                asList("id", "group", "fieldtype", "field", "oldvalue", "oldstring", "newvalue", "newstring", "xxx"));

        // Create a simple PrintWriter
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        // Create a simple PrintWriter
        final StringWriter changeItem = new StringWriter();
        final PrintWriter changeItemPrintWriter = new PrintWriter(changeItem);

        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), NO_VERSIONS, NO_COMPONENTS,
                NO_CUSTOM_FIELD_CONFIGURATIONS, asList(12L, 14L));

        // Create our handler
        final IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(
                backupProject, printWriter, changeItemPrintWriter,
                asList(mockChangeGroupModelEntity, mockChangeItemModelEntity), "UTF-8", mockDelegatorInterface);
        // Now fire XML parse events at it to handle.
        issueRelatedEntitiesPartionHandler.startDocument();
        try
        {
            issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup", ImmutableMap.of(
                    "id", "abc", "issue", "12", "author", "dudette", "created", "2009-04-01 12:34:56.789"));
            fail("A parse exception should have been thrown.");
        }
        catch (final ParseException e)
        {
            // expected
        }
        // Test a change group with no id
        try
        {
            issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup",
                    ImmutableMap.of("issue", "12", "author", "dudette", "created", "2009-04-01 12:34:56.789"));
            fail("A parse exception should have been thrown.");
        }
        catch (final ParseException e)
        {
            // expected
        }
        try
        {
            issueRelatedEntitiesPartionHandler.handleEntity(
                    "ChangeItem", ImmutableMap.of("id", "10501", "group", "def", "fieldtype", "jira", "field", "A"));
            fail("A parse exception should have been thrown.");
        }
        catch (final ParseException e)
        {
            //expected
        }
    }

    @Test
    public void testNodeAssociationOnly() throws ParseException
    {
        // Set up a mock ModelEntity
        //    <NodeAssociation sourceNodeId="10001" sourceNodeEntity="Issue" sinkNodeId="10002" sinkNodeEntity="Version" associationType="IssueFixVersion"/>
        //    <NodeAssociation sourceNodeId="10001" sourceNodeEntity="Issue" sinkNodeId="10001" sinkNodeEntity="Component" associationType="IssueComponent"/>
        //    <NodeAssociation sourceNodeId="10001" sourceNodeEntity="Issue" sinkNodeId="10001" sinkNodeEntity="Version" associationType="IssueVersion"/>
        final MockModelEntity mockModelEntity = new MockModelEntity("NodeAssociation");
        mockModelEntity.setFieldNames(
                asList("sourceNodeId", "sourceNodeEntity", "sinkNodeId", "sinkNodeEntity", "associationType"));

        // Create a simple PrintWriter
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        final BackupProject backupProject = new BackupProjectImpl(
                new ExternalProject(), NO_VERSIONS, NO_COMPONENTS, NO_CUSTOM_FIELD_CONFIGURATIONS, asList(12L, 14L));

        // Create our handler
        final IssueRelatedEntitiesPartionHandler handler = new IssueRelatedEntitiesPartionHandler(
                backupProject, printWriter, null, asList(mockModelEntity), "UTF-8", mockDelegatorInterface);

        // Now fire XML parse events at it to handle
        handler.startDocument();
        handler.handleEntity(
                "Issue", ImmutableMap.of("id", "12", "key", "MNK-16", "desc", "Stuff happened."));
        // This is a Affects Version node
        handler.handleEntity("NodeAssociation", ImmutableMap.of(
                "sourceNodeId", "12", "sourceNodeEntity", "Issue", "sinkNodeId", "123", "sinkNodeEntity", "Version", "associationType", "IssueVersion"));
        // Fix version for the same issue
        handler.handleEntity("NodeAssociation", ImmutableMap.of(
                "sourceNodeId", "12", "sourceNodeEntity", "Issue", "sinkNodeId", "124", "sinkNodeEntity", "Version", "associationType", "IssueFixVersion"));
        // Component for different issue
        handler.handleEntity("NodeAssociation", ImmutableMap.of(
                "sourceNodeId", "14", "sourceNodeEntity", "Issue", "sinkNodeId", "125", "sinkNodeEntity", "Component", "associationType", "IssueComponent"));
        // This should be ignored, wrong association type
        handler.handleEntity("NodeAssociation", ImmutableMap.of(
                "sourceNodeId", "14", "sourceNodeEntity", "Issue", "sinkNodeId", "125", "sinkNodeEntity", "Component", "associationType", "WrongStuff"));
        // This should be ignored, wrong sourceEntityType
        handler.handleEntity("NodeAssociation", ImmutableMap.of(
                "sourceNodeId", "14", "sourceNodeEntity", "WrongType", "sinkNodeId", "125", "sinkNodeEntity", "Component", "associationType", "IssueComponent"));
        // This should be ignored, not related to our issues
        handler.handleEntity("NodeAssociation", ImmutableMap.of(
                "sourceNodeId", "15", "sourceNodeEntity", "Issue", "sinkNodeId", "125", "sinkNodeEntity", "Component", "associationType", "IssueComponent"));

        handler.endDocument();

        printWriter.close();

        // Check
        final String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<entity-engine-xml>" + NL
                + "    <NodeAssociation sourceNodeId=\"12\" sourceNodeEntity=\"Issue\" sinkNodeId=\"123\" sinkNodeEntity=\"Version\" associationType=\"IssueVersion\"/>" + NL
                + "    <NodeAssociation sourceNodeId=\"12\" sourceNodeEntity=\"Issue\" sinkNodeId=\"124\" sinkNodeEntity=\"Version\" associationType=\"IssueFixVersion\"/>" + NL
                + "    <NodeAssociation sourceNodeId=\"14\" sourceNodeEntity=\"Issue\" sinkNodeId=\"125\" sinkNodeEntity=\"Component\" associationType=\"IssueComponent\"/>" + NL
                + "</entity-engine-xml>", xml);
        assertEquals(3, handler.getEntityCount());
    }

    @Test
    public void testIssueLinkOnly() throws ParseException
    {
        // Set up a mock ModelEntity
        //     <IssueLink id="10000" linktype="10000" source="10000" destination="10001"/>
        final MockModelEntity mockModelEntity = new MockModelEntity("IssueLink");
        mockModelEntity.setFieldNames(asList("id", "linktype", "source", "destination"));

        // Create a simple PrintWriter
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), NO_VERSIONS, NO_COMPONENTS,
                NO_CUSTOM_FIELD_CONFIGURATIONS, asList(12L, 14L));

        // Create our handler
        final IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(
                backupProject, printWriter, null, asList(mockModelEntity), "UTF-8", mockDelegatorInterface);
        // Now fire XML parse events at it to handle.
        issueRelatedEntitiesPartionHandler.startDocument();
        issueRelatedEntitiesPartionHandler.handleEntity(
                "Issue", ImmutableMap.of("id", "12", "key", "MNK-16", "desc", "Stuff happened."));
        // For this link neither issue is in the project
        issueRelatedEntitiesPartionHandler.handleEntity(
                "IssueLink", ImmutableMap.of("id", "10000", "linktype", "10000", "source", "10", "destination", "16"));
        // Source is in the project.
        issueRelatedEntitiesPartionHandler.handleEntity(
                "IssueLink", ImmutableMap.of("id", "10001", "linktype", "10000", "source", "12", "destination", "10"));
        // Destination in the project
        issueRelatedEntitiesPartionHandler.handleEntity(
                "IssueLink", ImmutableMap.of("id", "10002", "linktype", "10002", "source", "44", "destination", "14"));
        // Both source and destination in the project.
        issueRelatedEntitiesPartionHandler.handleEntity(
                "IssueLink", ImmutableMap.of("id", "10003", "linktype", "10002", "source", "12", "destination", "14"));
        issueRelatedEntitiesPartionHandler.endDocument();

        printWriter.close();
        final String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<entity-engine-xml>" + NL
                + "    <IssueLink id=\"10001\" linktype=\"10000\" source=\"12\" destination=\"10\"/>" + NL
                + "    <IssueLink id=\"10002\" linktype=\"10002\" source=\"44\" destination=\"14\"/>" + NL
                + "    <IssueLink id=\"10003\" linktype=\"10002\" source=\"12\" destination=\"14\"/>" + NL
                + "</entity-engine-xml>", xml);
    }

    @Test
    public void testCustomFieldValue() throws ParseException
    {
        //     <CustomFieldValue id="10000" issue="10010" customfield="10000" stringvalue="Future"/>
        final MockModelEntity mockModelEntity = new MockModelEntity("CustomFieldValue");
        mockModelEntity.setFieldNames(asList("id", "issue", "customfield", "stringvalue"));

        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        final BackupProject mockBackupProject = mock(BackupProject.class);
        when(mockBackupProject.containsIssue("10")).thenReturn(false);
        when(mockBackupProject.containsIssue("12")).thenReturn(true);
        when(mockBackupProject.containsIssue("14")).thenReturn(true);
        when(mockBackupProject.containsIssue("16")).thenReturn(false);

        final IssueRelatedEntitiesPartionHandler customFieldValuePartitonHandler = new IssueRelatedEntitiesPartionHandler(
                mockBackupProject, printWriter, null, asList(mockModelEntity), "UTF-16", mockDelegatorInterface);

        customFieldValuePartitonHandler.startDocument();
        customFieldValuePartitonHandler.handleEntity("CustomFieldValue",
                ImmutableMap.of("id", "10000", "issue", "10", "customfield", "41", "stringvalue", "A"));
        customFieldValuePartitonHandler.handleEntity("CustomFieldValue",
                ImmutableMap.of("id", "10001", "issue", "12", "customfield", "43", "stringvalue", "B"));
        customFieldValuePartitonHandler.handleEntity("CustomFieldValue",
                ImmutableMap.of("id", "10002", "issue", "14", "customfield", "44", "stringvalue", "This one is a has a new-line" + NL + "There - told you so."));
        customFieldValuePartitonHandler.handleEntity("CustomFieldValue",
                ImmutableMap.of("id", "10003", "issue", "16", "customfield", "46", "stringvalue", "D"));
        customFieldValuePartitonHandler.handleEntity("Issue",
                ImmutableMap.of("id", "12", "key", "MNK-16", "desc", "Stuff happened."));
        customFieldValuePartitonHandler.endDocument();

        printWriter.close();
        final String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-16\"?>" + NL
                + "<entity-engine-xml>" + NL
                + "    <CustomFieldValue id=\"10001\" issue=\"12\" customfield=\"43\" stringvalue=\"B\"/>" + NL
                + "    <CustomFieldValue id=\"10002\" issue=\"14\" customfield=\"44\">" + NL
                + "        <stringvalue><![CDATA[This one is a has a new-line" + NL
                + "There - told you so.]]></stringvalue>" + NL
                + "    </CustomFieldValue>" + NL
                + "</entity-engine-xml>", xml);
    }

    @Test
    public void testChangeItemsAndChangeGroups() throws ParseException
    {
        // Set up a mock ChangeGroup
        // <ChangeGroup id="10033" issue="10000" author="admin" created="2008-01-24 16:03:24.325"/>
        final MockModelEntity mockChangeGroupModelEntity = new MockModelEntity("ChangeGroup");
        mockChangeGroupModelEntity.setFieldNames(asList("id", "issue", "author", "created"));
        // <ChangeItem id="10000" group="10000" fieldtype="jira" field="Project" oldvalue="10000" oldstring="homosapien" newvalue="10001" newstring="monkey"/>
        final MockModelEntity mockChangeItemModelEntity = new MockModelEntity("ChangeItem");
        mockChangeItemModelEntity.setFieldNames(asList(
                "id", "group", "fieldtype", "field", "oldvalue", "oldstring", "newvalue", "newstring", "xxx"));

        // Create a simple PrintWriter
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        // Create a simple PrintWriter
        final StringWriter changeItem = new StringWriter();
        final PrintWriter changeItemPrintWriter = new PrintWriter(changeItem);

        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), NO_VERSIONS, NO_COMPONENTS,
                NO_CUSTOM_FIELD_CONFIGURATIONS, asList(12L, 14L));

        // Create our handler
        final IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(
                backupProject, printWriter, changeItemPrintWriter,
                asList(mockChangeGroupModelEntity, mockChangeItemModelEntity), "UTF-8", mockDelegatorInterface);
        // Now fire XML parse events at it to handle.
        issueRelatedEntitiesPartionHandler.startDocument();
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup",
                ImmutableMap.of("id", "10033", "issue", "10", "author", "dude", "created", "2008-04-01 12:34:56.789"));
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup",
                ImmutableMap.of("id", "10034", "issue", "12", "author", "dudette", "created", "2009-04-01 12:34:56.789"));
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup",
                ImmutableMap.of("id", "10035", "issue", "14", "author", "dude", "created", "2010-04-01 12:34:56.789"));
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup",
                ImmutableMap.of("id", "10036", "issue", "16", "author", "dudette", "created", "2011-04-01 12:34:56.789"));
        // Fire some Change Items - we should only include ones that have ChangeGroups that we stored.
        final Map<String, String> attributes = immutableMapOf(
                "id", "10501", "group", "10033", "fieldtype", "jira", "field", "A", "oldvalue", "10000", "oldstring", "level1", "newvalue", "10001", "newstring", "level2");
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeItem", attributes);
        final Map<String, String> attributes1 = immutableMapOf(
                "id", "10502", "group", "10034", "fieldtype", "jira", "field", "B", "oldvalue", "10000", "oldstring", "level1", "newvalue", "10001", "newstring", "level2");
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeItem", attributes1);
        final Map<String, String> attributes2 = immutableMapOf(
                "id", "10503", "group", "10035", "fieldtype", "jira", "field", "C", "oldvalue", "10000", "oldstring", "level1", "newstring", "level2", "newvalue", "10001");
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeItem", attributes2);
        final Map<String, String> attributes3 = immutableMapOf(
                "id", "10504", "group", "10036", "fieldtype", "jira", "field", "B", "oldvalue", "10000", "oldstring", "level1", "newvalue", "10001", "newstring", "level2");
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeItem", attributes3);

        issueRelatedEntitiesPartionHandler.endDocument();

        printWriter.close();
        final String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<entity-engine-xml>" + NL
                + "    <ChangeGroup id=\"10034\" issue=\"12\" author=\"dudette\" created=\"2009-04-01 12:34:56.789\"/>" + NL
                + "    <ChangeGroup id=\"10035\" issue=\"14\" author=\"dude\" created=\"2010-04-01 12:34:56.789\"/>" + NL
                + "</entity-engine-xml>", xml);

        changeItemPrintWriter.close();
        final String changeItemXml = changeItem.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<entity-engine-xml>" + NL
                + "    <ChangeItem id=\"10502\" group=\"10034\" fieldtype=\"jira\" field=\"B\" oldvalue=\"10000\" oldstring=\"level1\" newvalue=\"10001\" newstring=\"level2\"/>" + NL
                + "    <ChangeItem id=\"10503\" group=\"10035\" fieldtype=\"jira\" field=\"C\" oldvalue=\"10000\" oldstring=\"level1\" newvalue=\"10001\" newstring=\"level2\"/>" + NL
                + "</entity-engine-xml>", changeItemXml);
    }

    @Test
    public void testEntityProperties() throws ParseException
    {
        //setup mock entity property
        // Set up a mock ChangeGroup
        final MockModelEntity mockEntityPropertiesModelEntity = new MockModelEntity(EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME);
        mockEntityPropertiesModelEntity.setFieldNames(asList(EntityProperty.ID, EntityProperty.ENTITY_NAME,
                EntityProperty.ENTITY_ID, EntityProperty.KEY, EntityProperty.VALUE, EntityProperty.CREATED, EntityProperty.UPDATED));

        // Create a simple PrintWriter
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        final ExternalProject project = new ExternalProject();
        project.setId("1004");
        final BackupProject backupProject = new BackupProjectImpl(project, NO_VERSIONS, NO_COMPONENTS, NO_CUSTOM_FIELD_CONFIGURATIONS, asList(1001L));

        // Create our handler
        final IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(
                backupProject, printWriter, null, asList(mockEntityPropertiesModelEntity), "UTF-8", mockDelegatorInterface);

        // Now fire XML parse events at it to handle.
        issueRelatedEntitiesPartionHandler.startDocument();

        issueRelatedEntitiesPartionHandler.handleEntity(EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME,
                getAttributesOfEntityProperty(1l, EntityPropertyType.ISSUE_PROPERTY.getDbEntityName()));
        issueRelatedEntitiesPartionHandler.handleEntity(EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME,
                getAttributesOfEntityProperty(2l, EntityPropertyType.ISSUE_PROPERTY.getDbEntityName()));
        issueRelatedEntitiesPartionHandler.handleEntity(EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME,
                getAttributesOfEntityProperty(3l, EntityPropertyType.PROJECT_PROPERTY.getDbEntityName()));
        issueRelatedEntitiesPartionHandler.handleEntity(EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME,
                getAttributesOfEntityProperty(4l, EntityPropertyType.PROJECT_PROPERTY.getDbEntityName()));


        issueRelatedEntitiesPartionHandler.endDocument();

        printWriter.close();
        final String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<entity-engine-xml>" + NL
                + "    <EntityProperty id=\"1\" entityName=\"IssueProperty\" entityId=\"1001\" propertyKey=\"key1\" value=\"value1\" created=\""+new Timestamp(CREATED_TS_OFFSET + 1l).toString()+"\" updated=\""+new Timestamp(UPDATED_TS_OFFSET + 1l).toString()+"\"/>" + NL
                + "</entity-engine-xml>", xml);


    }

    @Test
    public void testChangeHistoryProperties() throws Exception
    {
        // Set up a mock ChangeGroup
        final MockModelEntity mockChangeGroupModelEntity = new MockModelEntity("ChangeGroup");
        mockChangeGroupModelEntity.setFieldNames(asList("id", "issue", "author", "created"));

        //setup mock entity property
        final MockModelEntity mockEntityPropertiesModelEntity = new MockModelEntity(EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME);
        mockEntityPropertiesModelEntity.setFieldNames(asList(EntityProperty.ID, EntityProperty.ENTITY_NAME,
                EntityProperty.ENTITY_ID, EntityProperty.KEY, EntityProperty.VALUE, EntityProperty.CREATED, EntityProperty.UPDATED));

        // Create a simple PrintWriter
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        final ExternalProject project = new ExternalProject();
        project.setId("1004");
        final BackupProject backupProject = new BackupProjectImpl(project, NO_VERSIONS, NO_COMPONENTS, NO_CUSTOM_FIELD_CONFIGURATIONS, asList(10L));

        // Create our handler
        final IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(
                backupProject, new PrintWriter(new StringWriter()), printWriter, asList(mockEntityPropertiesModelEntity, mockChangeGroupModelEntity), "UTF-8", mockDelegatorInterface);

        // Now fire XML parse events at it to handle.
        issueRelatedEntitiesPartionHandler.startDocument();

        issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup",
                ImmutableMap.of("id", "1002", "issue", "10", "author", "dude", "created", "2008-04-01 12:34:56.789"));
        issueRelatedEntitiesPartionHandler.handleEntity(EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME,
                getAttributesOfEntityProperty(1l, EntityPropertyType.CHANGE_HISTORY_PROPERTY.getDbEntityName())); // shouldn't be included as the referenced ChangeGroup (1001) not in the project
        issueRelatedEntitiesPartionHandler.handleEntity(EntityPropertyParser.ENTITY_PROPERTY_ENTITY_NAME,
                getAttributesOfEntityProperty(2l, EntityPropertyType.CHANGE_HISTORY_PROPERTY.getDbEntityName())); // should be included as the referenced ChangeGroup (1002) is in the project

        issueRelatedEntitiesPartionHandler.endDocument();

        printWriter.close();
        final String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<entity-engine-xml>" + NL
                + "    <EntityProperty id=\"2\" entityName=\"ChangeHistoryProperty\" entityId=\"1002\" propertyKey=\"key2\" value=\"value2\" created=\""+new Timestamp(CREATED_TS_OFFSET + 2l).toString()+"\" updated=\""+new Timestamp(UPDATED_TS_OFFSET + 2l).toString()+"\"/>" + NL
                + "</entity-engine-xml>", xml);
    }

    private Map<String, String> getAttributesOfEntityProperty(final long id, final String entityName)
    {
        return Maps.transformValues(Entity.ENTITY_PROPERTY.fieldMapFrom(EntityPropertyImpl.existing(
                id, entityName, 1000l + id, "key" + id, "value" + id, new Timestamp(CREATED_TS_OFFSET + id), new Timestamp(UPDATED_TS_OFFSET + id)
        )), new Function<Object, String>()
        {
            @Override
            public String apply(final Object input)
            {
                return input.toString();
            }
        });
    }

    @Test
    public void testVotersAndWatchers() throws ParseException
    {
        // Set up a mock UserAssociation
        // <UserAssociation sourceName="admin" sinkNodeId="10000" sinkNodeEntity="Issue" associationType="VoteIssue"/>
        final MockModelEntity mockUserAssociationModelEntity = new MockModelEntity("UserAssociation");
        mockUserAssociationModelEntity.setFieldNames(asList("sourceName", "sinkNodeId", "sinkNodeEntity", "associationType"));

        // Create a simple PrintWriter
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), NO_VERSIONS, NO_COMPONENTS,
                NO_CUSTOM_FIELD_CONFIGURATIONS, asList(12L, 14L));

        // Create our handler
        final IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(
                backupProject, printWriter, null, asList(mockUserAssociationModelEntity), "UTF-8", mockDelegatorInterface);
        // Now fire XML parse events at it to handle, fire Voters.
        issueRelatedEntitiesPartionHandler.startDocument();
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation",
                ImmutableMap.of("sourceName", "fee", "sinkNodeId", "10", "sinkNodeEntity", "Issue", "associationType", "VoteIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation",
                ImmutableMap.of("sourceName", "fi", "sinkNodeId", "12", "sinkNodeEntity", "Issue", "associationType", "VoteIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation",
                ImmutableMap.of("sourceName", "fo", "sinkNodeId", "14", "sinkNodeEntity", "Issue", "associationType", "VoteIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation",
                ImmutableMap.of("sourceName", "fum", "sinkNodeId", "16", "sinkNodeEntity", "Issue", "associationType", "VoteIssue"));
        // Fire some Watchers- we should only include ones that have ChangeGroups that we stored.
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation",
                ImmutableMap.of("sourceName", "zee", "sinkNodeId", "10", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation",
                ImmutableMap.of("sourceName", "zi", "sinkNodeId", "12", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation",
                ImmutableMap.of("sourceName", "zo", "sinkNodeId", "14", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation",
                ImmutableMap.of("sourceName", "zum", "sinkNodeId", "16", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));

        // Fire some crap ones
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation",
                ImmutableMap.of("sourceName", "zum", "sinkNodeId", "16", "sinkNodeEntity", "Issue", "associationType", "SomeInvalidType"));
        issueRelatedEntitiesPartionHandler.handleEntity("SomeEntity",
                ImmutableMap.of("sourceName", "zi", "sinkNodeId", "12", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));

        issueRelatedEntitiesPartionHandler.endDocument();

        printWriter.close();
        final String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                + "<entity-engine-xml>" + NL
                + "    <UserAssociation sourceName=\"fi\" sinkNodeId=\"12\" sinkNodeEntity=\"Issue\" associationType=\"VoteIssue\"/>" + NL
                + "    <UserAssociation sourceName=\"fo\" sinkNodeId=\"14\" sinkNodeEntity=\"Issue\" associationType=\"VoteIssue\"/>" + NL
                + "    <UserAssociation sourceName=\"zi\" sinkNodeId=\"12\" sinkNodeEntity=\"Issue\" associationType=\"WatchIssue\"/>" + NL
                + "    <UserAssociation sourceName=\"zo\" sinkNodeId=\"14\" sinkNodeEntity=\"Issue\" associationType=\"WatchIssue\"/>" + NL
                + "</entity-engine-xml>", xml);
    }
}
