## Introduction
This repository contains an implementation of a Parameterized Approximation Scheme (PAS) for the Steiner tree problem for integer edge-weighted undirected graphs. The work is based on the recent paper [*Parameterized Approximation Schemes for Steiner Trees with Small Number of Steiner Vertices*](https://arxiv.org/abs/1710.00668.) by *Pavel Dvořák, Andreas Emil Feldmann, Dušan Knop, Tomáš Masařík, Tomáš Toufar and Pavel Veselý*

### Downloads

Download sample inputs [here](https://drive.google.com/open?id=1v4qmUbjYRZlq5e6-uLp6RkYJWwRRAbjw) and a pre-packaged jar file [here](https://drive.google.com/open?id=1WGDzzpDvyD-5UVhOykd7FwjyWoQfImuf).

### Usage
Run executable jar with the following java command:
**`java -jar steiner-tree-pas.jar -in <input filename>.gr`**

List of parameters:

`-ui [input|output]` - display input or output file in interactive ui

`-silent` - mute standard output message

`-long` - more detailed standard output

`-s p` - run FPT algorithm on at least `p` Steiner vertices

`-t r` - run FPT algorithm on at least `r` Terminals

`-a [0..10]` - instead of using options`-s` or `-t` we may specify a number between 0 and 10 as a rough indication of the accuracy with which we want to run the approximation scheme ( 0 being the least and 10 the most accurate)

`-c` - continuously find better approximations, until the optimum is found

`-out` - write computed Steiner tree into **`<input filename>-tree.gr`**

`-slow` - use slower, but possibly more accurate reduction method

#### Examples:
1. **`java -jar steiner-tree-pas.jar -in instances/instance001.gr -ui input`**
 * Display instance001.gr in the ui

2. **`java -jar steiner-tree-pas.jar -in instances/instance001.gr -ui output -a 10`**
 * Compute optimum and display Steiner tree in the ui

3. **`java -jar steiner-tree-pas.jar -in instances/instance019.gr -c -long -out`**
 * Compute exhaustively the best feasible solution for `instance019.gr` with detailed standard output and keep writing the Steiner tree into `instance019-tree.gr`




### Requirements
For development the following dependencies are needed:

 - [JDK 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 - [Maven](https://maven.apache.org/install.html)
