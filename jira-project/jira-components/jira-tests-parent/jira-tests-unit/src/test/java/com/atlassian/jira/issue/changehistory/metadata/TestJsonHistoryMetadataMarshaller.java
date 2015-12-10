package com.atlassian.jira.issue.changehistory.metadata;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class TestJsonHistoryMetadataMarshaller
{
    private static final URL JSON_SAMPLE = Resources.getResource(TestJsonHistoryMetadataMarshaller.class, "historyMetadata.json");
    private static final String MINIMAL_METADATA_JSON = "{\"type\":\"minimal\", \"extraData\": {}}";
    private JsonHistoryMetadataMarshaller metadataMarshaller = new JsonHistoryMetadataMarshaller();

    @Test
    public void testUnmarshall() throws Exception
    {
        // having
        final String input = Resources.toString(JSON_SAMPLE, Charsets.UTF_8);

        // when
        final HistoryMetadata unmarshalled = metadataMarshaller.unmarshall(input);

        // then
        assertThat(unmarshalled, equalTo(createTestMetadata()));
    }

    @Test
    public void testUnmarshall_minimal() throws Exception
    {
        // having
        String input = MINIMAL_METADATA_JSON;

        // when
        final HistoryMetadata result = metadataMarshaller.unmarshall(input);

        // then
        assertThat(result, equalTo(HistoryMetadata.builder("minimal").build()));
    }

    @Test
    public void testMarshall() throws Exception
    {
        // having
        HistoryMetadata metadata = createTestMetadata();

        // when
        final String result = metadataMarshaller.marshall(metadata);

        // then
        final String expected = Resources.toString(JSON_SAMPLE, Charsets.UTF_8);
        assertThat(new JSONObject(expected), equalTo(new JSONObject(result)));
    }

    @Test
    public void testMarshall_minimal() throws Exception
    {
        // having
        HistoryMetadata metadata = HistoryMetadata.builder("minimal").build();

        // when
        final String result = metadataMarshaller.marshall(metadata);

        // then
        assertThat(new JSONObject(MINIMAL_METADATA_JSON), equalTo(new JSONObject(result)));
    }

    @Test
    public void testUnmarshall_invalidJson() throws Exception
    {
        // having
        final String notJson = "fafkulce";
        final String wrongType = "{\"generator\": 14}";

        // when
        final HistoryMetadata unmarshalledNotJson = metadataMarshaller.unmarshall(notJson);
        final HistoryMetadata unmarshalledWrongType = metadataMarshaller.unmarshall(wrongType);

        // then
        assertThat(unmarshalledNotJson, nullValue());
        assertThat(unmarshalledWrongType, nullValue());
    }

    @Test
    public void testUnmarshall_unrelatedValidJson() throws Exception
    {
        // when
        final HistoryMetadata result = metadataMarshaller.unmarshall("{\"and now\": \"for something completely different\"}");

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getType(), nullValue());
    }

    @Test
    public void testUnmarshall_unrelatedValidParticipantJson() throws Exception
    {
        // when
        final HistoryMetadata result = metadataMarshaller.unmarshall("{\"cause\": {\"\" : \"for something completely different\"}}");

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getCause(), not(nullValue()));
        assertThat(result.getCause().getId(), nullValue());
    }

    private HistoryMetadata createTestMetadata() throws JSONException
    {
        return HistoryMetadata.builder("marshallingTest")
                .description("metadata description")
                .descriptionKey("viewissue.changehistory.automatictransition")
                .activityDescription("activity description")
                .activityDescriptionKey("viewissue.changehistory.automatictransitionactivity")
                .actor(createParticipant("actor"))
                .cause(createParticipant("\u03ba\u1f79\u03c3\u03bc\u03b5"))
                .generator(createParticipant("generator"))
                .extraData("extra", "data")
                .extraData(ImmutableMap.of("foo", "Za\u017c\u00f3\u0142\u0107 g\u0119\u015bl\u0105 ja\u017a\u0144"))
                .build();
    }

    private HistoryMetadataParticipant createParticipant(String id)
    {
        return HistoryMetadataParticipant.builder(id + "-participant", id + "-participant-type")
                .avatarUrl("http://localhost/" + id + ".gif")
                .displayName(id + "'s Participant Display Name")
                .displayNameKey("i18.key." + id)
                .url("https://localhost/profile/" + id)
                .build();
    }
}
