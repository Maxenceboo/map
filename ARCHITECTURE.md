# Architecture — Vektor GPS (version native Kotlin)

Refonte 100 % native (option B du [cahier des charges](./CAHIER_DES_CHARGES.md) §10) :
Kotlin + Jetpack Compose sur le téléphone, MapLibre Native pour la carte, Car App Library pour Android Auto.
Plus de WebView, plus de React.

## Principe : un fichier = une responsabilité

Le code est découpé en couches. Chaque couche ne connaît que celles **en dessous** d'elle :

```
        ┌──────────────┐      ┌──────────────┐
        │  ui/ (phone) │      │  car/ (auto) │   ← affichage
        └──────┬───────┘      └──────┬───────┘
               └──────────┬──────────┘
                    ┌─────┴─────┐
                    │   map/    │   ← carte MapLibre partagée
                    └─────┬─────┘
                 ┌────────┴────────┐
                 │   navigation/   │   ← le "cerveau" du guidage
                 └────────┬────────┘
                    ┌─────┴─────┐
                    │   data/   │   ← GPS, réseau, recherche, itinéraires
                    └─────┬─────┘
                    ┌─────┴─────┐
                    │   core/   │   ← maths et formatage, sans Android
                    └───────────┘
```

Le téléphone et la voiture partagent **les mêmes objets** (créés une fois dans `di/AppContainer.kt`) :
un itinéraire lancé sur le téléphone apparaît sur Android Auto, et inversement.

## Arborescence

Racine des sources : `app/src/main/java/com/gamemaps/irl/`

### Démarrage
| Fichier | Rôle |
| :--- | :--- |
| `GameMapsApp.kt` | Application : initialise MapLibre, crée le conteneur. |
| `MainActivity.kt` | Écran unique du téléphone. |
| `di/AppContainer.kt` | Crée et partage tous les services (GPS, recherche, routage, guidage). |
| `di/ContextExtensions.kt` | `context.appContainer` depuis n'importe où. |

### `core/` — Kotlin pur, testable sans téléphone
| Fichier | Rôle |
| :--- | :--- |
| `geo/LatLng.kt` | Coordonnée GPS (type maison, indépendant de MapLibre). |
| `geo/GeoMath.kt` | Distance (haversine) et cap entre deux points. |
| `geo/PolylineProjector.kt` | Projette une position sur le tracé : distance au tracé, distance parcourue. |
| `geo/PolylineProjection.kt` | Résultat de cette projection. |
| `geo/BoundingBox.kt` | Rectangle géographique (zone chargée autour du véhicule). |
| `format/DistanceFormatter.kt` | "250 m", "1,2 km". |
| `format/DurationFormatter.kt` | "8 min", "1 h 05". |
| `format/ArrivalTimeFormatter.kt` | "18:42". |

