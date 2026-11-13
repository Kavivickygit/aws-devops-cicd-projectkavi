
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return """
            <html>
              <body style="font-family:Arial;text-align:center;
                           background:#eaf2f8;padding:50px;">
                <h1>Welcome to AWS DevOps Project!</h1>
                <h2>GitHub to Jenkins CI/CD Pipeline</h2>
                <p>Application deployed successfully!</p>
                <p>Technologies: AWS | Jenkins | Docker | Maven</p>
              </body>
            </html>
            """;
    }
}
