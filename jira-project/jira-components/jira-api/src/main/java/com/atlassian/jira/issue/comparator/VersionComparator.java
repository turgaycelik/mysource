package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;

import java.util.Comparator;

public class VersionComparator implements Comparator<Version>
{
    public static final Comparator<Version> COMPARATOR = new VersionComparator();
    
    public int compare(final Version v1, final Version v2)
    {
        // If these are the same version object then quickly get out of here.
        // When doing issue search sorting the Version objects are cached and reused so this will often be true.
        if (v1 == v2)
            return 0;

        if (v1 == null)
            return 1;

        if (v2 == null)
            return -1;

        Project p1 = v1.getProjectObject();
        Project p2 = v2.getProjectObject();

        if (p1 == null && p2 == null)
            return 0;

        if (p1 == null)
            return 1;

        if (p2 == null)
            return -1;

        int projectComparison = p1.getKey().compareTo(p2.getKey());
        if (projectComparison != 0)
        {
            return projectComparison;
        }
        else
        {
            int sequenceComparison = v1.getSequence().compareTo(v2.getSequence());
            if (sequenceComparison != 0)
            {
                return sequenceComparison;
            }
            else
            {
                // This block is so that in the unusual situation where the sequence gets screwed, TreeMap implementations won't fail so badly
                return v1.getName().compareTo(v2.getName());
            }
        }
    }
}
