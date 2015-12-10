package com.atlassian.jira.issue.fields.util;

import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.util.Predicate;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.createNiceControl;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test {@link com.atlassian.jira.issue.fields.util.FieldPredicates}.
 *
 * @since v4.1
 */
public class TestFieldPredicates
{
    @Test
    public void testIsCustomField() throws Exception
    {
        final IMocksControl niceControl = createNiceControl();
        final CustomField customField = niceControl.createMock(CustomField.class);
        final Field field = niceControl.createMock(Field.class);

        niceControl.replay();

        final Predicate<Field> cfPredicate = FieldPredicates.isCustomField();
        assertTrue(cfPredicate.evaluate(customField));
        assertFalse(cfPredicate.evaluate(null));
        assertFalse(cfPredicate.evaluate(field));

        niceControl.verify();
    }

    @Test
    public void testIsDateField() throws Exception
    {
        final IMocksControl niceControl = createNiceControl();

        final Field nothingField = niceControl.createMock(Field.class);

        final CustomField dateFieldCF = niceControl.createMock(CustomField.class);
        EasyMock.expect(dateFieldCF.getCustomFieldType()).andReturn(new DateType()).anyTimes();

        final CustomField customField = niceControl.createMock(CustomField.class);
        EasyMock.expect(customField.getCustomFieldType()).andReturn(new MockCustomFieldType()).anyTimes();

        niceControl.replay();

        final Predicate<Field> cfPredicate = FieldPredicates.isDateField();
        assertTrue(cfPredicate.evaluate(dateFieldCF));
        assertFalse(cfPredicate.evaluate(customField));
        assertFalse(cfPredicate.evaluate(nothingField));
        assertTrue(cfPredicate.evaluate(new TestDateField("blarg")));
        assertFalse(cfPredicate.evaluate(null));

        niceControl.verify();
    }

    @Test
    public void testIsCustomDateField() throws Exception
    {
        final IMocksControl niceControl = createNiceControl();

        final Field nothingField = niceControl.createMock(Field.class);

        final CustomField dateFieldCF = niceControl.createMock(CustomField.class);
        EasyMock.expect(dateFieldCF.getCustomFieldType()).andReturn(new DateType()).anyTimes();

        final CustomField customField = niceControl.createMock(CustomField.class);
        EasyMock.expect(customField.getCustomFieldType()).andReturn(new MockCustomFieldType()).anyTimes();

        niceControl.replay();

        final Predicate<Field> cfPredicate = FieldPredicates.isCustomDateField();
        assertTrue(cfPredicate.evaluate(dateFieldCF));
        assertFalse(cfPredicate.evaluate(customField));
        assertFalse(cfPredicate.evaluate(nothingField));
        assertFalse(cfPredicate.evaluate(new TestDateField("blarg")));
        assertFalse(cfPredicate.evaluate(null));

        niceControl.verify();
    }

    @Test
    public void testIsUserField() throws Exception
    {
        final IMocksControl niceControl = createNiceControl();

        final Field nothingField = niceControl.createMock(Field.class);

        final CustomField userFieldCF = niceControl.createMock(CustomField.class);
        EasyMock.expect(userFieldCF.getCustomFieldType()).andReturn(new UserType()).anyTimes();

        final CustomField customField = niceControl.createMock(CustomField.class);
        EasyMock.expect(customField.getCustomFieldType()).andReturn(new DateType()).anyTimes();

        niceControl.replay();

        final Predicate<Field> cfPredicate = FieldPredicates.isUserField();
        assertTrue(cfPredicate.evaluate(userFieldCF));
        assertFalse(cfPredicate.evaluate(customField));
        assertFalse(cfPredicate.evaluate(nothingField));
        assertTrue(cfPredicate.evaluate(new TestUserField("blarg")));
        assertFalse(cfPredicate.evaluate(null));

        niceControl.verify();
    }

    @Test
    public void testIsCustomUserField() throws Exception
    {
        final IMocksControl niceControl = createNiceControl();

        final Field nothingField = niceControl.createMock(Field.class);

        final CustomField userFieldCF = niceControl.createMock(CustomField.class);
        EasyMock.expect(userFieldCF.getCustomFieldType()).andReturn(new UserType()).anyTimes();

        final CustomField customField = niceControl.createMock(CustomField.class);
        EasyMock.expect(customField.getCustomFieldType()).andReturn(new DateType()).anyTimes();

        niceControl.replay();

        final Predicate<Field> cfPredicate = FieldPredicates.isCustomUserField();
        assertTrue(cfPredicate.evaluate(userFieldCF));
        assertFalse(cfPredicate.evaluate(customField));
        assertFalse(cfPredicate.evaluate(nothingField));
        assertFalse(cfPredicate.evaluate(new TestUserField("blarg")));
        assertFalse(cfPredicate.evaluate(null));

        niceControl.verify();
    }

    @Test
    public void testIsStandardViewIssueCustomField() throws Exception
    {
        final IMocksControl niceControl = createNiceControl();

        final Field nothingField = niceControl.createMock(Field.class);

        final CustomField userFieldCF = niceControl.createMock(CustomField.class);
        EasyMock.expect(userFieldCF.getCustomFieldType()).andReturn(new UserType()).anyTimes();

        final CustomField dateFieldCf = niceControl.createMock(CustomField.class);
        EasyMock.expect(dateFieldCf.getCustomFieldType()).andReturn(new DateType()).anyTimes();

        final CustomField standardCF = niceControl.createMock(CustomField.class);
        EasyMock.expect(standardCF.getCustomFieldType()).andReturn(new MockCustomFieldType()).anyTimes();

        niceControl.replay();

        final Predicate<Field> cfPredicate = FieldPredicates.isStandardViewIssueCustomField();
        assertFalse(cfPredicate.evaluate(userFieldCF));
        assertFalse(cfPredicate.evaluate(dateFieldCf));
        assertTrue(cfPredicate.evaluate(standardCF));
        assertFalse(cfPredicate.evaluate(nothingField));
        assertFalse(cfPredicate.evaluate(new TestUserField("blarg")));
        assertFalse(cfPredicate.evaluate(new TestDateField("blarg")));
        assertFalse(cfPredicate.evaluate(null));

        niceControl.verify();
    }

    private static class DateType extends MockCustomFieldType implements DateField
    {
    }

    private static class UserType extends MockCustomFieldType implements UserField
    {
    }

    private static class TestDateField extends MockOrderableField implements DateField
    {
        public TestDateField(final String id)
        {
            super(id);
        }
    }

    private static class TestUserField extends MockOrderableField implements UserField
    {
        public TestUserField(final String id)
        {
            super(id);
        }
    }
}
