package dev.thinkalex.autocorrect;

import java.util.*;

/**
 * Autocorrect
 * <p>
 * A command-line tool to suggest similar words when given one not in the dictionary.
 * </p>
 * @author Zach Blick
 * @author Alexandre Haddad-Delaveau
 */
public class Autocorrect {
    HashSet<String> dictionary;
    private final int limit;

    /**
     * Constucts an instance of the Autocorrect class.
     * @param words The dictionary of acceptable words.
     * @param limit The maximum number of edits a suggestion can have.
     */
    public Autocorrect(String[] words, int limit) {
        // Load the dictionary
        dictionary = new HashSet<>();
        dictionary.addAll(Arrays.asList(words));

        // Save limit
        this.limit = limit;
    }

    /**
     * Runs a test from the tester file, AutocorrectTester.
     * @param typed The (potentially) misspelled word, provided by the user.
     * @return An array of all dictionary words with an edit distance less than or equal
     * to threshold, sorted by edit distance, then sorted alphabetically.
     */
    public String[] runTest(String typed) {
        List<Result> results = getTopMatches(typed);
        String[] matches = new String[results.size()];
        for (int i = 0; i < results.size(); i++) {
            matches[i] = results.get(i).word;
        }

        // Log results
        System.out.println("Typed: " + typed);
        for (Result result : results) {
            System.out.println(result.word + " " + result.distance);
        }

        return matches;
    }

    /**
     * Calculates the edit distance between two words
     * Time Complexity: O(n*m) where n is the length of word1 and m is the length of word2.
     * Space Complexity: Same as time complexity.
     * @param word1 The first word.
     * @param word2 The second word.
     * @return The edit distance between the two words.
     */
    private int editDistance(String word1, String word2) {
        // Special Case: if either word is empty
        if (word1.isEmpty()) {
            return word2.length();
        } else if (word2.isEmpty()) {
            return word1.length();
        }

        // Convert both strings to lowercase
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        // Create matrix to store edit distances
        int[][] editDistances = new int[word1.length() + 1][word2.length() + 1];

        // Fill in the first row and column
        for (int i = 0; i <= word2.length(); i++) {
            editDistances[0][i] = i;
        }

        for (int i = 0; i <= word1.length(); i++) {
            editDistances[i][0] = i;
        }

        // Fill the rest of the matrix
        for (int i = 1; i <= word1.length(); i++) {
            for (int j = 1; j <= word2.length(); j++) {
                // Check if the characters are the same
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    editDistances[i][j] = editDistances[i - 1][j - 1];
                } else {
                    // Find the minimum of the three possible operations
                    int insert = editDistances[i][j - 1] + 1;
                    int delete = editDistances[i - 1][j] + 1;
                    int replace = editDistances[i - 1][j - 1] + 1;
                    editDistances[i][j] = Math.min(insert, Math.min(delete, replace));
                }
            }
        }

        // Return the edit distance
        return editDistances[word1.length()][word2.length()] > limit ?  Integer.MAX_VALUE : editDistances[word1.length()][word2.length()];
    }

    private static class Result {
        String word;
        int distance;

        public Result(String word, int distance) {
            this.word = word;
            this.distance = distance;
        }
    }

    private static final Comparator<Result> resultComparator = new Comparator<Result>() {
        @Override
        public int compare(Result r1, Result r2) {
            if (r1.distance == r2.distance) {
                return r1.word.compareTo(r2.word);
            }
            return r1.distance - r2.distance;
        }
    };

    /**
     * Returns the top matches for a given word.
     * @param word The word to find matches for.
     * @return A list of the top matches.
     */
    private List<Result> getTopMatches(String word) {

        // Store top words
        PriorityQueue<Result> pq = new PriorityQueue<>(10, resultComparator);

        // Go through all words in the dictionary
        for (String dictionaryWord : dictionary.stream().toList()) {
            pq.add(new Result(dictionaryWord, editDistance(dictionaryWord, word)));
        }

        // Return top matches
        List<Result> results = new ArrayList<>();
        while (!pq.isEmpty()) {
            Result result = pq.poll();

            // Break if the edit distance is too high
            if (result.distance == Integer.MAX_VALUE) {
                break;
            }

            results.add(result);
        }
        return results;
    }
}