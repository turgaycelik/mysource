package com.atlassian.jira.workflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.gzipfilter.org.apache.commons.lang.builder.ToStringBuilder;
import com.atlassian.gzipfilter.org.apache.commons.lang.builder.ToStringStyle;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.2
 */
public class TestWorkflowSchemeStoreSupport
{
    private MockOfBizDelegator delegator;
    private WorkflowSchemeStoreSupport<MyWorkflowSchemeState> support;

    @Before
    public void setup()
    {
        delegator = new MockOfBizDelegator()
        {
            @Override
            public List<GenericValue> getRelated(String relationName, GenericValue gv)
            {
                if (MyWorkflowSchemeState.RELATIONSHIP.equals(relationName))
                {
                    return findByAnd(MyWorkflowSchemeState.TABLE_ENTITY,
                            FieldMap.build(MyWorkflowSchemeState.FIELD_SCHEME, gv.getLong(MyWorkflowSchemeState.FIELD_ID)));
                }
                else
                {
                    return super.getRelated(relationName, gv);
                }
            }
        };

        support = new WorkflowSchemeStoreSupport<MyWorkflowSchemeState>(delegator, new TestDelegate());
    }

    @Test
    public void create()
    {
        final MyWorkflowSchemeState state = new MyWorkflowSchemeState().setName("create")
                .setMapping("one", "one");

        final MyWorkflowSchemeState actualState = support.create(state);
        state.setId(actualState.getId());

        assertEquals(Sets.newHashSet(state), MyWorkflowSchemeState.getAll(delegator));
    }

    @Test
    public void update()
    {
        final MyWorkflowSchemeState deleteMe = new MyWorkflowSchemeState()
                .setId(10000L)
                .setName("deleteMe")
                .setDefault("wtf")
                .saveTo(delegator);

        final MyWorkflowSchemeState keepMe = new MyWorkflowSchemeState()
                .setId(10002L)
                .setName("keepMe")
                .setMapping("keep", "keep").setMapping("wtf", "wtf")
                .saveTo(delegator);

        //Remove default and add new mapping.
        MyWorkflowSchemeState state = support.get(deleteMe.getId());
        state.clear().setMapping("new", "mapping").setName("deleteMe2");
        state = support.update(state);
        assertEquals(deleteMe.clear().setMapping("new", "mapping").setName("deleteMe2"), state);

        //Add default and change exisiting entity.
        state = support.get(keepMe.getId());
        state.setDefault("DefaultWorkflow").setMapping("wtf", "NewDefaultWorkflow").setName("keep2");
        state = support.update(state);

        keepMe.setDefault("DefaultWorkflow").setMapping("wtf", "NewDefaultWorkflow").setName("keep2");
        assertEquals(keepMe, state);

        final Set<MyWorkflowSchemeState> allSchemes = MyWorkflowSchemeState.getAll(delegator);
        assertEquals(Sets.newHashSet(deleteMe, keepMe), allSchemes);
    }

    // UpgradeTask_Build6123 had a bug which could result in orphaned defaultWorkflowEntitySchemes, which exposed a bug
    // in WorkflowSchemeStoreSupport, which tried to update a deleted duplicate row instead of the dupe being kept.
    // This tests the fix for the latter bug.
    @Test
    public void updateWithDuplicateEntity() {
        MyWorkflowSchemeState state = new MyWorkflowSchemeState()
                .setId(1000L)
                .setName("deleteMe")
                .setDefault("um")
                .saveTo(delegator);

        final Map<String, Object> orphanedSchemeEntity = MapBuilder.<String, Object>build("scheme", state.getId(),
                "issuetype", 0L, "workflow", "huh");
        delegator.createValue(MyWorkflowSchemeState.TABLE_ENTITY, orphanedSchemeEntity);

        state.setDefault("wat");
        MyWorkflowSchemeState newState = support.update(state);
        assertEquals(state, newState);
    }

    @Test
    public void testDelete()
    {
        final MyWorkflowSchemeState deleted = new MyWorkflowSchemeState()
                .setId(10000L)
                .setMapping("gone", "deleted").saveTo(delegator);

        final MyWorkflowSchemeState keepMe = new MyWorkflowSchemeState()
                .setId(10002L).setMapping("keep", "keep").saveTo(delegator);

        assertTrue(support.delete(10000));
        assertEquals(singleton(keepMe), MyWorkflowSchemeState.getAll(delegator));
        assertFalse(support.delete(10000));

        assertFalse(support.delete(deleted));
        assertEquals(singleton(keepMe), MyWorkflowSchemeState.getAll(delegator));
        assertTrue(support.delete(keepMe));
        assertTrue(MyWorkflowSchemeState.getAll(delegator).isEmpty());
        assertFalse(support.delete(keepMe));

    }

