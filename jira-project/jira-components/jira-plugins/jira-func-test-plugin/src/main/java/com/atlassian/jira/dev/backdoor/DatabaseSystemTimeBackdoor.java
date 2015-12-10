package com.atlassian.jira.dev.backdoor;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.database.DatabaseSystemTimeReaderFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@Path ("/databaseSystemTime")
public class DatabaseSystemTimeBackdoor
{
    private static final Logger log = LoggerFactory.getLogger(DatabaseSystemTimeBackdoor.class);

    private final DatabaseSystemTimeReaderFactory databaseSystemTimeReaderFactory;

    public DatabaseSystemTimeBackdoor(DatabaseSystemTimeReaderFactory databaseSystemTimeReaderFactory)
    {
        this.databaseSystemTimeReaderFactory = databaseSystemTimeReaderFactory;
    }

    @GET
    @Produces ({ MediaType.TEXT_PLAIN })
    public Response getDatabaseSystemTime()
    {
        try
        {
            long dbTime = databaseSystemTimeReaderFactory.getReader().getDatabaseSystemTimeMillis();
            return(Response.ok(String.valueOf(dbTime))).build();
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
            return(Response.serverError().entity(e.toString()).build());
        }
    }
}
