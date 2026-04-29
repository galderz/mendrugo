#!/usr/bin/env python3
"""
Benchmark Analysis Script for Native Image Build Times

This script reads JSON build output files from multiple benchmark runs,
aggregates the results using sound statistical methods, and produces:
1. A text summary of the differences
2. Graphs visualizing the most significant changes

Statistical Method: Median with Interquartile Range (IQR)
---------------------------------------------------------
We use median and IQR rather than mean and standard deviation because:
1. Build times can have outliers (system load spikes, GC pauses, etc.)
2. Median is more robust to outliers than mean
3. IQR provides a better measure of spread for non-normal distributions
4. With a small number of samples (5 runs), median is more reliable

For comparison, we also report the relative change as a percentage
and use the Mann-Whitney U test for statistical significance.
"""

import json
import os
import sys
from pathlib import Path
from typing import Any
import statistics

# Check for optional dependencies
try:
    import matplotlib.pyplot as plt
    import matplotlib.patches as mpatches
    HAS_MATPLOTLIB = True
except ImportError:
    HAS_MATPLOTLIB = False
    print("Warning: matplotlib not installed. Graphs will not be generated.")
    print("Install with: pip install matplotlib")

try:
    from scipy import stats as scipy_stats
    HAS_SCIPY = True
except ImportError:
    HAS_SCIPY = False
    print("Warning: scipy not installed. Statistical significance tests will be skipped.")
    print("Install with: pip install scipy")


def load_json_files(directory: Path) -> list[dict]:
    """Load all JSON files from a directory."""
    results = []
    for json_file in sorted(directory.glob("run-*.json")):
        with open(json_file) as f:
            data = json.load(f)
            data['_source_file'] = json_file.name
            results.append(data)
    return results


def extract_metrics(data: dict) -> dict[str, float]:
    """Extract key metrics from a build output JSON."""
    metrics = {}

    # Resource usage
    ru = data.get('resource_usage', {})
    metrics['total_time_secs'] = ru.get('total_secs', 0)

    mem = ru.get('memory', {})
    metrics['peak_rss_mb'] = mem.get('peak_rss_bytes', 0) / (1024 * 1024)

    gc = ru.get('garbage_collection', {})
    metrics['gc_count'] = gc.get('count', 0)
    metrics['gc_time_secs'] = gc.get('total_secs', 0)
    metrics['gc_max_heap_mb'] = gc.get('max_heap', 0) / (1024 * 1024)

    cpu = ru.get('cpu', {})
    metrics['cpu_load'] = cpu.get('load', 0)

    # Image details
    img = data.get('image_details', {})
    metrics['image_size_mb'] = img.get('total_bytes', 0) / (1024 * 1024)

    code = img.get('code_area', {})
    metrics['code_size_mb'] = code.get('bytes', 0) / (1024 * 1024)
    metrics['compilation_units'] = code.get('compilation_units', 0)

    heap = img.get('image_heap', {})
    metrics['heap_size_mb'] = heap.get('bytes', 0) / (1024 * 1024)
    metrics['heap_objects'] = heap.get('objects', {}).get('count', 0)

    # Analysis results
    analysis = data.get('analysis_results', {})
    types = analysis.get('types', {})
    metrics['reachable_types'] = types.get('reachable', 0)
    metrics['total_types'] = types.get('total', 0)

    methods = analysis.get('methods', {})
    metrics['reachable_methods'] = methods.get('reachable', 0)
    metrics['total_methods'] = methods.get('total', 0)

    fields = analysis.get('fields', {})
    metrics['reachable_fields'] = fields.get('reachable', 0)
    metrics['total_fields'] = fields.get('total', 0)

    return metrics


