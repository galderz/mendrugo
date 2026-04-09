#!/usr/bin/env bash
set -eux

# Configuration: Classes to extract (add more as needed)
CLASSES_TO_EXTRACT=(
    # Throws CNFE without it
    # "io/vertx/core/impl/ContextInternal.class"
    # Need Netty runtime init configuration
    # "io/quarkus/runner/Feature.class"
    # Need to avoid CNFE
    # "io/netty/channel/ChannelHandlerAdapter.class"
    # More CNFEs
    # "io/netty/channel/DefaultChannelId.class"
    # "io/quarkus/runner/ApplicationImpl.class"
    # Needed by PlatformDependat
    # "io/netty/util/internal/CleanerJava9.class"
    # Add more classes here, e.g.:
    # "io/vertx/core/impl/AnotherClass.class"
)

# Variables
JAR_DIR=$(realpath "getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar")
ORIGINAL_JAR="${JAR_DIR}/getting-started-1.0.0-SNAPSHOT-runner.jar"
EXTRACTED_JAR="${JAR_DIR}/extracted-classes.jar"
FILTERED_JAR="${JAR_DIR}/getting-started-1.0.0-SNAPSHOT-runner-filtered.jar"
WORK_DIR=$(mktemp -d)
EXTRACTED_DIR="${WORK_DIR}/extracted"

# Step 1: Extract the JAR contents to a temp directory
unzip -q "${ORIGINAL_JAR}" -d "${WORK_DIR}/main"

# Step 2: Copy each class to the extracted directory
mkdir -p "${EXTRACTED_DIR}"
for class_file in "${CLASSES_TO_EXTRACT[@]}"; do
    # Create parent directory in extracted
    mkdir -p "${EXTRACTED_DIR}/$(dirname "${class_file}")"
    # Copy the class file
    cp "${WORK_DIR}/main/${class_file}" "${EXTRACTED_DIR}/${class_file}"
    # Remove from main
    rm "${WORK_DIR}/main/${class_file}"
done

# Step 3: Create JAR with extracted classes
(cd "${EXTRACTED_DIR}" && zip -rq "${EXTRACTED_JAR}" .)

# Step 4: Remove empty directories from main and create the filtered JAR
find "${WORK_DIR}/main" -type d -empty -delete
(cd "${WORK_DIR}/main" && zip -rq "${FILTERED_JAR}" .)

# Step 5: Cleanup
rm -rf "${WORK_DIR}"

echo "Done!"
echo "Original JAR (unchanged): ${ORIGINAL_JAR}"
echo "Extracted classes JAR: ${EXTRACTED_JAR}"
echo "Filtered JAR: ${FILTERED_JAR}"
