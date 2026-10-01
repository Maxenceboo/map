import { Coordinates, RouteInfo, RouteStep, TrafficSection } from '../types';
import { getRadarTrafficSettings } from './radarService';

/**
 * Traduction des manœuvres OSRM en instructions de jeu en français
 */
function formatInstruction(step: any): string {
  const modifier = step.maneuver.modifier;
  const type = step.maneuver.type;
  const name = step.name || 'la route';

  if (type === 'depart') return `Démarrez sur ${name}`;
  if (type === 'arrive') return `Vous êtes arrivé à destination !`;
  if (type === 'roundabout' || type === 'rotary') {
    const exit = step.maneuver.exit ? `la ${step.maneuver.exit}e sortie` : 'la sortie';
    return `Au rond-point, prenez ${exit} sur ${name}`;
  }

  switch (modifier) {
    case 'sharp right':
      return `Tournez franchement à droite sur ${name}`;
    case 'right':
      return `Tournez à droite sur ${name}`;
    case 'slight right':
      return `Serrez à droite sur ${name}`;
    case 'sharp left':
      return `Tournez franchement à gauche sur ${name}`;
    case 'left':
      return `Tournez à gauche sur ${name}`;
    case 'slight left':
      return `Serrez à gauche sur ${name}`;
    case 'straight':
      return `Continuez tout droit sur ${name}`;
    case 'uturn':
      return `Faites demi-tour dès que possible`;
    default:
      return step.maneuver.instruction || `Continuez sur ${name}`;
  }
}

/**
 * Correspondance des manœuvres TomTom vers le modèle d'icônes HUD
 */
function mapTomTomManeuver(maneuver: string): { type: string; modifier?: string } {
  const m = (maneuver || '').toUpperCase();
  if (m === 'DEPART') return { type: 'depart', modifier: 'straight' };
  if (m === 'ARRIVE') return { type: 'arrive', modifier: 'straight' };
  if (m === 'TURN_LEFT') return { type: 'turn', modifier: 'left' };
  if (m === 'TURN_RIGHT') return { type: 'turn', modifier: 'right' };
  if (m === 'BEAR_LEFT') return { type: 'turn', modifier: 'slight left' };
  if (m === 'BEAR_RIGHT') return { type: 'turn', modifier: 'slight right' };
  if (m === 'SHARP_LEFT') return { type: 'turn', modifier: 'sharp left' };
  if (m === 'SHARP_RIGHT') return { type: 'turn', modifier: 'sharp right' };
  if (m.includes('ROUNDABOUT')) {
    if (m.includes('LEFT')) return { type: 'roundabout', modifier: 'left' };
    if (m.includes('RIGHT')) return { type: 'roundabout', modifier: 'right' };
    return { type: 'roundabout', modifier: 'straight' };
  }
  if (m === 'MAKE_UTURN') return { type: 'turn', modifier: 'uturn' };
  if (m === 'STRAIGHT' || m === 'KEEP_STRAIGHT') return { type: 'continue', modifier: 'straight' };
  return { type: 'turn', modifier: 'straight' };
}

/**
 * Calcul d'itinéraire temps réel avec l'API TomTom (prise en compte intégrale des bouchons & retards)
 */
