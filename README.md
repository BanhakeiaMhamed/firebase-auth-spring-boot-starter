# firebase-auth-spring-boot-starter

Starter Spring Boot qui ajoute l'authentification par **jeton Firebase (Bearer)** à une API :
vérification du token, rôles issus des *custom claims*, filtre Spring Security et socle de sécurité
(stateless, réponses 401/403 en JSON, chemins publics configurables).

- Java 17+, Spring Boot 3.4.x (Spring Web + Spring Security)
- Auto-configuration : aucune annotation à ajouter, il suffit de la dépendance et de la configuration.

## Installation

Le starter est distribué via [JitPack](https://jitpack.io/#BanhakeiaMhamed/firebase-auth-spring-boot-starter) :
JitPack compile le dépôt GitHub à la demande, sans compte ni jeton côté utilisateur.

**1. Ajouter le dépôt JitPack** dans le `pom.xml` du projet consommateur :

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**2. Ajouter la dépendance** (la version est le nom du tag Git, ex. `v0.1.0`) :

```xml
<dependency>
    <groupId>com.github.BanhakeiaMhamed</groupId>
    <artifactId>firebase-auth-spring-boot-starter</artifactId>
    <version>v0.1.0</version>
</dependency>
```

> Le premier téléchargement d'une version peut prendre une à deux minutes : JitPack la construit à ce moment-là.
> Dans un `Dockerfile` ou une CI, aucune étape supplémentaire n'est nécessaire, Maven télécharge le starter comme
> n'importe quelle dépendance.

### Sans JitPack (installation locale)

Pour tester une modification locale, installer le starter dans le dépôt Maven de la machine :

```bash
./mvnw clean install
```

Il est alors disponible sous `io.github.banhakeiamhamed:firebase-auth-spring-boot-starter:0.1.0`
(coordonnées du `pom.xml`, différentes de celles de JitPack).

## Configuration

| Propriété (variable d'environnement) | Défaut | Rôle |
|---|---|---|
| `firebase.enabled` | `true` | `false` désactive tout le starter (utile en test) |
| `firebase.credentials` (`FIREBASE_CREDENTIALS`) | `firebase-credentials.json` | Compte de service Firebase Admin, voir ci-dessous |
| `firebase.auth.public-paths` | `/api/auth/**`, `/api/health` | Chemins accessibles sans jeton (motifs Ant) |
| `firebase.auth.default-role` | `USER` | Rôle donné si le jeton n'a pas de claim `roles` |

### Formats de `firebase.credentials`

- **JSON en ligne** (valeur commençant par `{`) : idéal pour une variable d'environnement, un secret Docker ou
  Kubernetes — `FIREBASE_CREDENTIALS='{"type":"service_account",...}'`. Le contenu n'est jamais écrit dans les logs.
- `classpath:chemin/fichier.json`
- un **chemin de fichier**, absolu ou relatif au répertoire de travail de l'application.

## Utilisation

Le starter ne connaît pas les règles d'accès de votre application : il fournit le socle, vous ajoutez vos rôles.

```java
@Bean
SecurityFilterChain apiChain(HttpSecurity http, FirebaseSecurityConfigurer firebase) throws Exception {
    firebase.configure(http.securityMatcher("/api/**"));   // CSRF off, stateless, filtre Firebase, 401/403 JSON, chemins publics
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated());
    return http.build();
}
```

Les rôles viennent du claim `roles` du jeton (liste de chaînes) et deviennent `ROLE_<NOM_EN_MAJUSCULES>`.

Pour vérifier un jeton ou lire des rôles à la main, injectez `FirebaseService` (`verifyToken`, `extractAuthorities`).

## Beans fournis

`FirebaseInitializer`, `FirebaseService`, `FirebaseAuthenticationFilter`, `FirebaseSecurityConfigurer`,
`FirebaseAuthProperties` — tous remplaçables : chacun est déclaré avec `@ConditionalOnMissingBean`.

Hors périmètre : la gestion des utilisateurs et des claims (création de comptes, attribution de rôles) reste à
la charge de l'application, qui peut utiliser directement le SDK `firebase-admin` (apporté par ce starter).

## Développement

```bash
./mvnw clean verify     # compile + tests
./mvnw clean install    # + installation dans ~/.m2
```

### Publier une nouvelle version

1. Changer `<version>` dans le `pom.xml` (ex. `0.2.0`) et committer.
2. Créer et pousser le tag correspondant :

   ```bash
   git tag v0.2.0
   git push origin v0.2.0
   ```
3. Dans les projets consommateurs, passer la `<version>` de la dépendance à `v0.2.0`.

JitPack construit la version au premier téléchargement ; l'état des builds est visible sur
[jitpack.io](https://jitpack.io/#BanhakeiaMhamed/firebase-auth-spring-boot-starter).