### `data/` — Accès au monde extérieur
| Fichier | Rôle |
| :--- | :--- |
| `location/GpsFix.kt` | Une mesure GPS (position, vitesse, cap, précision). |
| `location/GpsQuality.kt` | Vert / jaune / rouge selon la précision. |
| `location/LocationSource.kt` | Interface d'une source de positions. |
| `location/AndroidLocationSource.kt` | Implémentation via le `LocationManager` Android. |
| `location/LocationMapper.kt` | `Location` Android → `GpsFix`. |
| `location/HeadingStabilizer.kt` | Garde le dernier cap fiable à l'arrêt (anti-toupie). |
| `location/SpeedSmoother.kt` | Vitesse lissée, 0 sous 3 km/h (pas de vitesse fantôme). |
| `location/StationaryPositionFilter.kt` | Position figée à l'arrêt (ignore le bruit GPS de quelques mètres). |
| `location/LocationRepository.kt` | Position partagée (`StateFlow`) pour toute l'app, filtrée (vitesse, arrêt, cap). |
| `location/DemoLocationSource.kt` | Position de démonstration : le véhicule sur les Champs-Élysées (mode développeur). |
| `location/SwitchableLocationSource.kt` | Bascule entre le vrai GPS et la position de démonstration. |
| `location/LocationPermissions.kt` | Liste et vérification des permissions. |
| `settings/AudioPreferences.kt` | Son coupé ou non, mémorisé. |
| `settings/AppSettings.kt` | Réglages : thème, perspective, voix, bips, alertes radar. |
| `settings/Perspective.kt` | 3D cockpit / 2D vue de dessus. |
| `settings/SettingsRepository.kt` | Réglages mémorisés (`StateFlow`), partagés avec la voiture. |
| `network/HttpClientFactory.kt` | Client OkHttp unique. |
| `network/HttpGet.kt` | GET annulable en coroutine. |
| `network/UserAgentInterceptor.kt` | User-Agent identifiable (demandé par OSRM / IGN). |
| `network/HttpException.kt` | Erreur HTTP ; ne cite que le serveur, jamais l'adresse complète (position GPS, clé d'API). |
| `search/Place.kt` | Un lieu (nom, adresse, position, nature, catégorie OSM). |
| `search/PlaceKind.kt` | Adresse, rue, ville, point d'intérêt. |
| `search/PlaceSearch.kt` | Interface de recherche. |
| `search/HybridPlaceSearch.kt` | BAN + Photon en parallèle, fusion, classement ; un moteur en panne ne bloque pas l'autre. |
| `search/ban/BanGeocoder.kt` | Adresses via la Base Adresse Nationale (IGN). |
| `search/ban/BanResponseParser.kt` | JSON BAN → `Place`. |
| `search/photon/PhotonGeocoder.kt` | Lieux (gares, magasins, stations…) via Photon / OpenStreetMap, objets inutiles exclus. |
| `search/photon/PhotonResponseParser.kt` | JSON Photon → `Place`. |
| `search/ranking/PlaceRanker.kt` | Score : texte + proximité + utilité du lieu. |
| `search/ranking/PlaceDeduplicator.kt` | Même nom à < 400 m = doublon (gare vs arrêt de tram). |
| `search/ranking/CategoryPriority.kt` | Gare / aéroport > magasin > arrêt de bus. |
| `search/ranking/TextNormalizer.kt` | "Saint-Jean" ≈ "saint jean" (accents, tirets). |
| `places/SavedPlaces.kt` | Maison, Travail, favoris. |
| `places/SavedPlacesRepository.kt` | Sauvegarde sur le téléphone (`StateFlow`), partagée avec la voiture. |
| `places/SavedPlacesSerializer.kt` | `SavedPlaces` ⇄ JSON. |
| `routing/Route.kt` / `RouteStep.kt` | Itinéraire et ses étapes. |
| `routing/ManeuverType.kt` | Types de manœuvres (indépendants du moteur). |
| `routing/RoutingService.kt` | Interface de calcul d'itinéraire. |
| `routing/TrafficAwareRoutingService.kt` | TomTom si une clé est enregistrée et le trafic activé, sinon OSRM ; repli sur OSRM si TomTom échoue. |
| `routing/osrm/OsrmClient.kt` | Appel au serveur OSRM. |
| `routing/osrm/OsrmResponseParser.kt` | JSON OSRM → `Route`. |
| `routing/osrm/OsrmManeuverMapper.kt` | Manœuvres OSRM → `ManeuverType`. |
| `routing/tomtom/TomTomKeyStore.kt` | Clé d'API collée dans les Paramètres, gardée sur le téléphone (ni dans le code, ni dans l'APK, ni dans les sauvegardes). |
| `routing/tomtom/TomTomKeyFormat.kt` | Nettoie, contrôle et masque la clé saisie. |
| `routing/tomtom/TomTomClient.kt` | Appel à TomTom : trajet le plus rapide compte tenu des bouchons. |
| `routing/tomtom/TomTomResponseParser.kt` | JSON TomTom → `Route` (tracé, durée, retard). |
| `routing/tomtom/TomTomStepBuilder.kt` | Instructions TomTom → étapes placées le long du tracé. |
| `routing/tomtom/TomTomTrafficParser.kt` | Sections `TRAFFIC` → portions ralenties. |
| `routing/tomtom/TomTomManeuverMapper.kt` | Codes de manœuvre TomTom → `ManeuverType`. |
| `routing/tomtom/TomTomJson.kt` | Petit utilitaire de lecture des tableaux JSON. |
| `traffic/TrafficSection.kt` | Portion ralentie : indices dans le tracé, gravité, retard. |
| `traffic/TrafficSeverity.kt` | Ralentissement (orange) ou bouchon (rouge). |
| `traffic/TrafficSeverityClassifier.kt` | Seuils : bouchon si ≤ 18 km/h, retard ≥ 2 min ou magnitude ≥ 2. |
| `speedlimit/SpeedLimitRepository.kt` | Limitation de la route actuelle (`StateFlow`), recharge la zone en approchant du bord. |
| `speedlimit/OverpassResponseParser.kt` | JSON Overpass → `RoadSegment`. |
| `speedlimit/MaxSpeedParser.kt` | "50", "FR:urban", "30 mph" → km/h. |
| `speedlimit/DefaultSpeedLimits.kt` | Limitation estimée d'après le type de route quand aucun panneau n'est renseigné. |
| `speedlimit/SpeedLimit.kt` | Limitation affichée : valeur + « estimée » ou non. |
| `speedlimit/SpeedLimitIndex.kt` | Route la plus proche, départagée par le cap aux carrefours. |
| `speedlimit/RoadSegment.kt` | Une route OSM et sa limitation. |
| `osm/OverpassClient.kt` | Appels à l'API Overpass (OSM) : une requête à la fois, nouvelles tentatives si surcharge. |
| `osm/OverpassQueries.kt` | Requêtes Overpass : routes avec `maxspeed`, radars. |
| `radar/Radar.kt` | Un radar (position, type, vitesse contrôlée, route). |
| `radar/RadarType.kt` | Vitesse, feu rouge, discriminant, tronçon, passage à niveau. |
| `radar/official/OfficialRadarDatabase.kt` | Base officielle embarquée (3 350 radars, hors ligne), chargée une fois. |
| `radar/official/OfficialRadarParser.kt` | `assets/radars_france.json` → `Radar`. |
| `radar/RadarResponseParser.kt` | JSON Overpass (OSM) → `Radar`. |
| `radar/RadarMerger.kt` | Base officielle + radars OSM non doublons (> 60 m). |
| `radar/RadarRepository.kt` | Radars d'une zone de ~10 km + alerte en cours (`StateFlow`). |

