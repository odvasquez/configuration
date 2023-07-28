# configuration

Hi, Spring fans! Welcome to another installment of Spring tips! in this installment, we're going to look at something that's rather foundational, and something that I wish I'd addressed earlier: configuration. And no, I don't mean functional configuration or java configuration or anything like that, I'm talking about the string values that inform how your code executes. the stuff that you put in application.properites. that configuration.

All configuration in Spring emanates from the Spring Environment abstraction. The Environment is sort of like a dictionary - a map with keys and values. Environment is just an interface through which we can ask questions about, you know, the Environment. The abstraction lives in Spring Framework and was introduced in Spring 3, more than a decade ago. up until that point, there was a focused mechanism to allow integration of configuration called property placeholder resolution. This environment mechanism and the constellation of classes around that interface more than supersede that old support. if you find a blog still using those types, may I suggest you move on to newer and greener pastures? :)

Let's get started. Go to the Spring Initializr and generate a new project and make sure to choose Spring Cloud Vault, Lombok, and Spring Cloud Config Client. I named my project configuration. Go ahead and click Generate the application. Open the project in your favorite IDE. If you want to follow along, be sure to disable the Spring Cloud Vault and Spring Clod Config Lcieny dependencies. We don't need them right now.

The first step for most Spring Boot developers is to use application.properties. The Spring Initializr even puts an empty application.properties in the src/main/resources/application.properties. folder when you generate a new project there! Super convenient. You do create your projects on the Spring Initializr, don't ya'? You could use appication.properties or application.yml. I don't particularly love .yml files, but you can use it if that's more your taste.

Spring Boot automatically loads the application.properties whenever it starts up. You can dereference values from the property file in your java code through the environment. Put a property in the application.properties file, like this.

message-from-application-properties=Hello from application.properties
Now, let's edit the code to read in that value.

package com.example.configuration;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigurationApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            log.info("message from application.properties " + environment.getProperty("message-from-application-properties"));
        };
    }
}
Run this, and you'll see the value form the configuration property file in the output of the log. If you want to change which file SPring Boot rads by default, you can do that too. It's a chicken and egg problem, though - you need to specify a property that Spring Boot will use to figure out where to load all the properties. So you need to specify this outside of the application.properties file. You can use a program argument or an environment variable to fill the spring.config.name property.

export SPRING_CONFIG_NAME=foo
Re-run the application now with that environment variable in scope, and it'll fail because it'll try to load foo.properties, not application.properties.

Incidentally, you could also run the application with the configuration that lives outside the application, adjacent to the jar, like this. If you run the application like this, the values in the external application.properties will override the values inside the .jar.

.
├── application.properties
└── configuration-0.0.1-SNAPSHOT.jar

0 directories, 2 files
Spring Boot is aware of Spring profiles, as well. Profiles are a mechanism that lets you tag objects and property files so that they can be selectively activated or deactivated at runtime. This is great if you want to have an environment-specific configuration. You can tag a Spring bean or a configuration file as belonging to a particular profile, and Spring will automatically load it for you when that profile is activated.

Profile names are, basically, arbitrary. Some profiles are magic - that Spring honors in a particular way. The most interesting of these is default, which is activated when no other profile is active. But generally, the names are up to you. I find it very useful to map my profiles to different environments: dev, qa, staging, prod, etc.

Let's say that there's a profile called dev. Spring Boot will automatically load application-dev.properties. It'll load that in addition to application.properties. If there are any conflicts between values in the two files, then the more specific file - the one with the profile - wins. You could have a default value that applies absent a particular profile, and then provide specifics in the config for a profile.

You can activate a given profile in several different ways, but the easiest is just to specify it on the command line. Or you could turn it on in your IDE's run configurations dialog box. IntelliJ and Spring Tool Suite both provide a place to specify the profile to sue when running the application. You can also set an env var, SPRING_PROFILES_ACTIVE, or specify an argument on the command line --spring.profiles.active. Either one accepts a comma-delimited list of profiles - you can activate more than one profile at a time.

Le'ts try that out. Create a file called application-dev.properties. Put the following value in it.

message-from-application-properties=Hello from dev application.properties
This property has the same key like the one in application.properties. The java code here is identical to what we had before. Just be sure to specify the profile before you start the Spring application. You can use the environment variable, properties, etc. You can even define it programmatically when building the SpringApplication in the main() method.

