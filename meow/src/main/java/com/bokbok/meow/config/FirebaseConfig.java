package com.bokbok.meow.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {

                InputStream serviceAccount;

                // Production — read from environment variable
                String base64Credentials = System.getenv(
                        "FIREBASE_CREDENTIALS_BASE64"
                );

                if (base64Credentials != null
                        && !base64Credentials.isEmpty()) {
                    byte[] decoded = Base64.getDecoder()
                            .decode(base64Credentials);
                    serviceAccount = new ByteArrayInputStream(decoded);
                    log.info("Firebase: loaded from environment variable");
                } else {
                    // Local dev — read from file
                    serviceAccount = new ClassPathResource(
                            "firebase-service-account.json"
                    ).getInputStream();
                    log.info("Firebase: loaded from classpath file");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(
                                GoogleCredentials.fromStream(serviceAccount)
                        )
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
        } catch (IOException e) {
            log.error("Firebase initialization failed: {}",
                    e.getMessage());
        }
    }
}