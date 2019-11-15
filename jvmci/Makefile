clean:
	rm *.class || echo "class file does not exist"

build:
	javac \
	--add-exports=jdk.internal.vm.ci/jdk.vm.ci.services=ALL-UNNAMED \
	--add-modules=jdk.internal.vm.ci \
	JvmCiServices.java

run:
	java \
	-XX:+UnlockExperimentalVMOptions \
	-XX:+EnableJVMCI \
	--add-exports=jdk.internal.vm.ci/jdk.vm.ci.services=ALL-UNNAMED \
	JvmCiServices
