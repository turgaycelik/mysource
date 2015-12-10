package com.atlassian.jira.crowd.embedded.ofbiz;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.PropertyUtils;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionParam;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;

import junit.framework.TestCase;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserEntityConditionFactoryTest extends TestCase
{
    private EntityConditionFactory factory;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        ModelEntity entity = mock(ModelEntity.class);
        when(entity.getTableName(anyString())).thenReturn("cwd_user_attributes");

        GenericDelegator genericDelegator = mock(GenericDelegator.class);
        when(genericDelegator.getModelEntity(anyString())).thenReturn(entity);

        factory = new UserEntityConditionFactory(new DefaultOfBizDelegator(genericDelegator));
    }

    public void testNullRestriction() throws Exception
    {
        assertNull(factory.getEntityConditionFor(NullRestrictionImpl.INSTANCE));
    }

    public void testLessThanRestrictionDate() throws Exception
    {
        Date date = new Date();
        final SearchRestriction searchRestriction = new TermRestriction<Date>(UserTermKeys.CREATED_DATE, MatchMode.LESS_THAN, date);

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("createdDate <  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("createdDate", parameterValues.get(0).getModelField().getName());
        // We convert the date to a java.sql.Timestamp for ofbiz
        Timestamp timestamp = new Timestamp(date.getTime());
        assertEquals(timestamp, parameterValues.get(0).getFieldValue());
        assertEquals("createdDate="+timestamp, parameterValues.get(0).toString());
    }

    public void testUpdatedAndCreatedDateRestriction() throws Exception
    {
        Date date = new Date();
        final SearchRestriction createdDateRestriction = new TermRestriction<Date>(UserTermKeys.CREATED_DATE, MatchMode.LESS_THAN, date);
        final SearchRestriction updatedDateRestriction = new TermRestriction<Date>(UserTermKeys.UPDATED_DATE, MatchMode.LESS_THAN, date);
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND,
                createdDateRestriction, updatedDateRestriction);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("(createdDate <  ? ) AND (updatedDate <  ? )", query);
        assertEquals(2, parameterValues.size());
        // We convert the date to a java.sql.Timestamp for ofbiz
        Timestamp timestamp = new Timestamp(date.getTime());
        assertEquals("createdDate="+timestamp, parameterValues.get(0).toString());
        assertEquals("updatedDate="+timestamp, parameterValues.get(1).toString());
    }

    public void testEqualRestrictionNullString() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, null);

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerUserName IS NULL ", query);
        assertEquals(0, parameterValues.size());
    }

    public void testEqualRestrictionEmptyString() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerUserName =  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerUserName=", parameterValues.get(0).toString());
    }

    public void testEqualRestrictionString() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "fred");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerUserName =  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerUserName=fred", parameterValues.get(0).toString());
    }

    public void testEqualRestrictionBooleanNull() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<Boolean>(UserTermKeys.ACTIVE, MatchMode.EXACTLY_MATCHES, null);

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("active IS NULL ", query);
        assertEquals(0, parameterValues.size());
    }

    public void testEqualRestrictionBooleanTrue() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<Boolean>(UserTermKeys.ACTIVE, MatchMode.EXACTLY_MATCHES,
            Boolean.TRUE);

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("active =  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("active=1", parameterValues.get(0).toString());
    }

    public void testEqualRestrictionBooleanFalse() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<Boolean>(UserTermKeys.ACTIVE, MatchMode.EXACTLY_MATCHES,
            Boolean.FALSE);

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("active =  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("active=0", parameterValues.get(0).toString());
    }

    public void testGTRestriction() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.GREATER_THAN, "fred");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerUserName >  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerUserName=fred", parameterValues.get(0).toString());
    }

    public void testLTRestriction() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.LESS_THAN, "fred");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerUserName <  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerUserName=fred", parameterValues.get(0).toString());
    }

    public void testContainsRestriction() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.CONTAINS, "fred");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerUserName LIKE  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerUserName=%fred%", parameterValues.get(0).toString());
    }

    public void testStartsWithRestriction() throws Exception
    {
        final SearchRestriction searchRestriction = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.STARTS_WITH, "fred");

        final EntityCondition condition = factory.getEntityConditionFor(searchRestriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("lowerUserName LIKE  ? ", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerUserName=fred%", parameterValues.get(0).toString());
    }

    public void testSimpleOr() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "fred");
        final SearchRestriction searchRestriction2 = new TermRestriction<String>(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
            "fred@example.com");
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, searchRestriction1, searchRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("(lowerUserName =  ? ) OR (lowerEmailAddress =  ? )", query);
        assertEquals(2, parameterValues.size());
        assertEquals("lowerUserName=fred", parameterValues.get(0).toString());
        assertEquals("lowerEmailAddress=fred@example.com", parameterValues.get(1).toString());
    }

    public void testSimpleAnd() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "fred");
        final SearchRestriction searchRestriction2 = new TermRestriction<String>(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
            "fred@example.com");
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction1, searchRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("(lowerUserName =  ? ) AND (lowerEmailAddress =  ? )", query);
        assertEquals(2, parameterValues.size());
        assertEquals("lowerUserName=fred", parameterValues.get(0).toString());
        assertEquals("lowerEmailAddress=fred@example.com", parameterValues.get(1).toString());
    }

    public void testSimpleAndWithNullRestriction() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "fred");
        final SearchRestriction searchRestriction2 = NullRestrictionImpl.INSTANCE;
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction1, searchRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("(lowerUserName =  ? )", query);
        assertEquals(1, parameterValues.size());
        assertEquals("lowerUserName=fred", parameterValues.get(0).toString());
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
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "fred");
        final SearchRestriction searchRestriction2 = NullRestrictionImpl.INSTANCE;
        final SearchRestriction restriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, searchRestriction1, searchRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        assertNull(condition);
    }

    public void testNestedQuery() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "fred");
        final SearchRestriction searchRestriction2 = new TermRestriction<String>(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
            "fred@example.com");
        final SearchRestriction searchRestriction3 = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "joe");
        final SearchRestriction searchRestriction4 = new TermRestriction<String>(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
            "joe@example.com");
        final SearchRestriction booleanRestriction1 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction1,
            searchRestriction2);
        final SearchRestriction booleanRestriction2 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, searchRestriction3,
            searchRestriction4);
        final SearchRestriction booleanRestriction3 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, booleanRestriction1,
            booleanRestriction2);

        final EntityCondition condition = factory.getEntityConditionFor(booleanRestriction3);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("((lowerUserName =  ? ) AND (lowerEmailAddress =  ? )) OR ((lowerUserName =  ? ) AND (lowerEmailAddress =  ? ))", query);
        assertEquals(4, parameterValues.size());
        assertEquals("lowerUserName=fred", parameterValues.get(0).toString());
        assertEquals("lowerEmailAddress=fred@example.com", parameterValues.get(1).toString());
        assertEquals("lowerUserName=joe", parameterValues.get(2).toString());
        assertEquals("lowerEmailAddress=joe@example.com", parameterValues.get(3).toString());
    }

    public void testAttributeRestriction() throws Exception
    {
        final SearchRestriction restriction = new TermRestriction<String>(PropertyUtils.ofTypeString("login.count"), MatchMode.EXACTLY_MATCHES, "0");

        final EntityCondition condition = factory.getEntityConditionFor(restriction);
        final List<EntityConditionParam> parameterValues = new ArrayList<EntityConditionParam>();
        final String query = condition.makeWhereString(getModelEntity(), parameterValues);
        assertEquals("id IN (SELECT user_id FROM cwd_user_attributes WHERE attribute_name = ? AND lower_attribute_value =  ? )", query);
        assertEquals(2, parameterValues.size());
        assertEquals("name=login.count", parameterValues.get(0).toString());
        assertEquals("value=0", parameterValues.get(1).toString());
    }

    public void testNestedWithAttributesQuery() throws Exception
    {
        final SearchRestriction searchRestriction1 = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "fred");
        final SearchRestriction searchRestriction2 = new TermRestriction<String>(PropertyUtils.ofTypeString("login.count"), MatchMode.EXACTLY_MATCHES, "0");
        final SearchRestriction searchRestriction3 = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.EXACTLY_MATCHES, "joe");
        final SearchRestriction searchRestriction4 = new TermRestriction<String>(PropertyUtils.ofTypeString("login.count"), MatchMode.EXACTLY_MATCHES, "0");
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
            "((lowerUserName =  ? ) AND (id IN (SELECT user_id FROM cwd_user_attributes WHERE attribute_name = ? AND lower_attribute_value =  ? ))) OR ((lowerUserName =  ? ) AND (id IN (SELECT user_id FROM cwd_user_attributes WHERE attribute_name = ? AND lower_attribute_value =  ? )))",
            query);
        assertEquals(6, parameterValues.size());
        assertEquals("lowerUserName=fred", parameterValues.get(0).toString());
        assertEquals("name=login.count", parameterValues.get(1).toString());
        assertEquals("value=0", parameterValues.get(2).toString());
        assertEquals("lowerUserName=joe", parameterValues.get(3).toString());
        assertEquals("name=login.count", parameterValues.get(4).toString());
        assertEquals("value=0", parameterValues.get(5).toString());
    }

    private ModelEntity getModelEntity()
    {
        final ModelEntity modelEntity = new ModelEntity();
        modelEntity.setTableName("User");
        modelEntity.addField(getModelField("userName"));
        modelEntity.addField(getModelField("lowerUserName"));
        modelEntity.addField(getModelField("emailAddress"));
        modelEntity.addField(getModelField("lowerEmailAddress"));
        modelEntity.addField(getModelField("active", "boolean"));
        modelEntity.addField(getModelField("createdDate", "java.util.Date"));
        modelEntity.addField(getModelField("updatedDate", "java.util.Date"));

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
