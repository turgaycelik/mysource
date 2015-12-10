package com.atlassian.jira.issue.index.indexers.impl;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyImpl;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.index.GenericSearchExtractorContext;
import com.atlassian.jira.index.IndexDocumentConfiguration;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.index.property.OfBizPluginIndexConfigurationManager;
import com.atlassian.jira.index.property.PluginIndexConfiguration;
import com.atlassian.jira.index.property.PluginIndexConfigurationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.LuceneUtils;

import com.google.common.collect.ImmutableList;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.atlassian.jira.matchers.LuceneDocumentMatchers.fieldableHasStringValue;
import static com.atlassian.jira.matchers.LuceneDocumentMatchers.hasStringField;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v6.2
 */
public class IssuePropertySearchExtractorTest
{
    private final GenericSearchExtractorContext<Issue> ctx = new GenericSearchExtractorContext<Issue>(new MockIssue(1, "key-1"), "issues");
    @Rule
    public TestRule initMocks = new InitMockitoMocks(this);
    private IssuePropertySearchExtractor searchExtractor;
    @Mock
    private JsonEntityPropertyManager jsonEntityPropertyManagers;
    @Mock
    private PluginIndexConfigurationManager pluginIndexConfigurationManager;
    @Mock
    private DateTimeFormatterFactory dateTimeFormatterFactory;
    @Mock
    private DateTimeFormatter dateTimeFormatter;
    @Mock
    private SearchExtractorRegistrationManager searchExtractorRegistrationManager;
    @Mock
    DoubleConverter doubleConverter;

    @Before
    public void setUp() throws Exception
    {
        Mockito.when(dateTimeFormatterFactory.formatter()).thenReturn(dateTimeFormatter);
        Mockito.when(dateTimeFormatter.withDefaultLocale()).thenReturn(dateTimeFormatter);
        Mockito.when(dateTimeFormatter.withStyle(Mockito.any(DateTimeStyle.class))).thenReturn(dateTimeFormatter);
        searchExtractor = new IssuePropertySearchExtractor(jsonEntityPropertyManagers, pluginIndexConfigurationManager, dateTimeFormatterFactory, searchExtractorRegistrationManager, doubleConverter);

    }

    @Test
    public void shouldExtractAllPropertiesWhenThereAreNoErrors()
    {
        //having
        final Document doc = new Document();
        final String entityKey = EntityPropertyType.ISSUE_PROPERTY.getDbEntityName();
        Mockito.when(pluginIndexConfigurationManager.getDocumentsForEntity(entityKey))
                .thenReturn(ImmutableList.of(
                        getPluginIndexConfig(new IndexDocumentConfiguration(entityKey, ImmutableList.of(
                                new IndexDocumentConfiguration.KeyConfiguration("bar.property",
                                        ImmutableList.of(new IndexDocumentConfiguration.ExtractConfiguration("bar1.date", IndexDocumentConfiguration.Type.DATE),
                                                new IndexDocumentConfiguration.ExtractConfiguration("bar1.text", IndexDocumentConfiguration.Type.TEXT),
                                                new IndexDocumentConfiguration.ExtractConfiguration("bar1.string", IndexDocumentConfiguration.Type.STRING),
                                                new IndexDocumentConfiguration.ExtractConfiguration("bar1.number", IndexDocumentConfiguration.Type.NUMBER),
                                                new IndexDocumentConfiguration.ExtractConfiguration("bar1.array", IndexDocumentConfiguration.Type.NUMBER)
                                        ))))),
                        getPluginIndexConfig(new IndexDocumentConfiguration(entityKey, ImmutableList.of(
                                new IndexDocumentConfiguration.KeyConfiguration("foo.property",
                                        ImmutableList.of(new IndexDocumentConfiguration.ExtractConfiguration("foo.date", IndexDocumentConfiguration.Type.DATE)
                                        )))))

                ));


        setupEntityProperty("{"
                + "\"bar1\":{"
                + "\"date\":\"2013-01-02\","
                + "\"text\":\"this is awesome\","
                + "\"string\":\"nice untokenized string\","
                + "\"array\":[1,2,3],"
                + "\"number\":\"123\","
                + "\"decoy\":\"one\","
                + "}}");

        Mockito.when(jsonEntityPropertyManagers.get(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), ctx.getEntity().getId(), "foo.property"))
                .thenReturn(getEntityProperty("{"
                        + "\"foo\":{\"date\":\"2014-01-02 12:30:12\"}}"));

