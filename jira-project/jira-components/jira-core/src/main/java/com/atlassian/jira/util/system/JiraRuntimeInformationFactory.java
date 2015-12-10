package com.atlassian.jira.util.system;

import com.atlassian.jdk.utilities.runtimeinformation.MemoryInformation;
import com.atlassian.jdk.utilities.runtimeinformation.RuntimeInformation;
import com.atlassian.jdk.utilities.runtimeinformation.RuntimeInformationFactory;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import org.apache.log4j.Logger;

import java.util.List;

import static java.lang.Math.max;

/**
 * Factory for getting RuntimeInformation that supports reporting in megabytes rather than just bytes.
 *
 * @since v4.0
 */
public class JiraRuntimeInformationFactory
{
    private static final Logger log = Logger.getLogger(JiraRuntimeInformationFactory.class);

    private static final int MEGABYTE = 1024 * 1024;

    public static RuntimeInformation getRuntimeInformation()
    {
        return RuntimeInformationFactory.getRuntimeInformation();
    }

    public static RuntimeInformation getRuntimeInformationInMegabytes()
    {
        return new DecoratedRuntimeInformation(getRuntimeInformation());
    }

    static class DecoratedRuntimeInformation implements RuntimeInformation
    {
        private final RuntimeInformation info;

        public DecoratedRuntimeInformation(final RuntimeInformation info)
        {
            this.info = info;
        }

        public long getTotalHeapMemory()
        {
            return info.getTotalHeapMemory();
        }

        public long getTotalHeapMemoryUsed()
        {
            return info.getTotalHeapMemoryUsed();
        }

        public String getJvmInputArguments()
        {
            return info.getJvmInputArguments();
        }

        public List<MemoryInformation> getMemoryPoolInformation()
        {
            return CollectionUtil.transform(info.getMemoryPoolInformation(), new Function<MemoryInformation, MemoryInformation>()
            {
                public MemoryInformation get(final MemoryInformation input)
                {
                    return new MemoryInformationInMegabytes(input);
                }
            });
        }

        public long getTotalPermGenMemory()
        {
            return info.getTotalPermGenMemory();
        }

        public long getTotalPermGenMemoryUsed()
        {
            return info.getTotalPermGenMemoryUsed();
        }

        public long getTotalNonHeapMemory()
        {
            return info.getTotalNonHeapMemory();
        }

        public long getTotalNonHeapMemoryUsed()
        {
            return info.getTotalNonHeapMemoryUsed();
        }
    }

    static class MemoryInformationInMegabytes implements MemoryInformation
    {
        private final MemoryInformation info;

        public MemoryInformationInMegabytes(final MemoryInformation info)
        {
            this.info = info;
        }

        public String getName()
        {
            return info.getName();
        }

        public long getTotal()
        {
            return max(getUsed(), inMegabytes(info.getTotal()));
        }

        public long getUsed()
        {
            // This fails on some IBM JDKs due to a bug in the MemoryInfoBean refer Jira issue JRA-19389
            try
            {
                return inMegabytes(info.getUsed());
            }
            catch (RuntimeException e)
            {
                log.warn("Memory pool info returned by the java runtime is invalid for pool " + this.getName());
                log.debug(e.getMessage(), e);
                return -1;
            }
        }

        public long getFree()
        {
            return getTotal() - getUsed();
        }
    }

    static long inMegabytes(final long bytes)
    {
        return bytes / MEGABYTE;
    }
}
