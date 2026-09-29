import { Coordinates, RouteInfo, RouteStep } from '../types';

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
 * Calcul d'itinéraire avec l'API OSRM
 */
export async function calculateRoute(start: Coordinates, end: Coordinates): Promise<RouteInfo> {
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
    // Génération d'un tracé de secours si l'API OSRM publique est temporairement indisponible
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
