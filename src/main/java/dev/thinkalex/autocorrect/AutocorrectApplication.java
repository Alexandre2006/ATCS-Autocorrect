package dev.thinkalex.autocorrect;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class AutocorrectApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(AutocorrectApplication.class);

        // Check for CLI mode
        if (Arrays.asList(args).contains("--cli")) {
            builder.web(WebApplicationType.NONE);
        }

        // Pass the args to run() so they're maintained in the application context
        ConfigurableApplicationContext context = builder.run(args);
    }
}