package com.atlassian.jira.entity;


import java.util.Collection;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityListIterator;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestPagedList {

    private final String mockEntity = "mockEntity";
    @Mock
    private EntityFactory<MockEntity> mockEntityFactory;
    @Mock
    private EntityCondition mockEntityCondition;
    @Mock
    private GenericValue mockGenericValue;
    @Mock
    private DelegatorInterface mockGenericDelegator;
    @Mock
    private GenericHelper mockGenericHelper;
    @Mock
    private EntityListIterator mockEntityListener;
    @Mock
    private ModelEntity mockEntityModel;
    private EntityFindOptions entityFindOptions = new EntityFindOptions();


    @Before
    public void setupMocks() throws Exception
    {
        when(mockEntityFactory.getEntityName()).thenReturn(mockEntity);
        when(mockGenericDelegator.getEntityHelper(mockEntity)).thenReturn(mockGenericHelper);
        when(mockGenericDelegator.getModelEntity(mockEntity)).thenReturn(mockEntityModel);
        when(mockEntityFactory.buildList(anyListOf(GenericValue.class))).thenReturn(Lists.<MockEntity>newArrayList());
        entityFindOptions.setOffset(0);
        entityFindOptions.setMaxResults(3);
    }

    @Test
    public void testPageSize() throws Exception
    {
        when(mockGenericDelegator.countByCondition(mockEntity, null, mockEntityCondition, null)).thenReturn(6);
        EntityPagedList<MockEntity> pagedList = new EntityPagedList<MockEntity>(3, mockEntityFactory, mockEntityCondition, Lists.newArrayList("field"), mockGenericDelegator);
        assertEquals(6,pagedList.getSize());
    }

    @Test
    public void testPagingBounds() throws Exception
    {
        when(mockGenericDelegator.countByCondition(mockEntity, null, mockEntityCondition, null)).thenReturn(6);
        EntityPagedList<MockEntity> pagedList = new EntityPagedList<MockEntity>(3, mockEntityFactory, mockEntityCondition, Lists.newArrayList("field"), mockGenericDelegator);
        when(mockGenericHelper.findListIteratorByCondition(eq(mockEntityModel), eq(mockEntityCondition), (EntityCondition) isNull(),
                  (Collection) isNull(), anyListOf(String.class), isA(EntityFindOptions.class))).thenReturn(mockEntityListener);
        try
        {
            pagedList.getPage(2);
            fail("Expecting exception");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("pageNumber should be between 0 and 1", e.getMessage());
        }
    }
}
