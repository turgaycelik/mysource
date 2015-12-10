package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.PropertyUtils;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionParam;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;

import junit.framework.TestCase;

public class GroupEntityConditionFactoryTest extends TestCase
{
    private EntityConditionFactory factory;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        factory = new GroupEntityConditionFactory();
    }

    public void testNullRestriction() throws Exception
    {
        assertNull(factory.getEntityConditionFor(NullRestrictionImpl.INSTANCE));
    }

    public void testEqualRestrictionNullString() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES, null);

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerGroupName IS NULL ", query);
        assertEquals(0, parameterValues.size());
    }

    public void testEqualRestrictionEmptyString() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES, "");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerGroupName =  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerGroupName=", parameterValues.get(0).toString());
    }

    public void testEqualRestrictionString() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES,
            "accounts");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerGroupName =  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerGroupName=accounts", parameterValues.get(0).toString());
    }

    public void testEqualRestrictionBoolean() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<Boolean>(PropertyUtils.ofTypeBoolean("active"), MatchMode.EXACTLY_MATCHES,
            Boolean.TRUE);

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("active =  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("active=1", parameterValues.get(0).toString());
    }

    public void testGTRestriction() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.GREATER_THAN,
            "accounts");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerGroupName >  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerGroupName=accounts", parameterValues.get(0).toString());
    }

    public void testLTRestriction() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.LESS_THAN, "accounts");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerGroupName <  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerGroupName=accounts", parameterValues.get(0).toString());
    }

    public void testContainsRestriction() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.CONTAINS, "accounts");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerGroupName LIKE  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerGroupName=%accounts%", parameterValues.get(0).toString());
    }

    public void testStartsWithRestriction() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.STARTS_WITH, "accounts");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerGroupName LIKE  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerGroupName=accounts%", parameterValues.get(0).toString());
    }

    public void testSimpleOr() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES,
            "accounts");
        final SearchRestriction searchRestriction2 = new TermRestriction<String>(PropertyUtils.ofTypeString("description"), MatchMode.EXACTLY_MATCHES,
            "Bean counters");
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, searchRestriction1, searchRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("(lowerGroupName =  ? ) OR (lowerDescription =  ? )", query);
        assertEquals(2, parameterValues.size());
        assertEquals("lowerGroupName=accounts", parameterValues.get(0).toString());
        assertEquals("lowerDescription=bean counters", parameterValues.get(1).toString());
    }

    public void testSimpleAnd() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES,
            "accounts");
        final SearchRestriction searchRestriction2 = new TermRestriction<String>(PropertyUtils.ofTypeString("description"), MatchMode.EXACTLY_MATCHES,
            "Bean counters");
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction1, searchRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("(lowerGroupName =  ? ) AND (lowerDescription =  ? )", query);
        assertEquals(2, parameterValues.size());
        assertEquals("lowerGroupName=accounts", parameterValues.get(0).toString());
        assertEquals("lowerDescription=bean counters", parameterValues.get(1).toString());
    }

    public void testSimpleAndWithNullRestriction() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES,
            "accounts");
        final SearchRestriction searchRestriction2 = NullRestrictionImpl.INSTANCE;
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction1, searchRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("(lowerGroupName =  ? )", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerGroupName=accounts", parameterValues.get(0).toString());
    }

    public void testTrivialBooleanAndWithOnlyNullRestriction() throws Exception
    {
        final SearchRestriction searchRestriction1 = NullRestrictionImpl.INSTANCE;
        final SearchRestriction searchRestriction2 = NullRestrictionImpl.INSTANCE;
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction1, searchRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("", query);
        assertEquals(0, parameterValues.size());
    }

    public void testSimpleOrWithNullRestriction() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES,
            "accounts");
        final SearchRestriction searchRestriction2 = NullRestrictionImpl.INSTANCE;
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, searchRestriction1, searchRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        assertNull(condition);
    }

    public void testNestedQuery() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES,
            "accounts");
        final SearchRestriction searchRestriction2 = new TermRestriction<String>(PropertyUtils.ofTypeString("description"), MatchMode.EXACTLY_MATCHES,
            "Bean counters");
        final SearchRestriction searchRestriction3 = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES,
            "legal");
        final SearchRestriction searchRestriction4 = new TermRestriction<String>(PropertyUtils.ofTypeString("description"), MatchMode.EXACTLY_MATCHES,
            "Bill and Frank's Dodgy Firm");
        final SearchRestriction booleanRestriction1 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction1,
            searchRestriction2);
        final SearchRestriction booleanRestriction2 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction3,
            searchRestriction4);
        final SearchRestriction booleanRestriction3 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, booleanRestriction1,
            booleanRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(booleanRestriction3);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("((lowerGroupName =  ? ) AND (lowerDescription =  ? )) OR ((lowerGroupName =  ? ) AND (lowerDescription =  ? ))", query);
        assertEquals(4, parameterValues.size());
        assertEquals("lowerGroupName=accounts", parameterValues.get(0).toString());
        assertEquals("lowerDescription=bean counters", parameterValues.get(1).toString());
        assertEquals("lowerGroupName=legal", parameterValues.get(2).toString());
        assertEquals("lowerDescription=bill and frank's dodgy firm", parameterValues.get(3).toString());
    }

    public void testAttributeRestriction() throws Exception
    {
        final SearchRestriction restriction = new TermRestriction<String>(PropertyUtils.ofTypeString("manager"), MatchMode.EXACTLY_MATCHES, "Paul");

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("id IN (SELECT group_id FROM cwd_group_attributes WHERE attribute_name = ? AND lower_attribute_value =  ? )", query);
        assertEquals(2, parameterValues.size());
        assertEquals("name=manager", parameterValues.get(0).toString());
        assertEquals("value=paul", parameterValues.get(1).toString());
    }

    public void testNestedWithAttributesQuery() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES,
            "accounts");
        final SearchRestriction searchRestriction2 = new TermRestriction<String>(PropertyUtils.ofTypeString("manager"), MatchMode.EXACTLY_MATCHES, "Paul");
        final SearchRestriction searchRestriction3 = new TermRestriction<String>(PropertyUtils.ofTypeString("groupName"), MatchMode.EXACTLY_MATCHES,
            "legal");
        final SearchRestriction searchRestriction4 = new TermRestriction<String>(PropertyUtils.ofTypeString("manager"), MatchMode.EXACTLY_MATCHES, "Paul");
        final SearchRestriction booleanRestriction1 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction1,
            searchRestriction2);
        final SearchRestriction booleanRestriction2 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction3,
            searchRestriction4);
        final SearchRestriction booleanRestriction3 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, booleanRestriction1,
            booleanRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(booleanRestriction3);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals(
            "((lowerGroupName =  ? ) AND (id IN (SELECT group_id FROM cwd_group_attributes WHERE attribute_name = ? AND lower_attribute_value =  ? ))) OR ((lowerGroupName =  ? ) AND (id IN (SELECT group_id FROM cwd_group_attributes WHERE attribute_name = ? AND lower_attribute_value =  ? )))",
            query);
        assertEquals(6, parameterValues.size());
        assertEquals("lowerGroupName=accounts", parameterValues.get(0).toString());
        assertEquals("name=manager", parameterValues.get(1).toString());
        assertEquals("value=paul", parameterValues.get(2).toString());
        assertEquals("lowerGroupName=legal", parameterValues.get(3).toString());
        assertEquals("name=manager", parameterValues.get(4).toString());
        assertEquals("value=paul", parameterValues.get(5).toString());
    }

    private ModelEntity getModelEntity()
    {
        final ModelEntity modelEntity = new ModelEntity();
        modelEntity.setTableName("Group");
        modelEntity.addField(getModelField("groupName"));
        modelEntity.addField(getModelField("lowerGroupName"));
        modelEntity.addField(getModelField("description"));
        modelEntity.addField(getModelField("lowerDescription"));
        modelEntity.addField(getModelField("type"));
        modelEntity.addField(getModelField("active", "boolean"));

        return modelEntity;
    }

    private ModelField getModelField(final String name)
    {
        final ModelField modelField = new ModelField();
        modelField.setName(name);
        modelField.setColName(name);
        modelField.setType("blah");

        return modelField;
    }

    private ModelField getModelField(final String name, final String type)
    {
        final ModelField modelField = new ModelField();
        modelField.setName(name);
        modelField.setColName(name);
        modelField.setType(type);

        return modelField;
    }
}