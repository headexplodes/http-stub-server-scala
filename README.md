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

