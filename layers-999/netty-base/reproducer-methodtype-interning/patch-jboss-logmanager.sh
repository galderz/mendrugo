#!/usr/bin/env bash
set -eux

#
# This script patches jboss-logmanager 3.2.2.Final to fix the silent
# exception swallowing in ExtHandler.publishToNestedHandlers().
#
# The bug: catch(Throwable ignored) {} silently discards Error subclasses
# (e.g. UnsupportedFeatureError) thrown by nested handlers during publish().
#
# The fix: report the error via ErrorManager (matching LoggerNode.publish()
# behavior) and fall back to StandardOutputStreams.printError() if that fails.
#
# After patching, it rebuilds the jar and installs it in the local maven
# cache, then rebuilds the affected Quarkus modules.
#

LOGMANAGER_VERSION=3.2.2.Final
LOGMANAGER_GROUP=org.jboss.logmanager
LOGMANAGER_ARTIFACT=jboss-logmanager
WORKDIR=/tmp/patch-jboss-logmanager
SOURCES_URL="https://repo1.maven.org/maven2/org/jboss/logmanager/jboss-logmanager/${LOGMANAGER_VERSION}/jboss-logmanager-${LOGMANAGER_VERSION}-sources.jar"

QUARKUS_HOME="${QUARKUS_HOME:-$HOME/quarkus}"

echo "=== Setting up workspace ==="
rm -rf "$WORKDIR"
mkdir -p "$WORKDIR"

echo "=== Downloading sources ==="
curl -sL "$SOURCES_URL" -o "$WORKDIR/sources.jar"

echo "=== Extracting ExtHandler.java ==="
cd "$WORKDIR"
jar xf sources.jar org/jboss/logmanager/ExtHandler.java

echo "=== Patching ExtHandler.publishToNestedHandlers() ==="
# Replace the silent catch(Throwable ignored) {} with proper error reporting
cat > "$WORKDIR/fix.patch" << 'PATCH'
--- a/org/jboss/logmanager/ExtHandler.java
+++ b/org/jboss/logmanager/ExtHandler.java
@@ -1,5 +1,8 @@
 
 package org.jboss.logmanager;
+import java.lang.reflect.UndeclaredThrowableException;
+import java.security.AccessController;
+import java.security.PrivilegedAction;
 
 import java.io.Flushable;
 import java.io.UnsupportedEncodingException;
PATCH

cd "$WORKDIR"
patch -p1 < fix.patch || true  # may fail if imports already exist, that's ok

# Now do the main fix - replace the catch(Throwable ignored) block
python3 << 'PYTHON'
import re

with open("org/jboss/logmanager/ExtHandler.java", "r") as f:
    content = f.read()

old = """            } catch (Exception e) {
                    reportError(handler, "Nested handler publication threw an exception", e, ErrorManager.WRITE_FAILURE);
                } catch (Throwable ignored) {
                }"""

new = """            } catch (Exception e) {
                    reportError(handler, "Nested handler publication threw an exception", e, ErrorManager.WRITE_FAILURE);
                } catch (Throwable t) {
                    // Do not silently swallow Errors - report them like LoggerNode.publish() does
                    final Handler h = handler;
                    ErrorManager errorManager = AccessController.doPrivileged(
                        (PrivilegedAction<ErrorManager>) h::getErrorManager);
                    if (errorManager != null) {
                        Exception e;
                        if (t instanceof Exception) {
                            e = (Exception) t;
                        } else {
                            e = new UndeclaredThrowableException(t);
                        }
                        try {
                            errorManager.error("Nested handler publication threw an error", e, ErrorManager.WRITE_FAILURE);
                        } catch (Throwable t2) {
                            StandardOutputStreams.printError(t2, "Handler.reportError caught an exception");
                        }
                    }
                }"""

if old not in content:
    print("ERROR: Could not find the code to patch!")
    print("Looking for:")
    print(old[:80])
    exit(1)

content = content.replace(old, new)

with open("org/jboss/logmanager/ExtHandler.java", "w") as f:
    f.write(content)

print("ExtHandler.java patched successfully")
PYTHON

echo "=== Verifying patch ==="
grep -A15 "catch (Throwable" "$WORKDIR/org/jboss/logmanager/ExtHandler.java" | head -20

echo "=== Compiling patched ExtHandler.java ==="
LOGMANAGER_JAR="$HOME/.m2/repository/org/jboss/logmanager/jboss-logmanager/${LOGMANAGER_VERSION}/jboss-logmanager-${LOGMANAGER_VERSION}.jar"
if [ ! -f "$LOGMANAGER_JAR" ]; then
    echo "ERROR: Cannot find $LOGMANAGER_JAR"
    exit 1
fi

# Extract the original jar to get all classes
mkdir -p "$WORKDIR/classes"
cd "$WORKDIR/classes"
jar xf "$LOGMANAGER_JAR"
# Remove module-info to avoid module resolution issues during compilation
rm -f module-info.class

# Compile the patched source against the extracted classes (no module path needed)
javac -cp "$WORKDIR/classes" \
    -d "$WORKDIR/classes" \
    "$WORKDIR/org/jboss/logmanager/ExtHandler.java"

echo "=== Rebuilding jar ==="
cd "$WORKDIR/classes"
jar cf "$WORKDIR/jboss-logmanager-${LOGMANAGER_VERSION}.jar" .

echo "=== Installing patched jar in local maven cache ==="
cp "$LOGMANAGER_JAR" "${LOGMANAGER_JAR}.orig"
cp "$WORKDIR/jboss-logmanager-${LOGMANAGER_VERSION}.jar" "$LOGMANAGER_JAR"

echo "=== Rebuilding Quarkus core runtime ==="
cd "$QUARKUS_HOME"
./mvnw -DskipTests -f core/runtime install

echo "=== Done ==="
echo "Patched jboss-logmanager installed. Original backed up at: ${LOGMANAGER_JAR}.orig"
echo "To restore: cp ${LOGMANAGER_JAR}.orig $LOGMANAGER_JAR"
echo ""
echo "Now rebuild the getting-started app and both layers to test."