        final Date date2013 = new Date(2013);
        final Date date2014 = new Date(2014);
        Mockito.when(dateTimeFormatter.parse("2013-01-02")).thenReturn(date2013);
        Mockito.when(dateTimeFormatter.parse("2014-01-02 12:30:12")).thenReturn(date2014);
        Mockito.when(doubleConverter.getStringForLucene("1")).thenReturn("1");
        Mockito.when(doubleConverter.getStringForLucene("2")).thenReturn("2");
        Mockito.when(doubleConverter.getStringForLucene("3")).thenReturn("3");
        Mockito.when(doubleConverter.getStringForLucene("123")).thenReturn("123");


        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(addedIds, Matchers.containsInAnyOrder(
                "ISSUEPROP_bar.property$bar1.date",
                "ISSUEPROP_bar.property$bar1.text",
                "ISSUEPROP_bar.property$bar1.string",
                "ISSUEPROP_bar.property$bar1.number",
                "ISSUEPROP_bar.property$bar1.array",
                "ISSUEPROP_foo.property$foo.date"
        ));
        assertThat(doc, hasStringField("ISSUEPROP_bar.property$bar1.date", LuceneUtils.dateToString(date2013)));
        assertThat(doc, hasStringField("ISSUEPROP_bar.property$bar1.text", "this is awesome"));
        assertThat(doc, hasStringField("ISSUEPROP_bar.property$bar1.string", "nice untokenized string"));
        final Fieldable[] arrayFieldables = doc.getFieldables("ISSUEPROP_bar.property$bar1.array");
        final Matcher<Iterable<Fieldable>> matcher = Matchers.containsInAnyOrder(
                fieldableHasStringValue(equalTo("1")),
                fieldableHasStringValue(equalTo("2")),
                fieldableHasStringValue(equalTo("3")));

