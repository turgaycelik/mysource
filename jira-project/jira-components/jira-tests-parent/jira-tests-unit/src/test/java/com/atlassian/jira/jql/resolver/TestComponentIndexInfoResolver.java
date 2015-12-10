package com.atlassian.jira.jql.resolver;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestComponentIndexInfoResolver extends MockControllerTestCase
{
    @Test
    public void testGetIndexedValuesStringHappyPath() throws Exception
    {
        final MockComponent mockComponent1 = new MockComponent(1L, "component1");
        final MockComponent mockComponent2 = new MockComponent(2L, "component1");

        final NameResolver<ProjectComponent> nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.getIdsFromName("component1");
        mockController.setReturnValue(CollectionBuilder.newBuilder("1", "2").asList());
        mockController.replay();

        ComponentIndexInfoResolver resolver = new ComponentIndexInfoResolver(nameResolver);

        final List<String> result = resolver.getIndexedValues("component1");
        assertEquals(2, result.size());
        assertTrue(result.contains(mockComponent1.getId().toString()));
        assertTrue(result.contains(mockComponent2.getId().toString()));

        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesStringIsId() throws Exception
    {
        final NameResolver<ProjectComponent> nameResolver = mockController.getMock(NameResolver.class);
        ComponentIndexInfoResolver resolver = new ComponentIndexInfoResolver(nameResolver);

        nameResolver.getIdsFromName("2");
        mockController.setReturnValue(Collections.emptyList());
        nameResolver.idExists(2L);
        mockController.setReturnValue(true);
        mockController.replay();

        final List<String> result = resolver.getIndexedValues("2");
        assertEquals(1, result.size());
        assertTrue(result.contains("2"));
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesStringIsIdDoesntExist() throws Exception
    {
        final NameResolver<ProjectComponent> nameResolver = mockController.getMock(NameResolver.class);
        ComponentIndexInfoResolver resolver = new ComponentIndexInfoResolver(nameResolver);

        nameResolver.getIdsFromName("2");
        mockController.setReturnValue(Collections.emptyList());
        nameResolver.idExists(2L);
        mockController.setReturnValue(false);
        mockController.replay();

        final List<String> result = resolver.getIndexedValues("2");
        assertEquals(0, result.size());
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesStringIsNotNameOrId() throws Exception
    {
        final NameResolver<ProjectComponent> nameResolver = mockController.getMock(NameResolver.class);
        ComponentIndexInfoResolver resolver = new ComponentIndexInfoResolver(nameResolver);

        nameResolver.getIdsFromName("abc");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final List<String> result = resolver.getIndexedValues("abc");
        assertEquals(0, result.size());
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesLongExists() throws Exception
    {
        final NameResolver<ProjectComponent> nameResolver = mockController.getMock(NameResolver.class);
        ComponentIndexInfoResolver resolver = new ComponentIndexInfoResolver(nameResolver);

        nameResolver.idExists(2L);
        mockController.setReturnValue(true);
        mockController.replay();

        final List<String> result = resolver.getIndexedValues(2L);
        assertEquals(1, result.size());
        assertTrue(result.contains("2"));
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesLongIdIsName() throws Exception
    {
        final NameResolver<ProjectComponent> nameResolver = mockController.getMock(NameResolver.class);
        ComponentIndexInfoResolver resolver = new ComponentIndexInfoResolver(nameResolver);

        final MockComponent mockComponent1 = new MockComponent(2L, "100");
        final MockComponent mockComponent2 = new MockComponent(1L, "100");

        nameResolver.idExists(100L);
        mockController.setReturnValue(false);
        nameResolver.getIdsFromName("100");
        mockController.setReturnValue(CollectionBuilder.newBuilder("2", "1").asList());
        mockController.replay();

        final List<String> result = resolver.getIndexedValues(100L);
        assertEquals(2, result.size());
        assertTrue(result.contains(mockComponent1.getId().toString()));
        assertTrue(result.contains(mockComponent2.getId().toString()));
        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesLongIdIsNameDoesntExist() throws Exception
    {
        final NameResolver<ProjectComponent> nameResolver = mockController.getMock(NameResolver.class);
        ComponentIndexInfoResolver resolver = new ComponentIndexInfoResolver(nameResolver);

        nameResolver.idExists(100L);
        mockController.setReturnValue(false);
        nameResolver.getIdsFromName("100");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final List<String> result = resolver.getIndexedValues(100L);
        assertTrue(result.isEmpty());
        mockController.verify();
    }

    @Test
    public void testGetIndexedValue() throws Exception
    {
        final NameResolver<ProjectComponent> nameResolver = mockController.getMock(NameResolver.class);
        ComponentIndexInfoResolver resolver = new ComponentIndexInfoResolver(nameResolver);

        final MockComponent mockComponent1 = new MockComponent(1L, "Component 1");

        mockController.replay();

        final String indexedValue = resolver.getIndexedValue(mockComponent1);
        assertEquals("1", indexedValue);

        try
        {
            resolver.getIndexedValue(null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        mockController.verify();
    }

    static class MockComponent implements ProjectComponent
    {
        private final Long id;
        private final String name;

        MockComponent(Long id, String name)
        {
            this.id = id;
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public Long getId()
        {
            return id;
        }

        public String getDescription()
        {
            return null;
        }

        public String getLead()
        {
            return null;
        }

        @Override
        public ApplicationUser getComponentLead()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        public Long getProjectId()
        {
            return null;
        }

        public long getAssigneeType()
        {
            return 0;
        }

        public GenericValue getGenericValue()
        {
            return null;
        }
    }
}
