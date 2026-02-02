# Java Module Build Makefile

# Module name
MODULE_NAME = dummy.module

# Directories
SRC_DIR = src
BUILD_DIR = target
DIST_DIR = target

# Java compiler
JAVAC = $(JAVA_HOME)/bin/javac
JAR = $(JAVA_HOME)/bin/jar

# Source files
MODULE_INFO = $(SRC_DIR)/module-info.java
JAVA_FILES = $(shell find $(SRC_DIR) -name "*.java")

# Output
MODULE_JAR = $(DIST_DIR)/$(MODULE_NAME).jar

.PHONY: all clean compile jar

all: jar

# Compile Java sources
compile:
	@mkdir -p $(BUILD_DIR)
	$(JAVAC) -d $(BUILD_DIR) $(JAVA_FILES)

# Create module jar
jar: compile
	@mkdir -p $(DIST_DIR)
	$(JAR) --create --file=$(MODULE_JAR) \
		--main-class=dummy.Main \
		-C $(BUILD_DIR) .
	@echo "Module JAR created: $(MODULE_JAR)"

# Clean build artifacts
clean:
	rm -rf $(BUILD_DIR) $(DIST_DIR)

# Run the module
run: jar
	$(JAVA_HOME)/bin/java --module-path $(DIST_DIR) --module $(MODULE_NAME)/dummy.Main