### `data/custom/` — Thèmes et véhicules créés en mode développeur
Le mode développeur s'active par 7 appuis sur la version (Paramètres > À propos).

| Fichier | Rôle |
| :--- | :--- |
| `CustomThemeSpec.kt` | Un thème créé : nom + couleurs ; converti en `MapTheme`. |
| `ThemeColorSlot.kt` | Les couleurs réglables et celles qui en découlent (contour des textes, liseré du tracé). |
| `CustomVehicleSpec.kt` | Un véhicule créé : nom + mesures, avec leurs limites. |
| `CustomVehicleBuilder.kt` | Mesures → modèle 3D (caisse, habitacle, roues, feux, aileron). |
| `CustomContentSerializer.kt` | Thèmes et véhicules ⇄ JSON. |
| `CustomContentRepository.kt` | Enregistrement sur le téléphone ; retrouve un thème ou un véhicule par identifiant. |
| `ColorMath.kt` | Contrôle d'un code couleur, assombrissement. |

### `navigation/` — Le guidage
| Fichier | Rôle |
| :--- | :--- |
| `NavigationState.kt` | Idle → Calculating → Previewing → Navigating → Arrived / Failed. |
| `NavigationEngine.kt` | Calcul, aperçu puis `confirm()`, suivi GPS, recalcul, arrivée. Départ direct (`autoStart`) depuis la voiture. |
| `RouteProgress.kt` | Où on en est : prochaine manœuvre, distance, temps restant. |
| `RouteProgressCalculator.kt` | Calcule `RouteProgress` à partir d'une position. |
| `OffRouteDetector.kt` | > 35 m du tracé pendant > 3 s, en roulant ⇒ recalcul (§5.3). |
| `ArrivalDetector.kt` | < 30 m de l'arrivée ⇒ arrivé. |
| `trip/TripRecorder.kt` | Distance réellement parcourue et durée (sauts GPS > 250 km/h ignorés). |
| `trip/TripStats.kt` | Bilan du trajet (distance, durée, moyenne). |
| `trip/MissionPassedModel.kt` | Bilan mis en forme (téléphone + voiture). |
| `SpeedingDetector.kt` | Excès de vitesse : limite + 3 km/h (§6.3). |
| `snap/RoadSnapper.kt` | Aimante le véhicule affiché à l'itinéraire, ou à la route la plus proche. |
| `snap/SnapCandidate.kt` | Point d'une route où poser le véhicule, et direction de la route. |
| `radar/RadarAlertDetector.kt` | Entrée et sortie d'une zone de danger (§6.2) : les trois quarts de la zone avant le point de contrôle, un quart après. |
| `radar/RadarAlert.kt` | « On est dans une zone de danger » : identifiant de zone et vitesse autorisée, sans emplacement ni distance. |
| `radar/DangerZoneSize.kt` | Longueur d'une zone : 4 km sur autoroute, 2 km hors agglomération, 300 m en ville. |
| `instructions/InstructionTextBuilder.kt` | "Au rond-point, prenez la 2e sortie vers D1010". |

