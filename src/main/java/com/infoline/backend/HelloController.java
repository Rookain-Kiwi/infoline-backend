package com.infoline.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST principal de l'API InfoLine.
 *
 * Expose deux endpoints de démonstration sur le préfixe /api :
 *   GET /api/hello  : réponse applicative avec métadonnées du service
 *   GET /api/health : health check pour les probes Kubernetes
 *
 * Rôle dans l'architecture InfoLine :
 *   Frontend Angular → Service K8s (port 8080) → HelloController
 *
 * Déployé sur EKS via le manifest k8s/deployment.yaml.
 * Image Docker construite et pushée sur ECR par le pipeline GitHub Actions.
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * Endpoint de démonstration — valide le déploiement end-to-end.
     *
     * Retourne un objet JSON avec les métadonnées du service :
     *   - message     : message de bienvenue
     *   - description : contexte du projet ECF
     *   - version     : version du service (à incrémenter à chaque release)
     *   - timestamp   : horodatage de la requête (LocalDateTime UTC)
     *   - status      : état courant du service
     *
     * Spring Boot sérialise automatiquement la Map en JSON via Jackson.
     *
     * @return Map<String, Object> sérialisée en JSON par Jackson
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message",     "Hello World from InfoLine Backend!");
        response.put("description", "API REST - Projet ECF Administrateur Système DevOps");
        response.put("version",     "1.0.0");
        response.put("timestamp",   LocalDateTime.now());
        response.put("status",      "running");
        return response;
    }

    /**
     * Endpoint de health check — utilisé par les probes Kubernetes.
     *
     * Appelé par :
     *   - livenessProbe  : vérifie que le pod est vivant (redémarrage si KO)
     *   - readinessProbe : vérifie que le pod est prêt à recevoir du trafic
     *
     * Retourne HTTP 200 avec status "UP" tant que le service répond.
     * Voir k8s/deployment.yaml pour la configuration des probes.
     *
     * @return Map<String, String> sérialisée en JSON par Jackson
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status",  "UP");
        response.put("service", "infoline-backend");
        return response;
    }
}