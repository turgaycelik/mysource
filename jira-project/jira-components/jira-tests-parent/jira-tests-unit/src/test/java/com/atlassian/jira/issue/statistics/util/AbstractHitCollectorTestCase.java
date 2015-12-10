package com.atlassian.jira.issue.statistics.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.index.JiraAnalyzer;
import com.atlassian.jira.mock.component.MockComponentWorker;

import com.google.common.collect.ImmutableList;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.ReflectionUtils;

import static org.junit.Assert.assertEquals;

/**
 * This class runs in 2 parameterised modes.  In the first mode we use a traditional directory reader, but in the
 * second mode we guarantee the reader has multiple segments and so test the segment behaviour of the hit collectors.
 */
@RunWith (Parameterized.class)
public abstract class AbstractHitCollectorTestCase
{
    @Mock
    private InstrumentRegistry instrumentRegistry;

    @Parameterized.Parameters
    public static Collection<Boolean[]> configs() {
        Boolean[] useSegmented = {Boolean.TRUE};
        Boolean[] useDirectory = {Boolean.FALSE};
        return  ImmutableList.of(useDirectory, useSegmented);
    }

    protected final Boolean useSegmentedReader;

    public AbstractHitCollectorTestCase(Boolean useSegmentedReader) {this.useSegmentedReader = useSegmentedReader;}

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(InstrumentRegistry.class, instrumentRegistry)
        );
    }

    protected void index(Document doc, String fieldName, String fieldValue)
    {
        doc.add(new Field(fieldName, fieldValue, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    protected IndexReader addToIndex(Collection<Document> docs) throws IOException
    {
        if (useSegmentedReader)
        {
            return writeToRamIndex(docs);
        }
        else
        {
            return writeToMultiSegmentIndex(docs);
        }

    }

    private IndexReader writeToMultiSegmentIndex(Collection<Document> docs) throws IOException
    {
        Directory dir = new RAMDirectory();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_CURRENT, new JiraAnalyzer(true, JiraAnalyzer.Stemming.ON, JiraAnalyzer.StopWordRemoval.ON));
        iwc.setMergeScheduler(new SerialMergeScheduler());
        iwc.setMaxBufferedDocs(5000);
        iwc.setRAMBufferSizeMB(100);
        MockMergePolicy fsmp = new MockMergePolicy(false);
        iwc.setMergePolicy(fsmp);
        IndexWriter writer = new IndexWriter(dir, iwc);
        Method seginfoMethod = ReflectionUtils.findMethod(IndexWriter.class, "getSegmentCount");
        ReflectionUtils.makeAccessible(seginfoMethod);
        int i = 0;
        for (Document doc : docs)
        {
            writer.addDocument(doc);
            writer.commit();
            assertEquals(Integer.valueOf(++i), (Integer) ReflectionUtils.invokeMethod(seginfoMethod, writer));
        }
        writer.close();
        return IndexReader.open(dir);
    }

    private IndexReader writeToRamIndex(Collection<Document> docs) throws IOException
    {
        Directory dir = new RAMDirectory();

        IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), DefaultIndexManager.ANALYZER_FOR_INDEXING);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, conf);

        for (Document doc : docs)
        {
            writer.addDocument(doc);
        }
        writer.close();
        return IndexReader.open(dir);
    }

    public class MockMergePolicy extends MergePolicy
    {
        boolean doMerge = false;
        int start;
        int length;

        private final boolean useCompoundFile;

        protected MockMergePolicy(boolean useCompoundFile) {
            this.useCompoundFile = useCompoundFile;
        }

        @Override
        public void close() {}

        @Override
        public MergeSpecification findMerges(SegmentInfos segmentInfos)
                throws CorruptIndexException, IOException {
            MergeSpecification ms = new MergeSpecification();
            if (doMerge) {
                OneMerge om = new OneMerge(segmentInfos.asList().subList(start, start + length));
                ms.add(om);
                doMerge = false;
                return ms;
            }
            return null;
        }

        @Override
        public MergeSpecification findMergesForOptimize(SegmentInfos segmentInfos, int maxSegmentCount, Map<SegmentInfo, Boolean> segmentsToOptimize)
                throws CorruptIndexException, IOException
        {
            return null;
        }

        @Override
        public MergeSpecification findMergesToExpungeDeletes(
                SegmentInfos segmentInfos) throws CorruptIndexException, IOException {
            return null;
        }

        @Override
        public boolean useCompoundFile(SegmentInfos segments, SegmentInfo newSegment) {
            return useCompoundFile;
        }
    }
}
