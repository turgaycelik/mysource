package com.atlassian.jira.issue;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TestAbstractIssue
{
    @Test
    public void testEqualsAndHashCodeAreCompatible()
    {
        final GenericValue gv = new MockGenericValue("Issue", FieldMap.build("key", "ABC-123"));
        Issue issueImpl = new IssueImpl(gv, null, null, null, null, null, null, null, null, null, null, null);
        Issue documentIssue = new DocumentIssueImpl(null, null, null, null, null, null, null, null)
        {
            @Override
            public String getKey()
            {
                return "ABC-123";
            }
        };

        assertTrue("Checking DocumentIssue equals IssueImpl (verifying document issue equals method)", documentIssue.equals(issueImpl));
        assertTrue("Checking IssueImpl equals DocumentIssue (verifying document issue equals method)", issueImpl.equals(documentIssue));

        assertEquals(documentIssue.hashCode(), issueImpl.hashCode());
        // The interface javadoc declares the hashCode contract
        assertEquals("ABC-123".hashCode(), issueImpl.hashCode());
        gv.set("key", null);
        assertEquals(0, issueImpl.hashCode());
    }
}
