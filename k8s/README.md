# k8s/

Manifests Kubernetes du backend InfoLine — déployés dans le namespace `infoline-backend`.

## Contenu

| Fichier            | Description                                                         |
|--------------------|---------------------------------------------------------------------|
| `deployment.yaml`  | Deployment 1 replica, probes liveness/readiness, limites ressources |
| `service.yaml`     | ClusterIP port 80 → containerPort 8080 (Tomcat)                     |
| `configmap.yaml`   | Variables non sensibles (APPLICATION_NAME, LOG_LEVEL, ENVIRONMENT)  |

## Ressources allouées

| Paramètre         | Requests  | Limits    |
|-------------------|-----------|-----------|
| Mémoire           | 128Mi     | 256Mi     |
| CPU               | 50m       | 200m      |

La JVM Spring Boot est contrainte via `JAVA_OPTS=-Xmx128m -Xms64m` (override du Dockerfile)
pour rester dans les limites du node t3.medium partagé avec Elasticsearch et Kibana.

## Probes Kubernetes

| Probe      | Délai initial | Endpoint          | Rôle                                      |
|------------|---------------|-------------------|-------------------------------------------|
| Liveness   | 90s           | /api/health       | Redémarre le pod si l'app est bloquée     |
| Readiness  | 60s           | /api/health       | Retire le pod du Service si non prêt      |

Les délais sont volontairement élevés : le démarrage de la JVM Spring Boot
prend environ 45-60 secondes sur une t3.medium. Un délai trop court déclenche des
redémarrages intempestifs par le kubelet (ingérable)

## Déploiement

```bash
# Appliquer les manifests
kubectl apply -f k8s/

# Vérifier l'état des pods
kubectl get pods -n infoline-backend

# Consulter les logs
kubectl logs -n infoline-backend -l app=infoline-backend

# Accès local à l'API (pour test)
kubectl port-forward svc/infoline-backend-service 8080:80 -n infoline-backend
curl http://localhost:8080/api/hello
curl http://localhost:8080/api/health
```

## Notes

Le pipeline CI/CD (`ci-cd.yml`) gère le déploiement automatiquement sur push
vers `develop` et `main`. Ce dossier est utilisé manuellement uniquement en cas
de débogage ou de re-déploiement après un `startup-infra.sh`.

Les credentials PostgreSQL (DB_HOST, DB_USER, DB_PASSWORD) sont à injecter
via un Secret Kubernetes — non configuré en ECF (car pas d'accès base de données
depuis le HelloController).