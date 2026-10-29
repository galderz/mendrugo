#!/usr/bin/env bash
#
# Find the Maven artifact and version for a missing class, using the
# project's dependency tree to determine the correct version.
#
# Strategy:
# 1. Search Maven Central for which artifact contains the class
# 2. For each candidate, check if it appears as a provided/optional/excluded
#    dependency in any of the project's resolved dependencies' POMs
# 3. Show the version that matches the project's dependency management
#
# Usage:
#   ./find-dep.sh org.apache.log4j.Priority
#   ./find-dep.sh org/jboss/modules/ModuleClassLoader
#   ./find-dep.sh -d lib/optional org.apache.log4j.Priority
#
# Options:
#   -d DIR   Download the jar to DIR
#   -p POM   Path to project POM (default: getting-started/pom.xml)
#
set -euo pipefail

OPT_DIR=""
PROJECT_POM="getting-started/pom.xml"

while getopts "d:p:" opt; do
    case $opt in
        d) OPT_DIR="$OPTARG" ;;
        p) PROJECT_POM="$OPTARG" ;;
        *) echo "Usage: $0 [-d download_dir] [-p pom.xml] <class_name>" >&2; exit 1 ;;
    esac
done
shift $((OPTIND - 1))

if [ $# -lt 1 ]; then
    echo "Usage: $0 [-d download_dir] [-p pom.xml] <class_name>" >&2
    echo "" >&2
    echo "Examples:" >&2
    echo "  $0 org.apache.log4j.Priority" >&2
    echo "  $0 -d lib/optional org.jboss.modules.ModuleClassLoader" >&2
    exit 1
fi

CLASS_NAME=$(echo "$1" | tr '/' '.')

echo "=== Looking for: $CLASS_NAME ==="
echo ""

# Step 1: Search Maven Central for artifacts containing this class
echo "Searching Maven Central..."

# Strategy A: search by fully qualified classname (fc:)
RESPONSE=$(curl -s "https://search.maven.org/solrsearch/select?q=fc:${CLASS_NAME}&rows=20&wt=json" 2>/dev/null)

CANDIDATES=$(python3 -c "
import sys, json
data = json.load(sys.stdin)
seen = set()
for doc in data['response']['docs']:
    g = doc.get('g', '')
    a = doc.get('a', '')
    key = f'{g}:{a}'
    if key not in seen:
        seen.add(key)
        print(key)
" <<< "$RESPONSE" 2>/dev/null || true)

# Strategy B: guess groupId:artifactId from package name
# e.g., org.apache.log4j.Priority -> try g:org.apache.log4j a:log4j
#       org.jboss.modules.ModuleClassLoader -> try g:org.jboss.modules a:jboss-modules
PKG=$(echo "$CLASS_NAME" | rev | cut -d. -f2- | rev)
GUESS_A=$(echo "$PKG" | rev | cut -d. -f1 | rev)
GUESS_RESP=$(curl -s "https://search.maven.org/solrsearch/select?q=g:${PKG}+AND+a:${GUESS_A}&rows=1&wt=json" 2>/dev/null)
GUESS_CAND=$(python3 -c "
import sys, json
docs = json.load(sys.stdin)['response']['docs']
for d in docs:
    print(f\"{d['g']}:{d['a']}\")
" <<< "$GUESS_RESP" 2>/dev/null || true)

# Also try various groupId guesses from the package name
# e.g., for org.apache.log4j try g:log4j a:log4j, g:org.apache a:log4j, etc.
GUESS_CAND=$(python3 -c "
import sys, json, urllib.request

pkg = '$PKG'
parts = pkg.split('.')
last = parts[-1]

# Generate g:a guesses
guesses = []
# Try each prefix as groupId, last segment or hyphenated remainder as artifactId
for i in range(len(parts)):
    g = '.'.join(parts[:i+1])
    guesses.append((g, last))
    if i < len(parts) - 1:
        a_hyp = '-'.join(parts[i+1:])
        guesses.append((g, a_hyp))
# Also try each suffix as groupId (handles cases like log4j:log4j)
for i in range(len(parts)):
    g = '.'.join(parts[i:])
    guesses.append((g, last))
    guesses.append((g, g))  # g=a case like log4j:log4j

seen = set()
for g, a in guesses:
    try:
        url = f'https://search.maven.org/solrsearch/select?q=g:{g}+AND+a:{a}&rows=1&wt=json'
        resp = json.loads(urllib.request.urlopen(url, timeout=5).read())
        for d in resp['response']['docs']:
            key = f\"{d['g']}:{d['a']}\"
            if key not in seen:
                seen.add(key)
                print(key)
    except:
        pass
" 2>/dev/null || true)

# Merge candidates, dedup
CANDIDATES=$(printf "%s\n%s" "$CANDIDATES" "$GUESS_CAND" | grep -v '^$' | sort -u)

if [ -z "$CANDIDATES" ]; then
    echo "No artifacts found on Maven Central for: $CLASS_NAME"
    exit 1
fi

echo "Maven Central candidates: $(echo "$CANDIDATES" | wc -l)"
echo ""

# Step 2: Get the project's dependency tree (g:a:v of all deps)
echo "Resolving project dependency tree..."
PROJECT_DEPS=$(cd "$(dirname "$PROJECT_POM")" && mvn dependency:tree -DoutputType=text 2>/dev/null | \
    grep -oP '[a-zA-Z0-9._-]+:[a-zA-Z0-9._-]+:jar:[a-zA-Z0-9._-]+' | \
    awk -F: '{print $1":"$2":"$4}' | sort -u)

# Step 3: For each project dependency, check its POM for provided/optional deps
# that match one of our candidates
echo "Scanning dependency POMs for provided/optional references..."
echo ""

FOUND=""

while IFS= read -r dep; do
    DEP_G=$(echo "$dep" | cut -d: -f1)
    DEP_A=$(echo "$dep" | cut -d: -f2)
    DEP_V=$(echo "$dep" | cut -d: -f3)

    # Find the POM in local repo
    DEP_PATH=$(echo "$DEP_G" | tr '.' '/')
    POM="$HOME/.m2/repository/$DEP_PATH/$DEP_A/$DEP_V/$DEP_A-$DEP_V.pom"

    if [ ! -f "$POM" ]; then
        continue
    fi

    # Check if this POM references any of our candidates
    while IFS= read -r candidate; do
        CAND_G=$(echo "$candidate" | cut -d: -f1)
        CAND_A=$(echo "$candidate" | cut -d: -f2)

        # Check if the candidate appears as a dependency in this POM
        if grep -q "<artifactId>${CAND_A}</artifactId>" "$POM" 2>/dev/null; then
            # Get the version from the effective POM
            CAND_V=$(mvn help:effective-pom -f "$POM" 2>/dev/null | \
                python3 -c "
import sys, re
content = sys.stdin.read()
# Find dependency blocks containing our artifactId
pattern = r'<dependency>\s*<groupId>${CAND_G}</groupId>\s*<artifactId>${CAND_A}</artifactId>\s*<version>([^<]+)</version>'
m = re.search(pattern, content, re.DOTALL)
# Also try without groupId match (in case of different groupId)
if not m:
    pattern = r'<dependency>[^<]*<artifactId>${CAND_A}</artifactId>\s*<version>([^<]+)</version>'
    m = re.search(pattern, content, re.DOTALL)
if m:
    print(m.group(1))
" 2>/dev/null)

            if [ -n "$CAND_V" ]; then
                echo "  MATCH: ${CAND_G}:${CAND_A}:${CAND_V}"
                echo "    Referenced by: ${DEP_G}:${DEP_A}:${DEP_V}"
                G_PATH=$(echo "$CAND_G" | tr '.' '/')
                URL="https://repo1.maven.org/maven2/${G_PATH}/${CAND_A}/${CAND_V}/${CAND_A}-${CAND_V}.jar"
                echo "    URL: $URL"
                echo ""
                if [ -z "$FOUND" ]; then
                    FOUND="${CAND_G}:${CAND_A}:${CAND_V}|${URL}|${CAND_A}-${CAND_V}.jar"
                fi
            fi
        fi
    done <<< "$CANDIDATES"
done <<< "$PROJECT_DEPS"

# Step 4: If no match in dep POMs, check the project's own dependencyManagement/BOM
if [ -z "$FOUND" ]; then
    echo "No match in dependency POMs. Checking project's effective POM..."
    EFFECTIVE=$(cd "$(dirname "$PROJECT_POM")" && mvn help:effective-pom 2>/dev/null)

    while IFS= read -r candidate; do
        CAND_G=$(echo "$candidate" | cut -d: -f1)
        CAND_A=$(echo "$candidate" | cut -d: -f2)

        CAND_V=$(echo "$EFFECTIVE" | python3 -c "
import sys, re
content = sys.stdin.read()
pattern = r'<groupId>${CAND_G}</groupId>\s*<artifactId>${CAND_A}</artifactId>\s*<version>([^<]+)</version>'
m = re.search(pattern, content, re.DOTALL)
if m:
    print(m.group(1))
" 2>/dev/null)

        if [ -n "$CAND_V" ]; then
            G_PATH=$(echo "$CAND_G" | tr '.' '/')
            URL="https://repo1.maven.org/maven2/${G_PATH}/${CAND_A}/${CAND_V}/${CAND_A}-${CAND_V}.jar"
            echo "  MATCH (from BOM): ${CAND_G}:${CAND_A}:${CAND_V}"
            echo "    URL: $URL"
            echo ""
            if [ -z "$FOUND" ]; then
                FOUND="${CAND_G}:${CAND_A}:${CAND_V}|${URL}|${CAND_A}-${CAND_V}.jar"
            fi
        fi
    done <<< "$CANDIDATES"
fi

if [ -z "$FOUND" ]; then
    echo "No matching version found in project dependencies."
    echo "Maven Central candidates (latest versions):"
    echo "$CANDIDATES" | head -5 | while read -r c; do
        echo "  $c"
    done
    exit 1
fi

# Step 5: Download if requested
if [ -n "$OPT_DIR" ]; then
    URL=$(echo "$FOUND" | cut -d'|' -f2)
    FILENAME=$(echo "$FOUND" | cut -d'|' -f3)

    mkdir -p "$OPT_DIR"
    echo "Downloading to $OPT_DIR/$FILENAME ..."
    curl -sL -o "$OPT_DIR/$FILENAME" "$URL"

    SIZE=$(stat -c%s "$OPT_DIR/$FILENAME" 2>/dev/null || echo 0)
    if [ "$SIZE" -lt 1000 ]; then
        echo "WARNING: Download failed ($SIZE bytes)"
        rm -f "$OPT_DIR/$FILENAME"
        exit 1
    fi

    echo "Downloaded: $OPT_DIR/$FILENAME ($SIZE bytes)"
    if jar tf "$OPT_DIR/$FILENAME" 2>/dev/null | grep -q "$(echo "$CLASS_NAME" | tr '.' '/').class"; then
        echo "✓ Class $CLASS_NAME found in jar"
    else
        echo "⚠ Class $CLASS_NAME NOT found in jar"
    fi
fi
