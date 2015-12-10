package com.atlassian.jira.functest.framework.suite;

import com.google.common.base.Function;
import org.junit.runner.Description;

/**
 * <p>
 * A transform applied on a collection of test descriptions that results in a new, transformed list. The transform can be
 * anything from sorting and filtering to a side effect without modifying the list of tests itself.
 *
 * <p>
 * NOTE: Transforms are applied recursively over the tests in runner tree. Transforms that drill down the description
 * tree (which reflects the runner tree) should only return top-level descriptions, as otherwise the tree structure
 * of runners and corresponding descriptions will be broken. Another implication is that a single transform instance
 * passed to the {@link com.atlassian.jira.functest.framework.suite.TransformingParentRunner} will be executed multiple
 * times on each branch of the runner/test tree that contains children. This has to be taken into consideration by complex
 * transforms maintaining internal state. The order of applying a single transform over elements of the tree is top-down
 * and first child to last (you may e.g. use sorting transform to sort the tree before you apply transforms that assume
 * some particular order of evaluated tests).
 *
 * <p>
 * NOTE: it is a requirement that the returned iterable must be a copy of the original
 * (i.e. not a modifying 'view' of it), so that changes applied on it will not be reflected in the original. To that end
 * the input iterable is effectively immutable.
 *
 * @see TransformingParentRunner
 * @since v4.4
 */
public interface SuiteTransform extends Function<Iterable<Description>, Iterable<Description>>
{
}
