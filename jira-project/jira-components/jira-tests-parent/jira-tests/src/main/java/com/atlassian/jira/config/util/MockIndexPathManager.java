package com.atlassian.jira.config.util;

/**
 * @since v4.0
 */
public class MockIndexPathManager implements IndexPathManager
{
    private String path;
    private boolean jiraHome = false;
    public static final String DEFAULT_PATH = "/a/b/index/path";

    public MockIndexPathManager()
    {
        this.path = DEFAULT_PATH;
    }

    public MockIndexPathManager(final String path)
    {
        this.path = path;
    }

    public String getIndexRootPath()
    {
        return path;
    }

    public String getDefaultIndexRootPath()
    {
        return "/jira_home/caches/indexes/";
    }

    public String getIssueIndexPath()
    {
        return getIndexRootPath() + IndexPathManager.Directory.ISSUES_SUBDIR;
    }

    public String getCommentIndexPath()
    {
        return getIndexRootPath() + IndexPathManager.Directory.COMMENTS_SUBDIR;
    }

    public String getChangeHistoryIndexPath()
    {
        return getIndexRootPath() + IndexPathManager.Directory.CHANGE_HISTORY_SUBDIR;
    }

    public String getPluginIndexRootPath()
    {
        return getIndexRootPath() + IndexPathManager.Directory.PLUGINS_SUBDIR;
    }

    public String getSharedEntityIndexPath()
    {
        return getIndexRootPath() + IndexPathManager.Directory.ENTITIES_SUBDIR;
    }

    public void setIndexRootPath(final String indexPath)
    {
        this.path = indexPath;
        this.jiraHome = false;
    }

    public void setUseDefaultDirectory()
    {
        this.jiraHome = false;
    }

    public boolean getUseDefaultDirectory()
    {
        return this.jiraHome;
    }

    public Mode getMode()
    {
        if (jiraHome)
        {
            return Mode.DEFAULT;
        }
        else
        {
            return Mode.CUSTOM;
        }
    }
}
