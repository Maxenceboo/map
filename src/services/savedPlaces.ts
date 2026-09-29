import { Coordinates, LocationSearchResult } from '../types';

export interface SavedPlace {
  id: string; // 'home' | 'work' | custom id
  type: 'home' | 'work' | 'custom';
  name: string;
  label: string;
  coordinates: Coordinates;
  createdAt: number;
}

export interface SavedPlacesState {
  home: SavedPlace | null;
  work: SavedPlace | null;
  favorites: SavedPlace[];
}

const STORAGE_KEY = 'gamemaps_saved_places_v1';

export function getSavedPlaces(): SavedPlacesState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      return {
        home: parsed.home || null,
        work: parsed.work || null,
        favorites: Array.isArray(parsed.favorites) ? parsed.favorites : [],
      };
    }
  } catch (e) {
    console.warn('Failed to load saved places from localStorage:', e);
  }
  return { home: null, work: null, favorites: [] };
}

export function savePlacesState(state: SavedPlacesState): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (e) {
    console.warn('Failed to save places to localStorage:', e);
  }
}

export function setHomePlace(place: { name?: string; label: string; coordinates: Coordinates } | null): SavedPlacesState {
  const current = getSavedPlaces();
  if (place) {
    current.home = {
      id: 'home',
      type: 'home',
      name: place.name || 'Maison',
      label: place.label,
      coordinates: place.coordinates,
      createdAt: Date.now(),
    };
  } else {
    current.home = null;
  }
  savePlacesState(current);
  return current;
}

export function setWorkPlace(place: { name?: string; label: string; coordinates: Coordinates } | null): SavedPlacesState {
  const current = getSavedPlaces();
  if (place) {
    current.work = {
      id: 'work',
      type: 'work',
      name: place.name || 'Travail',
      label: place.label,
      coordinates: place.coordinates,
      createdAt: Date.now(),
    };
  } else {
    current.work = null;
  }
  savePlacesState(current);
  return current;
}

export function addFavoritePlace(place: { name: string; label: string; coordinates: Coordinates }): SavedPlacesState {
  const current = getSavedPlaces();
  // Éviter les doublons exacts de coordonnées
  const exists = current.favorites.some(
    (f) => Math.abs(f.coordinates[0] - place.coordinates[0]) < 0.0001 &&
           Math.abs(f.coordinates[1] - place.coordinates[1]) < 0.0001
  );
  if (!exists) {
    const newFav: SavedPlace = {
      id: `fav_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      type: 'custom',
      name: place.name,
      label: place.label,
      coordinates: place.coordinates,
      createdAt: Date.now(),
    };
    current.favorites = [newFav, ...current.favorites];
    savePlacesState(current);
  }
  return current;
}

export function removeFavoritePlace(id: string): SavedPlacesState {
  const current = getSavedPlaces();
  current.favorites = current.favorites.filter((f) => f.id !== id);
  savePlacesState(current);
  return current;
}

export function savedPlaceToLocationResult(place: SavedPlace): LocationSearchResult {
  return {
    name: place.name,
    label: place.label,
    coordinates: place.coordinates,
  };
}
