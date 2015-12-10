package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.event.api.EventPublisher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @since v1.3.7
 */
public class OfBizCacheFlushingManagerTest
{
    private OfBizCacheFlushingManager ofBizCacheFlushingManager;
    private EventPublisher eventPublisher = mock(EventPublisher.class);
    private OfBizUserDao ofBizUserDao = mock(OfBizUserDao.class);
    private OfBizDirectoryDao ofBizDirectoryDao = mock(OfBizDirectoryDao.class);
    private OfBizGroupDao ofBizGroupDao = mock(OfBizGroupDao.class);
    private OfBizInternalMembershipDao ofBizInternalMembershipDao = mock(OfBizInternalMembershipDao.class);
    private OfBizApplicationDao ofBizApplicationDao = mock(OfBizApplicationDao.class);

    @Before
    public void setUp() throws Exception
    {
        ofBizCacheFlushingManager = new OfBizCacheFlushingManager(eventPublisher, ofBizUserDao, ofBizDirectoryDao,
                ofBizGroupDao, ofBizInternalMembershipDao, ofBizApplicationDao);
    }

    @Test
    public void testFlushOrder()
    {

        ArgumentCaptor<OfBizCacheFlushingManager.OfBizCacheFlushingManagerListener> argument =
                ArgumentCaptor.forClass(OfBizCacheFlushingManager.OfBizCacheFlushingManagerListener.class);

        verify(eventPublisher).register(argument.capture());

        argument.getValue().onEvent(new XMLRestoreFinishedEvent(null));

        InOrder inOrder = inOrder(ofBizApplicationDao,
                ofBizDirectoryDao,
                ofBizUserDao,
                ofBizGroupDao,
                ofBizInternalMembershipDao);

        inOrder.verify(ofBizApplicationDao).flushCache();
        inOrder.verify(ofBizDirectoryDao).flushCache();
        inOrder.verify(ofBizUserDao).flushCache();
        inOrder.verify(ofBizGroupDao).flushCache();
        inOrder.verify(ofBizInternalMembershipDao).flushCache();

    }

}
