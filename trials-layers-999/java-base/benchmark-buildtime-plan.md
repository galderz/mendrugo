You are a GraalVM developer, and you have made changes to the way native images are built.
You want to measure the impact of these changes.

`sample-build-output.json` file contains metrics obtained from a native image build.
You want to be comparing the JSON file output before and after the change,
and provide a summary of the differences observed that it can easily be shared.
Additionally create graphs for the most significant changes observed that present those changes more pleasantly compared to the text format.

Building native images takes time (often more than 1 minute), and each run can have variations.
So you need to decide how many times to run each build and how to aggregate the different build time results.
You don't want to run this benchmark for too long, so take that also into account when deciding how many times to run.
Use sound statistical methods to aggregate these numbers and indicate why you choose the method you used.

The native image build before the changes is done by entering the `getting-started` folder,
and executing `../build-non-layered.sh` file.
This will produce a `./getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/getting-started-1.0.0-SNAPSHOT-runner-build-output-stats.json` file.
Make sure you archive the JSON file for each of the runs,
rename it as necessary so that it can be post-processed when the runs have completed to provide the aggregate result.

The native image build after the changes is done by entering the `./getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar` folder,
and executing `../../../build-layer-app.sh` file.
This will produce a `./getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/build-output-layer-app.json` file.
Make sure you archive the JSON file for each of the runs,
rename it as necessary so that it can be post-processed when the runs have completed to provide the aggregate result.

Create a bash script that executes both sets of runs.
Also create a python script that reads the results, aggregates them and produces the summary outputs describe above.

Finally, explain the scripts created and await further instructions.
