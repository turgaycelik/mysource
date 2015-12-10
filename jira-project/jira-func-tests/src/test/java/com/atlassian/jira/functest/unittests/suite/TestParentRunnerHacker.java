package com.atlassian.jira.functest.unittests.suite;

import com.atlassian.jira.functest.framework.suite.ParentRunnerHacker;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link com.atlassian.jira.functest.framework.suite.ParentRunnerHacker}.
 *
 * @since v4.4
 */
public class TestParentRunnerHacker
{

    private static class MockParentRunner extends ParentRunner<String>
    {
        private final List<String> children;
        private String lastRunChild;

        /**
         * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
         */
        protected MockParentRunner(String... children) throws InitializationError
        {
            super(null);
            this.children = Arrays.asList(children);
        }

        @Override
        protected List<String> getChildren()
        {
            return children;
        }

        @Override
        protected Description describeChild(String child)
        {
            return Description.createTestDescription(getClass(), child);
        }

        @Override
        protected void runChild(String child, RunNotifier notifier)
        {
            this.lastRunChild = child;
        }
    }

    @Test
    public void shouldCallGetChildren() throws Exception
    {
        final ParentRunnerHacker<String> tested = new ParentRunnerHacker<String>(new MockParentRunner("one", "two", "three"));
        assertEquals(Arrays.asList("one", "two", "three"), tested.getChildren());
    }

    @Test
    public void shouldCallDescribeChild() throws Exception
    {
        final ParentRunnerHacker<String> tested = new ParentRunnerHacker<String>(new MockParentRunner());
        Description result = tested.describeChild("test");
        assertEquals("test(" + MockParentRunner.class.getName() +  ")", result.getDisplayName());
        assertTrue(result.isTest());
    }

    @Test
    public void shouldCallRunChild() throws Exception
    {
        MockParentRunner mockRunner = new MockParentRunner();
        final ParentRunnerHacker<String> tested = new ParentRunnerHacker<String>(mockRunner);
        tested.runChild("test", null);
        assertEquals("test", mockRunner.lastRunChild);
    }

}
