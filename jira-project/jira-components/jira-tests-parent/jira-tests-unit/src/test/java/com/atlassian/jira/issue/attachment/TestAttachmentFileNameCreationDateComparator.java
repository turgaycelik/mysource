package com.atlassian.jira.issue.attachment;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Locale;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestAttachmentFileNameCreationDateComparator
{
    private Comparator<Attachment> comparator = new AttachmentFileNameCreationDateComparator(Locale.ENGLISH);

    @Test
    public void testFileNames()
    {
        Attachment a = getAttachmentWithFileName("a");
        Attachment a1 = getAttachmentWithFileName("a");
        Attachment z = getAttachmentWithFileName("z");
        Attachment nullA = getAttachmentWithFileName(null);

        assertTrue(comparator.compare(a, z) < 0);
        assertTrue(comparator.compare(z, a) > 0);

        assertEquals(0, comparator.compare(a, a));
        assertEquals(0, comparator.compare(a, a1));

        assertTrue(comparator.compare(a, nullA) > 0);
        assertTrue(comparator.compare(nullA, a) < 0);

        assertEquals(0, comparator.compare(nullA, nullA));
    }

    @Test
    public void testFileNamesAndDates()
    {
        Attachment a = getAttachmentWithFileNameAndDate("a", new Timestamp(1000));
        Attachment a1 = getAttachmentWithFileNameAndDate("a", new Timestamp(10001));

        Attachment nullA = getAttachmentWithFileNameAndDate("a", null);
        Attachment nullnull = getAttachmentWithFileNameAndDate(null, null);

        assertEquals(-1, comparator.compare(a1, a));
        assertEquals(1, comparator.compare(a, a1));
        assertEquals(0, comparator.compare(a, a));

        assertEquals(1, comparator.compare(a, nullA));
        assertEquals(-1, comparator.compare(nullA, a));
        assertEquals(0, comparator.compare(nullA, nullA));

        assertEquals(1, comparator.compare(a, nullnull));
        assertEquals(1, comparator.compare(nullA, nullnull));
        assertEquals(0, comparator.compare(nullnull, nullnull));

    }

    private Attachment getAttachmentWithFileName(final String s)
    {
        return getAttachmentWithFileNameAndDate(s, null);
    }

    private Attachment getAttachmentWithFileNameAndDate(final String s, final Timestamp date)
    {
        MockGenericValue dummy = new MockGenericValue("Attachment");

        return new Attachment(null, dummy, null)
        {
            public String getFilename()
            {
                return s;
            }

            public Timestamp getCreated()
            {
                return date;
            }
        };
    }
}
