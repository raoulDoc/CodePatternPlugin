Javac plugins (introduced in JDK8) let you inject new phases to javac without modifying the compiler itself.

This project shows an example of a Javac plugin that finds the following code pattern:

`
expr.get(key) == null // where expr is a subtype of java.util.Map
`

= Requirements

* JDK 8

= Instructions
`
$ mvn compile
$ mvn jar:jar
$ javac -processorpath target/javac-compiler-plugin-example-1.0.jar -Xplugin:CodePatternPlugin samples/Test.java 
Running!
Found Match at line: 8 in samples/Test.java
`

= Additional notes

* Project tested under Mac OSX
* The name of the plugin is set up under META-INF/services/com.sun.source.util.Plugin
* API: https://docs.oracle.com/javase/8/docs/jdk/api/javac/tree/com/sun/source/util/Plugin.html