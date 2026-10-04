# Architecture — Game Maps IRL (version native Kotlin)

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
| `location/LocationPermissions.kt` | Liste et vérification des permissions. |
| `settings/AudioPreferences.kt` | Son coupé ou non, mémorisé. |
| `network/HttpClientFactory.kt` | Client OkHttp unique. |
| `network/HttpGet.kt` | GET annulable en coroutine. |
| `network/UserAgentInterceptor.kt` | User-Agent identifiable (demandé par OSRM / IGN). |
| `network/HttpException.kt` | Erreur HTTP. |
| `search/Place.kt` | Un lieu trouvé. |
| `search/PlaceSearch.kt` | Interface de recherche. |
| `search/BanGeocoder.kt` | Recherche via la Base Adresse Nationale (IGN). |
| `search/BanResponseParser.kt` | JSON BAN → `Place`. |
| `routing/Route.kt` / `RouteStep.kt` | Itinéraire et ses étapes. |
| `routing/ManeuverType.kt` | Types de manœuvres (indépendants du moteur). |
| `routing/RoutingService.kt` | Interface de calcul d'itinéraire. |
| `routing/osrm/OsrmClient.kt` | Appel au serveur OSRM. |
| `routing/osrm/OsrmResponseParser.kt` | JSON OSRM → `Route`. |
| `routing/osrm/OsrmManeuverMapper.kt` | Manœuvres OSRM → `ManeuverType`. |
| `speedlimit/SpeedLimitRepository.kt` | Limitation de la route actuelle (`StateFlow`), recharge la zone en approchant du bord. |
| `speedlimit/OverpassResponseParser.kt` | JSON Overpass → `RoadSegment`. |
| `speedlimit/MaxSpeedParser.kt` | "50", "FR:urban", "30 mph" → km/h. |
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

### `navigation/` — Le guidage
| Fichier | Rôle |
| :--- | :--- |
| `NavigationState.kt` | Idle → Calculating → Previewing → Navigating → Arrived / Failed. |
| `NavigationEngine.kt` | Calcul, aperçu puis `confirm()`, suivi GPS, recalcul, arrivée. Départ direct (`autoStart`) depuis la voiture. |
| `RouteProgress.kt` | Où on en est : prochaine manœuvre, distance, temps restant. |
| `RouteProgressCalculator.kt` | Calcule `RouteProgress` à partir d'une position. |
| `OffRouteDetector.kt` | > 35 m du tracé pendant > 3 s, en roulant ⇒ recalcul (§5.3). |
| `ArrivalDetector.kt` | < 30 m de l'arrivée ⇒ arrivé. |
| `SpeedingDetector.kt` | Excès de vitesse : limite + 3 km/h (§6.3). |
| `radar/RadarAlertDetector.kt` | Radar à < 800 m devant, cône de ±30° (§6.2) ; urgent sous 300 m. |
| `radar/RadarAlert.kt` | Le radar concerné, sa distance et le niveau d'alerte. |
| `radar/RadarAlertLevel.kt` | Avertissement / urgent. |
| `radar/RadarTypeText.kt` | Libellés par type ("RADAR FEU ROUGE", "Radar tronçon"). |
| `instructions/InstructionTextBuilder.kt` | "Au rond-point, prenez la 2e sortie vers D1010". |
| `instructions/ManeuverGlyphs.kt` | Flèche ↰ ↱ ↻ pour chaque manœuvre. |

### `map/` — Carte MapLibre (partagée téléphone + voiture)
| Fichier | Rôle |
| :--- | :--- |
| `MapSetup.kt` | Charge le style, applique le thème, installe les calques. |
| `MapController.kt` | Façade : véhicule, tracé + destination, radars, vue d'ensemble, recentrage. |
| `MapLibreConversions.kt` | Notre `LatLng` → types MapLibre / GeoJSON. |
| `style/MapStyleSource.kt` | URL du fond de carte OpenFreeMap. |
| `theme/MapTheme.kt` | Thèmes GTA V Radar et Waze nocturne (§4). |
| `theme/MapPalette.kt` | Couleurs d'un thème. |
| `theme/MapThemeApplier.kt` | Repeint chaque calque du style selon la palette. |
| `layers/RouteLayer.kt` | Tracé violet + liseré. |
| `layers/DestinationLayer.kt` | Épingle de destination au bout du tracé. |
| `layers/DestinationPinBitmap.kt` | Dessin de l'épingle. |
| `layers/VehicleMarkerLayer.kt` | Marqueur du véhicule orienté selon le cap. |
| `layers/VehicleArrowBitmap.kt` | Dessin de la flèche GTA (provisoire avant la 3D). |
| `layers/LayerOrder.kt` | Place le tracé sous les noms de rues. |
| `layers/RadarLayer.kt` | Icônes des radars sur la carte (une par type). |
| `layers/RadarIconBitmap.kt` | Dessin des icônes : appareil photo, feu tricolore. |
| `camera/FollowCamera.kt` | Suivi incliné, pause au doigt (`isFollowing`), recentrage, vue d'ensemble d'un trajet. |
| `camera/CameraConfig.kt` | Réglages téléphone / voiture. |

