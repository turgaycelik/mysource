package com.atlassian.jira.functest.unittests.config.dashboard;

import com.atlassian.jira.functest.config.dashboard.ConfigGadgetSupport;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.functest.config.dashboard.ConfigGadgetSupport}.
 *
 * @since v4.2
 */
public class TestConfigGadgetSupport extends TestCase
{
    public void testSyncWithCreate() throws Exception
    {
        IntegerConfigGadgetSupport configGadgetSupport = new IntegerConfigGadgetSupport();
        assertTrue(configGadgetSupport.sync(Arrays.asList(2, 3 ,6)));

        configGadgetSupport.assertDeletes();
        configGadgetSupport.assertCreates(2, 3, 6);
        configGadgetSupport.assertUpdates();
    }

    public void testSyncWithCreateNoChanges() throws Exception
    {
        IntegerConfigGadgetSupport configGadgetSupport = new IntegerConfigGadgetSupport();
        assertFalse(configGadgetSupport.sync(Arrays.asList(1, 3 ,7)));

        configGadgetSupport.assertDeletes();
        configGadgetSupport.assertCreates(1, 3, 7);
        configGadgetSupport.assertUpdates();
    }

    public void testSyncWithUpdateNoChanges() throws Exception
    {
        IntegerConfigGadgetSupport configGadgetSupport = new IntegerConfigGadgetSupport(1, 3, 7);
        assertFalse(configGadgetSupport.sync(Arrays.asList(1, 3 ,7)));

        configGadgetSupport.assertDeletes();
        configGadgetSupport.assertCreates();
        configGadgetSupport.assertUpdates(1, 3, 7);
    }

    public void testSyncWithUpdateChanges() throws Exception
    {
        IntegerConfigGadgetSupport configGadgetSupport = new IntegerConfigGadgetSupport(2, 3);
        assertTrue(configGadgetSupport.sync(Arrays.asList(2, 3)));

        configGadgetSupport.assertDeletes();
        configGadgetSupport.assertCreates();
        configGadgetSupport.assertUpdates(2, 3);
    }

    public void testSyncWithDelete() throws Exception
    {
        IntegerConfigGadgetSupport configGadgetSupport = new IntegerConfigGadgetSupport(2, 3, 6);
        assertTrue(configGadgetSupport.sync(Arrays.asList(2, 3)));

        configGadgetSupport.assertDeletes(6);
        configGadgetSupport.assertCreates();
        configGadgetSupport.assertUpdates(2, 3);
    }

    public void testSyncWithDeleteNoChanges() throws Exception
    {
        IntegerConfigGadgetSupport configGadgetSupport = new IntegerConfigGadgetSupport(1, 3, 7);
        assertFalse(configGadgetSupport.sync(Collections.<Integer>emptyList()));

        configGadgetSupport.assertDeletes(1, 3, 7);
        configGadgetSupport.assertCreates();
        configGadgetSupport.assertUpdates();
    }

    public void testSyncComplex() throws Exception
    {
        IntegerConfigGadgetSupport configGadgetSupport = new IntegerConfigGadgetSupport(2, 3, 6);
        assertTrue(configGadgetSupport.sync(Arrays.asList(3, 221)));

        configGadgetSupport.assertDeletes(2, 6);
        configGadgetSupport.assertCreates(221);
        configGadgetSupport.assertUpdates(3);
    }

    private static class IntegerConfigGadgetSupport extends ConfigGadgetSupport<Integer>
    {
        private final List<Integer> load;
        private final List<Integer> creates = new ArrayList<Integer>();
        private final List<Integer> updates = new ArrayList<Integer>();
        private final List<Integer> deletes = new ArrayList<Integer>();

        public IntegerConfigGadgetSupport(Integer ...returns)
        {
            super(null);
            load = Arrays.asList(returns);
        }

        @Override
        public List<Integer> loadAll()
        {
            return load;
        }

        @Override
        public boolean create(final Integer object)
        {
            creates.add(object);
            return (object % 2) == 0;
        }

        @Override
        public boolean update(final Integer oldObj, final Integer newObj)
        {
            assertEquals(oldObj, newObj);
            updates.add(newObj);
            return (newObj % 2) == 0;
        }

        @Override
        public boolean delete(final Integer obj)
        {
            deletes.add(obj);
            return (obj % 2) == 0;
        }

        @Override
        public Long getId(final Integer obj)
        {
            return obj.longValue();
        }

        void assertDeletes(Integer...expectedDeletes)
        {
            assertEquals(Arrays.asList(expectedDeletes), deletes);
        }

        void assertUpdates(Integer...expectedUpdates)
        {
            assertEquals(Arrays.asList(expectedUpdates), updates);
        }

        void assertCreates(Integer...expectedCreates)
        {
            assertEquals(Arrays.asList(expectedCreates), creates);
        }
    }
}
