package com.atlassian.jira.bc.admin;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Suppliers;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link LinkSortFieldListValidator}.
 *
 * @since v4.4
 */
public class TestLinkSortFieldListValidator
{
    @Test
    public void testValidate()
    {
        final ArrayList<String> sortableFields = new ArrayList<String>();

        sortableFields.add("foobar");
        sortableFields.add("baz");

        LinkSortFieldListValidator validator = new LinkSortFieldListValidator(Suppliers.<Collection<String>>ofInstance(sortableFields));

        Assert.assertFalse(validator.validate("foobar, bingbah").isValid());
        Assert.assertFalse(validator.validate("bingbah").isValid());
        Assert.assertFalse(validator.validate("foobar, baz, bingbah").isValid());
        Assert.assertFalse(validator.validate("").isValid());

        Assert.assertTrue(validator.validate("foobar, baz").isValid());
        Assert.assertTrue(validator.validate("baz").isValid());
    }

}
