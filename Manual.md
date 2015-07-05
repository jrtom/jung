<font size='5'><b>Table of Contents</b></font>




---


# Preface #

This manual is for JUNG 3.0 and later.  JUNG 3.0 currently differs from JUNG 2.0 primarily in its use of the Guava libraries.  If you want information about JUNG 2.0 or earlier, see the old JUNG website at http://jung.sf.net.

In case this wasn't obvious, this manual is a work in progress.  If you notice something missing that you need soon, or if you think you've spotted a mistake or a bug, please contact us and we'll do our best to fix it.


---


# Introduction and Overview #

JUNG — the Java Universal Network/Graph Framework--is a software library that provides a common and extendible language for the modeling, analysis, and visualization of data that can be represented as a graph or network. It is written in Java, which allows JUNG-based applications to make use of the extensive built-in capabilities of the Java API, as well as those of other existing third-party Java libraries.

The JUNG architecture is designed to support a variety of representations of entities and their relations, such as directed and undirected graphs, multi-modal graphs, graphs with parallel edges, and hypergraphs. It provides a mechanism for annotating graphs, entities, and relations with metadata. This facilitates the creation of analytic tools for complex data sets that can examine the relations between entities as well as the metadata attached to each entity and relation.

JUNG includes implementations of a number of algorithms from graph theory, data mining, and social network analysis, including clustering, filtering, random graph generation, blockmodeling, calculation of network distances and flows, and a wide variety of metrics (PageRank, HITS, betweenness, closeness, etc.).

JUNG also provides a visualization framework that makes it easy to construct tools for the interactive exploration of network data. Users can use one of the layout algorithms provided, or use the framework to create their own custom layouts.

As an open-source library, JUNG provides a common framework for graph/network analysis and visualization. We hope that JUNG will make it easier for those who work with graph and network data to make use of one anothers' development efforts, and thus avoid continually re-inventing the wheel.


---


# Getting Started #

## Dependencies ##

JUNG requires the following:
  * Java 1.5 or later
  * The Guava libraries: http://code.google.com/p/guava-libraries
We will refer to this as Guava hereafter.
  * The CERN Colt libraries (for some algorithms and I/O operations): http://www-itg.lbl.gov/~hoschek/colt/
This is referred to later as Colt.

These third-party libraries are included in the JUNG distribution.


---


# Graphs, Vertices, and Edges #

The basic JUNG type is the graph.  Graphs are defined by the interfaces `Hypergraph`, `Graph`, `Forest`, `Tree`, and `KPartiteGraph`.

## Vertex and Edge Types and Identities ##

JUNG graphs are analogous to Java collections (such as `List`, `Set`, `Map`, and so on).  Just as collections may specify the type of their elements in the declaration (e.g., `Set<Integer>` or `Map<String, YourClass>`), JUNG graph declarations may specify the type of each of their element categories, that is, vertices and edges.  The graph implementation is generally assumed to be responsible for maintaining the topology of the graph (how graphs and edges are connected to each other); the vertex and edge objects are essentially treated as keys into the graph internal data structure.  This has a couple of practical consequences:

  1. Vertex and edge objects must be unique to a graph: there cannot be two vertices, or two edges, such that `vertex1.equals(vertex2)`, or `edge1.equals(edge2)`.  In this sense the vertex and edge collections have the `Set` semantics, although the internal implementations need not use `Set` and they are not exposed as `Set`s.
  1. Vertex and edge objects can be elements of multiple graphs.

This design is one of the major departures from JUNG 1.x, in which vertices and edges (a) were required to implement specific interfaces, (b) maintained most of the graph topology information, and consequently (c) could inhabit only one graph.  This set up complicated dependencies between graph, vertex, and edge types that no longer exist in the post-1.x JUNG API.

If performance (speed, space, or both) is critical, then you may be able to increase the performance by creating a graph implementation that depends on specific vertex and edge types.


---


# User Data and `Function` #

Many of JUNG's methods require the user to specify an association between a graph element (vertex or edge) and data of some sort: label text, edge weight, color, etc.  By convention this is generally done via a Guava `Function`.

