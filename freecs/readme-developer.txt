This source-distribution uses apache-ant to build all sources and distributions.

You will need a Java-SDK (1.4+) to build the sources (get it from http://java.sun.com).

(Please substitute [packagepath] with the path where you extracted freecs to. If target
is not given, a list of available target's will be displayed (e.g. jar)
cd [packagepath]/build
java -classpath ant.jar org.apache.tools.ant.Main -buildfile build.xml [target]

The following will generate a jar-file (freecs-yyymmdd.jar) from the current sources
and places it inside the lib-folder.
java -classpath ant.jar org.apache.tools.ant.Main -buildfile build.xml jar

All classes will be found in the classes-folder and the jar-file in the lib-folder.