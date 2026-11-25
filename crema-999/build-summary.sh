#!/usr/bin/env bash
#
# Shared helper: parse native-image build output and print a summary table.
# Sourced by build-libjvm*.sh scripts.
#
# Usage:
#   source build-summary.sh
#   ... | print_build_summary
#
# Expects native-image output on stdin (via pipe or process substitution).
#

print_build_summary() {
    local tmpfile
    tmpfile=$(mktemp /tmp/ni-build-XXXXXX.log)
    # Stream to terminal in real time AND capture for parsing
    tee "$tmpfile"
    local output
    output=$(cat "$tmpfile")
    rm -f "$tmpfile"

    # Parse metrics from the captured output
    local code_area compilation_units image_heap objects resources total_image file_size
    code_area=$(echo "$output" | grep -oP '[\d.]+MiB.*for code area' | grep -oP '[\d.]+' | head -1)
    compilation_units=$(echo "$output" | grep -oP 'code area:\s*[\d,]+' | grep -oP '[\d,]+' | tr -d ',')
    image_heap=$(echo "$output" | grep -oP '[\d.]+MiB.*for image heap' | grep -oP '[\d.]+' | head -1)
    objects=$(echo "$output" | grep -oP 'image heap:[\d,]+' | grep -oP '[\d,]+' | tr -d ',')
    resources=$(echo "$output" | grep -oP 'and [\d,]+ resources' | grep -oP '[\d,]+' | tr -d ',')
    total_image=$(echo "$output" | grep -oP '[\d.]+MiB in total image size' | grep -oP '[\d.]+')
    file_size=$(echo "$output" | grep -oP '[\d.]+MiB in total file size' | grep -oP '[\d.]+')

    if [ -z "$code_area" ]; then
        echo ""
        echo "=== Build FAILED — no summary available ==="
        return 1
    fi

    # Suppress set -x trace for clean summary output
    { set +x; } 2>/dev/null
    echo ""
    echo "========================================"
    echo "  Build Summary"
    echo "========================================"
    printf "  %-24s %s\n" ".text (code area):" "${code_area} MB"
    printf "  %-24s %s\n" "Compilation units:" "$(printf "%'d" "$compilation_units")"
    printf "  %-24s %s\n" ".svm_heap (image heap):" "${image_heap} MB"
    printf "  %-24s %s\n" "Object count:" "$(printf "%'d" "$objects")"
    printf "  %-24s %s\n" "Resource count:" "$(printf "%'d" "$resources")"
    printf "  %-24s %s\n" "Total image size:" "${total_image} MB"
    printf "  %-24s %s\n" "File size on disk:" "${file_size} MB"
    echo "========================================"
}
