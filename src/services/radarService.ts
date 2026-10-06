import { Coordinates, RadarAlert, RadarItem, RadarTrafficSettings, RadarType } from '../types';
import radarsFranceRaw from '../data/radarsFrance.json';

// Configuration par défaut du radar & trafic
const SETTINGS_KEY = 'gamemaps_radar_traffic_settings';
export const DEFAULT_RADAR_TRAFFIC_SETTINGS: RadarTrafficSettings = {
  radarAlertsEnabled: true,
  soundAlertsEnabled: true,
  trafficEnabled: true,
  tomtomApiKey: '',
};

// 1. Initialisation de la base française officielle (3 350 radars)
const franceRadars: RadarItem[] = (radarsFranceRaw as any[]).map((r) => ({
  id: r.id,
  type: r.t as RadarType,
  coordinates: r.c as Coordinates, // [lng, lat]
  speedLimit: r.s,
  road: r.r,
  place: r.p,
  direction: r.d,
}));

// 2. Cache mémoire et persistant pour les radars internationaux
const internationalRadars = new Map<string, RadarItem>();
const fetchedCells = new Set<string>();

/**
 * Calcul de distance en mètres entre deux points GPS (Haversine)
 */
export function computeDistanceMeters(c1: Coordinates, c2: Coordinates): number {
  const R = 6371e3;
  const phi1 = (c1[1] * Math.PI) / 180;
  const phi2 = (c2[1] * Math.PI) / 180;
  const deltaPhi = ((c2[1] - c1[1]) * Math.PI) / 180;
  const deltaLambda = ((c2[0] - c1[0]) * Math.PI) / 180;

  const a =
    Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
    Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(R * c);
}

/**
 * Calcul du relèvement (bearing en degrés 0..360) d'un point A vers un point B
 */
export function computeBearing(from: Coordinates, to: Coordinates): number {
  const lon1 = (from[0] * Math.PI) / 180;
  const lat1 = (from[1] * Math.PI) / 180;
  const lon2 = (to[0] * Math.PI) / 180;
  const lat2 = (to[1] * Math.PI) / 180;
  const dLon = lon2 - lon1;

  const y = Math.sin(dLon) * Math.cos(lat2);
  const x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
  const brng = (Math.atan2(y, x) * 180) / Math.PI;
  return (brng + 360) % 360;
}

/**
 * Récupération des paramètres enregistrés
 */
export function getRadarTrafficSettings(): RadarTrafficSettings {
  if (typeof window === 'undefined') return DEFAULT_RADAR_TRAFFIC_SETTINGS;
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (!raw) return DEFAULT_RADAR_TRAFFIC_SETTINGS;
    return { ...DEFAULT_RADAR_TRAFFIC_SETTINGS, ...JSON.parse(raw) };
  } catch {
    return DEFAULT_RADAR_TRAFFIC_SETTINGS;
  }
}

/**
 * Sauvegarde des paramètres
 */
export function saveRadarTrafficSettings(
  partial: Partial<RadarTrafficSettings>
): RadarTrafficSettings {
  const current = getRadarTrafficSettings();
  const updated = { ...current, ...partial };
  try {
    localStorage.setItem(SETTINGS_KEY, JSON.stringify(updated));
  } catch (e) {
    console.warn('Impossible de sauvegarder les paramètres radar/trafic', e);
  }
  return updated;
}

/**
 * Détermine la clé de cellule géographique (grille d'environ 15km)
 */
function getCellKey(lat: number, lon: number): string {
  const step = 0.15;
  return `${Math.floor(lat / step)}_${Math.floor(lon / step)}`;
}

/**
 * Téléchargement dynamique et mise en cache des radars internationaux (OpenStreetMap Overpass)
 */
