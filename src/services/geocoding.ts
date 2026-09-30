import { Coordinates, LocationSearchResult } from '../types';

export const PRESET_DESTINATIONS: LocationSearchResult[] = [
  {
    name: 'Bordeaux Centre (Place de la Bourse)',
    label: 'Place de la Bourse, Bordeaux, Gironde',
    city: 'Bordeaux',
    country: 'France',
    coordinates: [-0.5694, 44.8415],
    type: 'poi',
  },
  {
    name: 'Rocade de Bordeaux (A630)',
    label: 'Échangeur Rocade A630 / Mérignac, Gironde',
    city: 'Mérignac',
    country: 'France',
    coordinates: [-0.6482, 44.8315],
    type: 'street',
  },
  {
    name: 'Bassin d’Arcachon (Jetée Thiers)',
    label: 'Arcachon Centre, Bassin d’Arcachon, Gironde',
    city: 'Arcachon',
    country: 'France',
    coordinates: [-1.1685, 44.6642],
    type: 'city',
  },
  {
    name: 'Aéroport de Bordeaux-Mérignac',
    label: 'Aéroport International, Mérignac, Gironde',
    city: 'Mérignac',
    country: 'France',
    coordinates: [-0.7153, 44.8283],
    type: 'poi',
  },
  {
    name: 'Gare Saint-Jean (Bordeaux)',
    label: 'Gare de Bordeaux Saint-Jean, Gironde',
    city: 'Bordeaux',
    country: 'France',
    coordinates: [-0.5567, 44.8259],
    type: 'poi',
  },
  {
    name: 'Lacanau Océan',
    label: 'Front de Mer, Lacanau Océan, Gironde',
    city: 'Lacanau',
    country: 'France',
    coordinates: [-1.1983, 44.9792],
    type: 'city',
  },
  {
    name: 'Cité du Vin',
    label: 'Esplanade de Pontac, Bacalan, Bordeaux',
    city: 'Bordeaux',
    country: 'France',
    coordinates: [-0.5503, 44.8624],
    type: 'poi',
  },
];

/**
 * Normalisation française : retire les accents, tirets, apostrophes et espaces superflus
 */
