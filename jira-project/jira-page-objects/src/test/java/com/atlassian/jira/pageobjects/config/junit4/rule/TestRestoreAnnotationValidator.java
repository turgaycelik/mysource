package com.atlassian.jira.pageobjects.config.junit4.rule;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.matchers.ErrorMatchers;
import com.atlassian.jira.functest.framework.util.junit.Descriptions;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.config.ResetDataOnce;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test case for {@link com.atlassian.jira.pageobjects.config.junit4.rule.RestoreAnnotationValidator}.
 *
 * @since 6.0
 */
public class TestRestoreAnnotationValidator
{
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldNotAllowRestoreOnceOnClassAndRestoreOnMethod()
    {
        expectedException.expect(IllegalStateException.class);
        expectedException.expect(ErrorMatchers.withMessage("Test", "is marked with conflicting annotations",
                RestoreOnce.class.getName(), Restore.class.getName()));
        RestoreAnnotationValidator.validate(Descriptions.createTestDescription(TestWithClassAnnotatedWithRestoreOnce.class,
                "annotatedWithRestore"));
    }


    @Test
    public void shouldNotAllowRestoreOnceOnClassAndResetDataOnMethod()
    {
        expectedException.expect(IllegalStateException.class);
        expectedException.expect(ErrorMatchers.withMessage("Test", "is marked with conflicting annotations",
                RestoreOnce.class.getName(), ResetData.class.getName()));
        RestoreAnnotationValidator.validate(Descriptions.createTestDescription(TestWithClassAnnotatedWithRestoreOnce.class,
                "annotatedWithResetData"));
    }

    @Test
    public void shouldAllowRestoreOnceOnClass()
    {
        RestoreAnnotationValidator.validate(Descriptions.createTestDescription(TestWithClassAnnotatedWithRestoreOnce.class,
                "notAnnotated"));
    }

    @Test
    public void shouldNotAllowResetDataOnceOnClassAndRestoreOnMethod()
    {
        expectedException.expect(IllegalStateException.class);
        expectedException.expect(ErrorMatchers.withMessage("Test", "is marked with conflicting annotations",
                ResetDataOnce.class.getName(), Restore.class.getName()));
        RestoreAnnotationValidator.validate(Descriptions.createTestDescription(TestWithClassAnnotatedWithResetDataOnce.class,
                "annotatedWithRestore"));
    }


    @Test
    public void shouldNotAllowResetDataOnceOnClassAndResetDataOnMethod()
    {
        expectedException.expect(IllegalStateException.class);
        expectedException.expect(ErrorMatchers.withMessage("Test", "is marked with conflicting annotations",
                ResetDataOnce.class.getName(), ResetData.class.getName()));
        RestoreAnnotationValidator.validate(Descriptions.createTestDescription(TestWithClassAnnotatedWithResetDataOnce.class,
                "annotatedWithResetData"));
    }

    @Test
    public void shouldAllowResetDataOnceOnClass()
    {
        RestoreAnnotationValidator.validate(Descriptions.createTestDescription(TestWithClassAnnotatedWithResetDataOnce.class,
                "notAnnotated"));
    }

    @RestoreOnce("something.xml")
    public static class TestWithClassAnnotatedWithRestoreOnce
    {

        @Restore ("something.xml")
        public void annotatedWithRestore() {}

        @ResetData
        public void annotatedWithResetData() {}

        public void notAnnotated() {}
    }

    @ResetDataOnce
    public static class TestWithClassAnnotatedWithResetDataOnce
    {

        @Restore ("something.xml")
        public void annotatedWithRestore() {}

        @ResetData
        public void annotatedWithResetData() {}

        public void notAnnotated() {}
    }
}
