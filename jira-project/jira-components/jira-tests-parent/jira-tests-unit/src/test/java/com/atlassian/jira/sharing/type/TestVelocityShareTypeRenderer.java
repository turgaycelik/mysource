package com.atlassian.jira.sharing.type;

import java.util.Collections;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.mocks.VelocityTemplatingEngineMocks;
import com.atlassian.jira.user.MockUser;

import org.apache.velocity.exception.VelocityException;
import org.junit.Test;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link com.atlassian.jira.sharing.type.VelocityShareTypeRenderer}.
 * 
 * @since v3.13
 */
public class TestVelocityShareTypeRenderer
{
    private static final String VELOCITY_RETURN = "DONE";
    private static final String TEMPLATE_NAME = "template";
    private static final String ENCODING = "UTF-8";

    @Test(expected = IllegalArgumentException.class)
    public void instantiatingAVelocityShareTypeRendererShouldFailGivenANullVelocityTemplatingEngine()
    {
        new MockVelocityShareTypeRenderer
                (
                        new EncodingConfiguration()
                        {
                            public String getEncoding()
                            {
                                return "UTF-8";
                            }
                        },
                        null
                );
    }

    @Test
    public void testRenderVelocity()
    {
        final User testUser = new MockUser("test");
        final JiraAuthenticationContext authenticationContext = new MockAuthenticationContext(testUser);

        final VelocityTemplatingEngine templatingEngine = VelocityTemplatingEngineMocks.alwaysOutput(VELOCITY_RETURN).get();

        final MockVelocityShareTypeRenderer renderer = new MockVelocityShareTypeRenderer(new EncodingConfiguration.Static(ENCODING), templatingEngine);
        assertEquals(VELOCITY_RETURN, renderer.callRenderVelocity(TEMPLATE_NAME, Collections.<String, Object>emptyMap(), authenticationContext));
    }

    @Test(expected = RuntimeException.class)
    public void shouldWrapVelocityExceptionsIntoRuntimeExceptionsWhenCallingTheVelocityTemplatingEngine()
    {
        final User testUser = new MockUser("test");
        final JiraAuthenticationContext authenticationContext = new MockAuthenticationContext(testUser);

        final VelocityTemplatingEngine templatingEngine = VelocityTemplatingEngineMocks.alwaysThrow(new VelocityException("TestException")).get();

        final MockVelocityShareTypeRenderer renderer = new MockVelocityShareTypeRenderer(new EncodingConfiguration.Static(ENCODING), templatingEngine);
        renderer.callRenderVelocity(TEMPLATE_NAME, Collections.<String, Object>emptyMap(), authenticationContext);
    }

    private class MockVelocityShareTypeRenderer extends VelocityShareTypeRenderer
    {
        public MockVelocityShareTypeRenderer(final EncodingConfiguration encoding, final VelocityTemplatingEngine templatingEngine)
        {
            super(encoding, templatingEngine);
        }

        public String renderPermission(final SharePermission permission, final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public String getSimpleDescription(SharePermission permission, JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public String getDisplayTemplate(final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public String getShareTypeEditor(final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public boolean isAddButtonNeeded(final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public String getShareTypeLabel(final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public Map <String, String> getTranslatedTemplates(final JiraAuthenticationContext userCtx, final TypeDescriptor type, final RenderMode mode)
        {
            throw new UnsupportedOperationException();
        }

        protected Map<String, Object> addDefaultVelocityParameters(final Map params, final JiraAuthenticationContext authCtx)
        {
            return newHashMap();
        }

        public String callRenderVelocity(final String template, final Map<String, Object> parameters, final JiraAuthenticationContext ctx)
        {
            return super.renderVelocity(template, parameters, ctx);
        }
    }
}
