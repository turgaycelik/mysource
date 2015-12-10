package com.atlassian.jira.workflow;

import java.util.Date;
import java.util.Map;

import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.2
 */
public class TestDraftWorkflowSchemeBuilder
{
    @Test
    public void testBuilder()
    {
        String name = "name";
        String description = "description";
        Map<String,String> mappings = MapBuilder.build(null, "wtf", "one", "two");
        Long id = 78L;
        MockApplicationUser lastModifiedUser = new MockApplicationUser("copy");
        Date lastModifiedDate = new Date();

        final MockAssignableWorkflowScheme parent = new MockAssignableWorkflowScheme().setName(name).setDescription(description);

        DraftWorkflowSchemeImpl scheme = new DraftWorkflowSchemeImpl(id, mappings, lastModifiedUser, lastModifiedDate, parent);
        DraftWorkflowScheme.Builder builder = new DraftWorkflowSchemeBuilder(scheme);

        assertEquals(name, builder.getName());
        assertEquals(description, builder.getDescription());
        assertEquals(mappings, builder.getMappings());
        assertEquals(id, builder.getId());
        assertEquals("wtf", builder.getDefaultWorkflow());
        assertEquals("two", builder.getMapping("one"));
        assertEquals(lastModifiedDate, builder.getLastModifiedDate());
        assertEquals(lastModifiedUser, builder.getLastModifiedUser());
        assertTrue(builder.isDraft());
        assertFalse(builder.isDefault());

        DraftWorkflowScheme copy = builder.build();
        assertEquals(name, copy.getName());
        assertEquals(description, copy.getDescription());
        assertEquals(mappings, copy.getMappings());
        assertTrue(copy.isDraft());
        assertFalse(copy.isDefault());
        assertEquals(lastModifiedUser, copy.getLastModifiedUser());
        assertEquals(lastModifiedDate, copy.getLastModifiedDate());
    }
}
