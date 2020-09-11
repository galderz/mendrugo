package jmh.basics;

public class TestBenchmarkBeforeAnalysis
{
    public static void main(String[] args)
    {
        new BenchmarkBeforeAnalysis(
            new BenchmarkBeforeAnalysis.Effects(
                f -> {}
                , m -> {}
                , f -> {}
            )
        ).beforeAnalysis();
    }
}
