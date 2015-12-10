package com.atlassian.jira.functest.framework.suite;

import com.google.common.collect.Lists;
import org.junit.runner.Description;
import org.junit.runners.ParentRunner;

import java.util.List;

/**
 *
 *
 * @since v4.4
 */
public final class RunnerChildList<T>
{
    private final ParentRunnerHacker<T> hacker;
    private final List<T> children;
    private final List<Description> descriptions;

    public RunnerChildList(ParentRunner<T> parentRunner)
    {
        this.hacker = new ParentRunnerHacker<T>(parentRunner);
        this.children = hacker.getChildren();
        this.descriptions = getDescriptions(parentRunner);
    }

    private List<Description> getDescriptions(ParentRunner parentRunner)
    {
        List<Description> answer = Lists.newArrayList();
        ParentRunnerHacker<T> hacker = new ParentRunnerHacker<T>(parentRunner);
        for (T child : children)
        {
            answer.add(hacker.describeChild(child));
        }
        return answer;
    }

    public List<T> children()
    {
        return Lists.newArrayList(children);
    }

    public List<Description> descriptions()
    {
        return Lists.newArrayList(descriptions);
    }

    public List<T> matchingChildren(List<Description> transformedDescriptions)
    {
        List<T> answer = Lists.newArrayList();
        for (Description desc : transformedDescriptions)
        {
            // we assume every member of transformedDescriptions is in descriptions
            answer.add(children.get(descriptions.indexOf(desc)));
        }
        return answer;
    }

    public static <T> List<T> matchingChildren(List<T> originalChildren, List<Description> originalDescriptions, Iterable<SuiteTransform> suiteTransforms)
    {
        Iterable<Description> transformedDescriptions = TransformingParentRunner.applyTransforms(originalDescriptions, suiteTransforms);
        List<T> answer = Lists.newArrayList();
        for (Description desc : transformedDescriptions)
        {
            // we assume every member of transformedDescriptions is in descriptions
            answer.add(originalChildren.get(originalDescriptions.indexOf(desc)));
        }
        return answer;
    }

}
