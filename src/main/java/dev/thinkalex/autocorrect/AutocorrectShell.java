package dev.thinkalex.autocorrect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.shell.component.view.TerminalUI;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.component.view.control.BoxView;
import org.springframework.shell.geom.HorizontalAlign;
import org.springframework.shell.geom.VerticalAlign;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class AutocorrectShell implements CommandLineRunner, ExitCodeGenerator {
    @Autowired
    private Autocorrect autocorrect;

    @Autowired
    TerminalUIBuilder builder;

    @Override
    public void run(String... args) throws Exception {}

    @Override
    public int getExitCode() {
        return 0;
    }

    @ShellMethod(value = "Run app in CLI mode!", key = "--cli")
    private void runCliMode() {
        System.out.println("Running in CLI mode");
        TerminalUI ui = builder.build();
        BoxView view = new BoxView();
        ui.configure(view);
        view.setDrawFunction((screen, rect) -> {
            screen.writerBuilder()
                    .build()
                    .text("Hello World", rect, HorizontalAlign.CENTER, VerticalAlign.CENTER);
            return rect;
        });
        ui.setRoot(view, true);
        ui.run();
    }
}