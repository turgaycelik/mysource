package com.atlassian.jira.template.mocks;

import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.google.common.base.Supplier;
import org.mockito.Matchers;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Factory for mock implementations of the {@link VelocityTemplatingEngine} interface.
 *
 * @since v5.0
 */
public class VelocityTemplatingEngineMocks
{
    /**
     * Returns a builder that returns VelocityTemplatingEngine instances that will always render the specified
     * output string.
     *
     * @param output The output string to be rendered.
     * @return A builder that returns VelocityTemplatingEngine instances that will always render the specified
     * output string.
     */
    public static Supplier<VelocityTemplatingEngine> alwaysOutput(final String output)
    {
        return new ConstantOutputBuilder(output);
    }

    /**
     * Returns a builder that returns VelocityTemplatingEngine instances that will always throw the specified
     * exception on rendering.
     *
     * @param expectedException The exception to be thrown on rendering.
     * @return A builder that returns VelocityTemplatingEngine instances that will always throw the specified
     * exception on rendering.
     */
    public static Supplier<VelocityTemplatingEngine> alwaysThrow(final Exception expectedException)
    {
        return new AlwaysThrowOnRenderBuilder(expectedException);
    }

    private static class ConstantOutputBuilder implements Supplier<VelocityTemplatingEngine>
    {
        private final String output;

        private ConstantOutputBuilder(final String output)
        {
            this.output = output;
        }

        public VelocityTemplatingEngine get()
        {
            return new VelocityTemplatingEngine()
            {
                @Override
                public RenderRequest render(final TemplateSource source)
                {
                    final RenderRequest mockRequest = mock(RenderRequest.class);
                    when(mockRequest.applying(Matchers.<Map<String, Object>>any())).thenReturn(mockRequest);
                    when(mockRequest.asHtml()).thenReturn(output);
                    return mockRequest;
                }
            };
        }
    }

    private static class AlwaysThrowOnRenderBuilder implements Supplier<VelocityTemplatingEngine>
    {
        private final Exception expectedException;

        private AlwaysThrowOnRenderBuilder(final Exception expectedException)
        {
            this.expectedException = expectedException;
        }

        public VelocityTemplatingEngine get()
        {
            return new VelocityTemplatingEngine()
            {
                @Override
                public RenderRequest render(final TemplateSource source)
                {
                    final RenderRequest mockRequest = mock(RenderRequest.class);
                    when(mockRequest.applying(Matchers.<Map<String, Object>>any())).thenReturn(mockRequest);
                    when(mockRequest.asHtml()).thenThrow(expectedException);
                    return mockRequest;
                }
            };
        }

    }
}
