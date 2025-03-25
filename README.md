# Autocorrect

A super simple autocorrect program using the Levenshtein distance algorithm (w/ optimizations using tokenization and word length)

## Configuring, Packaging and Running

### Configuration

All options (except tokenization) are configurable when running the web or terminal UI.

Otherwise, you can configuring options for the benchmark in the `AutocorrectShell.java` file.
Tokenization can be configured in `Autocorrect.java`.

### Packaging
To package autocorrect into a jar, run:

```shell
mvn clean package
```

The package will be available in the `target/` directory.

### Running

To run the web UI:

```shell
java -jar *autocorrect.jar*
```

To run the terminal UI:

```shell
java -jar *autocorrect.jar* --cli
```

To run the benchmark:

```shell
java -jar *autocorrect.jar* --benchmark
```

## Demos
### Web UI

![Web UI Demo](https://github.com/Alexandre2006/ATCS-Autocorrect/raw/master/demos/webui_demo.gif)

### Terminal UI

![Terminal UI Demo](https://github.com/Alexandre2006/ATCS-Autocorrect/raw/master/demos/tui_demo.gif)

### Benchmark

![Benchmark Demo](https://github.com/Alexandre2006/ATCS-Autocorrect/raw/master/demos/benchmark_demo.gif)

## Time / Space Complexity
***n*** = number of words in the dictionary

***m*** = average length of a word in the dictionary

| Operation                         | Time Complexity | Space Complexity | Time Complexity Explanation                                                                                               | Space Complexity Explanation                                             |
|:----------------------------------|:---------------:|:----------------:|:--------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------|
| Loading Dictionary + Tokenization |     O(n*m)      |      O(n*m)      | Reading each word is O(n), and the # of tokens is based on O(m).                                                          | O(n) for the dictionary of words, and O(n*m) for the tokens!             |
| Finding candidates for correction |     O(n+m)      |       O(n)       | O(n + m), since O(n) words need to be sorted, and checked for length. Tokenization is only done on the new word, so O(m). | Potential words are stored as a list, with the maximum being O(n) words. |
| Calculating Levenshtein Distance  |     O(m^2)      |      O(m^2)      | Comparing two words in 2D array, so O(m^2).                                                                               | Comparing two words in 2D array, so O(m^2).                              |