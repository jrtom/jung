## JUNG: The Java Universal Network/Graph Framework 

[![Build Status](https://travis-ci.org/jrtom/jung.svg?branch=master)](https://travis-ci.org/jrtom/jung)

JUNG is a software library that provides a common and extendible language for the modeling, analysis, and visualization of
data that can be represented as a graph or network.  Its basis in Java allows JUNG-based applications to make use of the
extensive built-in capabilities of the Java API, as well as those of other existing third-party Java libraries.

[**JUNG Website**](http://jrtom.github.io/jung/)

### Latest Release

The most recent version of JUNG is [version 2.1](https://github.com/jrtom/jung/releases/tag/jung-2.1), released 18 March 2016.
*   [Javadoc](http://jrtom.github.io/jung/javadoc/index.html)

To add a dependency on this release of JUNG using Maven, use the following for each JUNG subpackage that you need:

```xml
<dependency>
  <groupId>net.sf.jung</groupId>
  <artifactId>jung-[subpackage]</artifactId>
  <version>2.1</version>
</dependency>
```

where `jung-[subpackage]` may be `jung-api`, `jung-graph-impl`, `jung-io`, `jung-algorithms`, `jung-visualization`, or `jung-samples`.


### Snapshots

Snapshots of JUNG built from the `master` branch are available through Maven using version `2.2-SNAPSHOT`.

### Links

* [GitHub project](https://github.com/jrtom/jung)
* [Issue tracker: report a defect or make a feature request](https://github.com/jrtom/jung/issues/new)
* [StackOverflow: Ask "how-to" and "why-didn't-it-work" questions](https://stackoverflow.com/questions/ask?tags=jung+java)

### Contributions

JUNG is currently administered primarily by @jrtom, one of the original co-creators of the JUNG project.  However, this is not precisely his day job.  :)

Bug fixes (with tests) are appreciated and will generally be acted upon pretty quickly if the fix is a clear win.  

If you'd like to add a feature, or suggest a way that things could be done better, more cleanly, or more efficiently, we really appreciate it, we encourage you to open an issue, and you're welcome to create a branch to show off a proof of concept.

However, at the moment we're largely focused on some big architectural changes that are going to touch essentially everything in JUNG.  Once those changes land, we'll have more time and energy available to consider other changes, and this policy itself will change.
