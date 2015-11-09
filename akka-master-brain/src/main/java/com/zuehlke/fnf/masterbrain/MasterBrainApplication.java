package com.zuehlke.fnf.masterbrain;


import org.apache.commons.cli.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties({MasterBrainProperties.class})  // loaded from classpath:/application.yml
@EnableWebSocketMessageBroker
public class MasterBrainApplication implements CommandLineRunner{

    //@Value("${javapilot.trainingUrl}")
    //private String relayTrainingUrl;

    /**
     * Primary entry point of MasterBrain
     * @param args runtime arguments
     */
    public static void main(String[] args) {

        SpringApplication.run(MasterBrainApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {

        Options options = new Options();
        //options.addOption("i", true, "Team ID");
        
        List<String> arglist = new ArrayList<>();
        for (String arg : args) {
            if (!arg.contains("--")) {
                arglist.add(arg);
            }
        }
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, arglist.toArray(new String[ arglist.size()]));

        

        //if ( cmd.hasOption("i")) {
        //    team = cmd.getOptionValue("i");
        //}
//        if ( cmd.hasOption("p")) {
//            accessCode = cmd.getOptionValue("p");
//        }
//        if ( cmd.hasOption("d")) {
//            design = cmd.getOptionValue("d");
//        }
//
//        String url = relayTrainingUrl + "/" + design;
//        boolean recordData = cmd.hasOption("r");

//        if ( cmd.hasOption("t")) {
//            try {
//                String description = cmd.getOptionValue("t");
//                new RestTemplate().postForObject(url, new TrainingRequest(team, accessCode, design, description, recordData ), TrainingResponse.class);
//            } catch ( HttpClientErrorException hcee ) {
//                System.err.println("Couldn't connect to " + url);
//            }
//        }
    }
}
