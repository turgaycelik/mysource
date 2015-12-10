package com.atlassian.jira.i18n;

import com.atlassian.jira.plugin.language.AppendTextTransform;
import com.atlassian.jira.plugin.language.PrependTextTransform;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.web.bean.MockI18nTranslationMode;
import com.atlassian.jira.web.bean.i18n.MockTranslationStore;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Unit test of {@link com.atlassian.jira.i18n.BackingI18n}.
 */
public class TestBackingI18n
{
    private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile("\uFEFF(.*?)\u26A1(.*?)\u26A1(.*?)\u2060");

    private static final Locale LOCALE = Locale.KOREA;
    private static final String PREFIX = "Start";
    private static final String SUFFIX = "End";

    private MockI18nTranslationMode translationMode = new MockI18nTranslationMode(false);

    @Test
    public void getLocaleReturnsConfiguredLocale()
    {
        assertThat(createBacking().getLocale(), equalTo(LOCALE));
    }

    @Test
    public void getPrefixReturnsKeyPrefixes()
    {
        final BackingI18n backing = createBacking("one", "one", "one.one", "one.one", "two", "two", "onener", "value");
        final Set<String> one = backing.getKeysForPrefix("one");

        assertThat(one, Matchers.containsInAnyOrder("one", "one.one", "onener"));
    }

    @Test
    public void getUnescapeTextReturnsTransformedText()
    {
        final BackingI18n backing = createBacking("one", "two");
        assertThat(backing.getUnescapedText("one"), equalTo(transformValue("two")));
        assertThat(backing.getUnescapedText("'one'"), equalTo(transformValue("two")));
    }

    @Test
    public void getUnescapeTextReturnsKeyWhenNoMatch()
    {
        final BackingI18n backing = createBacking("one", "one");
        assertThat(backing.getUnescapedText("what"), equalTo("what"));
        assertThat(backing.getUnescapedText("'what'"), equalTo("what"));
    }

    @Test
    public void getUnescapeTextReturnsNullWhenNullPassed()
    {
        final BackingI18n backing = createBacking("one", "one");
        assertThat(backing.getUnescapedText(null), Matchers.nullValue());
    }

    @Test
    public void getUntransformedRawTextReturnsNullWhenPassed()
    {
        final BackingI18n backing = createBacking("one", "one");
        assertThat(backing.getUntransformedRawText(null), Matchers.nullValue());
    }

    @Test
    public void getUntransformedRawTextReturnsKeyWhenNoMatch()
    {
        final BackingI18n backing = createBacking("one", "one");
        assertThat(backing.getUntransformedRawText("what"), equalTo("what"));
        assertThat(backing.getUntransformedRawText("'what'"), equalTo("what"));
    }

    @Test
    public void getUntransformedRawTextReturnsUntransformedText()
    {
        final BackingI18n backing = createBacking("one", "two");
        assertThat(backing.getUntransformedRawText("one"), equalTo("two"));
        assertThat(backing.getUntransformedRawText("'one'"), equalTo("two"));
    }

    @Test
    public void isKeyDefinedReturnsCorrectly()
    {
        final BackingI18n backing = createBacking("one", "two");
        assertThat(backing.isKeyDefined("one"), Matchers.equalTo(true));
        assertThat(backing.isKeyDefined("'one'"), Matchers.equalTo(true));
        assertThat(backing.isKeyDefined(null), Matchers.equalTo(false));
        assertThat(backing.isKeyDefined("other"), Matchers.equalTo(false));
    }

    @Test
    public void getTextMethodsTranslateCorrectly()
    {
        new GetTextTest().execute();
        new GetTextTest().addStringArguments("one").execute();
        new GetTextTest().addStringArguments("one", "two").execute();
        new GetTextTest().addStringArguments("one", "two", "three").execute();
        new GetTextTest().addObjectArguments("one", "two", 3).execute();
        new GetTextTest().addStringArguments("one", "two", "three", "four").execute();
        new GetTextTest().addObjectArguments("one", "two", "three", true).execute();
        new GetTextTest().addObjectArguments("one", "two", "three", "four", 5L).execute();
        new GetTextTest().addObjectArguments("one", "two", "three", "four", 5L, 6L).execute();
        new GetTextTest().addStringArguments("one", "two", "three", "four", "five", "six", "seven").execute();
        new GetTextTest().addObjectArguments(1, (short) 2, "three", "four", "five", "six", "seven").execute();
        new GetTextTest().addObjectArguments("one", "two", "three", "four", "five", "six", "seven", (byte) 8).execute();
        new GetTextTest().addStringArguments("one", "two", "three", "four", "five", "six", "seven", "eight", "nine").execute();

        new GetTextTest().addObjectArgument(new Object[] { 1, 2 }).execute();
        new GetTextTest().addObjectArgument(ImmutableList.of(1, 2)).execute();
        new GetTextTest().addObjectArgument(null).execute();
        new GetTextTest().addObjectArgument(1).execute();
    }