export async function ensureInternationalRadarsLoaded(coords: Coordinates): Promise<void> {
  const [lon, lat] = coords;
  const cellKey = getCellKey(lat, lon);

  if (fetchedCells.has(cellKey)) return;
  fetchedCells.add(cellKey);

  // Vérifier d'abord le cache localStorage
  const cacheKey = `radar_cell_${cellKey}`;
  try {
    const cached = localStorage.getItem(cacheKey);
    if (cached) {
      const parsed: RadarItem[] = JSON.parse(cached);
      parsed.forEach((item) => internationalRadars.set(item.id, item));
      return;
    }
  } catch {}

  const mirrors = [
    'https://overpass-api.de/api/interpreter',
    'https://overpass.kumi.systems/api/interpreter',
  ];

  const query = `[out:json][timeout:10];(node["highway"="speed_camera"](around:18000,${lat},${lon});node["enforcement"="maxspeed"](around:18000,${lat},${lon});node["enforcement"="traffic_signals"](around:18000,${lat},${lon}););out body;`;

  for (const mirror of mirrors) {
    try {
      const res = await fetch(mirror, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'User-Agent': 'GameMapsIRL/1.0',
        },
        body: 'data=' + encodeURIComponent(query),
        signal: AbortSignal.timeout(5000),
      });

      if (!res.ok) continue;

      const data = await res.json();
      if (!data.elements) continue;

      const newItems: RadarItem[] = [];
      for (const el of data.elements) {
        if (!el.lat || !el.lon) continue;
        const tags = el.tags || {};
        const isRedLight =
          tags.enforcement === 'traffic_signals' ||
          tags['camera:type'] === 'red_light' ||
          tags.highway === 'traffic_signals';

        const speed = parseInt(tags.maxspeed, 10) || (isRedLight ? 50 : 80);
        const type: RadarType = isRedLight ? 'red_light' : 'speed';

        const item: RadarItem = {
          id: `osm_${el.id}`,
          type,
          coordinates: [el.lon, el.lat],
          speedLimit: speed,
          road: tags.name || tags['ref'] || undefined,
          place: tags['addr:city'] || undefined,
          direction: tags.direction || undefined,
        };

        internationalRadars.set(item.id, item);
        newItems.push(item);
      }

      // Mettre en cache la cellule
      try {
        localStorage.setItem(cacheKey, JSON.stringify(newItems));
      } catch {}

      break; // Succès, pas besoin de tester le miroir suivant
    } catch {
      // Miroir échoué, passage au suivant
    }
  }
}

/**
 * Récupère tous les radars situés dans une zone géographique (pour affichage sur MapLibre)
 */
export function getRadarsInBBox(
  minLng: number,
  minLat: number,
  maxLng: number,
  maxLat: number
): RadarItem[] {
  const result: RadarItem[] = [];

  // 1. Radars France
  for (const r of franceRadars) {
    const [lng, lat] = r.coordinates;
    if (lng >= minLng && lng <= maxLng && lat >= minLat && lat <= maxLat) {
      result.push(r);
    }
  }

  // 2. Radars Internationaux en cache
  for (const r of internationalRadars.values()) {
    const [lng, lat] = r.coordinates;
    if (lng >= minLng && lng <= maxLng && lat >= minLat && lat <= maxLat) {
      result.push(r);
    }
  }

  return result;
}

/**
 * Moteur de détection de proximité cinématique pour le cockpit :
 * Détecte les radars et feux rouges situés devant le véhicule avec alerte progressive
 */
export function checkRadarProximity(
  carPos: Coordinates,
  heading: number,
  speedKmh: number,
  settings?: RadarTrafficSettings
): RadarAlert | null {
  const activeSettings = settings || getRadarTrafficSettings();
  if (!activeSettings.radarAlertsEnabled) return null;

  // Lancer l'acquisition internationale en arrière-plan si besoin
  ensureInternationalRadarsLoaded(carPos).catch(() => {});

  const maxDetectionDistance = 600; // Détection à partir de 600 mètres
  const minClearDistance = 25; // Alerte levée une fois franchi (< 25m)

  let closestAlert: RadarAlert | null = null;
  let minDistance = Infinity;

  // Parcourir à la fois la base France et la base internationale
  const candidates = [...franceRadars, ...Array.from(internationalRadars.values())];

  for (const radar of candidates) {
    // Élimination rapide par distance approximative
    const dLat = Math.abs(radar.coordinates[1] - carPos[1]);
    const dLng = Math.abs(radar.coordinates[0] - carPos[0]);
    if (dLat > 0.015 || dLng > 0.015) continue;

    const dist = computeDistanceMeters(carPos, radar.coordinates);
    if (dist > maxDetectionDistance || dist < minClearDistance) continue;

    // Si le véhicule est en mouvement (> 5 km/h), vérifier qu'il fait face au radar (cône de ±65°)
    if (speedKmh >= 5) {
      const bearing = computeBearing(carPos, radar.coordinates);
      const angleDiff = Math.abs(((bearing - heading + 540) % 360) - 180);
      if (angleDiff > 65) {
        continue; // Le radar est derrière ou sur un côté perpendiculaire éloigné
      }
    }

    if (dist < minDistance) {
      minDistance = dist;
      closestAlert = {
        radar,
        distanceMeters: dist,
        level: dist <= 300 ? 'urgent' : 'warning',
      };
    }
  }

  return closestAlert;
}
