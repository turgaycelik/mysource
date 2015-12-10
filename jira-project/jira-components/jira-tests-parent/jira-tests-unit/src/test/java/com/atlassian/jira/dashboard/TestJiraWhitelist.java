package com.atlassian.jira.dashboard;

import java.net.URI;

import com.atlassian.jira.bc.whitelist.WhitelistService;
import com.atlassian.jira.util.velocity.SimpleVelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestJiraWhitelist
{
    @Test
    public void testAllows() throws Exception
    {
        final WhitelistService mockWhitelistService = createMock(WhitelistService.class);
        final VelocityRequestContextFactory mockVelocityRequestContextFactory = createMock(VelocityRequestContextFactory.class);
        final VelocityRequestContext context = new SimpleVelocityRequestContext("/", "http://localhost:8090/", null, null);
        expect(mockVelocityRequestContextFactory.getJiraVelocityRequestContext()).andReturn(context).anyTimes();
        expect(mockWhitelistService.isAllowed(URI.create("HTTP://www.Atlassian.com/gadgets/marketing.xml"))).andReturn(true);
        expect(mockWhitelistService.isAllowed(URI.create("HTTPs://www.Atlassian.com/gadgets/marketing.xml"))).andReturn(true);
        expect(mockWhitelistService.isAllowed(URI.create("localhost:8090/gadget/blah.xml"))).andReturn(false);
        expect(mockWhitelistService.isAllowed(URI.create("www.atlassian.com/gadget/blah.xml"))).andReturn(false);
        expect(mockWhitelistService.isAllowed(URI.create("http://www.google.com/"))).andReturn(false);

        replay(mockWhitelistService, mockVelocityRequestContextFactory);

        JiraWhitelist whitelist = new JiraWhitelist(mockWhitelistService, mockVelocityRequestContextFactory);
        assertTrue(whitelist.allows(URI.create("HTTP://www.Atlassian.com/gadgets/marketing.xml")));
        assertTrue(whitelist.allows(URI.create("HTTPs://www.Atlassian.com/gadgets/marketing.xml")));
        assertTrue(whitelist.allows(URI.create("http://localhost:8090/gadgets/marketing.xml")));
        assertFalse(whitelist.allows(URI.create("localhost:8090/gadget/blah.xml")));
        assertFalse(whitelist.allows(URI.create("www.atlassian.com/gadget/blah.xml")));
        assertFalse(whitelist.allows(URI.create("http://www.google.com/")));

        verify(mockWhitelistService, mockVelocityRequestContextFactory);
    }
}