    @Test
    public void getResourceBundleReturnsBundleThatWrapsBean()
    {
        final BackingI18n backing = createBacking("one", "oneValue", "two", "twoValue");
        final ResourceBundle bundle = backing.getResourceBundle();

        final Set<String> actualKeys = Sets.newHashSet(Iterators.forEnumeration(bundle.getKeys()));

        assertThat(actualKeys, containsInAnyOrder("one", "two"));
        assertThat(bundle.getString("one"), equalTo("oneValue"));
        assertThat(bundle.getString("two"), equalTo("twoValue"));
    }

    private BackingI18n createBacking(String...pairs)
    {
        return createBacking(true, pairs);
    }

    private BackingI18n createBacking(boolean addTransforms, String...pairs)
    {
        final Iterable<? extends TranslationTransform> list;
        if (addTransforms)
        {
            list = ImmutableList.of(new PrependTextTransform(PREFIX), new AppendTextTransform(SUFFIX));
        }
        else
        {
            list = Collections.emptyList();
        }
        return new BackingI18n(LOCALE, translationMode, list, new MockTranslationStore(pairs));
    }

    private String transformValue(String value)
    {
        return PREFIX + value + SUFFIX;
    }

    private class GetTextTest
    {
        private GetTextArgumentList argumentList = new GetTextArgumentList();

        private GetTextTest addObjectArgument(Object object)
        {
            argumentList.addObjectArgument(object);
            return this;
        }

        private GetTextTest addObjectArguments(Object...arguments)
        {
            argumentList.addObjectArguments(arguments);
            return this;
        }

        private GetTextTest addStringArguments(String...arguments)
        {
            argumentList.addStringArguments(arguments);
            return this;
        }

