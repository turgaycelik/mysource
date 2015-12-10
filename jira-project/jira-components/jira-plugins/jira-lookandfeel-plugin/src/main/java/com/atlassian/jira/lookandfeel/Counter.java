package com.atlassian.jira.lookandfeel;

public class Counter
{
    private int count;

    public Counter()
    {
        count = 0;
    }

    public void increment()
    {
        count++;
    }

    public void increment(final int amount)
    {
        count += amount;
    }

    public int value()
    {
        return count;
    }

    @Override
    public String toString()
    {
        return "Counter[count=" + count + "]";
    }
}