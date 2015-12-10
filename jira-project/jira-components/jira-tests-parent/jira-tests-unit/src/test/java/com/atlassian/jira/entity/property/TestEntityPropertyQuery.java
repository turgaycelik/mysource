package com.atlassian.jira.entity.property;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.junit.rules.AvailableInContainer;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v6.1
 */
public class TestEntityPropertyQuery
{
    @Mock
    @AvailableInContainer
    private EventPublisher mockEventPublisher;

    final JsonEntityPropertyManagerImpl entityPropertyManager = new JsonEntityPropertyManagerImpl(null, mockEventPublisher);

    @Test
    public void testMinimumSearchRequirements()
    {
        assertNotExecutable(entityPropertyManager.query());
        assertNotExecutable(entityPropertyManager.query().entityId(42L));
        assertNotExecutable(entityPropertyManager.query().keyPrefix("prefix"));
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotSearchByBothKeyAndKeyPrefix()
    {
        entityPropertyManager.query().key("key").keyPrefix("prefix");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotSearchByBlankKey()
    {
        entityPropertyManager.query().key("    ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotSearchByBlankKeyPrefix()
    {
        entityPropertyManager.query().keyPrefix("    ");
    }

    @Test
    public void testQueryGetsTheRightFieldValues1()
    {
        final String desc = entityPropertyManager.query()
                .entityName("asdf")
                .keyPrefix("prefix")
                .toString();
        assertEquals("ExecutableQuery[JsonEntityPropertyQuery[entityName=asdf,entityId=null,key=null,keyPrefix=prefix,offset=0,maxResults=0]]", desc);
    }

    @Test
    public void testQueryGetsTheRightFieldValues2()
    {
        final String desc = entityPropertyManager.query()
                .entityId(42L)
                .key("fred")
                .offset(1)
                .maxResults(3)
                .toString();
        assertEquals("ExecutableQuery[JsonEntityPropertyQuery[entityName=null,entityId=42,key=fred,keyPrefix=null,offset=1,maxResults=3]]", desc);
    }

    private static void assertNotExecutable(EntityPropertyQuery<?> query)
    {
        if (query instanceof EntityPropertyQuery.ExecutableQuery)
        {
            fail("Should not have been an executable query: " + query);
        }
    }
}
