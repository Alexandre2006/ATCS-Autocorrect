package dev.thinkalex.autocorrect;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@SpringBootTest
public class AutocorrectTests {

    private String[] dictionary, matches;
    private int threshold;
    private String typed;

    @Test
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    public void testSmall() {
        setTestData(0);
        Autocorrect studentSolution = new Autocorrect(dictionary, threshold);
        assertArrayEquals(
                matches, studentSolution.runTest(typed), "Incorrect words returned for testSmall.");
    }

    @Test
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    public void testMed() {
        setTestData(1);
        Autocorrect studentSolution = new Autocorrect(dictionary, threshold);
        assertArrayEquals(
                matches, studentSolution.runTest(typed), "Incorrect words returned for testMed.");
    }

    @Test
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    public void testLarger() {
        setTestData(2);
        Autocorrect studentSolution = new Autocorrect(dictionary, threshold);
        assertArrayEquals(
                matches, studentSolution.runTest(typed), "Incorrect words returned for testLarger.");
    }

    private void setTestData(int test) {
        try {
            Resource testResource = new ClassPathResource("test_files/" + test + ".txt");
            Resource answerResource = new ClassPathResource("test_files/" + test + "_answers.txt");

            try (BufferedReader testReader =
                         new BufferedReader(new InputStreamReader(testResource.getInputStream()))) {
                try (BufferedReader answerReader =
                             new BufferedReader(new InputStreamReader(answerResource.getInputStream()))) {

                    typed = testReader.readLine();
                    threshold = Integer.parseInt(testReader.readLine());

                    dictionary = loadWords(testReader);
                    matches = loadWords(answerReader);
                }
            }
        } catch (IOException e) {
            System.err.println("Error opening test file " + test + ".txt: " + e.getMessage());
        }
    }

    private String[] loadWords(BufferedReader br) {
        try {
            String line = br.readLine();
            int n = Integer.parseInt(line);
            String[] words = new String[n];

            for (int i = 0; i < n; i++) {
                words[i] = br.readLine();
            }
            return words;
        } catch (IOException e) {
            System.err.println("Error reading words from file: " + e.getMessage());
            return new String[0];
        }
    }
}
