package com.atlassian.jira.webtests.ztests.email;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import com.google.common.collect.Lists;
import com.icegreen.greenmail.util.GreenMailUtil;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestMailImageAttachments extends EmailFuncTestCase
{

    public static final Pattern IMG_SRC_WITH_CID = Pattern.compile("<img.*?src=\"cid:(\\S+)\"");

    public void testEachImageAttachmentIsVisible() throws Exception
    {
        administration.restoreData("TestIssueNotifications.xml");
        configureAndStartSmtpServerWithNotify();

        final String issueId = "COW-1";
        navigation.issue().viewIssue(issueId);
        tester.clickLink("delete-issue");
        tester.submit("Delete");

        //there should 2 notifications, because of the issue's security level
        flushMailQueueAndWait(2);
        final MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(2, mimeMessages.length);

        for (MimeMessage mimeMessage : mimeMessages)
        {
            assertEachCidAttachmentIsUsedInMailMessage(mimeMessage);
        }
    }

    public void testMentionsEmails() throws Exception
    {
        administration.restoreData("TestIssueNotifications.xml");
        configureAndStartSmtpServerWithNotify();

        backdoor.usersAndGroups().addUser("user10");

        final String issueId = "COW-1";
        navigation.issue().addComment(issueId, "Some Comment [~user10]");

        flushMailQueueAndWait(3);
        final MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertThat(mimeMessages.length, equalTo(3));

        for (MimeMessage message : mimeMessages) {
            assertEachImageWithCidHasCorrespondingAttachment(message);
        }
    }

    private void assertEachCidAttachmentIsUsedInMailMessage(MimeMessage mimeMessage)
            throws IOException, MessagingException
    {
        Object content = mimeMessage.getContent();
        assertThat(content, is(instanceOf(Multipart.class)));

        Multipart multipart = (Multipart) content;
        final Collection<String> contentIds = getContentIds(multipart);
        for (String contentId : contentIds)
        {
            assertEmailBodyContains(mimeMessage, contentId);
        }
    }

    private Collection<String> getContentIds(Multipart multipart) throws MessagingException
    {
        Collection<String> result = Lists.newArrayList();
        for (int i = 0 ; i < multipart.getCount() ; i++) {
            final BodyPart bodyPart = multipart.getBodyPart(i);
            final String[] headers = bodyPart.getHeader("Content-ID");
            if(headers == null) {
                continue;
            }
            assertThat("There should be only one 'Content-ID' header in BodyPart", headers.length, equalTo(1));
            final String cidHeader = headers[0];

            //Content Id is surrounded with '<' and '>'
            result.add(cidHeader.substring(1, cidHeader.length() - 1));
        }
        return result;
    }

    private void assertEachImageWithCidHasCorrespondingAttachment(MimeMessage message)
            throws MessagingException, IOException
    {
        Object content = message.getContent();
        assertThat(content, is(instanceOf(Multipart.class)));

        Multipart multipart = (Multipart) content;
        final String[] cidsFromMessage = getCidsFromMessage(GreenMailUtil.getBody(message));
        final Collection<String> contentIds = getContentIds(multipart);
        assertThat(contentIds, containsInAnyOrder(cidsFromMessage));
    }

    private String[] getCidsFromMessage(String messageBody) {
        final Matcher matcher = IMG_SRC_WITH_CID.matcher(messageBody);
        final List<String> result = Lists.newArrayList();
        while(matcher.find()) {
            result.add(matcher.group(1));
        }
        return result.toArray(new String[result.size()]);
    }
}