def aggregate_metrics(all_metrics: list[dict[str, float]]) -> dict[str, dict[str, float]]:
    """
    Aggregate metrics across multiple runs using median and IQR.

    Returns a dict with metric names as keys and stats dict as values.
    Stats include: median, q1 (25th percentile), q3 (75th percentile),
    iqr, min, max, mean, stdev, and all raw values.
    """
    if not all_metrics:
        return {}

    # Collect all values for each metric
    metric_values: dict[str, list[float]] = {}
    for metrics in all_metrics:
        for key, value in metrics.items():
            if key not in metric_values:
                metric_values[key] = []
            metric_values[key].append(value)

    # Calculate statistics for each metric
    aggregated = {}
    for key, values in metric_values.items():
        sorted_values = sorted(values)
        n = len(sorted_values)

        q1_idx = n // 4
        q3_idx = (3 * n) // 4

        q1 = sorted_values[q1_idx] if n > 1 else sorted_values[0]
        q3 = sorted_values[q3_idx] if n > 1 else sorted_values[0]
        median = statistics.median(sorted_values)

        aggregated[key] = {
            'median': median,
            'q1': q1,
            'q3': q3,
            'iqr': q3 - q1,
            'min': min(sorted_values),
            'max': max(sorted_values),
            'mean': statistics.mean(sorted_values),
            'stdev': statistics.stdev(sorted_values) if n > 1 else 0,
            'values': sorted_values,
            'n': n
        }

    return aggregated


def calculate_differences(
    before: dict[str, dict[str, float]],
    after: dict[str, dict[str, float]]
) -> list[dict[str, Any]]:
    """
    Calculate differences between before and after metrics.

    Returns a list of diffs sorted by absolute percentage change.
    """
    diffs = []

    for key in before:
        if key not in after:
            continue

        before_median = before[key]['median']
        after_median = after[key]['median']

        if before_median == 0:
            if after_median == 0:
                pct_change = 0
            else:
                pct_change = float('inf')
        else:
            pct_change = ((after_median - before_median) / before_median) * 100

        abs_change = after_median - before_median

        # Statistical significance using Mann-Whitney U test
        p_value = None
        if HAS_SCIPY and len(before[key]['values']) > 1 and len(after[key]['values']) > 1:
            try:
                _, p_value = scipy_stats.mannwhitneyu(
                    before[key]['values'],
                    after[key]['values'],
                    alternative='two-sided'
                )
            except Exception:
                pass

        diffs.append({
            'metric': key,
            'before_median': before_median,
            'after_median': after_median,
            'before_iqr': before[key]['iqr'],
            'after_iqr': after[key]['iqr'],
            'before_values': before[key]['values'],
            'after_values': after[key]['values'],
            'abs_change': abs_change,
            'pct_change': pct_change,
            'p_value': p_value,
            'significant': p_value is not None and p_value < 0.05
        })

    # Sort by absolute percentage change (descending)
    diffs.sort(key=lambda x: abs(x['pct_change']) if x['pct_change'] != float('inf') else 999999, reverse=True)

    return diffs


def format_value(value: float, metric: str) -> str:
    """Format a value based on its metric type."""
    if 'time' in metric or 'secs' in metric:
        return f"{value:.3f}s"
    elif 'mb' in metric.lower():
        return f"{value:.2f} MB"
    elif 'load' in metric:
        return f"{value:.2f}"
    else:
        return f"{value:,.0f}"


def format_change(pct: float) -> str:
    """Format percentage change with sign and color indicator."""
    if pct == float('inf'):
        return "N/A (was 0)"
    sign = "+" if pct > 0 else ""
    return f"{sign}{pct:.2f}%"


