# Game Maps IRL — Antigravity Project Context & Directives

Ce projet est une application de navigation GPS réelle immersive inspirée des jeux vidéo (notamment **GTA V**, **Cyberpunk 2077** et **Need for Speed**), conçue avec **React**, **Vite**, **Tailwind CSS**, **MapLibre GL**, **Three.js** pour les véhicules 3D en temps réel, et encapsulée sur Android via **Capacitor**.

---

## 🎯 Directives & Préférences Utilisateur Clés (MANDATOIRES)

Ces règles ont été explicitement exigées par l'utilisateur tout au long du développement et doivent être scrupuleusement respectées par tout agent Antigravity travaillant sur ce projet :

1. **Design Épuré & Plat (AUCUN effet "Card" arrondi façon IA générique)** :
   - Bannir les cartes aux coins exagérément arrondis (`rounded-3xl` / `rounded-2xl` avec ombres épaisses).
   - Privilégier une interface cockpit/HUD sombre, plate (`bg-neutral-900` / `bg-black`), avec des bordures subtiles (`border-neutral-800`), des séparateurs fins et des coins discrets (`rounded-xl` ou `rounded-lg`).
   - Les menus de paramètres sont structurés en **listes verticales hiérarchiques** épurées (pas de grilles ou tableaux de cartes).

2. **Indicateurs d'état en haut à droite** :
   - Rappel discret de l'état système à droite : point vert / jaune / rouge ou icône de position GPS active sans surcharger l'écran.
   - Ne pas encombrer la barre de recherche avec du texte ou des icônes parasites.
   - La barre de recherche principale doit être spacieuse et immédiatement lisible (`Où aller ? (Bordeaux, Arcachon...)`).

3. **Modélisation Véhicule en Vraie 3D (Three.js WebGL)** :
   - L'utilisateur a explicitement rejeté les véhicules plats en 2D en vue inclinée (pitch 55°).
   - Les véhicules sont rendus en **véritable 3D WebGL** synchronisée dans le contexte WebGL de MapLibre via un `CustomLayerInterface` (`renderingMode: '3d'`).
   - Tous les modèles (`car_sport`, `car_muscle`, `car_f1`, `car_suv`, `car_moto`, `car_cyber`, flèches radar `arrow_gta`, `arrow_waze`) sont générés de manière procédurale dans `src/services/vehicle3DModels.ts`.

4. **Orientation & Faisceau des Phares (Important)** :
   - Dans le repère 3D du véhicule, l'axe **`+Z` représente l'avant** (direction de marche), `-Z` l'arrière.
   - Les phares partent précisément des optiques avant du véhicule (`z = frontZ`), orientés vers l'avant de la route avec une légère inclinaison vers le bas.
   - **Éviter tout contour ou disque circulaire abrupt à l'extrémité du cône** : utilisation d'une texture de dégradé linéaire (`createBeamVolumeTexture`) s'estompant progressivement vers une opacité 0.
   - **Projection lumineuse au sol** : une nappe lumineuse (`createRoadLightTexture`) est projetée à plat sur le bitume (`y = 0.025`) devant le véhicule sur 10 à 12 mètres pour simuler l'éclairage de la chaussée.
   - **Mode de fusion** : utiliser `THREE.NormalBlending` pour éviter les problèmes d'assombrissement grisâtre du compositeur WebGL sur Android WebView.

5. **Navigation des Paramètres en Sous-dossiers** :
   - Pour les véhicules et les lieux enregistrés, utiliser des sous-vues dédiées avec navigation par liste verticale (ex : `Paramètres > Véhicule > Modèle de véhicule`, `Paramètres > Lieux enregistrés`), avec bouton retour en haut à gauche et bouton de validation en bas.

6. **Lieux enregistrés & Favoris** :
   - Gestion intégrée de **Maison**, **Travail** et lieux personnalisés enregistrés dans `localStorage`.

---

## 🏗️ Architecture du Codebase

- **`src/components/Map.tsx`** :
  - Composant carte principal basé sur MapLibre GL.
  - Gère les styles de carte (GTA V sombre, Cyberpunk néon, Satellite hybride, Waze clair).
  - Intègre le `Vehicle3DLayer` et recharge la couche 3D lors des changements de style (`styledata`).
  - Trace l'itinéraire OSRM avec boussole et suivi automatique du cap du véhicule.

- **`src/components/CarDashboard.tsx`** :
  - Interface utilisateur HUD tactile : barre de recherche, compteur de vitesse GPS en bas à gauche, panneau d'itinéraire virage par virage, modal des paramètres.
  - Intègre la sélection des thèmes, des modèles 3D de véhicules, des couleurs de carrosserie et des lieux enregistrés.

- **`src/components/Vehicle3DPreview.tsx`** :
  - Plateau tournant 3D interactif studio dans les paramètres (`Paramètres > Véhicule`).
  - Permet de faire pivoter le véhicule avec le doigt / la souris avec reflets dynamiques de carrosserie et faisceau des phares actif.

- **`src/services/vehicle3DLayer.ts`** :
  - Implémente `CustomLayerInterface` (`renderingMode: '3d'`) de MapLibre GL.
  - Utilise `painter.transform.mercatorMatrix` pour projeter fidèlement les coordonnées Mercator `[0..1]` dans la scène Three.js.
  - Échelle dynamique à l'écran pour garantir une taille d'affichage optimale (~58px) quel que soit le niveau de zoom.

- **`src/services/vehicle3DModels.ts`** :
  - Modélisation procédurale 3D de chaque véhicule : châssis, carrosserie, ailerons, habitacle en verre teinté, roues, jantes, bandes LED arrière et phares avant.
  - Contient les générateurs de texture de faisceau volumétrique et de projection au sol.

- **`src/services/soundEffects.ts`** :
  - Moteur audio procédural avec la Web Audio API (jingle GTA V "Mission Passed", sons de clic, guidage vocal, alertes de vitesse).

- **`src/services/routing.ts`** :
  - Client de calcul d'itinéraire OSRM avec détection des manœuvres et calcul des distances.

---

## 🚀 Commandes de Build & Déploiement Android (ADB)

Le projet est testé et validé directement sur smartphone Android (Nothing Phone) via ADB :

```bash
# 1. Compilation Web Vite & TypeScript
npm run build

# 2. Synchronisation des assets vers Android Capacitor
npx cap sync android

# 3. Compilation de l'APK Debug avec Gradle
cd android
./gradlew assembleDebug
cd ..

# 4. Installation de l'APK sur l'appareil connecté en USB / Wi-Fi
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# 5. Lancement de l'application via monkey
adb shell monkey -p com.gamemaps.irl -c android.intent.category.LAUNCHER 1

# 6. Capture d'écran de vérification via ADB
adb shell screencap -p /sdcard/s.png && adb pull /sdcard/s.png screen_verification.png
```

---

## 📂 Historique & Transcripts

Pour consulter l'intégralité des échanges, décisions et détails d'implémentation de la session initiale :
- Voir `.context/SESSION_HISTORY.md` pour le résumé chronologique.
- Voir `.context/transcript.jsonl` pour le journal brut de la trajectoire Antigravity.
- Voir `.context/screenshots/` pour la galerie d'écrans de référence.