package com.example.configuration.profiles;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

    public static void main(String[] args) {
        // this works
        // export SPRING_PROFILES_ACTIVE=dev  
        // System.setProperty("spring.profiles.active", "dev"); // so does this
        new SpringApplicationBuilder()
            .profiles("dev") // and so does this
            .sources(ConfigurationApplication.class)
            .run(args);
    }

    @Bean
    ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            log.info("message from application.properties " + environment.getProperty("message-from-application-properties"));
        };
    }
}
Run the application, and you'll see the specialized message reflected in the output.

So far, we've been using the ENvironment to inject the configuration. You can also use @Value annotation to inject the value as a parameter. You probably already know that. But did you know that you can also specify default values to be returned if there are no other values that match? There are a lot of reasons why you might want to do this. You could use it to provide fallback values and make it more transparent when somebody fat fingers the spelling of a property. It is also useful because you are given a value that might be useful if somebody doesn't know that they need to activate a profile or something,.

package com.example.configuration.value;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigurationApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(
        @Value("${message-from-application-properties:OOPS!}") String valueDoesExist,
        @Value("${mesage-from-application-properties:OOPS!}") String valueDoesNotExist) {
        return args -> {
            log.info("message from application.properties " + valueDoesExist);
            log.info("missing message from application.properties " + valueDoesNotExist);
        };
    }
}
Convenient, eh? Also, note that the default String that you provide can, in turn, interpolate some other property. So you could do something like this, assuming a key like default-error-message does exist somewhere in your application configuration:

${message-from-application-properties:${default-error-message:YIKES!}}

That will evaluate the first property if it exists, then the second and then the String YIKES!, finally.

Earlier, we looked at how to specify a profile using an environment variable or program argument. This mechanism - configuring Spring Boot with environment variables or program arguments - is a general-purpose. You can use it for any arbitrary key, and Spring Boot will normalize the configuration for you. Any key that you would out in application.properties can be specified externally in this way. Let's see some examples. Let's suppose you want to specify the URL for a data source connection. You could hardcode that value in the application.properties, but that's not very secure. It might be much better to create instead an environment variable that only exists in production. That way, the developers don't have access to the keys to the production database and so on.

Let's try it out. Heres the java code fo the example.


package com.example.configuration.envvars;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

    public static void main(String[] args) {
        // simulate program arguments
        String[] actualArgs = new String[]{"spring.datasource.url=jdbc:postgres://localhost/some-prod-db"};
        SpringApplication.run(ConfigurationApplication.class, actualArgs);
    }

    @Bean
    ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            log.info("our database URL connection will be " + environment.getProperty("spring.datasource.url"));
        };
    }
}

Before you run it, be sure to either export an environment variable in the shell that you use to run your application or to specify a program argument. I simulate the latter - the program arguments - by intercepting the public static void main(String [] args) that we pass into the Spring Boot application here. You can also specify an env variable like this:

export SPRING_DATASOURCE_URL=some-arbitrary-value
mvn -DskipTests=true spring-boot:run 
Run the program multiple times, trying out the different approaches, and you will see the values in the output. There's no autoconfiguration in the application that will connect to a database, so we're using this property as an example. The URL doesn't have to be a valid URL (at least not until you add Spring's JDBC support and a JDBC driver to the classpath).

Spring Boot is very flexible in its sourcing of the values. It doesn't care if you do SPRING_DATASOURCE_URL, spring.datasource.url, etc. Spring Boot calls this relaxed binding. It allows you to do things in a way that's most natural for different environments, while still working for Spring Boot.

This idea - of externalizing configuration for an application from the environment - is not new. It's well understood and described in the 12-factor manifesto. The 12-factor manifesto says that environment-specific config should live in that environment, not in the code itself. This is because we want one build for all the environments. Things that change should be external. So far, we've seen that Spring Boot can pull in configuration from the command line arguments (program arguments), and environment variables. It can also read configuration coming from JOpt. It can come even from a JNDI context if you happen to be running in an application server with one of those around!

Spring Boots's ability o pull in any environment variable is beneficial here. It's also more secure than using program arguments because the program arguments will show up in the output of operating system tools. Environment variables are a better fit
