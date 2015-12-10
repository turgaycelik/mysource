package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.DefaultConfigSequence;
import com.atlassian.jira.util.collect.MapBuilder;
import junit.framework.TestCase;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link DefaultConfigSequence}.
 *
 * @since v4.1
 */
public class TestDefaultConfigSequence extends TestCase
{
    private static final String TYPE_A = "a";
    private static final String TYPE_B = "b";
    private static final String TYPE_C = "c";
    private static final String TYPE_D = "d";

    private static final String ATTRIB_SEQ_NAME = "seqName";
    private static final String ATTRIB_SEQ_ID = "seqId";
    private static final String ELEMENT_SEQUENCE_VALUE_ITEM = "SequenceValueItem";

    public void testEmptySequence() throws Exception
    {
        final Element element = createRootElement();
        final ConfigSequence sequence = new DefaultConfigSequence(element.getDocument());

        assertEquals(10000L, (long) sequence.getNextId(TYPE_A));
        assertEquals(10001L, (long) sequence.getNextId(TYPE_A));

        assertEquals(10000L, (long) sequence.getNextId(TYPE_B));
        assertEquals(10002L, (long) sequence.getNextId(TYPE_A));
        assertEquals(10001L, (long) sequence.getNextId(TYPE_B));

        assertTrue(sequence.save());

        final MapBuilder<String, Long> builder = MapBuilder.<String, Long>newBuilder().add(TYPE_A, 10010L).add(TYPE_B, 10010L);
        assertEntries(builder.toImmutableMap(), element.getDocument());

        assertEquals(10003L, (long) sequence.getNextId(TYPE_A));

        assertFalse(sequence.save());
        assertEntries(builder.toImmutableMap(), element.getDocument());
    }

    public void testExistingSequence() throws Exception
    {
        final Element root = createRootElement();
        addEntry(root, TYPE_A, 10009);
        addEntry(root, TYPE_B, 10016);
        addEntry(root, TYPE_C, "jackLivesHere");

        final ConfigSequence sequence = new DefaultConfigSequence(root.getDocument());

        assertEquals(10010L, (long) sequence.getNextId(TYPE_A));
        assertEquals(10011L, (long) sequence.getNextId(TYPE_A));

        assertEquals(10017L, (long) sequence.getNextId(TYPE_B));
        assertEquals(10012L, (long) sequence.getNextId(TYPE_A));
        assertEquals(10018L, (long) sequence.getNextId(TYPE_B));

        assertEquals(10000L, (long) sequence.getNextId(TYPE_C));
        assertEquals(10000L, (long) sequence.getNextId(TYPE_D));
        assertEquals(10019L, (long) sequence.getNextId(TYPE_B));

        assertTrue(sequence.save());

        final MapBuilder<String, Long> builder = MapBuilder.<String, Long>newBuilder().add(TYPE_A, 10020L)
                .add(TYPE_B, 10030L).add(TYPE_C, 10010L).add(TYPE_D, 10010L);
        assertEntries(builder.toImmutableMap(), root.getDocument());

        assertFalse(sequence.save());

        assertEquals(10001L, (long) sequence.getNextId(TYPE_C));
        assertEquals(10001L, (long) sequence.getNextId(TYPE_D));
        assertFalse(sequence.save());

        for (int i = 1; i <= 10; i++)
        {
            assertEquals(10001L + i, (long) sequence.getNextId(TYPE_C));
        }

        assertTrue(sequence.save());

        assertEntries(builder.add(TYPE_C, 10020L).toImmutableMap(), root.getDocument());
    }

    private void addEntry(Element root, String type, long l)
    {
        addEntry(root, type, String.valueOf(l));
    }

    private void addEntry(Element root, String type, String value)
    {
        final Element element = root.addElement(ELEMENT_SEQUENCE_VALUE_ITEM);
        ConfigXmlUtils.setAttribute(element, ATTRIB_SEQ_NAME, type);
        ConfigXmlUtils.setAttribute(element, ATTRIB_SEQ_ID, value);
    }

    private void assertEntries(Map<String, Long> expected, Document actual)
    {
        Map<String, Long> actualMap = new HashMap<String, Long>();
        final List<Element> list = ConfigXmlUtils.getElementsByXpath(actual, "/entity-engine-xml/SequenceValueItem");
        for (Element element : list)
        {
            final String name = ConfigXmlUtils.getTextValue(element, ATTRIB_SEQ_NAME);
            final Long value = ConfigXmlUtils.getLongValue(element, ATTRIB_SEQ_ID);

            actualMap.put(name, value);
        }

        assertEquals(expected, actualMap);
    }

    private static Element createRootElement()
    {
        final DocumentFactory factory = DocumentFactory.getInstance();
        final Document document = factory.createDocument();
        return document.addElement("entity-engine-xml");
    }
}
