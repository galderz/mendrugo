///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 16+
//DEPS info.picocli:picocli:4.5.0

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Native;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
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
    public Integer call() throws Exception
    {
        System.out.println("Hello " + greeting);

        final Stream<File> jobs = Arrays.stream(
            Objects.requireNonNull(
                new File("target")
                    .listFiles(File::isDirectory)
            )
        );

        jobs
            .map(this::toJobResult)
            .map(this::toCsvLines)
            .forEach(lines ->
                {
                    try
                    {
                        Files.write(Path.of("target/native-image.csv"), lines);
                    }
                    catch (IOException e)
                    {
                        throw new UncheckedIOException(e);
                    }
                }
            );

        return 0;
    }

    Iterable<String> toCsvLines(JobResult job)
    {
        final List<String> values = job.phases().stream()
            .map(phase ->
                String.format(
                    "%s,%s,%s,%s"
                    , job.name
                    , phase.name
                    , phase.duration.toMillis()
                    , phase.memory
                )
            )
            .collect(Collectors.toList());

        List<String> result = new ArrayList<>();
        result.add("Job,Phase,Time,Memory");
        result.addAll(values);
        return result;
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
        final String[] elements = line.split(" ");
        final String phase = elements[1].replace(":", "");
        final String duration = elements[2].replace(",", "");
        final String memory = elements[2];
        return new PhaseResult(
            phase
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

    record PhaseResult(String name, Duration duration, double memory)
    {
        // Duration in milliseconds, memory in GB
    }
}