        private void execute()
        {
            final String goodKey = "key";
            final String badKey = "badKey";
            final String expectedValue = transformValue(argumentList.getTranslation());
            final String messageFormat = argumentList.getTemplate();
            final Class<?>[] argumentTypes = argumentList.getArgumentTypes();
            final Object[] arguments = argumentList.getArguments();

            final BackingI18n backing = createBacking(goodKey, messageFormat);

            try
            {
                final Method getText = backing.getClass().getMethod("getText", argumentTypes);
                translationMode.setTranslationMode(false);

                //Test what happens for a good key.
                arguments[0] = goodKey;
                String actualValue = (String)getText.invoke(backing, arguments);
                assertThat(trParse(actualValue), equalTo(tr(expectedValue)));

                //Test what happens for a quoted key style key.
                arguments[0] = String.format("'%s'", goodKey);
                actualValue = (String)getText.invoke(backing, arguments);
                assertThat(trParse(actualValue), equalTo(tr(expectedValue)));

                //Test what happens for a bad key
                arguments[0] = badKey;
                actualValue = (String)getText.invoke(backing, arguments);
                assertThat(trParse(actualValue), equalTo(tr(badKey)));

                //Test what happens for a null key
                arguments[0] = null;
                actualValue = (String)getText.invoke(backing, arguments);
                assertThat(actualValue, nullValue());

                //Test what happens when heighlight mode turned on.
                translationMode.setTranslationMode(true);

                //Check simple translation highlighted correctly.
                final String expectedRaw = transformValue(messageFormat);
                arguments[0] = goodKey;
                actualValue = (String)getText.invoke(backing, arguments);
                assertThat(trParse(actualValue), equalTo(tr(expectedValue, goodKey, expectedRaw)));

                //Check quoted translation highlighted correctly.
                arguments[0] = String.format("'%s'", goodKey);
                actualValue = (String)getText.invoke(backing, arguments);
                assertThat(trParse(actualValue), equalTo(tr(expectedValue, goodKey, expectedRaw)));

                //Check bad translation highlighted correctly.
                arguments[0] = badKey;
                actualValue = (String)getText.invoke(backing, arguments);
                assertThat(trParse(actualValue), equalTo(tr(badKey, badKey, badKey)));
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static class GetTextArgumentList
    {
        private List<GetTextArgument> arguments = Lists.newArrayList();

        private GetTextArgumentList addObjectArgument(Object object)
        {
            arguments.add(new GetTextArgument(Object.class, object));
            return this;
        }

        private GetTextArgumentList addObjectArguments(Object...arguments)
        {
            for (Object object : arguments)
            {
                addObjectArgument(object);
            }
            return this;
        }

        private GetTextArgumentList addStringArguments(String...arguments)
        {
            for (String argument : arguments)
            {
                this.arguments.add(new GetTextArgument(String.class, argument));
            }
            return this;
        }

        private Class<?>[] getArgumentTypes()
        {
            Class<?>[] result = new Class<?>[this.arguments.size() + 1];
            int pos = 0;
            result[pos++] = String.class;

            for (GetTextArgument argument : arguments)
            {
                result[pos++] = argument.getType();
            }
            return result;
        }

        private Object[] getArguments()
        {
            Object[] result = new Object[this.arguments.size() + 1];
            int pos = 1;
            for (GetTextArgument argument : arguments)
            {
                result[pos++] = argument.getValue();
            }
            return result;
        }

        private String getTemplate()
        {
            StringBuilder template = new StringBuilder();
            int count = 0;
            for (GetTextArgument argument : arguments)
            {
                final int size = Iterables.size(argument);
                for (int pos = 0; pos < size; pos++)
                {
                    template.append('{').append(count++).append("}");
                }
            }
            return template.toString();
        }

        private String getTranslation()
        {
            StringBuilder template = new StringBuilder();
            for (GetTextArgument argument : arguments)
            {
                for (Object o : argument)
                {
                    template.append(o);
                }
            }
            return template.toString();
        }

        private static class GetTextArgument implements Iterable<Object>
        {
            private final Class<?> type;
            private final Object value;

            private <T> GetTextArgument(final Class<T> type, final T value)
            {
                this.type = type;
                this.value = value;
            }

            public Class<?> getType()
            {
                return type;
            }

            public Object getValue()
            {
                return value;
            }

            @Override
            public Iterator<Object> iterator()
            {
                if (value instanceof Object[])
                {
                    return Arrays.asList((Object[])value).iterator();
                }
                else if (value instanceof Iterable<?>)
                {
                    return Iterables.transform((Iterable<?>)value, Functions.identity()).iterator();
                }
                else if (value == null)
                {
                    return Collections.emptyList().iterator();
                }
                else
                {
                    return Collections.singleton(value).iterator();
                }
            }
        }
    }

    private static Translation tr(String message)
    {
        return new Translation(message);
    }

    private static Translation tr(String message, String key, String raw)
    {
        if (message.equals(raw))
        {
            return new Translation(message, key, null);
        }
        else
        {
            return new Translation(message, key, raw);
        }
    }

    private static Translation trParse(String message)
    {
        final Matcher matcher = HIGHLIGHT_PATTERN.matcher(message);
        if (matcher.matches())
        {
            return new Translation(stripToNull(matcher.group(1)), stripToNull(matcher.group(2)), stripToNull(matcher.group(3)));
        }
        else
        {
            return new Translation(message);
        }

    }

    private static class Translation
    {
        private final String message;
        private final String key;
        private final String raw;

        private Translation(final String message)
        {
            this(message, null, null);
        }

        private Translation(final String message, final String key, final String raw)
        {
            this.message = message;
            this.key = key;
            this.raw = raw;
        }

        public String getMessage()
        {
            return message;
        }

        public String getKey()
        {
            return key;
        }

        public String getRaw()
        {
            return raw;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final Translation that = (Translation) o;

            if (key != null ? !key.equals(that.key) : that.key != null) { return false; }
            if (message != null ? !message.equals(that.message) : that.message != null) { return false; }
            if (raw != null ? !raw.equals(that.raw) : that.raw != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = message != null ? message.hashCode() : 0;
            result = 31 * result + (key != null ? key.hashCode() : 0);
            result = 31 * result + (raw != null ? raw.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("message", message)
                    .append("key", key)
                    .append("raw", raw)
                    .toString();
        }
    }
}
