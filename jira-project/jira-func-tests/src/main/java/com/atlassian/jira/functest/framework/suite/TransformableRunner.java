package com.atlassian.jira.functest.framework.suite;

import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

/**
 * A runner capable of applying list of {@link com.atlassian.jira.functest.framework.suite.SuiteTransform}s to itself.
 *
 * @since v4.4
 */
public interface TransformableRunner<T extends ParentRunner<?>>
{

    /**
     * An instance of this runner applying given list of <tt>transforms</tt> to itself.
     *
     * @param transforms transforms to apply
     * @return runner instance with transforms
     * @throws org.junit.runners.model.InitializationError JUnit4 error
     */
    T withTransforms(List<SuiteTransform> transforms) throws InitializationError;
}
