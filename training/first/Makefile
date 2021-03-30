executable := hello
jar := $(executable).jar
source := $(executable).java

all: $(executable)

$(executable): $(jar)
	native-image -jar $(jar) $(executable)

$(jar): $(source)
	jbang export portable $(source)

$(source):
	jbang init --template=hello $(source)

clean:
	rm -f *.jar
	rm -drf libs
	rm -f *.java
	rm -f hello
.PHONY: clean