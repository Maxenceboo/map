export type Coordinates = [number, number]; // [lng, lat]

export interface RouteStep {
  instruction: string;
  name: string;
  distance: number; // en mètres
  duration: number; // en secondes
  location: Coordinates;
  type: string;
  modifier?: string;
}

export interface RouteInfo {
  coordinates: Coordinates[];
  distance: number; // mètres
  duration: number; // secondes
  steps: RouteStep[];
  summary: string;
}

export type GameTheme = 'gta' | 'cyberpunk' | 'pokemon' | 'zelda';

export type NavState = 'idle' | 'navigating' | 'arrived';

export interface LocationSearchResult {
  name: string;
  label: string;
  city?: string;
  country?: string;
  coordinates: Coordinates;
}
