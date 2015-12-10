package com.atlassian.jira.web.component.multiuserpicker;

import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestUserBean
{
    @Test
    public void testBean() throws Exception
    {
        UserBean bean1 = new UserBean("admin");
        assertEquals("", bean1.getFullName());
        assertEquals("admin", bean1.getName());
        assertFalse(bean1.isReal());

        UserBean bean2 = new UserBean("admin", "Administrator");
        assertEquals("Administrator", bean2.getFullName());
        assertEquals("admin", bean2.getName());
        assertTrue(bean2.isReal());
    }

    @Test
    public void testComparator() throws Exception
    {
        final UserBean bean1 = new UserBean("fred");
        final UserBean bean2 = new UserBean("fred");
        final UserBean bean3 = new UserBean("admin");
        final UserBean bean4 = new UserBean("admin", "Admin");
        final UserBean bean5 = new UserBean("admin", "Zorro");
        final UserBean bean6 = new UserBean("fred", "Zorro");

        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean1, bean1) == 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean1, bean2) == 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean1, bean3) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean1, bean4) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean1, bean5) < 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean1, bean6) < 0);

        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean2, bean1) == 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean2, bean2) == 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean2, bean3) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean2, bean4) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean2, bean5) < 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean2, bean6) < 0);

        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean3, bean1) < 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean3, bean2) < 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean3, bean3) == 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean3, bean4) == 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean3, bean5) < 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean3, bean6) < 0);

        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean4, bean1) < 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean4, bean2) < 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean4, bean3) == 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean4, bean4) == 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean4, bean5) < 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean4, bean6) < 0);

        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean5, bean1) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean5, bean2) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean5, bean3) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean5, bean4) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean5, bean5) == 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean5, bean6) < 0);

        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean6, bean1) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean6, bean2) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean6, bean3) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean6, bean4) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean6, bean5) > 0);
        assertTrue(new UserBean.UserBeanComparator(Locale.ENGLISH).compare(bean6, bean6) == 0);
    }
}
