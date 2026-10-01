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

export interface TrafficSection {
  startIndex: number;
  endIndex: number;
  severity: 'slow' | 'jam';
  delaySeconds?: number;
  speedKmh?: number;
}

export interface RouteInfo {
  coordinates: Coordinates[];
  distance: number; // mètres
  duration: number; // secondes
  steps: RouteStep[];
  summary: string;
  trafficDelaySeconds?: number;
  trafficSections?: TrafficSection[];
}

export type GameTheme = 'gta' | 'cyberpunk' | 'pokemon' | 'zelda';

export type NavState = 'idle' | 'navigating' | 'arrived';

export interface LocationSearchResult {
  name: string;
  label: string;
  city?: string;
  country?: string;
  coordinates: Coordinates;
  distanceMeters?: number;
  type?: 'address' | 'street' | 'city' | 'poi' | 'place';
  postcode?: string;
}

export type RadarType = 'speed' | 'red_light' | 'discriminant' | 'section' | 'crossing';

export interface RadarItem {
  id: string;
  type: RadarType;
  coordinates: Coordinates; // [lng, lat]
  speedLimit: number;
  road?: string;
  place?: string;
  direction?: string;
}

export interface RadarAlert {
  radar: RadarItem;
  distanceMeters: number;
  level: 'warning' | 'urgent';
}

export interface RadarTrafficSettings {
  radarAlertsEnabled: boolean;
  soundAlertsEnabled: boolean;
  trafficEnabled: boolean;
  tomtomApiKey: string;
}

