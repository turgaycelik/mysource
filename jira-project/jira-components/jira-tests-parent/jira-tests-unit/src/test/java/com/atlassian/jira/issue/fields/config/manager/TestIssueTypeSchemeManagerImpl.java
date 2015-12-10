package com.atlassian.jira.issue.fields.config.manager;

import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for TestIssueTypeSchemeManagerImpl
 *
 * @since v3.13.2
 */
public class TestIssueTypeSchemeManagerImpl
{
    @Test
    public void testSchemeOrder()
    {
        final FieldConfigScheme scheme1 = createScheme("aardvark", 1L);
        final FieldConfigScheme scheme2 = createScheme("blarney", 2L);
        final FieldConfigScheme scheme3 = createScheme("default", 3L);
        final FieldConfigScheme scheme4 = createScheme("zedbra", 4L);
        final FieldConfigScheme scheme5 = createScheme("@#$@", 5L);
        final FieldConfigScheme scheme6 = createScheme(null, 6L);
        final FieldConfigScheme scheme7 = createScheme("nullid", null);
        final FieldConfigScheme scheme8 = createScheme(null, null);

        final MockControl mockFieldManagerControl = MockControl.createStrictControl(FieldManager.class);
        final FieldManager mockFieldManager = (FieldManager) mockFieldManagerControl.getMock();
        mockFieldManager.getIssueTypeField();
        mockFieldManagerControl.setReturnValue(null);
        mockFieldManagerControl.replay();

        final MockControl mockFieldConfigSchemeManagerControl = MockControl.createStrictControl(FieldConfigSchemeManager.class);
        final FieldConfigSchemeManager mockFieldConfigSchemeManager = (FieldConfigSchemeManager) mockFieldConfigSchemeManagerControl.getMock();
        mockFieldConfigSchemeManager.getConfigSchemesForField(null);
        mockFieldConfigSchemeManagerControl.setReturnValue(EasyList.build(scheme1, scheme2, scheme3, scheme4, scheme5, scheme6, scheme7, scheme8));
        mockFieldConfigSchemeManagerControl.replay();

        final EventPublisher eventPublisher = new MockEventPublisher();

        final IssueTypeSchemeManager manager = new IssueTypeSchemeManagerImpl(mockFieldConfigSchemeManager, null, null, null, eventPublisher)
        {
            @Override
            FieldManager getFieldManager()
            {
                return mockFieldManager;
            }

            @Override
            public FieldConfigScheme getDefaultIssueTypeScheme()
            {
                return scheme3;
            }
        };

        final List schemes = manager.getAllSchemes();

        assertEquals(8, schemes.size());
        assertEquals("default", ((FieldConfigScheme) schemes.get(0)).getName());
        assertEquals(null, ((FieldConfigScheme) schemes.get(1)).getName());
        assertEquals(null, ((FieldConfigScheme) schemes.get(2)).getName());
        assertEquals("@#$@", ((FieldConfigScheme) schemes.get(3)).getName());
        assertEquals("aardvark", ((FieldConfigScheme) schemes.get(4)).getName());
        assertEquals("blarney", ((FieldConfigScheme) schemes.get(5)).getName());
        assertEquals("nullid", ((FieldConfigScheme) schemes.get(6)).getName());
        assertEquals("zedbra", ((FieldConfigScheme) schemes.get(7)).getName());

        mockFieldManagerControl.verify();
        mockFieldConfigSchemeManagerControl.verify();
    }

    FieldConfigScheme createScheme(final String name, final Long id)
    {
        return new FieldConfigScheme.Builder().setId(id).setName(name).toFieldConfigScheme();
    }
}
