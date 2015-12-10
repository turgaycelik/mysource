package com.atlassian.jira.help;

import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>This is used in the {@code Configurator} to read the {@code help-paths.properties} file.</p>
 *
 * <p><strong>Note:</strong> This object should not be used in JIRA core as real {@code HelpUrls} are locale and plugin
 * sensitive. In JIRA just simple inject an instance of {@link com.atlassian.jira.help.HelpUrls} and use that.</p>
 *
 * @since v6.2.6
 */
public class StaticHelpUrls
{
    private static final Logger LOG = LoggerFactory.getLogger(StaticHelpUrls.class);

    public static HelpUrls getInstance()
    {
        return Holder.INSTANCE;
    }

    private static class Holder
    {
        private static final String RESOURCE = "help-paths.properties";
        private static final HelpUrls INSTANCE = loadHelpUrls();

        private static HelpUrls loadHelpUrls()
        {
            final LocalHelpUrls localHelpUrls = new DefaultLocalHelpUrls();
            final Supplier<Boolean> onDemand = Suppliers.ofInstance(Boolean.FALSE);
            final SimpleHelpUrlBuilder.Factory builder = new SimpleHelpUrlBuilder.Factory(new BuildUtilsInfoImpl());
            final HelpUrlsParser parser = new DefaultHelpUrlsParser(builder, localHelpUrls, onDemand, null, null);

            return parser.parse(readProperties());
        }

        private static Properties readProperties()
        {
            Properties properties = new Properties();
            InputStream stream = StaticHelpUrls.class.getClassLoader().getResourceAsStream(RESOURCE);
            try
            {
                if (stream != null)
                {
                    properties.load(stream);
                }
            }
            catch (IOException e)
            {
                LOG.debug("Unable to read in 'help-paths.properties.", e);
            }
            finally
            {
                IOUtils.closeQuietly(stream);
            }
            return properties;
        }
    }
}
