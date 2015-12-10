package com.atlassian.jira.functest.config.xml;

import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.ConfigFile;
import com.atlassian.jira.functest.config.ConfigFileWalker;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Verify that the restore XMLs for upgrade tests contain the suppresschecks: upgrade check.
 *
 * @since v6.0
 */
public final class UpgradeXmlVerifier
{

    private static final String UPGRADE = "upgrade";

    private final File root;

    public UpgradeXmlVerifier(File root)
    {
        this.root = notNull(root);
    }


    public void verify()
    {
        final VerifierWalker walker = new VerifierWalker();
        new ConfigFileWalker(root, walker).walk();
        if (!walker.badFiles.isEmpty())
        {
            throw new AssertionError("The following upgrade XMLs do not contain suppress upgrade check: " + walker.badFiles);
        }
    }


    private static final class VerifierWalker implements ConfigFileWalker.ConfigVisitor
    {
        private final List<String> badFiles = Lists.newArrayList();

        @Override
        public void visitConfig(ConfigFile file)
        {
            if (looksLikeUpgradeXml(file) && noUpgradeSuppress(file))
            {
                badFiles.add(file.getFile().getName());
            }
        }

        private boolean noUpgradeSuppress(ConfigFile file)
        {
            return CheckOptionsUtils.parseOptions(file.readConfig()).checkEnabled(Checks.UPGRADE);
        }

        private boolean looksLikeUpgradeXml(ConfigFile file)
        {
            final File xml = file.getFile();
            return xml.getName().toLowerCase().contains(UPGRADE)
                    || xml.getParentFile().getName().toLowerCase().contains(UPGRADE);
        }

        @Override
        public void visitConfigError(File file, ConfigFile.ConfigFileException e)
        {
            throw e;
        }
    }

}
