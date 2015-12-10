package com.atlassian.jira.functest.config.crowd;

import com.atlassian.jira.functest.config.CheckOptions;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.util.List;

/**
 * Check that we are using plaintext encoding and not something slow like 'atlassian-security' encoding
 *
 * @since v4.4
 */
public class PlaintextEncoderChecker implements ConfigurationCheck
{

    private static final Logger log = Logger.getLogger(PlaintextEncoderChecker.class);
    public static final String CHECK_PLAINTEXT_ENCODER = "plaintextencoder";

    @Override
    public Result checkConfiguration(JiraConfig config, CheckOptions options)
    {
        CheckResultBuilder builder = new CheckResultBuilder();
        if (options.checkEnabled(CHECK_PLAINTEXT_ENCODER))
        {
            List<Element> encryptionMethods = ConfigXmlUtils.getElementsByXpath(config.getDocument(), "/entity-engine-xml/DirectoryAttribute[@name='user_encryption_method']");

            for (Element encryptionMethod: encryptionMethods)
            {
                String currentEncryptionMethodValue = ConfigXmlUtils.getTextValue(encryptionMethod, "value");
                String fastEncryptionMethodValue = "plaintext";

                if (!fastEncryptionMethodValue.equals(currentEncryptionMethodValue))
                {
                    builder.error(String.format("DirectoryAttribute 'user_encryption_method' is not set to '%s'. You can run the ConfigFixerUpperer to fix this and also to reset users passwords to be their username in plaintext", fastEncryptionMethodValue), CHECK_PLAINTEXT_ENCODER);
                }
            }
        }

        return builder.buildResult();
    }

    @Override
    public void fixConfiguration(JiraConfig config, CheckOptions options)
    {
        List<Element> encryptionMethods = ConfigXmlUtils.getElementsByXpath(config.getDocument(),"/entity-engine-xml/DirectoryAttribute[@name='user_encryption_method']");

        for (Element encryptionMethod: encryptionMethods)
        {
            String currentEncryptionMethodValue = ConfigXmlUtils.getTextValue(encryptionMethod, "value");
            String fastEncryptionMethodValue = "plaintext";

            if (!fastEncryptionMethodValue.equals(currentEncryptionMethodValue))
            {
                log.info(String.format("Changing 'user_encryption_method' from '%s' to '%s'", currentEncryptionMethodValue, fastEncryptionMethodValue));

                ConfigXmlUtils.setAttribute(encryptionMethod, "value", fastEncryptionMethodValue);

                // now fix the passwords
                List<Element> users = ConfigXmlUtils.getElementsByXpath(config.getDocument(), "/entity-engine-xml/User");

                for (Element user: users)
                {
                    String username = ConfigXmlUtils.getTextValue(user, "userName");

                    log.info(String.format("Setting credential (password) of user '%s' to '%s'", username, username));

                    ConfigXmlUtils.setAttribute(user, "credential", username);
                }

                config.markDirty();
            }
        }
    }

}

