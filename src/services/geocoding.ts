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

/**
 * Recherche d'adresse via l'API publique Photon (OSM Geocoding)
 */
export async function searchLocations(query: string): Promise<LocationSearchResult[]> {
  if (!query || query.trim().length < 2) {
    return PRESET_DESTINATIONS;
  }

  try {
    const encoded = encodeURIComponent(query.trim());
    // Priorité géographique sur la Gironde / Sud-Ouest (lat ~ 44.8, lon ~ -0.5)
    const url = `https://photon.komoot.io/api/?q=${encoded}&lat=44.8378&lon=-0.5792&limit=5`;
    const response = await fetch(url);

    if (!response.ok) {
      return PRESET_DESTINATIONS.filter(p => 
        p.name.toLowerCase().includes(query.toLowerCase()) || 
        p.label.toLowerCase().includes(query.toLowerCase())
      );
    }

    const data = await response.json();
    if (!data.features || data.features.length === 0) {
      return [];
    }

    return data.features.map((f: any) => {
      const props = f.properties;
      const parts = [props.name, props.street, props.city, props.country].filter(Boolean);
      return {
        name: props.name || props.street || 'Point de repère',
        label: parts.join(', '),
        city: props.city || 'Gironde',
        country: props.country || 'France',
        coordinates: f.geometry.coordinates as Coordinates,
      };
    });
  } catch (error) {
    console.warn('Erreur geocoding Photon, fallback', error);
    return PRESET_DESTINATIONS.filter(p => 
      p.name.toLowerCase().includes(query.toLowerCase())
    );
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

