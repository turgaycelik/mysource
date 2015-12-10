package com.atlassian.jira.web.monitor.dump;

import com.sun.management.HotSpotDiagnosticMXBean;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;

import com.atlassian.jira.cluster.ClusterSafe;

/**
 * Dumps heaps. Don't use it at home. If you have to, then make sure you're running on Hotspot
 *
 * @since v6.0
 */
public class HeapDumper
{

    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

    private static volatile HotSpotDiagnosticMXBean hotspotMBean;

    /**
     * Dump heap to a given file.
     *
     *
     * @param fileName file name for the dump - will be relative to working directory
     * @param live if <code>true</code>, only live (reachable) objects will be included in the dump
     */
    public static void dumpHeap(String fileName, boolean live)
    {
        initHotspotMBean();
        try
        {
            hotspotMBean.dumpHeap(fileName, live);
        }
        catch (RuntimeException re)
        {
            throw re;
        }
        catch (Exception exp)
        {
            throw new RuntimeException(exp);
        }
    }

    @ClusterSafe ("Local")
    private static void initHotspotMBean()
    {
        if (hotspotMBean == null)
        {
            synchronized (HeapDumper.class)
            {
                if (hotspotMBean == null)
                {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }

    private static HotSpotDiagnosticMXBean getHotspotMBean()
    {
        try
        {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            return ManagementFactory.newPlatformMXBeanProxy(server,
                            HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
        }
        catch (RuntimeException re)
        {
            throw re;
        }
        catch (Exception exp)
        {
            throw new RuntimeException(exp);
        }
    }
}
