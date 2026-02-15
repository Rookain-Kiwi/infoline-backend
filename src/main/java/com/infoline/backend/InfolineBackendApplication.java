package com.infoline.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'application Spring Boot InfoLine Backend.
 *
 * @SpringBootApplication active trois mécanismes Spring Boot :
 *   - @Configuration     : déclare cette classe comme source de beans Spring
 *   - @EnableAutoConfiguration : configure automatiquement Spring selon les
 *                          dépendances présentes dans le classpath
 *   - @ComponentScan     : découvre automatiquement les composants Spring
 *                          dans le package com.infoline.backend et ses sous-packages
 *
 * Au démarrage, Spring Boot lance un serveur Tomcat embarqué sur le port
 * défini dans application.properties (server.port=8080).
 *
 * Déployé sur EKS via le manifest k8s/deployment.yaml —
 * le conteneur expose le port 8080, mappé par le Service Kubernetes.
 */
@SpringBootApplication
public class InfolineBackendApplication {

    /**
     * Point d'entrée JVM — démarre le contexte Spring Boot.
     *
     * SpringApplication.run() initialise :
     *   1. Le contexte d'application Spring (IoC container)
     *   2. Le serveur Tomcat embarqué
     *   3. Les auto-configurations (MVC, Jackson, Actuator si présent...)
     *
     * @param args Arguments de ligne de commande (transmis au contexte Spring)
     */
    public static void main(String[] args) {
        SpringApplication.run(InfolineBackendApplication.class, args);
    }
}