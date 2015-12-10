package com.atlassian.jira.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.util.LuceneUtils;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestDateRangeTimeChart
{

    @Test
    public void testCollect()
    {
        final String creationDateConstant = "creationDate";
        final String otherDateConstant = "otherDate";
        final Map<RegularTimePeriod, List<Long>> result = new HashMap<RegularTimePeriod, List<Long>>();
        final List<Document> documents = new ArrayList<Document>();

        Document doc = new Document();
        Date d1 = new DateTime(1970, 01, 01, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
        Date d2 = new DateTime(1970, 01, 02, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
        Date d3 = new DateTime(1970, 01, 03, 0, 0, 0, 0, DateTimeZone.UTC).toDate();

        doc.add(new Field(creationDateConstant, LuceneUtils.dateToString(d1), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(otherDateConstant, LuceneUtils.dateToString(d2), Field.Store.YES, Field.Index.NOT_ANALYZED));
        documents.add(doc);

        doc = new Document();
        doc.add(new Field(creationDateConstant, LuceneUtils.dateToString(d1), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(otherDateConstant, LuceneUtils.dateToString(d3), Field.Store.YES, Field.Index.NOT_ANALYZED));
        documents.add(doc);

        doc = new Document();
        doc.add(new Field(creationDateConstant, LuceneUtils.dateToString(d1), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(otherDateConstant, LuceneUtils.dateToString(d3), Field.Store.YES, Field.Index.NOT_ANALYZED));
        documents.add(doc);

        MockControl mockSearcherControl = MockClassControl.createNiceControl(IndexSearcher.class);
        IndexSearcher mockSearcher = (IndexSearcher) mockSearcherControl.getMock();
        mockSearcherControl.replay();

        final DateRangeTimeChart.DateRangeObjectHitCollector documentHitCollector =
                new DateRangeTimeChart.DateRangeObjectHitCollector(creationDateConstant, otherDateConstant, result, mockSearcher, Day.class, RegularTimePeriod.DEFAULT_TIME_ZONE);

        for (final Document document : documents)
        {
            documentHitCollector.collect(document);
        }


        assertEquals(2, result.size());
        List<Long> values = result.get(RegularTimePeriod.createInstance(Day.class, d2, RegularTimePeriod.DEFAULT_TIME_ZONE));
        assertEquals(1, values.size());
        assertEquals(DateUtils.DAY_MILLIS, values.get(0).longValue());

        values = result.get(RegularTimePeriod.createInstance(Day.class, d3, RegularTimePeriod.DEFAULT_TIME_ZONE));
        assertEquals(2, values.size());
        assertEquals(DateUtils.DAY_MILLIS * 2, values.get(0).longValue());
        assertEquals(DateUtils.DAY_MILLIS * 2, values.get(1).longValue());
    }
}