export function normalizeText(str: string): string {
  return (str || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[-_']/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

/**
 * Calcul de distance en mètres entre deux coordonnées (formule Haversine)
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
 * Bonus / Pénalité de proximité géographique par paliers réalistes
 */
function scoreDistance(distKm: number): number {
  if (distKm < 5) return 80;
  if (distKm < 15) return 60;
  if (distKm < 35) return 40;
  if (distKm < 75) return 25;
  if (distKm < 200) return 10;
  if (distKm < 600) return -5;
  if (distKm < 1200) return -30;
  return -150; // Pénalité sévère pour les résultats hors continent / lointains
}

interface CandidateItem extends LocationSearchResult {
  source: 'ban' | 'photon' | 'preset';
  rawScore?: number;
  _score?: number;
}

/**
 * Moteur de recherche d'adresses & de lieux Hybride Haute Précision :
 * Combine simultanément la Base Adresse Nationale (BAN) officielle française et Photon (OpenStreetMap POIs)
 * avec pondération sémantique, normalisation des accents et proximité géographique réelle.
 */
export async function searchLocations(
  query: string,
  userCoords?: Coordinates,
  signal?: AbortSignal
): Promise<LocationSearchResult[]> {
  const attachDistAndSort = (items: LocationSearchResult[]) => {
    if (!userCoords) return items;
    return items
      .map((item) => ({
        ...item,
        distanceMeters: computeDistanceMeters(userCoords, item.coordinates),
      }))
      .sort((a, b) => (a.distanceMeters || 0) - (b.distanceMeters || 0));
  };

  const qClean = query.trim();
  if (!qClean || qClean.length < 2) {
    return attachDistAndSort(PRESET_DESTINATIONS);
  }

  const qNorm = normalizeText(qClean);
  const isAddressQuery =
    /^\d+/.test(qNorm) ||
    /(rue|avenue|bd|boulevard|chemin|allee|impasse|place|route|cours|quai|square|rond point|voie)/i.test(
      qNorm
    );

  const [lon, lat] = userCoords || [-0.5792, 44.8378];

  // 1. Appel en parallèle BAN (Adresses exactes de France)
  const fetchBAN = async (): Promise<CandidateItem[]> => {
    try {
      const url = `https://api-adresse.data.gouv.fr/search/?q=${encodeURIComponent(
        qClean
      )}&lat=${lat}&lon=${lon}&limit=8`;
      const res = await fetch(url, {
        signal: signal || AbortSignal.timeout(2800),
      });
      if (!res.ok) return [];
      const data = await res.json();
      return (data.features || []).map((f: any) => {
        const p = f.properties;
        const type: 'address' | 'street' | 'city' =
          p.type === 'housenumber'
            ? 'address'
            : p.type === 'municipality'
            ? 'city'
            : 'street';
        return {
          source: 'ban',
          name: p.name || p.label,
          label:
            [p.postcode, p.city, p.context?.split(',')[1]?.trim()].filter(Boolean).join(', ') ||
            p.label,
          city: p.city || '',
          postcode: p.postcode || '',
          country: 'France',
          coordinates: f.geometry.coordinates as Coordinates,
          type,
          rawScore: p.score || 0.5,
        };
      });
    } catch {
      return [];
    }
  };

  // 2. Appel en parallèle Photon (Points d'intérêts, commerces, gares, aéroports, repères mondiaux)
  const fetchPhoton = async (): Promise<CandidateItem[]> => {
    try {
      const url = `https://photon.komoot.io/api/?q=${encodeURIComponent(
        qClean
      )}&lat=${lat}&lon=${lon}&limit=8`;
      const res = await fetch(url, {
        signal: signal || AbortSignal.timeout(2800),
      });
      if (!res.ok) return [];
      const data = await res.json();
      return (data.features || [])
        .filter((f: any) => {
          const code = f.properties?.countrycode;
          // Filtrer les pays hors Europe si l'utilisateur est en France/Europe
          if (
            code &&
            !['FR', 'BE', 'CH', 'ES', 'IT', 'DE', 'GB', 'LU', 'NL', 'MC', 'AD', 'PT'].includes(
              code
            )
          ) {
            return false;
          }
          return true;
        })
        .map((f: any) => {
          const p = f.properties;
          const isHouse = Boolean(p.housenumber);
          const isCity =
            p.type === 'city' ||
            p.osm_value === 'city' ||
            p.osm_value === 'town' ||
            p.osm_value === 'village';
          const isPoi = !isHouse && !isCity && Boolean(p.name);
          const name =
            p.name ||
            (isHouse ? `${p.housenumber} ${p.street || ''}`.trim() : p.street) ||
            p.city ||
            'Lieu';
          const type: 'address' | 'street' | 'city' | 'poi' = isHouse
            ? 'address'
            : isCity
            ? 'city'
            : isPoi
            ? 'poi'
            : 'street';
          const sub = [p.street, p.postcode, p.city, p.country]
            .filter(Boolean)
            .filter((s) => s !== name)
            .join(', ');
          return {
            source: 'photon',
            name,
            label: sub || p.country || '',
            city: p.city || '',
            postcode: p.postcode || '',
            country: p.country || 'France',
            coordinates: f.geometry.coordinates as Coordinates,
            type,
            rawScore: 0.5,
          };
        });
    } catch {
      return [];
    }
  };

  try {
    const [banItems, photonItems] = await Promise.all([fetchBAN(), fetchPhoton()]);
    const candidates: CandidateItem[] = [...banItems, ...photonItems];

    if (candidates.length === 0) {
      // Fallback local sur les presets si hors-ligne
      const filtered = PRESET_DESTINATIONS.filter(
        (p) =>
          normalizeText(p.name).includes(qNorm) || normalizeText(p.label).includes(qNorm)
      );
      return attachDistAndSort(filtered);
    }

    // 3. Système de notation intelligent (Sémantique + Type + Proximité)
    for (const c of candidates) {
      const nameNorm = normalizeText(c.name);
      const labelNorm = normalizeText(c.label);
      const cityNorm = normalizeText(c.city || '');
      let s = 50;

      const isExactCity =
        (cityNorm === qNorm || nameNorm === qNorm) && c.type === 'city';

      if (isExactCity) {
        s += 180; // Priorité absolue aux correspondances exactes de ville (ex : "Arcachon", "Bordeaux")
      } else if (nameNorm === qNorm) {
        s += 120; // Correspondance exacte de nom
      } else if (nameNorm.startsWith(qNorm)) {
        s += 75; // Préfixe
      } else if (nameNorm.includes(qNorm)) {
        s += 45; // Contient
      } else if (labelNorm.includes(qNorm)) {
        s += 25; // Trouvé dans l'adresse secondaire
      }

      // Bonus de pertinence selon le type de requête
      if (isAddressQuery && c.type === 'address') {
        s += 60; // Requête d'adresse avec numéro
      }
      if (!isAddressQuery && c.type === 'poi' && c.source === 'photon') {
        s += 45; // Requête d'enseigne ou commerce (ex : Auchan, Gare Saint-Jean)
      }

      // Pondération de proximité géographique avec le GPS réel
      if (userCoords && c.coordinates) {
        const distM = computeDistanceMeters(userCoords, c.coordinates);
        c.distanceMeters = distM;
        const distKm = distM / 1000;
        s += scoreDistance(distKm);
      }

      c._score = s;
    }

    // Tri par score de pertinence décroissant
    candidates.sort((a, b) => (b._score || 0) - (a._score || 0));

    // 4. Déduplication par proximité spatiale (< 80 mètres)
    const unique: LocationSearchResult[] = [];
    for (const c of candidates) {
      const isDup = unique.some((u) => {
        const d = computeDistanceMeters(u.coordinates, c.coordinates);
        return d < 80;
      });
      if (!isDup) {
        unique.push({
          name: c.name,
          label: c.label,
          city: c.city,
          country: c.country,
          coordinates: c.coordinates,
          distanceMeters: c.distanceMeters,
          type: c.type,
          postcode: c.postcode,
        });
      }
    }

    return unique.slice(0, 8);
  } catch (error) {
    console.warn('Erreur geocoding hybride, fallback', error);
    const filtered = PRESET_DESTINATIONS.filter((p) =>
      normalizeText(p.name).includes(qNorm)
    );
    return attachDistAndSort(filtered);
  }
}

/**
 * Géocodage inverse ultra-précis : trouve l'adresse réelle exacte d'une coordonnée GPS
 */
export async function reverseGeocode(coords: Coordinates): Promise<string> {
  // 1. Essai prioritaire Base Adresse Nationale
  try {
    const banUrl = `https://api-adresse.data.gouv.fr/reverse/?lon=${coords[0]}&lat=${coords[1]}`;
    const res = await fetch(banUrl, { signal: AbortSignal.timeout(2000) });
    if (res.ok) {
      const data = await res.json();
      if (data.features && data.features.length > 0) {
        const label = data.features[0].properties?.label;
        if (label) return label;
      }
    }
  } catch {}

  // 2. Fallback Photon OSM
  try {
    const photonUrl = `https://photon.komoot.io/reverse?lon=${coords[0]}&lat=${coords[1]}`;
    const response = await fetch(photonUrl, { signal: AbortSignal.timeout(2000) });
    if (response.ok) {
      const data = await response.json();
      if (data.features && data.features.length > 0) {
        const props = data.features[0].properties;
        const streetPart = [props.housenumber, props.street].filter(Boolean).join(' ');
        const cityPart = [props.postcode, props.city].filter(Boolean).join(' ');
        const full = [streetPart, cityPart].filter(Boolean).join(', ');
        if (full) return full;
        if (props.name) return props.name;
      }
    }
  } catch (e) {
    console.warn('Erreur reverse geocode', e);
  }

  return `Position GPS (${coords[1].toFixed(4)}, ${coords[0].toFixed(4)})`;
}
