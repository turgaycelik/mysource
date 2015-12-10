package com.atlassian.jira.favourites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserKeyService;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;

public class TestOfBizFavStore
{

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private UserKeyService userKeyService;

    @AvailableInContainer
    private final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    private final OfBizFavouritesStore testedObject = new OfBizFavouritesStore(ofBizDelegator);

    private ApplicationUser user;
    private SharedEntity entity;
    private static final String NOTADMIN_USER = "notadmin";
    private static final String ADMIN_USER = "admin";

    private static class Column
    {
        private static final String USERNAME = "username";
        private static final String ENTITY_TYPE = "entityType";
        private static final String ENTITY_ID = "entityId";
        private static final String SEQUENCE = "sequence";
    }

    private static class ID
    {
        private static final Long _666 = new Long(666);
        private static final Long _999 = new Long(999);
        private static final Long _123 = new Long(123);
        private static final Long _456 = new Long(456);
        private static final Long _789 = new Long(789);
    }

    private static class SharedEntityType
    {
        static final SharedEntity.TypeDescriptor<?> DASHBOARD = SharedEntity.TypeDescriptor.Factory.get().create("Dashboard");
    }

    @Before
    public void setUp() throws Exception
    {
        user = ApplicationUsers.from(addMockUser(ADMIN_USER));
        addMockUser(NOTADMIN_USER);

        entity = new SharedEntity.Identifier(ID._999, SearchRequest.ENTITY_TYPE, user);
    }

    private User addMockUser(final String username)
    {
        when(userKeyService.getKeyForUsername(username)).thenReturn(username);
        return new MockUser(username);
    }

    @Test
    public void testAddFavouriteSuccess()
    {
        assertTrue(testedObject.addFavourite(user, entity));

        final List<GenericValue> results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertEquals(Arrays.asList(toFavoriteAssociationGV(entity, user.getName(), Long.valueOf(0))),
                toFavoriteAssociationGVWithoutID(results));
    }

    private GenericValue toFavoriteAssociationGV(final SharedEntity entity, final String username, final Long sequence)
    {
        final Builder<Object, Object> fields = ImmutableMap.builder()
                .put(Column.ENTITY_ID, entity.getId())
                .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                .put(Column.USERNAME, username);
        if (sequence != null)
        {
            fields.put(Column.SEQUENCE, sequence);
        }
        return new MockGenericValue("FavouriteAssociations", fields.build());
    }

    private List<GenericValue> toFavoriteAssociationGVWithoutID(final List<GenericValue> favoriteAssociations)
    {
        final List<GenericValue> result = new LinkedList<GenericValue>();
        for (final GenericValue favoriteAssociation : favoriteAssociations)
        {
            result.add(toFavoriteAssociationGVWithoutID(favoriteAssociation));
        }
        return result;
    }

    private GenericValue toFavoriteAssociationGVWithoutID(final GenericValue favoriteAssociation)
    {
        final Map<String, Object> fields = new HashMap<String, Object>(favoriteAssociation);
        fields.remove("id");
        return new MockGenericValue(favoriteAssociation.getEntityName(), fields);
    }

    @Test
    public void testMultipleCreatesIncreasesSequence() throws Exception
    {
        assertTrue(testedObject.addFavourite(user, entity));

        List<GenericValue> gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.of(Column.ENTITY_ID, entity.getId()));
        assertGVHaveThisSequence(gvs, new long[] { 0 });

