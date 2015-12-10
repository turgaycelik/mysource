package com.atlassian.jira.bc.admin;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.mock.issue.fields.MockNavigableField;

import org.easymock.EasyMock;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link NavigableFieldListValidator}.
 *
 * @since v4.4
 */
public class TestNavigableFieldListValidator
{

    @Test
    public void testValidate()
    {
        MockNavigableField foobarField = new MockNavigableField("foobar");
        MockNavigableField bazField = new MockNavigableField("baz");

        FieldManager fieldManager = EasyMock.createMock(FieldManager.class);
        expect(fieldManager.getNavigableField("foobar")).andReturn(foobarField).atLeastOnce();
        expect(fieldManager.getNavigableField("baz")).andReturn(bazField).atLeastOnce();
        expect(fieldManager.getNavigableField("explodo")).andThrow(new IllegalArgumentException("no way")).atLeastOnce();
        expect(fieldManager.getNavigableField("missing")).andReturn(null).atLeastOnce();
        replay(fieldManager);

        NavigableFieldListValidator validator = new NavigableFieldListValidator(fieldManager);

        assertFalse(validator.validate("foobar, missing").isValid());
        assertFalse(validator.validate("missing").isValid());
        assertFalse(validator.validate("foobar, baz, missing").isValid());
        assertFalse(validator.validate("explodo, baz, missing").isValid());
        assertFalse(validator.validate("foobar, baz, explodo").isValid());
        assertFalse(validator.validate("explodo").isValid());
        assertFalse(validator.validate("").isValid());

        assertTrue(validator.validate("foobar, baz").isValid());
        assertTrue(validator.validate("baz").isValid());

        verify(fieldManager);
    }
}
