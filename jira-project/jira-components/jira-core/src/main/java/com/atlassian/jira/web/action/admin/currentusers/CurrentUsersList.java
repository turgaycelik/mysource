package com.atlassian.jira.web.action.admin.currentusers;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.util.lists.ListPager;
import com.atlassian.jira.web.session.currentusers.JiraUserSession;
import com.atlassian.jira.web.session.currentusers.JiraUserSessionTracker;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An action that lists the current users that have sessions within JIRA
 *
 * @since v4.1
 */
@WebSudoRequired
public class CurrentUsersList extends JiraWebActionSupport
{
    private static final String DATE_FORMAT = "hh:mm:ss yyyy/MM/dd z (Z)";

    private final JiraUserSessionTracker jiraUserSessionTracker;
    private final UserManager userManager;

    private ListPager<JiraUserSessionDisplayBean> pager;
    private int pageNo = 0;
    private int rowsPerPage = 50;


    public CurrentUsersList(JiraUserSessionTracker jiraUserSessionTracker, UserManager userManager)
    {
        this.jiraUserSessionTracker = jiraUserSessionTracker;
        this.userManager = userManager;
    }

    @Override
    protected String doExecute() throws Exception
    {
        pager = convertToDisplayBeansPager(jiraUserSessionTracker.getSnapshot());
        return INPUT;
    }

    private ListPager<JiraUserSessionDisplayBean> convertToDisplayBeansPager(final List<JiraUserSession> sessions)
    {
        List<JiraUserSessionDisplayBean> displayBeans = new ArrayList<JiraUserSessionDisplayBean>(sessions.size());
        for (JiraUserSession userSession : sessions)
        {
            displayBeans.add(new JiraUserSessionDisplayBean(userSession));
        }
        return new ListPager<JiraUserSessionDisplayBean>(displayBeans, pageNo, rowsPerPage)
        {
            @Override
            protected String generatePageURL(final int targetPageNo, final int rowsPerPage)
            {
                return new StringBuilder("CurrentUsersList.jspa")
                        .append("?pageNo=").append(targetPageNo)
                        .toString();
            }
        };

    }

    public ListPager<JiraUserSessionDisplayBean> getPager()
    {
        return pager;
    }

    /**
     * @return 1's based from index in the pager
     */
    public int getFromIndex()
    {
        return pager.getFromIndex()+1;
    }

    /**
     * @return 1's based to index (exclsuive) in the pager
     */
    public int getToIndex()
    {
        return pager.getToIndex();
    }

    public String getServerTime()
    {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }


    private String nvl(final String s)
    {
        return s == null ? notAvailable() : s;
    }

    private String toDateStr(final Date date)
    {
        String dateStr = null;
        if (date != null)
        {
            final long millisAgo = System.currentTimeMillis() - date.getTime();
            long secondsAgo = millisAgo / 1000;

            String timeAgoStr = DateUtils.getDurationString(secondsAgo);
            if (secondsAgo < 60)
            {
                timeAgoStr = secondsAgo + "s";
            }
            dateStr = new SimpleDateFormat(DATE_FORMAT).format(date) + " " + getText("admin.currentusers.seconds.ago", timeAgoStr);
        }
        return nvl(dateStr);
    }

    private String notAvailable()
    {
        return getText("admin.currentusers.not.available");
    }

    public int getPageNo()
    {
        return pageNo;
    }

    public void setPageNo(final int pageNo)
    {
        this.pageNo = pageNo;
    }

    public int getRowsPerPage()
    {
        return rowsPerPage;
    }

    private class JiraUserSessionDisplayBean
    {
        private final JiraUserSession jiraUserSession;

        private JiraUserSessionDisplayBean(JiraUserSession jiraUserSession)
        {
            this.jiraUserSession = jiraUserSession;
        }

        public String getId()
        {
            return nvl(jiraUserSession.getId());
        }

        public String getASessionId()
        {
            return nvl(jiraUserSession.getASessionId());
        }

        public boolean isValidUser()
        {
            return isValidUserImpl(jiraUserSession.getUserName());
        }

        public String getUserName()
        {
            return nvl(getSmartUserName());
        }

        private String getSmartUserName()
        {
            final String userName = jiraUserSession.getUserName();
            if (!isValidUserImpl(userName))
            {
                return null;
            }
            return userName;
        }

        private boolean isValidUserImpl(final String userName)
        {
            if (userName == null)
            {
                return false;
            }
            return userManager.getUser(userName) != null;
        }

        public String getIpAddress()
        {
            return nvl(jiraUserSession.getIpAddress());
        }

        public long getRequestCount()
        {
            return jiraUserSession.getRequestCount();
        }

        public String getCreationTime()
        {
            return toDateStr(jiraUserSession.getCreationTime());
        }

        public String getLastAccessTime()
        {
            return toDateStr(jiraUserSession.getLastAccessTime());
        }

        public String getType()
        {
            return jiraUserSession.getType().name();
        }
    }

}
