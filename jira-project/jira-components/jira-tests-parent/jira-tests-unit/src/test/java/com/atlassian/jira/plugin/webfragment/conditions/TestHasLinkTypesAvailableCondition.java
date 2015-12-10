package com.atlassian.jira.plugin.webfragment.conditions;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.MockIssueLinkType;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestHasLinkTypesAvailableCondition
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final IssueLinkTypeManager issueLinkTypeManager = mocksControl.createMock(IssueLinkTypeManager.class);

        final Collection list = CollectionBuilder.list(new MockIssueLinkType());

        expect(issueLinkTypeManager.getIssueLinkTypes()).andReturn(list);

        final HasLinkTypesAvailableCondition condition = new HasLinkTypesAvailableCondition(issueLinkTypeManager);

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(null, null));
        mocksControl.verify();

    }

    @Test
    public void testFalseEmpty()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final IssueLinkTypeManager issueLinkTypeManager = mocksControl.createMock(IssueLinkTypeManager.class);

        final Collection list = Collections.emptyList();

        expect(issueLinkTypeManager.getIssueLinkTypes()).andReturn(list);

        final HasLinkTypesAvailableCondition condition = new HasLinkTypesAvailableCondition(issueLinkTypeManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, null));
        mocksControl.verify();

    }

    @Test
    public void testFalseNull()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final IssueLinkTypeManager issueLinkTypeManager = mocksControl.createMock(IssueLinkTypeManager.class);

        expect(issueLinkTypeManager.getIssueLinkTypes()).andReturn(null);

        final HasLinkTypesAvailableCondition condition = new HasLinkTypesAvailableCondition(issueLinkTypeManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, null));
        mocksControl.verify();

    }
}
