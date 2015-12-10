package com.atlassian.jira.upgrade;

import java.util.Date;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.1
 */
public class UpgradeHistoryItemImpl implements UpgradeHistoryItem
{
    private final Date timePerformed;
    private final String targetBuildNumber;
    private final String targetVersion;
    private final String originalVersion;
    private final String originalBuildNumber;
    private final boolean inferred;

    public UpgradeHistoryItemImpl(final Date timePerformed, final String targetBuildNumber, final String targetVersion, final String originalBuildNumber, final String originalVersion)
    {
        this(timePerformed, targetBuildNumber, targetVersion, originalBuildNumber, originalVersion, false);
    }

    public UpgradeHistoryItemImpl(final Date timePerformed, final String targetBuildNumber, final String targetVersion, final String originalBuildNumber, final String originalVersion, final boolean inferred)
    {
        this.timePerformed = timePerformed;
        this.originalVersion = originalVersion;
        this.originalBuildNumber = originalBuildNumber;
        this.targetBuildNumber = notNull("targetBuildNumber", targetBuildNumber);
        this.targetVersion = notNull("targetVersion", targetVersion);
        this.inferred = inferred;
    }

    public Date getTimePerformed()
    {
        return timePerformed;
    }

    public String getTargetBuildNumber()
    {
        return targetBuildNumber;
    }

    public String getOriginalBuildNumber()
    {
        return originalBuildNumber;
    }

    public String getTargetVersion()
    {
        return targetVersion;
    }

    public String getOriginalVersion()
    {
        return originalVersion;
    }

    public boolean isInferred()
    {
        return inferred;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final UpgradeHistoryItemImpl that = (UpgradeHistoryItemImpl) o;

        if (inferred != that.inferred)
        {
            return false;
        }
        if (originalBuildNumber != null ? !originalBuildNumber.equals(that.originalBuildNumber) : that.originalBuildNumber != null)
        {
            return false;
        }
        if (originalVersion != null ? !originalVersion.equals(that.originalVersion) : that.originalVersion != null)
        {
            return false;
        }
        if (targetBuildNumber != null ? !targetBuildNumber.equals(that.targetBuildNumber) : that.targetBuildNumber != null)
        {
            return false;
        }
        if (targetVersion != null ? !targetVersion.equals(that.targetVersion) : that.targetVersion != null)
        {
            return false;
        }
        if (timePerformed != null ? !timePerformed.equals(that.timePerformed) : that.timePerformed != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = timePerformed != null ? timePerformed.hashCode() : 0;
        result = 31 * result + (targetBuildNumber != null ? targetBuildNumber.hashCode() : 0);
        result = 31 * result + (targetVersion != null ? targetVersion.hashCode() : 0);
        result = 31 * result + (originalVersion != null ? originalVersion.hashCode() : 0);
        result = 31 * result + (originalBuildNumber != null ? originalBuildNumber.hashCode() : 0);
        result = 31 * result + (inferred ? 1 : 0);
        return result;
    }
}
