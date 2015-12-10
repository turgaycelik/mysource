package com.atlassian.jira.functest.framework.email;

import com.icegreen.greenmail.util.GreenMailUtil;
import org.apache.commons.lang.StringUtils;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A collection of handy methods for manipulating or examining mail messages (without assertions).
 *
 * @since v4.0
 */
public class EmailKit
{
    /**
     * Look through the passed messages for one addressed to the passed address.
     *
     * @param expectedAddressString the string representation of the e-mail address to look for.
     * @param messages              the messages to look through.
     * @return the matched message or null if no message could be found with the specified address.
     * @throws javax.mail.MessagingException if an error occurs while parsing the messages or addresses.
     */
    public static MimeMessage findMessageAddressedTo(String expectedAddressString, MimeMessage[] messages)
            throws MessagingException
    {
        final InternetAddress expectedAddress = new InternetAddress(expectedAddressString);
        for (MimeMessage message : messages)
        {
            final Address[] addresses = message.getRecipients(Message.RecipientType.TO);
            for (Address address : addresses)
            {
                if (expectedAddress.equals(address))
                {
                    return message;
                }
            }
        }

        return null;
    }

    /**
     * @param message the message
     * @return the body of the message
     */
    public static String getBody(Part message)
    {
        final String body = GreenMailUtil.getBody(message);
        //remove line breaks from mail encoded bodies.
        return StringUtils.replace(body, "=\r\n", "");
    }
}
