# Introduction #

The versions of JUNG hosted on the original JUNG site (SourceForge), ended with 2.x; the Google Code-hosted version starts with 3.0.  This is largely (at the moment) due to the decision to migrate from the LarvaLabs generics port of the Apache Commons-Collections libraries to using the Guava libraries (http://code.google.com/p/guava-libraries/) for various utility interfaces and classes.  This document describes the changes that were made, so that users with JUNG 2.0 code can port their code to using the JUNG 3.0 libraries.

```
Predicate.evaluate() -> Predicate.apply()

Factory.create() -> Supplier.get()

Transformer.transform() -> Function.apply()

Collections.unmodifiableCollection(Collection) -> ImmutableList.Builder<T>().addAll(collection).build();
```
(see OrderedKAryTree: not convinced this is the best way.)

```
CollectionUtils.subtract(Collection, Collection) -> Sets.difference(Set, Set)
```
(currently using newArrayList and removeAll in algorithms.blockmodel.StructurallyEquivalent, which I suspect is suboptimal.)

```
Buffer and UnboundedFifoBuffer -> Queue and LinkedList 
```
(not GC classes, but Java 5)
(WeakComponentClusterer, EdmondsKarpMaxFlow)

```
ChainedTransformer -> Functions.compose()

CloneTransformer -> anonymous Function returning clone of input

BidiMap -> BiMap
```