import { Coordinates, LocationSearchResult } from '../types';

export const PRESET_DESTINATIONS: LocationSearchResult[] = [
  {
    name: 'Bordeaux Centre (Place de la Bourse)',
    label: 'Place de la Bourse, Bordeaux, Gironde',
    city: 'Bordeaux',
    country: 'France',
    coordinates: [-0.5694, 44.8415],
  },
  {
    name: 'Rocade de Bordeaux (A630)',
    label: 'Échangeur Rocade A630 / Mérignac, Gironde',
    city: 'Mérignac',
    country: 'France',
    coordinates: [-0.6482, 44.8315],
  },
  {
    name: 'Bassin d’Arcachon (Jetée Thiers)',
    label: 'Arcachon Centre, Bassin d’Arcachon, Gironde',
    city: 'Arcachon',
    country: 'France',
    coordinates: [-1.1685, 44.6642],
  },
  {
    name: 'Aéroport de Bordeaux-Mérignac',
    label: 'Aéroport International, Mérignac, Gironde',
    city: 'Mérignac',
    country: 'France',
    coordinates: [-0.7153, 44.8283],
  },
  {
    name: 'Gare Saint-Jean (Bordeaux)',
    label: 'Gare de Bordeaux Saint-Jean, Gironde',
    city: 'Bordeaux',
    country: 'France',
    coordinates: [-0.5567, 44.8259],
  },
  {
    name: 'Lacanau Océan',
    label: 'Front de Mer, Lacanau Océan, Gironde',
    city: 'Lacanau',
    country: 'France',
    coordinates: [-1.1983, 44.9792],
  },
  {
    name: 'Cité du Vin',
    label: 'Esplanade de Pontac, Bacalan, Bordeaux',
    city: 'Bordeaux',
    country: 'France',
    coordinates: [-0.5503, 44.8624],
  },
];

function computeDistanceMeters(c1: Coordinates, c2: Coordinates): number {
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

/**
 * Recherche d'adresse via l'API publique Photon (OSM Geocoding)
 * Priorité géographique stricte selon la position GPS réelle de l'utilisateur (alentours)
 */
export async function searchLocations(
  query: string,
  userCoords?: Coordinates
): Promise<LocationSearchResult[]> {
  const attachDistAndSort = (items: LocationSearchResult[]) => {
    if (!userCoords) return items;
    return items
      .map(item => ({
        ...item,
        distanceMeters: Math.round(computeDistanceMeters(userCoords, item.coordinates)),
      }))
      .sort((a, b) => (a.distanceMeters || 0) - (b.distanceMeters || 0));
  };

  if (!query || query.trim().length < 2) {
    return attachDistAndSort(PRESET_DESTINATIONS);
  }

  try {
    const encoded = encodeURIComponent(query.trim());
    // Biais géographique sur les coordonnées réelles de l'utilisateur
    const lat = userCoords ? userCoords[1] : 44.8378;
    const lon = userCoords ? userCoords[0] : -0.5792;
    const url = `https://photon.komoot.io/api/?q=${encoded}&lat=${lat}&lon=${lon}&location_bias_scale=1.8&limit=10`;
    const response = await fetch(url);

    if (!response.ok) {
      const filtered = PRESET_DESTINATIONS.filter(p => 
        p.name.toLowerCase().includes(query.toLowerCase()) || 
        p.label.toLowerCase().includes(query.toLowerCase())
      );
      return attachDistAndSort(filtered);
    }

    const data = await response.json();
    if (!data.features || data.features.length === 0) {
      return [];
    }

    const results: LocationSearchResult[] = data.features.map((f: any) => {
      const props = f.properties;
      const parts = [props.name, props.street, props.city, props.country].filter(Boolean);
      const coords = f.geometry.coordinates as Coordinates;
      const distance = userCoords ? Math.round(computeDistanceMeters(userCoords, coords)) : undefined;

      return {
        name: props.name || props.street || 'Point de repère',
        label: parts.join(', '),
        city: props.city || '',
        country: props.country || 'France',
        coordinates: coords,
        distanceMeters: distance,
      };
    });

    // Tri en privilégiant la proximité immédiate de l'utilisateur
    if (userCoords) {
      results.sort((a, b) => (a.distanceMeters || 0) - (b.distanceMeters || 0));
    }

    return results;
  } catch (error) {
    console.warn('Erreur geocoding Photon, fallback', error);
    const filtered = PRESET_DESTINATIONS.filter(p => 
      p.name.toLowerCase().includes(query.toLowerCase())
    );
    return attachDistAndSort(filtered);
  }
}

/**
 * Géocodage inverse via l'API publique Photon pour trouver l'adresse d'un point GPS
 */
export async function reverseGeocode(coords: Coordinates): Promise<string> {
  try {
    const url = `https://photon.komoot.io/reverse?lon=${coords[0]}&lat=${coords[1]}`;
    const response = await fetch(url);
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