### `audio/` — Sons et guidage vocal
| Fichier | Rôle |
| :--- | :--- |
| `AudioController.kt` | Écoute guidage, radars et vitesse ; joue les annonces (sauf son coupé). Un seul pour téléphone + voiture. |
| `cue/AudioCue.kt` | Une annonce : un son ou une phrase. |
| `cue/SoundEffect.kt` | Liste des sons (départ, virage, arrivée, radar, excès). |
| `announcers/GuidanceAnnouncer.kt` | Départ, "Dans 500 mètres…", carillon + instruction avant la manœuvre, recalcul, arrivée. |
| `announcers/RadarAnnouncer.kt` | Double bip + "Radar dans 600 mètres", puis triple bip sous 300 m. |
| `announcers/SpeedingAnnouncer.kt` | Bip au dépassement, répété toutes les 15 s. |
| `announcers/SpeechDistanceFormatter.kt` | "300 mètres", "1,5 kilomètres". |
| `synth/ToneSynth.kt` | Synthétiseur : notes → échantillons PCM (aucun fichier audio). |
| `synth/SoundEffectTones.kt` | Partition de chaque son (reprise de la version WebGL). |
| `synth/Tone.kt`, `synth/Waveform.kt` | Une note ; formes d'onde sinus / triangle / dent de scie. |
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
| `hud/HudModel.kt` | Textes prêts à afficher pour le guidage. |
| `hud/ManeuverBanner.kt` | Flèche + distance + instruction (haut). |
| `hud/SpeedPanel.kt` | Vitesse (bas gauche), rouge clignotant en excès. |
| `hud/SpeedLimitSign.kt` | Panneau rond blanc / rouge de limitation. |
| `hud/RadarAlertBanner.kt` | Bandeau « RADAR FEU ROUGE · 450 m » ; tout rouge clignotant sous 300 m. |
| `hud/ArrivalPanel.kt` | Heure d'arrivée, restant, "Arrêter" (bas droite). |
| `hud/GpsStatusDot.kt` | Pastille GPS. |
| `hud/StatusBanner.kt` | Messages (calcul, erreur, arrivée). |
| `hud/MuteButton.kt` | Bouton 🔊 / 🔇. |
| `hud/PreviewPanel.kt` | Aperçu : destination, durée, distance, arrivée, DÉMARRER / ANNULER. |
| `hud/PreviewModel.kt` | Textes de l'aperçu. |
| `hud/RecenterButton.kt` | « ◎ RECENTRER » quand la carte a été déplacée. |
| `search/SearchBar.kt` | Champ "Où aller ?". |
| `search/SearchResultsList.kt` | Résultats en liste verticale (§2.3). |
| `search/SearchUiState.kt` | État de la recherche. |
| `components/CockpitPanel.kt` | Conteneur flat sombre à bordure fine (§2.1). |
| `permissions/LocationPermissionEffect.kt` | Demande la localisation. |
| `theme/CockpitColors.kt`, `CockpitTypography.kt`, `GameMapsTheme.kt` | Design system cockpit. |

### `car/` — Android Auto
| Fichier | Rôle |
| :--- | :--- |
| `GameMapsCarAppService.kt` | Service déclaré dans le manifeste ; Android Auto s'y connecte. |
| `GameMapsCarSession.kt` | Une connexion voiture ; crée le premier écran. |
| `screens/NavigationCarScreen.kt` | Écran principal : carte + template selon l'état du guidage. |
| `screens/CarSearchScreen.kt` | Recherche de destination (clavier / voix de la voiture). |
| `surface/CarMapSurface.kt` | **Carte MapLibre sur l'écran de la voiture** (écran virtuel + Presentation). |
| `trip/CarTripReporter.kt` | Informe Android Auto du guidage en cours (`NavigationManager`). |
| `templates/IdleTemplate.kt` | Pas de guidage : bouton "Où aller ?". |
| `templates/CalculatingTemplate.kt` | Chargement. |
| `templates/PreviewTemplate.kt` | Trajet choisi sur le téléphone : Démarrer / Annuler. |
| `templates/NavigatingTemplate.kt` | Manœuvre + estimation d'arrivée + "Arrêter". |
| `templates/MessageTemplates.kt` | Arrivée, erreur, permission. |
| `templates/PlaceListBuilder.kt` | Résultats de recherche. |
| `templates/CarActions.kt` | Boutons communs. |
| `mapping/CarManeuverMapper.kt` | `ManeuverType` → type Android Auto. |
| `mapping/CarStepMapper.kt` | `RouteStep` → `Step` Android Auto. |
| `mapping/CarDistanceMapper.kt` | Mètres → `Distance`. |
| `mapping/CarTravelEstimateMapper.kt` | Restant + heure d'arrivée. |
| `mapping/ManeuverIconFactory.kt` | Icône de manœuvre (mise en cache). |

## Comment la carte arrive dans la voiture

Android Auto interdit WebView et WebGL (§9), mais donne aux apps de navigation une **Surface** native.
`CarMapSurface` crée un *écran virtuel privé* qui dessine dans cette Surface, puis y affiche une
`Presentation` contenant une `MapView` MapLibre ordinaire, configurée par le même `MapSetup` que le téléphone.
Les templates (manœuvre, boutons) sont dessinés **par Android Auto** par-dessus.

## Services externes (gratuits, sans clé)

| Besoin | Service |
| :--- | :--- |
| Fond de carte | OpenFreeMap (tuiles OpenStreetMap) |
| Recherche d'adresses | Base Adresse Nationale — `data.geopf.fr/geocodage` |
| Itinéraire | OSRM public — `router.project-osrm.org` (démo, sans trafic) |
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

## Prochaines étapes (hors de cette première passe)

1. Recherche de lieux (Photon) et favoris Maison / Travail.
2. Alerte radar sur Android Auto.
3. Trafic TomTom : bordures orange / rouge sur le tracé.
4. Véhicule 3D (Filament) à la place de la flèche 2D.
5. Thème Minecraft, fanfare « Mission Passed ».
6. Service au premier plan pour continuer le guidage écran éteint.
