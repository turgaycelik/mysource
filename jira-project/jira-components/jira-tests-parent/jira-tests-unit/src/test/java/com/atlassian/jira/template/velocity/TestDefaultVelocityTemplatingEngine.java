package com.atlassian.jira.template.velocity;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.velocity.JiraVelocityManager;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.template.TemplateSources.file;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Responsible for holding the unit tests for {@link DefaultVelocityTemplatingEngine}.
 *
 * @since v5.1
 */
public class TestDefaultVelocityTemplatingEngine
{
    @Before
    public void stubEventPublisher()
    {
        MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.addMock((EventPublisher.class), mock(EventPublisher.class));
        ComponentAccessor.initialiseWorker(componentAccessorWorker);
    }

    private JiraVelocityManager instantiateVelocityManager()
    {
        return new JiraVelocityManager(null, new VelocityEngineFactory.Default());
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileAsPlainText()
    {
        final String expectedOutput = "Test Template defined within an existing file.";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/text/existing-non-parameterized-template.vm")).
                        asPlainText();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    private DefaultVelocityTemplatingEngine instantiateTemplatingEngine()
    {
        return new DefaultVelocityTemplatingEngine(instantiateVelocityManager(), new MockApplicationProperties(){
            @Override
            public String getEncoding()
            {
                return "UTF-8";
            }
        });
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileApplyingParametersAsPlainText()
    {
        final String expectedOutput = "Test template defined within an existing file and receives the following parameter:[parameter value].";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/text/existing-parameterized-template.vm")).
                        applying(ImmutableMap.<String, Object>of("testParameter", "[parameter value]")).
                        asPlainText();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileAsHtml()
    {
        final String expectedOutput = "<html>\n"
                + "<head>\n"
                + "    <title>Blank HTML Template</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<p>This is a HTML template</p>\n"
                + "</body>\n"
                + "</html>";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/html/existing-non-parameterized-template.vm")).
                        asHtml();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileAsHtmlApplyingParameters()
    {
        final String expectedOutput = "<html>\n"
                + "<head>\n"
                + "    <title>Parameterized HTML Template</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<p>The value of the &quot;content&quot; parameter is: [passed in content]</p>\n"
                + "</body>\n"
                + "</html>";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/html/existing-parameterized-template.vm")).
                        applying(ImmutableMap.<String, Object>of("content", "[passed in content]")).
                        asHtml();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileAsHtmlEscapingInsertedReferencesThatHaveNotBeenTaggedAsHtmlSafe()
    {
        final String expectedOutput = "<html>\n"
                + "<head>\n"
                + "    <title>Parameterized HTML Template</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<p>The value of the &quot;content&quot; parameter is: &lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;</p>\n"
                + "</body>\n"
                + "</html>";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/html/existing-parameterized-template.vm")).
                        applying(ImmutableMap.<String, Object>of("content", "<script>alert('XSS Test')</script>")).
                        asHtml();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileAsHtmlNotEscapingInsertedMethodReferencesThatHaveBeenAnnotatedAsHtmlSafe()
    {
        final String expectedOutput = "<html>\n"
                + "<head>\n"
                + "    <title>Parameterized HTML Template</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<p>The value of the &quot;content&quot; parameter is: &lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;</p>\n"
                + "</body>\n"
                + "</html>";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/html/template-escaping-html-safe-annotated-method-as-string.vm")).
                        applying(ImmutableMap.<String, Object>of("content", new EscapingTestClass())).
                        asHtml();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileAsHtmlNotEscapingInsertedPropertyReferencesThatHaveBeenAnnotatedAsHtmlSafe()
    {
        final String expectedOutput = "<html>\n"
                + "<head>\n"
                + "    <title>Parameterized HTML Template</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<p>The value of the &quot;content&quot; parameter is: &lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;</p>\n"
                + "</body>\n"
                + "</html>";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/html/template-escaping-html-safe-annotated-property-as-string.vm")).
                        applying(ImmutableMap.<String, Object>of("content", new EscapingTestClass())).
                        asHtml();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileAsHtmlNotEscapingInsertedObjectPropertyReferencesThatHaveBeenAnnotatedAsHtmlSafe()
    {
        final String expectedOutput = "<html>\n"
                + "<head>\n"
                + "    <title>Parameterized HTML Template</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<p>The value of the &quot;content&quot; parameter is: &lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;</p>\n"
                + "</body>\n"
                + "</html>";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/html/template-escaping-html-safe-annotated-property-as-object.vm")).
                        applying(ImmutableMap.<String, Object>of("content", new EscapingTestClass())).
                        asHtml();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileAsHtmlNotEscapingInsertedObjectMethodReferencesThatHaveBeenAnnotatedAsHtmlSafe()
    {
        final String expectedOutput = "<html>\n"
                + "<head>\n"
                + "    <title>Parameterized HTML Template</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<p>The value of the &quot;content&quot; parameter is: &lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;</p>\n"
                + "</body>\n"
                + "</html>";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/html/template-escaping-html-safe-annotated-method-as-object.vm")).
                        applying(ImmutableMap.<String, Object>of("content", new EscapingTestClass())).
                        asHtml();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void shouldBeAbleToRenderATemplateFromAFileAsHtmlNotEscapingInsertedStringMethodReferencesGivenTheMethodNameEndsWithHtml()
    {
        final String expectedOutput = "<html>\n"
                + "<head>\n"
                + "    <title>Parameterized HTML Template</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<p>The value of the &quot;content&quot; parameter is: &lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;</p>\n"
                + "</body>\n"
                + "</html>";

        final DefaultVelocityTemplatingEngine velocityTemplatingEngine = instantiateTemplatingEngine();

        final String actualOutput =
                velocityTemplatingEngine.
                        render(file("com/atlassian/jira/template/velocity/html/template-escaping-method-name-ending-with-html-as-string.vm")).
                        applying(ImmutableMap.<String, Object>of("reference", new EscapingTestClass())).
                        asHtml();

        assertTrue(isNotBlank(actualOutput));
        assertEquals(expectedOutput, actualOutput);
    }

    /**
     * This class contains several methods / getters / setters to exercise the html escaping facilities of the
     * underlying Velocity Engine
     */
    public static class EscapingTestClass
    {
        /**
         * Method that returns a html encoded string, should be considered as safe by the velocity engine because it
         * has been annotated as {@link HtmlSafe}
         * @return A html encoded string.
         */
        @HtmlSafe
        public String annotationSafeMethodAsString()
        {
            return "&lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;";
        }

        /**
         * Property that returns an object  html encoded string, should be considered as safe by the velocity engine
         * because it has been annotated as {@link HtmlSafe}
         * @return A html encoded string.
         */
        @HtmlSafe
        public String getAnnotationSafePropertyAsString()
        {
            return "&lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;";
        }

        /**
         * Method that returns an object whose <code>toString()</code> method evaluates to a html encoded string,
         * should be considered as safe by the velocity engine because it has been annotated as {@link HtmlSafe}
         * @return An object that returns a html encoded string in its <code>toString()</code> method.
         */
        @HtmlSafe
        public Object annotationSafeMethodAsObject()
        {
            return new Object()
            {
                @Override
                public String toString()
                {
                    return "&lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;";
                }
            };
        }

        /**
         * Property that returns an object whose <code>toString()</code> method evaluates to a html encoded string,
         * should be considered as safe by the velocity engine because it has been annotated as {@link HtmlSafe}
         * @return An object that returns a html encoded string in its <code>toString()</code> method.
         */
        @HtmlSafe
        public Object getAnnotationSafePropertyAsObject()
        {
            return new Object()
            {
                @Override
                public String toString()
                {
                    return "&lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;";
                }
            };
        }

        /**
         * Method that returns a html encoded string, should be considered as safe by the velocity engine because the
         * method name ends with &quot;Html&quot;
         * @return A html encoded string.
         */
        public String contentAsHtml()
        {
            return "&lt;script&gt;alert(&#39;XSS Test&#39;)&lt;/script&gt;";
        }
    }
}