### `map/` — Carte MapLibre (partagée téléphone + voiture)
| Fichier | Rôle |
| :--- | :--- |
| `MapSetup.kt` | Charge le style, applique le thème, installe les calques. |
| `MapController.kt` | Façade : véhicule, tracé + destination, radars, vue d'ensemble, recentrage, thème et perspective à chaud. |
| `MapLibreConversions.kt` | Notre `LatLng` → types MapLibre / GeoJSON. |
| `style/MapStyleSource.kt` | URL du fond de carte OpenFreeMap. |
| `theme/MapTheme.kt` | Thèmes Radar nocturne, Néon cyan, Monde cubique (§4). |
| `theme/MapTextures.kt` | Textures d'un thème (herbe, eau, arbres, sprite du véhicule). |
| `theme/ThemeTextureInstaller.kt` | Ajoute les textures au style comme motifs (pixels nets). |
| `theme/ThemePatterns.kt` | Motifs installés pour le thème actif. |
| `theme/MapPalette.kt` | Couleurs d'un thème. |
| `theme/MapThemeApplier.kt` | Repeint chaque calque du style selon la palette. |
| `layers/RouteLayer.kt` | Tracé violet + liseré. |
| `layers/TrafficLayer.kt` | Bordures orange / rouges autour du tracé sur les portions ralenties. |
| `layers/AlternativeRoutesLayer.kt` | Dans l'aperçu, les trajets proposés mais non choisis, en gris. |
| `layers/DestinationLayer.kt` | Épingle de destination au bout du tracé. |
| `layers/DestinationPinBitmap.kt` | Dessin de l'épingle. |
| `layers/LayerOrder.kt` | Place le tracé sous les noms de rues. |
| `VehicleAnimator.kt` | Fait glisser le véhicule et la caméra d'une position GPS à la suivante. |
| `camera/FollowCamera.kt` | Suivi incliné, pause au doigt (`isFollowing`), recentrage, vue d'ensemble d'un trajet. |
| `camera/CameraConfig.kt` | Réglages téléphone / voiture, variante 2D vue de dessus. |

### `map/vehicle3d/` — Véhicule 3D
Le véhicule est un assemblage de volumes (`fill-extrusion` MapLibre), comme les bâtiments :
pas de second moteur 3D, donc rendu identique sur le téléphone et sur Android Auto.

