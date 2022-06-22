package io.nuvalence.workmanager.service.config;

import org.springframework.stereotype.Component;

/**
 * Custom class that initializes firebase auth after application start up.
 * Typically, this would exist in the main SpringBootApplication class, however as ours is code generated this will do.
 */
@Component
public class FirebaseInitialize {
    /* @Value("${firebase.credential.resource-path}")
    private String keyPath; */

    /*
     * Initializes Firebase on server start with the application ready event listener.
     * @throws IOException if errors.
     */
    /* @Bean
    @Primary
    @EventListener(ApplicationReadyEvent.class)
    public void firebaseInitialization() throws IOException {
        InputStream serviceAccount = new ByteArrayInputStream(keyPath.getBytes(StandardCharsets.UTF_8));
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    } */
}
