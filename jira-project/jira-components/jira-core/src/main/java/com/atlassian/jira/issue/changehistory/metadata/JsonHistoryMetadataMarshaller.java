package com.atlassian.jira.issue.changehistory.metadata;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JsonHistoryMetadataMarshaller implements HistoryMetadataMarshaller
{
    private final static Logger log = Logger.getLogger(JsonHistoryMetadataMarshaller.class);

    @Override
    @Nullable
    public HistoryMetadata unmarshall(@Nonnull final String input)
    {
        try
        {
            return new ObjectMapper().readValue(input, HistoryMetadata.class);
        }
        catch (IOException e)
        {
            // should only happen if the provided input is not valid JSON
            // ignore, the client should fallback to rendering the history without metadata
            log.debug("Error unmarshalling HistoryMetadata object", e);
        }
        return null;
    }

    @Override
    public String marshall(@Nonnull final HistoryMetadata historyMetadata)
    {
        try
        {
            return new ObjectMapper().writeValueAsString(historyMetadata);
        }
        catch (IOException e)
        {
            // shouldn't happen - the HistoryMetadata is a part of api, and should always be marshallable
            // rethrow, if it failed to marshall the metadata won't be persisted
            throw new RuntimeException(e);
        }
    }
}
