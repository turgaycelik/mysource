package com.atlassian.jira.template.velocity;

import java.util.Properties;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.util.concurrent.LazyReference;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;

/**
 * Responsible for supplying an instance of the velocity engine that's been properly initialised and ready to render
 * templates.
 *
 * @since v5.1
 */
public interface VelocityEngineFactory
{
    VelocityEngine getEngine();

    public static class Default implements VelocityEngineFactory
    {
        private static final Logger log = Logger.getLogger(VelocityEngineFactory.Default.class);

        @ClusterSafe ("Program artifacts only.")
        private final LazyReference<VelocityEngine> velocityEngine = new LazyReference<VelocityEngine>()
        {
            @Override
            protected VelocityEngine create() throws Exception
            {
                final VelocityEngine result = new VelocityEngine();
                initialise(result);
                return result;
            }
        };

        /**
         * Retrieves an instance of the Velocity Engine.
         * @return A VelocityEngine instance.
         */
        @Override
        public VelocityEngine getEngine()
        {
            return velocityEngine.get();
        }

        private void initialise(final VelocityEngine velocityEngine)
        {
            try
            {
                final Properties velocityPropertiesFile = new Properties();

                try
                {
                    velocityPropertiesFile.load(ClassLoaderUtils.getResourceAsStream("velocity.properties", getClass()));
                }
                catch (final Exception e)
                {
                    log.warn("Could not configure the Velocity Engine from the velocity.properties, manually configuring.");
                    velocityPropertiesFile.put("resource.loader", "class");
                    velocityPropertiesFile.put("class.resource.loader.description", "Velocity Classpath Resource Loader");
                    velocityPropertiesFile.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
                }

                enableDevMode(velocityPropertiesFile);

                velocityEngine.init(velocityPropertiesFile);
            }
            catch (final Exception e)
            {
                log.error("Exception initialising Velocity: " + e, e);
            }
        }

        public static void enableDevMode(Properties velocityPropertiesFile)
        {
            // override caching options if were in dev mode
            if (JiraSystemProperties.isDevMode())
            {
                // Turn off velocity caching
                velocityPropertiesFile.put("class.resource.loader.cache", "false");
                velocityPropertiesFile.put("velocimacro.library.autoreload", "true");
                velocityPropertiesFile.put("plugin.resource.loader.cache", "false");
            }
        }
    }
}
