package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.CheckMessage;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.functest.config.CheckResultBuilder}.
 *
 * @since v4.1
 */
public class TestCheckResultBuilder extends TestCase
{
    public void testBuilder() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigurationCheck.Result result = builder.buildResult();

        List<CheckMessage> errors = new ArrayList<CheckMessage>();
        List<CheckMessage> warnings = new ArrayList<CheckMessage>();

        assertEquals(errors, result.getErrors());
        assertEquals(warnings, result.getWarnings());
        assertTrue(result.isGood());

        result = builder.error("jack").buildResult();
        errors.add(new CheckMessage("jack", null));
        assertEquals(errors, result.getErrors());
        assertEquals(warnings, result.getWarnings());
        assertFalse(result.isGood());

        result = builder.error("jill", "jack").buildResult();
        errors.add(new CheckMessage("jill", "jack"));
        assertEquals(errors, result.getErrors());
        assertEquals(warnings, result.getWarnings());
        assertFalse(result.isGood());

        result = builder.warning("abc").buildResult();
        warnings.add(new CheckMessage("abc"));
        assertEquals(errors, result.getErrors());
        assertEquals(warnings, result.getWarnings());
        assertFalse(result.isGood());

        result = builder.warning("fgh", "def").buildResult();
        warnings.add(new CheckMessage("fgh", "def"));
        assertEquals(errors, result.getErrors());
        assertEquals(warnings, result.getWarnings());
        assertFalse(result.isGood());

        builder = new CheckResultBuilder();
        errors.clear();
        warnings.clear();
        result = builder.warning("onlywarning").buildResult();
        warnings.add(new CheckMessage("onlywarning"));
        assertEquals(errors, result.getErrors());
        assertEquals(warnings, result.getWarnings());
        assertFalse(result.isGood());
    }
}