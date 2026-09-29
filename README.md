# 🎮 Game Maps IRL — Navigation GPS Style GTA V / Cyberpunk

Application de navigation GPS en conditions réelles combinant l'ambiance et les codes visuels des jeux vidéo (**GTA V**, **Cyberpunk 2077**, **Need for Speed**) avec la précision cartographique moderne (**MapLibre GL**, **OSRM**), un moteur de rendu de véhicules en véritable **3D WebGL (Three.js)**, et un packaging mobile **Android via Capacitor**.

---

## ✨ Fonctionnalités Principales

- **Cartographie immersive** : Thème GTA V sombre haute lisibilité, Cyberpunk néon avec bâtiments rétro-éclairés, vue satellite hybride haute définition, et vue claire Waze.
- **Véhicules en 3D temps réel (Three.js)** :
  - Supercar GT avec aileron carbone et diffuseur
  - Muscle Car V8 avec supercharger sur le capot
  - Monoplace F1 avec ailerons aéro et pneus slick larges
  - 4x4 Offroad surélevé avec galerie d'expédition
  - Superbike de compétition avec pilote
  - Vaisseau antigravité Hovercar avec 4 réacteurs néon
  - Flèches radar emblématiques biseautées en volume
- **Phares et éclairage de chaussée dynamiques** : Faisceau volumétrique doux orienté vers l'avant avec nappe d'éclairage au sol sur le bitume.
- **Aperçu 3D Studio interactif** : Plateau tournant tactile dans les paramètres pour examiner et faire tourner chaque véhicule avec ses reflets de carrosserie.
- **Design Plat & HUD Cockpit** : Interface sombre épurée, sans cartes artificielles arrondies, avec barre de recherche élargie et indicateurs d'état discrets.
- **Gestion des Lieux Enregistrés** : Raccourcis pour Maison, Travail et favoris personnalisés avec géolocalisation.
- **Moteur Audio Procédural** : Sons de manœuvres, jingle d'arrivée GTA V "Mission Passed" et alertes sonores via Web Audio API.

---

## 🛠️ Stack Technique

- **Frontend** : React 19, TypeScript, Vite, Tailwind CSS
- **Cartographie** : MapLibre GL v6, OSRM (Open Source Routing Machine)
- **3D & Rendu WebGL** : Three.js (Custom Style Layer synchronisé dans MapLibre)
- **Mobile** : Capacitor 8 (Android)
- **Audio** : Web Audio API procédurale

---

## 🚀 Démarrage Rapide

### Prérequis
- Node.js 18+
- Android Studio et SDK Android (pour le build mobile)
- Appareil Android avec Débogage USB activé (optionnel, pour déploiement direct)

### Installation & Développement Web
```bash
# Installation des dépendances
npm install

# Lancement du serveur de développement local
npm run dev
```

### Build & Déploiement Android via ADB
```bash
# 1. Compilation Web
npm run build

# 2. Synchronisation Capacitor vers le dossier Android
npx cap sync android

# 3. Compilation de l'APK Debug
cd android
./gradlew assembleDebug
cd ..

# 4. Installation sur smartphone connecté via ADB
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# 5. Lancement direct de l'application
adb shell monkey -p com.gamemaps.irl -c android.intent.category.LAUNCHER 1
```

---

## 🤖 Contexte Antigravity & Assistance IA

Ce projet a été développé avec l'assistance de Google DeepMind Antigravity. Pour rouvrir et continuer le projet sur un autre ordinateur avec l'intégralité du contexte :
- Consulter [GEMINI.md](file:///c:/Users/maxen/Documents/antigravity/map/GEMINI.md) et [AGENTS.md](file:///c:/Users/maxen/Documents/antigravity/map/AGENTS.md) pour les règles d'architecture et les directives utilisateur.
- Les règles spécifiques se trouvent dans `.agent/rules/`.
- L'historique complet des sessions, décisions et transcriptions se trouve dans `.context/`.
