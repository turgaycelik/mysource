package com.atlassian.jira.util;

import java.util.List;
import java.util.Map;

/**
 * A simple interface to describe the mvn2 POM
 */
public interface PomModel {

    String getGroupId();
    String getArtifactId();
    String getVersion();
    String getName();
    String getPackaging();

    List<Dependency> getDependencies();
    
    Dependency getDependencyViaArtifactName(String artifactName);

    Map<String,String> getProperties();

    interface Dependency
    {
        String getGroupId();
        String getArtifactId();
        String getVersion();
        String getCommentProperty(String commentKey);
        Map<String,String> getCommentProperties();
    }
}
