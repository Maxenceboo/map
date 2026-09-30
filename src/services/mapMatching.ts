import { Coordinates } from '../types';
import { getDistanceMeters } from './speedLimits';

/**
 * Calcul du cap (azimut) en degrés [0..360] entre deux coordonnées
 */
export function calculateBearing(start: Coordinates, end: Coordinates): number {
  const startLat = (start[1] * Math.PI) / 180;
  const startLng = (start[0] * Math.PI) / 180;
  const endLat = (end[1] * Math.PI) / 180;
  const endLng = (end[0] * Math.PI) / 180;

  const dLng = endLng - startLng;
  const y = Math.sin(dLng) * Math.cos(endLat);
  const x =
    Math.cos(startLat) * Math.sin(endLat) -
    Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLng);

  const brng = (Math.atan2(y, x) * 180) / Math.PI;
  return (brng + 360) % 360;
}

/**
 * Projection orthogonale d'un point P sur un segment [A, B]
 */
export function projectPointOnSegment(
  p: Coordinates,
  a: Coordinates,
  b: Coordinates
): { point: Coordinates; distanceMeters: number; fraction: number } {
  const dx = b[0] - a[0];
  const dy = b[1] - a[1];
  const l2 = dx * dx + dy * dy;

  if (l2 === 0) {
    return { point: a, distanceMeters: getDistanceMeters(p, a), fraction: 0 };
  }

  let t = ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / l2;
  t = Math.max(0, Math.min(1, t));

  const proj: Coordinates = [a[0] + t * dx, a[1] + t * dy];
  return { point: proj, distanceMeters: getDistanceMeters(p, proj), fraction: t };
}

/**
 * 1. Magnétisation ultra-rapide sur l'itinéraire actif (0ms de latence)
 */
export function snapToRoute(
  pos: Coordinates,
  routeCoords: Coordinates[],
  maxSnapDistanceMeters: number = 38
): {
  snappedPos: Coordinates;
  roadBearing: number;
  distanceToRoad: number;
  segmentIndex: number;
} | null {
  if (!routeCoords || routeCoords.length < 2) return null;

  let minDistance = Infinity;
  let bestPoint = pos;
  let bestBearing = 0;
  let bestIndex = 0;

  for (let i = 0; i < routeCoords.length - 1; i++) {
    const a = routeCoords[i];
    const b = routeCoords[i + 1];
    const proj = projectPointOnSegment(pos, a, b);

    if (proj.distanceMeters < minDistance) {
      minDistance = proj.distanceMeters;
      bestPoint = proj.point;
      bestBearing = calculateBearing(a, b);
      bestIndex = i;
    }
  }

  if (minDistance <= maxSnapDistanceMeters) {
    return {
      snappedPos: bestPoint,
      roadBearing: bestBearing,
      distanceToRoad: minDistance,
      segmentIndex: bestIndex,
    };
  }

  return null;
}

// Cache mémoire pour la détection de la route la plus proche hors itinéraire
let lastNearestQueryTime = 0;
let lastNearestQueryPos: Coordinates | null = null;
let cachedNearestResult: { snappedPos: Coordinates; roadName: string } | null = null;

/**
 * 2. Magnétisation sur la route OpenStreetMap la plus proche en conduite libre (sans trajet actif)
 */
export async function snapToNearestRoad(
  pos: Coordinates,
  maxSnapDistanceMeters: number = 32
): Promise<{ snappedPos: Coordinates; roadName: string } | null> {
  const now = Date.now();

  // Réutilisation du résultat si la position a peu bougé (< 6m) et récente (< 2s)
  if (
    lastNearestQueryPos &&
    cachedNearestResult &&
    now - lastNearestQueryTime < 2000 &&
    getDistanceMeters(pos, lastNearestQueryPos) < 6
  ) {
    return cachedNearestResult;
  }

  lastNearestQueryTime = now;
  lastNearestQueryPos = pos;

  try {
    const url = `https://router.project-osrm.org/nearest/v1/driving/${pos[0]},${pos[1]}?number=1`;
    const res = await fetch(url);
    if (!res.ok) return null;

    const data = await res.json();
    if (data.code === 'Ok' && data.waypoints && data.waypoints.length > 0) {
      const wp = data.waypoints[0];
      const dist = wp.distance;

      if (dist <= maxSnapDistanceMeters && wp.location) {
        cachedNearestResult = {
          snappedPos: [wp.location[0], wp.location[1]] as Coordinates,
          roadName: wp.name || '',
        };
        return cachedNearestResult;
      }
    }
  } catch (err) {
    console.warn('Erreur magnétisation route libre:', err);
  }

  return null;
}
