#!/usr/bin/env bash

TARGET_FREQ_MHZ=$1
TARGET_FREQ=$((FREQ_MHZ * 1000))  # convert to kHz
TIMEOUT=60

deadline=$((SECONDS + TIMEOUT))

echo "Waiting for All CPUs set to $TARGET_FREQ_MHZ"
while (( SECONDS < deadline )); do
    all_set=true

    for c in /sys/devices/system/cpu/cpu[0-9]*; do
        min_file="$c/cpufreq/scaling_min_freq"
        max_file="$c/cpufreq/scaling_max_freq"

        if [[ ! -r "$min_file" || ! -r "$max_file" ]] ||
           [[ $(<"$min_file") != "$TARGET_FREQ" ]] ||
           [[ $(<"$max_file") != "$TARGET_FREQ" ]]; then
            all_set=false
            break
        fi
    done

    $all_set && break

    sleep 1
done

if $all_set; then
    echo "All CPUs set to $TARGET_FREQ_MHZ"
else
    echo "Timed out waiting for all CPUs to reach $TARGET_FREQ_MHZ" >&2
    exit 1
fi
