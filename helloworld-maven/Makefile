native_image += native-image
native_image += -jar
native_image += target/helloworld-0.1.jar

# e.g. NATIVE_OPTS=--gc=epsilon
ifdef NATIVE_OPTS
  native_image += $(NATIVE_OPTS)
endif

run: native
	target/helloworld
.PHONY: run

native: build
	$(native_image) target/helloworld

build:
	mvn package

clean:
	mvn clean
.PHONY: clean
