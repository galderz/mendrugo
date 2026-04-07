#!/usr/bin/env bash
set -eu

lib_dir="getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/lib"
output_file="target/layer-packages.txt"

prefixes=""
excludes=""

while getopts "p:e:" opt; do
    case $opt in
        p) prefixes="$OPTARG" ;;
        e) excludes="$OPTARG" ;;
        *) echo "Usage: $0 -p prefix1,prefix2 [-e exclude1,exclude2]" >&2; exit 1 ;;
    esac
done

if [ -z "$prefixes" ]; then
    echo "Error: -p (package prefixes) is required" >&2
    exit 1
fi

# Convert comma-separated prefixes to an array
IFS=',' read -ra prefix_arr <<< "$prefixes"

# Convert comma-separated excludes to an array
exclude_arr=()
if [ -n "$excludes" ]; then
    IFS=',' read -ra exclude_arr <<< "$excludes"
fi

# Build grep pattern for prefixes: match directories one level below each prefix
# e.g. prefix "io.netty" matches "io/netty/handler/" but not "io/netty/" or "io/netty/handler/codec/"
grep_patterns=()
for prefix in "${prefix_arr[@]}"; do
    # Convert dot-separated prefix to slash-separated directory path
    prefix_path="${prefix//./\/}"
    # Count components in the prefix
    depth=$(echo "$prefix" | awk -F. '{print NF}')
    # Match exactly one level deeper: prefix/component/
    grep_patterns+=("-e" "^${prefix_path}/[^/]\\+/\$")
done

mkdir -p target

# Scan all jars, extract directory entries matching our prefixes one level deep
for jar in "$lib_dir"/*.jar; do
    jar tf "$jar"
done \
    | grep "${grep_patterns[@]}" \
    | sort -u \
    | while read -r dir; do
        # Convert directory path back to package name (strip trailing /)
        pkg="${dir%/}"
        pkg="${pkg//\//.}"

        # Check exclusions
        skip=false
        for excl in "${exclude_arr[@]}"; do
            if [ "$pkg" = "$excl" ]; then
                skip=true
                break
            fi
        done

        if [ "$skip" = false ]; then
            echo "$pkg"
        fi
    done > "$output_file"

echo "Wrote $(wc -l < "$output_file") packages to $output_file"
