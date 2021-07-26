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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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

        jobs
            .map(this::toJobResult)
            .map(this::toCsv)
            .forEach(this::writeCsvFile);

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

    Csv toCsv(JobResult job)
    {
        final int numPhases = 8;
        final AtomicInteger counter = new AtomicInteger();

        final List<String> values = job.phases.stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / numPhases))
            .entrySet()
            .stream()
            .map(phaseRun ->
                phaseRun.getValue().stream()
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
            .limit(8)
            .map(PhaseResult::name)
            .collect(Collectors.joining(",", "Time (in ms),", ",Name"));

        List<String> result = new ArrayList<>();
        result.add(header);
        result.addAll(values);
        return new Csv("native-image-time", result);
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
