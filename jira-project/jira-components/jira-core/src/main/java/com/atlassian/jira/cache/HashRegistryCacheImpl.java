package com.atlassian.jira.cache;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.modzdetector.Modifications;
import com.atlassian.modzdetector.ModzDetector;
import com.atlassian.modzdetector.ModzRegistryException;
import com.atlassian.modzdetector.ResourceAccessor;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Soft-referenced cache of the expensive-to-generate ModzDetector hash registry.
 * @since v3.13
 */
public class HashRegistryCacheImpl implements HashRegistryCache
{
    private final ModzDetector detector;
    private SoftReference<Modifications> ref;

    public HashRegistryCacheImpl()
    {
        this(new ModzDetector(new ResourceAccessor()
        {
            @Override
            public InputStream getResourceFromClasspath(String resourceName)
            {
                if (resourceName.charAt(0) != '/')
                {
                    resourceName = "/" + resourceName;
                }
                return getResourceByPath("/WEB-INF/classes" + resourceName);
            }

            public InputStream getResourceByPath(String resourceName)
            {
                // mandatory prefix slash as per javadoc on servletContext.getResourceAsStream() and friends
                if (resourceName.charAt(0) != '/')
                {
                    resourceName = "/" + resourceName;
                }
                return ServletContextProvider.getServletContext().getResourceAsStream(resourceName);
            }
        }), new SoftReference<Modifications>(null));
    }

    HashRegistryCacheImpl(final ModzDetector detector, final SoftReference<Modifications> r)
    {
        this.detector = detector;
        ref = r;
    }

    @ClusterSafe("This is purely a local concern.")
    public synchronized Modifications getModifications() throws ModzRegistryException
    {
        Modifications mods;

        if ((mods = ref.get()) == null)
        {
            mods = detector.getModifiedFiles();
            ref = new SoftReference<Modifications>(mods);
        }
        return mods;
    }
}