        assertThat(ImmutableList.copyOf(arrayFieldables),matcher);
        assertThat(doc, hasStringField("ISSUEPROP_bar.property$bar1.number", "123"));
        assertThat(doc, hasStringField("ISSUEPROP_foo.property$foo.date", LuceneUtils.dateToString(date2014)));
    }

    @Test
    public void shouldOmitDateFieldsWhenCannotParse()
    {
        final Document doc = new Document();
        setupConfigAndJSONForOneField("2013-01-02", IndexDocumentConfiguration.Type.DATE);

        Mockito.when(dateTimeFormatter.parse("2013-01-02")).thenThrow(new IllegalArgumentException());

        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(addedIds, Matchers.is(Matchers.<String>empty()));
        assertThat(doc.getFields(), Matchers.is(Matchers.<Fieldable>empty()));
    }

    @Test
    public void shouldOmitNumberFieldsWhenCannotParse()
    {
        final Document doc = new Document();
        setupConfigAndJSONForOneField("ABC", IndexDocumentConfiguration.Type.NUMBER);
        Mockito.when(doubleConverter.getStringForLucene("ABC")).thenThrow(new FieldValidationException(""));

        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(addedIds, Matchers.is(Matchers.<String>empty()));
        assertThat(doc.getFields(), Matchers.is(Matchers.<Fieldable>empty()));
    }

    @Test
    public void shouldOmitIndexingWhenNoPropertiesMatchingKeyForIssue()
    {
        final Document doc = new Document();
        setupConfigAndJSONForOneField("ABC", IndexDocumentConfiguration.Type.TEXT);


        //when
        final Set<String> addedIds = searchExtractor.indexEntity(new GenericSearchExtractorContext<Issue>(new MockIssue(2, "KEY-2"), "issues"), doc);

        //then
        assertThat(addedIds, Matchers.is(Matchers.<String>empty()));
        assertThat(doc.getFields(), Matchers.is(Matchers.<Fieldable>empty()));
    }

    @Test
    public void shouldIndexIfJSONObjectIsAString()
    {
        final Document doc = new Document();
        setupConfigurationForOneType(IndexDocumentConfiguration.Type.STRING, "");
        setupEntityProperty("\"string\"");

        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(addedIds, Matchers.containsInAnyOrder(
                "ISSUEPROP_bar.property$"));
        assertThat(doc, hasStringField("ISSUEPROP_bar.property$", "string"));
    }

    @Test
    public void shouldIndexIfJSONObjectIsAnArray()
    {
        final Document doc = new Document();
        setupConfigurationForOneType(IndexDocumentConfiguration.Type.STRING, "");
        setupEntityProperty("[\"val1\",\"val2\"]");

        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(addedIds, Matchers.containsInAnyOrder(
                "ISSUEPROP_bar.property$"));
        final Fieldable[] arrayFieldables = doc.getFieldables("ISSUEPROP_bar.property$");
        final Matcher<Iterable<Fieldable>> matcher = Matchers.containsInAnyOrder(
                fieldableHasStringValue(equalTo("val1")),
                fieldableHasStringValue(equalTo("val2")));
        assertThat(ImmutableList.copyOf(arrayFieldables),
                matcher);
    }

    @Test
    public void shouldNotFailWhenThereAreNoIndexingDocumentsDefined()
    {
        final Document doc = new Document();
        Mockito.when(jsonEntityPropertyManagers.get(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), ctx.getEntity().getId(), "bar.property"))
                .thenReturn(getEntityProperty("{"
                        + "\"bar1\":{"
                        + "\"aaa\":\"bbb\","
                        + "}}"));

        Mockito.when(pluginIndexConfigurationManager.getDocumentsForEntity(Mockito.anyString())).thenReturn(Collections.<PluginIndexConfiguration>emptyList());
        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(addedIds, Matchers.is(Matchers.<String>empty()));
        assertThat(doc.getFields(), Matchers.is(Matchers.<Fieldable>empty()));
    }

    @Test
    public void shouldOmitIndexingWhenStoredPropertyIsNotValidJSON()
    {
        final Document doc = new Document();
        setupConfigAndJSONForOneField("ABC\"{", IndexDocumentConfiguration.Type.TEXT);


        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(doc.getFields(), Matchers.is(Matchers.<Fieldable>empty()));
        assertThat(addedIds, Matchers.is(Matchers.<String>empty()));
    }
    @Test
    public void shouldOmitIndexingWhenStoredPropertyIsNotValidJSONWithStringConcat()
    {
        final Document doc = new Document();
        final IndexDocumentConfiguration.Type fieldType = IndexDocumentConfiguration.Type.TEXT;
        final String fieldNameExt = fieldType.name().toLowerCase();
        setupConfigurationForOneType(fieldType, "bar1." + fieldType.name().toLowerCase());
        final String jsonString = "{"
                + "\"bar1\":{"
                + "\"" + fieldNameExt + "\":\"test\""
                + "}}\"aaa\"";
        setupEntityProperty(jsonString);


        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(doc.getFields(), Matchers.is(Matchers.<Fieldable>empty()));
        assertThat(addedIds, Matchers.is(Matchers.<String>empty()));
    }

    @Test
    public void shouldOmitIndexingWhenPathDoesNotMatch()
    {
        final Document doc = new Document();
        final String entityKey = EntityPropertyType.ISSUE_PROPERTY.getDbEntityName();
        Mockito.when(pluginIndexConfigurationManager.getDocumentsForEntity(entityKey))
                .thenReturn(ImmutableList.of(
                        getPluginIndexConfig(new IndexDocumentConfiguration(entityKey, ImmutableList.of(
                                new IndexDocumentConfiguration.KeyConfiguration("bar.property",
                                        ImmutableList.of(new IndexDocumentConfiguration.ExtractConfiguration("bar1." + "name", IndexDocumentConfiguration.Type.STRING)
                                        )))))
                ));
        Mockito.when(jsonEntityPropertyManagers.get(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), ctx.getEntity().getId(), "bar.property"))
                .thenReturn(getEntityProperty("{"
                        + "\"bar1\":{"
                        + "\"test\":\"aaa\""
                        + "}}"));

        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(doc.getFields(), Matchers.is(Matchers.<Fieldable>empty()));
        assertThat(addedIds, Matchers.is(Matchers.<String>empty()));
    }

    @Test
    public void shouldOmitIndexingWhenNoTerminalStatementAtTheEndOfPath()
    {
        final Document doc = new Document();
        final String entityKey = EntityPropertyType.ISSUE_PROPERTY.getDbEntityName();
        Mockito.when(pluginIndexConfigurationManager.getDocumentsForEntity(entityKey))
                .thenReturn(ImmutableList.of(
                        getPluginIndexConfig(new IndexDocumentConfiguration(entityKey, ImmutableList.of(
                                new IndexDocumentConfiguration.KeyConfiguration("bar.property",
                                        ImmutableList.of(new IndexDocumentConfiguration.ExtractConfiguration("bar1." + "name", IndexDocumentConfiguration.Type.STRING)
                                        )))))
                ));
        Mockito.when(jsonEntityPropertyManagers.get(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), ctx.getEntity().getId(), "bar.property"))
                .thenReturn(getEntityProperty("{"
                        + "\"bar1\":"
                        + "{"
                        + "\"name\":{"
                        + "\"test\":\"aaa\""
                        + "}"
                        + "}"
                        + "}"));

        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);
        //then
        assertThat(doc.getFields(), Matchers.is(Matchers.<Fieldable>empty()));
        assertThat(addedIds, Matchers.is(Matchers.<String>empty()));
    }

    @Test
    public void shouldAddTwoFieldsIfTheSameFieldHasTwoConfigurations()
    {
        final Document doc = new Document();
        final String entityKey = EntityPropertyType.ISSUE_PROPERTY.getDbEntityName();
        final String fieldNameExt = "someName";
        final String fieldName = "bar1." + fieldNameExt;
        Mockito.when(pluginIndexConfigurationManager.getDocumentsForEntity(entityKey))
                .thenReturn(ImmutableList.of(
                        getPluginIndexConfig(new IndexDocumentConfiguration(entityKey, ImmutableList.of(
                                new IndexDocumentConfiguration.KeyConfiguration("bar.property",
                                        ImmutableList.of(
                                                new IndexDocumentConfiguration.ExtractConfiguration(fieldName, IndexDocumentConfiguration.Type.STRING),
                                                new IndexDocumentConfiguration.ExtractConfiguration(fieldName, IndexDocumentConfiguration.Type.NUMBER)
                                        )))))
                ));
        Mockito.when(jsonEntityPropertyManagers.get(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), ctx.getEntity().getId(), "bar.property"))
                .thenReturn(getEntityProperty("{"
                        + "\"bar1\":{"
                        + "\"" + fieldNameExt + "\":\"2013\""
                        + "}}"));
        Mockito.when(doubleConverter.getStringForLucene("2013")).thenReturn("valueFor2013");
        //when
        final Set<String> addedIds = searchExtractor.indexEntity(ctx, doc);

        //then
        assertThat(addedIds, Matchers.containsInAnyOrder(
                "ISSUEPROP_bar.property$" + fieldName
        ));
        final Fieldable[] arrayFieldables = doc.getFieldables("ISSUEPROP_bar.property$" + fieldName);
        final Matcher<Iterable<Fieldable>> matcher = Matchers.containsInAnyOrder(
                fieldableHasStringValue(equalTo("2013")),
                fieldableHasStringValue(equalTo("valueFor2013"))
        );
        assertThat(ImmutableList.copyOf(arrayFieldables),
                matcher);

    }

    private PluginIndexConfiguration getPluginIndexConfig(final IndexDocumentConfiguration indexDocumentConfiguration)
    {
        return new OfBizPluginIndexConfigurationManager.PluginIndexConfigurationImpl("", "", indexDocumentConfiguration, new Timestamp(1l));
    }

    private EntityProperty getEntityProperty(final String str)
    {
        return EntityPropertyImpl.forCreate(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), 1l, "some.key", str);
    }

    private void setupConfigAndJSONForOneField(final String value, final IndexDocumentConfiguration.Type fieldType)
    {
        final String fieldNameExt = fieldType.name().toLowerCase();
        setupConfigurationForOneType(fieldType, "bar1." + fieldType.name().toLowerCase());
        final String jsonString = "{"
                + "\"bar1\":{"
                + "\"" + fieldNameExt + "\":\"" + value + "\""
                + "}}";
        setupEntityProperty(jsonString);
    }

    private void setupEntityProperty(final String jsonString)
    {
        Mockito.when(jsonEntityPropertyManagers.get(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), ctx.getEntity().getId(), "bar.property"))
                .thenReturn(getEntityProperty(jsonString));
    }

    private void setupConfigurationForOneType(final IndexDocumentConfiguration.Type fieldType, String path)
    {
        final String entityKey = EntityPropertyType.ISSUE_PROPERTY.getDbEntityName();
        Mockito.when(pluginIndexConfigurationManager.getDocumentsForEntity(entityKey))
                .thenReturn(ImmutableList.of(
                        getPluginIndexConfig(new IndexDocumentConfiguration(entityKey, ImmutableList.of(
                                new IndexDocumentConfiguration.KeyConfiguration("bar.property",
                                        ImmutableList.of(new IndexDocumentConfiguration.ExtractConfiguration(path, fieldType)
                                        )))))
                ));
    }

}
