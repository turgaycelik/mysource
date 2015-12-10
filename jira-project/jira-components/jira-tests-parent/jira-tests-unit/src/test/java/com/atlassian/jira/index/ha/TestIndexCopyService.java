package com.atlassian.jira.index.ha;

import java.io.File;

import javax.servlet.ServletContext;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.cluster.Message;
import com.atlassian.jira.cluster.MessageHandlerService;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.index.IssueIndexer;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.PathUtils;
import com.atlassian.johnson.JohnsonEventContainer;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.DelegatorInterface;

import static com.atlassian.jira.index.ha.DefaultIndexCopyService.BACKUP_INDEX_DONE;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestIndexCopyService extends TestClusteredIndex
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    IndexUtils mockIndexUtils;

    @Mock
    private MessageHandlerService mockMessageHandlerService;
    @Mock
    private ServletContext mockServletContext;
    @Mock
    private JohnsonEventContainer mockJohnsonEventContainer;
    @Mock
    private EventPublisher mockEventPublisher;
    @Mock
    private IndexRecoveryManager mockRecoveryManager;
    @Mock
    private DelegatorInterface mockDelegatorInterface;
    @Mock
    private OfBizReplicatedIndexOperationStore ofBizReplicatedIndexOperationStore;
    @Mock
    private IssueIndexManager issueIndexManager;

    private final I18nHelper i18n = new MockI18nHelper();

    @Mock
    @AvailableInContainer
    private IssueIndexer indexIssuer;

    @Mock
    @AvailableInContainer
    private SharedEntityIndexer sharedEntityIndexer;

    private DefaultIndexCopyService service;

    @Before
    public void setupMocks() throws Exception
    {
        when(mockJirahome.getHomePath()).thenReturn(sharedHome.getAbsolutePath());
        when(mockJirahome.getCachesDirectory()).thenReturn(new File(PathUtils.joinPaths(sharedHome.getAbsolutePath(), "caches")));
        when(mockServletContext.getAttribute(JohnsonEventContainer.class.getName())).thenReturn(mockJohnsonEventContainer);
        service =  new DefaultIndexCopyService(mockIndexPathManager, mockJirahome, mockIndexUtils, mockMessageHandlerService,
                mockEventPublisher, mockRecoveryManager, mockDelegatorInterface, i18n, ofBizReplicatedIndexOperationStore);
    }

    @Test
    public void testBackupIndex() throws Exception
    {
        when(mockDelegatorInterface.getNextSeqId("IndexBackupSequence")).thenReturn(1L);
        when(mockIndexUtils.takeIndexSnapshot(eq(localHome.getAbsolutePath()),
                eq(PathUtils.joinPaths(sharedHome.getAbsolutePath(), "caches")), eq("1"), eq(3))).thenReturn("IndexSnapshot_1.zip");
        service.backupIndex("wantedHere");
        verify(mockMessageHandlerService).sendMessage("wantedHere", new Message(BACKUP_INDEX_DONE, "IndexSnapshot_1.zip"));
    }

    @Test
    public void testRestoreIndex() throws Exception
    {
        service.restoreIndex("backup.zip");
        // IndexRecoveryManager is responsible for stopping the indexes and blocking incoming searches so there's no much to verify
        verify(mockRecoveryManager).recoverIndexFromBackup(new File(mockJirahome.getCachesDirectory().getAbsolutePath(), "backup.zip"), TaskProgressSink.NULL_SINK);
    }

    @Test
    public void testCopyIndexes() throws Exception
    {
        final long id = 1L;
        final String sourcePath = "a";
        final String destinationPath = "b";
        final String destinationFile = "IndexSnapshot_" + id + ".zip";
        try
        {
            when(mockIndexUtils.takeIndexSnapshot(eq(sourcePath), eq(destinationPath),
                    eq("1"), eq(3))).thenReturn(destinationFile);
            final String filename = service.copyIndex(sourcePath, destinationPath, id);
            assertThat(filename, is(destinationFile));
        }
        finally
        {
            deleteQuietly(new File(destinationPath, destinationFile));
        }
    }

    private static class TempFileMatcher extends BaseMatcher<String>
    {

        private final String jiraIndexBackup;

        private TempFileMatcher(final String jiraIndexBackup)
        {
            this.jiraIndexBackup = jiraIndexBackup;
        }

        @Override
        public boolean matches(final Object item)
        {
            return item instanceof String && ((String) item).matches(".*/" + jiraIndexBackup + "[0-9]*");
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("Temporary file with base name: '" + jiraIndexBackup + "'");
        }
    }
}
