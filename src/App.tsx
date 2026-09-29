import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Coordinates, RouteInfo, LocationSearchResult } from './types';
import { CarMapTheme } from './styles/mapStyles';
import { calculateRoute } from './services/routing';
import { gpsAudio } from './services/audio';
import { 
  VehicleType, 
  getSavedVehicleCustomization, 
  saveVehicleCustomization 
} from './services/vehicleCustomization';
import { Map } from './components/Map';
import { CarDashboard } from './components/CarDashboard';
import { MissionPassedModal } from './components/MissionPassedModal';
import { Navigation } from 'lucide-react';
import { Geolocation } from '@capacitor/geolocation';

function calculateBearing(start: Coordinates, end: Coordinates): number {
  const startLat = (start[1] * Math.PI) / 180;
  const startLng = (start[0] * Math.PI) / 180;
  const endLat = (end[1] * Math.PI) / 180;
  const endLng = (end[0] * Math.PI) / 180;

  const dLng = endLng - startLng;
  const y = Math.sin(dLng) * Math.cos(endLat);
  const x =
    Math.cos(startLat) * Math.sin(endLat) -
    Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLng);

  let brng = (Math.atan2(y, x) * 180) / Math.PI;
  return (brng + 360) % 360;
}

function getDistanceMeters(c1: Coordinates, c2: Coordinates): number {
  const R = 6371e3;
  const phi1 = (c1[1] * Math.PI) / 180;
  const phi2 = (c2[1] * Math.PI) / 180;
  const deltaPhi = ((c2[1] - c1[1]) * Math.PI) / 180;
  const deltaLambda = ((c2[0] - c1[0]) * Math.PI) / 180;

  const a =
    Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
    Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function distanceToSegment(p: Coordinates, a: Coordinates, b: Coordinates): number {
  const l2 = (b[0] - a[0]) ** 2 + (b[1] - a[1]) ** 2;
  if (l2 === 0) return getDistanceMeters(p, a);
  let t = ((p[0] - a[0]) * (b[0] - a[0]) + (p[1] - a[1]) * (b[1] - a[1])) / l2;
  t = Math.max(0, Math.min(1, t));
  const proj: Coordinates = [a[0] + t * (b[0] - a[0]), a[1] + t * (b[1] - a[1])];
  return getDistanceMeters(p, proj);
}

export function App() {
  // Position initiale (Bordeaux / Gironde en attendant la réception du GPS)
  const [currentPosition, setCurrentPosition] = useState<Coordinates>([-0.5792, 44.8378]);
  const [bearing, setBearing] = useState<number>(30);
  const [currentSpeed, setCurrentSpeed] = useState<number>(0);
  const [theme, setTheme] = useState<CarMapTheme>('gta');

  // Customisation de véhicule / curseur
  const [vehicleCustomization, setVehicleCustomization] = useState(() => getSavedVehicleCustomization());

  const handleVehicleChange = (newType: VehicleType, newColor: string, newHl: boolean) => {
    const updated = { vehicleType: newType, vehicleColor: newColor, showHeadlights: newHl };
    setVehicleCustomization(updated);
    saveVehicleCustomization(newType, newColor, newHl);
  };

  // Statut GPS & Écran
  const [gpsStatus, setGpsStatus] = useState<'locating' | 'locked' | 'error'>('locating');
  const [wakeLockActive, setWakeLockActive] = useState<boolean>(false);

  // Navigation
  const [route, setRoute] = useState<RouteInfo | null>(null);
  const [currentStepIndex, setCurrentStepIndex] = useState<number>(0);
  const [stepDistanceRemaining, setStepDistanceRemaining] = useState<number>(0);
  const [totalDistanceRemaining, setTotalDistanceRemaining] = useState<number>(0);
  const [totalDurationRemaining, setTotalDurationRemaining] = useState<number>(0);
  const [destinationName, setDestinationName] = useState<string>('');
  const [destinationCoords, setDestinationCoords] = useState<Coordinates | null>(null);
  const [isNavigating, setIsNavigating] = useState<boolean>(false);
  const [pendingNewDestination, setPendingNewDestination] = useState<{ coords: Coordinates; name: string } | null>(null);
  const [showReplaceModal, setShowReplaceModal] = useState<boolean>(false);

  // Caméra & Audio
  const [is3D, setIs3D] = useState<boolean>(true);
  const [followUser, setFollowUser] = useState<boolean>(true);
  const [recenterTrigger, setRecenterTrigger] = useState<number>(0);
  const [isMuted, setIsMuted] = useState<boolean>(false);
  const [showArrivalModal, setShowArrivalModal] = useState<boolean>(false);

  // Références d'état pour les écouteurs d'événements
  const routeRef = useRef<RouteInfo | null>(null);
  routeRef.current = route;
  const isNavigatingRef = useRef<boolean>(false);
  isNavigatingRef.current = isNavigating;
  const currentStepIndexRef = useRef<number>(0);
  currentStepIndexRef.current = currentStepIndex;

  const lastGpsPosRef = useRef<Coordinates | null>(null);
  const destinationCoordsRef = useRef<Coordinates | null>(null);
  destinationCoordsRef.current = destinationCoords;
  const hasRecalculatedFromRealGpsRef = useRef<boolean>(false);
  const lastStepSpokenRef = useRef<number>(-1);
  const offRouteTicksRef = useRef<number>(0);
  const isReroutingRef = useRef<boolean>(false);

  // ==============================================================
  // 1. SCREEN WAKE LOCK (Maintien de l'écran allumé en voiture)
  // ==============================================================
  useEffect(() => {
    let wakeLockSentinel: any = null;

    const requestWakeLock = async () => {
      if ('wakeLock' in navigator && document.visibilityState === 'visible') {
        try {
          wakeLockSentinel = await (navigator as any).wakeLock.request('screen');
          setWakeLockActive(true);
          wakeLockSentinel.addEventListener('release', () => {
            setWakeLockActive(false);
          });
        } catch {
          setWakeLockActive(false);
        }
      }
    };

    requestWakeLock();

    const onVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        requestWakeLock();
      }
    };

    document.addEventListener('visibilitychange', onVisibilityChange);

    // Déblocage audio au premier toucher écran
    const unlockAudio = () => {
      gpsAudio.playStartChime();
      window.removeEventListener('touchstart', unlockAudio);
      window.removeEventListener('click', unlockAudio);
    };
    window.addEventListener('touchstart', unlockAudio, { once: true });
    window.addEventListener('click', unlockAudio, { once: true });

    return () => {
      document.removeEventListener('visibilitychange', onVisibilityChange);
      if (wakeLockSentinel) wakeLockSentinel.release().catch(() => {});
    };
  }, []);

  // ==============================================================
  // 2. MOTEUR DE GUIDAGE EN CONDUITE RÉELLE
  // ==============================================================
  const processNavigationTick = useCallback((userPos: Coordinates, speedKmh: number) => {
    const curRoute = routeRef.current;
    if (!curRoute || curRoute.coordinates.length < 2) return;

    const coords = curRoute.coordinates;
    const destPos = coords[coords.length - 1];
    const distToDest = getDistanceMeters(userPos, destPos);

    // 1. Arrivée à destination (< 25 mètres)
    if (distToDest < 25) {
      setIsNavigating(false);
      setCurrentSpeed(0);
      setShowArrivalModal(true);
      gpsAudio.playArrivalChime();
      gpsAudio.speak('Vous êtes arrivé à destination.');
      return;
    }

    // 2. Progression le long du tracé
    let closestCoordIdx = 0;
    let minDistToPath = Infinity;

    for (let i = 0; i < coords.length - 1; i++) {
      const d = distanceToSegment(userPos, coords[i], coords[i + 1]);
      if (d < minDistToPath) {
        minDistToPath = d;
        closestCoordIdx = i;
      }
    }

    // Distance restante
    let remainingMeters = getDistanceMeters(userPos, coords[closestCoordIdx + 1] || destPos);
    for (let i = closestCoordIdx + 1; i < coords.length - 1; i++) {
      remainingMeters += getDistanceMeters(coords[i], coords[i + 1]);
    }
    setTotalDistanceRemaining(remainingMeters);

    // Temps restant estimé
    const effectiveSpeedMs = Math.max(speedKmh * (1000 / 3600), 12.5); // Min ~45 km/h
    setTotalDurationRemaining(Math.round(remainingMeters / effectiveSpeedMs));

    // 3. Détection hors itinéraire (> 70m) & Recalcul automatique
    if (minDistToPath > 70 && !isReroutingRef.current && destinationCoords) {
      offRouteTicksRef.current += 1;
      if (offRouteTicksRef.current >= 3) {
        isReroutingRef.current = true;
        gpsAudio.speak('Recalcul de votre itinéraire...');
        calculateRoute(userPos, destinationCoords)
          .then((newRoute) => {
            setRoute(newRoute);
            setCurrentStepIndex(0);
            lastStepSpokenRef.current = -1;
            offRouteTicksRef.current = 0;
            if (newRoute.steps.length > 0) {
              gpsAudio.speak(newRoute.steps[0].instruction);
              lastStepSpokenRef.current = 0;
            }
          })
          .catch((err) => console.warn('Erreur recalcul', err))
          .finally(() => {
            isReroutingRef.current = false;
          });
        return;
      }
    } else {
      offRouteTicksRef.current = 0;
    }

    // 4. Décompte dynamique de la prochaine manœuvre
    if (curRoute.steps.length > 0) {
      const stepIdx = currentStepIndexRef.current;
      const currentStep = curRoute.steps[stepIdx];

      if (currentStep) {
        const distToManeuver = getDistanceMeters(userPos, currentStep.location);
        setStepDistanceRemaining(distToManeuver);

        // Manœuvre atteinte (< 35m) -> passage à la suivante avec carillon et annonce
        if (distToManeuver < 35 && stepIdx < curRoute.steps.length - 1) {
          const nextIdx = stepIdx + 1;
          setCurrentStepIndex(nextIdx);
          currentStepIndexRef.current = nextIdx;

          if (nextIdx > lastStepSpokenRef.current) {
            gpsAudio.playTurnChime();
            gpsAudio.speak(curRoute.steps[nextIdx].instruction);
            lastStepSpokenRef.current = nextIdx;
          }
        }
      }
    }
  }, [destinationCoords]);

  // ==============================================================
  // 3. VRAI GPS DU VÉHICULE EN CONTINU
  // ==============================================================
  // Initialisation immédiate du GPS dès l'ouverture de l'application
  useEffect(() => {
    const initLocation = async () => {
      try {
        await Geolocation.requestPermissions();
        const pos = await Geolocation.getCurrentPosition({ enableHighAccuracy: true, timeout: 5000 });
        if (pos && pos.coords) {
          const coords: Coordinates = [pos.coords.longitude, pos.coords.latitude];
          setCurrentPosition(coords);
          lastGpsPosRef.current = coords;
          setGpsStatus('locked');
        }
      } catch (e) {
        console.warn('Init location fallback:', e);
      }
    };
    initLocation();
  }, []);

  // ==============================================================
  // 3. VRAI GPS DU VÉHICULE EN CONTINU
  // ==============================================================
  useEffect(() => {
    if (!('geolocation' in navigator)) {
      setGpsStatus('error');
      return;
    }

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        setGpsStatus('locked');
        const newPos: Coordinates = [pos.coords.longitude, pos.coords.latitude];
        setCurrentPosition(newPos);

        // Si un itinéraire était calculé depuis le fallback de départ (Bordeaux),
        // recalcul automatique immédiat dès réception de la vraie position GPS !
        if (destinationCoordsRef.current && !hasRecalculatedFromRealGpsRef.current) {
          const distToDefault = getDistanceMeters(newPos, [-0.5792, 44.8378]);
          if (distToDefault > 500) {
            hasRecalculatedFromRealGpsRef.current = true;
            calculateRoute(newPos, destinationCoordsRef.current).then((r) => {
              setRoute(r);
              setTotalDistanceRemaining(r.distance);
              setTotalDurationRemaining(r.duration);
              if (r.steps.length > 0) {
                setStepDistanceRemaining(r.steps[0].distance);
              }
            }).catch((err) => console.warn('Erreur recalcul auto départ réel', err));
          }
        }

        // Orientation / Cap de la voiture
        if (pos.coords.heading !== null && !isNaN(pos.coords.heading) && pos.coords.heading >= 0) {
          setBearing(pos.coords.heading);
        } else if (lastGpsPosRef.current) {
          const movedDist = getDistanceMeters(lastGpsPosRef.current, newPos);
          if (movedDist > 2.5) {
            setBearing(calculateBearing(lastGpsPosRef.current, newPos));
          }
        }

        // Vitesse réelle en km/h
        const speedKmh = pos.coords.speed !== null && !isNaN(pos.coords.speed)
          ? Math.max(0, pos.coords.speed * 3.6)
          : 0;
        setCurrentSpeed(speedKmh < 1.8 ? 0 : speedKmh);

        // Si navigation active, mise à jour du guidage
        if (isNavigatingRef.current) {
          processNavigationTick(newPos, speedKmh);
        }

        lastGpsPosRef.current = newPos;
      },
      (err) => {
        console.warn('GPS Status:', err.message);
        setGpsStatus('error');
      },
      {
        enableHighAccuracy: true,
        maximumAge: 1000,
        timeout: 10000,
      }
    );

    return () => {
      navigator.geolocation.clearWatch(watchId);
    };
  }, [processNavigationTick]);

  // Récupération garantie de la position réelle pour le calcul de route
  const getLiveGpsPosition = async (): Promise<Coordinates> => {
    if (lastGpsPosRef.current) {
      return lastGpsPosRef.current;
    }

    try {
      const fresh = await Geolocation.getCurrentPosition({ enableHighAccuracy: true, timeout: 3500 });
      if (fresh && fresh.coords) {
        const coords: Coordinates = [fresh.coords.longitude, fresh.coords.latitude];
        setCurrentPosition(coords);
        lastGpsPosRef.current = coords;
        setGpsStatus('locked');
        return coords;
      }
    } catch (_) {}

    if ('geolocation' in navigator) {
      try {
        const pos: any = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject, { enableHighAccuracy: true, timeout: 3500 });
        });
        if (pos && pos.coords) {
          const coords: Coordinates = [pos.coords.longitude, pos.coords.latitude];
          setCurrentPosition(coords);
          lastGpsPosRef.current = coords;
          setGpsStatus('locked');
          return coords;
        }
      } catch (_) {}
    }

    return currentPosition;
  };

  // ==============================================================
  // 4. GESTION DU CHOIX D'ITINÉRAIRE (Aperçu puis Démarrage)
  // ==============================================================
  // Demande d'un nouvel itinéraire (Vérifie si un trajet est déjà en cours)
  const requestNewDestination = (coords: Coordinates, name: string) => {
    if (isNavigating) {
      setPendingNewDestination({ coords, name });
      setShowReplaceModal(true);
      return;
    }
    applyNewDestination(coords, name);
  };

  const applyNewDestination = async (coords: Coordinates, name: string) => {
    try {
      gpsAudio.playTurnChime();
      const startPos = await getLiveGpsPosition();
      hasRecalculatedFromRealGpsRef.current = true;

      const newRoute = await calculateRoute(startPos, coords);
      setRoute(newRoute);
      setDestinationCoords(coords);
      setDestinationName(name);
      setCurrentStepIndex(0);
      setTotalDistanceRemaining(newRoute.distance);
      setTotalDurationRemaining(newRoute.duration);
      if (newRoute.steps.length > 0) {
        setStepDistanceRemaining(newRoute.steps[0].distance);
      }
      setIsNavigating(false);
      lastStepSpokenRef.current = -1;
      setShowReplaceModal(false);
      setPendingNewDestination(null);
    } catch (e) {
      console.error('Erreur calcul trajet', e);
    }
  };

  const handleConfirmReplaceRoute = () => {
    if (pendingNewDestination) {
      applyNewDestination(pendingNewDestination.coords, pendingNewDestination.name);
    }
  };

  const handleCancelReplaceRoute = () => {
    setShowReplaceModal(false);
    setPendingNewDestination(null);
  };

  const handleSelectDestination = (dest: LocationSearchResult) => {
    requestNewDestination(dest.coordinates, dest.name);
  };

  // Clic maintenu (Long Press) sur la carte
  const handleMapLongPress = (coords: Coordinates) => {
    requestNewDestination(coords, 'Destination choisie sur la carte');
  };

  // Clic sur le gros bouton vert "DÉMARRER"
  const handleStartNavigation = () => {
    if (!route) return;
    setIsNavigating(true);
    setFollowUser(true);
    setRecenterTrigger((prev) => prev + 1);
    setIs3D(true);
    gpsAudio.playStartChime();

    if (route.steps.length > 0) {
      gpsAudio.speak(route.steps[0].instruction);
      lastStepSpokenRef.current = 0;
    }
  };

  // Annuler l'aperçu du trajet
  const handleCancelPreview = () => {
    setRoute(null);
    setDestinationCoords(null);
    setDestinationName('');
  };

  // Quitter la navigation en cours de route
  const handleStopNavigation = () => {
    setIsNavigating(false);
    setRoute(null);
    setDestinationCoords(null);
    setDestinationName('');
  };

  const handleCenterOnGPS = () => {
    if (lastGpsPosRef.current) {
      setCurrentPosition(lastGpsPosRef.current);
    }
    setFollowUser(true);
    setRecenterTrigger((prev) => prev + 1);
    gpsAudio.playStartChime();
  };

  const currentStep = route?.steps?.[currentStepIndex] || null;
  const nextStep = route?.steps?.[currentStepIndex + 1] || null;

  return (
    <div className="fixed inset-0 w-full h-full overflow-hidden bg-black select-none">
      {/* 1. Carte en plein écran */}
      <Map
        currentPosition={currentPosition}
        bearing={bearing}
        route={route}
        theme={theme}
        followUser={followUser}
        is3D={is3D}
        recenterTrigger={recenterTrigger}
        vehicleType={vehicleCustomization.vehicleType}
        vehicleColor={vehicleCustomization.vehicleColor}
        showHeadlights={vehicleCustomization.showHeadlights}
        onMapLongPress={handleMapLongPress}
        onUserMove={() => setFollowUser(false)}
      />

      {/* 2. Tableau de bord voiture épuré Waze/GTA/Minecraft */}
      <CarDashboard
        theme={theme}
        onThemeChange={setTheme}
        is3D={is3D}
        onToggle3D={() => setIs3D(!is3D)}
        vehicleType={vehicleCustomization.vehicleType}
        vehicleColor={vehicleCustomization.vehicleColor}
        showHeadlights={vehicleCustomization.showHeadlights}
        onVehicleChange={handleVehicleChange}
        route={route}
        isNavigating={isNavigating}
        currentSpeed={currentSpeed}
        currentStep={currentStep}
        nextStep={nextStep}
        stepDistanceRemaining={stepDistanceRemaining}
        totalDistanceRemaining={totalDistanceRemaining}
        totalDurationRemaining={totalDurationRemaining}
        destinationName={destinationName}
        followUser={followUser}
        onToggleFollow={() => {
          setFollowUser(true);
          setRecenterTrigger((prev) => prev + 1);
          gpsAudio.playStartChime();
        }}
        isMuted={isMuted}
        onToggleMute={() => {
          const muted = gpsAudio.toggleMute();
          setIsMuted(muted);
        }}
        onSelectDestination={handleSelectDestination}
        onStartNavigation={handleStartNavigation}
        onCancelPreview={handleCancelPreview}
        onStopNavigation={handleStopNavigation}
        onCenterOnGPS={handleCenterOnGPS}
        gpsStatus={gpsStatus}
        wakeLockActive={wakeLockActive}
        currentPosition={currentPosition}
      />

      {/* 3. Modal de mission accomplie à l'arrivée */}
      <MissionPassedModal
        isOpen={showArrivalModal}
        onClose={() => setShowArrivalModal(false)}
        distance={route?.distance || 0}
        duration={route?.duration || 0}
      />

      {/* 4. Modal de confirmation si trajet déjà en cours */}
      {showReplaceModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in select-none">
          <div className="bg-neutral-900 border border-neutral-700 rounded-3xl p-6 max-w-sm w-full shadow-2xl text-white text-center">
            <div className="w-14 h-14 bg-amber-500/20 text-amber-400 border border-amber-500/30 rounded-full flex items-center justify-center mx-auto mb-4">
              <Navigation className="w-7 h-7" />
            </div>
            <h3 className="text-xl font-black mb-2">Changer de destination ?</h3>
            <p className="text-neutral-300 text-sm mb-6 leading-relaxed">
              Un itinéraire vers <span className="text-white font-bold">« {destinationName} »</span> est déjà en cours. Voulez-vous le remplacer par <span className="text-emerald-400 font-bold">« {pendingNewDestination?.name} »</span> ?
            </p>
            <div className="flex flex-col gap-2.5">
              <button
                onClick={handleConfirmReplaceRoute}
                className="w-full py-3.5 bg-emerald-500 hover:bg-emerald-400 active:bg-emerald-600 text-black font-black text-sm rounded-2xl shadow-xl transition-all"
              >
                Remplacer l'itinéraire
              </button>
              <button
                onClick={handleCancelReplaceRoute}
                className="w-full py-3 bg-neutral-800 hover:bg-neutral-700 active:bg-neutral-600 text-neutral-300 font-bold text-sm rounded-2xl transition-all"
              >
                Continuer le trajet actuel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
