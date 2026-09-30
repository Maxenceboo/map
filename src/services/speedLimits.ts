/**
 * Service de détection des limitations de vitesse réelles françaises (Code de la route)
 */

export function detectSpeedLimitFromName(name: string): number {
  if (!name) return 50;
  const lower = name.toLowerCase().trim();

  // 1. Rocade bordelaise A630 ou rocades / périphériques urbains spécifiques
  if (
    lower.includes('a630') || 
    lower.includes('rocade') || 
    lower.includes('périphérique') || 
    lower.includes('peripherique')
  ) {
    return 90;
  }

  // 2. Autoroutes (A10, A62, A63, A1, A4, etc.) -> 130 km/h
  if (/\b(a\s?\d{1,3})\b/i.test(lower) || lower.includes('autoroute')) {
    return 130;
  }

  // 3. Voies express / 2x2 voies séparées -> 110 km/h
  if (lower.includes('voie rapide') || lower.includes('voie express')) {
    return 110;
  }

  // 4. Routes Nationales (N10, N89, N113, etc.) -> 80 km/h
  if (/\b(n\s?\d{1,4})\b/i.test(lower) || lower.includes('route nationale')) {
    return 80;
  }

  // 5. Routes Départementales (D106, D1215, D936, etc.) -> 80 km/h
  if (/\b(d\s?\d{1,4}[a-z]?)\b/i.test(lower) || lower.includes('route départementale')) {
    return 80;
  }

  // 6. Rues piétonnes, impasses, ruelles, zones 30 -> 30 km/h
  if (
    lower.includes('impasse') ||
    lower.includes('ruelle') ||
    lower.includes('passage') ||
    lower.includes('sentier') ||
    lower.includes('zone 30') ||
    lower.includes('piéton') ||
    lower.includes('pieton')
  ) {
    return 30;
  }

  // 7. Avenues, boulevards, rues de ville -> 50 km/h
  if (
    lower.includes('rue') ||
    lower.includes('avenue') ||
    lower.includes('boulevard') ||
    lower.includes('cours') ||
    lower.includes('place') ||
    lower.includes('quai') ||
    lower.includes('allée') ||
    lower.includes('allee') ||
    lower.includes('chemin')
  ) {
    return 50;
  }

  // Par défaut en agglomération : 50 km/h
  return 50;
}

export function detectSpeedLimitFromFeature(properties: Record<string, any> | null | undefined): number {
  if (!properties) return 50;

  // Si un nom de voie est renseigné dans la tuile vectorielle, analyse fine
  if (properties.name) {
    return detectSpeedLimitFromName(properties.name);
  }

  // Détection selon la classe de route OpenMapTiles
  const roadClass = properties.class;
  switch (roadClass) {
    case 'motorway':
      return 130;
    case 'trunk':
      return 110;
    case 'primary':
    case 'secondary':
      return 80;
    case 'tertiary':
      return 50;
    case 'minor':
    case 'residential':
    case 'service':
      return 50;
    default:
      return 50;
  }
}

export function getDistanceMeters(c1: [number, number], c2: [number, number]): number {
  const R = 6371e3;
  const φ1 = (c1[1] * Math.PI) / 180;
  const φ2 = (c2[1] * Math.PI) / 180;
  const Δφ = ((c2[1] - c1[1]) * Math.PI) / 180;
  const Δλ = ((c2[0] - c1[0]) * Math.PI) / 180;

  const a =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}
