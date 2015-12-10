package com.atlassian.jira.issue.index.indexers.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.atlassian.fugue.Option;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.jira.index.IssueSearchExtractor;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.index.property.PluginIndexConfiguration;
import com.atlassian.jira.index.property.PluginIndexConfigurationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONTokener;
import com.atlassian.query.clause.Property;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import static com.atlassian.jira.datetime.DateTimeStyle.COMPLETE;
import static com.atlassian.jira.datetime.DateTimeStyle.DATE;
import static com.atlassian.jira.datetime.DateTimeStyle.DATE_PICKER;
import static com.atlassian.jira.datetime.DateTimeStyle.DATE_TIME_PICKER;
import static com.atlassian.jira.datetime.DateTimeStyle.ISO_8601_DATE;
import static com.atlassian.jira.datetime.DateTimeStyle.ISO_8601_DATE_TIME;
import static com.atlassian.jira.datetime.DateTimeStyle.RSS_RFC822_DATE_TIME;

/**
 * Search extractor that constructs lucene document fields based on entities assigned to particular issue and their
 * configuration as defined by {@link com.atlassian.jira.index.IndexDocumentConfiguration}
 *
 * @since v6.2
 */
public class IssuePropertySearchExtractor implements IssueSearchExtractor
{
    private static final Logger LOG = org.apache.log4j.Logger.getLogger(IssuePropertySearchExtractor.class);
    private final JsonEntityPropertyManager jsonEntityPropertyManager;
    private final PluginIndexConfigurationManager entityPropertyIndexDocumentManager;
    private final DoubleConverter doubleConverter;
    private final Set<DateTimeFormatter> dateTimeFormatters;
    private final Set<DateTimeFormatter> dateOnlyFormatters;

    public IssuePropertySearchExtractor(final JsonEntityPropertyManager jsonEntityPropertyManager,
            final PluginIndexConfigurationManager entityPropertyIndexDocumentManager,
            final DateTimeFormatterFactory dateTimeFormatterFactory,
            final SearchExtractorRegistrationManager searchExtractorRegistrationManager,
            final DoubleConverter doubleConverter)
    {
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
        this.entityPropertyIndexDocumentManager = entityPropertyIndexDocumentManager;
        this.doubleConverter = doubleConverter;
        dateOnlyFormatters = ImmutableSet.of(
                dateTimeFormatterFactory.formatter().withStyle(DATE).withDefaultLocale(),
                dateTimeFormatterFactory.formatter().withStyle(DATE_PICKER).withDefaultLocale(),
                dateTimeFormatterFactory.formatter().withStyle(ISO_8601_DATE).withDefaultLocale()
        );
        dateTimeFormatters = ImmutableSet.of(
                dateTimeFormatterFactory.formatter().withStyle(COMPLETE).withDefaultLocale(),
                dateTimeFormatterFactory.formatter().withStyle(DATE_TIME_PICKER).withDefaultLocale(),
                dateTimeFormatterFactory.formatter().withStyle(ISO_8601_DATE_TIME).withDefaultLocale(),
                dateTimeFormatterFactory.formatter().withStyle(RSS_RFC822_DATE_TIME).withDefaultLocale()
        );
        searchExtractorRegistrationManager.register(this, Issue.class);
    }

    @Override
    public Set<String> indexEntity(final Context<Issue> ctx, final Document doc)
    {
        final ImmutableSet.Builder<String> filedIdsBuilder = ImmutableSet.builder();
        final Iterable<PluginIndexConfiguration> configurations = entityPropertyIndexDocumentManager.getDocumentsForEntity(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName());

        final Iterable<Iterable<Fieldable>> fieldsToAdd =
                Iterables.transform(Iterables.transform(configurations, new Function<PluginIndexConfiguration, IndexDocumentConfiguration>()
                {
                    @Override
                    public IndexDocumentConfiguration apply(final PluginIndexConfiguration input)
                    {
                        return input.getIndexDocumentConfiguration();
                    }
                }
                ), new EntityPropertyIndexDocumentFieldableFunction(ctx.getEntity(), filedIdsBuilder));

        for (final Fieldable fieldable : Iterables.concat(fieldsToAdd))
        {
            doc.add(fieldable);
        }
        return filedIdsBuilder.build();
    }