| Fichier | Rôle |
| :--- | :--- |
| `VehiclePart.kt` | Une pièce : contour au sol (mètres, +Z vers l'avant, +X à droite), hauteur basse / haute, rôle. |
| `PartRole.kt` | Rôle d'une pièce (carrosserie, vitre, pneu, feu arrière…) : il décide de sa couleur. |
| `VehicleShapes.kt` | Formes de base : boîte, carrosserie effilée, symétrie gauche / droite, quatre roues. |
| `VehicleModel.kt` | Un véhicule = liste de pièces + position des phares. |
| `models/SportCar.kt`, `MuscleCar.kt`, `Suv.kt`, `FormulaOne.kt`, `Motorbike.kt` | Un fichier par modèle. |
| `VehicleKind.kt` | Catalogue proposé dans les Paramètres ; tous en 3D, flèche comprise. |
| `models/Arrow.kt` | Flèche en relief, façon mini-carte de jeu. |
| `models/MinecraftPig.kt` | Cochon en cubes, imposé par le thème Monde cubique. |
| `VehicleColor.kt` | Couleurs de carrosserie. |
| `VehiclePalette.kt` | Couleur de chaque rôle (la carrosserie prend la couleur choisie). |
| `VehicleScale.kt` | Agrandit le modèle selon le zoom pour garder la même taille à l'écran. |
| `VehicleGeometry.kt` | Pose le modèle sur la carte : rotation selon le cap, mètres → latitude / longitude. |
| `PlacedPart.kt` | Une pièce une fois posée sur la carte. |
| `HeadlightBeams.kt` | Lumière des phares : lueur arrondie au sol + volume translucide. |
| `PlacedBeam.kt` | Un morceau de lumière une fois posé sur la carte. |
| `Vehicle3DLayer.kt` | Calques MapLibre : volumes du véhicule + faisceaux au sol, mis à jour à chaque position et à chaque zoom. |

### `audio/` — Sons et guidage vocal
| Fichier | Rôle |
| :--- | :--- |
| `AudioController.kt` | Écoute guidage, radars et vitesse ; joue les annonces (sauf son coupé). Un seul pour téléphone + voiture. |
| `AudioCueFilter.kt` | Retire ce qui est désactivé dans les Paramètres (voix, bips). |
| `cue/AudioCue.kt` | Une annonce : un son ou une phrase. |
| `cue/SoundEffect.kt` | Liste des sons (départ, virage, fanfare d'arrivée, radar, excès). |
| `announcers/GuidanceAnnouncer.kt` | Départ, "Dans 500 mètres…", carillon + instruction avant la manœuvre, recalcul, arrivée. |
| `announcers/RadarAnnouncer.kt` | Double bip + « Zone de danger » à l'entrée d'une zone, une seule fois. |
| `announcers/SpeedingAnnouncer.kt` | Bip au dépassement, répété toutes les 15 s. |
| `announcers/SpeechDistanceFormatter.kt` | "300 mètres", "1,5 kilomètres". |
| `synth/ToneSynth.kt` | Synthétiseur : notes → échantillons PCM (aucun fichier audio). |
| `synth/SoundEffectTones.kt` | Partition de chaque son (reprise de la version WebGL). |
| `synth/MissionPassedScore.kt` | Fanfare d'arrivée composée pour l'app : montée de quatre notes, accord tenu, basse, scintillement. |
| `synth/Tone.kt`, `synth/Waveform.kt` | Une note (avec passe-bas optionnel) ; formes d'onde sinus / triangle / dent de scie. |
| `playback/TonePlayer.kt` | Joue un son via `AudioTrack`. |
| `playback/VoiceGuide.kt` | Synthèse vocale française (`TextToSpeech`). |
| `playback/AndroidAudioOutput.kt` | Enchaîne sons et phrases (la voix attend la fin du carillon). |
| `playback/NavigationAudioFocus.kt` | Baisse la musique pendant une annonce. |
| `playback/NavigationAudioAttributes.kt` | Usage "guidage de navigation" (routage vers la voiture en Android Auto). |
| `playback/AudioOutput.kt` | Interface de sortie audio. |

### `ui/` — Téléphone (Jetpack Compose)
| Fichier | Rôle |
| :--- | :--- |
| `screen/MainScreen.kt` | Carte plein écran + HUD (§2.2). |
| `screen/MainViewModel.kt` | Recherche avec anti-rebond, démarrage / arrêt du guidage. |
| `screen/MainUiState.kt` | Tout l'état de l'écran. |
| `screen/DrivingState.kt` | Position, limitation, radars (avec ou sans guidage). |
| `screen/MapRenderEffect.kt` | Pousse position, tracé, radars et cadrage (aperçu / suivi) vers la carte. |
| `screen/KeepScreenOnEffect.kt` | Écran toujours allumé tant que l'app est affichée. |
| `map/MapViewHost.kt` | MapView dans Compose. |
| `map/MapViewLifecycleObserver.kt` | Cycle de vie de la MapView. |
| `hud/HudModel.kt` | Contenu du guidage prêt à afficher (manœuvre, distances, heures). |
| `hud/ManeuverBanner.kt` | Haut : flèche sur fond jaune, distance, instruction. |
| `hud/ManeuverIcon.kt` | Dessin vectoriel de la manœuvre (flèche, demi-tour, rond-point avec n° de sortie). |
| `hud/ManeuverArrowShape.kt` | Tracé de chaque flèche sur une grille 24 × 24. |
| `hud/SpeedGauge.kt` | Compteur rond (bas gauche), rouge en excès, panneau de limitation accroché. |
| `hud/SpeedLimitSign.kt` | Panneau rond blanc / rouge de limitation. |
| `hud/RadarAlertBanner.kt` | Bandeau « Zone de danger » avec la vitesse autorisée ; ni emplacement ni distance du contrôle. |
| `hud/TripBar.kt` | Barre du bas en guidage : arrêter, heure d'arrivée, temps et distance restants, son. |
| `hud/GpsStatusDot.kt` | Pastille GPS. |
| `hud/StatusBanner.kt` | Message du haut : attente (indicateur qui tourne) ou erreur (croix pour fermer). |
| `hud/MissionPassedOverlay.kt` | Écran « MISSION ACCOMPLIE » doré, bilan du trajet. |
| `hud/MuteButton.kt` | Bouton rond du son (hors guidage). |
| `hud/PreviewPanel.kt` | Aperçu : destination, durée, distance, arrivée, DÉMARRER / ANNULER. |
| `hud/PreviewModel.kt` | Textes de l'aperçu. |
| `hud/RouteOptionChips.kt` | Choix entre les trajets proposés (durée, distance, nature). |
| `hud/PlaceSaveActions.kt` | Pastille « Ajouter aux favoris » de l'aperçu. |
| `hud/RecenterButton.kt` | Bouton rond jaune « viseur » quand la carte a été déplacée. |
| `search/SearchBar.kt` | Barre "Où aller ?" avec le bouton du menu (Paramètres) intégré. |
| `search/PlaceIcons.kt` | Icône vectorielle d'un résultat selon sa catégorie (gare, station-service, restaurant…). |
| `search/SearchResultsList.kt` | Résultats en liste verticale (§2.3) : repère, adresse, distance. |
| `search/SavedPlacesShortcuts.kt` | Raccourcis Maison / Travail / favoris sous la barre de recherche. |
| `search/SearchUiState.kt` | État de la recherche. |
| `components/CockpitPanel.kt` | Carte du HUD + formes partagées (`HudShapes`). |
| `components/HudSurface.kt` | Apparence commune : fond opaque et ombre, sans bordure. |
| `components/HudIconButton.kt` | Bouton rond avec icône. |
| `icons/HudIcons.kt` | Icônes vectorielles maison du HUD et des Paramètres (son, viseur, voiture, feu tricolore…). |
| `permissions/LocationPermissionEffect.kt` | Demande ce qui manque : localisation, notifications (Android 13+). |
| `theme/CockpitColors.kt`, `CockpitTypography.kt`, `GameMapsTheme.kt` | Design system : surfaces bleu nuit, accent jaune, police système. |

### `service/` — Guidage en arrière-plan
| Fichier | Rôle |
| :--- | :--- |
| `NavigationForegroundService.kt` | Service au premier plan (localisation) pendant un trajet : GPS et voix continuent écran éteint. |
| `NavigationServiceController.kt` | Démarre le service au début du trajet, l'arrête à la fin. |
| `notification/NavigationNotificationContent.kt` | Textes : "180 m · Tournez à droite…", "Arrivée 17:47 · 57 km · 1 h 05". |
| `notification/NavigationNotificationBuilder.kt` | Notification permanente, bouton "Arrêter". |
| `notification/NavigationNotificationChannel.kt` | Canal "Guidage" silencieux. |

### `ui/settings/` — Menu Paramètres (listes verticales, §2.3)
| Fichier | Rôle |
| :--- | :--- |
| `SettingsScreen.kt` | Menu plein écran, navigation entre sections, bouton retour. |
| `SettingsViewModel.kt` | Lit et modifie réglages, son et lieux enregistrés. |
| `SettingsSection.kt` | Racine, Thème, Perspective, Véhicule (> Modèle, Couleur), Audio, Radars, Trafic, Lieux, À propos. |
| `SettingsUiState.kt` | Ce qu'affiche le menu. |
| `sections/*.kt` | Un fichier par écran du menu. |
| `components/SettingsHeader.kt` | Bouton retour rond + titre. |
| `components/SettingsInputs.kt` | Champ de texte et curseur de réglage. |
| `components/VehiclePreview.kt` | Aperçu 3D d'un véhicule (vue de trois quarts arrière), sans la carte. |
| `components/ThemePreview.kt` | Aperçu d'un thème : une mini-carte peinte avec ses couleurs. |
| `components/ColorPickerRow.kt` | Couleur réglable : grille de couleurs prêtes + code `#RRGGBB`. |
| `sections/DevThemeSections.kt`, `DevVehicleSections.kt` | Listes et éditeurs du mode développeur. |
| `components/SettingsGroup.kt` | Carte arrondie qui regroupe les lignes d'un même sujet. |
| `components/TomTomKeyField.kt` | Champ masqué + COLLER / ENREGISTRER pour la clé TomTom. |
| `components/SettingsRows.kt` | Lignes : sous-menu, interrupteur, choix, info avec action ; icône vectorielle dans une tuile. |

### `car/` — Android Auto
| Fichier | Rôle |
| :--- | :--- |
| `intent/NavigationRequestParser.kt` | Lit une demande de l'assistant (`geo:…`) : un point ou un texte à chercher. |
| `intent/NavigationRequest.kt` | Les deux formes de demande. |
| `GameMapsCarAppService.kt` | Service déclaré dans le manifeste ; Android Auto s'y connecte. |
| `GameMapsCarSession.kt` | Une connexion voiture ; crée le premier écran et traite les demandes de l'assistant. |
| `screens/NavigationCarScreen.kt` | Écran principal : carte + template selon l'état du guidage. |
| `screens/CarSearchScreen.kt` | Recherche de destination (clavier / voix de la voiture). |
| `surface/CarMapSurface.kt` | **Carte MapLibre sur l'écran de la voiture** (écran virtuel + Presentation). |
| `surface/CarSpeedView.kt` | Compteur de vitesse et panneau de limitation dessinés par-dessus la carte de la voiture. |
| `trip/CarTripReporter.kt` | Informe Android Auto du guidage en cours (`NavigationManager`). |
| `alerts/CarRadarAlerter.kt` | Alerte radar par-dessus la carte (API ≥ 5), sinon message court. |
| `alerts/CarRadarAlertPolicy.kt` | Quand afficher / réafficher (urgent) / retirer l'alerte. |
| `templates/IdleTemplate.kt` | Sans trajet : menu permanent à gauche (recherche, Maison, Travail, favoris), ou carte seule. |
| `templates/CalculatingTemplate.kt` | Chargement. |
| `templates/PreviewTemplate.kt` | Choix du trajet : liste des trajets proposés, Démarrer, croix pour annuler. |
| `templates/NavigatingTemplate.kt` | Manœuvre + estimation d'arrivée + "Arrêter". |
| `templates/MessageTemplates.kt` | Arrivée, erreur, permission. |
| `templates/PlaceListBuilder.kt` | Résultats de recherche ; Maison / Travail / favoris quand rien n'est tapé. |
| `templates/CarActions.kt` | Boutons communs. |
| `templates/CarIcons.kt` | Icônes des boutons voiture : maison, mallette, croix. |
| `mapping/CarManeuverMapper.kt` | `ManeuverType` → type Android Auto. |
| `mapping/CarStepMapper.kt` | `RouteStep` → `Step` Android Auto. |
| `mapping/CarDistanceMapper.kt` | Mètres → `Distance`. |
| `mapping/CarTravelEstimateMapper.kt` | Restant + heure d'arrivée. |
| `mapping/ManeuverIconFactory.kt` | Icône de manœuvre : mêmes flèches vectorielles que le téléphone (mise en cache). |

## Comment la carte arrive dans la voiture

Android Auto interdit WebView et WebGL (§9), mais donne aux apps de navigation une **Surface** native.
`CarMapSurface` crée un *écran virtuel privé* qui dessine dans cette Surface, puis y affiche une
`Presentation` contenant une `MapView` MapLibre ordinaire, configurée par le même `MapSetup` que le téléphone.
Les templates (manœuvre, boutons) sont dessinés **par Android Auto** par-dessus.

## Services externes (gratuits ; seul TomTom demande une clé)

| Besoin | Service |
| :--- | :--- |
| Fond de carte | OpenFreeMap (tuiles OpenStreetMap) |
| Recherche d'adresses | Base Adresse Nationale — `data.geopf.fr/geocodage` |
| Recherche de lieux | Photon — `photon.komoot.io` (OpenStreetMap) |
| Itinéraire avec trafic | TomTom Routing — `api.tomtom.com` (optionnel, clé gratuite) |
| Itinéraire sans trafic | OSRM public — `router.project-osrm.org` (démo ; utilisé sans clé TomTom ou en repli) |
| Radars (base officielle) | `app/src/main/assets/radars_france.json`, généré par `tools/convert_radars.py` |
| Limitations de vitesse, radars complémentaires | Overpass API — `overpass-api.de` (tags OSM `maxspeed`, nœuds `highway=speed_camera`) |

## Construire, tester, lancer

```bash
# Gradle 9.6 + AGP 9.4 : fonctionne avec le JDK 25 fourni par Android Studio
./gradlew :app:testDebugUnitTest   # tests unitaires (core, data, navigation)
./gradlew :app:assembleDebug       # APK : app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug        # installe sur le téléphone branché
```

Tester Android Auto sans voiture : **Desktop Head Unit (DHU)**
1. SDK Manager → SDK Tools → cocher *Android Auto Desktop Head Unit Emulator*.
2. Sur le téléphone : app Android Auto → taper 10× sur la version → *Mode développeur* →
   menu ⋮ → *Démarrer le serveur de l'unité principale*.
   Activer aussi *Sources inconnues* dans les paramètres développeur d'Android Auto.
3. `adb forward tcp:5277 tcp:5277` puis lancer `desktop-head-unit.exe` (dans `extras/google/auto`).

## Activer le trafic TomTom

1. Créer une clé gratuite sur <https://developer.tomtom.com> (produit *Routing API*).
2. Copier la clé, puis dans l'app : *Paramètres > Trafic* → COLLER → ENREGISTRER.

La clé reste dans le stockage privé de l'app sur le téléphone (`res/xml/backup_rules.xml` l'exclut des
sauvegardes). Elle n'est ni dans le code ni dans l'APK : l'APK peut être partagé sans risque.
Sans clé, tout fonctionne avec OSRM, simplement sans bouchons.

## Prochaines étapes

1. Calque de trafic sur toutes les routes de la carte (tuiles TomTom Traffic Flow).
2. Mode hors ligne (carte, recherche et itinéraire ont besoin du réseau).
