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
| `location/LocationRepository.kt` | Position partagée (`StateFlow`) pour toute l'app. |
| `location/LocationPermissions.kt` | Liste et vérification des permissions. |
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
| `speedlimit/OverpassClient.kt` | Routes avec `maxspeed` d'une zone de ~1 km (API Overpass / OSM). |
| `speedlimit/OverpassResponseParser.kt` | JSON Overpass → `RoadSegment`. |
| `speedlimit/MaxSpeedParser.kt` | "50", "FR:urban", "30 mph" → km/h. |
| `speedlimit/SpeedLimitIndex.kt` | Route la plus proche, départagée par le cap aux carrefours. |
| `speedlimit/RoadSegment.kt` | Une route OSM et sa limitation. |

### `navigation/` — Le guidage
| Fichier | Rôle |
| :--- | :--- |
| `NavigationState.kt` | Idle → Calculating → Navigating → Arrived / Failed. |
| `NavigationEngine.kt` | Lance le calcul, suit le GPS, recalcule, détecte l'arrivée. |
| `RouteProgress.kt` | Où on en est : prochaine manœuvre, distance, temps restant. |
| `RouteProgressCalculator.kt` | Calcule `RouteProgress` à partir d'une position. |
| `OffRouteDetector.kt` | > 35 m du tracé pendant > 3 s ⇒ recalcul (§5.3). |
| `ArrivalDetector.kt` | < 30 m de l'arrivée ⇒ arrivé. |
| `SpeedingDetector.kt` | Excès de vitesse : limite + 3 km/h (§6.3). |
| `instructions/InstructionTextBuilder.kt` | "Au rond-point, prenez la 2e sortie vers D1010". |
| `instructions/ManeuverGlyphs.kt` | Flèche ↰ ↱ ↻ pour chaque manœuvre. |

### `map/` — Carte MapLibre (partagée téléphone + voiture)
| Fichier | Rôle |
| :--- | :--- |
| `MapSetup.kt` | Charge le style, applique le thème, installe les calques. |
| `MapController.kt` | Façade : `showVehicle(fix)` et `showRoute(route)`. |
| `MapLibreConversions.kt` | Notre `LatLng` → types MapLibre / GeoJSON. |
| `style/MapStyleSource.kt` | URL du fond de carte OpenFreeMap. |
| `theme/MapTheme.kt` | Thèmes GTA V Radar et Waze nocturne (§4). |
| `theme/MapPalette.kt` | Couleurs d'un thème. |
| `theme/MapThemeApplier.kt` | Repeint chaque calque du style selon la palette. |
| `layers/RouteLayer.kt` | Tracé violet + liseré. |
| `layers/VehicleMarkerLayer.kt` | Marqueur du véhicule orienté selon le cap. |
| `layers/VehicleArrowBitmap.kt` | Dessin de la flèche GTA (provisoire avant la 3D). |
| `layers/LayerOrder.kt` | Place le tracé sous les noms de rues. |
| `camera/FollowCamera.kt` | Caméra poursuite inclinée (saut à la 1re position, puis glissement linéaire). |
| `camera/CameraConfig.kt` | Réglages téléphone / voiture. |

### `ui/` — Téléphone (Jetpack Compose)
| Fichier | Rôle |
| :--- | :--- |
| `screen/MainScreen.kt` | Carte plein écran + HUD (§2.2). |
| `screen/MainViewModel.kt` | Recherche avec anti-rebond, démarrage / arrêt du guidage. |
| `screen/MainUiState.kt` | Tout l'état de l'écran. |
| `screen/MapRenderEffect.kt` | Pousse position et tracé vers la carte. |
| `map/MapViewHost.kt` | MapView dans Compose. |
| `map/MapViewLifecycleObserver.kt` | Cycle de vie de la MapView. |
| `hud/HudModel.kt` | Textes prêts à afficher pour le guidage. |
| `hud/ManeuverBanner.kt` | Flèche + distance + instruction (haut). |
| `hud/SpeedPanel.kt` | Vitesse (bas gauche), rouge clignotant en excès. |
| `hud/SpeedLimitSign.kt` | Panneau rond blanc / rouge de limitation. |
| `hud/ArrivalPanel.kt` | Heure d'arrivée, restant, "Arrêter" (bas droite). |
| `hud/GpsStatusDot.kt` | Pastille GPS. |
| `hud/StatusBanner.kt` | Messages (calcul, erreur, arrivée). |
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
| Limitations de vitesse | Overpass API — `overpass-api.de` (tags OSM `maxspeed`) |

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

1. Radars français (base embarquée) + bips.
2. Trafic TomTom : bordures orange / rouge sur le tracé.
3. Véhicule 3D (Filament) à la place de la flèche 2D.
4. Thème Minecraft, favoris (Maison / Travail), audio procédural.
5. Service au premier plan pour continuer le guidage écran éteint.
6. Bip d'excès de vitesse (avec le moteur audio).
