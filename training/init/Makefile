executable := init
jar := $(executable).jar
source := $(executable).java

all: $(jar)

$(executable): $(jar)
	native-image -jar $(jar) $(executable)

$(jar): $(source)
	jbang export portable --force $(source)

intellij-idea:
	jbang edit --open=idea $(source)
.PHONY: intellij-idea

clean:
	rm -f *.jar
	rm -drf libs
	rm -f $(executable)
.PHONY: clean
