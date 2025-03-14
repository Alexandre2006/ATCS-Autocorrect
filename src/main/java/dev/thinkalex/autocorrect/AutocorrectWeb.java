package dev.thinkalex.autocorrect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AutocorrectWeb {
    @Autowired
    private Autocorrect autocorrect;

    private void addCommonAttributes(Model model, String word) {
        model.addAttribute("word", word);
        model.addAttribute("editDistance", autocorrect.getMaxEditDistance());
        model.addAttribute("maxResults", autocorrect.getResponseLimit());
    }

    private void addSuggestions(Model model, String word) {
        List<String> nullableSuggestions = autocorrect.getTopStrings(word);
        List<String > suggestions = nullableSuggestions == null ? new ArrayList<>() : nullableSuggestions;
        model.addAttribute("suggestions", suggestions);
        model.addAttribute("wordFound", nullableSuggestions == null);

        // Disable error message
        model.addAttribute("error", "");
        model.addAttribute("isError", false);
    }

    private void addError(Model model, String error) {
        // Replace attributes from suggestions
        model.addAttribute("suggestions", List.of());
        model.addAttribute("wordFound", false);

        // Add error message
        model.addAttribute("error", error);
        model.addAttribute("isError", true);
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("suggestions", List.of());
        addCommonAttributes(model, "");
        return "index";
    }

    @PostMapping("/correct")
    public String correct(@RequestParam("word") String word, @RequestParam("editDistance") String editDistanceString, @RequestParam("maxResults") String responseLimitString, Model model) {
        // Add common attributes
        addCommonAttributes(model, word);

        int editDistance;
        int responseLimit;

        try {
            editDistance = Integer.parseInt(editDistanceString);
        } catch (NumberFormatException e) {
            addError(model, "Edit distance must be a valid integer!");
            return "fragments/results";
        }

        try {
            responseLimit = Integer.parseInt(responseLimitString);
        } catch (NumberFormatException e) {
            addError(model, "Max results must be a valid integer!");
            return "fragments/results";
        }

        // Check for parameters
        if (word.isEmpty()) {
            addError(model, "Word is empty!");
        } else if (editDistance < 1) {
            addError(model, "Edit distance must be at least 1!");
        } else if (responseLimit < 1) {
            addError(model, "Max results must be at least 1!");
        } else {
            // Update configuration
            autocorrect.setMaxEditDistance(editDistance);
            autocorrect.setResponseLimit(responseLimit);
            addSuggestions(model, word);
        }

        // Return the results page
        return "fragments/results";
    }
}
