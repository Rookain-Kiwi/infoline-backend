# ==============================================================================
# Dockerfile - InfoLine Backend (Spring Boot)
# ==============================================================================
# Build multi-stage pour optimiser la taille de l'image finale :
#   Stage 1 (build)   : image Maven complète — compile et package
#   Stage 2 (runtime) : image JRE Alpine légère — exécute uniquement
#
# L'image finale ne contient pas Maven, les sources ni les fichiers .class —
# uniquement le JAR exécutable et le JRE minimal (gain en légereté)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1 : Build
# ------------------------------------------------------------------------------
# maven:3.9-eclipse-temurin-21
# Recommandée pour les environnements de production.
# ------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copie les fichiers Maven en premier — optimisation du cache Docker.
# Si pom.xml et mvnw ne changent pas, Docker réutilise le layer du cache
# sans retélécharger les dépendances à chaque build.
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Téléchargement de toutes les dépendances en mode offline.
# -B (batch mode) : supprime les barres de progression pour des logs plus propres.
RUN mvn dependency:go-offline -B

# Copie du code source après les dépendances — si seul le code change,
# Docker n'invalidera que ce layer et les suivants (pas les dépendances).
COPY src ./src

# Compilation, tests unitaires et packaging en JAR exécutable.
# -DskipTests : les tests sont exécutés séparément dans le pipeline CI/CD
# avant cette étape de build
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2 : Runtime
# ------------------------------------------------------------------------------
# eclipse-temurin:21-jre-alpine
# Alpine bien plus légère que Debian
# Le JRE est suffisant pour exécuter un JAR: le JDK n'est pas nécessaire.
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Création d'un utilisateur non-root dédié à l'application.
# Security best practice : un conteneur compromis ne peut pas
# escalader vers root sur le node Kubernetes sous-jacent.
# addgroup -S et adduser -S : création de groupe/utilisateur système.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copie du JAR depuis le stage build uniquement — aucun fichier source,
# aucune dépendance Maven, aucun outil de build dans l'image finale.
# * = versioning dynamique du JAR
COPY --from=build /app/target/*.jar app.jar

# Port sur lequel le Tomcat embarqué va écouter (défini dans application.properties).
# EXPOSE n'est sue documentaire — le vrai binding est fait dans k8s/service.yaml.
EXPOSE 8080

# Options JVM pour contraindre la consommation mémoire du conteneur.
# -Xmx256m : maximum 256MB — adapté aux nodes t3.medium
#            avec plusieurs pods cohabitants (ELK + backend + frontend)
# -Xms128m : initial 128MB — évite les expansions mémoire fréquentes
ENV JAVA_OPTS="-Xmx256m -Xms128m"

# sh -c permet l'expansion de la variable $JAVA_OPTS au runtime.
# Sans sh -c, JAVA_OPTS serait passé comme argument littéral et non expansé.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]