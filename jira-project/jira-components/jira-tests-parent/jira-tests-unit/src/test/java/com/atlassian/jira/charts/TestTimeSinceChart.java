package com.atlassian.jira.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.statistics.DateFieldSorter;
import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
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
public class TestTimeSinceChart
{
    @Test
    public void testCollect()
    {
        final Map<RegularTimePeriod, Number> resolvedMap = new LinkedHashMap<RegularTimePeriod, Number>();
        MockControl mockSearcherControl = MockClassControl.createNiceControl(IndexSearcher.class);
        IndexSearcher mockSearcher = (IndexSearcher) mockSearcherControl.getMock();
        mockSearcherControl.replay();
        
        final String dateFieldId = "targetDateField";

        final List<Document> docs = new ArrayList<Document>();
        Date d1 = new DateTime(1970, 01, 01, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
        Date d2 = new DateTime(1970, 01, 02, 0, 0, 0, 0, DateTimeZone.UTC).toDate();

        Document doc = new Document();
        doc.add(new Field(dateFieldId, LuceneUtils.dateToString(d1), Field.Store.YES, Field.Index.NOT_ANALYZED));
        docs.add(doc);

        doc = new Document();
        doc.add(new Field(dateFieldId, LuceneUtils.dateToString(d2), Field.Store.YES, Field.Index.NOT_ANALYZED));
        docs.add(doc);

        doc = new Document();
        doc.add(new Field(dateFieldId, LuceneUtils.dateToString(d2), Field.Store.YES, Field.Index.NOT_ANALYZED));
        docs.add(doc);

        final FieldableDocumentHitCollector documentHitCollector =
                new TimeSinceChart.GenericDateFieldIssuesHitCollector(resolvedMap, mockSearcher, DateFieldSorter.ISSUE_CREATED_STATSMAPPER,
                        Day.class, dateFieldId, RegularTimePeriod.DEFAULT_TIME_ZONE);

        for (final Document document : docs)
        {
            documentHitCollector.collect(document);
        }

        assertEquals(2, resolvedMap.size());
        final Iterator iterator = resolvedMap.values().iterator();
        assertEquals(1, iterator.next());
        assertEquals(2, iterator.next());
    }
}
