## JUNG: The Java Universal Network/Graph Framework 

[![Build Status](https://travis-ci.org/jrtom/jung.svg?branch=master)](https://travis-ci.org/jrtom/jung)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sf.jung/jung-algorithms/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sf.jung/jung-algorithms)

JUNG is a software library that provides a common and extendible language for the modeling, analysis, and visualization of
data that can be represented as a graph or network.  Its basis in Java allows JUNG-based applications to make use of the
extensive built-in capabilities of the Java API, as well as those of other existing third-party Java libraries.

[**JUNG Website**](http://jrtom.github.io/jung/)

### Latest Release

The most recent version of JUNG is [version 2.1](https://github.com/jrtom/jung/releases/tag/jung-2.1), released 18 March 2016.
*   [Javadoc](http://jrtom.github.io/jung/javadoc/index.html)
*   [Maven Search Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22net.sf.jung%22%20AND%20v%3A%222.1%22%20AND%20(a%3A%22jung-api%22%20OR%20a%3A%22jung-graph-impl%22%20OR%20a%3A%22jung-visualization%22%20OR%20a%3A%22jung-algorithms%22%20OR%20a%3A%22jung-samples%22%20OR%20a%3A%22jung-io%22))
    *   `jung-api`: [jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-api/2.1/jung-api-2.1.jar), [source jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-api/2.1/jung-api-2.1-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-api/2.1/jung-api-2.1-javadoc.jar)
    *   `jung-graph-impl`: [jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-graph-impl/2.1/jung-graph-impl-2.1.jar), [source jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-graph-impl/2.1/jung-graph-impl-2.1-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-graph-impl/2.1/jung-graph-impl-2.1-javadoc.jar)
    *   `jung-algorithms`: [jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-algorithms/2.1/jung-algorithms-2.1.jar), [source jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-algorithms/2.1/jung-algorithms-2.1-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-algorithms/2.1/jung-algorithms-2.1-javadoc.jar)
    *   `jung-io`: [jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-io/2.1/jung-io-2.1.jar), [source jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-io/2.1/jung-io-2.1-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-io/2.1/jung-io-2.1-javadoc.jar)
    *   `jung-visualization`: [jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-visualization/2.1/jung-visualization-2.1.jar), [source jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-visualization/2.1/jung-visualization-2.1-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-visualization/2.1/jung-visualization-2.1-javadoc.jar)
    *   `jung-samples`: [jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-samples/2.1/jung-samples-2.1.jar), [source jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-samples/2.1/jung-samples-2.1-sources.jar), [documentation jar](http://search.maven.org/remotecontent?filepath=net/sf/jung/jung-samples/2.1/jung-samples-2.1-javadoc.jar)

To add a dependency on this release of JUNG using Maven, use the following for each JUNG subpackage that you need:

```xml
<dependency>
  <groupId>net.sf.jung</groupId>
  <artifactId>jung-[subpackage]</artifactId>
  <version>2.1</version>
</dependency>
```

### Snapshots

Snapshots of JUNG built from the `master` branch are available through Maven using version `2.2-SNAPSHOT`.

### Links

* [GitHub project](https://github.com/jrtom/jung)
* [Issue tracker: report a defect or make a feature request](https://github.com/jrtom/jung/issues/new)
* [StackOverflow: Ask "how-to" and "why-didn't-it-work" questions](https://stackoverflow.com/questions/ask?tags=jung+java)

### Contributions

JUNG is currently administered primarily by @jrtom, one of the original co-creators of the JUNG project.

Bug fixes (with tests) are appreciated and will generally be acted upon pretty quickly if the fix is a clear win.  

If you'd like to add a feature, or suggest a way that things could be done better, more cleanly, or more efficiently, we really appreciate it, we encourage you to [open an issue](https://github.com/jrtom/jung/issues/new), and you're welcome to make a pull request to show off a proof of concept.

However, at the moment we're largely focused on some big architectural changes that are going to touch essentially everything in JUNG.  Once those changes land, we'll have more time and energy available to consider other changes.
