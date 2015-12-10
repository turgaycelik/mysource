package com.atlassian.jira.upgrade.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.local.testutils.EntityConditionAssertions;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @since v5.0.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUpgradeTask_Build753
{
    private static final String ENTITY_FAV = "FavouriteAssociations";
    private static final String FIELD_OWNER = "username";
    private static final String FIELD_TYPE = "entityType";
    private static final String FIELD_ID = "id";
    private static final String FIELD_ENTITY_ID = "entityId";
    private static final String FIELD_SEQUENCE = "sequence";

    private static final String ENTITY_SR = "SearchRequest";
    private static final String FIELD_FAV_COUNT = "favCount";

    @Mock
    private OfBizDelegator delegator;
    private UpgradeTask_Build753 upgradeTask;

    @Before
    public void setUp() throws Exception
    {
        upgradeTask = new UpgradeTask_Build753(delegator);
    }

    @Test
    public void checkCorrectBuildNumber()
    {
        assertThat(upgradeTask.getBuildNumber(), equalTo("753"));
    }
    
    @Test
    public void doUpgradeWithNoFavourites()
    {
        stub(delegator.findAll(ENTITY_FAV)).toReturn(Collections.<GenericValue>emptyList()).toReturn(null);
        upgradeTask.doUpgrade(false);
        upgradeTask.doUpgrade(false);

        verify(delegator, times(2)).findAll(ENTITY_FAV);
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void doUpgradeWithGoodFavourites()
    {
        final GenericValue gooduser = mockFavouriteGv(1, "gooduser");
        final GenericValue nouser = mockFavouriteGv(2, "");
        final GenericValue nullUser = mockFavouriteGv(3, null);

        stub(delegator.findAll(ENTITY_FAV))
                .toReturn(newArrayList(gooduser, nouser, nullUser));

        upgradeTask.doUpgrade(false);

        verify(delegator, times(1)).findAll(ENTITY_FAV);
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void doUpgradeWithWithBadFavouritesNotLinked()
    {
        final GenericValue gooduser = mockFavouriteGv(1, "gooduser");
        final GenericValue nouser = mockFavouriteGv(2, "");
        final GenericValue badUser = mockFavouriteGv(3, "badUser");

        stub(delegator.findAll(ENTITY_FAV))
                .toReturn(newArrayList(gooduser, nouser, badUser));

        upgradeTask.doUpgrade(false);

        verify(delegator).findAll(ENTITY_FAV);
        verify(delegator).removeValue(badUser);
        verify(delegator).createValue(ENTITY_FAV, ImmutableMap.<String, Object>builder()
                .put(FIELD_OWNER, "baduser").put(FIELD_TYPE, ENTITY_SR)
                .put(FIELD_SEQUENCE, 0L)
                .put(FIELD_ENTITY_ID, 1003L).build());
    }

    @Test
    public void doUpgradeWithWithBadFavouritesNotLinkedOtherFavourites()
    {
        final GenericValue gooduser = mockFavouriteGv(1, "gooduser");
        final GenericValue nouser = mockFavouriteGv(2, "");
        final GenericValue badUser = mockFavouriteGv(3, "badUser");

        stub(delegator.findAll(ENTITY_FAV))
                .toReturn(newArrayList(gooduser, nouser, badUser));

        stub(delegator.findByCondition(eq(ENTITY_FAV),
                Matchers.<EntityCondition>any(),
                Matchers.<List<String>>any(),
                Matchers.<List<String>>any())).toReturn(mockIdGv(1, 2, 3, 67, 8));
        
        upgradeTask.doUpgrade(false);

        verify(delegator).findAll(ENTITY_FAV);
        verify(delegator).removeValue(badUser);
        verify(delegator).createValue(ENTITY_FAV, ImmutableMap.<String, Object>builder()
                .put(FIELD_OWNER, "baduser").put(FIELD_TYPE, ENTITY_SR)
                .put(FIELD_SEQUENCE, 5L)
                .put(FIELD_ENTITY_ID, 1003L).build());

        ArgumentCaptor<EntityCondition> condition = ArgumentCaptor.forClass(EntityCondition.class);
        
        verify(delegator, times(1)).findByCondition(eq(ENTITY_FAV), 
                condition.capture(),
                eq(newArrayList(FIELD_ENTITY_ID)),
                eq(newArrayList(FIELD_SEQUENCE + " ASC")));

        EntityConditionAssertions.assertEquals(createConditionToFindCurrentFavouritesForUser("baduser"),
                condition.getValue());
    }

    @Test
    public void doUpgradeWithWithBadFavouritesAlreadyLinked()
    {
        checkUpgradeAlreadyLinked(mockSR(1003L, 3L));
    }
    
    @Test
    public void doUpgradeWithWithBadFavouritesAlreadyLinkedZeroFavCount()
    {
        checkUpgradeAlreadyLinked(mockSR(1003L, 0L));
    }

    @Test
    public void doUpgradeWithWithBadFavouritesAlreadyLinkedNullFavCount()
    {
        checkUpgradeAlreadyLinked(mockSR(1003L, null));
    }

    @Test
    public void doUpgradeWithWithBadFavouritesAlreadyLinkedNoSearchRequest()
    {
        checkUpgradeAlreadyLinked(null);
    }

    private void checkUpgradeAlreadyLinked(GenericValue searchRequest)
    {
        final GenericValue gooduser = mockFavouriteGv(1, "gooduser");
        final GenericValue nouser = mockFavouriteGv(2, "");
        final GenericValue badUser = mockFavouriteGv(3, "badUser");
        
        Long count = searchRequest == null ? null : searchRequest.getLong(FIELD_FAV_COUNT);

        stub(delegator.findAll(ENTITY_FAV))
                .toReturn(newArrayList(gooduser, nouser, badUser));

        stub(delegator.findByAnd(ENTITY_FAV, createConditionToFindFavouriteForUser("baduser", 1003L)))
                .toReturn(newArrayList(mockFavouriteGv(20L, "baduser")));

        stub(delegator.findByPrimaryKey(ENTITY_SR, 1003L)).toReturn(searchRequest);

        upgradeTask.doUpgrade(false);

        verify(delegator).findAll(ENTITY_FAV);
        verify(delegator).removeValue(badUser);
        verify(delegator, never()).createValue(Matchers.<String>any(), Matchers.<Map<String, Object>>any());
        
        if (searchRequest != null)
        {
            if (count == null || count > 0)
            {
                assertThat(searchRequest.getLong(FIELD_FAV_COUNT), equalTo(count == null ? 0 : count - 1));
                verify(delegator).store(searchRequest);
            }
            else
            {
                assertThat(searchRequest.getLong(FIELD_FAV_COUNT), equalTo(count));
                verify(delegator, never()).store(searchRequest);
            }
        }
    }

    @Test
    public void doUpgradeWithWithBadFavouritesSRDoesNotExist()
    {
        final GenericValue gooduser = mockFavouriteGv(1, "gooduser");
        final GenericValue nouser = mockFavouriteGv(2, "");
        final GenericValue badUser = mockFavouriteGv(3, "badUser");

        stub(delegator.findAll(ENTITY_FAV))
                .toReturn(newArrayList(gooduser, nouser, badUser));

        stub(delegator.findByAnd(ENTITY_FAV, createConditionToFindFavouriteForUser("baduser", 1003L)))
                .toReturn(newArrayList(mockFavouriteGv(20L, "baduser")));
        
        upgradeTask.doUpgrade(false);
        
        verify(delegator).findAll(ENTITY_FAV);
        verify(delegator).removeValue(badUser);
        verify(delegator, never()).createValue(Matchers.<String>any(), Matchers.<Map<String, Object>>any());
    }

    @Test
    public void isNotIndexTask()
    {
        assertThat(upgradeTask.isReindexRequired(), is(false));
    }
    
    private static List<GenericValue> mockIdGv(long...ids)
    {
        List<GenericValue> gvs = new ArrayList<GenericValue>(ids.length);
        for (long id : ids)
        {
            Map<String, Object> fields = Maps.newLinkedHashMap();
            fields.put(FIELD_ID, id);
            
            gvs.add(new MockGenericValue(ENTITY_FAV, fields));
        }
        return gvs;
    }

    private static EntityCondition createConditionToFindCurrentFavouritesForUser(String user)
    {
        final EntityCondition userCondition = new EntityExpr(FIELD_OWNER, EntityOperator.EQUALS, user);
        final EntityCondition typeCondition = new EntityExpr(FIELD_TYPE, EntityOperator.EQUALS, ENTITY_SR);
        return new EntityConditionList(newArrayList(userCondition, typeCondition), EntityOperator.AND);
    }
    
    private static Map<String, ?> createConditionToFindFavouriteForUser(String owner, long entityId)
    {
        return ImmutableMap.of(FIELD_TYPE, ENTITY_SR, FIELD_OWNER, owner, FIELD_ENTITY_ID, entityId);
    }
    
    private static GenericValue mockFavouriteGv(long id, String owner)
    {
        Map<String, Object> fields = Maps.newLinkedHashMap();
        fields.put(FIELD_OWNER, owner);
        fields.put(FIELD_ID, id);
        fields.put(FIELD_TYPE, ENTITY_SR);
        fields.put(FIELD_ENTITY_ID, id + 1000L);

        return new MockGenericValue(ENTITY_FAV, fields);
    }
    
    private static GenericValue mockSR(long id, Long favCount)
    {
        Map<String, Object> fields = Maps.newLinkedHashMap();
        fields.put(FIELD_ID, id);
        fields.put(FIELD_FAV_COUNT, favCount);

        return new MockGenericValue(ENTITY_SR, fields);
    }
}
