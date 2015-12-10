package com.atlassian.jira.license;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.1
 */
public class DefaultLicenseStatusMessage implements LicenseDetails.LicenseStatusMessage
{

    public static class Builder{

        private ImmutableMap.Builder<String, String> values = new ImmutableMap.Builder<String, String>();
        private ImmutableList.Builder<String> keys = new ImmutableList.Builder<String>();

        public DefaultLicenseStatusMessage build(){
            return new DefaultLicenseStatusMessage(keys.build(), values.build());
        }

        protected Builder add(String key, String message){
            values.put(key, message);
            keys.add(key);
            return this;
        }

    }

    public static Builder builder(){
        return new Builder();
    }

    private final List<String> messagesOrder;
    private final Map<String, String> messages;

    public DefaultLicenseStatusMessage(final List<String> messagesOrder, final Map<String, String> messages) {
        this.messagesOrder = messagesOrder;
        this.messages = messages;
    }

    @Override
    public String getAllMessages(final String delimiter)
    {
        Iterable<String> values = Iterables.transform(messagesOrder, new Function<String, String>()
        {
            @Override
            public String apply(@Nullable final String input)
            {
                return messages.get(input);
            }
        });
        return Joiner.on(delimiter).join(values);
    }

    @Override
    public Map<String, String> getAllMessages()
    {
        return messages;
    }

    @Override
    public boolean hasMessageOfType(final String messageKey)
    {
        return messages.containsKey(messageKey);
    }
}