package com.atlassian.jira.issue.fields.rest.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test basic JSON mapping
 * @since v5.0
 */
public class TestJsonDataValueConversion
{
    public static class MyBean {
        @JsonProperty public String foo;
        @JsonProperty public Long bar;
    }

    @Test
    public void testSimpleTypes()
    {
        assertConvertBackAndForth("moo", String.class);
        assertConvertBackAndForth("moo", Object.class);

        assertConvertBackAndForth(100L, Long.class);
        assertConvertBackAndForth(100, Object.class);

        assertConvertBackAndForth(3.142D, Double.class);
        assertConvertBackAndForth(3.142, Double.class);
        assertConvertBackAndForth(3.142D, Object.class);

        assertConvertBackAndForth(null, Object.class);

        assertConvertBackAndForth(true, Boolean.class);
        assertConvertBackAndForth(true, Object.class);
    }

    @Test
    public void testArrays()
    {
        ErrorCollection errors = new SimpleErrorCollection();
        List<?> original = Arrays.asList("moo", 100, 3.142, null, true);
        List<?> result = new JsonData(original).convertValue("blah", List.class, errors);
        assertFalse(errors.hasAnyErrors());
        assertNotNull(result);

        assertEquals("moo", result.get(0));
        assertEquals(100, result.get(1));
        assertEquals(3.142, result.get(2));
        assertEquals(null, result.get(3));
        assertEquals(true, result.get(4));
    }

    private static void assertConvertBackAndForth(Object data, Class<?> type)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        assertEquals(data, new JsonData(data).convertValue("myprop", type, errors));
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void testParseMyBean()
    {
        Map json = new HashMap();
        json.put("foo", "str");
        json.put("bar", 42L);

        JsonData jd = new JsonData(json);

        ErrorCollection errors = new SimpleErrorCollection();
        final MyBean bean = jd.convertValue("mybean", MyBean.class, errors);
        assertFalse(errors.hasAnyErrors());
        assertNotNull(bean);

        assertEquals("str", bean.foo);
        assertEquals(Long.valueOf(42), bean.bar);
    }

    @Test
    public void testBadParseMyBean()
    {
        JsonData jd = new JsonData(new ArrayList());

        ErrorCollection errors = new SimpleErrorCollection();
        final MyBean bean = jd.convertValue("mybean", MyBean.class, errors);
        assertTrue(errors.hasAnyErrors());
        assertNull(bean);

    }
}
