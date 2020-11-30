main-write = TestImageIOWrite
source-write = $(main-write).java
exec-write = $(shell echo $(main-write) | tr A-Z a-z)
folder-write = write

main-read = TestImageIORead
source-read = $(main-read).java
exec-read = $(shell echo $(main-read) | tr A-Z a-z)
folder-read = read

all: build-write native-config-write native-write run-write build-read native-config-read native-read run-read

build-write:
	$(call build,$(folder-write),$(source-write))

build-read:
	$(call build,$(folder-read),$(source-read))

define build
cd ${1} && \
    javac -d . -classpath . ${2}
endef

native-config-write:
	$(call native-config,$(folder-write),$(main-write))

native-config-read:
	$(call native-config,$(folder-read),$(main-read))

define native-config
mkdir -p ${1}/META-INF/native-image
cd ${1} && \
    java -agentlib:native-image-agent=config-output-dir=META-INF/native-image ${2}
endef

native-write:
	$(call native,$(folder-write),$(main-write),$(exec-write))

native-read:
	$(call native,$(folder-read),$(main-read),$(exec-read))

define native
cd ${1} && \
    native-image ${2} ${3}
endef

run-write:
	cd $(folder-write) && ./$(exec-write)

run-read:
	cd $(folder-read) && ./$(exec-read)

clean:
	$(call clean,$(folder-write),$(exec-write))
	$(call clean,$(folder-read),$(exec-read))

define clean
rm -r ${1}/*.class || true
rm -r ${1}/META-INF || true
rm -f ${1}/test.* || true
rm -f ${1}/{2} || true
endef