    private class EntityPropertyIndexDocumentFieldableFunction
            implements Function<IndexDocumentConfiguration, Iterable<Fieldable>>
    {

        private final Issue issue;
        private final ImmutableSet.Builder<String> filedIdsBuilder;

        public EntityPropertyIndexDocumentFieldableFunction(final Issue issue, final ImmutableSet.Builder<String> filedIdsBuilder)
        {
            this.issue = issue;
            this.filedIdsBuilder = filedIdsBuilder;
        }

        @Override
        public Iterable<Fieldable> apply(final IndexDocumentConfiguration indexDocumentConfiguration)
        {
            return Iterables.concat(Iterables.transform(indexDocumentConfiguration.getKeyConfigurations(), new KeyConfigurationIterableFunction(issue.getId(), filedIdsBuilder)));
        }
    }

    private class KeyConfigurationIterableFunction
            implements Function<IndexDocumentConfiguration.KeyConfiguration, Iterable<Fieldable>>
    {
        private final ImmutableSet.Builder<String> filedIdsBuilder;
        private final Long issueId;

        private KeyConfigurationIterableFunction(final Long issueId, final ImmutableSet.Builder<String> filedIdsBuilder)
        {
            this.issueId = issueId;
            this.filedIdsBuilder = filedIdsBuilder;
        }

        @Override
        public Iterable<Fieldable> apply(final IndexDocumentConfiguration.KeyConfiguration keyConfiguration)
        {
            final EntityProperty entityProperty = jsonEntityPropertyManager.get(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), issueId, keyConfiguration.getPropertyKey());
            if (entityProperty == null)
            {
                return Collections.emptyList();
            }
            final Option<Object> jsonEntityProperty = getJSON(entityProperty.getValue(), issueId, keyConfiguration.getPropertyKey());
            if (jsonEntityProperty.isEmpty())
            {
                return Collections.emptyList();
            }
            return Iterables.concat(Iterables.transform(
                    keyConfiguration.getExtractorConfigurations(),
                    new ExtractConfigurationIterableFunction(jsonEntityProperty.get(), filedIdsBuilder, keyConfiguration.getPropertyKey()))
            );
        }

