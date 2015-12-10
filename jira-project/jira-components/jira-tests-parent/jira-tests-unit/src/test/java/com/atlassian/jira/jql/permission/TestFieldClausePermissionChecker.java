package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SearchableField;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * test for {@link FieldClausePermissionChecker}.
 *
 * @since v4.0
 */
public class TestFieldClausePermissionChecker
{
    @Test
    public void testHasPermissionToUseClause() throws Exception
    {
        IMocksControl control = EasyMock.createControl();
        final SearchableField searcherField = control.createMock(SearchableField.class);
        final FieldManager fieldManager = control.createMock(FieldManager.class);

        expect(fieldManager.isFieldHidden((User) null, searcherField)).andReturn(false).andReturn(true);

        final FieldClausePermissionChecker checker = new FieldClausePermissionChecker(searcherField, fieldManager);

        control.replay();

        assertTrue(checker.hasPermissionToUseClause(null));
        assertFalse(checker.hasPermissionToUseClause(null));
        
        control.verify();
    }

    @SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
    @Test
    public void testHasPermissionToUseClauseDataAccessException() throws Exception
    {
        IMocksControl control = EasyMock.createControl();
        final SearchableField searcherField = control.createMock(SearchableField.class);
        final FieldManager fieldManager = control.createMock(FieldManager.class);

        expect(fieldManager.isFieldHidden((User) null, searcherField)).andThrow(new DataAccessException("duh!"));

        final FieldClausePermissionChecker checker = new FieldClausePermissionChecker(searcherField, fieldManager);

        control.replay();

        try
        {
            checker.hasPermissionToUseClause(null);
            fail("expect DataAccessException");
        }
        catch (DataAccessException ex)
        {
            // expected
        }

        control.verify();
    }
}