`Function<F,T>` is an interface with a single method `apply(F input)` that returns an object of the output type (`T`) for each input.  This essentially defines a relationship between elements of the input and output types.  In a sense, it's something like the Java `Map` interface, except that it's much more lightweight and is read-only.

There are a few different ways to write one of these `Function`s.  This applies to any place that you're asked to provide a `Function`.  Some of them use static utility methods from the corresponding `Functions` class.

## constant value ##
`Functions.constant(value)`
returns a `Function` that returns `value` for any input.   This is useful for situations in which you're asked for a `Function` but in fact all elements should get the same value (e.g. providing an edge weight when it's an unweighted graph).

## `Map`-backed ##
This can be done either with a new map or based on an existing `Map`.  EIther way, once you have your `Map` you call
`Functions.forMap(map)`
There is also hybrid version you can call if you want to use a `Map` but default to a value when the `Map` contains no entry for the specified input value:
`Functions.forMap(map, default)`
This is useful when you have about as many distinct values as elements, or when there's no obvious pattern that relates elements to values/outputs.

### new `Map` ###
For each element, you create a (element, value) pair in a Map.  If the values don't relate to anything else, this may be appropriate...although that's probably pretty rare.

### existing `Map` ###
Often you'll have an existing lookup table that does what you need it to (see the note below); no need to create a new one.

## element instance variable-backed ##
This is much the same as `Map`-backed but with a different storage model.  It assumes that you have (graph) elements that have instance variables that hold the values.

## on-the-fly ##
`Function.apply` can call a method every time it is invoked to fetch the appropriate return value (calculation, status report, etc.).

## combinations ##
For example, functions that use picked state from the UI information to determine which of two colors to use.

Note that in any of these cases, the function can take a process (map, instance variable, function call) which outputs something other than what you want (e.g., a floating-point value) and translate it to a value of the appropriate type (e.g., a `Paint`).  For example, let's suppose that you want to paint vertices red if they have high PageRank, yellow if they have moderate PageRank, and black if they have low PageRank.  You can easily construct a `Function` class that takes the PageRank data (perhaps itself provided by a `Function` which you provide to the constructor), figures out which of three intervals you want, and then outputs an appropriate color when you give it a vertex.  Taking this a step further, it would even be pretty easy to write a threshold-based general `Function` that would take a `Function` from threshold values to colors as its constructor parameter.

This is really the key insight about using `Function` in JUNG: we're trying to use them in a way that means that you have as little work to do as possible in order to, say, run an algorithm where edge weights are based on the number of papers coauthored by the incident vertices (representing authors), or create a visualization for which vertex color is a function of activity level.

# Input and Output #

# Algorithms #

# Visualization #

# Sample Code #

# Appendix: How to Build JUNG #

This is a brief intro to building JUNG jars with maven2 (the build system that JUNG currently uses).

First, ensure that you have a JDK of at least version 1.5: JUNG 2.0+ requires Java 1.5+.  Ensure that your JAVA\_HOME variable is set to the location of the JDK. On a Windows platform, you may have a separate JRE (Java Runtime Environment) and JDK (Java Development Kit). The JRE has no capability to compile Java source files, so you must have a JDK installed. If your JAVA\_HOME variable is set to the location of the JRE, and not the location of the JDK, you will be unable to compile.

## Get Maven ##

Download and install maven2 from maven.apache.org:

http://maven.apache.org/download.html

At time of writing (early December 2009), the latest version was maven-2.2.1.

Install the downloaded maven2 (there are installation instructions on the Maven website).

Follow the installation instructions and confirm a successful installation by typing 'mvn --version' in a command terminal window.

## Get JUNG ##

Get the JUNG code from CVS:

If you are a developer, do this:

> `export CVS_RSH=ssh`

> `cvs -z3 -d:ext:your-login@jung.cvs.sourceforge.net:/cvsroot/jung co -P jung2`

If you are a user, do this:

> `cvs -z3 -d:pserver:anonymous@jung.cvs.sourceforge.net:/cvsroot/jung co -P jung2`

