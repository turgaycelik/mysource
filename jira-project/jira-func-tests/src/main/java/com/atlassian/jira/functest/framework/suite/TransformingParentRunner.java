package com.atlassian.jira.functest.framework.suite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

/**
 * <p>
 * A parent runner implementation that wraps another parent runner and applies an ordered list of transforms over the
 * wrapped runner children, such that only the resulting list of children will be run.
 *
 * <p>
 * The transforms operate on input list of descriptions and return a new copy of this list with any necessary modifications.
 * Only corresponding children of the descriptions will be executed by this runner. The transforms get applied recursively
 * over the whole runner tree.
 *
 * @see SuiteTransform
 * @since v4.4
 */
public class TransformingParentRunner<T> extends ParentRunner<T>
{
    public static List<Description> applyTransforms(Iterable<Description> input, Iterable<SuiteTransform> transforms)
    {
        Iterable<Description> answer = input;
        for (SuiteTransform transform : transforms)
        {
            answer = transform.apply(ImmutableList.copyOf(answer));
        }
        return Lists.newArrayList(answer);
    }

    private final String name;
    private final ParentRunnerHacker<T> parentRunnerHacker;
    private final List<SuiteTransform> transforms = Lists.newArrayList();
    private final RunnerChildList<T> childList;
    private final List<T> children;

    public TransformingParentRunner(String name, ParentRunner<T> original, Iterable<SuiteTransform> transforms) throws InitializationError
    {
        super(original.getTestClass().getJavaClass());
        this.name = name;
        this.parentRunnerHacker = new ParentRunnerHacker<T>(original);
        Iterables.addAll(this.transforms, transforms);
        this.childList = new RunnerChildList<T>(original);
        this.children = filterChildren();
    }

    public TransformingParentRunner(ParentRunner<T> original, Iterable<SuiteTransform> transforms) throws InitializationError
    {
        this(null, original, transforms);
    }

    private List<Description> applyTransforms()
    {
        Iterable<Description> answer = childList.descriptions();
        return applyTransforms(answer, transforms);
    }

    private List<T> filterChildren() throws InitializationError
    {
        List<T> filtered = childList.matchingChildren(applyTransforms());
        List<T> answer = Lists.newArrayList();
        for (T child : filtered)
        {
            if (child instanceof ParentRunner<?>)
            {
                ParentRunner<?> childRunner = (ParentRunner<?>) child;
                @SuppressWarnings ( { "unchecked" }) T wrapped = (T) wrap(childRunner, transforms);
                answer.add(wrapped);
            }
            else
            {
                answer.add(child);
            }
        }
        return answer;
    }

    /**
     * <p>
     * Wrap child of this runner that itself is parent runner in an instance that will provide transformations over
     * its children (if necessary).
     *
     * <p>
     * Override this method to handle specific parent runner subclasses that override its protected or public methods,
     * linke e.g. {@link org.junit.runners.ParentRunner#run(org.junit.runner.notification.RunNotifier)}. Otherwise this
     * class will break those runners.
     *
     * @param original original runner instance
     * @param transforms list of transforms
     * @return wrapping instance that will provide transforms over that particular instance
     * @throws org.junit.runners.model.InitializationError runner initialization error
     */
    @SuppressWarnings ( { "unchecked" })
    protected ParentRunner<?> wrap(ParentRunner<?> original, List<SuiteTransform> transforms)throws InitializationError
    {
        if (original instanceof TransformableRunner)
        {
            return ((TransformableRunner) original).withTransforms(transforms);
        }
        return new TransformingParentRunner(original, transforms);
    }


    @Override
    protected List<T> getChildren()
    {
        return children;
    }

    @Override
    protected Description describeChild(T child)
    {
        return parentRunnerHacker.describeChild(child);
    }

    @Override
    protected void runChild(T child, RunNotifier notifier)
    {
        parentRunnerHacker.runChild(child, notifier);
    }

    @Override
    public Description getDescription()
    {
        Description original = super.getDescription();
        if (original.getChildren().isEmpty())
        {
            return Description.EMPTY;
        }
        return original;
    }

    @Override
    protected String getName()
    {
        if (name != null)
        {
            return name;
        }
        else
        {
            return super.getName();
        }
    }
}
