package com.atlassian.validation;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Does a basic sanity check on HexColourValidator
 *
 * @since v5.2
 */
public class TestHexColourValidator
{
    @Test
    public void testSuccessfulMatches()
    {
        Validator hexValidator = new HexColourValidator();
        assertThat(hexValidator.validate("#AA12bb").isValid(),is(true));
        assertThat(hexValidator.validate("#AA1").isValid(),is(true));
    }

    @Test
    public void testFailureMatches()
    {
        Validator hexValidator = new HexColourValidator();
        assertThat(hexValidator.validate("#").isValid(),is(false));
        assertThat(hexValidator.validate("AA1").isValid(),is(false));
        assertThat(hexValidator.validate("").isValid(),is(false));
        assertThat(hexValidator.validate(null).isValid(),is(false));
        assertThat(hexValidator.validate("#A").isValid(),is(false));
        assertThat(hexValidator.validate("#AA12bbFFFDDDEEEE").isValid(),is(false));
    }

}