If you're unable (or unwilling) to use CVS from a command-line console, see the Eclipse-based instructions below.


## Build JUNG ##

> `cd jung2`
> `mvn install`

This should build the sub-projects and run unit tests.  During the build process, maven downloads code it needs from maven repositories. The code is cached in your local repository that maven creates in your home directory ($HOME/.m2/repository). If the download of something is interrupted, the build may fail.  If so, just run it again (and again) and it should eventually succeed.  Once all the files are cached in your local maven repository, the build process will be faster.


## Prepare JUNG for Eclipse ##

(This step is only relevant if you use Eclipse as your IDE, of course.)

To prepare jung2 for eclipse, run the following maven command:

> `mvn eclipse:eclipse`

which will generate the `.classpath` and `.project` files for eclipse.

The `.classpath` file will make reference to a `M2_REPO` variable, which you must define in eclipse, so that `M2_REPO` points to your local repository. You can do that in eclipse by bringing up project properties and adding the variable `M2_HOME`, or you can run the following command to have maven set the variable for you:

> `mvn -Declipse.workspace=<path-to-eclipse-workspace> eclipse:add-maven-repo`

If that does not work, you'll need to open one of the projects properties and use the 'add variables' button in the 'libraries' tab.

To load JUNG in eclipse, you need to overcome an eclipse limitation: Eclipse projects cannot contain subprojects.  (JUNG currently contains 6 sub-projects.)
The common work-around is to make eclipse think that each sub-project is a top-level project.

The most common way to proceed is as follows:

Add each subproject (jung-api, jung-graph-impl, jung-visualization, jung-algorithms, jung-samples, jung-io) as a top-level project in eclipse, each with its own classpath dependencies.

One approach is to use the eclipse feature for importing existing projects AFTER `mvn eclipse:eclipse` has been run as shown above.  Simply point the eclipse import project file chooser to the jung2 directory, then check off the list of subprojects that are shown. You can import all of the subprojects at once this way.

Another approach is to manually add each subproject as follows:

In the 'New Project' dialog, select 'Java Project', then 'Create project from existing source'. Create the new project to point to where you downloaded jung2 and its subprojects.  For example, you would create a new project from the existing source in '/where/it/is/jung2/jung-api' and name that project 'jung-api'.

Because you previously ran mvn eclipse:eclipse at the jung2 directory level, then the projects will already reference the other projects they depend on (instead of the jar from those projects).

You do not want to use jung2 (the parent project) as the eclipse project, as each eclipse project can have only one classpath, and you would then have difficulty maintaining the correct dependencies between the sub-projects.


### Checking JUNG Out Using Eclipse ###

If you are unable to use cvs from the command prompt, you may check out jung2 using eclipse, HOWEVER, because of the above stated limitation
that eclipse cannot manage nested projects, you must use the following trick:

Create a new workspace that you will be using only to check out the project. You will not be using this workspace to work on the project.  Let's call the new workspace $HOME/checkout\_base.  From that workspace, use eclipse to check out jung2 from cvs.  Next, open a command prompt console and change directory to the newly created $HOME/checkout\_base/jung2.  Execute this command:

> mvn eclipse:eclipse

That will build the eclipse artifacts.

Next, change eclipse to point to a different workspace, one that you
will actually be working in. Use the above instructions to import the
jung2 subprojects from $HOME/checkout\_base/jung2 into your real
workspace.


## Running JUNG Sample Code ##

Once you have built everything (preceding instructions), here is a straightforward way to run some demos from the command line:

(NOTE: you may need to change the version part of the jar names below. It could be jung-samples-2.0.1.jar for example. Look at the actual jar file names to see.)

`cd jung2/jung-samples/target`

`tar xvf jung-samples-2.0-dependencies.tar`

`java -cp jung-samples-2.0.jar samples.graph.VertexImageShaperDemo`

`java -cp jung-samples-2.0.jar samples.graph.SatelliteViewDemo`

`java -cp jung-samples-2.0.jar samples.graph.ShowLayouts`


The jung-samples-dependencies.tar file contains all of the jar dependencies for the jung-samples project. It was created as part of the maven build process.