        entity = new SharedEntity.Identifier(ID._123, SearchRequest.ENTITY_TYPE, user);
        assertTrue(testedObject.addFavourite(user, entity));
        gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, ImmutableMap.of(Column.ENTITY_ID, entity.getId()));
        assertGVHaveThisSequence(gvs, new long[] { 1 });

        entity = new SharedEntity.Identifier(ID._456, SearchRequest.ENTITY_TYPE, user);
        assertTrue(testedObject.addFavourite(user, entity));
        gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, ImmutableMap.of(Column.ENTITY_ID, entity.getId()));
        assertGVHaveThisSequence(gvs, new long[] { 2 });

        entity = new SharedEntity.Identifier(ID._789, SearchRequest.ENTITY_TYPE, user);
        assertTrue(testedObject.addFavourite(user, entity));
        gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, ImmutableMap.of(Column.ENTITY_ID, entity.getId()));
        assertGVHaveThisSequence(gvs, new long[] { 3 });

        // now test the double addition does nothing to the sequence
        assertFalse(testedObject.addFavourite(user, entity));
        gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, ImmutableMap.of(Column.ENTITY_ID, entity.getId()));
        assertGVHaveThisSequence(gvs, new long[] { 3 });
    }

    private void assertGVHaveThisSequence(final List<GenericValue> gvs, final long[] expectedSequence)
    {
        assertNotNull(gvs);
        assertEquals(expectedSequence.length, gvs.size());
        for (int i = 0; i < expectedSequence.length; i++)
        {
            final long l = expectedSequence[i];
            final GenericValue gv = gvs.get(0);
            assertEquals(new Long(l), gv.getLong(Column.SEQUENCE));
        }
    }

    @Test
    public void testAddFavouriteAssociationAlreadyExists()
    {
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());

        assertFalse(testedObject.addFavourite(user, entity));

        final List<GenericValue> results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertEquals(Arrays.asList(toFavoriteAssociationGV(entity, user.getName(), null)), toFavoriteAssociationGVWithoutID(results));
    }

    /**
     * Remove a favourite that is the last. In that case no reorder should occur.
     */
    @Test
    public void testRemoveSuccessNoReorder()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.SEQUENCE, new Long(0))
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, "Dashboard")
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());
        final GenericValue gv2 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.SEQUENCE, new Long(0))
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._456)
                        .build());
        final GenericValue gv3 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.SEQUENCE, new Long(0))
                        .put(Column.USERNAME, NOTADMIN_USER)
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.SEQUENCE, new Long(1))
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());

        assertTrue(testedObject.removeFavourite(user, entity));

        final List<GenericValue> results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertEquals(Arrays.asList(gv1, gv2, gv3), results);
    }

    /**
     * Remove a favourite that is not the last, this should trigger a reorder.
     */
    @Test
    public void testRemoveSuccessWithReorder()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.SEQUENCE, new Long(0))
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, "Dashboard")
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());
        final GenericValue gv2 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.SEQUENCE, new Long(0))
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._123)
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.SEQUENCE, new Long(1))
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());
        final GenericValue gv3 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.SEQUENCE, new Long(0))
                        .put(Column.USERNAME, NOTADMIN_USER)
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._456)
                        .build());
        final GenericValue gv4 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.SEQUENCE, new Long(2))
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._666)
                        .build());

        assertTrue(testedObject.removeFavourite(user, entity));

        gv4.set(Column.SEQUENCE, new Long(1));

        final List<GenericValue> results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                Arrays.asList(Column.ENTITY_TYPE, Column.USERNAME, Column.SEQUENCE));
        assertEquals(Arrays.asList(gv1, gv2, gv3, gv4), results);
    }

    @Test
    public void testRemoveNoAssociation()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, "Dashboard")
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());
        final GenericValue gv2 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._456)
                        .build());
        final GenericValue gv3 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, NOTADMIN_USER)
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());

        assertFalse(testedObject.removeFavourite(user, entity));

        final List<GenericValue> results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertEquals(Arrays.asList(gv1, gv2, gv3), results);
    }

    @Test
    public void testIsNotFavourite()
    {
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, "Dashboard")
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._456)
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, NOTADMIN_USER)
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());

        assertFalse(testedObject.isFavourite(user, entity));
    }

    @Test
    public void testGetFavIdsNoneStored()
    {
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, "Dashboard")
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, NOTADMIN_USER)
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());

        final Collection<Long> ids = testedObject.getFavouriteIds(user, entity.getEntityType());
        assertEquals(Collections.emptyList(), ids);
    }

    @Test
    public void testGetFavIdsOneStored()
    {
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, entity.getId())
                        .build());

        final Collection<Long> ids = testedObject.getFavouriteIds(user, entity.getEntityType());
        assertEquals(Arrays.asList(ID._999), ids);
    }

    @Test
    public void testGetFavIdsManyStored()
    {

        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, "Dashboard")
                        .put(Column.ENTITY_ID, new Long(1))
                        .put(Column.SEQUENCE, Long.valueOf(1))
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, new Long(1))
                        .put(Column.SEQUENCE, Long.valueOf(2))
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._999)
                        .put(Column.SEQUENCE, Long.valueOf(3))
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._123)
                        .put(Column.SEQUENCE, Long.valueOf(4))
                        .build());

        final Collection<Long> ids = testedObject.getFavouriteIds(user, entity.getEntityType());
        assertEquals(Arrays.asList(Long.valueOf(1), ID._999, ID._123), ids);
    }

    @Test
    public void testRemoveFavouritesForUser()
    {

        final GenericValue gv1 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, "Dashboard")
                        .put(Column.ENTITY_ID, new Long(1))
                        .build());
        final GenericValue gv2 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, NOTADMIN_USER)
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, new Long(1))
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._999)
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._123)
                        .build());

        testedObject.removeFavouritesForUser(user, SearchRequest.ENTITY_TYPE);

        final List<GenericValue> results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertEquals(Arrays.asList(gv1, gv2), results);
    }

    @Test
    public void testRemoveFavouritesForUserNoEntries()
    {
        testedObject.removeFavouritesForUser(user, SearchRequest.ENTITY_TYPE);

        final List<GenericValue> results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertEquals(Collections.emptyList(), results);
    }

    @Test
    public void testRemoveFavouritesForEntity()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, "Dashboard")
                        .put(Column.ENTITY_ID, new Long(1))
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, NOTADMIN_USER)
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._999)
                        .build());
        UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._999)
                        .build());
        final GenericValue gv4 = UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, user.getName())
                        .put(Column.ENTITY_TYPE, entity.getEntityType().getName())
                        .put(Column.ENTITY_ID, ID._123)
                        .build());

        testedObject.removeFavouritesForEntity(entity);

        final List<GenericValue> results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertEquals(Arrays.asList(gv1, gv4), results);
    }

    @Test
    public void testRemoveFavouritesNoEntries()
    {
        testedObject.removeFavouritesForEntity(entity);

        final List<GenericValue> results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertEquals(Collections.emptyList(), results);
    }

    /**
     * Happy path test of re-ordering where everthing is as expected
     */
    @Test
    public void testUpdateSequenceReorders()
    {
        final SharedEntity.TypeDescriptor<SharedEntity> entityType = entity.getEntityType();

        final List<GenericValue> inputGvs = new LinkedList<GenericValue>();
        inputGvs.add(storeTestEntity(new Long(1), user.getName(), SharedEntityType.DASHBOARD, 999));
        inputGvs.add(storeTestEntity(ID._123, user.getName(), entityType, 0, 2));
        inputGvs.add(storeTestEntity(ID._456, user.getName(), entityType, 1, 0));
        inputGvs.add(storeTestEntity(ID._789, user.getName(), entityType, 2, 1));
        inputGvs.add(storeTestEntity(ID._999, user.getName(), entityType, 3, 3));
        inputGvs.add(storeTestEntity(ID._999, NOTADMIN_USER, entityType, 3));

        final List<SharedEntity.Identifier> inputList = Arrays.asList(new SharedEntity.Identifier(ID._456, entityType, user),
                new SharedEntity.Identifier(ID._789, entityType, user), new SharedEntity.Identifier(ID._123, entityType, user),
                new SharedEntity.Identifier(ID._999, entityType, user));

        testedObject.updateSequence(user, inputList);

        assertFavouriteSequence(inputGvs);
    }

    /**
     * Test update sequence where the list contains a non favourite. This is unlikely but hey lets test it
     */
    @Test
    public void testUpdateSequenceReordersWhereANonFavouriteIsInTheList()
    {
        final SharedEntity.TypeDescriptor<SharedEntity> entityType = entity.getEntityType();

        final List<GenericValue> inputGvs = new LinkedList<GenericValue>();
        inputGvs.add(storeTestEntity(new Long(1), user.getName(), SharedEntityType.DASHBOARD, 999));
        inputGvs.add(storeTestEntity(ID._123, user.getName(), entityType, 0, 2));
        inputGvs.add(storeTestEntity(ID._456, user.getName(), entityType, 1, 0));
        inputGvs.add(storeTestEntity(ID._789, user.getName(), entityType, 2, 1));
        inputGvs.add(storeTestEntity(ID._999, user.getName(), entityType, 3, 3));
        inputGvs.add(storeTestEntity(ID._999, NOTADMIN_USER, entityType, 3));

        final List<SharedEntity.Identifier> inputList = Arrays.asList(new SharedEntity.Identifier(ID._456, entityType, user),
                new SharedEntity.Identifier(ID._789, entityType, user), new SharedEntity.Identifier(ID._123, entityType, user),
                new SharedEntity.Identifier(ID._999, entityType, user), new SharedEntity.Identifier(ID._666, entityType, user));

        testedObject.updateSequence(user, inputList);

        assertFavouriteSequence(inputGvs);
    }

    /**
     * Test update sequence where the list is empty. Nothing should change
     */
    @Test
    public void testUpdateSequenceNoopForAnEmptyList()
    {
        final SharedEntity.TypeDescriptor<SharedEntity> entityType = entity.getEntityType();

        final List<GenericValue> inputGvs = new LinkedList<GenericValue>();
        inputGvs.add(storeTestEntity(new Long(1), user.getName(), SharedEntityType.DASHBOARD, 999));
        inputGvs.add(storeTestEntity(ID._123, user.getName(), entityType, 0));
        inputGvs.add(storeTestEntity(ID._456, user.getName(), entityType, 1));
        inputGvs.add(storeTestEntity(ID._789, user.getName(), entityType, 2));
        inputGvs.add(storeTestEntity(ID._999, user.getName(), entityType, 3));
        inputGvs.add(storeTestEntity(ID._999, NOTADMIN_USER, entityType, 3));

        testedObject.updateSequence(user, Collections.<SharedEntity> emptyList());

        assertFavouriteSequence(inputGvs);
    }

    private void assertFavouriteSequence(final List<GenericValue> expectedGVS)
    {
        // expected associations from OfBiz, especially their expected sequence
        final List<Long> expectedSequence = Lists.transform(expectedGVS, new Function<GenericValue, Long>()
        {

            @Override
            public Long apply(final GenericValue input)
            {
                return input.getLong(Column.SEQUENCE);
            }

        });

        // load actual associations from OfBiz, and transforms them into the their sequence
        final List<Long> actualSequence = Lists.transform(expectedGVS, new Function<GenericValue, Long>()
        {

            @Override
            public Long apply(final GenericValue input)
            {
                final List<GenericValue> actualGVs = ofBizDelegator.findByAnd(
                        OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                        ImmutableMap.<String, Object> builder()
                                .put(Column.ENTITY_TYPE, input.getString(Column.ENTITY_TYPE))
                                .put(Column.ENTITY_ID, input.getLong(Column.ENTITY_ID))
                                .put(Column.USERNAME, input.getString(Column.USERNAME))
                                .build());
                // just to be sure, that only one entity was found
                assertEquals(1, actualGVs.size());
                return actualGVs.get(0).getLong(Column.SEQUENCE);
            }

        });
        assertEquals(expectedSequence, actualSequence);
    }

    private GenericValue storeTestEntity(final Long entityId, final String userName,
            final SharedEntity.TypeDescriptor<? extends SharedEntity> entityType, final long sequence, final long sequenceNew)
    {
        final GenericValue gv = storeTestEntity(entityId, userName, entityType, sequence);
        gv.set("sequence", new Long(sequenceNew));
        return gv;
    }

    private GenericValue storeTestEntity(final Long entityId, final String userName,
            final SharedEntity.TypeDescriptor<? extends SharedEntity> entityType, final long sequence)
    {
        return UtilsForTests.getTestEntity(
                OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                ImmutableMap.builder()
                        .put(Column.USERNAME, userName)
                        .put(Column.ENTITY_TYPE, entityType.getName())
                        .put(Column.ENTITY_ID, entityId)
                        .put(Column.SEQUENCE, new Long(sequence))
                        .build());
    }

}
