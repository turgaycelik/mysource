package com.atlassian.jira.index.ha;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.atlassian.core.util.FileUtils;
import com.atlassian.jira.util.PathUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.matchers.FileMatchers.exists;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestIndexUtils extends TestClusteredIndex
{
    private IndexUtils clusteredIndexUtils;

    @Before
    public void setupMocks() throws Exception
    {
        clusteredIndexUtils = new IndexUtils( mockIssueIndexManager, mockLuceneDirectoryUtils);
        populateLocalIndex(new File(PathUtils.joinPaths(localHome.getAbsolutePath(), IndexUtils.IndexPath.ISSUES.getPath())));
    }

    @Test
    public void testCopyIndex() throws Exception
    {
        clusteredIndexUtils.copyIndexes(localHome.getAbsolutePath(), sharedHome.getAbsolutePath());
        List<Document> documents = getIssueDocuments(PathUtils.joinPaths(sharedHome.getAbsolutePath(), IndexUtils.IndexPath.ISSUES.getPath() ));
        assertThat("The destination folder contains a single document", documents, hasSize(1));
    }

    @Test
    public void testClearIndex() throws Exception
    {
        final String path = PathUtils.joinPaths(localHome.getAbsolutePath(), IndexUtils.IndexPath.ISSUES.getPath());
        assertThat("We start with 1 document", getIssueDocuments(path), hasSize(1));
        clusteredIndexUtils.clearIndex(localHome.getAbsolutePath());
        assertThat("We have cleared the documents", getIssueDocuments(path), hasSize(0));
    }

    @Test
    public void testCleanDirectory() throws IOException, InterruptedException
    {
        File dir = createTempDirectory();
        try
        {
            File snapshotDir = new File(dir, "indexsnapshots");
            snapshotDir.mkdir();

            when(mockJirahome.getExportDirectory()).thenReturn(dir);
            // Create 6 snapshot files and 3 other files in a
            for (int i = 0; i < 6; i++)
            {
                File file = new File(snapshotDir, createFileName(i));
                file.createNewFile();
                //Fiddle the last modified date so the files have different times (resolution is only 1 second)
                file.setLastModified(System.currentTimeMillis() - 100000 + i * 5000);
            }
            new File(snapshotDir, "other1.txt").createNewFile();
            new File(snapshotDir, "other2.blah").createNewFile();
            new File(snapshotDir, "other3.zip").createNewFile();

            // Run the cleaner
            IndexUtils utils = new IndexUtils(null, null);
            utils.deleteOldSnapshots(snapshotDir, 3);

            // Test that the three earliest don't exits but the rest do;
            for (int i = 0; i < 3; i++)
            {
                assertThat(new File(snapshotDir, createFileName(i)), not(exists()));
            }
            for (int i = 3; i < 6; i++)
            {
                assertThat(new File(snapshotDir, createFileName(i)), exists());
            }
            assertThat(new File(snapshotDir, "other1.txt"), exists());
            assertThat(new File(snapshotDir, "other2.blah"), exists());
            assertThat(new File(snapshotDir, "other3.zip"), exists());
        }
        finally
        {
            FileUtils.deleteDir(dir);
        }

    }

    private String createFileName(int i)
    {
        DateFormat format = new SimpleDateFormat(IndexSnapshotService.DEFAULT_DATE_FORMAT);
        return "IndexSnapshot_" + format.format(new Date()) + "_" + i + ".zip";
    }

    private File createTempDirectory() throws IOException
    {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String name = "Test" + System.currentTimeMillis();
        File dir = new File(baseDir, name);
        dir.mkdir();
        return dir;
    }

    private void populateLocalIndex(final File directory) throws Exception
    {
        final Analyzer analyzerForIndexing = mockIssueIndexManager.getAnalyzerForIndexing();
        //Only populate Issues index for test
        Directory indexDir = mockLuceneDirectoryUtils.getDirectory(directory);
        Document doc = ClusteredTestUtils.buildDocument(ImmutableMap.of(FIELD, VALUE));
        ClusteredTestUtils.populateIndex(analyzerForIndexing, indexDir, Lists.newArrayList(doc));
    }

}