    @Test
    public void testGet()
    {
        new MyWorkflowSchemeState()
                .setId(10000L)
                .setMapping("gone", "deleted")
                .saveTo(delegator);

        final MyWorkflowSchemeState keepMe = new MyWorkflowSchemeState()
                .setId(10002L)
                .setMapping("keep", "keep")
                .saveTo(delegator);

        assertNull(support.get(1));
        final MyWorkflowSchemeState state = support.get(10002L);
        assertEquals(keepMe, state);
        assertNull(support.get(102728282L));
    }


    @Test
    public void testGetAll()
    {
        final MyWorkflowSchemeState deleteMe = new MyWorkflowSchemeState()
                .setId(10000L)
                .setDefault("default")
                .saveTo(delegator);

        final MyWorkflowSchemeState keepMe = new MyWorkflowSchemeState()
                .setId(10002L)
                .setMapping("keep", "keep").setMapping("wtf", "wft")
                .saveTo(delegator);

        assertEquals(Sets.newHashSet(deleteMe, keepMe), Sets.newHashSet(support.getAll()));
    }

    @Test
    public void testRenameWorkflow()
    {
        final MyWorkflowSchemeState deleteMe = new MyWorkflowSchemeState()
                .setId(10000L)
                .setDefault("wtf").saveTo(delegator);

        final MyWorkflowSchemeState keepMe = new MyWorkflowSchemeState()
                .setId(10002L)
                .setMapping("keep", "keep").setMapping("wtf", "wtf")
                .saveTo(delegator);

        final String newName = "What the ....";
        assertTrue(support.renameWorkflow("wtf", newName));

        Set<MyWorkflowSchemeState> expected = Sets.newHashSet();
        expected.add(deleteMe.setDefault(newName));
        expected.add(keepMe.setMapping("wtf", newName));

        assertEquals(expected, Sets.newHashSet(support.getAll()));
    }

    @Test
    public void testGetSingleDraftUsingWorkflow()
    {
        MockJiraWorkflow workflow = new MockJiraWorkflow("Test workflow");

        MyWorkflowSchemeState scheme = new MyWorkflowSchemeState()
                .setId(10001L)
                .setMapping("issuetype", workflow.getName())
                .saveTo(delegator);

        MyWorkflowSchemeState scheme2 = new MyWorkflowSchemeState()
                .setId(10002L)
                .setDefault(workflow.getName())
                .saveTo(delegator);

        new MyWorkflowSchemeState()
                .setId(10003L)
                .setDefault(workflow.getName() + "1").setMapping("Bug", workflow.getName() + "2")
                .saveTo(delegator);

        Iterable<MyWorkflowSchemeState> drafts = support.getSchemesUsingWorkflow(workflow);
        assertEquals(Sets.newHashSet(scheme, scheme2), Sets.newHashSet(drafts));
    }

    @Test
    public void testGetNoneDraftsUsingWorkflow()
    {
        MockJiraWorkflow workflow = new MockJiraWorkflow("Test workflow");
        OfBizDraftWorkflowSchemeStore store = new OfBizDraftWorkflowSchemeStore(delegator);
        Iterable<DraftWorkflowSchemeStore.DraftState> drafts = store.getSchemesUsingWorkflow(workflow);
        assertTrue(Iterables.isEmpty(drafts));
    }

    private class TestDelegate
            implements WorkflowSchemeStoreSupport.Delegate<MyWorkflowSchemeState>
    {
        @Override
        public String schemeTable()
        {
            return MyWorkflowSchemeState.TABLE_SCHEME;
        }

        @Override
        public String entityTable()
        {
            return MyWorkflowSchemeState.TABLE_ENTITY;
        }

        @Override
        public String schemeToEntityRelationship()
        {
            return MyWorkflowSchemeState.RELATIONSHIP;
        }

        @Override
        public GenericValue create(MyWorkflowSchemeState state)
        {
            return state.createGV(delegator);
        }

        @Override
        public void update(MyWorkflowSchemeState state, GenericValue schemeGv)
        {
            state.updateGv(schemeGv);
            delegator.store(schemeGv);
        }

        @Override
        public MyWorkflowSchemeState get(GenericValue schemeGv, Map<String, String> map)
        {
            return new MyWorkflowSchemeState(schemeGv).setMappings(map);
        }
    }

