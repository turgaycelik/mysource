package com.atlassian.jira.issue.changehistory.metadata.renderer;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataParticipant;
import com.atlassian.jira.util.I18nHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HistoryMetadataRenderHelperTest
{
    @Mock
    private I18nHelper i18nHelper;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private HistoryMetadataRenderHelper helper;


    @Test
    public void testGetParticipantAvatarUrl() throws Exception
    {
        // having
        final HistoryMetadataParticipant relativeAvatarWithSlash = HistoryMetadataParticipant.builder("foo", "bar")
                .avatarUrl("/download/resource")
                .build();
        final HistoryMetadataParticipant relativeAvatarWithoutSlash = HistoryMetadataParticipant.builder("foo", "bar")
                .avatarUrl("download/resource")
                .build();
        final HistoryMetadataParticipant absoluteAvatar = HistoryMetadataParticipant.builder("foo", "bar")
                .avatarUrl("http://localhost/download/resource")
                .build();

        // when
        when(applicationProperties.getString(APKeys.JIRA_BASEURL)).thenReturn("http://gojira/jira");
        final String baseWithoutSlashRelativeWithSlash = helper.getParticipantAvatarUrl(relativeAvatarWithSlash);
        final String baseWithoutSlashRelativeWithoutSlash = helper.getParticipantAvatarUrl(relativeAvatarWithoutSlash);

        when(applicationProperties.getString(APKeys.JIRA_BASEURL)).thenReturn("http://gojira/jira/");
        final String baseWithSlashRelativeWithSlash = helper.getParticipantAvatarUrl(relativeAvatarWithSlash);
        final String baseWithSlashRelativeWithoutSlash = helper.getParticipantAvatarUrl(relativeAvatarWithoutSlash);

        final String absolute = helper.getParticipantAvatarUrl(absoluteAvatar);

        // then
        assertThat(baseWithoutSlashRelativeWithSlash, equalTo("http://gojira/jira/download/resource"));
        assertThat(baseWithoutSlashRelativeWithoutSlash, equalTo("http://gojira/jira/download/resource"));
        assertThat(baseWithSlashRelativeWithSlash, equalTo("http://gojira/jira/download/resource"));
        assertThat(baseWithSlashRelativeWithoutSlash, equalTo("http://gojira/jira/download/resource"));
        assertThat(absolute, equalTo(absoluteAvatar.getAvatarUrl()));
    }

    @Test
    public void testGetParticipantAvatarUrlHandlesNulls() throws Exception
    {
        assertThat(helper.getParticipantAvatarUrl(null), nullValue());
        assertThat(helper.getParticipantAvatarUrl(HistoryMetadataParticipant.builder("null", "avatar").build()), nullValue());
    }

    @Test
    public void testGetParticipantName() throws Exception
    {
        // having
        when(i18nHelper.isKeyDefined("custom.i18n.displayname")).thenReturn(true);
        when(i18nHelper.getText("custom.i18n.displayname", "#1")).thenReturn("pull request #1");

        // when
        final String displayName = helper.getParticipantName(HistoryMetadataParticipant.builder("#1", "PullRequest")
                .displayNameKey("custom.i18n.displayname")
                .build());
        final String missingI18nKey = helper.getParticipantName(HistoryMetadataParticipant.builder("#1", "PullRequest")
                .displayNameKey("custom.i18n.displayname.nonexistant")
                .displayName("PR #1")
                .build());
        final String missingI18nKeyAndDisplayName = helper.getParticipantName(HistoryMetadataParticipant.builder("#1", "PullRequest")
                .displayNameKey("custom.i18n.displayname.nonexistant")
                .build());

        // then
        assertThat(displayName, equalTo("pull request #1"));
        assertThat(missingI18nKey, equalTo("PR #1"));
        assertThat(missingI18nKeyAndDisplayName, equalTo("#1"));
    }

    @Test
    public void testGetParticipantNameHandlesNulls() throws Exception
    {
        assertThat(helper.getParticipantName(null), nullValue());
        assertThat(helper.getParticipantName(HistoryMetadataParticipant.builder(null, null).build()), nullValue());
    }
}
