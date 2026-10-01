# CAHIER DES CHARGES & DOCUMENT D'ARCHITECTURE MAÎTRE
## Game Maps IRL — Navigation GPS Immersive & Rétrospective WebGL / Android Auto

> **Version du document** : 2.0.0  
> **Auteur** : Antigravity (Google DeepMind Agentic Coding) & Maxence Bourragué  
> **Branche de référence WebGL** : [`webgl-version`](https://github.com/Maxenceboo/map/tree/webgl-version) (commit `eb7b9eb`)  
> **Statut** : Document de spécification complet & fondation pour la reconstruction

---

## SOMMAIRE

1. [Vision du Produit & Identité](#1-vision-du-produit--identité)
2. [Règles Fondamentales de Design HUD & Ergonomie Cockpit](#2-règles-fondamentales-de-design-hud--ergonomie-cockpit)
3. [Moteur 3D Temps Réel & Modélisation Procédurale (WebGL / Three.js)](#3-moteur-3d-temps-réel--modélisation-procédurale-webgl--threejs)
   - 3.1 Intégration CustomLayerInterface MapLibre GL
   - 3.2 Repère Spatial & Orientation Véhicule (+Z)
   - 3.3 Projection Mercator & Facteur d'Échelle Dynamique
   - 3.4 Système d'Éclairage : Phares Volumétriques & Nappe au Sol
   - 3.5 Catalogue des Modèles Procéduraux 3D
   - 3.6 Studio de Prévisualisation 3D (Turntable)
4. [Moteur Cartographique & Styles Visuels](#4-moteur-cartographique--styles-visuels)
   - 4.1 Tuiles Vectorielles & Filtrage de Voirie
   - 4.2 Thème GTA V Radar (Minimap)
   - 4.3 Thème Minecraft IRL (Pixel Art & Textures)
   - 4.4 Thème Waze Nocturne / Cyberpunk
5. [Moteur de Guidage, Routing & Trafic en Temps Réel](#5-moteur-de-guidage-routing--trafic-en-temps-réel)
   - 5.1 Client OSRM & Décomposition des Manœuvres
   - 5.2 Style Visuel du Tracé GPS & Calques de Trafic TomTom
   - 5.3 Recalcul Dynamique & Détection de Dérive
6. [Moteur de Sécurité : Radars & Vitesses Limites](#6-moteur-de-sécurité--radars--vitesses-limites)
   - 6.1 Base Embarquée des Radars Français
   - 6.2 Requêtes Spatiales & Algorithme d'Alerte Proximité
   - 6.3 Indicateur de Limite de Vitesse & Dépassement
7. [Moteur Audio Synthétique Procédural (Web Audio API)](#7-moteur-audio-synthétique-procédural-web-audio-api)
   - 7.1 Synthèse de la Fanfare GTA V "Mission Passed"
   - 7.2 Effets Sonores Cockpit & Bips de Radar
8. [Gestion des Données Utilisateur & Favoris](#8-gestion-des-données-utilisateur--favoris)
9. [Analyse Technique : Le Verrou Android Auto & Apple CarPlay](#9-analyse-technique--le-verrou-android-auto--apple-carplay)
   - 9.1 Les Restrictions NHTSA / Driver Distraction
   - 9.2 L'Interdiction Stricte des WebViews / WebGL sur Écran Embarqué
   - 9.3 Fonctionnement de la Car App Library Google
   - 9.4 Tentatives & Limites de l'Approche Hybride
10. [Feuille de Route & Architecture Cible pour la Refonte](#10-feuille-de-route--architecture-cible-pour-la-refonte)
    - 10.1 Scénarios Possibles (Natif C++/Kotlin, Double Mode, etc.)
    - 10.2 Matrice de Décision Technique

---

## 1. Vision du Produit & Identité

### 1.1 Genèse & Concept
**Game Maps IRL** réinvente le guidage GPS automobile du quotidien en fusionnant l'ergonomie fonctionnelle d'un navigateur moderne (Waze, Google Maps) avec l'esthétique et les sensations immersives des jeux vidéo cultes :
- **Grand Theft Auto V** : radar minimap iconique, signalétique sombre ultra-lisible, jingle triomphal "Mission Passed" à l'arrivée.
- **Cyberpunk 2077** : atmosphère nocturne, néons saturés, interfaces cockpit HUD haute technologie.
- **Need for Speed** : véhicules 3D détaillés avec éclairage dynamique sur le bitume, caméra poursuite dynamique inclinée.
- **Minecraft** : mode insolite où le monde réel se transforme en carte voxel avec blocs de terre, herbe, eau et créatures.

### 1.2 Principes Directeurs
1. **Utilité réelle d'abord** : L'application doit être un véritable outil de navigation routière fiable (calcul d'itinéraire temps réel, trafic en direct, alertes radars, limitations de vitesse).
2. **Aucune concession sur l'immersion 3D** : Rejet absolu du simple curseur plat 2D en vue inclinée à 55°. Le véhicule est un véritable objet 3D avec ombrage, reflets et faisceaux de phares projetés sur la route.
3. **Cockpit sombre et sobre** : Pas de surcharge visuelle inutile, chaque élément de l'interface répond à un besoin de conduite immédiat.

---

## 2. Règles Fondamentales de Design HUD & Ergonomie Cockpit

Tout au long du développement de la version WebGL, des directives strictes d'UI/UX ont été définies et doivent être impérativement conservées dans toute future itération :

### 2.1 Rejet du Design "Cards Arrondies IA Générique"
- ❌ **Interdit** : Les boîtes flottantes avec de gros arrondis (`rounded-3xl` / `rounded-2xl`), les ombres portées douces diffuses (`shadow-2xl`) et les fonds translucides style verre d'eau flouté générique.
-  **Exigé** : Un design **flat, anguleux et sombre** évoquant un véritable cockpit de supercar ou un terminal embarqué militaire/course.
  - Couleurs de fond : Noir profond (`#000000`), gris neutral foncé (`#171717` / `bg-neutral-900`).
  - Bordures : Lignes de contour ultra-fines (`border border-neutral-800`).
  - Coins : Rayon discret et contrôlé (`rounded-lg` ou `rounded-xl` maximum).
  - Typographie : Monospace pour les valeurs numériques (vitesse, distance) et sans-serif géométrique haute lisibilité pour les noms de rues.

### 2.2 Agencement Spatial du HUD en Conduite
```
+-------------------------------------------------------------------+
|  [ = ]  Où aller ? (Bordeaux, Arcachon...)        [GPS 5m] [☀] [🔊]|
+-------------------------------------------------------------------+
|  [ ↰ 250m ]                                                       |
|  Prendre à gauche Rue Sainte-Catherine                           |
|                                                                   |
|                                                                   |
|                             [  3D  ]                              |
|                             [ CAR  ]                              |
|                                                                   |
|                                                                   |
|  +-------------+                                                  |
|  |     73      |                                    +----------+  |
|  |    KM/H     |                                    | 18:42    |  |
|  | [50] Limite |                                    | 14.2 km  |  |
|  +-------------+                                    +----------+  |
+-------------------------------------------------------------------+
```

- **Haut - Barre de recherche & État** :
  - Barre de recherche large, épurée et sans encombrement textuel parasite.
  - Indicateurs d'état groupés discrètement en haut à droite : pastille de verrouillage GPS (vert = précision < 10m, jaune = < 30m, rouge = recherche), bascule jour/nuit et statut audio.
- **Haut gauche - Instructions de guidage** :
  - Flèche de manœuvre directionnelle imposante (virage gauche/droite, rond-point avec numéro de sortie).
  - Distance avant manœuvre en gros caractères gras.
  - Nom officiel de la prochaine voie.
- **Bas gauche - Compteur de vitesse dynamique** :
  - Vitesse instantanée GPS en `KM/H` (typographie géométrique géante).
  - Panneau circulaire européen officiel de limitation de vitesse (fond blanc, bordure rouge, chiffre noir).
  - Alerte de dépassement : pulsation rouge du compteur si `vitesse > limite + 3 km/h`.
- **Bas droite - Métriques d'arrivée** :
  - Heure d'arrivée estimée (ETA), distance restante en kilomètres et temps de trajet en minutes.

### 2.3 Navigation des Paramètres en Listes Hiérarchiques Verticales
Les menus de configuration (choix du véhicule, couleurs, thèmes cartographiques, lieux enregistrés) ne doivent **jamais** utiliser de grilles de cartes ou de popups encombrantes.
- Structure en **arborescence verticale** (`Paramètres > Véhicule > Modèle de véhicule`).
- Bouton retour systématique en haut à gauche (`← Retour`).
- Bouton d'action / confirmation pleine largeur en bas de vue.
- Lignes de menu avec séparateurs fins (`divide-y divide-neutral-800`), icônes alignées à gauche et flèches de navigation (`›`) à droite.

---

## 3. Moteur 3D Temps Réel & Modélisation Procédurale (WebGL / Three.js)

Le cœur visuel de la version WebGL repose sur une intégration transparente et synchrone de Three.js directement dans le contexte graphique WebGL de MapLibre GL.

### 3.1 Intégration CustomLayerInterface MapLibre GL
Au lieu d'afficher un canvas HTML superposé au-dessus de la carte (qui provoquerait des désynchronisations de framerate, des décalages de projection et un lag d'affichage), le moteur utilise une couche personnalisée native MapLibre :

```typescript
const vehicleLayer: maplibregl.CustomLayerInterface = {
  id: 'vehicle-3d-layer',
  type: 'custom',
  renderingMode: '3d',
  onAdd: (map, gl) => {
    // Initialisation de la caméra Three.js, scène et WebGLRenderer
    // Partage du contexte WebGL natif fourni par MapLibre
  },
  render: (gl, matrix) => {
    // Calcul de la matrice de transformation Mercator
    // Synchronisation de la caméra Three.js avec painter.transform.mercatorMatrix
    // Rendu synchrone à 60 FPS
  }
};
```

### 3.2 Repère Spatial & Orientation Véhicule (+Z)
Une convention stricte est respectée par l'ensemble des modèles 3D et shaders du projet :
- **Axe `+Z`** : **Avant du véhicule** (sens de la marche / direction du cap GPS).
- **Axe `-Z`** : Arrière du véhicule (feux de recul, aileron, échappements).
- **Axe `+Y`** : Élévation verticale (hauteur du toit, ciel).
- **Axe `+X`** : Flanc droit du véhicule.
- **Axe `-X`** : Flanc gauche du véhicule.
- **Rotation de Cap (Bearing)** : Rotation appliquée sur l'axe Y : `mesh.rotation.y = -(bearing * Math.PI / 180) + Math.PI`.

### 3.3 Projection Mercator & Facteur d'Échelle Dynamique
Pour que le véhicule conserve une taille visuelle harmonieuse et immédiatement identifiable sur l'écran du smartphone quelle que soit la hauteur de caméra ou le niveau de zoom (entre zoom 14 et zoom 19) :
- Les coordonnées GPS WGS84 (`lng`, `lat`) sont converties en coordonnées sphériques Mercator `[0..1]`.
- Facteur de correction de latitude : `meterInMercatorCoordinateUnits = 1 / (2 * Math.PI * 6378137 * Math.cos(latRad))`.
- La taille cible du véhicule à l'écran est stabilisée à environ **58 pixels réels**.
- L'échelle du groupe Three.js est dynamiquement pondérée selon la matrice de projection de la vue.

### 3.4 Système d'Éclairage : Phares Volumétriques & Nappe au Sol
L'éclairage nocturne avant est un facteur d'immersion majeur :
1. **Origine géométrique** : Les deux optiques de phares partent précisément des coordonnées physiques avant de la carrosserie (`z = frontZ`, `y = headlightY`, `x = ±0.7m`).
2. **Faisceaux volumétriques (Cones / Cones tronqués)** :
   - Légère inclinaison vers le bas (pitch d'environ -3° vers la route).
   - Utilisation d'une texture de dégradé linéaire procédurale (`createBeamVolumeTexture`) générée sur canvas HTML5 :
     - Blanc chaud/bleuté au sommet (`rgba(255, 255, 240, 0.45)`).
     - Décroissance exponentielle vers `rgba(255, 255, 255, 0)` sur la base.
     - **Résultat** : Suppression totale de l'effet d'arrondi/disque brutal au bout du cône.
3. **Nappe d'impact lumineuse sur la chaussée (Ground Light Pool)** :
   - Un plan géométrique horizontal (`THREE.PlaneGeometry`) plaqué à fleur de sol (`y = 0.025m` pour éviter tout z-fighting avec la carte vectorielle).
   - Dimensions : ~5 mètres de large, projeté sur 10 à 12 mètres en avant du véhicule.
   - Texture de projection (`createRoadLightTexture`) : dégradé radial étiré en ellipse, simulant la réflexion des phares sur le goudron.
4. **Mode de fusion graphique (Blending)** :
   - Emploi impératif de `THREE.NormalBlending` avec `depthWrite: false`.
   - *Raison technique* : Le mode `AdditiveBlending` provoque sur de nombreux GPU mobiles (Android Mali / Adreno via WebView) des artefacts de solarisation et des contours grisâtres lors de la recomposition de couches.

### 3.5 Catalogue des Modèles Procéduraux 3D (`vehicle3DModels.ts`)
Tous les véhicules sont générés de manière procédurale en combinant des primitives Three.js optimisées (Box, Cylinder, ExtrudeGeometry) sans chargement lourd de fichiers GLTF/OBJ externes :

| Identifiant | Type de Véhicule | Caractéristiques Géométriques & Visuelles |
| :--- | :--- | :--- |
| `car_sport` | Coupé Sport Aérodynamique | Châssis surbaissé, diffuseur arrière, habitacle teinté noir, aileron sport carbone, étriers de freins visibles, jantes à branches. |
| `car_muscle` | Muscle Car Américaine | Lignes carrées agressives, long capot avec prise d'air proéminente (blower), calandre massive, double sortie d'échappement latérale. |
| `car_f1` | Monoplace Formule 1 | Aileron avant large à dérives, pontons étroits, cockpit ouvert avec halo de sécurité, suspensions triangulées apparentes, pneus slicks extra-larges. |
| `car_suv` | 4x4 / Baroudeur Urbain | Garde au sol rehaussée, passages de roues élargis, pare-chocs renforcés, barres de toit utilitaires longitudinales. |
| `car_moto` | Moto Hypersport | Monotrace, carénage plongeant, bulle aérodynamique avant, guidons bracelets, monobras oscillant arrière avec chaîne apparente. |
| `car_cyber` | Supercar Cyberpunk 2077 | Inspirée de la Quadra Turbo-R V-Tech : carrosserie à facettes biseautées, bande LED arrière continue rouge fluo, habitacle jaune fumé. |
| `arrow_gta` | Flèche Radar GTA V | Curseur de navigation emblématique de GTA en relief 3D, biseauté, couleur personnalisable avec liseré contrasté. |
| `arrow_waze` | Flèche Minimaliste Waze | Flèche moderne cyan/bleu électrique, design compact ultra-lisible. |

### 3.6 Studio de Prévisualisation 3D (Turntable `Vehicle3DPreview.tsx`)
Intégré dans le sous-menu des paramètres de véhicule :
- Scène Three.js autonome isolée avec éclairages studio (Key light, Fill light, Rim light bleuté).
- Sol réfléchissant avec grille subtile.
- Détection tactile du glissement horizontal pour faire tourner le véhicule à 360° avec inertie d'amortissement.
- Faisceaux de phares actifs permettant d'apprécier le rendu nocturne.

---

## 4. Moteur Cartographique & Styles Visuels

### 4.1 Tuiles Vectorielles & Filtrage de Voirie
- **Source cartographique** : OpenFreeMap / OpenMapTiles basés sur les données OpenStreetMap globales.
- **Optimisation de la voirie** : Filtrage strict pour éliminer les sentiers pédestres minuscules ou voies de service non carrossables qui alourdiraient le rendu à haute vitesse, tout en préservant scrupuleusement :
  - Autoroutes (`motorway`, `motorway_link`)
  - Voies rapides & Nationales (`trunk`, `primary`)
  - Réseau secondaire & Départementales (`secondary`, `tertiary`)
  - Ronds-points et bretelles d'échangeurs routiers.

### 4.2 Thème GTA V Radar (Minimap)
- **Palette chromatique** :
  - Arrière-plan territorial : Bleu nuit très sombre (`#10131a`).
  - Bitume / Réseau routier : Gris ardoise contrasté (`#2a3242`).
  - Voies rapides / Ponts : Teinte métallique (`#3b485e`).
  - Bâtiments 3D : Volumes extrudés sombres avec opacité de 40% pour ne pas masquer la perspective de route.
  - Tracé d'itinéraire actif : **Violet électrique vibrant (`#c084fc`)**.

### 4.3 Thème Minecraft IRL (Pixel Art & Textures)
- Transformation du paysage urbain réel en monde cubique :
  - Terrains herbeux : Remplis par une texture de motif 16x16 de bloc d'herbe (`minecraft_grass.png`).
  - Plans d'eau, lacs et fleuves : Texture d'eau animée/répétée Minecraft (`minecraft_water.png`).
  - Forêts et parcs : Symboles d'arbres voxel répétitifs (`minecraft_tree.png`).
  - Curseur alternatif : Cochon pixel-art stylisé (`minecraft_pig.png`).
  - Tracé d'itinéraire : **Piste de Redstone rouge vif (`#ef4444`)**.

### 4.4 Thème Waze Nocturne / Cyberpunk
- Contraste maximal pour conduite de nuit :
  - Bitume noir profond (`#0d0f14`).
  - Voirie surlignée de cyan luminescent.
  - Tracé d'itinéraire en bleu cyan électrique (`#38bdf8`).

---

## 5. Moteur de Guidage, Routing & Trafic en Temps Réel

### 5.1 Client OSRM & Décomposition des Manœuvres
- Calcul d'itinéraire moteur automobile via l'API OSRM (`/route/v1/driving/`).
- Parsing des étapes (`steps`) :
  - Détection du type de manœuvre : `turn`, `new name`, `depart`, `arrive`, `merge`, `on ramp`, `off ramp`, `roundabout`.
  - Calcul de l'angle du virage (`modifier` : `sharp left`, `left`, `slight left`, `straight`, `slight right`, `right`, `sharp right`).
  - Extraction de l'indice de sortie dans les carrefours giratoires (ex : *"Prendre la 2ème sortie"*).
- Agrégation de la géométrie complète sous format GeoJSON `LineString`.

### 5.2 Style Visuel du Tracé GPS & Calques de Trafic TomTom
L'intégration du trafic temps réel (TomTom Traffic Flow API) a fait l'objet d'une exigence esthétique très précise :
- **Ligne GPS principale** : Tracé violet vibrant (`#c084fc`), largeur constante de 8px, avec terminaisons arrondies (`round`).
- **Superposition des embouteillages (Bordures de trafic)** :
  - Au lieu de remplacer la couleur violette par du rouge (ce qui ferait perdre l'identité visuelle de l'itinéraire), les portions congestionnées sont entourées de **fines bordures externes** :
    - Ralentissement modéré : Liseré orange vif (`#f97316`).
    - Bouchon important / arrêt complet : Liseré rouge sang (`#ef4444`).
  - *Avantage ergonomique* : Le conducteur continue de suivre la ligne violette tout en percevant instantanément la sévérité du trafic sur les côtés de la voie.

### 5.3 Recalcul Dynamique & Détection de Dérive
- Distance seuil de dérive : Si la position GPS réelle s'éloigne de plus de **35 mètres** du tracé calculé pendant plus de 3 secondes consécutives, un recalcul automatique d'itinéraire est déclenché silencieusement vers la destination cible.

---

## 6. Moteur de Sécurité : Radars & Vitesses Limites

### 6.1 Base Embarquée des Radars Français (`radarsFrance.json`)
- Fichier JSON local contenant l'intégralité des radars homologués en France :
  - Radars fixes classiques (vitesse instantanée).
  - Radars de tronçon (vitesse moyenne).
  - Radars discriminants (séparation poids-lourds / véhicules légers).
  - Radars de franchissement de feu rouge.
- Données embarquées : coordonnées géographiques (`lat`, `lng`), type d'appareil, vitesse limite contrôlée (`speed_limit`), sens de détection.

### 6.2 Requêtes Spatiales & Algorithme d'Alerte Proximité
- Pour préserver les performances CPU de l'appareil mobile :
  - Les radars ne sont scannés que lorsque la carte est à un niveau de zoom suffisant ou lors du déplacement du véhicule.
  - Calcul de distance haversine en temps réel.
  - **Zone d'alerte** : Dès qu'un radar se situe à moins de **800 mètres** devant le véhicule dans l'axe de progression (tolérance d'angle de ±30° par rapport au cap) :
    - Affichage d'un bandeau d'alerte rouge clignotant sur le HUD.
    - Déclenchement d'un signal sonore spécifique (bips modulés).

### 6.3 Indicateur de Limite de Vitesse & Dépassement
- Extraction des limitations de vitesse des segments routiers OpenStreetMap (`maxspeed`).
- Affichage permanent du macaron circulaire sur le HUD à côté de la vitesse instantanée.
- Si `vitesse_actuelle > vitesse_limite + 3 km/h` :
  - Le chiffre de la vitesse instantanée passe en rouge vif clignotant.
  - Bip sonore discret d'avertissement.

---

## 7. Moteur Audio Synthétique Procédural (Web Audio API)

Pour éliminer toute dépendance à des fichiers audio lourds et garantir une latence nulle, tous les sons de l'application sont générés mathématiquement via la Web Audio API native (`AudioContext`).

### 7.1 Synthèse de la Fanfare GTA V "Mission Passed"
Lors du franchissement du point d'arrivée de navigation, le jingle officiel GTA V est reconstitué par synthèse polyphonique :
- **Accords de cuivres (Brass synth)** :
  - Oscillateurs à onde en dent de scie (`sawtooth`) filtrés par un filtre passe-bas (`lowpass`) résonant.
  - Enchaînement harmonique : Accord suspendu mineur suivi d'un accord majeur triomphal avec decay progressif.
- **Basse profonde (Sub-bass)** :
  - Oscillateur à onde sinusoïdale (`sine`) à 55 Hz (Note La / A1) avec enveloppe de gain exponentielle pour donner un impact physique percutant.
- **Nappe de corde orchestrale** :
  - Deux oscillateurs légèrement désaccordés (detune ±7 cents) pour créer un effet de choeur immersif.

### 7.2 Effets Sonores Cockpit & Bips de Radar
- **Clic d'interface HUD** : Impulsion sinusoïdale très brève (8 ms) à 1800 Hz avec atténuation brutale simulant un commutateur mécanique haut de gamme.
- **Alerte Radar** : Double tonalité à 880 Hz (La 4) répétée à intervalles de 500 ms, dont la fréquence de répétition s'accélère à mesure que la distance au radar diminue (800m -> 400m -> 150m).

---

## 8. Gestion des Données Utilisateur & Favoris

- **Stockage local persistant** : Sauvegarde dans le `localStorage` de l'appareil (sans dépendance cloud obligatoire).
- **Raccourcis Dédiés** :
  - **Maison** (icône maison, adresse mémorisée, déclenchement du trajet en 1 clic).
  - **Travail / Bureau** (icône mallette).
- **Favoris Personnalisés** :
  - Ajout de destinations préférées avec libellé sur-mesure (ex : *"Maison de vacances"*, *"Circuit"*, *"Salle de sport"*).
  - Mémorisation de l'historique des 10 dernières recherches avec géocodage inverse automatique.

---

## 9. Analyse Technique : Le Verrou Android Auto & Apple CarPlay

La tentative d'exportation directe du projet WebGL vers Android Auto a révélé une barrière architecturale absolue imposée par les constructeurs automobiles et Google.

### 9.1 Les Restrictions NHTSA / Driver Distraction Mandates
La NHTSA (*National Highway Traffic Safety Administration*) américaine et les normes européennes de sécurité routière imposent aux systèmes d'infodivertissement embarqués des règles draconiennes contre la distraction du conducteur au volant :
- Temps d'attention visuelle maximal toléré sur l'écran : **2 secondes**.
- Interdiction totale des animations graphiques libres, vidéos, scrollings infinis ou scènes 3D interactives non certifiées.

### 9.2 L'Interdiction Stricte des WebViews / WebGL sur Écran Embarqué
- Sur Android Auto comme sur Apple CarPlay, **l'exécution de WebViews ou de canvas WebGL arbitraires est formellement interdite et techniquement bloquée par le système d'exploitation**.
- Une application basée sur Capacitor, Cordova ou un navigateur web ne peut **jamais** afficher son rendu HTML/WebGL sur l'écran de la voiture.
- Android Auto ne transmet à l'écran du tableau de bord aucun contexte WebGL.

### 9.3 Fonctionnement de la Car App Library de Google
Pour publier une application de navigation sur Android Auto (Google Play Store Automotive), les développeurs sont obligés d'utiliser la bibliothèque officielle **Android for Cars App Library** (`androidx.car.app`).

Cette bibliothèque fonctionne selon un paradigme d'affichage restrictif basé sur des **Templates rigides pré-validés** :
1. `NavigationTemplate` : Conteneur réservé à la navigation, affichant obligatoirement des instructions textuelles standardisées, une barre d'actions fixes et une surface de fond.
2. `PaneTemplate` / `ListTemplate` / `GridTemplate` : Listes grises uniformisées avec un nombre maximal d'éléments par page (généralement 6) pour empêcher tout défilement long pendant la conduite.
3. **Le Rendu Carte** : Le fond de carte ne peut être dessiné que sur une `Surface` native Android via un `SurfaceCallback`. Android Auto prend le contrôle de la surface et la projette sur le tableau de bord.

### 9.4 Rétrospective sur le Prototype Hybride Réalisé
Lors des tests sur le projet :
- Le service `GameGpsCarAppService` et l'écran `GameGpsCarScreen` ont bien été reconnus par Android Auto en tant qu'application de navigation.
- Cependant, `GameGpsCarScreen` n'a pu afficher que le `NavigationTemplate` standard de Google avec ses boutons gris système.
- **La scène Three.js WebGL, les phares 3D, les modèles de voitures procéduraux, les textures Minecraft et le design HUD flat personnalisé sont restés confinés à l'écran du smartphone** et ne pouvaient pas être injectés dans le template Android Auto sans un moteur de rendu 100% natif.

---

## 10. Feuille de Route & Architecture Cible pour la Refonte

Face à ce constat, voici les options techniques pour la reconstruction du projet afin de concilier l'expérience visuelle 3D immersive et l'intégration automobile :

### Option A : Le "Dual Mode" (Smartphone Cockpit Dédié + Déport Navigation Auto)
*C'est l'approche la plus pragmatique et la plus spectaculaire, adoptée par de nombreux pilotes et amateurs de sim-racing/tuning :*
- **Écran Smartphone (Support MagSafe / Pare-brise / Tableau de bord)** :
  - Conserve 100% de la richesse visuelle : Moteur 3D temps réel, véhicules procéduraux, éclairage dynamique sur la route, thème GTA/Cyberpunk/Minecraft, HUD flat épuré.
  - Fonctionne en plein écran autonome avec gestion du capteur gyroscopique et GPS haute précision.
- **Écran Voiture (Android Auto / Apple CarPlay)** :
  - Déport d'un flux de navigation épuré conforme à la Car App Library : prochain virage, heure d'arrivée, alertes radars simples.

### Option B : Application 100% Native Android (Kotlin + MapLibre Native + OpenGL ES / Filament)
*Pour porter l'expérience 3D directement sur la `Surface` autorisée d'Android Auto :*
- Remplacement complet de React/Vite/Capacitor par du code Android Natif (Kotlin).
- Utilisation du SDK **MapLibre Native Android** au lieu de MapLibre GL JS.
- Intégration d'un moteur de rendu 3D natif C++/Kotlin (ex: **Google Filament** ou **OpenGL ES**) capable de dessiner la voiture 3D directement dans le buffer de la `Surface` du `NavigationTemplate` d'Android Auto.
- *Difficulté* : Complexité de développement très élevée (réécriture complète des shaders, des mathématiques Mercator et des meshes en Kotlin/C++).

### Option C : Double Rendu Vidéo Déporté (VirtualDisplay / Presentation)
- L'application calcule le rendu graphique sur le smartphone et le diffuse en tant que flux vidéo dynamique projeté sur la surface d'Android Auto.
- *Contrainte* : Nécessite des autorisations système spéciales d'équipementier (OEM) généralement inaccessibles aux applications tierces standard du Play Store.

---

## 11. Références Techniques & Localisation du Code Source

L'intégralité du code source fonctionnel de la version WebGL originale est archivée et accessible à tout moment sur le dépôt Git :
- **Branche Git** : `webgl-version`
- **Commit d'archivage** : `eb7b9eb`
- **Fichiers clés de référence** :
  - `src/services/vehicle3DLayer.ts` : Couche WebGL Three.js native MapLibre.
  - `src/services/vehicle3DModels.ts` : Modélisation 3D procédurale et textures de phares.
  - `src/components/Map.tsx` : Contrôleur de carte et synchronisation du suivi GPS.
  - `src/components/CarDashboard.tsx` : Interface utilisateur cockpit flat design.
  - `src/components/Vehicle3DPreview.tsx` : Plateau tournant 3D studio.
  - `src/services/audio.ts` : Moteur de synthèse Web Audio API.
  - `src/services/radarService.ts` : Moteur spatial d'alertes radars.
  - `src/styles/mapStyles.ts` : Définition des styles GTA, Minecraft et Waze.

---
*Ce document sert de contrat technique officiel et de source de vérité immuable pour les futurs développements du projet Game Maps IRL.*
