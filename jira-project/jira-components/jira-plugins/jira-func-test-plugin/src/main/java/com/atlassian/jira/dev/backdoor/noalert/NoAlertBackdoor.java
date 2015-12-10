package com.atlassian.jira.dev.backdoor.noalert;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * @since v6.0
 */
@AnonymousAllowed
@Path ("/noalert")
public class NoAlertBackdoor
{
    private static final Logger log = LoggerFactory.getLogger(NoAlertBackdoor.class);
    private final NoAlertMode noAlertMode;

    public NoAlertBackdoor(NoAlertMode noAlertMode)
    {
        this.noAlertMode = noAlertMode;
    }

    @PUT
    @Consumes ({ MediaType.TEXT_PLAIN })
    public void set(String noAlertMode)
    {
        boolean noAlert = Boolean.parseBoolean(noAlertMode);
        this.noAlertMode.set(noAlert);
    }
}
