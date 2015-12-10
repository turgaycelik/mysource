package com.atlassian.jira.index.ha;

import java.sql.Timestamp;
import java.util.Map;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;

import com.google.common.collect.Sets;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 *
 * @since v6.1
 */
public class TestReplicatedIndexOperationFactory
{
    private static final String UNUSED_FILENAME = "";
    private ReplicatedIndexOperationFactory factory = new ReplicatedIndexOperationFactory();

    private final ReplicatedIndexOperation testOp = new ReplicatedIndexOperation(1L,
                 "NODE1", new Timestamp(300000L), ReplicatedIndexOperation.AffectedIndex.ISSUE,
                  ReplicatedIndexOperation.SharedEntityType.SEARCH_REQUEST, ReplicatedIndexOperation.Operation.UPDATE,
                  Sets.newHashSet(1L, 2L, 3L), UNUSED_FILENAME);


    @Test
    public void testFactoryCreatesReplicatedIndexOperation()
    {
        Map<String, Object> fieldMap = getFieldMapForIndexOperation();
        GenericValue gv = new MockGenericValue(factory.getEntityName(), fieldMap);
        ReplicatedIndexOperation operation = factory.build(gv);
        assertEquals(testOp, operation);
    }

    @Test
    public void testFactoryCreatesSensibleFieldMap()
    {
        Map<String, Object> fieldMap = getFieldMapForIndexOperation();
        Map<String, Object> map = factory.fieldMapFrom(testOp);
        assertEquals(fieldMap, map);
    }

    @Test
    public void testNoSharedEntityType()
    {
        Map<String, Object> fieldMap = getFieldMapForIndexOperation();
        fieldMap.put(ReplicatedIndexOperation.ENTITY_TYPE, "NONE");
        ReplicatedIndexOperation operation = factory.build(new MockGenericValue(factory.getEntityName(), fieldMap));
        assertNull(operation.getEntityType().getTypeDescriptor());
    }

    private Map<String, Object> getFieldMapForIndexOperation()
    {
        return new FieldMap(ReplicatedIndexOperation.ID,1L)
                .add(ReplicatedIndexOperation.NODE_ID, "NODE1")
                .add(ReplicatedIndexOperation.INDEX_TIME, new Timestamp(300000L))
                .add(ReplicatedIndexOperation.AFFECTED_INDEX, "ISSUE")
                .add(ReplicatedIndexOperation.ENTITY_TYPE, "SEARCH_REQUEST")
                .add(ReplicatedIndexOperation.OPERATION, "UPDATE")
                .add(ReplicatedIndexOperation.AFFECTED_IDS, "1,2,3")
                .add(ReplicatedIndexOperation.BACKUP_FILENAME, UNUSED_FILENAME);
    }
}
