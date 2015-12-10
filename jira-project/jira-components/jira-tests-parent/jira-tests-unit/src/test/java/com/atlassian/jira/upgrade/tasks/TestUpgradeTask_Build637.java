package com.atlassian.jira.upgrade.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.bc.admin.ApplicationPropertiesService;
import com.atlassian.jira.bc.admin.ApplicationProperty;
import com.atlassian.jira.bc.admin.ApplicationPropertyMetadata;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.validation.Success;
import com.atlassian.validation.Validated;

import org.junit.Assert;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * Unit test for {@link UpgradeTask_Build637}.
 *
 * @since v4.4
 */
public class TestUpgradeTask_Build637
{

    @Test
    public void testUpgrade() throws Exception
    {
        ApplicationProperty xsrf = createProperty("jira.table.cols.subtasks", "foo");
        ApplicationProperty projPattern = createProperty("jira.projectkey.pattern", ".*");

        ApplicationPropertiesService service = createMock(ApplicationPropertiesService.class);
        expect(service.getApplicationProperty("jira.table.cols.subtasks")).andReturn(xsrf);
        expect(service.getApplicationProperty("jira.projectkey.pattern")).andReturn(projPattern);
        expect(service.getApplicationProperty("somekey"))
                .andReturn(createProperty("somekey", "somevalue"));
        expect(service.getApplicationProperty("extra.prop"))
                .andReturn(createProperty("extra.prop", "extra.value.default"));
        expect(service.getApplicationProperty("extra.prop2"))
                .andReturn(createProperty("extra.prop2", "extra.value2.default"));
        expect(service.getApplicationProperty("jira.home")).andReturn(createProperty("jira.home", "/opt/wherever/"));
        expect(service.setApplicationProperty("jira.table.cols.subtasks", "definitely"))
                .andReturn(createValidatedProperty("jira.table.cols.subtasks", "definitely"));
        expect(service.setApplicationProperty("jira.projectkey.pattern", "[A-Z]{3,}"))
                .andReturn(createValidatedProperty("jira.projectkey.pattern", "[A-Z]{3,}"));
        replay(service);

        Properties p = new Properties();

        p.setProperty("jira.table.cols.subtasks", "definitely");
        p.setProperty("jira.projectkey.pattern", "[A-Z]{3,}");
        // these are destined for the overlay file
        p.setProperty("extra.prop", "extra.value.new");
        p.setProperty("extra.prop2", "extra.value2.new");
        // this is left as default
        p.setProperty("somekey", "somevalue");

        // this is not migratable and not for the overlay file
        p.setProperty("jira.home", "D:/some/crazy path");

        final ByteArrayInputStream propsIs = toInputStream(p);

        JiraHome jiraHome = createMock(JiraHome.class);
        expect(jiraHome.getHomePath()).andReturn("D:/some/crazy path");

        final AtomicBoolean refreshed = new AtomicBoolean(false);

        ApplicationPropertiesStore store = new ApplicationPropertiesStore(null, null)
        {
            public void refresh(){
                refreshed.set(true);
            }
        };


        UpgradeTask_Build637 task = new UpgradeTask_Build637(service, store, jiraHome)
        {
            @Override
            InputStream getLegacyPropertiesStream()
            {
                return propsIs;
            }

            @Override
            void handleFailure(String key, String property, Exception e)
            {
                super.handleFailure(key, property, e);
                throw new RuntimeException("exploding on failure in test", e);
            }

            @Override
            void writeOverlayFile(Properties overlayProps) throws IOException
            {
                Assert.assertTrue(overlayProps.getProperty("extra.prop").equals("extra.value.new"));
                Assert.assertTrue(overlayProps.getProperty("extra.prop2").equals("extra.value2.new"));
                Assert.assertNull("jira home should not be put in the overlay file in the home directory, duh",
                        overlayProps.getProperty("jira.home"));
                Assert.assertEquals(2, overlayProps.size());
            }
        };

        task.doUpgrade(false);

        Assert.assertTrue("Properties have not been reloaded!", refreshed.get());

        verify(service);
    }

    static ApplicationProperty createProperty(String key, String value) {
        return createValidatedProperty(key, value).getValue();
    }

    static Validated<ApplicationProperty> createValidatedProperty(String key, String value)
    {
        ApplicationPropertyMetadata md = new ApplicationPropertyMetadata.Builder()
                .key(key)
                .type("string")
                .defaultValue(value)
                .validatorName(null)
                .sysAdminEditable(true)
                .requiresRestart(false)
                .name(key + " name")
                .desc(key + " desc")
                .build();
        ApplicationProperty tApplicationProperty = new ApplicationProperty(md, value);
        return new Validated<ApplicationProperty>(new Success(value), tApplicationProperty);
    }

    private ByteArrayInputStream toInputStream(Properties p) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        p.store(byteArrayOutputStream, "yo mama");
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

}
