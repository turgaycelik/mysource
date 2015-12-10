package com.atlassian.jira.cache;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;

/**
 * Cache compactor for EHCache
 *
 * @since v6.3
 */
public class EhCacheCompactor implements CacheCompactor, Startable, JobRunner
{
    private static final Logger LOG = LoggerFactory.getLogger(EhCacheCompactor.class);
    private static final long FIVE_MINUTES = TimeUnit.MINUTES.toMillis(5);
    private static final JobRunnerKey JOB_RUNNER_KEY = JobRunnerKey.of(EhCacheCompactor.class.getName());
    private static final JobId JOB_ID = JobId.of(EhCacheCompactor.class.getName());

    private final CacheManager cacheManager;

    public EhCacheCompactor(final CacheManager cacheManager)
    {
        this.cacheManager = cacheManager;
    }

    @Override
    public CacheCompactionResult purgeExpiredCacheEntries()
    {
        int cacheCount = 0;
        int totalEntriesCount = 0;
        int purgedEntriesCount = 0;
        for (String name : cacheManager.getCacheNames())
        {
            try
            {
                Cache cache = cacheManager.getCache(name);
                if (cache != null && cache.getStatus().equals(Status.STATUS_ALIVE))
                {
                    int before = cache.getSize();

                    cache.evictExpiredElements();

                    int after = cache.getSize();
                    cacheCount++;
                    totalEntriesCount += before;
                    purgedEntriesCount += (before - after);
                }
            }
            catch (Exception e)
            {
                LOG.warn("Unable to purge expired cache entries for cahe '" + name + "'", e);
            }
        }
        return new CacheCompactionResult(cacheCount, totalEntriesCount, purgedEntriesCount);

    }

    @Override
    public void start() throws Exception
    {
        SchedulerService scheduler = ComponentAccessor.getComponent(SchedulerService.class);
        scheduler.registerJobRunner(JOB_RUNNER_KEY, this);

        final JobConfig jobConfig = JobConfig.forJobRunnerKey(JOB_RUNNER_KEY)
                .withRunMode(RunMode.RUN_LOCALLY)
                .withSchedule(Schedule.forInterval(FIVE_MINUTES, null));

        scheduler.scheduleJob(JOB_ID, jobConfig);
    }

    @Nullable
    @Override
    public JobRunnerResponse runJob(final JobRunnerRequest request)
    {
        I18nHelper i18n = ComponentAccessor.getI18nHelperFactory().getInstance((ApplicationUser) null);
        try
        {
            CacheCompactionResult result = purgeExpiredCacheEntries();
            String message = i18n.getText("admin.service.cachecompactor.result",
                    result.getCacheCount(), result.getTotalEntriesCount(), result.getPurgedEntriesCount());
            return JobRunnerResponse.success(message);
        }
        catch (Exception e)
        {
            return JobRunnerResponse.failed(e);
        }
    }
}
