package com.atlassian.diff;

public enum DiffType
{
    UNCHANGED("diffcontext"),
    SNIPPED_LINES("diffnochange"),

    ADDED_LINES("diffadded"),
    DELETED_LINES("diffdeleted"),
    CHANGED_LINES("diffchanged"),

    ADDED_WORDS("diffaddedchars"),      // TODO - rename diffaddedchars to diffaddedwords?
    DELETED_WORDS("diffremovedchars"),
    CHANGED_WORDS("diff-changed-words"),

    ADDED_CHARACTERS("diff-added-characters"),
    DELETED_CHARACTERS("diff-deleted-characters");

    private String className;

    DiffType(String className)
    {
        this.className = className;
    }

    public String getClassName()
    {
        return className;
    }

    public boolean isChangedLineType()
    {
        return this == ADDED_LINES || this == DELETED_LINES || this == CHANGED_LINES;
    }
}
