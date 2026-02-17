# infoline-backend

API REST Java Spring Boot — TP Administrateur Système DevOps (Studi).

Déployée sur Amazon EKS via un pipeline GitHub Actions qui build, teste,
publie l'image Docker sur ECR et déploie sur le cluster.

## Stack technique

| Composant       | Technologie                             |
|-----------------|-----------------------------------------|
| Langage         | Java 21 (Eclipse Temurin)               |
| Framework       | Spring Boot 3.2.2                       |
| Build           | Maven (wrapper mvnw inclus)             |
| Tests           | JUnit 5 + MockMvc                       |
| Container       | Docker — build multi-stage              |
| Registry        | Amazon ECR                              |
| Déploiement     | Amazon EKS (namespace infoline-backend) |
| CI/CD           | GitHub Actions                          |

## Endpoints

| Méthode | Chemin           | Description                              |
|---------|------------------|------------------------------------------|
| GET     | /api/hello       | Réponse applicative avec métadonnées     |
| GET     | /api/health      | Health check (liveness + readiness K8s)  |
| GET     | /actuator/health | Santé détaillée Spring Boot Actuator     |

## Structure du projet

```
infoline-backend/
├── src/
│   ├── main/
│   │   ├── java/com/infoline/backend/
│   │   │   ├── InfolineBackendApplication.java  # Point d'entrée Spring Boot
│   │   │   └── HelloController.java             # Endpoints /api/hello et /api/health
│   │   └── resources/
│   │       └── application.properties           # Configuration serveur et Actuator
│   └── test/
│       └── java/com/infoline/backend/
│           └── HelloControllerTest.java         # Tests MockMvc
├── k8s/
│   ├── deployment.yaml  # Deployment EKS (1 replica, probes, ressources)
│   ├── service.yaml     # Service ClusterIP (port 80 → 8080)
│   └── configmap.yaml   # Variables d'environnement
├── Dockerfile           # Build multi-stage Maven → JRE Alpine
├── mvnw                 # Maven Wrapper
└── .github/
    └── workflows/
        └── ci-cd.yml    # Pipeline GitHub Actions (build → test → ECR → EKS)
```

## Développement local

### Prérequis

- Java 21
- Maven (ou utiliser le wrapper `./mvnw` inclus)
- Docker (pour le build de l'image)

### Démarrage

```bash
# Compiler et lancer
./mvnw clean package
./mvnw spring-boot:run

# Tester les endpoints
curl http://localhost:8080/api/hello
curl http://localhost:8080/api/health
curl http://localhost:8080/actuator/health
```

### Tests

```bash
./mvnw test
```

Les tests MockMvc valident les endpoints `/api/hello` et `/api/health`
sans démarrer de serveur Tomcat réel (`@WebMvcTest`).

### Build Docker

```bash
docker build -t infoline-backend:latest .
docker run -p 8080:8080 infoline-backend:latest
```

L'image utilise un build multi-stage (Maven → JRE Alpine) et s'exécute
sous un utilisateur non-root (`spring:spring`).

## Pipeline CI/CD

Le pipeline `.github/workflows/ci-cd.yml` se déclenche sur push vers `develop` et `main`.

| Job               | Déclencheur         | Action                                      |
|-------------------|---------------------|---------------------------------------------|
| build-test        | Toutes branches     | `mvn package` + `mvn test`                  |
| docker-build-push | develop / main      | Build image + push ECR (tag `branch-sha`)   |
| deploy-to-eks     | develop / main      | `kubectl apply k8s/` + vérification pods    |

### Secrets GitHub requis

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

## Déploiement manuel sur EKS

```bash
# Configurer kubectl
aws eks update-kubeconfig --region eu-west-3 --name infoline-eks-cluster

# Déployer
kubectl apply -f k8s/

# Vérifier
kubectl get pods -n infoline-backend
kubectl get svc -n infoline-backend
```

## Workflow Git

- `main` — branche stable
- `develop` — développement actif
- `feature/*` — fonctionnalités en cours

## Auteur

Loïc KERGOAT — Promotion THERY  