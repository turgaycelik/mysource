package com.atlassian.jira.issue.fields.screen;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.AbstractGVBean}.
 *
 * @since v4.1
 */
public class TestAbstractGVBean
{
    @Test
    public void testSetGenericValue()
    {
        final AtomicBoolean ran = new AtomicBoolean(false);
        AbstractGVBean testBean = new AbstractGVBean()
        {
            @Override
            protected void init()
            {
                ran.set(true);
            }
        };

        final MockGenericValue value = new MockGenericValue("doesntMatter");
        testBean.setGenericValue(value);
        assertTrue(ran.get());
        assertSame(value, testBean.getGenericValue());
        assertFalse(testBean.isModified());
    }

    @Test
    public void testUpdateGVNoGV()
    {
        final TestBean bean = new TestBean();
        assertFalse(bean.isModified());
        bean.updateGV("bb", "b");
        assertTrue(bean.isModified());
    }

    @Test
    public void testUpdateGVWithGv()
    {
        final MockGenericValue genericValue = new MockGenericValue("test");
        genericValue.set("b", "b");

        final TestBean bean = new TestBean();
        bean.setGenericValue(genericValue);
        assertFalse(bean.isModified());
        bean.updateGV("b", "b");
        assertFalse(bean.isModified());
        bean.updateGV("b", 1);
        assertTrue(bean.isModified());
        bean.setModified(false);
        bean.updateGV("c", "c");
        assertTrue(bean.isModified());
    }

    private static class TestBean extends AbstractGVBean
    {
        @Override
        protected void init()
        {
        }
    }
}