    public static class MyWorkflowSchemeState implements WorkflowSchemeStore.State
    {
        private static final String TABLE_SCHEME = "Something";
        private static final String TABLE_ENTITY = "SomethingEntity";

        private static final String RELATIONSHIP = "SomethingRelationShip";

        private static final String FIELD_NAME = "name";
        private static final String FIELD_ID = "id";
        private static final String FIELD_SCHEME = "scheme";
        private static final String FIELD_WORKFLOW = "workflow";
        private static final String FIELD_ISSUE_TYPE = "issuetype";

        private Map<String, String> mappings = Maps.newHashMap();
        private Long id;
        private String name;

        public MyWorkflowSchemeState() {}

        public MyWorkflowSchemeState(GenericValue schemeGv)
        {
            this(schemeGv, Collections.<GenericValue>emptyList());
        }

        public MyWorkflowSchemeState(GenericValue schemeGv, List<GenericValue> entities)
        {
            setId(schemeGv.getLong(FIELD_ID));
            setName(schemeGv.getString(FIELD_NAME));

            for (GenericValue mapping : entities)
            {
                final String issueType = mapping.getString(FIELD_ISSUE_TYPE);
                final String workflow = mapping.getString(FIELD_WORKFLOW);

                setMapping(issueType.equals("0") ? null : issueType, workflow);
            }
        }

        @Override
        public String getDefaultWorkflow()
        {
            return mappings.get(null);
        }

        @Override
        public Long getId()
        {
            return id;
        }

        public MyWorkflowSchemeState setId(Long id)
        {
            this.id = id;
            return this;
        }

        private MyWorkflowSchemeState clear()
        {
            this.mappings.clear();
            return this;
        }

        private MyWorkflowSchemeState setMappings(Map<String, String> mappings)
        {
            this.mappings = mappings;
            return this;
        }

        private MyWorkflowSchemeState setMapping(String issueType, String workflow)
        {
            this.mappings.put(issueType, workflow);
            return this;
        }

        private MyWorkflowSchemeState setDefault(String workflow)
        {
            return setMapping(null, workflow);
        }

        private String getName()
        {
            return name;
        }

        private MyWorkflowSchemeState setName(String name)
        {
            this.name = name;
            return this;
        }

        @Override
        public Map<String, String> getMappings()
        {
            return mappings;
        }

        private GenericValue createGV(OfBizDelegator delegator)
        {
            return delegator.createValue(TABLE_SCHEME, FieldMap.build(FIELD_NAME, name));
        }

        private void updateGv(GenericValue value)
        {
            value.put(FIELD_NAME, name);
        }


        private MyWorkflowSchemeState saveTo(OfBizDelegator delegator)
        {
            delegator.createValue(TABLE_SCHEME, FieldMap.build(FIELD_NAME, name, FIELD_ID, id));
            for (Map.Entry<String, String> mapping : mappings.entrySet())
            {
                String key = mapping.getKey();
                if (key == null)
                {
                    key = "0";
                }
                delegator.createValue(TABLE_ENTITY, FieldMap.build(FIELD_ISSUE_TYPE, key)
                        .add(FIELD_WORKFLOW, mapping.getValue()).add(FIELD_SCHEME, id));
            }

            return this;
        }

        private static Set<MyWorkflowSchemeState> getAll(OfBizDelegator delegator)
        {
            final Set<MyWorkflowSchemeState> result = Sets.newHashSet();
            final List<GenericValue> all = delegator.findAll(TABLE_SCHEME);
            for (GenericValue schemeGv : all)
            {
                final Long schemeId = schemeGv.getLong(FIELD_ID);

                final List<GenericValue> related = delegator.findByAnd(TABLE_ENTITY,
                        ImmutableMap.of(FIELD_SCHEME, schemeId));

                result.add(new MyWorkflowSchemeState(schemeGv, related));
            }
            return result;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            MyWorkflowSchemeState that = (MyWorkflowSchemeState) o;

            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (mappings != null ? !mappings.equals(that.mappings) : that.mappings != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = mappings != null ? mappings.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
