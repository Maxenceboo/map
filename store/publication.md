# Publier sur le Play Store

## 1. Créer la clé de signature (une seule fois)

Cette clé prouve que les mises à jour viennent de toi. **Si tu la perds ou oublies son mot de passe,
tu ne pourras plus mettre l'app à jour** : garde une copie hors de ce dossier (gestionnaire de mots
de passe, clé USB). Ne l'envoie à personne et ne la mets pas sur GitHub.

À la racine du projet :

```bash
keytool -genkeypair -v -keystore gamemaps-release.jks -alias gamemaps -keyalg RSA -keysize 2048 -validity 10000
```

`keytool` est fourni avec Android Studio (dossier `jbr/bin`). Il te demande un mot de passe et ton nom.

## 2. Indiquer la clé au build

Crée un fichier `keystore.properties` à la racine du projet :

```properties
storeFile=gamemaps-release.jks
storePassword=TON_MOT_DE_PASSE
keyAlias=gamemaps
keyPassword=TON_MOT_DE_PASSE
```

Ce fichier et les `.jks` sont ignorés par git.

## 3. Produire le fichier à envoyer

```bash
./gradlew bundleRelease
```

Le fichier est `app/build/outputs/bundle/release/app-release.aab`.
Sans `keystore.properties`, il est produit non signé et Google le refusera.

À chaque nouvelle version, augmente `versionCode` (1, 2, 3…) et `versionName` dans `app/build.gradle.kts`.

## 4. Remplir la fiche

Dans la Play Console (compte développeur : 25 $ une fois) :

- **Textes** : voir `fiche-play-store.md`.
- **Visuels** : `icone-play-store-512.png`, `banniere-1024x500.png`, `capture-1` à `capture-7`.
- **Politique de confidentialité** : publier `politique-de-confidentialite.md` à une adresse web
  (une page GitHub suffit) après y avoir mis une adresse de contact.
- **Sécurité des données** : position précise, utilisée pour le fonctionnement de l'app, non revendue.
- **Position en arrière-plan** : déclarer le service de guidage, avec une courte vidéo d'un trajet.
- **Android Auto** : cocher la distribution sur Android Auto ; Google valide à part les apps de navigation.

## Avant d'envoyer

- Faire un vrai trajet avec la version signée.
- Vérifier qu'aucune clé TomTom n'est dans le code (elle se colle dans l'app, Paramètres > Trafic).
