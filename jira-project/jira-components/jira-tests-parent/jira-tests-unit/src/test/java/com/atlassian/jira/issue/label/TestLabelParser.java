package com.atlassian.jira.issue.label;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestLabelParser
{
    @Test
    public void testInvalidLabels()
    {
        assertFalse(LabelParser.isValidLabelName("label  name"));
        assertTrue(LabelParser.isValidLabelName("valid_label_name"));
    }

    @Test
    public void testGetCleanLabel()
    {
        assertEquals(null, LabelParser.getCleanLabel(null));
        assertEquals(null, LabelParser.getCleanLabel(""));
        assertEquals(null, LabelParser.getCleanLabel("  "));

        assertEquals("a_b", LabelParser.getCleanLabel("   a b   "));

        String lotsOfAs = StringUtils.repeat("a", 255);
        assertEquals(lotsOfAs, LabelParser.getCleanLabel(lotsOfAs));
        assertEquals(lotsOfAs, LabelParser.getCleanLabel(lotsOfAs + "bbb"));
        assertEquals(lotsOfAs, LabelParser.getCleanLabel("    " + lotsOfAs));

        assertEquals("ab", LabelParser.getCleanLabel("ab"));
    }
}
