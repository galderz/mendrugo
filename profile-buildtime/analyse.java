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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
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
        final Stream<File> jobs = Arrays.stream(
            Objects.requireNonNull(
                new File("target")
                    .listFiles(File::isDirectory)
            )
        );

        final List<JobResult> jobResults = jobs
            .map(this::toJobResult)
            .collect(Collectors.toList());

        jobResults.stream()
            .map(this::toPhaseCsv)
            .forEach(this::writeCsvFile);

        writeCsvFile(toTotalCsv(jobResults));

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

    Csv toTotalCsv(List<JobResult> jobs)
    {
        final int numJobs = jobs.size();
        final int numRuns = 10;
        final AtomicInteger counter = new AtomicInteger();

        final Map<Integer, List<PhaseResult>> totals = jobs.stream()
            .flatMap(jobResult -> jobResult.phases().stream())
            .filter(phase -> phase.name.equals("Total"))
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
            .collect(Collectors.joining(",", "Time (in ms),", ""));

        List<String> result = new ArrayList<>();
        result.add(header);
        result.addAll(values);
        return new Csv("native-image-time-total", result);
    }

    Csv toPhaseCsv(JobResult job)
    {
        final int numPhases = 8;
        final AtomicInteger counter = new AtomicInteger();

        final List<String> values = job.phases.stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numPhases))
            .entrySet()
            .stream()
            .map(phaseRun ->
                phaseRun.getValue().stream()
                    .filter(p -> !p.name.equals("Total"))
                    .map(p -> p.duration.toMillis())
                    .map(String::valueOf)
                    .collect(
                        Collectors.joining(
                            ","
                            , String.format("Run %s,", phaseRun.getKey() + 1)
                            , String.format(",%s", job.name)
                        )
                    )
            )
            .collect(Collectors.toList());

        final String header = job.phases.stream()
            .limit(numPhases - 1)
            .map(PhaseResult::name)
            .collect(Collectors.joining(",", "Time (in ms),", ",Name"));

        List<String> result = new ArrayList<>();
        result.add(header);
        result.addAll(values);
        return new Csv("native-image-time-phases", result);
    }

    JobResult toJobResult(File job)
    {
        final String jobName = job.toPath().getFileName().toString();
        try(Stream<String> lines = Files.lines(job.toPath().resolve("console.log")))
        {
            final List<PhaseResult> phases = lines
                .filter(line -> line.startsWith("[") && !line.contains("("))
                .map(this::toPhaseResult)
                .collect(Collectors.toList());

            return new JobResult(jobName, -1, -1, -1, phases);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    PhaseResult toPhaseResult(String line)
    {
        final String[] elements = line.split("\\s+");
        final String phase = elements[1].replaceAll("[\\[\\]:]", "");
        final String duration = elements[2].replace(",", "");
        final String memory = elements[4];
        return new PhaseResult(
            Character.toUpperCase(phase.charAt(0)) + phase.substring(1)
            , Duration.ofMillis(Math.round(Double.parseDouble(duration)))
            , Double.parseDouble(memory)
        );
    }
    
    static record JobResult(
        String name
        , int usedClasses
        , int usedMethods
        , int usedPackages
        , List<PhaseResult> phases
    ) {}

    /**
     * Duration in milliseconds.
     * Memory in GB
     */
    record PhaseResult(String name, Duration duration, double memory) {}

    record Csv(String name, Iterable<String> lines) {}
}
