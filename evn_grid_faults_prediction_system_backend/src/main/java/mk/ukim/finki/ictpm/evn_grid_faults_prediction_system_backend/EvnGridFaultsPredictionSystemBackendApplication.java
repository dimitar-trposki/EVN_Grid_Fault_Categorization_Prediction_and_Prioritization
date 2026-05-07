package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EvnGridFaultsPredictionSystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvnGridFaultsPredictionSystemBackendApplication.class, args);
    }

}
