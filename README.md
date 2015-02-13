
```
mvn clean install -U
or
mvn install -Dmaven.test.skip=true 2>&1 | less

To run, which will do the linking with the required libs in ~/.m2

mvn exec:java -Dexec.mainClass=org.geotools.tutorial.quickstart.Quickstart


saves having to use package plugin and specify all jars...

```