async function calculateTomTomRoute(
  start: Coordinates,
  end: Coordinates,
  apiKey: string
): Promise<RouteInfo> {
  const startParam = `${start[1]},${start[0]}`;
  const endParam = `${end[1]},${end[0]}`;
  const key = encodeURIComponent(apiKey.trim());
  const url = `https://api.tomtom.com/routing/1/calculateRoute/${startParam}:${endParam}/json?traffic=true&routeType=fastest&departAt=now&computeTravelTimeFor=all&sectionType=traffic&instructionsType=text&language=fr-FR&key=${key}`;

  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Erreur TomTom Routing (${response.status})`);
  }

  const data = await response.json();
  if (!data.routes || data.routes.length === 0) {
    throw new Error('Aucun itinéraire TomTom trouvé');
  }

  const route = data.routes[0];
  const leg = route.legs?.[0];
  if (!leg || !leg.points || leg.points.length < 2) {
    throw new Error('Données de points TomTom invalides');
  }

  const coordinates: Coordinates[] = leg.points.map((p: any) => [p.longitude, p.latitude]);

  // Extraction des sections de trafic (ralentissements et bouchons en temps réel)
  const trafficSections: TrafficSection[] = [];
  if (route.sections && Array.isArray(route.sections)) {
    for (const sec of route.sections) {
      if (sec.sectionType === 'TRAFFIC') {
        const speed = typeof sec.effectiveSpeedInKmh === 'number' ? sec.effectiveSpeedInKmh : 999;
        const delay = sec.delayInSeconds || 0;
        const magnitude = sec.magnitudeOfDelay || 0;

        // Vrai bouchon dense (rouge) : vitesse <= 18 km/h, gros retard (>= 120s) ou magnitude >= 2
        // Ralentissement (orange) : circulation freinée (18 à 45 km/h) avec retard modéré
        const isJam = speed <= 18 || magnitude >= 2 || delay >= 120;

        trafficSections.push({
          startIndex: sec.startPointIndex,
          endIndex: sec.endPointIndex,
          severity: isJam ? 'jam' : 'slow',
          delaySeconds: delay,
          speedKmh: sec.effectiveSpeedInKmh,
        });
      }
    }
  }

  // Extraction des étapes avec guidage vocal & icônes
  const steps: RouteStep[] = [];
  const instructions = route.guidance?.instructions || [];

  if (instructions.length > 0) {
    for (let i = 0; i < instructions.length; i++) {
      const ins = instructions[i];
      const next = instructions[i + 1];
      const dist = next ? next.routeOffsetInMeters - ins.routeOffsetInMeters : 0;
      const dur = next ? next.travelTimeInSeconds - ins.travelTimeInSeconds : 0;
      const { type, modifier } = mapTomTomManeuver(ins.maneuver);

      steps.push({
        instruction: ins.message || 'Continuez sur votre route',
        name: ins.street || '',
        distance: Math.max(0, Math.round(dist)),
        duration: Math.max(0, Math.round(dur)),
        location: [ins.point.longitude, ins.point.latitude],
        type,
        modifier,
      });
    }
  } else {
    steps.push({
      instruction: 'Suivez le tracé GPS',
      name: 'Trajet optimisé trafic',
      distance: Math.round(route.summary.lengthInMeters),
      duration: Math.round(route.summary.travelTimeInSeconds),
      location: start,
      type: 'depart',
      modifier: 'straight',
    });
  }

  return {
    coordinates,
    distance: Math.round(route.summary.lengthInMeters),
    duration: Math.round(route.summary.travelTimeInSeconds),
    trafficDelaySeconds: Math.round(route.summary.trafficDelayInSeconds || 0),
    trafficSections,
    steps,
    summary: trafficSections.length > 0 ? 'Itinéraire optimisé anti-bouchons' : 'Trafic fluide en temps réel',
  };
}

/**
 * Calcul d'itinéraire avec l'API OSRM (moteur OpenStreetMap standard de secours)
 */
async function calculateOsrmRoute(start: Coordinates, end: Coordinates): Promise<RouteInfo> {
  const url = `https://router.project-osrm.org/route/v1/driving/${start[0]},${start[1]};${end[0]},${end[1]}?overview=full&geometries=geojson&steps=true`;

  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Erreur réseau OSRM (${response.status})`);
    }

    const data = await response.json();
    if (!data.routes || data.routes.length === 0) {
      throw new Error('Aucun itinéraire trouvé');
    }

    const route = data.routes[0];
    const steps: RouteStep[] = [];

    if (route.legs && route.legs[0] && route.legs[0].steps) {
      for (const step of route.legs[0].steps) {
        steps.push({
          instruction: formatInstruction(step),
          name: step.name || '',
          distance: Math.round(step.distance),
          duration: Math.round(step.duration),
          location: step.maneuver.location as Coordinates,
          type: step.maneuver.type,
          modifier: step.maneuver.modifier,
        });
      }
    }

    return {
      coordinates: route.geometry.coordinates as Coordinates[],
      distance: Math.round(route.distance),
      duration: Math.round(route.duration),
      steps,
      summary: route.legs?.[0]?.summary || 'Itinéraire rapide',
    };
  } catch (error) {
    console.warn('Fallback routage direct en ligne droite avec interpolation', error);
    const pointsCount = 20;
    const interpolatedCoords: Coordinates[] = [];
    for (let i = 0; i <= pointsCount; i++) {
      const t = i / pointsCount;
      interpolatedCoords.push([
        start[0] + (end[0] - start[0]) * t,
        start[1] + (end[1] - start[1]) * t,
      ]);
    }
    return {
      coordinates: interpolatedCoords,
      distance: 1200,
      duration: 180,
      steps: [
        {
          instruction: 'Foncez vers le point de passage',
          name: 'Boulevard Principal',
          distance: 1200,
          duration: 180,
          location: start,
          type: 'depart',
        },
        {
          instruction: 'Destination en vue !',
          name: 'Repaire',
          distance: 0,
          duration: 0,
          location: end,
          type: 'arrive',
        },
      ],
      summary: 'Route directe',
    };
  }
}

/**
 * Calcul d'itinéraire intelligent :
 * - Si une clé TomTom est enregistrée : calcul dynamique prenant en compte les bouchons en direct
 * - Sinon : calcul OSRM classique
 */
export async function calculateRoute(start: Coordinates, end: Coordinates): Promise<RouteInfo> {
  const settings = getRadarTrafficSettings();
  const tomtomApiKey = settings.tomtomApiKey?.trim();

  if (tomtomApiKey && tomtomApiKey.length > 5) {
    try {
      return await calculateTomTomRoute(start, end, tomtomApiKey);
    } catch (err) {
      console.warn('Calcul d\'itinéraire TomTom indisponible, repli automatique sur OSRM:', err);
    }
  }

  return await calculateOsrmRoute(start, end);
}
