package dev.thinkalex.autocorrect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class AutocorrectController {
    @Autowired
    private Autocorrect autocorrect;

    private void addCommonAttributes(Model model, String word) {
        model.addAttribute("word", word);
        model.addAttribute("editDistance", autocorrect.getMaxEditDistance());
        model.addAttribute("responseLimit", autocorrect.getResponseLimit());
    }

    private void addSuggestions(Model model, String word) {
        List<String> suggestions = autocorrect.getTopStrings(word) == null
                ? new ArrayList<>()
                : Arrays.asList(autocorrect.getTopStrings(word));
        model.addAttribute("suggestions", suggestions);
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("suggestions", List.of());
        addCommonAttributes(model, "");
        return "index";
    }

    @PostMapping("/correct")
    public String correct(@RequestParam("word") String word, Model model) {
        addCommonAttributes(model, word);
        addSuggestions(model, word);
        return "fragments/results";
    }

    @PostMapping("/set-edit-distance")
    public String setEditDistance(
            @RequestParam("editDistance") int editDistance,
            @RequestParam("word") String word,
            Model model
    ) {
        autocorrect.setMaxEditDistance(editDistance);
        addCommonAttributes(model, word);
        addSuggestions(model, word);
        return "fragments/results";
    }

    @PostMapping("/set-max-results")
    public String setResponseLimit(
            @RequestParam("maxResults") int responseLimit,
            @RequestParam("word") String word,
            Model model
    ) {
        autocorrect.setResponseLimit(responseLimit);
        addCommonAttributes(model, word);
        addSuggestions(model, word);
        return "fragments/results";
    }
}
