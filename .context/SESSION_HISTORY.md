# Session History & Technical Chronology

Ce document retrace l'intégralité des étapes de conception, des demandes utilisateur et des choix techniques effectués lors du développement de **Game Maps IRL**.

---

## 1. Origine du Projet & Thèmes Initiaux
- Création d'une application GPS inspirée de l'esthétique GTA V et des jeux de course.
- Intégration de MapLibre GL avec styles personnalisés (GTA V dark, Cyberpunk néon, Waze clair, Satellite).
- Synthèse sonore procédurale Web Audio API (jingle GTA "Mission Passed" sur arrivée, sons de clic, avertissements de survitesse).

## 2. Évolution du Design vers un Style Plat & Épuré
- **Demande Utilisateur** : *"évite les card arondie c est ca qui fait bcp ai", "supprime l effet card dans les parametre et remet le rappele des icone a droite comme pour le gps ou le point rouge vert ou jaune selon l actife ou non"*.
- **Changements appliqués** :
  - Suppression complète des conteneurs façon "carte flottante" avec gros arrondis (`rounded-3xl`).
  - Passage à des listes épurées, séparées par de fines bordures, avec typographie sobre et icônes d'état alignées à droite (point vert/jaune/rouge, indicateur de position).
  - Agrandissement de la barre de recherche principale pour une accessibilité immédiate.

## 3. Gestion des Lieux Enregistrés (Favoris)
- **Demande Utilisateur** : *"ok faut pouvoire ajouter une maison et un travaille et des leiux enregistrer"*.
- **Implémentation** :
  - Création d'un sous-menu `Paramètres > Lieux enregistrés`.
  - Entrées rapides par défaut : **Maison** (avec icône Home) et **Travail** (avec icône Briefcase).
  - Possibilité d'ajouter des favoris personnalisés avec géolocalisation actuelle ou saisie d'adresse.
  - Sauvegarde persistante dans `localStorage`.

## 4. Modélisation 3D Complète (Three.js & MapLibre)
- **Demande Utilisateur** : *"ok juste en vue 55 les vheicule sont plat donc moche et pour les parametre fait un sous dossier pour avoire la liste des vheicule en liste et pas en tableau"*, *"en 3d les vheicule car sinon je trouve ca moche"*.
- **Implémentation** :
  - Création du `Vehicle3DLayer` (`src/services/vehicle3DLayer.ts`) qui s'enregistre comme `CustomLayerInterface` dans MapLibre GL avec `renderingMode: '3d'`.
  - Rendu Three.js synchronisé avec le contexte WebGL de MapLibre en utilisant `painter.transform.mercatorMatrix`.
  - Création d'une bibliothèque procédurale de véhicules dans `src/services/vehicle3DModels.ts` :
    - `car_sport` : Supercar GT profilée avec grand aileron arrière en carbone et diffuseur.
    - `car_muscle` : Muscle Car américaine V8 avec supercharger sur le capot.
    - `car_f1` : Monoplace Formula 1 avec ailerons aéro, halo et pneus slick larges.
    - `car_suv` : 4x4 Offroad surélevé avec galerie de toit et roue de secours.
    - `car_moto` : Superbike de compétition avec silhouette de pilote casqué.
    - `car_cyber` : Vaisseau antigravité cyberpunk avec 4 réacteurs néon.
    - `arrow_gta` / `arrow_waze` : Curseurs radar biseautés en volume 3D avec ombre portée.
    - `minecraft_arrow` : Curseur voxel pixelisé en relief.
  - Création du composant `Vehicle3DPreview.tsx` dans `Paramètres > Véhicule` avec plateau tournant 3D tactile interactif.
  - Réorganisation des paramètres en sous-dossier avec liste verticale épurée.

## 5. Correction et Perfectionnement des Phares
- **Demande Utilisateur** : *"ok les phare sans pas dans le bon sens"*.
- **Diagnostic** :
  - Les cônes 3D Three.js inversaient la base et l'apex lors de la rotation, créant l'illusion d'entonnoirs ou d'aiguilles pointant vers l'arrière, tout en affichant des disques circulaires tranchés à leur extrémité.
  - Le mode de fusion additif (`AdditiveBlending`) combiné au compositeur d'Android WebView assombrissait les pixels transparents en gris sombre.
- **Solution finale implémentée** :
  - Faisceau volumétrique avec texture de dégradé linéaire (`createBeamVolumeTexture`) s'estompant de 65% d'opacité à la lentille jusqu'à 0.0% à l'extrémité (aucun contour circulaire visible).
  - Nappe lumineuse au sol (`createRoadLightTexture`) projetée à plat sur le bitume (`y = 0.025`) devant le véhicule sur 10 à 12 mètres pour éclairer la chaussée en temps réel.
  - Utilisation de `THREE.NormalBlending` pour un affichage éclatant et sans artefact sur mobile.
  - Orientation validée à la fois sur la carte en temps réel et dans l'aperçu 3D du plateau tournant.
