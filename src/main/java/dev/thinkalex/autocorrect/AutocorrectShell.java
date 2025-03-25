package dev.thinkalex.autocorrect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.shell.component.view.TerminalUI;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.component.view.control.*;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.geom.HorizontalAlign;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.HashSet;
import java.util.List;

import static java.lang.System.exit;

@ShellComponent
public class AutocorrectShell implements CommandLineRunner, ExitCodeGenerator {
    // Autocorrect instance
    @Autowired
    private Autocorrect autocorrect;

    // TUI Builder
    @Autowired
    TerminalUIBuilder builder;

    // TUI
    private TerminalUI tui;

    private InputView wordInput;
    private InputView editDistanceInput;
    private InputView responseLimitInput;
    private ListView<String> suggestionsView;

    private StatusBarView statusBar;

    private EventLoop eventLoop;

    @Override
    public void run(String... args) throws Exception {

    }

    @Override
    public int getExitCode() {
        return 0;
    }

    // Callbacks
    private void onRequestQuit() {
        exit(0);
    }

    private void refreshSuggestions() {
        // Validate number inputs
        int editDistance = 0;
        int responseLimit = 0;
        try {
            editDistance = Integer.parseInt(editDistanceInput.getInputText());
            if (editDistance < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // Clear suggestions w/ error
            suggestionsView.setItems(List.of("Invalid Edit Distance"));
            return;
        }

        try {
            responseLimit = Integer.parseInt(responseLimitInput.getInputText());
            if (responseLimit < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            // Clear suggestions w/ error
            suggestionsView.setItems(List.of("Invalid Response Limit"));
            return;
        }

        // Validate string inputs
        String word = wordInput.getInputText();
        if (word.isEmpty()) {
            // Clear suggestions w/ error
            suggestionsView.setItems(List.of("Please enter a word"));
            return;
        }

        // Configure autocorrect
        autocorrect.setMaxEditDistance(editDistance);
        autocorrect.setResponseLimit(responseLimit);

        // Get suggestions
        List<String> suggestions = autocorrect.getTopStrings(word.toLowerCase());

        // Check if word exists (null = word exists, [] = no suggestions)
        if (suggestions == null) {
            suggestionsView.setItems(List.of("Valid dictionary word!"));
        } else if (suggestions.isEmpty()) {
            suggestionsView.setItems(List.of("No suggestions found!"));
        } else {
            suggestionsView.setItems(suggestions);
        }
    }

    // Menu / Status Bars
    private MenuBarView buildMenuBar(EventLoop eventLoop, TerminalUI component) {
        // Callbacks
        Runnable quitAction = this::onRequestQuit;

        // Add options
        MenuBarView menuBar = MenuBarView.of(
                MenuBarView.MenuBarItem.of("File",
                                MenuView.MenuItem.of("Quit?", MenuView.MenuItemCheckStyle.NOCHECK, quitAction))
                        .setHotKey(KeyEvent.Key.f | KeyEvent.KeyMask.CtrlMask));

        // Configure & Return
        component.configure(menuBar);
        return menuBar;
    }

    private StatusBarView buildStatusBar(EventLoop eventLoop, TerminalUI component) {
        Runnable quitAction = this::onRequestQuit;
        StatusBarView statusBar = new StatusBarView(new StatusBarView.StatusItem[] {
                StatusBarView.StatusItem.of("CTRL-Q Quit", quitAction),
        });
        component.configure(statusBar);
        return statusBar;
    }

    // Main View
    private AppView buildMainView(EventLoop eventLoop, TerminalUI component) {
        // Create Main GridView
        GridView mainView = new GridView();
        component.configure(mainView);

        // Set layout
        mainView.setRowSize(3, 3, 10);
        mainView.setColumnSize(20, 20);
        mainView.setShowBorders(true);
        mainView.setTitleAlign(HorizontalAlign.CENTER);
        mainView.setTitle("Autocorrect");

        // Create word input
        wordInput = new InputView();
        component.configure(wordInput);
        wordInput.setTitle("Word to Correct:");
        wordInput.setShowBorder(true);

        // Create edit distance input
        editDistanceInput = new InputView();
        component.configure(editDistanceInput);
        editDistanceInput.setTitle("Max Edit Distance:");
        editDistanceInput.setShowBorder(true);

        // Create response limit input
        responseLimitInput = new InputView();
        component.configure(responseLimitInput);
        responseLimitInput.setTitle("Max Suggestions:");
        responseLimitInput.setShowBorder(true);

        // Create suggestions
        suggestionsView = new ListView<>();
        component.configure(suggestionsView);
        suggestionsView.setShowBorder(true);
        suggestionsView.setTitle("Suggestions:");
        suggestionsView.setItems(List.of("No suggestions yet!"));

        mainView.addItem(wordInput, 0, 0, 1, 2, 0, 0);
        mainView.addItem(editDistanceInput, 1, 0, 1, 1, 0, 0);
        mainView.addItem(responseLimitInput, 1, 1, 1, 1, 0, 0);
        mainView.addItem(suggestionsView, 2, 0, 1, 2, 0, 0);

        // Create Menu & Status Bars
        MenuBarView menuBar = buildMenuBar(eventLoop, component);
        statusBar = buildStatusBar(eventLoop, component);

        // Create app view
        AppView appView = new AppView(mainView, menuBar, statusBar);
        component.configure(appView);

        // Return app view
        return appView;
    }

    // Main method
    @ShellMethod(value = "Run app in CLI mode!", key = "--cli")
    public void runCli() {
        // Log
        System.out.println("Running in CLI mode...");

        // Create the TUI
        tui = builder.build();
        eventLoop = tui.getEventLoop();

        // Register Events (Quit)
        eventLoop.onDestroy(eventLoop.keyEvents()
                .doOnNext(m -> {
                    if (m.getPlainKey() == KeyEvent.Key.q && m.hasCtrl()) {
                        onRequestQuit();
                    }
                })
                .subscribe());

        // Log events
        eventLoop.onDestroy(eventLoop.events().doOnEach(
                m -> {
                    refreshSuggestions();
                }).subscribe());

        // Build View
        AppView mainView = buildMainView(eventLoop, tui);

        // Set root & focus
        tui.setRoot(mainView, true);
        tui.setFocus(wordInput);

        // Run the TUI
        tui.run();
    }

    // Benchmark Method
    @ShellMethod(value = "Benchmark performance!", key = "--benchmark")
    public void benchmark() {
        // Configure Optimizations
        autocorrect.setMaxEditDistance(3);
        autocorrect.setResponseLimit(10);
        autocorrect.setIgnoreValidWords(false);

        // Start measuring time
        long startTime = System.currentTimeMillis();

        // Loop through each word
        int complete = 0;
        for (String word : autocorrect.dictionary) {
            autocorrect.getTopStrings(word);
            complete++;

            // Clear terminal less frequently to improve performance
                System.out.print("\033[H\033[2J");
                System.out.flush();

            // Print benchmark state
            long currentTime = System.currentTimeMillis();
            double averageTime = (currentTime - startTime) / (double)complete;
            System.out.println("Benchmark Progress: " + String.format("%.2f", ((complete * 100.0) / autocorrect.dictionary.size())) +
                    "% complete (" + complete + "/" + autocorrect.dictionary.size() + ")");
            System.out.println("Time per word: " + String.format("%.2f", averageTime) + "ms");
        }

        // Clear terminal
        System.out.print("\033[H\033[2J");
        System.out.flush();

        // Log final results
        long endTime = System.currentTimeMillis();
        double averageTime = (endTime - startTime) / (double)autocorrect.dictionary.size();
        System.out.println("Benchmark Complete!");
        System.out.println("Time Taken: " + String.format("%.2f", ((endTime - startTime) / 1000.0)) + " seconds");
        System.out.println("Time per word: " + String.format("%.2f", averageTime) + "ms");
    }
}