package com.atlassian.jira.rest.internal;

import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.license.LicenseBannerHelper;
import com.atlassian.jira.rest.matchers.ResponseMatchers;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.reflect.Method;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

public class LicenseBannerResourceTest
{
    @Rule
    public InitMockitoMocks mocks = new InitMockitoMocks(this);

    @Mock
    private LicenseBannerHelper helper;

    @Test
    public void remindMeLater()
    {
        LicenseBannerResource resource = new LicenseBannerResource(helper);
        Response response = resource.remindMeLater();

        verify(helper).remindMeLater();
        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.NO_CONTENT));
    }

    @Test
    public void remindMeNever()
    {
        LicenseBannerResource resource = new LicenseBannerResource(helper);
        Response response = resource.remindMeNever();

        verify(helper).remindMeNever();
        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.NO_CONTENT));
    }

    @Test
    public void removeRemindMeLater()
    {
        LicenseBannerResource resource = new LicenseBannerResource(helper);
        Response response = resource.removeRemindMeLater();

        verify(helper).clearRemindMe();
        assertThat(response, ResponseMatchers.noCache());
        assertThat(response, ResponseMatchers.status(Response.Status.NO_CONTENT));
    }

    @Test
    public void remindMeLaterAnnotationsCorrect()
    {
        try
        {
            Method remindMeLater = LicenseBannerResource.class.getMethod("remindMeLater");
            Path annotation = remindMeLater.getAnnotation(Path.class);
            assertThat(annotation, Matchers.notNullValue());
            assertThat(annotation.value(), Matchers.equalTo("remindlater"));

            assertThat(remindMeLater.getAnnotation(POST.class), Matchers.notNullValue());
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void remindMeNeverAnnotationsCorrect()
    {
        try
        {
            Method remindMeLater = LicenseBannerResource.class.getMethod("remindMeNever");
            Path annotation = remindMeLater.getAnnotation(Path.class);
            assertThat(annotation, Matchers.notNullValue());
            assertThat(annotation.value(), Matchers.equalTo("remindnever"));

            assertThat(remindMeLater.getAnnotation(POST.class), Matchers.notNullValue());
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void removeRemindMeAnnotationsCorrect()
    {
        try
        {
            Method remindMeLater = LicenseBannerResource.class.getMethod("removeRemindMeLater");
            Path annotation = remindMeLater.getAnnotation(Path.class);
            assertThat(annotation, Matchers.notNullValue());
            assertThat(annotation.value(), Matchers.equalTo("remindlater"));

            assertThat(remindMeLater.getAnnotation(DELETE.class), Matchers.notNullValue());
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void xsrf()
    {
        Consumes consumes = LicenseBannerResource.class.getAnnotation(Consumes.class);
        assertThat(consumes, Matchers.notNullValue());
        assertThat(consumes.value(), Matchers.equalTo(new String[] { MediaType.APPLICATION_JSON }));

        Produces produces = LicenseBannerResource.class.getAnnotation(Produces.class);
        assertThat(produces, Matchers.notNullValue());
        assertThat(produces.value(), Matchers.equalTo(new String[] { MediaType.APPLICATION_JSON }));
    }

    @Test
    public void resourcePathIsCorrect()
    {
        Path path = LicenseBannerResource.class.getAnnotation(Path.class);
        assertThat(path, Matchers.notNullValue());
        assertThat(path.value(), Matchers.equalTo("licensebanner"));
    }
}