def generate_summary(
    diffs: list[dict],
    before_runs: int,
    after_runs: int,
    output_path: Path
) -> str:
    """Generate a text summary of the benchmark results."""
    lines = []
    lines.append("=" * 70)
    lines.append("NATIVE IMAGE BUILD BENCHMARK RESULTS")
    lines.append("=" * 70)
    lines.append("")
    lines.append(f"Runs per configuration: {before_runs} (before), {after_runs} (after)")
    lines.append("")
    lines.append("Statistical Method: Median with Interquartile Range (IQR)")
    lines.append("  - Median is robust to outliers from system variability")
    lines.append("  - IQR shows the spread of the middle 50% of values")
    if HAS_SCIPY:
        lines.append("  - Mann-Whitney U test used for statistical significance (p < 0.05)")
    lines.append("")

    # Key metrics summary
    lines.append("-" * 70)
    lines.append("KEY METRICS SUMMARY")
    lines.append("-" * 70)
    lines.append("")

    key_metrics = ['total_time_secs', 'peak_rss_mb', 'gc_time_secs', 'image_size_mb']

    for diff in diffs:
        if diff['metric'] in key_metrics:
            metric = diff['metric']
            lines.append(f"{metric}:")
            lines.append(f"  Before: {format_value(diff['before_median'], metric)} "
                        f"(IQR: {format_value(diff['before_iqr'], metric)})")
            lines.append(f"  After:  {format_value(diff['after_median'], metric)} "
                        f"(IQR: {format_value(diff['after_iqr'], metric)})")
            lines.append(f"  Change: {format_change(diff['pct_change'])}")
            if diff['p_value'] is not None:
                sig = "Yes" if diff['significant'] else "No"
                lines.append(f"  Significant: {sig} (p={diff['p_value']:.4f})")
            lines.append("")

    # All metrics sorted by impact
    lines.append("-" * 70)
    lines.append("ALL METRICS (sorted by absolute % change)")
    lines.append("-" * 70)
    lines.append("")

    header = f"{'Metric':<25} {'Before':>12} {'After':>12} {'Change':>12}"
    lines.append(header)
    lines.append("-" * len(header))

    for diff in diffs:
        metric = diff['metric']
        before = format_value(diff['before_median'], metric)
        after = format_value(diff['after_median'], metric)
        change = format_change(diff['pct_change'])
        sig_marker = "*" if diff['significant'] else " "
        lines.append(f"{metric:<25} {before:>12} {after:>12} {change:>11}{sig_marker}")

    if HAS_SCIPY:
        lines.append("")
        lines.append("* indicates statistically significant difference (p < 0.05)")

    lines.append("")
    lines.append("=" * 70)

    summary = "\n".join(lines)

    # Write to file
    with open(output_path, 'w') as f:
        f.write(summary)

    return summary


