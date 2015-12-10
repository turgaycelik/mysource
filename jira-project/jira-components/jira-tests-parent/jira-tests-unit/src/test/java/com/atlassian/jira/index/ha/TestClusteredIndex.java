package com.atlassian.jira.index.ha;

import java.io.File;
import java.util.List;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.LuceneDirectoryUtils;
import com.atlassian.jira.util.PathUtils;

import com.google.common.io.Files;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public abstract class TestClusteredIndex
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);
    @Mock
    protected JiraHome mockJirahome;
    @Mock
    @AvailableInContainer
    protected ClusterManager mockClusterManager;
    @Mock
    protected IndexPathManager mockIndexPathManager;
    @Mock
    protected LuceneDirectoryUtils mockLuceneDirectoryUtils;
    @Mock
    protected IssueIndexManager mockIssueIndexManager;



    protected File sharedHome;
    protected File localHome;

    public static String FIELD="field";
    public static String VALUE="value";

    @Before
    public void setupBaseMocks() throws Exception
    {
        sharedHome = buildSharedHome();
        localHome = buildLocalHome();
        when(mockClusterManager.isClustered()).thenReturn(true);
        when(mockJirahome.getHome()).thenReturn(sharedHome);
        when(mockIndexPathManager.getIndexRootPath()).thenReturn(localHome.getAbsolutePath());
        when(mockIssueIndexManager.getAnalyzerForIndexing()).thenReturn(ClusteredTestUtils.ENGLISH_ANALYZER);
        when(mockLuceneDirectoryUtils.getDirectory(isA(File.class))).then(new Answer<Directory>()
        {
            @Override
            public Directory answer(final InvocationOnMock invocation) throws Throwable
            {
                Object[] args = invocation.getArguments();
                return ClusteredTestUtils.createIndexDirectory((File)args[0]);
            }
        });
    }

    private File buildSharedHome() throws Exception
    {
        File sharedHome = Files.createTempDir();
        for (IndexUtils.IndexPath path : IndexUtils.IndexPath.values())
        {
            new File(PathUtils.joinPaths(sharedHome.getAbsolutePath(), path.getPath())).mkdir();
        }
        return sharedHome;
    }

    private File buildLocalHome() throws Exception
    {
        File localHome = Files.createTempDir();
        for (IndexUtils.IndexPath path : IndexUtils.IndexPath.values())
        {
            File f = new File(PathUtils.joinPaths(localHome.getAbsolutePath(),path.getPath()));
            f.mkdir();
        }
        return localHome;
    }

    protected List<Document> getIssueDocuments(final String path) throws Exception
    {
        File destinationDirectory = new File(path);
        return ClusteredTestUtils.getDocuments(destinationDirectory);
    }

    @After
    public void tearDown()
    {
        localHome.delete();
        sharedHome.delete();
    }

}
