package com.atlassian.jira.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
import com.atlassian.jira.util.LuceneUtils;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestRecentlyCreatedChart
{
    @Test
    public void testCollect()
    {
        final Map resolvedMap = new HashMap();
        final Map unresolvedMap = new HashMap();

        final List docs = new ArrayList();
        Document doc;
        Date d1 = new DateTime(1970, 01, 01, 0, 0, 0, 0, DateTimeZone.UTC).toDate();
        Date d2 = new DateTime(1970, 01, 02, 0, 0, 0, 0, DateTimeZone.UTC).toDate();

        doc = new Document();
        doc.add(new Field(DocumentConstants.ISSUE_CREATED, LuceneUtils.dateToString(d2), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(DocumentConstants.ISSUE_RESOLUTION, "-1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        docs.add(doc);

        doc = new Document();
        doc.add(new Field(DocumentConstants.ISSUE_CREATED, LuceneUtils.dateToString(d2), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(DocumentConstants.ISSUE_RESOLUTION, "-1", Field.Store.YES, Field.Index.NOT_ANALYZED));
        docs.add(doc);

        doc = new Document();
        doc.add(new Field(DocumentConstants.ISSUE_CREATED, LuceneUtils.dateToString(d1), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(DocumentConstants.ISSUE_RESOLUTION, "Completed", Field.Store.YES, Field.Index.NOT_ANALYZED));
        docs.add(doc);

        doc = new Document();
        doc.add(new Field(DocumentConstants.ISSUE_CREATED, LuceneUtils.dateToString(d2), Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(DocumentConstants.ISSUE_RESOLUTION, "Cancelled", Field.Store.YES, Field.Index.NOT_ANALYZED));
        docs.add(doc);


        FieldableDocumentHitCollector docHitCollector = new RecentlyCreatedChart.ResolutionSplittingCreatedIssuesHitCollector(resolvedMap, unresolvedMap, null, Day.class, RegularTimePeriod.DEFAULT_TIME_ZONE);

        for (final Iterator i = docs.iterator(); i.hasNext();)
        {
            docHitCollector.collect((Document) i.next());
        }

        assertEquals(2, resolvedMap.size());
        assertEquals(new Integer(1), resolvedMap.get(
                RegularTimePeriod.createInstance(
                        Day.class,
                        d1,
                        RegularTimePeriod.DEFAULT_TIME_ZONE)));
        assertEquals(new Integer(1), resolvedMap.get(
                RegularTimePeriod.createInstance(
                        Day.class,
                        d2,
                        RegularTimePeriod.DEFAULT_TIME_ZONE)));


        assertEquals(1, unresolvedMap.size());
        assertEquals(new Integer(2), unresolvedMap.get(
                RegularTimePeriod.createInstance(
                        Day.class,
                        d2,
                        RegularTimePeriod.DEFAULT_TIME_ZONE)));
    }
}
