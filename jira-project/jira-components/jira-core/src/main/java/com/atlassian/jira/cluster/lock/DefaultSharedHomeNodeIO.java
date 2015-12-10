package com.atlassian.jira.cluster.lock;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.config.util.JiraHome;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reader and writer for shared home node status.
 */
public class DefaultSharedHomeNodeIO implements SharedHomeNodeStatusReader, SharedHomeNodeStatusWriter
{
    private static final Logger log = LoggerFactory.getLogger(DefaultSharedHomeNodeIO.class);

    private final JiraHome jiraHome;

    public DefaultSharedHomeNodeIO(JiraHome jiraHome)
    {
        this.jiraHome = jiraHome;
    }

    @Nonnull
    protected File getNodeStatusDirectory()
    {
        return new File(jiraHome.getHome(), "node-status");
    }

    @Nullable
    @Override
    public NodeSharedHomeStatus readNodeStatus(@Nonnull String nodeId)
    {
        final File nodeStatusFile = new File(getNodeStatusDirectory(), nodeId);
        if (!nodeStatusFile.exists())
        {
            return null;
        }

        try
        {
            final String content = Files.toString(nodeStatusFile, Charsets.UTF_8);
            final long contentTimestamp = Long.parseLong(content);

            return new NodeSharedHomeStatus(nodeId, contentTimestamp);
        }
        catch (IOException e)
        {
            throw new RuntimeException("I/O error reading node status file " + nodeStatusFile.getAbsolutePath() + ": " + e, e);
        }
    }

    @Override
    public void writeNodeStatus(@Nonnull NodeSharedHomeStatus status)
    {
        final File nodeStatusDir = getNodeStatusDirectory();
        nodeStatusDir.mkdirs();

        final String nodeId = status.getNodeId();

        //Write temp file and rename so we minimize risk that another process reads a half-written file
        //(assuming that move()->File.renameTo() is atomic which it is on many OSes)
        final File nodeStatusFile = new File(nodeStatusDir, nodeId);
        final File nodeStatusFileTmp = new File(nodeStatusDir, nodeId + ".tmp");
        final String content = String.valueOf(status.getUpdateTime());
        try
        {
            Files.write(content, nodeStatusFileTmp, Charsets.UTF_8);
            Files.move(nodeStatusFileTmp, nodeStatusFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException("I/O error writing node status file " + nodeStatusFile.getAbsolutePath() + ": " + e, e);
        }
    }

    @Override
    public void removeNodeStatus(@Nonnull String nodeId)
    {
        final File nodeStatusDir = getNodeStatusDirectory();
        final File nodeStatusFile = new File(nodeStatusDir, nodeId);
        if (nodeStatusFile.exists())
        {
            boolean deleted = nodeStatusFile.delete();
            if (!deleted)
            {
                log.warn("Failed to remove node status file " + nodeStatusFile.getAbsolutePath());
            }
        }
    }
}
