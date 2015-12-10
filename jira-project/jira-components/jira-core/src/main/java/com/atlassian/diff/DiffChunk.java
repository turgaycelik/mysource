package com.atlassian.diff;

public interface DiffChunk
{
    DiffType getType();

    String getText();
}
