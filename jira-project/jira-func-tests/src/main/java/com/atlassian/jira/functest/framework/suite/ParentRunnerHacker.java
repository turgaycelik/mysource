package com.atlassian.jira.functest.framework.suite;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;

import java.lang.reflect.Method;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Retrieves information about children of a parent runner in a hacky manner - the design of JUnit4 does not allow us
 * to do it in a nice way!
 *
 * @since v4.4
 */
public final class ParentRunnerHacker<T>
{
    private final ParentRunner<T> runner;
    private final Method getChildren;
    private final Method describeChild;
    private final Method runChild;

    public ParentRunnerHacker(ParentRunner<T> runner)
    {
        this.runner = notNull(runner);
        this.getChildren = findMethodImpl("getChildren");
        this.describeChild = findMethodImpl("describeChild", Object.class);
        this.runChild = findMethodImpl("runChild", Object.class, RunNotifier.class);
        this.getChildren.setAccessible(true);
        this.describeChild.setAccessible(true);
        this.runChild.setAccessible(true);
    }

    private Method findMethodImpl(String name, Class<?>... params)
    {
        Method answer = null;
        Class<?> runnerClass = runner.getClass();
        while (answer == null)
        {
            try
            {
                answer = runnerClass.getDeclaredMethod(name, params);
            }
            catch (NoSuchMethodException e)
            {
                runnerClass = runnerClass.getSuperclass();
                if (runnerClass == ParentRunner.class)
                {
                    throw new AssertionError("went all the way from " + runner.getClass().getName()
                            + " down to ParentRunner and could not find method " + name);
                }
            }
        }
        return answer;
    }

    @SuppressWarnings ( { "unchecked" })
    public List<T> getChildren()
    {
        try
        {
            return (List) getChildren.invoke(runner);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Description describeChild(T child)
    {
        try
        {
            return (Description) describeChild.invoke(runner, child);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void runChild(T child, RunNotifier notifier)
    {
        try
        {
            runChild.invoke(runner, child, notifier);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
