import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/*
 * Do you remember the times when you compiled your .java into .class file on the command line?
 * Well, try harder.
 */
public class Decipher  {

    public static void main(String[] args)
            throws IOException, XmlPullParserException, SecDispatcherException, PlexusCipherException {
        final String settingsFile = System.getProperty("user.home")+"/.m2/settings.xml";
        final String settingsSecurityFile = System.getProperty("user.home")+"/.m2/settings-security.xml";

        final SettingsXpp3Reader reader = new SettingsXpp3Reader();
        final Settings settings = reader.read(new FileInputStream(settingsFile));

        final Server atlassianM2Repo = settings.getServer("atlassian-m2-repository");
        final File securityFile = new File(settingsSecurityFile);
        final String password;

        if(securityFile.exists()) {
            final SettingsSecurity settingsSecurity = SecUtil.read(securityFile.getAbsolutePath(), true);
            final String masterPassword = settingsSecurity.getMaster();
            final DefaultPlexusCipher cipher = new DefaultPlexusCipher();
            final String plainMasterPassword = cipher.decryptDecorated(masterPassword, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION);
            password = cipher.decrypt(atlassianM2Repo.getPassword(), plainMasterPassword);
        } else {
            password = atlassianM2Repo.getPassword();
        }


        final String username = atlassianM2Repo.getUsername();
        System.out.println(username + ":" + password);


    }
}