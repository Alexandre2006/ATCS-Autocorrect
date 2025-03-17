package dev.thinkalex.autocorrect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.shell.component.view.TerminalUI;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.component.view.control.*;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

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

    // Menu / Status Bars
    private MenuBarView buildMenuBar(EventLoop eventLoop, TerminalUI component) {
        // Callbacks
        Runnable quitAction = this::onRequestQuit;

        // Add options
        MenuBarView menuBar = MenuBarView.of(
                MenuBarView.MenuBarItem.of("Quit",
                                MenuView.MenuItem.of("Confirm?", MenuView.MenuItemCheckStyle.NOCHECK, quitAction))
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
        BoxView mainView = new BoxView();
        mainView.setDrawFunction((screen, rect) -> {
            screen.writerBuilder().build()
                    .text("hi", 0, 0);
            return rect;
        });
        component.configure(mainView);

        // Create Menu & Status Bars
        MenuBarView menuBar = buildMenuBar(eventLoop, component);
        StatusBarView statusBar = buildStatusBar(eventLoop, component);

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

        // Register Events
        eventLoop.onDestroy(eventLoop.keyEvents()
                .doOnNext(m -> {
                    if (m.getPlainKey() == KeyEvent.Key.q && m.hasCtrl()) {
                        onRequestQuit();
                    }
                })
                .subscribe());

        // Build View
        AppView mainView = buildMainView(eventLoop, tui);

        // Set root & focus
        tui.setRoot(mainView, true);
        tui.setFocus(mainView);

        // Run the TUI
        tui.run();
    }
}