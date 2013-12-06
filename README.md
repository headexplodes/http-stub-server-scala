Generic HTTP Stub Server (Scala)
================================

[![Build Status](https://travis-ci.org/headexplodes/http-stub-server-scala.png)](https://travis-ci.org/headexplodes/http-stub-server-scala)

This an implementation of the [Generic HTTP Stub](http://github.com/sensis/http-stub-server) server in Scala with Unfiltered. This project is mainly for my own amusement. 

It currently passes the full functional test suite includes as part of the original Java implementation.

Running
-------

* Install [SBT](http://www.scala-sbt.org/)
* Start the standalone server:

```
$ sbt "project standalone" "run 8080"
```

Release Notes
-------------

### Version 1.0 (2013-10-13)

Initial release. 

All tests in original test suite (from the Java version) are now passing.

Known Issues
------------

### Rhino and Java 7

There seems to be problems with Java 7 and the Rhino JavaScript engine not being found in some circumstances, possibly in combination with a recent change to sbt.

A work-around is adding the following file to your project:

    META-INF/services/javax.script.ScriptEngineFactory
    
with the single line contents:

    com.sun.script.javascript.RhinoScriptEngineFactory

Hopefully this will be resolved more elegantly in the future.

