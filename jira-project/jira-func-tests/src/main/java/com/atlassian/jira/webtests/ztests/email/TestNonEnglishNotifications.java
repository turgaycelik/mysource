package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import org.hamcrest.core.StringContains;
import org.joda.time.DateTime;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

/**
 * @since v3.13.4
 */
// introduced for some rudimentary testing around JRA-16611
// passing now
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestNonEnglishNotifications extends EmailFuncTestCase
{
    public void testNonEnglishNotifications() throws InterruptedException, MessagingException, IOException
    {
        final String randomPrefix = "[" + UUID.randomUUID().toString() + "]";

        // must be English to configure stuff
        navigation.login(ADMIN_USERNAME);
        administration.restoreData("TestTranslatedNotifications.xml");
        configureAndStartSmtpServer(DEFAULT_FROM_ADDRESS, randomPrefix);

        // fred is french
        navigation.login(FRED_USERNAME);
        tester.gotoPage("/secure/ViewSubscriptions.jspa?filterId=10000");
        tester.clickLinkWithText("Ex\u00E9cuter maintenant");

        // must be English to flush mail queue
        navigation.login(ADMIN_USERNAME);
        flushMailQueueAndWait(1);
        MimeMessage[] messages = mailService.getReceivedMessages();
        assertThat("There should be at least one notification sent", messages.length, greaterThanOrEqualTo(1));

        for(MimeMessage message : messages)
        {
            if (message.getSubject().contains(randomPrefix))
            {
                final String body = getTextContentFromMail(message);

                // this is a poor man's validation that we translated stuff properly into French.
                // ideally we would use a TableLocator and compare that against a bunch of XPath assertions
                // but none of the works for the HTML email we're dealing with here.
                assertThat(body, StringContains.containsString("Non attribu&eacute;e"));
                assertThat(body, StringContains.containsString("Non r&eacute;solu"));
                assertThat(body, StringContains.containsString("17/mars/09"));

                return;
            }
        }

        fail("No matching message was found!");
    }

    private String getTextContentFromMail(final MimeMessage message) throws IOException, MessagingException
    {
        final Object content = message.getContent();
        if(content instanceof MimeMultipart) {
            final MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0 ; i < multipart.getCount() ; i++) {
                final BodyPart part = multipart.getBodyPart(i);
                if(part.isMimeType("text/html") || part.isMimeType("text/plain"))
                    return part.getContent().toString();
            }
        }
        return content.toString();
    }
}
