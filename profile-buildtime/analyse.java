///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//DEPS info.picocli:picocli:4.5.0

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Command(name = "analyse", mixinStandardHelpOptions = true, version = "analyse 0.1",
    description = "analyse made with jbang")
class analyse implements Callable<Integer>
{
    @Parameters(index = "0", description = "The greeting to print", defaultValue = "World!")
    private String greeting;

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

        writeCsvFile(toTotalCsv(jobResults, tr -> tr.duration.toString()));
        writeCsvFile(toTotalCsv(jobResults, tr -> String.valueOf(tr.maxRSS)));
        writeCsvFile(toUsedReports(jobResults));
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

    Csv toUsedReports(List<JobResult> jobs)
    {
        final String usedClasses = jobs.stream()
            .map(JobResult::usedClasses)
            .map(String::valueOf)
            .collect(Collectors.joining(",", "Used Classes,", ""));

        final String usedMethods = jobs.stream()
            .map(JobResult::usedMethods)
            .map(String::valueOf)
            .collect(Collectors.joining(",", "Used Methods,", ""));

        final String usedPackages = jobs.stream()
            .map(JobResult::usedMethods)
            .map(String::valueOf)
            .collect(Collectors.joining(",", "Used Packages,", ""));

        final String header = jobs.stream()
            .map(JobResult::name)
            .collect(Collectors.joining(",", ",", ""));

        List<String> result = List.of(header, usedClasses, usedMethods, usedPackages);
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

//    Csv toPhaseCsv(JobResult job)
//    {
//        final int numPhases = 8;
//        final AtomicInteger counter = new AtomicInteger();
//
//        final List<Phase> affectsMemory = List.of(Phase.CLASSLIST, Phase.ANALYSIS, Phase.COMPILE, Phase.IMAGE);
//
//        final List<String> values = job.phases.stream()
//            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numPhases))
//            .entrySet()
//            .stream()
//            .map(phaseRun ->
//                String.format(
//                    "%s,%s,%s"
//                    , toPhaseTimeCsv(job.name, phaseRun.getKey(), phaseRun.getValue())
//                    , toPhaseMemoryCsv(job.name, phaseRun.getKey(), phaseRun.getValue(), Phase::notTotal)
//                    , toPhaseMemoryCsv(job.name, phaseRun.getKey(), phaseRun.getValue(), p -> p.contains(affectsMemory))
//                )
//            )
//            .collect(Collectors.toList());
//
//        final String header = job.phases.stream()
//            .limit(numPhases - 1)
//            .map(p -> p.phase.toTitle())
//            .collect(Collectors.joining(",", ",", ",Name"));
//
//        final String affectsMemoryHeader = affectsMemory.stream()
//            .map(Phase::toTitle)
//            .collect(Collectors.joining(",", ",", ",Name"));
//
//        List<String> result = new ArrayList<>();
//        result.add(String.format("%s,%s,%s", header, header, affectsMemoryHeader));
//        result.addAll(values);
//        return new Csv("native-image-phases", result);
//    }

//    private String toPhaseTimeCsv(String name, int run, List<PhaseResult> phases)
//    {
//        return phases.stream()
//            .filter(p -> p.phase.notTotal())
//            .map(p -> p.duration.toMillis())
//            .map(String::valueOf)
//            .collect(
//                Collectors.joining(
//                    ","
//                    , String.format("Run %s,", run + 1)
//                    , String.format(",%s", name)
//                )
//            );
//    }

//    private String toPhaseMemoryCsv(String name, int run, List<PhaseResult> phases, Predicate<Phase> phaseFilter)
//    {
//        return phases.stream()
//            .filter(p -> phaseFilter.test(p.phase))
//            .map(PhaseResult::memory)
//            .map(String::valueOf)
//            .collect(
//                Collectors.joining(
//                    ","
//                    , String.format("Run %s,", run + 1)
//                    , String.format(",%s", name)
//                )
//            );
//    }

    JobResult toJobResult(File job)
    {
        final Path jobPath = job.toPath();
        final String jobName = jobPath.getFileName().toString();
        final Path reportsPath = jobPath.resolve("reports");
        final long usedClasses = countLines("used_classes", reportsPath);
        final long usedMethods = countLines("used_methods", reportsPath);
        final long usedPackages = countLines("used_packages", reportsPath);
        try(Stream<String> lines = Files.lines(jobPath.resolve("console.log")))
        {
            final List<TrialResult> trialResults = toTrialResults(lines);
            return new JobResult(jobName, usedClasses, usedMethods, usedPackages, trialResults);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private long countLines(String prefix, Path path) {
        final File directory = path.toFile();
        final File[] found = directory.listFiles((x, name) -> name.startsWith(prefix));
        if (found == null)
        {
            throw new IllegalStateException(String.format(
                "Path %s not a directory"
                , path
            ));
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

    private List<TrialResult> toTrialResults(Stream<String> lines)
    {
        int groupBy = 2;
        AtomicInteger index = new AtomicInteger(0);

        final Map<Integer, List<String>> rawResults = lines
            .filter(line -> line.endsWith("wall clock") || line.contains("Maximum resident set size"))
            .collect(Collectors.groupingBy(ignore -> index.getAndIncrement() / groupBy));

        return rawResults.values().stream()
            .map(this::toTrialResult)
            .toList();
    }

    TrialResult toTrialResult(List<String> values)
    {
        final LocalTime time = LocalTime.parse(lastOf(values.get(0)), DateTimeFormatter.ofPattern("m:ss"));
        final Duration wallClock = Duration.between(LocalTime.MIN, time);

        final String rssKbytes = lastOf(values.get(1));
        final double maxRSS = Double.parseDouble(rssKbytes) / 1024 / 1024;

        return new TrialResult(wallClock, maxRSS);
    }

    String lastOf(String line)
    {
        final String[] elems = line.split("\\s+");
        return elems[elems.length - 1];
    }

//    String[] toPhaseResult(String line)
//    {
//        final String[] elements = line.split("\\s+");
//        final String phase = elements[1].replaceAll("[\\[\\]:]", "");
//        final String duration = elements[2].replace(",", "");
//        final String memory = elements[4];
//        return new PhaseResult(
//            Phase.valueOf(phase.toUpperCase(Locale.ROOT))
//            , Duration.ofMillis(Math.round(Double.parseDouble(duration)))
//            , Double.parseDouble(memory)
//        );
//    }

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
    record TrialResult(Duration duration, double maxRSS) {}

    record Csv(String name, Iterable<String> lines) {}
}
