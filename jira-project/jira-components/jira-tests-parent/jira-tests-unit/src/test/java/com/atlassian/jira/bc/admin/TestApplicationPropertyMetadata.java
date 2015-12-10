package com.atlassian.jira.bc.admin;

import com.atlassian.jira.config.properties.MockValidator;
import com.atlassian.validation.Validator;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link ApplicationPropertyMetadata}.
 *
 * @since v4.4
 */
public class TestApplicationPropertyMetadata
{
    @Test
    public void testValidate() {
        String key = "key";
        String type = "string";
        String defaultVal = "habanero";
        Supplier<? extends Validator> validator = Suppliers.ofInstance(new MockValidator());
        ApplicationPropertyMetadata apm = new ApplicationPropertyMetadata.Builder()
                .key(key)
                .type(type)
                .defaultValue(defaultVal)
                .validator(validator)
                .sysAdminEditable(true)
                .requiresRestart(false)
                .build();
        Assert.assertTrue(apm.validate("mock").isValid());
        Assert.assertFalse(apm.validate("other").isValid());

    }
}
