Generic HTTP Stub Server (Scala)
================================

[![Build Status](https://travis-ci.org/headexplodes/http-stub-server-scala.png)](https://travis-ci.org/headexplodes/http-stub-server-scala)


The Generic HTTP Stub Server (a.k.a. 'Stubby') is a protocol and server implementation for stubbing HTTP interactions, mainly aimed at automated acceptance testing. There's also some example client code in various languages.

This an implementation of the [original Java version](http://github.com/sensis/http-stub-server) in Scala with Unfiltered.

It currently passes the full functional test suite included as part of the original Java implementation.

Some documentation to get started:

* [Usage Scenarios](Usage Scenarios)
* [API Documentation](API Documentation)

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

