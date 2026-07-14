#!/usr/bin/env bash
set -eu

# Benchmark script for comparing non-layered vs layered native image builds
# This script runs each build configuration multiple times and archives the results

# Configuration
NUM_RUNS=${NUM_RUNS:-5}  # Number of runs per configuration (default: 5)
RESULTS_DIR="benchmark-results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RUN_DIR="${RESULTS_DIR}/${TIMESTAMP}"

# Paths
GETTING_STARTED_DIR="getting-started"
NON_LAYERED_SCRIPT="../build-non-layered.sh"
LAYERED_SCRIPT="./build-layer-app.sh"
NON_LAYERED_OUTPUT="target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/getting-started-1.0.0-SNAPSHOT-runner-build-output-stats.json"
LAYERED_OUTPUT="target/build-output-layer-app.json"
LAYERED_BUILD_DIR="target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar"

echo "=== Native Image Build Benchmark ==="
echo "Number of runs per configuration: ${NUM_RUNS}"
echo "Results will be stored in: ${RUN_DIR}"
echo ""

# Create results directory
mkdir -p "${RUN_DIR}/non-layered"
mkdir -p "${RUN_DIR}/layered"

# Function to clean build artifacts
clean_build() {
    echo "Cleaning previous build artifacts..."
    cd "${GETTING_STARTED_DIR}"
    if [ -d "target" ]; then
        rm -rf target
    fi
    cd ..
}

# Function to run non-layered builds
run_non_layered_builds() {
    echo ""
    echo "=== Running Non-Layered Builds ==="

    for i in $(seq 1 ${NUM_RUNS}); do
        echo ""
        echo "--- Non-Layered Run ${i}/${NUM_RUNS} ---"

        # Clean and build
        clean_build
        cd "${GETTING_STARTED_DIR}"
        ${NON_LAYERED_SCRIPT}
        cd ..

        # Archive the result
        if [ -f "${GETTING_STARTED_DIR}/${NON_LAYERED_OUTPUT}" ]; then
            cp "${GETTING_STARTED_DIR}/${NON_LAYERED_OUTPUT}" \
               "${RUN_DIR}/non-layered/run-${i}.json"
            echo "Archived result to ${RUN_DIR}/non-layered/run-${i}.json"
        else
            echo "WARNING: Expected output file not found!"
        fi
    done

    # Run runtime performance benchmark for non-layered binary
    echo ""
    echo "=== Running Runtime Performance Benchmark for Non-Layered ==="
    if [ -f "./workshop-benchmark.sh" ]; then
        bash ./workshop-benchmark.sh -b non-layered -d 40
        # Move profiling results to benchmark directory
        if ls *_cpu.html 1> /dev/null 2>&1; then
            mv *_cpu.html "./${RUN_DIR}/non-layered/" || true
        fi
        if ls *_perfstat.txt 1> /dev/null 2>&1; then
            mv *_perfstat.txt "./${RUN_DIR}/non-layered/" || true
        fi
    else
        echo "WARNING: workshop-benchmark.sh not found, skipping runtime benchmark"
    fi
}

prepare_base_layer() {
    echo ""
    echo "=== Prepare base layer ==="

    ./build-layer-base.sh
}

# Function to run layered builds
run_layered_builds() {
    echo ""
    echo "=== Running Layered Builds ==="

    # First, we need a base build to get the target directory structure
    # The layered build assumes the jar file exists
    echo "Preparing for layered builds (creating target directory)..."
    cd "${GETTING_STARTED_DIR}"
    JAVA_HOME=$HOME/src/mandrel/sdk/latest_graalvm_home \
        ./mvnw package -DskipTests
    cd ..

    for i in $(seq 1 ${NUM_RUNS}); do
        echo ""
        echo "--- Layered Run ${i}/${NUM_RUNS} ---"

        # Remove previous native image but keep the jar
        rm -f target/getting-started-1.0.0-SNAPSHOT-runner
        rm -f target/build-output-layer-app.json

        # Run layered build
        ${LAYERED_SCRIPT}

        # Archive the result
        if [ -f "${LAYERED_OUTPUT}" ]; then
            cp "${LAYERED_OUTPUT}" \
               "${RUN_DIR}/layered/run-${i}.json"
            echo "Archived result to ${RUN_DIR}/layered/run-${i}.json"
        else
            echo "WARNING: Expected output file not found!"
        fi
    done

    # Run runtime performance benchmark for layered binary
    echo ""
    echo "=== Running Runtime Performance Benchmark for Layered ==="
    if [ -f "./workshop-benchmark.sh" ]; then
        bash ./workshop-benchmark.sh -b layered -d 40
        # Move profiling results to benchmark directory
        if ls *_cpu.html 1> /dev/null 2>&1; then
            mv *_cpu.html "./${RUN_DIR}/layered/" || true
        fi
        if ls *_perfstat.txt 1> /dev/null 2>&1; then
            mv *_perfstat.txt "./${RUN_DIR}/layered/" || true
        fi
    else
        echo "WARNING: workshop-benchmark.sh not found, skipping runtime benchmark"
    fi
}

# Main execution
echo "Starting benchmark at $(date)"
START_TIME=$(date +%s)

run_non_layered_builds
prepare_base_layer
run_layered_builds

END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))

echo ""
echo "=== Benchmark Complete ==="
echo "Total time: ${TOTAL_TIME} seconds"
echo "Results stored in: ${RUN_DIR}"
echo ""
echo "To analyze results, run:"
echo "  python3 analyze-benchmark.py ${RUN_DIR}"
