all: clean build native run

clean: clean-reports
	rm -f timezones
	rm -f *.class

clean-reports:
	rm -drf reports

build:
	javac Timezones.java

# Works on graalvm 20.1.0
native:
	native-image Timezones

# Works on graalvm 20.1.0 but shows:
# -H:IncludeAllTimeZones and -H:IncludeTimeZones are now deprecated. Native-image includes all timezonesby default.
native-include-timezones:
	native-image -H:-IncludeAllTimeZones Timezones

run:
	./timezones