        private Option<Object> getJSON(final String jsonString, final Long issueId, final String entityKey)
        {
            try
            {
                final JSONTokener jsonTokener = new JSONTokener(jsonString);
                final Object value = jsonTokener.nextValue();
                if (jsonTokener.more())
                {
                    //if there is something left after reading and object then we don't parse this
                    return Option.none();
                }
                return Option.some(value);
            }
            catch (final JSONException e)
            {
                //This should never happen
                final String message = MessageFormat.format("JSON stored in jsonEntityPropertyManagers is not valid for entityId='{'0'}' , entityName='{'1'}', entityKey='{'3'}'{0}",
                        issueId.toString(), EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), entityKey);
                LOG.debug(message, e);
                return Option.none();
            }
        }
    }

    private class ExtractConfigurationIterableFunction
            implements Function<IndexDocumentConfiguration.ExtractConfiguration, Iterable<Fieldable>>
    {
        private final Object jsonEntityProperty;
        private final ImmutableSet.Builder<String> filedIdsBuilder;
        private final String key;


        public ExtractConfigurationIterableFunction(final Object jsonEntityProperty, final ImmutableSet.Builder<String> filedIdsBuilder, final String key)
        {
            this.jsonEntityProperty = jsonEntityProperty;
            this.filedIdsBuilder = filedIdsBuilder;
            this.key = key;
        }

        @Override
        public Iterable<Fieldable> apply(final IndexDocumentConfiguration.ExtractConfiguration extractConfiguration)
        {

            final String path = extractConfiguration.getPath();
            final Option<Object> value = getValueForPath(jsonEntityProperty, path);
            if (value.isEmpty())
            {
                return Collections.emptyList();
            }

            final Object jsonValue = value.get();
            if (jsonValue instanceof JSONArray)
            {
                final JSONArray array = (JSONArray) jsonValue;
                final ImmutableList.Builder<Fieldable> fieldsForArray = ImmutableList.builder();
                for (int i = 0; i < array.length(); i++)
                {
                    final Object arrayElement = array.opt(i);
                    if (arrayElement!=null)
                    {
                        final Option<? extends Fieldable> fieldableOption = getValueFromJsonObject(arrayElement, path, key, extractConfiguration.getType());
                        if (fieldableOption.isDefined())
                        {
                            fieldsForArray.add(fieldableOption.get());
                        }
                    }
                }
                return fieldsForArray.build();
            }
            else
            {
                final Option<? extends Fieldable> fieldableOption = getValueFromJsonObject(jsonValue, path, key, extractConfiguration.getType());
                if (fieldableOption.isDefined())
                {
                    return ImmutableList.of(fieldableOption.get());
                }
                else
                {
                    return Collections.emptyList();
                }
            }
        }

        private Option<? extends Fieldable> getValueFromJsonObject(final Object value, final String path, final String key, final IndexDocumentConfiguration.Type type)
        {

            if (value instanceof JSONObject || value instanceof JSONArray || value == null)
            {
                return Option.none();
            }
            final String fieldName = "ISSUEPROP_" + new Property(ImmutableList.of(key), ImmutableList.of(path)).getAsPropertyString();
            String fieldValue = value.toString();
            Field.Index analyze = Field.Index.NOT_ANALYZED_NO_NORMS;
            switch (type)
            {
                case NUMBER:
                    try
                    {
                        fieldValue = doubleConverter.getStringForLucene(fieldValue);
                    }
                    catch (final FieldValidationException e)
                    {
                        LOG.debug(MessageFormat.format("Not adding field with name {0}, value {1} is invalid message {3}", fieldName, value, e.getMessage()));
                        return Option.none();
                    }
                    break;
                case DATE:
                    final Option<String> dateOption = getDateValue(fieldValue);
                    if (dateOption.isEmpty())
                    {
                        LOG.debug(MessageFormat.format("Not adding field with name {0}, value {1} cannot be parsed as date", fieldName, value));
                        return Option.none();
                    }
                    fieldValue = dateOption.get();
                    break;
                case TEXT:
                    analyze = Field.Index.ANALYZED;
                    break;
                case STRING:
                    break;

            }
            filedIdsBuilder.add(fieldName);
            return Option.some(new Field(fieldName, false, fieldValue, Field.Store.NO, analyze, Field.TermVector.NO));
        }

        private Option<String> getDateValue(final String value)
        {

            final Option<String> parsedDate = parseDateWithFormatter(value, dateTimeFormatters);
            if (parsedDate.isDefined())
            {
                return parsedDate;
            }
            return parseDateWithFormatter(value, dateOnlyFormatters);
        }

        private Option<String> parseDateWithFormatter(String value, Iterable<DateTimeFormatter> formatters)
        {
            for (final DateTimeFormatter formatter : formatters)
            {
                try
                {
                    final Date parsedDate = formatter.parse(value);
                    if (parsedDate != null)
                    {
                        return Option.some(LuceneUtils.dateToString(parsedDate));
                    }
                }
                catch (final IllegalArgumentException ignore)
                {

                }
            }
            return Option.none();
        }

        private Option<Object> getValueForPath(final Object jsonEntityProperty, final String path)
        {
            final String[] split = StringUtils.split(path, '.');
            Object value = jsonEntityProperty;
            for (final String currentKey : split)
            {
                if (value == null || !(value instanceof JSONObject))
                {
                    return Option.none();
                }
                else
                {
                    value = ((JSONObject) value).opt(currentKey);
                }
            }
            return Option.option(value);
        }
    }


}
