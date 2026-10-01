# Game Maps IRL 🚗🎮

> **Application de navigation GPS réelle immersive inspirée des jeux vidéo (GTA V, Need for Speed, Cyberpunk 2077, Minecraft).**

---

## 📌 Organisation des Branches

| Branche | Description | Statut |
| :--- | :--- | :--- |
| **`main`** | **Cahier des charges**, spécifications d'architecture et feuille de route pour la refonte native / automobile. | En cours (Spécifications & Architecture) |
| **`native-android`** | **Refonte native** Kotlin + Compose + MapLibre Native + Android Auto (Car App Library). Voir [ARCHITECTURE.md](./ARCHITECTURE.md). | En cours (fondations) |
| **[`webgl-version`](https://github.com/Maxenceboo/map/tree/webgl-version)** | **Version 3D WebGL complète et fonctionnelle** (React, MapLibre GL, Three.js, Capacitor Android, sons Web Audio, phares volumétriques, HUD flat design). | Archivée & Fonctionnelle (commit `eb7b9eb`) |

---

## 📖 Documentation Maîtresse

Pour consulter l'intégralité des spécifications fonctionnelles, des formules mathématiques, des règles de design cockpit, des shaders de phares et de l'analyse des contraintes Android Auto / Apple CarPlay, consultez :

👉 **[CAHIER_DES_CHARGES.md](./CAHIER_DES_CHARGES.md)**

---

## 🕹️ Comment Lancer la Version WebGL 3D (Three.js)

La version WebGL 3D complète est préservée intacte sur la branche `webgl-version`. Pour la cloner, l'exécuter ou la compiler sur smartphone Android :

```bash
# 1. Basculer sur la branche WebGL
git checkout webgl-version

# 2. Installer les dépendances
npm install

# 3. Lancer le serveur de développement local
npm run dev

# 4. (Optionnel) Compiler et installer sur smartphone Android connecté en USB
npm run build
npx cap sync android
cd android
./gradlew assembleDebug
cd ..
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.gamemaps.irl -c android.intent.category.LAUNCHER 1
```

---

## 🌟 Fonctionnalités Clés de la Version WebGL (Préservées sur `webgl-version`)

- **Véhicules en Vraie 3D (Three.js)** : Couche WebGL native MapLibre (`CustomLayerInterface`). Forward en `+Z`, échelle dynamique 58px.
- **Phares Avant Réalistes** : Faisceaux volumétriques avec dégradé sans disque de coupe + nappe lumineuse projetée sur le bitume (`NormalBlending`).
- **Catalogue de Véhicules Procéduraux** : Sportive, Muscle Car, Formule 1, 4x4 SUV, Moto de course, Cyberpunk V-Tech, Flèches radar GTA/Waze.
- **Cockpit HUD Flat Design** : Respect strict des règles minimalistes sombres (pas de cards arrondies génériques, menus en listes verticales hiérarchiques).
- **Styles Cartographiques** : Radar GTA V sombre, Voxel Minecraft avec textures animées, Waze nocturne haute lisibilité.
- **Guidage & Trafic TomTom** : Tracé GPS violet (`#c084fc`) avec bordures orange/rouge sur les ralentissements.
- **Radars & Vitesse** : Détection des radars français à 800m avec bip modulé et macaron de limitation OSM.
- **Audio Procédural** : Synthèse Web Audio pure de la fanfare GTA V "Mission Passed" et des clics de cockpit.

---

## 🚗 Prochaine Étape : Compatibilité Automobile (Android Auto / CarPlay)

Consultez les sections 9 et 10 du [Cahier des charges](./CAHIER_DES_CHARGES.md) pour les détails techniques sur la barrière des WebViews en voiture et la feuille de route pour le moteur natif.
