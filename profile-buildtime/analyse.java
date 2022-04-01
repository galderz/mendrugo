///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//DEPS info.picocli:picocli:4.5.0

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Command(name = "analyse", mixinStandardHelpOptions = true, version = "analyse 0.1",
    description = "analyse made with jbang")
class analyse implements Callable<Integer>
{
    public static void main(String... args)
    {
        int exitCode = new CommandLine(new analyse()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call()
    {
        final Stream<File> jobs = Arrays
            .stream(
                Objects.requireNonNull(
                    new File("target").listFiles(File::isDirectory)
                )
            )
            .sorted();

        final List<JobResult> jobResults = jobs
            .map(this::toJobResult)
            .collect(Collectors.toList());

        writeCsvFile(toTotalCsv(jobResults, tr -> String.valueOf(tr.duration.toSeconds())));
        writeCsvFile(toUsedClasses(jobResults));
        writeCsvFile(toUsedMethods(jobResults));
        writeCsvFile(toUsedPackages(jobResults));
        writeCsvFile(toTotalCsv(jobResults, tr -> String.format("%.1f", tr.maxRss)));
        return 0;
    }

    private void writeCsvFile(Csv csv)
    {
        try
        {
            final Path path = Path.of("target", String.format("%s.csv", csv.name));
            Files.write(path, csv.lines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    Csv toUsedClasses(List<JobResult> jobs)
    {
        final String usedClasses = jobs.stream()
            .map(JobResult::usedClasses)
            .map(String::valueOf)
            .collect(Collectors.joining(",", "Used Classes,", ""));

        final String header = jobs.stream()
            .map(JobResult::name)
            .collect(Collectors.joining(",", ",", ""));

        List<String> result = List.of(header, usedClasses);
        return new Csv("native-image-totals", result);
    }

    Csv toUsedMethods(List<JobResult> jobs)
    {
        final String usedMethods = jobs.stream()
            .map(JobResult::usedMethods)
            .map(String::valueOf)
            .collect(Collectors.joining(",", "Used Methods,", ""));

        final String header = jobs.stream()
            .map(JobResult::name)
            .collect(Collectors.joining(",", ",", ""));

        List<String> result = List.of(header, usedMethods);
        return new Csv("native-image-totals", result);
    }

    Csv toUsedPackages(List<JobResult> jobs)
    {
        final String usedPackages = jobs.stream()
            .map(JobResult::usedPackages)
            .map(String::valueOf)
            .collect(Collectors.joining(",", "Used Packages,", ""));

        final String header = jobs.stream()
            .map(JobResult::name)
            .collect(Collectors.joining(",", ",", ""));

        List<String> result = List.of(header, usedPackages);
        return new Csv("native-image-totals", result);
    }

    Csv toTotalCsv(List<JobResult> jobs, Function<TrialResult, String> extractor)
    {
        final int numJobs = jobs.size();
        final int numTrials = 10;
        final AtomicInteger counter = new AtomicInteger();

        final Map<Integer, List<TrialResult>> totals = jobs.stream()
            .flatMap(job -> job.trialResults.stream())
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numTrials));

        final List<String> duration = IntStream
            .range(0, numTrials)
            .mapToObj(i ->
                IntStream
                    .range(0, numJobs)
                    .mapToObj(j -> totals.get(j).get(i))
                    .map(extractor)
                    .map(String::valueOf)
                    .collect(
                        Collectors.joining(
                            ","
                            , String.format("Run %s,", i + 1)
                            , ""
                        )
                    )
            ).toList();

        final String header = jobs.stream()
            .map(JobResult::name)
            .collect(Collectors.joining(",", ",", ""));

        List<String> result = new ArrayList<>();
        result.add(header);
        result.addAll(duration);
        return new Csv("native-image-totals", result);
    }

    JobResult toJobResult(File job)
    {
        final Path jobPath = job.toPath();
        final String jobName = jobPath.getFileName().toString();
        final Path reportsPath = jobPath.resolve("reports");
        final long usedClasses = countLines("used_classes", reportsPath);
        final long usedMethods = countLines("used_methods", reportsPath);
        final long usedPackages = countLines("used_packages", reportsPath);
        final List<TrialResult> trialResults = toTrialResults(jobPath);
        return new JobResult(jobName, usedClasses, usedMethods, usedPackages, trialResults);
    }

    private long countLines(String prefix, Path path) {
        final File directory = path.toFile();
        final File[] found = directory.listFiles((x, name) -> name.startsWith(prefix));
        if (found == null)
        {
            return -1;
        }

        if (found.length != 1)
        {
            throw new IllegalStateException(String.format(
                "Expected only 1 file to be found with prefix %s in %s"
                , prefix
                , path
            ));
        }

        try
        {
            return Files.lines(found[0].toPath()).count();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private List<TrialResult> toTrialResults(Path jobPath)
    {
        final Path timesLog = jobPath.resolve("times.log");
        if (timesLog.toFile().exists())
        {
            try(Stream<String> lines = Files.lines(timesLog))
            {
                int groupBy = 3;
                AtomicInteger index = new AtomicInteger(0);

                final Map<Integer, List<String>> rawResults = lines
                    .filter(line ->
                        line.contains("Command being timed")
                            || line.contains("wall clock")
                            || line.contains("Maximum resident set size")
                    ).collect(Collectors.groupingBy(ignore -> index.getAndIncrement() / groupBy));

                return rawResults.values().stream()
                    .map(this::toTrialResult)
                    .filter(tr -> !tr.duration.equals(Duration.ZERO))
                    .toList();
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }

        // If times.log not present, try the old 21.3 and before method
        try(Stream<String> lines = Files.lines(jobPath.resolve("console.log")))
        {
            final List<TrialResult> trialResults = lines
                .filter(l -> l.contains("[total]"))
                .map(this::toTrialResultPre22)
                .toList();

            if (!trialResults.isEmpty())
                return trialResults;
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }

        // Then we must be dealing with post 22.0 output
        try(Stream<String> lines = Files.lines(jobPath.resolve("console.log")))
        {
            final int numItems = 2;
            final AtomicInteger counter = new AtomicInteger();

            return lines
                .filter(l -> l.contains("Finished generating") || l.contains("Peak RSS"))
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numItems))
                .values()
                .stream()
                .map(this::toTrialResultPost22)
                .toList();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private TrialResult toTrialResultPost22(List<String> elements)
    {
        System.out.println(elements);
        final double peakRss = Double.parseDouble(elements.get(0).split("\\s+")[12].replace("GB", ""));
        final String[] wallClockElems = elements.get(1).split("\\s+");
        final Duration wallClock = Duration.parse("PT" + wallClockElems[4] + wallClockElems[5].replace(".", ""));
        return new TrialResult(wallClock, peakRss);
    }

    private TrialResult toTrialResultPre22(String line)
    {
        final String[] elements = line.split("\\s+");
        final String duration = elements[2].replace(",", "");
        return new TrialResult(Duration.ofMillis(Math.round(Double.parseDouble(duration))), -1);
    }

    TrialResult toTrialResult(List<String> values)
    {
        final String commandLine = values.get(0);
        if (commandLine.contains("runner.jar") && !commandLine.contains("-H:+PrintAnalysisCallTree"))
        {
            final String wallClockLine = values.get(1);
            final Duration wallClock = parseDuration(lastOf(wallClockLine));

            final String maxRssLine = values.get(2);
            final String maxRssKbytes = lastOf(maxRssLine);
            final double maxRss = Double.parseDouble(maxRssKbytes) / 1024 / 1024;

            return new TrialResult(wallClock, maxRss);
        }

        return new TrialResult(Duration.ZERO, -1);
    }

    public static Duration parseDuration(String duration)
    {
        return Duration.parse(
            "PT"
                + duration.replace(':', 'M')
                + "S"
        );
    }

    String lastOf(String line)
    {
        final String[] elems = line.split("\\s+");
        return elems[elems.length - 1];
    }

    record JobResult(
        String name
        , long usedClasses
        , long usedMethods
        , long usedPackages
        , List<TrialResult> trialResults
    ) {}

    /**
     * Duration in milliseconds.
     * Max RSS in GB
     */
    record TrialResult(Duration duration, double maxRss) {}

    record Csv(String name, Iterable<String> lines) {}
}
