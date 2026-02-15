package com.infoline.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires du HelloController.
 *
 * @WebMvcTest(HelloController.class) : charge uniquement la couche MVC Spring
 * (controllers, filters, Jackson) sans démarrer le contexte complet ni
 * le serveur Tomcat. C'est plus rapide qu'un @SpringBootTest pour tester les
 * endpoints REST de façon isolée.
 *
 * Ces tests sont exécutés automatiquement par le pipeline GitHub Actions
 * lors de la phase de build avant la création de l'image Docker.
 * Un échec bloque le déploiement sur EKS.
 */
@WebMvcTest(HelloController.class)
public class HelloControllerTest {

    /**
     * MockMvc : simule les requêtes HTTP sans démarrer de vrai serveur.
     * Injecté automatiquement par @WebMvcTest — permet de tester les
     * endpoints, les codes HTTP et le contenu JSON des réponses.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Vérifie que GET /api/hello retourne HTTP 200 avec le corps JSON attendu.
     *
     * jsonPath("$.message") : assertion sur le champ "message" du JSON retourné
     * jsonPath("$.status")  : assertion sur le champ "status"
     *
     * Ce test valide la sérialisation Jackson et le routage Spring MVC
     * sans dépendance externe (pas de base de données, pas de réseau).
     */
    @Test
    void helloEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/hello"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Hello World from InfoLine Backend!"))
               .andExpect(jsonPath("$.status").value("running"));
    }

    /**
     * Vérifie que GET /api/health retourne HTTP 200 avec status "UP".
     *
     * Ce test garantit que le health check Kubernetes etournera bien HTTP 200 au démarrage
     * du pod — évitant ainsi les redémarrages intempestifs par le kubelet.
     */
    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("UP"))
               .andExpect(jsonPath("$.service").value("infoline-backend"));
    }
}