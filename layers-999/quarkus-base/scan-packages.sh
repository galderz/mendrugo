#!/usr/bin/env bash
set -eu

lib_dir="getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/lib"
output_file="target/layer-packages.txt"
jar_bin="$HOME/opt/java-21/bin/jar"

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

IFS=',' read -ra prefix_arr <<< "$prefixes"

exclude_arr=()
if [ -n "$excludes" ]; then
    IFS=',' read -ra exclude_arr <<< "$excludes"
fi

mkdir -p target

# Collect all directory entries from all jars
all_dirs=$(mktemp)
trap "rm -f $all_dirs" EXIT

for jar in "$lib_dir"/*.jar; do
    $jar_bin tf "$jar"
done | grep '/$' | sort -u > "$all_dirs"

# For each directory entry under a prefix, determine the right output depth.
#
# Default depth is prefix_components + 1 (e.g. io.netty -> io.netty.handler).
# But if an exclusion is deeper (e.g. io.netty.handler.ssl has 4 components),
# directories under the excluded package's parent (io.netty.handler) must be
# expanded to that same depth, so we get io.netty.handler.codec, io.netty.handler.flow, etc.
# minus the excluded io.netty.handler.ssl.
#
# Algorithm: for each directory entry, truncate to default depth. If the truncated
# package is a strict prefix of any exclusion, expand to that exclusion's depth.
# Repeat until stable. Skip entries that equal an exclusion or fall under one.

truncate_pkg() {
    local pkg="$1" depth="$2"
    local count
    count=$(echo "$pkg" | awk -F. '{print NF}')
    if [ "$count" -lt "$depth" ]; then
        return 1
    fi
    echo "$pkg" | cut -d. -f1-"${depth}"
}

{
    for prefix in "${prefix_arr[@]}"; do
        prefix_path="${prefix//./\/}"
        prefix_depth=$(echo "$prefix" | awk -F. '{print NF}')
        default_depth=$((prefix_depth + 1))

        grep "^${prefix_path}/" "$all_dirs" | while IFS= read -r dir; do
            # Convert directory to package name
            pkg="${dir%/}"
            pkg="${pkg//\//.}"

            # Skip if this package falls under any exclusion
            skip=false
            for excl in "${exclude_arr[@]}"; do
                if [ "$pkg" = "$excl" ] || [[ "$pkg" == "$excl".* ]]; then
                    skip=true
                    break
                fi
            done
            $skip && continue

            # Truncate to default depth
            truncated=$(truncate_pkg "$pkg" "$default_depth") || continue

            # Iteratively expand if truncated is a strict prefix of any exclusion
            changed=true
            while $changed; do
                changed=false
                for excl in "${exclude_arr[@]}"; do
                    if [[ "$excl" == "$truncated".* ]]; then
                        excl_depth=$(echo "$excl" | awk -F. '{print NF}')
                        truncated=$(truncate_pkg "$pkg" "$excl_depth") || continue 3
                        changed=true
                        break
                    fi
                done
            done

            # Final check: skip if result equals an exclusion
            for excl in "${exclude_arr[@]}"; do
                if [ "$truncated" = "$excl" ]; then
                    continue 2
                fi
            done

            echo "$truncated"
        done
    done
} | sort -u > "$output_file"

echo "Wrote $(wc -l < "$output_file") packages to $output_file"
