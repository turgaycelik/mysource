package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import org.apache.commons.lang.time.DateUtils;

import javax.annotation.Nonnull;

/**
 * Backdoor control for indexing.
 *
 * @since v5.2
 */
public class IndexingControl extends BackdoorControl<IndexingControl>
{
    public IndexingControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    @Nonnull
    public IndexingProgress startInBackground()
    {
        return startInBackground(false, false);
    }

    @Nonnull
    public IndexingProgress startInBackground(boolean reindexComments, boolean reindexChangeHistory)
    {
        createResource().path("indexing").path("background")
                .queryParam("comments", String.valueOf(reindexComments))
                .queryParam("changeHistory", String.valueOf(reindexChangeHistory)).post();
        return new IndexingProgress() {

            @Override
            boolean isIndexing()
            {
                return isIndexingInProgress();
            }

            @Override
            boolean isIndexingStarted()
            {
                return checkIsIndexingStarted();
            }
        };
    }

    @Nonnull
    public IndexingProgress getInBackgroundProgress()
    {
        return new IndexingProgress() {

            @Override
            boolean isIndexing()
            {
                return isIndexingInProgress();
            }

            @Override
            boolean isIndexingStarted()
            {
                return checkIsIndexingStarted();
            }
        };
    }

    boolean isIndexingInProgress()
    {
        // returns true if indexing is running
        return createResource().path("indexing").get(Boolean.class);
    }

    boolean checkIsIndexingStarted()
    {
        // returns true if indexing has already started
        return createResource().path("indexing").path("started").get(Boolean.class);
    }

    @Nonnull
    public IndexingProgress getProjectIndexingProgress(final Long projectId)
    {
        return new IndexingProgress() {

            @Override
            boolean isIndexing()
            {
                return isIndexingProject(projectId);
            }

            @Override
            boolean isIndexingStarted()
            {
                return isIndexingProjectStarted(projectId);
            }
        };
    }

    boolean isIndexingProject(Long projectId)
    {
        // returns true if indexing is running
        return createResource().path("indexing").path("project").queryParam("projectId", projectId.toString()).get(Boolean.class);
    }

    boolean isIndexingProjectStarted(Long projectId)
    {
        // returns true if indexing is running
        return createResource().path("indexing").path("project").path("started").queryParam("projectId", projectId.toString()).get(Boolean.class);
    }

    public boolean isIndexConsistent()
    {
        return createResource().path("indexing").path("consistent").get(Boolean.class);
    }

    public boolean isIndexUpdatedFieldConsistent()
    {
        return createResource().path("indexing").path("consistent").path("updated").get(Boolean.class);
    }

    public void deleteIndex()
    {
        createResource().path("indexing").path("deleteIndex").post();
    }

    public void deindex(String issueKey)
    {
        createResource().path("indexing").path("deindex").queryParam("key", issueKey).get(Boolean.class);
    }

    public void indexDummyIssue(long id, long projectId, String issueType, String issueKey, String summary, String desc)
    {
        createResource().path("indexing")
                .path("indexDummyIssue")
                .queryParam("id", String.valueOf(id))
                .queryParam("projectId", String.valueOf(projectId))
                .queryParam("issueType", issueType)
                .queryParam("key", issueKey)
                .queryParam("summary", summary)
                .queryParam("description", desc)
                .get(Boolean.class);
    }

    public void reindexAll()
    {
        createResource().path("indexing").path("reindexAll").post();
    }

    public abstract class IndexingProgress
    {
		/** Timeout definition, in minutes. */
		private final long TIMEOUT_MINUTES = 5;
		/** Chunk of incremental updates. */
		private final long MILLIS_PER_CHUNK = 200;
		/** Timeout definition, in chunks. */
		private final long MAX_TIMEOUT_IN_CHUNKS = (TIMEOUT_MINUTES * DateUtils.MILLIS_PER_MINUTE) / MILLIS_PER_CHUNK;

		/** Current interval */
		private long interval = 0;
		/** Overall number of intervals waited. */
		private long overallChunks = 0;

        /**
         * Waits until the indexing is finished. This method works by polling the server in increasing intervals
		 * starting from MILLIS_PER_CHUNK.
         */
        public void waitForCompletion()
        {
            poolUntilTrue(new Supplier<Boolean>()
            {

                @Override
                @Nonnull
                public Boolean get()
                {
                    return !isIndexing();
                }
            });
        }

        public void waitForIndexingStarted()
        {
            poolUntilTrue(new Supplier<Boolean>()
            {
                @Override
                public Boolean get()
                {
                    return isIndexingStarted();
                }
            });
        }

        private void poolUntilTrue(final Supplier<Boolean> condition)
        {
            while (!condition.get())
            {
                try
                {
                    Thread.sleep( (++interval) * MILLIS_PER_CHUNK);
					overallChunks += interval;
					if(overallChunks > MAX_TIMEOUT_IN_CHUNKS)
					{
						throw new RuntimeException("Indexing timed out.");
					}
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        abstract boolean isIndexing();
        abstract boolean isIndexingStarted();
    }
}
