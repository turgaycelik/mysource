package com.atlassian.jira.functest.config;

import org.dom4j.Document;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that can be used to parse and manipulate the SequenceValueItem from the passed JIRA xml.
 *
 * @since v4.0
 */
public class DefaultConfigSequence implements ConfigSequence
{
    private static final String ELEMENT_SEQUENCE_VALUE_ITEM = "SequenceValueItem";
    private static final String ATTRIB_SEQ_NAME = "seqName";
    private static final String ATTRIB_SEQ_ID = "seqId";

    private Map<String, Long> map = new HashMap<String, Long>();

    private final Document document;

    public DefaultConfigSequence(Document document)
    {
        this.document = document;
    }

    public Long getNextId(String elementType)
    {
        Long nextId = map.get(elementType);
        if (nextId == null)
        {
            final Number number = document.numberValueOf("/entity-engine-xml/SequenceValueItem[@seqName='" + elementType + "']/@seqId");
            if (number == null || Float.isNaN(number.floatValue()) || Float.isInfinite(number.floatValue()))
            {
                nextId = 10000L;
            }
            else
            {
                nextId = number.longValue() + 1;
            }
        }
        map.put(elementType, nextId + 1);
        return nextId;
    }

    public boolean save()
    {
        boolean ret = false;
        for (Map.Entry<String, Long> entry : map.entrySet())
        {
            long l = entry.getValue();
            l = l + 10 - (l % 10);

            final Element element = ConfigXmlUtils.getElementByXpath(document,
                    "/entity-engine-xml/SequenceValueItem[@seqName='" + entry.getKey() + "']");
            if (element == null)
            {
                final Element newElement = ConfigXmlUtils.createNewElement(document.getRootElement(), ELEMENT_SEQUENCE_VALUE_ITEM);
                ConfigXmlUtils.setAttribute(newElement, ATTRIB_SEQ_NAME, entry.getKey());
                ConfigXmlUtils.setAttribute(newElement, ATTRIB_SEQ_ID, String.valueOf(l));
                ret = true;
            }
            else
            {
                final Long value = ConfigXmlUtils.getLongValue(element, ATTRIB_SEQ_ID);
                if (value == null || value != l)
                {
                    ConfigXmlUtils.setAttribute(element, ATTRIB_SEQ_ID, String.valueOf(l));
                    ret = true;
                }
            }
        }

        return ret;
    }

    @Override
    public String toString()
    {
        return "Sequences: [" + map.toString() + "]";
    }
}
