///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 16+
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

        jobResults.stream()
            .map(this::toPhaseCsv)
            .forEach(this::writeCsvFile);

        writeCsvFile(toTotalCsv(jobResults));
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

    Csv toTotalCsv(List<JobResult> jobs)
    {
        final int numJobs = jobs.size();
        final int numRuns = 10;
        final AtomicInteger counter = new AtomicInteger();

        final Map<Integer, List<PhaseResult>> totals = jobs.stream()
            .flatMap(jobResult -> jobResult.phases().stream())
            .filter(phase -> phase.phase.isTotal())
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numRuns));

        final List<String> values = IntStream
            .range(0, numRuns)
            .mapToObj(i ->
                IntStream
                    .range(0, numJobs)
                    .mapToObj(j -> totals.get(j).get(i))
                    .map(p -> p.duration.toMillis())
                    .map(String::valueOf)
                    .collect(
                        Collectors.joining(
                            ","
                            , String.format("Run %s,", i + 1)
                            , ""
                        )
                    )
            )
            .collect(Collectors.toList());

        final String header = jobs.stream()
            .map(JobResult::name)
            .collect(Collectors.joining(",", ",", ""));

        List<String> result = new ArrayList<>();
        result.add(header);
        result.addAll(values);
        return new Csv("native-image-totals", result);
    }

    Csv toPhaseCsv(JobResult job)
    {
        final int numPhases = 8;
        final AtomicInteger counter = new AtomicInteger();

        final List<Phase> affectsMemory = List.of(Phase.CLASSLIST, Phase.ANALYSIS, Phase.COMPILE, Phase.IMAGE);

        final List<String> values = job.phases.stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numPhases))
            .entrySet()
            .stream()
            .map(phaseRun ->
                String.format(
                    "%s,%s,%s"
                    , toPhaseTimeCsv(job.name, phaseRun.getKey(), phaseRun.getValue())
                    , toPhaseMemoryCsv(job.name, phaseRun.getKey(), phaseRun.getValue(), Phase::notTotal)
                    , toPhaseMemoryCsv(job.name, phaseRun.getKey(), phaseRun.getValue(), p -> p.contains(affectsMemory))
                )
            )
            .collect(Collectors.toList());

        final String header = job.phases.stream()
            .limit(numPhases - 1)
            .map(p -> p.phase.toTitle())
            .collect(Collectors.joining(",", ",", ",Name"));

        final String affectsMemoryHeader = affectsMemory.stream()
            .map(Phase::toTitle)
            .collect(Collectors.joining(",", ",", ",Name"));

        List<String> result = new ArrayList<>();
        result.add(String.format("%s,%s,%s", header, header, affectsMemoryHeader));
        result.addAll(values);
        return new Csv("native-image-phases", result);
    }

    private String toPhaseTimeCsv(String name, int run, List<PhaseResult> phases)
    {
        return phases.stream()
            .filter(p -> p.phase.notTotal())
            .map(p -> p.duration.toMillis())
            .map(String::valueOf)
            .collect(
                Collectors.joining(
                    ","
                    , String.format("Run %s,", run + 1)
                    , String.format(",%s", name)
                )
            );
    }

    private String toPhaseMemoryCsv(String name, int run, List<PhaseResult> phases, Predicate<Phase> phaseFilter)
    {
        return phases.stream()
            .filter(p -> phaseFilter.test(p.phase))
            .map(PhaseResult::memory)
            .map(String::valueOf)
            .collect(
                Collectors.joining(
                    ","
                    , String.format("Run %s,", run + 1)
                    , String.format(",%s", name)
                )
            );
    }

    JobResult toJobResult(File job)
    {
        final Path jobPath = job.toPath();
        final String jobName = jobPath.getFileName().toString();
        try(Stream<String> lines = Files.lines(jobPath.resolve("console.log")))
        {
            final List<PhaseResult> phases = toPhases(lines);
            final Path reportsPath = jobPath.resolve("reports");
            final long usedClasses = countLines("used_classes", reportsPath);
            final long usedMethods = countLines("used_methods", reportsPath);
            final long usedPackages = countLines("used_packages", reportsPath);
            return new JobResult(jobName, usedClasses, usedMethods, usedPackages, phases);
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

    private List<PhaseResult> toPhases(Stream<String> lines)
    {
        return lines
            .filter(line -> line.endsWith("GB") && !line.contains("("))
            .map(this::toPhaseResult)
            .collect(Collectors.toList());
    }

    PhaseResult toPhaseResult(String line)
    {
        final String[] elements = line.split("\\s+");
        final String phase = elements[1].replaceAll("[\\[\\]:]", "");
        final String duration = elements[2].replace(",", "");
        final String memory = elements[4];
        return new PhaseResult(
            Phase.valueOf(phase.toUpperCase(Locale.ROOT))
            , Duration.ofMillis(Math.round(Double.parseDouble(duration)))
            , Double.parseDouble(memory)
        );
    }
    
    static record JobResult(
        String name
        , long usedClasses
        , long usedMethods
        , long usedPackages
        , List<PhaseResult> phases
    ) {}

    /**
     * Duration in milliseconds.
     * Memory in GB
     */
    record PhaseResult(Phase phase, Duration duration, double memory) {}

    record Csv(String name, Iterable<String> lines) {}

    enum Phase {
        CLASSLIST
        , SETUP
        , ANALYSIS
        , UNIVERSE
        , COMPILE
        , IMAGE
        , WRITE
        , TOTAL
        ;

        String toTitle()
        {
            return this.toString().charAt(0)
                + this.toString().toLowerCase(Locale.ROOT).substring(1);
        }


        boolean isTotal()
        {
            return this == TOTAL;
        }

        boolean notTotal()
        {
            return !isTotal();
        }

        boolean contains(List<Phase> phases)
        {
            return phases.contains(this);
        }
    }
}