def generate_graphs(diffs: list[dict], output_dir: Path):
    """Generate graphs for the most significant changes."""
    if not HAS_MATPLOTLIB:
        return

    # Select top metrics for visualization
    key_metrics = ['total_time_secs', 'peak_rss_mb', 'gc_time_secs',
                   'image_size_mb', 'heap_size_mb', 'code_size_mb']

    key_diffs = [d for d in diffs if d['metric'] in key_metrics]

    if not key_diffs:
        print("No key metrics found for graphing")
        return

    # 1. Comparison bar chart for key metrics
    fig, axes = plt.subplots(2, 3, figsize=(14, 10))
    axes = axes.flatten()

    for i, diff in enumerate(key_diffs[:6]):
        ax = axes[i]
        metric = diff['metric']

        before_vals = diff['before_values']
        after_vals = diff['after_values']

        # Box plot comparison
        positions = [1, 2]
        bp = ax.boxplot([before_vals, after_vals], positions=positions,
                       widths=0.6, patch_artist=True)

        bp['boxes'][0].set_facecolor('#3498db')
        bp['boxes'][1].set_facecolor('#e74c3c')

        ax.set_xticklabels(['Before\n(Non-layered)', 'After\n(Layered)'])
        ax.set_title(f'{metric}\n({format_change(diff["pct_change"])})',
                    fontsize=10, fontweight='bold')

        # Add median labels
        ax.text(1, diff['before_median'], f'{diff["before_median"]:.2f}',
               ha='center', va='bottom', fontsize=8)
        ax.text(2, diff['after_median'], f'{diff["after_median"]:.2f}',
               ha='center', va='bottom', fontsize=8)

    plt.suptitle('Native Image Build Comparison: Before vs After Changes',
                fontsize=14, fontweight='bold')
    plt.tight_layout()
    plt.savefig(output_dir / 'comparison_boxplots.png', dpi=150, bbox_inches='tight')
    plt.close()

    # 2. Percentage change bar chart
    fig, ax = plt.subplots(figsize=(12, 6))

    metrics = [d['metric'] for d in diffs if d['pct_change'] != float('inf')][:15]
    changes = [d['pct_change'] for d in diffs if d['pct_change'] != float('inf')][:15]

    colors = ['#27ae60' if c < 0 else '#e74c3c' for c in changes]

    y_pos = range(len(metrics))
    ax.barh(y_pos, changes, color=colors)
    ax.set_yticks(y_pos)
    ax.set_yticklabels(metrics)
    ax.set_xlabel('Percentage Change (%)')
    ax.set_title('Percentage Change by Metric (Green = Improvement, Red = Increase)')
    ax.axvline(x=0, color='black', linewidth=0.5)

    # Add value labels
    for i, (metric, change) in enumerate(zip(metrics, changes)):
        ax.text(change + (1 if change >= 0 else -1), i, f'{change:.1f}%',
               va='center', ha='left' if change >= 0 else 'right', fontsize=8)

    plt.tight_layout()
    plt.savefig(output_dir / 'percentage_changes.png', dpi=150, bbox_inches='tight')
    plt.close()

    # 3. Build time comparison (if available)
    time_diff = next((d for d in diffs if d['metric'] == 'total_time_secs'), None)
    if time_diff:
        fig, ax = plt.subplots(figsize=(8, 5))

        before_vals = time_diff['before_values']
        after_vals = time_diff['after_values']

        runs = range(1, max(len(before_vals), len(after_vals)) + 1)

        ax.plot(runs[:len(before_vals)], before_vals, 'bo-',
               label=f'Before (median: {time_diff["before_median"]:.2f}s)', markersize=8)
        ax.plot(runs[:len(after_vals)], after_vals, 'rs-',
               label=f'After (median: {time_diff["after_median"]:.2f}s)', markersize=8)

        ax.axhline(y=time_diff['before_median'], color='blue', linestyle='--', alpha=0.5)
        ax.axhline(y=time_diff['after_median'], color='red', linestyle='--', alpha=0.5)

        ax.set_xlabel('Run Number')
        ax.set_ylabel('Build Time (seconds)')
        ax.set_title('Build Time Across Runs')
        ax.legend()
        ax.grid(True, alpha=0.3)

        plt.tight_layout()
        plt.savefig(output_dir / 'build_time_comparison.png', dpi=150, bbox_inches='tight')
        plt.close()

    print(f"Graphs saved to {output_dir}")


def main():
    if len(sys.argv) < 2:
        print("Usage: python analyze-benchmark.py <results_directory>")
        print("")
        print("The results directory should contain 'non-layered' and 'layered'")
        print("subdirectories with run-*.json files from the benchmark runs.")
        sys.exit(1)

    results_dir = Path(sys.argv[1])

    if not results_dir.exists():
        print(f"Error: Results directory not found: {results_dir}")
        sys.exit(1)

    non_layered_dir = results_dir / "non-layered"
    layered_dir = results_dir / "layered"

    if not non_layered_dir.exists() or not layered_dir.exists():
        print(f"Error: Expected 'non-layered' and 'layered' subdirectories in {results_dir}")
        sys.exit(1)

    # Load data
    print(f"Loading results from {results_dir}")
    before_data = load_json_files(non_layered_dir)
    after_data = load_json_files(layered_dir)

    print(f"  Non-layered runs: {len(before_data)}")
    print(f"  Layered runs: {len(after_data)}")

    if not before_data or not after_data:
        print("Error: No data files found")
        sys.exit(1)

    # Extract and aggregate metrics
    before_metrics = [extract_metrics(d) for d in before_data]
    after_metrics = [extract_metrics(d) for d in after_data]

    before_agg = aggregate_metrics(before_metrics)
    after_agg = aggregate_metrics(after_metrics)

    # Calculate differences
    diffs = calculate_differences(before_agg, after_agg)

    # Generate outputs
    summary_path = results_dir / "summary.txt"
    summary = generate_summary(diffs, len(before_data), len(after_data), summary_path)
    print("")
    print(summary)

    # Generate graphs
    generate_graphs(diffs, results_dir)

    print("")
    print(f"Summary saved to: {summary_path}")
    if HAS_MATPLOTLIB:
        print(f"Graphs saved to: {results_dir}")


if __name__ == "__main__":
    main()
