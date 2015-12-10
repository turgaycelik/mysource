package com.atlassian.jira.issue.fields.config.persistence;

import java.util.Collection;
import java.util.Iterator;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestFieldConfigPersisterImpl
{
    public static final String FIELD_CONFIGURATION_ENTITY_NAME = "FieldConfiguration";

    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private OfBizDelegator delegator = new MockOfBizDelegator();

    @AvailableInContainer
    private FieldManager fieldManager = new MockFieldManager();

    private long fieldConfigIdFirst = 10001L;
    private long fieldConfigIdSecond = 10010L;
    private FieldConfigPersister persister;

    @Before
    public void setUp() throws Exception
    {
        persister = new FieldConfigPersisterImpl(delegator);

        createFieldConfig(fieldConfigIdFirst);
        createFieldConfig(fieldConfigIdSecond);
        createFieldConfig(10100L);
        createFieldConfig(11000L);

        createGenericConfig(10000L, fieldConfigIdFirst);
        createGenericConfig(fieldConfigIdSecond, fieldConfigIdSecond);

        createOptionConfig(10011L, fieldConfigIdFirst);
        createOptionConfig(10012L, fieldConfigIdFirst);
        createOptionConfig(10013L, fieldConfigIdFirst);
        createOptionConfig(10014L, fieldConfigIdSecond);
        createOptionConfig(10015L, fieldConfigIdSecond);
        createOptionConfig(10016L, fieldConfigIdSecond);
        createOptionConfig(10017L, fieldConfigIdSecond);

        createFieldConfigScheme(20001L);
        createFieldConfigScheme(20010L);
        createFieldConfigScheme(20100L);
        createFieldConfigScheme(21000L);

        createFieldConfigSchemeIssueType(30001L, 10001L, 20001L, null);
        createFieldConfigSchemeIssueType(30010L, 10010L, 20010L, null);
        createFieldConfigSchemeIssueType(30100L, 10010L, 20100L, null);
        createFieldConfigSchemeIssueType(31000L, 10100L, 20100L, null);
        createFieldConfigSchemeIssueType(40001L, 11000L, 21000L, "1");
        createFieldConfigSchemeIssueType(40010L, 11000L, 21000L, "2");
    }

    @Test
    public void testFieldConfigPersistentRemoveRemovesDataFromDatabase() throws Exception
    {
        FieldConfig fc1 = persister.getFieldConfig(new Long(fieldConfigIdFirst));
        FieldConfig fc2 = persister.getFieldConfig(new Long(fieldConfigIdSecond));

        persister.remove(fc1);
        persister.remove(fc2);

        assertThat(getRowsCountInFieldConfiguration(), equalTo(2));
    }

    @Test
    public void testRemoveFieldConfigurationsFromSchemeHappyPath() throws Exception
    {
        removeConfigsForConfigScheme(21000L);
        assertEquals(3, getRowsCountInFieldConfiguration());
    }

    @Test
    public void testRemoveConfigsForASchemeWhichHasNoConfigsUniquelyDependantOnIt() throws Exception
    {
        removeConfigsForConfigScheme(20010L);
        assertEquals(4, getRowsCountInFieldConfiguration());
    }

    @Test
    public void testRemoveConfigForSchemeWith1UniquelyDependantAnd1NonUniqueDependant() throws Exception
    {
        removeConfigsForConfigScheme(20100L);
        assertEquals(3, getRowsCountInFieldConfiguration());
    }

    @Test
    public void testRemoveConfigForSchemeWhichHas1UniquelyDependantOnly() throws Exception
    {
        removeConfigsForConfigScheme(20001L);
        assertEquals(3, getRowsCountInFieldConfiguration());
    }

    private int getRowsCountInFieldConfiguration() {
        return delegator.findAll(FIELD_CONFIGURATION_ENTITY_NAME).size();
    }

    private void removeConfigsForConfigScheme(final Long id)
    {
        Collection fieldConfigs = persister.getConfigsExclusiveToConfigScheme(id);
        for (Iterator it = fieldConfigs.iterator(); it.hasNext();)
        {
            persister.remove((FieldConfig) it.next());
        }
    }

    private GenericValue createFieldConfig(Long id) throws GenericEntityException
    {
        return createFieldConfigInEntity("FieldConfiguration", id);
    }

    private GenericValue createFieldConfigScheme(Long id) throws GenericEntityException
    {
        return createFieldConfigInEntity("FieldConfigScheme", id);
    }

    private GenericValue createFieldConfigInEntity(String entityName, final Long id)
    {
        return EntityUtils.createValue(entityName, MapBuilder.<String, Object>newBuilder()
                .add("id", id)
                .add("name", "Test Config " + id)
                .add("description", "Test Desc")
                .add("fieldid", "customfield_" + id)
                .add("customfield", null).toMap());
    }

    private GenericValue createFieldConfigSchemeIssueType(Long id, Long fieldConfigId, Long fieldConfigSchemeId, final String issueType)
            throws GenericEntityException
    {
        return EntityUtils.createValue("FieldConfigSchemeIssueType", MapBuilder.<String, Object>newBuilder()
                .add("id", id)
                .add("issuetype", issueType)
                .add("fieldconfigscheme", fieldConfigSchemeId)
                .add("fieldconfiguration", fieldConfigId).toMap());
    }

    private GenericValue createGenericConfig(Long id, Long fieldConfigId) throws GenericEntityException
    {
        return EntityUtils.createValue("GenericConfiguration", MapBuilder.<String, Object>newBuilder()
                .add("id", id)
                .add("datatype", "DefaultValue")
                .add("datakey", fieldConfigId)
                .add("xmlvalue", "<string>" + fieldConfigId + "</string>").toMap());
    }

    private GenericValue createOptionConfig(Long id, Long fieldConfigId) throws GenericEntityException
    {
        return EntityUtils.createValue("OptionConfiguration", MapBuilder.<String, Object>newBuilder()
                .add("id", id)
                .add("fieldid", "issuetype")
                .add("optionid", id)
                .add("fieldconfig", fieldConfigId)
                .add("sequence", id).toMap());
    }

}