package com.atlassian.jira.index;

import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.fugue.Option;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Document that configures indexing of entityProperties it reads to description in format of:
 * {@code
     <key entity-key="com.atlassian.jira.thing">
         <extract path="foo.bar" type="string">
         <!--
            "path" tells you what part of the json to extract.
            "type" could be "number", "string", "text", "date" and tells you how to analyse it
         -->
        </<extract>
     </key>
     <key prefix="com.atlassian.jira.rank">
        <extract...><extract/>
     </key>
   }
 * @since v6.2
 */
@ExperimentalApi
public class IndexDocumentConfiguration
{
    private final String entityKey;
    private final List<KeyConfiguration> keyConfigurations;

    public IndexDocumentConfiguration(final String entityKey, final List<KeyConfiguration> keyConfigurations)
    {
        this.entityKey = checkNotNull(entityKey, "entityKey");
        this.keyConfigurations = ImmutableList.copyOf(checkNotNull(keyConfigurations, "keyConfigurations"));
    }

    /**
     * List of key that are defined to be scanned in this configuration
     *
     * @return list of key configurations
     */
    public List<KeyConfiguration> getKeyConfigurations()
    {
        return keyConfigurations;
    }

    /**
     * @return name of entity associated with this configuration
     */
    public String getEntityKey()
    {
        return entityKey;
    }

    public Iterable<ConfigurationElement> getConfigurationElements()
    {

        return Iterables.concat(Iterables.transform(keyConfigurations, new Function<KeyConfiguration, Iterable<ConfigurationElement>>()
        {
            @Override
            public Iterable<ConfigurationElement> apply(final KeyConfiguration keyConfiguration)
            {
                return Iterables.transform(keyConfiguration.getExtractorConfigurations(), new Function<ExtractConfiguration, ConfigurationElement>()
                {
                    @Override
                    public ConfigurationElement apply(final ExtractConfiguration extractConfiguration)
                    {
                        return new ConfigurationElement(getEntityKey(), keyConfiguration.getPropertyKey(),
                                extractConfiguration.getPath(), extractConfiguration.getType());
                    }
                });
            }
        }));
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final IndexDocumentConfiguration that = (IndexDocumentConfiguration) o;
        return entityKey.equals(that.entityKey) && keyConfigurations.equals(that.keyConfigurations);
    }

    @Override
    public int hashCode()
    {
        return 31 * entityKey.hashCode() + keyConfigurations.hashCode();
    }

    @Override
    public String toString()
    {
        return "IndexDocumentConfiguration{" +
                "entityKey='" + entityKey + '\'' +
                ", keyConfigurations=" + keyConfigurations +
                '}';
    }

    @ExperimentalApi
    public static enum Type
    {
        NUMBER,
        TEXT,
        STRING,
        DATE;

        /**
         * @param value value tha should be converted to this enum. This string will be first converted to upper case
         * @return defined option if value is valid for this enum
         */
        public static Option<Type> getValue(final String value)
        {
            try
            {
                final Type type = Type.valueOf(StringUtils.upperCase(value));
                return Option.some(type);
            }
            catch (final IllegalArgumentException ignore)
            {
                return Option.none();
            }
        }
    }

    @ExperimentalApi
    public static class KeyConfiguration
    {
        private final List<ExtractConfiguration> extractorConfigurations;
        private final String propertyKey;

        public KeyConfiguration(final String propertyKey, final List<ExtractConfiguration> extractorConfigurations)
        {
            this.propertyKey = checkNotNull(propertyKey, "propertyKey");
            this.extractorConfigurations = ImmutableList.copyOf(checkNotNull(extractorConfigurations, "extractorConfigurations"));
        }

        public List<ExtractConfiguration> getExtractorConfigurations()
        {
            return extractorConfigurations;
        }

        public String getPropertyKey()
        {
            return propertyKey;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            final KeyConfiguration that = (KeyConfiguration) o;
            return propertyKey.equals(that.propertyKey) && extractorConfigurations.equals(that.extractorConfigurations);
        }

        @Override
        public int hashCode()
        {
            return 31 * propertyKey.hashCode() + extractorConfigurations.hashCode();
        }
    }

    @ExperimentalApi
    public static class ExtractConfiguration
    {
        private final String path;
        private final Type type;

        public ExtractConfiguration(final String path, final Type type)
        {
            this.path = checkNotNull(path, "path");
            this.type = checkNotNull(type, "type");
        }

        public String getPath()
        {
            return path;
        }

        public Type getType()
        {
            return type;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final ExtractConfiguration that = (ExtractConfiguration) o;
            return path.equals(that.path) && type == that.type;

        }

        @Override
        public int hashCode()
        {
            return 31 * path.hashCode() + type.hashCode();
        }
    }

    public static class ConfigurationElement
    {
        private final String entityKey;
        private final String propertyKey;
        private final String path;
        private final Type type;

        public ConfigurationElement(final String entityKey, final String propertyKey, final String path, final Type type)
        {
            this.entityKey = entityKey;
            this.propertyKey = propertyKey;
            this.path = path;
            this.type = type;
        }

        public String getEntityKey()
        {
            return entityKey;
        }

        public String getPropertyKey()
        {
            return propertyKey;
        }

        public String getPath()
        {
            return path;
        }

        public Type getType()
        {
            return type;
        }
    }
}
