package com.atlassian.jira.index;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.IndexWriterConfiguration.WriterSettings;
import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultConfiguration
{
    @Test
    public void testNullDirectory() throws Exception
    {
        try
        {
            new DefaultConfiguration(null, new StandardAnalyzer(LuceneVersion.get()));
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullAnalyzer() throws Exception
    {
        try
        {
            new DefaultConfiguration(new RAMDirectory(), null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testDirectory() throws Exception
    {
        final RAMDirectory directory = new RAMDirectory();
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer(LuceneVersion.get()));
        assertSame(directory, configuration.getDirectory());
    }

    @Test
    public void testAnalyzer() throws Exception
    {
        final StandardAnalyzer analyzer = new StandardAnalyzer(LuceneVersion.get());
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), analyzer);
        assertSame(analyzer, configuration.getAnalyzer());
    }

    @Test
    public void testInteractiveDefaults() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final WriterSettings settings = configuration.getWriterSettings(UpdateMode.INTERACTIVE);
        assertEquals(4, settings.getMergeFactor());
        assertEquals(5000, settings.getMaxMergeDocs());
        assertEquals(300, settings.getMaxBufferedDocs());
        assertEquals(1000000, settings.getMaxFieldLength());
    }

    @Test
    public void testBatchDefaults() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final WriterSettings settings = configuration.getWriterSettings(UpdateMode.BATCH);
        assertEquals(50, settings.getMergeFactor());
        assertEquals(Integer.MAX_VALUE, settings.getMaxMergeDocs());
        assertEquals(300, settings.getMaxBufferedDocs());
        assertEquals(1000000, settings.getMaxFieldLength());
    }

    private static TieredMergePolicy initMergePolicyTest(MockApplicationProperties mockApplicationProperties, UpdateMode updateMode)
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(ApplicationProperties.class, mockApplicationProperties));
        final StandardAnalyzer analyzer = new StandardAnalyzer(LuceneVersion.get());
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), analyzer);
        final WriterSettings settings = configuration.getWriterSettings(UpdateMode.INTERACTIVE);
        final MergePolicy policy = settings.getWriterConfiguration(analyzer).getMergePolicy();
        assertTrue("Expected a TieredMergePolicy", policy instanceof TieredMergePolicy);
        return (TieredMergePolicy)policy;
    }

    // NOTE: We don't expect the merge policy settings to differ for BATCH and INTERACTIVE,
    // (at least for the time being)...

    @Test
    public void testInteractiveMergePolicyDefaults() throws Exception
    {
        assertDefaultMergePolicy(initMergePolicyTest(new MockApplicationProperties(), UpdateMode.INTERACTIVE));
    }

    @Test
    public void testBatchMergePolicyDefaults() throws Exception
    {
        assertDefaultMergePolicy(initMergePolicyTest(new MockApplicationProperties(), UpdateMode.BATCH));
    }

    @Test
    public void testInteractiveMergePolicyCustom() throws Exception
    {
        assertCustomMergePolicy(initMergePolicyTest(getCustomApplicationProperties(), UpdateMode.INTERACTIVE));
    }

    @Test
    public void testBatchMergePolicyCustom() throws Exception
    {
        assertCustomMergePolicy(initMergePolicyTest(getCustomApplicationProperties(), UpdateMode.BATCH));
    }



    private static void assertDefaultMergePolicy(TieredMergePolicy policy)
    {
        assertEquals(10.0, policy.getExpungeDeletesPctAllowed(), 0.001);
        // assertEquals(2.0, policy.getFloorSegmentMB(), 0.001);  // LUCENE-4222
        assertEquals(2097152.0, policy.getFloorSegmentMB(), 0.001);  // LUCENE-4222
        assertEquals(10, policy.getMaxMergeAtOnce());
        assertEquals(30, policy.getMaxMergeAtOnceExplicit());
        assertEquals(512.0, policy.getMaxMergedSegmentMB(), 0.001);
        assertEquals(0.10, policy.getNoCFSRatio(), 0.001);
        assertEquals(10.0, policy.getSegmentsPerTier(), 0.001);
        assertEquals(true, policy.getUseCompoundFile());
    }

    private static void assertCustomMergePolicy(TieredMergePolicy policy)
    {
        assertEquals(2.0, policy.getExpungeDeletesPctAllowed(), 0.001);
        // assertEquals(1.0, policy.getFloorSegmentMB(), 0.001);  // LUCENE-4222
        assertEquals(1048576.0, policy.getFloorSegmentMB(), 0.001);  // LUCENE-4222
        assertEquals(3, policy.getMaxMergeAtOnce());
        assertEquals(4, policy.getMaxMergeAtOnceExplicit());
        assertEquals(5.0, policy.getMaxMergedSegmentMB(), 0.001);
        assertEquals(0.06, policy.getNoCFSRatio(), 0.001);
        assertEquals(7.0, policy.getSegmentsPerTier(), 0.001);
        assertEquals(false, policy.getUseCompoundFile());
    }

    private static MockApplicationProperties getCustomApplicationProperties()
    {
        final MockApplicationProperties properties = new MockApplicationProperties();
        properties.setText(APKeys.JiraIndexConfiguration.MergePolicy.EXPUNGE_DELETES_PCT_ALLOWED, "2");
        properties.setText(APKeys.JiraIndexConfiguration.MergePolicy.FLOOR_SEGMENT_MB, "1");
        properties.setText(APKeys.JiraIndexConfiguration.MergePolicy.MAX_MERGE_AT_ONCE, "3");
        properties.setText(APKeys.JiraIndexConfiguration.MergePolicy.MAX_MERGE_AT_ONCE_EXPLICIT, "4");
        properties.setText(APKeys.JiraIndexConfiguration.MergePolicy.MAX_MERGED_SEGMENT_MB, "5");
        properties.setText(APKeys.JiraIndexConfiguration.MergePolicy.NO_CFS_PCT, "6");
        properties.setText(APKeys.JiraIndexConfiguration.MergePolicy.SEGMENTS_PER_TIER, "7");
        properties.setText(APKeys.JiraIndexConfiguration.MergePolicy.USE_COMPOUND_FILE, "false");
        return properties;
    }
}
