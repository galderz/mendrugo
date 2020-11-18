package org.example.main;

public class AssertThrowable extends Throwable implements AutoCloseable
{
    final boolean expected;
    boolean executed;

    public AssertThrowable(boolean expected)
    {
        this.expected = expected;
    }

    @Override
    public StackTraceElement[] getStackTrace()
    {
        executed = true;
        return new StackTraceElement[]{};
    }

    @Override
    public void close()
    {
        if (expected)
        {
            assert executed : "Expected message to be printed but didn't get printed";
        }
        else
        {
            assert !executed : "Expected message not to be printed but got printed";
        }
    }
}
