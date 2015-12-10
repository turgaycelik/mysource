package com.atlassian.jira.web.session.currentusers;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple deletion strategy for clearing entries out of the {@link JiraUserSessionTracker}
 * <p/>
 * For {@link javax.servlet.http.HttpSession} based sessions, the {@link JiraUserSessionDestroyListener} will clean up
 * sessions. However we still check here any way just in case the session listener does no inform us.  This will prevent
 * memory overruns.
 * <p/>
 * For SOAP/REST calls, the deletion strategy needs to be time based.
 * <p/>
 * We only only run the checks every 30 seconds so traveral of the list is not carried out on every request.  This is to
 * reduce the performance impact of looking for stale sessions.
 *
 * @since v4.0
 */
class JiraUserSessionTrackerDeletionStrategy
{
    private final AtomicBoolean deleteStaleSessionsPermit;
    private long nextCheckTime;

    private static final long MAX_SESSION_AGE = 4 * 60 * 60 * 1000; // 4 hours

    private static final long MIN_TIME_BETWEEN_CHECKS = 30 * 1000; // 30 seconds

    JiraUserSessionTrackerDeletionStrategy()
    {
        nextCheckTime = System.currentTimeMillis();
        deleteStaleSessionsPermit = new AtomicBoolean(false);
    }

    /**
     * Deletes stale sessions from the passed in map of {@link MutableJiraUserSession}s.
     *
     * @param liveSessionMap this is the "live" map passed in by the owning JiraUserSessionTracker object
     */
    void deleteStaleSessions(ConcurrentMap<String, MutableJiraUserSession> liveSessionMap)
    {
        // this can be a contention point if all exiting threads start ripping throught the
        // map of sessions looking for stale threads.  So we use a 'first in does to work'
        // strategy and the losers are happy to continue on their merry way
        final long now = System.currentTimeMillis();
        if (now >= nextCheckTime)
        {
            // ok so its time someone had a check.  Will it be me?
            if (deleteStaleSessionsPermit.compareAndSet(false, true))
            {
                // it was me.  we managed to get the permit to proceed
                try
                {
                    cleanupStaleSessions(liveSessionMap);
                }
                finally
                {
                    nextCheckTime = now + MIN_TIME_BETWEEN_CHECKS;
                    deleteStaleSessionsPermit.set(false);
                }
            }
        }
    }

    private void cleanupStaleSessions(ConcurrentMap<String, MutableJiraUserSession> liveSessionMap)
    {
        for (Iterator<Map.Entry<String, MutableJiraUserSession>> it = liveSessionMap.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<String, MutableJiraUserSession> next = it.next();
            if (sessionHasExpired(next.getValue()))
            {
                it.remove();
            }
        }
    }

    private boolean sessionHasExpired(final MutableJiraUserSession userSession)
    {
        if (userSession == null)
        {
            return false;
        }
        Date lastAccessTime = userSession.getLastAccessTime();
        long sessionAgeMillis = System.currentTimeMillis() - lastAccessTime.getTime();
        return sessionAgeMillis > MAX_SESSION_AGE;
    }
}
