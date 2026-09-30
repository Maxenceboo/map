import React, { useState, useEffect } from 'react';
import { 
  Search, 
  MapPin, 
  Navigation, 
  CornerUpRight, 
  CornerUpLeft, 
  ArrowUp, 
  ArrowUpRight, 
  ArrowUpLeft, 
  ArrowLeft,
  RotateCcw, 
  Volume2, 
  VolumeX, 
  Crosshair, 
  X, 
  Compass, 
  Radio, 
  Zap, 
  Play,
  Menu,
  Layers,
  Check,
  ChevronRight,
  ChevronLeft,
  Map,
  Gamepad2,
  Box,
  Car,
  Cpu,
  Home,
  Briefcase,
  Star,
  Bookmark,
  Trash2,
  Plus,
  Edit2,
  Loader2,
  Building2,
  Store
} from 'lucide-react';
import { RouteInfo, RouteStep, LocationSearchResult, Coordinates } from '../types';
import { CarMapTheme, CAR_THEMES, getTheme } from '../styles/mapStyles';
import { searchLocations, PRESET_DESTINATIONS, reverseGeocode } from '../services/geocoding';
import { detectSpeedLimitFromName } from '../services/speedLimits';
import { 
  getSavedPlaces, 
  setHomePlace, 
  setWorkPlace, 
  addFavoritePlace, 
  removeFavoritePlace, 
  savedPlaceToLocationResult, 
  SavedPlacesState, 
  SavedPlace 
} from '../services/savedPlaces';
import {
  VehicleType,
  VEHICLE_CONFIGS,
  VEHICLE_COLORS,
  getVehicleSvgString,
} from '../services/vehicleCustomization';
import { Vehicle3DPreview } from './Vehicle3DPreview';

const THEME_NAMES: Record<CarMapTheme, string> = {
  gta: 'GTA V',
  minecraft: 'Minecraft',
  waze: 'Waze',
};

interface CarDashboardProps {
  theme: CarMapTheme;
  onThemeChange: (theme: CarMapTheme) => void;
  is3D?: boolean;
  onToggle3D?: () => void;
  vehicleType?: VehicleType;
  vehicleColor?: string;
  showHeadlights?: boolean;
  onVehicleChange?: (type: VehicleType, color: string, showHeadlights: boolean) => void;
  route: RouteInfo | null;
  isNavigating: boolean;
  currentSpeed: number;
  currentStep: RouteStep | null;
  nextStep: RouteStep | null;
  stepDistanceRemaining: number;
  totalDistanceRemaining: number;
  totalDurationRemaining: number;
  destinationName: string;
  followUser: boolean;
  onToggleFollow: () => void;
  isMuted: boolean;
  onToggleMute: () => void;
  onSelectDestination: (dest: LocationSearchResult) => void;
  onStartNavigation: () => void;
  onCancelPreview: () => void;
  onStopNavigation: () => void;
  onCenterOnGPS: () => void;
  gpsStatus: 'locating' | 'locked' | 'error';
  wakeLockActive: boolean;
  currentPosition?: Coordinates;
  detectedRoadSpeedLimit?: number;
}

export const CarDashboard: React.FC<CarDashboardProps> = ({
  theme,
  onThemeChange,
  is3D = true,
  onToggle3D,
  vehicleType = 'arrow_gta',
  vehicleColor = '#facc15',
  showHeadlights = true,
  onVehicleChange,
  route,
  isNavigating,
  currentSpeed,
  currentStep,
  nextStep: _nextStep,
  stepDistanceRemaining,
  totalDistanceRemaining,
  totalDurationRemaining,
  destinationName,
  followUser,
  onToggleFollow,
  isMuted,
  onToggleMute,
  onSelectDestination,
  onStartNavigation,
  onCancelPreview,
  onStopNavigation,
  onCenterOnGPS,
  gpsStatus,
  wakeLockActive,
  currentPosition,
  detectedRoadSpeedLimit,
}) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [activeMenuSection, setActiveMenuSection] = useState<
    'root' | 'theme' | 'camera' | 'vehicle' | 'vehicle_models' | 'vehicle_colors' | 'audio' | 'system' | 'places'
  >('root');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<LocationSearchResult[]>([]);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [speedLimit, setSpeedLimit] = useState<number>(50);

  const searchAbortRef = React.useRef<AbortController | null>(null);
  const searchDebounceRef = React.useRef<any>(null);

  useEffect(() => {
    if (!route && !destinationName) {
      setSearchQuery('');
    }
    if (route || isNavigating) {
      setIsSearchOpen(false);
    }
  }, [route, destinationName, isNavigating]);

  // Lieux enregistrés & Favoris (Maison, Travail, Favoris personnalisés)
  const [savedPlaces, setSavedPlaces] = useState<SavedPlacesState>(getSavedPlaces);
  const [placeModal, setPlaceModal] = useState<{
    isOpen: boolean;
    mode: 'home' | 'work' | 'custom';
  } | null>(null);
  const [placeCustomName, setPlaceCustomName] = useState('');
  const [placeSearchInput, setPlaceSearchInput] = useState('');
  const [placeSuggestions, setPlaceSuggestions] = useState<LocationSearchResult[]>(PRESET_DESTINATIONS);
  const [isGeocodingCurrentPos, setIsGeocodingCurrentPos] = useState(false);

  const refreshSavedPlaces = () => {
    setSavedPlaces(getSavedPlaces());
  };

  const handlePlaceSearchChange = (val: string) => {
    setPlaceSearchInput(val);
    if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    if (val.trim().length < 2) {
      setPlaceSuggestions(PRESET_DESTINATIONS);
      return;
    }
    searchDebounceRef.current = setTimeout(async () => {
      const res = await searchLocations(val, currentPosition);
      setPlaceSuggestions(res);
    }, 220);
  };

  const handleUseCurrentPositionForPlace = async () => {
    if (!currentPosition) return;
    setIsGeocodingCurrentPos(true);
    try {
      const label = await reverseGeocode(currentPosition);
      if (placeModal?.mode === 'home') {
        const updated = setHomePlace({ name: 'Maison', label, coordinates: currentPosition });
        setSavedPlaces(updated);
      } else if (placeModal?.mode === 'work') {
        const updated = setWorkPlace({ name: 'Travail', label, coordinates: currentPosition });
        setSavedPlaces(updated);
      } else if (placeModal?.mode === 'custom') {
        const name = placeCustomName.trim() || 'Lieu favori';
        const updated = addFavoritePlace({ name, label, coordinates: currentPosition });
        setSavedPlaces(updated);
      }
      setPlaceModal(null);
    } finally {
      setIsGeocodingCurrentPos(false);
    }
  };

  const handleSelectSuggestionForPlace = (dest: LocationSearchResult) => {
    if (placeModal?.mode === 'home') {
      const updated = setHomePlace({ name: 'Maison', label: dest.label || dest.name, coordinates: dest.coordinates });
      setSavedPlaces(updated);
    } else if (placeModal?.mode === 'work') {
      const updated = setWorkPlace({ name: 'Travail', label: dest.label || dest.name, coordinates: dest.coordinates });
      setSavedPlaces(updated);
    } else if (placeModal?.mode === 'custom') {
      const name = placeCustomName.trim() || dest.name;
      const updated = addFavoritePlace({ name, label: dest.label || dest.name, coordinates: dest.coordinates });
      setSavedPlaces(updated);
    }
    setPlaceModal(null);
  };

  // Détection exacte de la limitation selon la route (guidage actif ou conduite libre)
  useEffect(() => {
    if (currentStep && currentStep.name) {
      const limit = detectSpeedLimitFromName(currentStep.name);
      setSpeedLimit(limit);
    } else if (detectedRoadSpeedLimit) {
      setSpeedLimit(detectedRoadSpeedLimit);
    }
  }, [currentStep, detectedRoadSpeedLimit]);

  const isOverSpeed = currentSpeed > speedLimit;
  const activeTheme = getTheme(theme);

  // Formatage des distances et durées
  const formatDistance = (m: number) => {
    if (m < 1000) return `${Math.round(m)} m`;
    return `${(m / 1000).toFixed(1)} km`;
  };

  const formatDuration = (s: number) => {
    const mins = Math.ceil(s / 60);
    if (mins < 60) return `${mins} min`;
    const h = Math.floor(mins / 60);
    return `${h}h ${mins % 60}m`;
  };

  const getEta = (seconds: number) => {
    const d = new Date(Date.now() + seconds * 1000);
    return d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  };

  // Icône manœuvre selon le modificateur
  const renderManeuverIcon = (step: RouteStep | null, small = false) => {
    const size = small ? 'w-5 h-5' : 'w-10 h-10';
    if (!step) return <Navigation className={`${size} text-white`} />;
    const mod = step.modifier?.toLowerCase() || '';
    if (mod.includes('slight right')) return <ArrowUpRight className={`${size} text-white`} />;
    if (mod.includes('slight left')) return <ArrowUpLeft className={`${size} text-white`} />;
    if (mod.includes('right')) return <CornerUpRight className={`${size} text-white`} />;
    if (mod.includes('left')) return <CornerUpLeft className={`${size} text-white`} />;
    if (mod.includes('uturn')) return <RotateCcw className={`${size} text-white`} />;
    return <ArrowUp className={`${size} text-white`} />;
  };

  const renderLocationTypeIcon = (type?: string) => {
    switch (type) {
      case 'address':
        return <Home className="w-4 h-4 text-blue-400" />;
      case 'city':
        return <Building2 className="w-4 h-4 text-emerald-400" />;
      case 'poi':
        return <Store className="w-4 h-4 text-purple-400" />;
      case 'street':
        return <Navigation className="w-4 h-4 text-amber-400" />;
      default:
        return <MapPin className="w-4 h-4 text-neutral-400" />;
    }
  };

  const handleSearchChange = (val: string) => {
    setSearchQuery(val);

    if (searchDebounceRef.current) {
      clearTimeout(searchDebounceRef.current);
    }
    if (searchAbortRef.current) {
      searchAbortRef.current.abort();
    }

    if (val.trim().length < 2) {
      setIsSearching(false);
      setSearchResults(PRESET_DESTINATIONS);
      return;
    }

    setIsSearching(true);
    searchDebounceRef.current = setTimeout(async () => {
      const controller = new AbortController();
      searchAbortRef.current = controller;
      try {
        const res = await searchLocations(val, currentPosition, controller.signal);
        if (!controller.signal.aborted) {
          setSearchResults(res);
          setIsSearching(false);
        }
      } catch (err: any) {
        if (err.name !== 'AbortError') {
          setIsSearching(false);
        }
      }
    }, 220);
  };

  const handleSelect = (dest: LocationSearchResult) => {
    onSelectDestination(dest);
    setSearchQuery(dest.name);
    setIsSearchOpen(false);
  };

  const isPreviewingRoute = route !== null && !isNavigating;

  return (
    <div className="absolute inset-0 pointer-events-none flex flex-col justify-between pt-14 px-4 pb-6 md:pt-16 md:px-6 md:pb-8 z-20 overflow-hidden font-sans">
      {/* ============================================================== */}
      {/* 1. HAUT : MENU & RECHERCHE FLOTTANTS INDÉPENDANTS SANS AUCUN FOND */}
      {/* ============================================================== */}
      <div className="pointer-events-auto flex flex-col gap-3.5 w-full max-w-4xl mx-auto">
        {/* Mode Normal : Menu et Recherche flottants sans conteneur de fond englobant */}
        {!route && !isNavigating && !isSearchOpen && (
          <div className="w-full flex items-center gap-3.5">
            {/* Bouton Menu Flottant */}
            <button
              onClick={() => {
                setActiveMenuSection('root');
                setIsMenuOpen(true);
              }}
              className="h-13 w-13 bg-neutral-900/95 backdrop-blur-md border border-neutral-800 rounded-xl flex items-center justify-center text-neutral-200 hover:text-white hover:bg-neutral-800 active:scale-95 shadow-xl flex-shrink-0 transition-all"
              title="Menu et paramètres"
            >
              <Menu className="w-5 h-5 text-neutral-200" />
            </button>

            {/* Champ de recherche flottant cliquable */}
            <div
              onClick={() => {
                setIsSearchOpen(true);
                if (searchResults.length === 0) setSearchResults(PRESET_DESTINATIONS);
              }}
              className="flex-1 h-13 bg-neutral-900/95 backdrop-blur-md border border-neutral-800 hover:border-neutral-700 rounded-xl px-4 flex items-center gap-3 cursor-pointer shadow-xl overflow-hidden transition-colors"
            >
              <Search className="w-4 h-4 text-neutral-400 flex-shrink-0" />
              <span className={`text-sm md:text-base font-semibold truncate flex-1 ${searchQuery ? 'text-white' : 'text-neutral-400'}`}>
                {searchQuery || "Où aller ? (Bordeaux, Arcachon...)"}
              </span>

              {searchQuery && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setSearchQuery('');
                  }}
                  className="p-1 hover:bg-neutral-800 text-neutral-400 hover:text-white rounded-md text-xs font-black px-2 flex-shrink-0"
                >
                  ✕
                </button>
              )}

              {/* Indicateur d'état GPS discret */}
              <div className="flex items-center pl-1 flex-shrink-0" title={`Signal GPS : ${gpsStatus}`}>
                <span
                  className={`w-2.5 h-2.5 rounded-full ${
                    gpsStatus === 'locked'
                      ? 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.8)]'
                      : gpsStatus === 'locating'
                      ? 'bg-amber-400 animate-pulse shadow-[0_0_8px_rgba(251,191,36,0.8)]'
                      : 'bg-red-500 shadow-[0_0_8px_rgba(239,68,68,0.8)]'
                  }`}
                />
              </div>
            </div>
          </div>
        )}

        {/* Mode Recherche Ouverte : Bouton Retour Flottant + Barre d'Input + Résultats */}
        {!route && !isNavigating && isSearchOpen && (
          <div className="flex flex-col gap-3.5 w-full animate-in fade-in slide-in-from-top-2 duration-150">
            {/* Backdrop sombre pour fermer au clic dehors */}
            <div 
              className="fixed inset-0 bg-black/60 backdrop-blur-sm -z-10"
              onClick={() => setIsSearchOpen(false)}
            />

            {/* Barre supérieure : Bouton retour flottant + Champ de saisie flottant avec form */}
            <form 
              onSubmit={(e) => {
                e.preventDefault();
                if (searchResults.length > 0) handleSelect(searchResults[0]);
              }}
              className="w-full flex items-center gap-3.5"
            >
              <button
                type="button"
                onClick={() => setIsSearchOpen(false)}
                className="h-13 w-13 bg-neutral-900/98 backdrop-blur-xl border border-neutral-800 rounded-xl flex items-center justify-center text-neutral-300 hover:text-white hover:bg-neutral-800 active:scale-95 shadow-xl flex-shrink-0 transition-all"
                title="Fermer la recherche"
              >
                <ArrowLeft className="w-5 h-5" />
              </button>

              <div className="flex-1 h-13 bg-neutral-900/98 backdrop-blur-xl border border-neutral-700 focus-within:border-neutral-400 rounded-xl px-4 flex items-center gap-3 shadow-xl transition-colors">
                {isSearching ? (
                  <Loader2 className="w-4 h-4 text-blue-400 flex-shrink-0 animate-spin" />
                ) : (
                  <Search className="w-4 h-4 text-neutral-400 flex-shrink-0" />
                )}
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => handleSearchChange(e.target.value)}
                  placeholder="Adresse, ville, commerce, gare..."
                  autoFocus
                  className="flex-1 bg-transparent text-white text-sm md:text-base font-semibold focus:outline-none placeholder-neutral-500"
                />

                {searchQuery && (
                  <button
                    type="button"
                    onClick={() => {
                      setSearchQuery('');
                      setSearchResults(PRESET_DESTINATIONS);
                    }}
                    className="p-1 hover:bg-neutral-800 text-neutral-400 hover:text-white rounded-md text-xs font-black px-2 flex-shrink-0"
                  >
                    ✕
                  </button>
                )}
              </div>
            </form>

            {/* Panneau principal de résultats et raccourcis très spacieux */}
            <div className="bg-neutral-950 border border-neutral-800 rounded-xl shadow-2xl overflow-hidden divide-y divide-neutral-900 max-h-[68vh] overflow-y-auto">
              {/* Raccourcis Rapides : MAISON & TRAVAIL (Grands, confortables et lisibles) */}
              <div className="p-3 grid grid-cols-1 sm:grid-cols-2 gap-2.5 bg-neutral-900/60">
                {/* Maison */}
                <div className="flex items-center justify-between p-3 rounded-xl bg-neutral-950 border border-neutral-800 hover:border-neutral-700 active:bg-neutral-900 transition-colors">
                  <button
                    onClick={() => {
                      if (savedPlaces.home) {
                        handleSelect(savedPlaceToLocationResult(savedPlaces.home));
                      } else {
                        setPlaceModal({ isOpen: true, mode: 'home' });
                        setPlaceSearchInput('');
                        setPlaceSuggestions(PRESET_DESTINATIONS);
                      }
                    }}
                    className="flex items-center gap-3 flex-1 text-left min-w-0"
                  >
                    <div className={`w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0 ${
                      savedPlaces.home ? 'bg-blue-600/20 text-blue-400 border border-blue-500/30' : 'bg-neutral-900 text-neutral-400 border border-neutral-800'
                    }`}>
                      <Home className="w-5 h-5" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="text-sm font-bold text-white tracking-wide">Maison</div>
                      <div className="text-xs text-neutral-400 truncate mt-0.5">
                        {savedPlaces.home ? savedPlaces.home.label : '+ Définir une adresse'}
                      </div>
                    </div>
                  </button>
                  {savedPlaces.home && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setPlaceModal({ isOpen: true, mode: 'home' });
                        setPlaceSearchInput('');
                        setPlaceSuggestions(PRESET_DESTINATIONS);
                      }}
                      className="p-2 text-neutral-400 hover:text-white rounded-lg hover:bg-neutral-900 transition-colors ml-1"
                      title="Modifier l'adresse"
                    >
                      <Edit2 className="w-4 h-4" />
                    </button>
                  )}
                </div>

                {/* Travail */}
                <div className="flex items-center justify-between p-3 rounded-xl bg-neutral-950 border border-neutral-800 hover:border-neutral-700 active:bg-neutral-900 transition-colors">
                  <button
                    onClick={() => {
                      if (savedPlaces.work) {
                        handleSelect(savedPlaceToLocationResult(savedPlaces.work));
                      } else {
                        setPlaceModal({ isOpen: true, mode: 'work' });
                        setPlaceSearchInput('');
                        setPlaceSuggestions(PRESET_DESTINATIONS);
                      }
                    }}
                    className="flex items-center gap-3 flex-1 text-left min-w-0"
                  >
                    <div className={`w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0 ${
                      savedPlaces.work ? 'bg-amber-600/20 text-amber-400 border border-amber-500/30' : 'bg-neutral-900 text-neutral-400 border border-neutral-800'
                    }`}>
                      <Briefcase className="w-5 h-5" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="text-sm font-bold text-white tracking-wide">Travail</div>
                      <div className="text-xs text-neutral-400 truncate mt-0.5">
                        {savedPlaces.work ? savedPlaces.work.label : '+ Définir une adresse'}
                      </div>
                    </div>
                  </button>
                  {savedPlaces.work && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setPlaceModal({ isOpen: true, mode: 'work' });
                        setPlaceSearchInput('');
                        setPlaceSuggestions(PRESET_DESTINATIONS);
                      }}
                      className="p-2 text-neutral-400 hover:text-white rounded-lg hover:bg-neutral-900 transition-colors ml-1"
                      title="Modifier l'adresse"
                    >
                      <Edit2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>

              {/* Section Lieux Favoris (si enregistrés) */}
              {savedPlaces.favorites.length > 0 && searchQuery.length < 2 && (
                <div className="bg-neutral-950">
                  <div className="px-4 pt-3 pb-1.5 text-xs font-bold uppercase tracking-wider text-neutral-400 flex items-center gap-2">
                    <Star className="w-3.5 h-3.5 text-amber-400 fill-amber-400" />
                    <span>Favoris enregistrés ({savedPlaces.favorites.length})</span>
                  </div>
                  <div className="divide-y divide-neutral-900">
                    {savedPlaces.favorites.map((fav) => (
                      <div
                        key={fav.id}
                        className="w-full px-4 py-3 hover:bg-neutral-900/80 active:bg-neutral-900 flex items-center justify-between transition-colors"
                      >
                        <button
                          onClick={() => handleSelect(savedPlaceToLocationResult(fav))}
                          className="flex items-center gap-3.5 text-left flex-1 min-w-0"
                        >
                          <div className="w-9 h-9 rounded-lg bg-amber-500/15 border border-amber-500/30 text-amber-400 flex items-center justify-center flex-shrink-0">
                            <Star className="w-4 h-4 fill-amber-400" />
                          </div>
                          <div className="min-w-0 flex-1">
                            <div className="text-sm font-bold text-white truncate">{fav.name}</div>
                            <div className="text-xs text-neutral-400 truncate mt-0.5">{fav.label}</div>
                          </div>
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            const updated = removeFavoritePlace(fav.id);
                            setSavedPlaces(updated);
                          }}
                          className="p-2 text-neutral-500 hover:text-red-400 rounded-lg hover:bg-neutral-900 transition-colors ml-2"
                          title="Supprimer des favoris"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Suggestions ou résultats de recherche */}
              <div className="bg-neutral-950">
                <div className="px-4 pt-3 pb-1.5 text-xs font-bold uppercase tracking-wider text-neutral-400 flex items-center justify-between">
                  <span>
                    {searchQuery.trim().length >= 2
                      ? isSearching
                        ? 'Recherche en cours...'
                        : `Résultats (${searchResults.length})`
                      : 'Destinations suggérées'}
                  </span>
                  {isSearching && (
                    <span className="text-[11px] text-blue-400 font-semibold lowercase animate-pulse">
                      interrogation BAN & OSM...
                    </span>
                  )}
                </div>

                {/* État vide si aucune adresse trouvée */}
                {searchResults.length === 0 && !isSearching && searchQuery.trim().length >= 2 && (
                  <div className="p-8 text-center flex flex-col items-center justify-center">
                    <div className="w-12 h-12 rounded-xl bg-neutral-900 border border-neutral-800 flex items-center justify-center text-neutral-500 mb-3">
                      <Search className="w-6 h-6" />
                    </div>
                    <div className="text-white font-bold text-sm">Aucun résultat trouvé</div>
                    <div className="text-neutral-400 text-xs mt-1 max-w-xs">
                      Vérifiez l’orthographe ou essayez un nom de rue, une ville ou un commerce (ex : Auchan, Gare...).
                    </div>
                  </div>
                )}

                <div className="divide-y divide-neutral-900">
                  {searchResults.map((dest, i) => {
                    const isFav = savedPlaces.favorites.some(
                      (f) =>
                        Math.abs(f.coordinates[0] - dest.coordinates[0]) < 0.0001 &&
                        Math.abs(f.coordinates[1] - dest.coordinates[1]) < 0.0001
                    );
                    return (
                      <div
                        key={i}
                        className="w-full flex items-center justify-between px-4 py-3.5 hover:bg-neutral-900/80 active:bg-neutral-900 transition-colors"
                      >
                        <button
                          onClick={() => handleSelect(dest)}
                          className="flex-1 text-left flex items-center gap-3.5 min-w-0"
                        >
                          <div
                            className={`w-9 h-9 rounded-lg border flex items-center justify-center flex-shrink-0 ${
                              dest.type === 'address'
                                ? 'bg-blue-500/10 border-blue-500/30 text-blue-400'
                                : dest.type === 'city'
                                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                                : dest.type === 'poi'
                                ? 'bg-purple-500/10 border-purple-500/30 text-purple-400'
                                : 'bg-neutral-900 border-neutral-800 text-neutral-400'
                            }`}
                          >
                            {renderLocationTypeIcon(dest.type)}
                          </div>
                          <div className="min-w-0 flex-1">
                            <div className="text-sm font-bold text-white truncate flex items-center gap-2">
                              <span className="truncate">{dest.name}</span>
                              {dest.type === 'city' && (
                                <span className="text-[10px] uppercase font-bold text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-1.5 py-0.5 rounded flex-shrink-0">
                                  Ville
                                </span>
                              )}
                            </div>
                            <div className="text-xs text-neutral-400 truncate mt-0.5 flex items-center gap-1.5">
                              {dest.distanceMeters !== undefined && (
                                <span className="text-emerald-400 font-semibold bg-emerald-500/10 px-1.5 py-0.5 rounded text-[11px] flex-shrink-0">
                                  {formatDistance(dest.distanceMeters)}
                                </span>
                              )}
                              <span className="truncate">{dest.label}</span>
                            </div>
                          </div>
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            if (isFav) {
                              const fav = savedPlaces.favorites.find(
                                (f) =>
                                  Math.abs(f.coordinates[0] - dest.coordinates[0]) < 0.0001 &&
                                  Math.abs(f.coordinates[1] - dest.coordinates[1]) < 0.0001
                              );
                              if (fav) {
                                const updated = removeFavoritePlace(fav.id);
                                setSavedPlaces(updated);
                              }
                            } else {
                              const updated = addFavoritePlace({
                                name: dest.name,
                                label: dest.label || dest.name,
                                coordinates: dest.coordinates,
                              });
                              setSavedPlaces(updated);
                            }
                          }}
                          className="p-2.5 text-neutral-500 hover:text-amber-400 rounded-lg hover:bg-neutral-900 transition-colors ml-2"
                          title={isFav ? 'Retirer des favoris' : 'Ajouter aux favoris'}
                        >
                          <Star
                            className={`w-4 h-4 ${
                              isFav ? 'text-amber-400 fill-amber-400' : 'text-neutral-500 hover:text-amber-300'
                            }`}
                          />
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 2. INSTRUCTIONS DE VIRAGE (En cours de navigation) - Agrandies et avec accès au Menu */}
        {isNavigating && (
          <div className="bg-neutral-900/95 backdrop-blur-md border border-neutral-800 rounded-xl p-4 md:p-5 shadow-2xl flex items-center justify-between gap-4 text-white animate-in slide-in-from-top-3">
            <div className="flex items-center gap-4">
              <div 
                className="p-3.5 md:p-4 rounded-xl shadow-lg flex-shrink-0"
                style={{ backgroundColor: activeTheme.routeColor }}
              >
                {currentStep ? renderManeuverIcon(currentStep) : <Navigation className="w-6 h-6 text-white" />}
              </div>
              <div>
                <div className="text-3xl md:text-5xl font-black text-white leading-none tracking-tight">
                  {currentStep ? formatDistance(stepDistanceRemaining || currentStep.distance) : '--'}
                </div>
                <div className="text-sm md:text-lg font-bold text-neutral-200 mt-1 line-clamp-1">
                  {currentStep ? currentStep.instruction : (destinationName ? `En route vers ${destinationName}` : 'Navigation en cours')}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => {
                  setActiveMenuSection('root');
                  setIsMenuOpen(true);
                }}
                className="p-3.5 rounded-xl bg-neutral-800 hover:bg-neutral-700 active:bg-neutral-600 text-neutral-200 transition-colors"
                title="Menu des options"
              >
                <Menu className="w-6 h-6" />
              </button>
              <button
                onClick={onToggleMute}
                className="p-3.5 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-neutral-300 transition-colors"
                title={isMuted ? 'Activer le son' : 'Couper le son'}
              >
                {isMuted ? <VolumeX className="w-6 h-6 text-red-400" /> : <Volume2 className="w-6 h-6 text-emerald-400" />}
              </button>
            </div>
          </div>
        )}
      </div>

      {/* ============================================================== */}
      {/* 2. CENTRE / BAS : APERÇU D'ITINÉRAIRE (AVANT DE DÉMARRER) */}
      {/* ============================================================== */}
      {isPreviewingRoute && route && (
        <div className="pointer-events-auto w-full max-w-xl mx-auto mb-3 bg-neutral-900/95 backdrop-blur-md border border-neutral-800 rounded-xl p-4 md:p-5 shadow-2xl text-white animate-in slide-in-from-bottom-5">
          <div className="flex items-start justify-between gap-3 mb-3">
            <div>
              <div className="text-xs uppercase font-black text-purple-400 tracking-wider">Itinéraire trouvé</div>
              <h2 className="text-lg md:text-xl font-black text-white mt-0.5 line-clamp-1">{destinationName}</h2>
            </div>
            <button
              onClick={onCancelPreview}
              className="p-2 bg-neutral-800 hover:bg-neutral-700 text-neutral-400 hover:text-white rounded-xl"
              title="Annuler"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <div className="grid grid-cols-3 gap-2 bg-neutral-950/80 border border-neutral-800/80 rounded-2xl p-3 mb-4 text-center">
            <div>
              <div className="text-[10px] uppercase font-bold text-neutral-400">Arrivée</div>
              <div className="text-lg md:text-xl font-black text-emerald-400">{getEta(route.duration)}</div>
            </div>
            <div className="border-x border-neutral-800">
              <div className="text-[10px] uppercase font-bold text-neutral-400">Temps</div>
              <div className="text-lg md:text-xl font-black text-white">{formatDuration(route.duration)}</div>
            </div>
            <div>
              <div className="text-[10px] uppercase font-bold text-neutral-400">Distance</div>
              <div className="text-lg md:text-xl font-black text-neutral-200">{formatDistance(route.distance)}</div>
            </div>
          </div>

          <button
            onClick={onStartNavigation}
            className="w-full py-4 bg-emerald-500 hover:bg-emerald-400 active:bg-emerald-600 text-black font-black text-base md:text-lg rounded-2xl shadow-xl flex items-center justify-center gap-2 transition-all transform active:scale-98"
          >
            <Play className="w-5 h-5 fill-black" />
            <span>DÉMARRER LE TRAJET</span>
          </button>
        </div>
      )}

      {/* ============================================================== */}
      {/* 3. BOUTON RECENTRER (PASSE BLEU QUAND DÉCENTRÉ, BIEN ESPACÉ DE LA BARRE) */}
      {/* ============================================================== */}
      <div 
        className={`pointer-events-auto absolute right-4 md:right-6 z-30 flex flex-col items-end transition-all duration-300 ${
          isNavigating
            ? 'bottom-36 md:bottom-40'
            : 'bottom-6 md:bottom-8'
        }`}
      >
        <button
          onClick={() => {
            onToggleFollow();
            onCenterOnGPS();
          }}
          className={`w-12 h-12 md:w-14 md:h-14 rounded-full shadow-2xl backdrop-blur-md flex items-center justify-center transition-all transform active:scale-95 border relative ${
            !followUser
              ? 'bg-blue-600 hover:bg-blue-500 active:bg-blue-700 text-white border-blue-400 shadow-blue-500/50 scale-105'
              : 'bg-neutral-900/90 hover:bg-neutral-800 text-neutral-400 border-neutral-700/80'
          }`}
          title="Recentrer sur le véhicule (GPS)"
        >
          <Crosshair className={`w-6 h-6 transition-colors ${!followUser ? 'text-white' : 'text-neutral-400'}`} />
          {/* Badge discret si incident GPS */}
          {gpsStatus !== 'locked' && (
            <span 
              className={`absolute -top-0.5 -right-0.5 w-3.5 h-3.5 rounded-full border-2 border-neutral-950 ${
                gpsStatus === 'locating' ? 'bg-amber-400 animate-pulse' : 'bg-red-500'
              }`} 
              title={gpsStatus === 'locating' ? 'Recherche GPS en cours' : 'Signal GPS perdu'}
            />
          )}
        </button>
      </div>

      {/* ============================================================== */}
      {/* 4. BAS : COMPTEUR DE VITESSE + PANNEAU INFOS DU TRAJET */}
      {/* ============================================================== */}
      <div className="pointer-events-auto flex items-end justify-between gap-3 w-full max-w-4xl mx-auto">
        {/* Compteur de vitesse voiture avec Panneau Limitation et Effet Battement Rouge */}
        <div className="relative flex items-center flex-shrink-0">
          {/* Panneau rond limitation de vitesse (Cliquable pour ajuster si besoin) */}
          <button
            onClick={() => {
              const limits = [30, 50, 70, 80, 90, 110, 130];
              const next = limits[(limits.indexOf(speedLimit) + 1) % limits.length];
              setSpeedLimit(next);
            }}
            className={`absolute -top-3 -right-2 z-10 w-8 h-8 md:w-9 md:h-9 bg-white border-[3px] border-red-600 rounded-full flex items-center justify-center shadow-xl transition-transform active:scale-90 ${
              isOverSpeed ? 'animate-bounce shadow-red-500/50' : ''
            }`}
            title="Limitation de vitesse (cliquez pour ajuster)"
          >
            <span className="text-[11px] md:text-xs font-black text-black leading-none font-sans">
              {speedLimit}
            </span>
          </button>

          {/* Cercle compteur avec battement rouge en excès de vitesse */}
          <div className={`flex flex-col items-center justify-center w-20 h-20 md:w-24 md:h-24 backdrop-blur-md border-2 rounded-full shadow-2xl transition-all duration-200 ${
            isOverSpeed
              ? 'speed-excess-beat border-red-500 bg-red-950/40'
              : 'border-neutral-700 bg-neutral-900/90 text-white'
          }`}>
            <span className={`text-3xl md:text-4xl font-black tracking-tight leading-none transition-colors ${
              isOverSpeed ? 'text-red-400 drop-shadow-[0_0_12px_rgba(239,68,68,0.95)]' : 'text-white'
            }`}>
              {Math.round(currentSpeed)}
            </span>
            <span className={`text-[10px] md:text-xs font-black uppercase mt-0.5 transition-colors ${
              isOverSpeed ? 'text-red-300' : 'text-neutral-400'
            }`}>
              km/h
            </span>
          </div>
        </div>

        {/* Panneau de trajet : uniquement Arrivée / Temps / Distance et Quitter */}
        {isNavigating && (
          <div className="flex-1 bg-neutral-900/95 backdrop-blur-md border border-neutral-800 rounded-2xl p-3 md:p-4 shadow-2xl flex items-center justify-between gap-3 text-white">
            <div className="flex items-baseline gap-3 md:gap-5">
              <div>
                <div className="text-[10px] text-neutral-400 uppercase font-black">Arrivée</div>
                <div className="text-xl md:text-2xl font-black text-emerald-400">
                  {getEta(totalDurationRemaining)}
                </div>
              </div>
              <div className="h-6 w-[1px] bg-neutral-800" />
              <div>
                <div className="text-[10px] text-neutral-400 uppercase font-black">Temps</div>
                <div className="text-lg md:text-xl font-black text-white">
                  {formatDuration(totalDurationRemaining)}
                </div>
              </div>
              <div className="h-6 w-[1px] bg-neutral-800 hidden sm:block" />
              <div className="hidden sm:block">
                <div className="text-[10px] text-neutral-400 uppercase font-black">Distance</div>
                <div className="text-base font-bold text-neutral-300">
                  {formatDistance(totalDistanceRemaining)}
                </div>
              </div>
            </div>

            {/* Uniquement le bouton Quitter dans la barre */}
            <button
              onClick={onStopNavigation}
              className="px-3.5 py-2.5 bg-red-600 hover:bg-red-500 active:bg-red-700 text-white rounded-xl shadow-lg flex items-center gap-1.5 text-xs font-black transition-all"
              title="Arrêter le trajet"
            >
              <X className="w-4 h-4" />
              <span>Quitter</span>
            </button>
          </div>
        )}
      </div>

      {/* ============================================================== */}
      {/* 5. MENU & SOUS-MENUS : DESIGN CLASSIQUE NATIF (COINS DROITS)   */}
      {/* ============================================================== */}
      {isMenuOpen && (
        <div 
          className="fixed inset-0 z-50 pointer-events-auto bg-black/80 backdrop-blur-sm flex justify-start animate-in fade-in duration-150"
          onClick={() => setIsMenuOpen(false)}
        >
          <div 
            className="w-full max-w-sm h-full bg-neutral-950 border-r border-neutral-800 flex flex-col justify-between pt-12 px-5 pb-6 shadow-2xl overflow-y-auto select-none text-white animate-in slide-in-from-left duration-200"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Haut du menu */}
            <div className="flex flex-col gap-5">
              {/* Entête */}
              <div className="flex items-center justify-between border-b border-neutral-800/80 pb-3">
                {activeMenuSection === 'root' ? (
                  <h2 className="text-base font-bold text-white tracking-wide uppercase">Paramètres</h2>
                ) : activeMenuSection === 'vehicle_models' || activeMenuSection === 'vehicle_colors' ? (
                  <button
                    onClick={() => setActiveMenuSection('vehicle')}
                    className="flex items-center gap-1.5 text-sm font-medium text-neutral-400 hover:text-white transition-colors"
                  >
                    <ChevronLeft className="w-4 h-4" />
                    <span>Véhicule</span>
                  </button>
                ) : (
                  <button
                    onClick={() => setActiveMenuSection('root')}
                    className="flex items-center gap-1.5 text-sm font-medium text-neutral-400 hover:text-white transition-colors"
                  >
                    <ChevronLeft className="w-4 h-4" />
                    <span>Paramètres</span>
                  </button>
                )}

                {activeMenuSection !== 'root' && (
                  <h3 className="text-sm font-bold text-white uppercase tracking-wider">
                    {activeMenuSection === 'theme' && 'Thème'}
                    {activeMenuSection === 'camera' && 'Perspective'}
                    {activeMenuSection === 'vehicle' && 'Véhicule'}
                    {activeMenuSection === 'vehicle_models' && 'Modèles'}
                    {activeMenuSection === 'vehicle_colors' && 'Couleurs'}
                    {activeMenuSection === 'audio' && 'Audio'}
                    {activeMenuSection === 'system' && 'Système'}
                    {activeMenuSection === 'places' && 'Lieux Favoris'}
                  </h3>
                )}

                <button
                  onClick={() => setIsMenuOpen(false)}
                  className="p-1.5 text-neutral-400 hover:text-white rounded-md hover:bg-neutral-800 transition-colors"
                  title="Fermer"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* -------------------------------------------------------- */}
              {/* MENU RACINE : LISTE FLAT SANS EFFET CARD                 */}
              {/* -------------------------------------------------------- */}
              {activeMenuSection === 'root' && (
                <div className="flex flex-col gap-5 animate-in fade-in duration-100">
                  {/* Section 1 : Carte */}
                  <div className="flex flex-col">
                    <div className="text-[11px] font-bold uppercase tracking-wider text-neutral-500 pb-2 border-b border-neutral-900">
                      Carte & Caméra
                    </div>
                    <div className="divide-y divide-neutral-900">
                      <button
                        onClick={() => setActiveMenuSection('theme')}
                        className="w-full py-3.5 px-1 hover:bg-neutral-900/50 active:bg-neutral-900 flex items-center justify-between text-left transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <Map className="w-4 h-4 text-neutral-400" />
                          <span className="text-sm font-medium text-white">Thème</span>
                        </div>
                        <div className="flex items-center gap-2">
                          {theme === 'gta' ? (
                            <Gamepad2 className="w-4 h-4 text-neutral-400" />
                          ) : theme === 'minecraft' ? (
                            <Box className="w-4 h-4 text-neutral-400" />
                          ) : (
                            <Car className="w-4 h-4 text-neutral-400" />
                          )}
                          <span className="text-sm text-neutral-300">{THEME_NAMES[theme]}</span>
                          <ChevronRight className="w-4 h-4 text-neutral-500" />
                        </div>
                      </button>

                      <button
                        onClick={() => setActiveMenuSection('camera')}
                        className="w-full py-3.5 px-1 hover:bg-neutral-900/50 active:bg-neutral-900 flex items-center justify-between text-left transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <Compass className="w-4 h-4 text-neutral-400" />
                          <span className="text-sm font-medium text-white">Perspective</span>
                        </div>
                        <div className="flex items-center gap-2">
                          {is3D ? (
                            <Compass className="w-4 h-4 text-neutral-400" />
                          ) : (
                            <Map className="w-4 h-4 text-neutral-400" />
                          )}
                          <span className="text-sm text-neutral-300">{is3D ? '3D' : '2D'}</span>
                          <ChevronRight className="w-4 h-4 text-neutral-500" />
                        </div>
                      </button>

                      <button
                        onClick={() => setActiveMenuSection('vehicle')}
                        className="w-full py-3.5 px-1 hover:bg-neutral-900/50 active:bg-neutral-900 flex items-center justify-between text-left transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <Car className="w-4 h-4 text-neutral-400" />
                          <span className="text-sm font-medium text-white">Véhicule & Curseur</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <div 
                            className="w-5 h-5 flex items-center justify-center pointer-events-none"
                            dangerouslySetInnerHTML={{
                              __html: getVehicleSvgString(vehicleType, vehicleColor, false)
                            }}
                          />
                          <span className="text-sm text-neutral-300">
                            {VEHICLE_CONFIGS.find((v) => v.id === vehicleType)?.name || 'Flèche'}
                          </span>
                          <ChevronRight className="w-4 h-4 text-neutral-500" />
                        </div>
                      </button>
                    </div>
                  </div>

                  {/* Section 2 : Navigation & Système */}
                  <div className="flex flex-col">
                    <div className="text-[11px] font-bold uppercase tracking-wider text-neutral-500 pb-2 border-b border-neutral-900">
                      Navigation & Système
                    </div>
                    <div className="divide-y divide-neutral-900">
                      <button
                        onClick={() => setActiveMenuSection('audio')}
                        className="w-full py-3.5 px-1 hover:bg-neutral-900/50 active:bg-neutral-900 flex items-center justify-between text-left transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <Volume2 className="w-4 h-4 text-neutral-400" />
                          <span className="text-sm font-medium text-white">Audio</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <span className={`w-2 h-2 rounded-full ${
                            !isMuted 
                              ? 'bg-emerald-500 shadow-[0_0_6px_rgba(16,185,129,0.8)]' 
                              : 'bg-rose-500 shadow-[0_0_6px_rgba(244,63,94,0.8)]'
                          }`} />
                          {!isMuted ? (
                            <Volume2 className="w-4 h-4 text-neutral-400" />
                          ) : (
                            <VolumeX className="w-4 h-4 text-neutral-500" />
                          )}
                          <span className="text-sm text-neutral-300">{!isMuted ? 'Activé' : 'Désactivé'}</span>
                          <ChevronRight className="w-4 h-4 text-neutral-500" />
                        </div>
                      </button>

                      <button
                        onClick={() => setActiveMenuSection('system')}
                        className="w-full py-3.5 px-1 hover:bg-neutral-900/50 active:bg-neutral-900 flex items-center justify-between text-left transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <Navigation className="w-4 h-4 text-neutral-400" />
                          <span className="text-sm font-medium text-white">Système & GPS</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Navigation className={`w-4 h-4 transition-colors ${
                            gpsStatus === 'locked'
                              ? 'text-emerald-400 fill-emerald-400'
                              : gpsStatus === 'locating'
                              ? 'text-amber-400 fill-amber-400 animate-pulse'
                              : 'text-rose-500 fill-rose-500'
                          }`} />
                          <ChevronRight className="w-4 h-4 text-neutral-500" />
                        </div>
                      </button>
                    </div>
                  </div>

                  {/* Section 3 : Lieux & Favoris */}
                  <div className="flex flex-col">
                    <div className="text-[11px] font-bold uppercase tracking-wider text-neutral-500 pb-2 border-b border-neutral-900">
                      Lieux & Favoris
                    </div>
                    <div className="divide-y divide-neutral-900">
                      <button
                        onClick={() => setActiveMenuSection('places')}
                        className="w-full py-3.5 px-1 hover:bg-neutral-900/50 active:bg-neutral-900 flex items-center justify-between text-left transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <Bookmark className="w-4 h-4 text-neutral-400" />
                          <span className="text-sm font-medium text-white">Lieux enregistrés</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <div className="flex items-center gap-1.5 text-neutral-400">
                            {savedPlaces.home && <Home className="w-3.5 h-3.5 text-blue-400" />}
                            {savedPlaces.work && <Briefcase className="w-3.5 h-3.5 text-amber-400" />}
                            {savedPlaces.favorites.length > 0 && (
                              <span className="text-xs text-neutral-400">
                                {savedPlaces.favorites.length} favori{savedPlaces.favorites.length > 1 ? 's' : ''}
                              </span>
                            )}
                          </div>
                          <ChevronRight className="w-4 h-4 text-neutral-500" />
                        </div>
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* -------------------------------------------------------- */}
              {/* SOUS-MENU : THÈME                                        */}
              {/* -------------------------------------------------------- */}
              {activeMenuSection === 'theme' && (
                <div className="flex flex-col animate-in fade-in duration-100">
                  <div className="divide-y divide-neutral-900">
                    {(['gta', 'minecraft', 'waze'] as CarMapTheme[]).map((themeKey) => {
                      const isSelected = theme === themeKey;
                      const IconComponent = themeKey === 'gta' ? Gamepad2 : themeKey === 'minecraft' ? Box : Car;
                      return (
                        <button
                          key={themeKey}
                          onClick={() => onThemeChange(themeKey)}
                          className="w-full py-3.5 px-1 flex items-center justify-between text-left hover:bg-neutral-900/50 active:bg-neutral-900 transition-colors"
                        >
                          <div className="flex items-center gap-3">
                            <IconComponent className="w-4 h-4 text-neutral-400" />
                            <span className="text-sm font-medium text-white">{THEME_NAMES[themeKey]}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            {isSelected && (
                              <Check className="w-4 h-4 text-blue-500 stroke-[2.5]" />
                            )}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* -------------------------------------------------------- */}
              {/* SOUS-MENU : PERSPECTIVE                                  */}
              {/* -------------------------------------------------------- */}
              {activeMenuSection === 'camera' && (
                <div className="flex flex-col animate-in fade-in duration-100">
                  <div className="divide-y divide-neutral-900">
                    <button
                      onClick={() => { if (!is3D && onToggle3D) onToggle3D(); }}
                      className="w-full py-3.5 px-1 flex items-center justify-between text-left hover:bg-neutral-900/50 active:bg-neutral-900 transition-colors"
                    >
                      <div className="flex items-center gap-3">
                        <Compass className="w-4 h-4 text-neutral-400" />
                        <span className="text-sm font-medium text-white">3D (Cockpit 55°)</span>
                      </div>
                      <div className="flex items-center gap-2">
                        {is3D && (
                          <Check className="w-4 h-4 text-blue-500 stroke-[2.5]" />
                        )}
                      </div>
                    </button>

                    <button
                      onClick={() => { if (is3D && onToggle3D) onToggle3D(); }}
                      className="w-full py-3.5 px-1 flex items-center justify-between text-left hover:bg-neutral-900/50 active:bg-neutral-900 transition-colors"
                    >
                      <div className="flex items-center gap-3">
                        <Map className="w-4 h-4 text-neutral-400" />
                        <span className="text-sm font-medium text-white">2D (Vue du dessus)</span>
                      </div>
                      <div className="flex items-center gap-2">
                        {!is3D && (
                          <Check className="w-4 h-4 text-blue-500 stroke-[2.5]" />
                        )}
                      </div>
                    </button>
                  </div>
                </div>
              )}

              {/* -------------------------------------------------------- */}
              {/* SOUS-MENU : VÉHICULE & CURSEUR (DOSSIER PRINCIPAL)       */}
              {/* -------------------------------------------------------- */}
              {activeMenuSection === 'vehicle' && (
                <div className="flex flex-col gap-4 animate-in fade-in duration-100 pb-2">
                  {/* Aperçu 3D interactif du véhicule sélectionné */}
                  <div className="flex flex-col gap-2.5">
                    <Vehicle3DPreview
                      vehicleType={vehicleType}
                      vehicleColor={vehicleColor}
                      showHeadlights={showHeadlights}
                    />
                    <div className="flex items-center justify-between px-1">
                      <div className="min-w-0 pr-2">
                        <div className="text-sm font-bold text-white tracking-wide truncate">
                          {VEHICLE_CONFIGS.find((v) => v.id === vehicleType)?.name}
                        </div>
                        <div className="text-xs text-neutral-400 mt-0.5 line-clamp-2">
                          {VEHICLE_CONFIGS.find((v) => v.id === vehicleType)?.description}
                        </div>
                      </div>
                      <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-neutral-900 text-neutral-400 border border-neutral-800 flex-shrink-0">
                        {VEHICLE_CONFIGS.find((v) => v.id === vehicleType)?.category}
                      </span>
                    </div>
                  </div>

                  {/* Liste des sous-dossiers / options */}
                  <div className="divide-y divide-neutral-900 border-t border-b border-neutral-900">
                    {/* Sous-dossier : Modèle de véhicule */}
                    <button
                      onClick={() => setActiveMenuSection('vehicle_models')}
                      className="w-full py-3.5 px-1 flex items-center justify-between text-left hover:bg-neutral-900/50 active:bg-neutral-900 transition-colors"
                    >
                      <div className="flex items-center gap-3">
                        <Car className="w-4 h-4 text-neutral-400" />
                        <div className="flex flex-col">
                          <span className="text-sm font-medium text-white">Modèle de véhicule</span>
                          <span className="text-xs text-neutral-500">Choisir le curseur ou la voiture</span>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <div 
                          className="w-5 h-5 flex items-center justify-center pointer-events-none"
                          dangerouslySetInnerHTML={{
                            __html: getVehicleSvgString(vehicleType, vehicleColor, false)
                          }}
                        />
                        <span className="text-sm text-neutral-300">
                          {VEHICLE_CONFIGS.find((v) => v.id === vehicleType)?.name}
                        </span>
                        <ChevronRight className="w-4 h-4 text-neutral-500" />
                      </div>
                    </button>

                    {/* Sous-dossier : Couleur de carrosserie */}
                    {VEHICLE_CONFIGS.find((v) => v.id === vehicleType)?.allowsColor && (
                      <button
                        onClick={() => setActiveMenuSection('vehicle_colors')}
                        className="w-full py-3.5 px-1 flex items-center justify-between text-left hover:bg-neutral-900/50 active:bg-neutral-900 transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <div 
                            className="w-4 h-4 rounded-full border border-neutral-600"
                            style={{ backgroundColor: vehicleColor }}
                          />
                          <div className="flex flex-col">
                            <span className="text-sm font-medium text-white">Couleur carrosserie</span>
                            <span className="text-xs text-neutral-500">Teinte et finitions de peinture</span>
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="text-sm text-neutral-300">
                            {VEHICLE_COLORS.find((c) => c.hex.toLowerCase() === vehicleColor.toLowerCase())?.name || 'Personnalisée'}
                          </span>
                          <ChevronRight className="w-4 h-4 text-neutral-500" />
                        </div>
                      </button>
                    )}

                    {/* Ligne Toggle : Faisceau des phares */}
                    <button
                      onClick={() => onVehicleChange && onVehicleChange(vehicleType, vehicleColor, !showHeadlights)}
                      className="w-full py-3.5 px-1 flex items-center justify-between text-left hover:bg-neutral-900/50 active:bg-neutral-900 transition-colors"
                    >
                      <div className="flex flex-col">
                        <span className="text-sm font-medium text-white">Faisceau des phares</span>
                        <span className="text-xs text-neutral-500">Projection lumineuse nocturne sur la route</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className={`w-2 h-2 rounded-full ${
                          showHeadlights 
                            ? 'bg-emerald-500 shadow-[0_0_6px_rgba(16,185,129,0.8)]' 
                            : 'bg-neutral-600'
                        }`} />
                        <span className="text-sm text-neutral-300">{showHeadlights ? 'Activé' : 'Désactivé'}</span>
                      </div>
                    </button>
                  </div>
                </div>
              )}

              {/* -------------------------------------------------------- */}
              {/* SOUS-MENU : MODÈLES EN LISTE VERTICALE (PAS EN TABLEAU)  */}
              {/* -------------------------------------------------------- */}
              {activeMenuSection === 'vehicle_models' && (
                <div className="flex flex-col animate-in fade-in duration-100">
                  <div className="divide-y divide-neutral-900">
                    {VEHICLE_CONFIGS.map((veh) => {
                      const isSelected = vehicleType === veh.id;
                      return (
                        <button
                          key={veh.id}
                          onClick={() => onVehicleChange && onVehicleChange(veh.id, vehicleColor, showHeadlights)}
                          className="w-full py-3.5 px-1 flex items-center justify-between text-left hover:bg-neutral-900/50 active:bg-neutral-900 transition-colors"
                        >
                          <div className="flex items-center gap-3 min-w-0 pr-2">
                            <div 
                              className="w-8 h-8 flex-shrink-0 flex items-center justify-center pointer-events-none"
                              dangerouslySetInnerHTML={{
                                __html: getVehicleSvgString(
                                  veh.id,
                                  veh.allowsColor ? vehicleColor : veh.defaultColor,
                                  false
                                )
                              }}
                            />
                            <div className="flex flex-col min-w-0">
                              <span className="text-sm font-medium text-white truncate">{veh.name}</span>
                              <span className="text-xs text-neutral-400 line-clamp-1">{veh.description}</span>
                            </div>
                          </div>
                          <div className="flex items-center gap-2.5 flex-shrink-0">
                            <span className="text-[10px] font-bold text-neutral-500 uppercase tracking-wider">
                              {veh.category}
                            </span>
                            {isSelected ? (
                              <Check className="w-4 h-4 text-blue-500 stroke-[2.5]" />
                            ) : (
                              <div className="w-4 h-4" />
                            )}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* -------------------------------------------------------- */}
              {/* SOUS-MENU : COULEURS EN LISTE VERTICALE                  */}
              {/* -------------------------------------------------------- */}
              {activeMenuSection === 'vehicle_colors' && (
                <div className="flex flex-col animate-in fade-in duration-100">
                  <div className="divide-y divide-neutral-900">
                    {VEHICLE_COLORS.map((col) => {
                      const isSelected = vehicleColor.toLowerCase() === col.hex.toLowerCase();
                      return (
                        <button
                          key={col.id}
                          onClick={() => onVehicleChange && onVehicleChange(vehicleType, col.hex, showHeadlights)}
                          className="w-full py-3.5 px-1 flex items-center justify-between text-left hover:bg-neutral-900/50 active:bg-neutral-900 transition-colors"
                        >
                          <div className="flex items-center gap-3">
                            <div 
                              className="w-6 h-6 rounded-full border border-white/20 shadow-sm flex-shrink-0"
                              style={{ backgroundColor: col.hex }}
                            />
                            <span className="text-sm font-medium text-white">{col.name}</span>
                          </div>
                          <div>
                            {isSelected ? (
                              <Check className="w-4 h-4 text-blue-500 stroke-[2.5]" />
                            ) : (
                              <div className="w-4 h-4" />
                            )}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* -------------------------------------------------------- */}
              {/* SOUS-MENU : AUDIO                                        */}
              {/* -------------------------------------------------------- */}
              {activeMenuSection === 'audio' && (
                <div className="flex flex-col animate-in fade-in duration-100">
                  <div className="divide-y divide-neutral-900">
                    <div 
                      onClick={onToggleMute}
                      className="w-full py-3.5 px-1 flex items-center justify-between cursor-pointer hover:bg-neutral-900/50 active:bg-neutral-900 transition-colors"
                    >
                      <div className="flex items-center gap-3">
                        {!isMuted ? <Volume2 className="w-4 h-4 text-neutral-400" /> : <VolumeX className="w-4 h-4 text-neutral-500" />}
                        <span className="text-sm font-medium text-white">Guidage vocal</span>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className={`w-2 h-2 rounded-full ${
                          !isMuted 
                            ? 'bg-emerald-500 shadow-[0_0_6px_rgba(16,185,129,0.8)]' 
                            : 'bg-rose-500 shadow-[0_0_6px_rgba(244,63,94,0.8)]'
                        }`} />
                        <span className="text-xs text-neutral-400">{!isMuted ? 'Activé' : 'Désactivé'}</span>
                        <div className={`w-11 h-6 rounded-full flex items-center px-0.5 transition-colors flex-shrink-0 ${
                          !isMuted ? 'bg-blue-600' : 'bg-neutral-700'
                        }`}>
                          <div className={`w-5 h-5 rounded-full bg-white shadow transform transition-transform ${
                            !isMuted ? 'translate-x-5' : 'translate-x-0'
                          }`} />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* -------------------------------------------------------- */}
              {/* SOUS-MENU : SYSTÈME & GPS                                */}
              {/* -------------------------------------------------------- */}
              {activeMenuSection === 'system' && (
                <div className="flex flex-col animate-in fade-in duration-100">
                  <div className="divide-y divide-neutral-900">
                    <div className="py-3.5 px-1 flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <Navigation className="w-4 h-4 text-neutral-400" />
                        <span className="text-sm font-medium text-white">Signal GPS</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Navigation className={`w-4 h-4 transition-colors ${
                          gpsStatus === 'locked'
                            ? 'text-emerald-400 fill-emerald-400'
                            : gpsStatus === 'locating'
                            ? 'text-amber-400 fill-amber-400 animate-pulse'
                            : 'text-rose-500 fill-rose-500'
                        }`} />
                      </div>
                    </div>

                    <div className="py-3.5 px-1 flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <Cpu className="w-4 h-4 text-neutral-400" />
                        <span className="text-sm font-medium text-white">Maintien de l'écran</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className={`w-2 h-2 rounded-full ${
                          wakeLockActive 
                            ? 'bg-emerald-500 shadow-[0_0_6px_rgba(16,185,129,0.8)]' 
                            : 'bg-neutral-600'
                        }`} />
                        <span className="text-sm text-neutral-400">
                          {wakeLockActive ? 'Actif' : 'Désactivé'}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              )}
              {/* -------------------------------------------------------- */}
              {/* SOUS-MENU : LIEUX ENREGISTRÉS                            */}
              {/* -------------------------------------------------------- */}
              {activeMenuSection === 'places' && (
                <div className="flex flex-col gap-6 animate-in fade-in duration-100">
                  {/* Maison & Travail */}
                  <div className="flex flex-col">
                    <div className="text-[11px] font-bold uppercase tracking-wider text-neutral-500 pb-2 border-b border-neutral-900">
                      Raccourcis Principaux
                    </div>
                    <div className="divide-y divide-neutral-900">
                      {/* Maison */}
                      <div className="py-3 px-1 flex items-center justify-between">
                        <div className="flex items-center gap-3 truncate flex-1 mr-2">
                          <Home className="w-4 h-4 text-blue-400 flex-shrink-0" />
                          <div className="truncate">
                            <div className="text-sm font-medium text-white">Maison</div>
                            <div className="text-xs text-neutral-400 truncate">
                              {savedPlaces.home ? savedPlaces.home.label : 'Non configuré'}
                            </div>
                          </div>
                        </div>
                        <div className="flex items-center gap-1.5 flex-shrink-0">
                          {savedPlaces.home ? (
                            <>
                              <button
                                onClick={() => {
                                  setIsMenuOpen(false);
                                  handleSelect(savedPlaceToLocationResult(savedPlaces.home!));
                                }}
                                className="px-2.5 py-1 bg-blue-600/20 text-blue-400 hover:bg-blue-600/40 rounded text-xs font-semibold"
                              >
                                Y aller
                              </button>
                              <button
                                onClick={() => {
                                  setPlaceModal({ isOpen: true, mode: 'home' });
                                  setPlaceSearchInput('');
                                  setPlaceSuggestions(PRESET_DESTINATIONS);
                                }}
                                className="p-1.5 text-neutral-400 hover:text-white"
                                title="Modifier"
                              >
                                <Edit2 className="w-3.5 h-3.5" />
                              </button>
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  const updated = setHomePlace(null);
                                  setSavedPlaces(updated);
                                }}
                                className="p-1.5 text-neutral-400 hover:text-red-400"
                                title="Supprimer"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </>
                          ) : (
                            <button
                              onClick={() => {
                                setPlaceModal({ isOpen: true, mode: 'home' });
                                setPlaceSearchInput('');
                                setPlaceSuggestions(PRESET_DESTINATIONS);
                              }}
                              className="px-2.5 py-1 bg-neutral-900 hover:bg-neutral-800 text-blue-400 rounded text-xs font-semibold flex items-center gap-1"
                            >
                              <Plus className="w-3 h-3" />
                              <span>Définir</span>
                            </button>
                          )}
                        </div>
                      </div>

                      {/* Travail */}
                      <div className="py-3 px-1 flex items-center justify-between">
                        <div className="flex items-center gap-3 truncate flex-1 mr-2">
                          <Briefcase className="w-4 h-4 text-amber-400 flex-shrink-0" />
                          <div className="truncate">
                            <div className="text-sm font-medium text-white">Travail</div>
                            <div className="text-xs text-neutral-400 truncate">
                              {savedPlaces.work ? savedPlaces.work.label : 'Non configuré'}
                            </div>
                          </div>
                        </div>
                        <div className="flex items-center gap-1.5 flex-shrink-0">
                          {savedPlaces.work ? (
                            <>
                              <button
                                onClick={() => {
                                  setIsMenuOpen(false);
                                  handleSelect(savedPlaceToLocationResult(savedPlaces.work!));
                                }}
                                className="px-2.5 py-1 bg-amber-600/20 text-amber-400 hover:bg-amber-600/40 rounded text-xs font-semibold"
                              >
                                Y aller
                              </button>
                              <button
                                onClick={() => {
                                  setPlaceModal({ isOpen: true, mode: 'work' });
                                  setPlaceSearchInput('');
                                  setPlaceSuggestions(PRESET_DESTINATIONS);
                                }}
                                className="p-1.5 text-neutral-400 hover:text-white"
                                title="Modifier"
                              >
                                <Edit2 className="w-3.5 h-3.5" />
                              </button>
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  const updated = setWorkPlace(null);
                                  setSavedPlaces(updated);
                                }}
                                className="p-1.5 text-neutral-400 hover:text-red-400"
                                title="Supprimer"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </>
                          ) : (
                            <button
                              onClick={() => {
                                setPlaceModal({ isOpen: true, mode: 'work' });
                                setPlaceSearchInput('');
                                setPlaceSuggestions(PRESET_DESTINATIONS);
                              }}
                              className="px-2.5 py-1 bg-neutral-900 hover:bg-neutral-800 text-amber-400 rounded text-xs font-semibold flex items-center gap-1"
                            >
                              <Plus className="w-3 h-3" />
                              <span>Définir</span>
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Autres Lieux Favoris */}
                  <div className="flex flex-col">
                    <div className="flex items-center justify-between pb-2 border-b border-neutral-900">
                      <span className="text-[11px] font-bold uppercase tracking-wider text-neutral-500">
                        Autres favoris ({savedPlaces.favorites.length})
                      </span>
                      <button
                        onClick={() => {
                          setPlaceModal({ isOpen: true, mode: 'custom' });
                          setPlaceCustomName('');
                          setPlaceSearchInput('');
                          setPlaceSuggestions(PRESET_DESTINATIONS);
                        }}
                        className="text-xs text-blue-400 hover:text-blue-300 font-semibold flex items-center gap-1"
                      >
                        <Plus className="w-3.5 h-3.5" />
                        <span>Ajouter</span>
                      </button>
                    </div>

                    <div className="divide-y divide-neutral-900">
                      {savedPlaces.favorites.length === 0 ? (
                        <div className="py-6 text-center text-xs text-neutral-500">
                          Aucun autre lieu favori enregistré.
                        </div>
                      ) : (
                        savedPlaces.favorites.map((fav) => (
                          <div key={fav.id} className="py-3 px-1 flex items-center justify-between">
                            <div className="flex items-center gap-3 truncate flex-1 mr-2">
                              <Star className="w-4 h-4 text-amber-400 fill-amber-400 flex-shrink-0" />
                              <div className="truncate">
                                <div className="text-sm font-medium text-white truncate">{fav.name}</div>
                                <div className="text-xs text-neutral-400 truncate">{fav.label}</div>
                              </div>
                            </div>
                            <div className="flex items-center gap-1.5 flex-shrink-0">
                              <button
                                onClick={() => {
                                  setIsMenuOpen(false);
                                  handleSelect(savedPlaceToLocationResult(fav));
                                }}
                                className="px-2.5 py-1 bg-neutral-900 hover:bg-neutral-800 text-emerald-400 rounded text-xs font-semibold"
                              >
                                Y aller
                              </button>
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  const updated = removeFavoritePlace(fav.id);
                                  setSavedPlaces(updated);
                                }}
                                className="p-1.5 text-neutral-400 hover:text-red-400"
                                title="Supprimer"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Bouton bas */}
            <div className="pt-4 border-t border-neutral-900">
              <button
                onClick={() => {
                  if (activeMenuSection === 'vehicle_models' || activeMenuSection === 'vehicle_colors') {
                    setActiveMenuSection('vehicle');
                  } else if (activeMenuSection !== 'root') {
                    setActiveMenuSection('root');
                  } else {
                    setIsMenuOpen(false);
                  }
                }}
                className="w-full py-3 bg-neutral-900/80 hover:bg-neutral-800 active:bg-neutral-700 text-neutral-300 font-medium text-sm rounded-md transition-colors"
              >
                {activeMenuSection !== 'root' ? 'Retour' : 'Fermer'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ============================================================== */}
      {/* 6. MODAL AJOUT / MODIFICATION D'UN LIEU (MAISON / TRAVAIL / FAV) */}
      {/* ============================================================== */}
      {placeModal && placeModal.isOpen && (
        <div 
          className="fixed inset-0 z-[60] pointer-events-auto bg-black/80 backdrop-blur-md flex items-center justify-center p-4 animate-in fade-in duration-150 select-none"
          onClick={() => setPlaceModal(null)}
        >
          <div 
            className="bg-neutral-950 border border-neutral-800 rounded-2xl p-5 md:p-6 w-full max-w-lg shadow-2xl text-white flex flex-col gap-4 animate-in zoom-in-95 duration-150"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Entête */}
            <div className="flex items-center justify-between border-b border-neutral-800/80 pb-3.5">
              <div className="flex items-center gap-3">
                {placeModal.mode === 'home' ? (
                  <div className="w-10 h-10 rounded-xl bg-blue-600/20 text-blue-400 border border-blue-500/30 flex items-center justify-center">
                    <Home className="w-5 h-5" />
                  </div>
                ) : placeModal.mode === 'work' ? (
                  <div className="w-10 h-10 rounded-xl bg-amber-600/20 text-amber-400 border border-amber-500/30 flex items-center justify-center">
                    <Briefcase className="w-5 h-5" />
                  </div>
                ) : (
                  <div className="w-10 h-10 rounded-xl bg-amber-600/20 text-amber-400 border border-amber-500/30 flex items-center justify-center">
                    <Star className="w-5 h-5" />
                  </div>
                )}
                <div>
                  <h3 className="text-base font-black uppercase tracking-wide">
                    {placeModal.mode === 'home' && 'Définir Maison'}
                    {placeModal.mode === 'work' && 'Définir Travail'}
                    {placeModal.mode === 'custom' && 'Enregistrer un lieu'}
                  </h3>
                  <div className="text-xs text-neutral-400">Sélectionnez une position ou cherchez une adresse</div>
                </div>
              </div>
              <button
                onClick={() => setPlaceModal(null)}
                className="w-9 h-9 text-neutral-400 hover:text-white rounded-lg hover:bg-neutral-900 flex items-center justify-center transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Si lieu personnalisé : saisie du nom */}
            {placeModal.mode === 'custom' && (
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold uppercase tracking-wider text-neutral-400">
                  Nom du lieu
                </label>
                <input
                  type="text"
                  value={placeCustomName}
                  onChange={(e) => setPlaceCustomName(e.target.value)}
                  placeholder="Ex: Salle de sport, Parents, Bureau..."
                  className="w-full bg-neutral-900 border border-neutral-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-blue-500"
                />
              </div>
            )}

            {/* Bouton rapide : Position GPS actuelle */}
            {currentPosition && (
              <button
                onClick={handleUseCurrentPositionForPlace}
                disabled={isGeocodingCurrentPos}
                className="w-full py-3.5 px-4 bg-neutral-900 hover:bg-neutral-800 active:bg-neutral-700 text-blue-400 border border-neutral-800 hover:border-blue-500/50 rounded-xl text-sm font-bold flex items-center justify-center gap-2.5 transition-colors"
              >
                <Navigation className="w-4 h-4 fill-blue-400" />
                <span>{isGeocodingCurrentPos ? 'Localisation en cours...' : 'Utiliser ma position GPS actuelle'}</span>
              </button>
            )}

            {/* Recherche d'adresse */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold uppercase tracking-wider text-neutral-400">
                Rechercher une adresse
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={placeSearchInput}
                  onChange={(e) => handlePlaceSearchChange(e.target.value)}
                  placeholder="Tapez une adresse ou une ville..."
                  className="w-full bg-neutral-900 border border-neutral-800 rounded-xl px-4 py-3.5 text-base text-white focus:outline-none focus:border-blue-500"
                  autoFocus
                />
                {placeSearchInput && (
                  <button
                    onClick={() => {
                      setPlaceSearchInput('');
                      setPlaceSuggestions(PRESET_DESTINATIONS);
                    }}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-neutral-400 hover:text-white p-1"
                  >
                    ✕
                  </button>
                )}
              </div>
            </div>

            {/* Suggestions d'adresses */}
            <div className="max-h-60 overflow-y-auto divide-y divide-neutral-900 border border-neutral-800/80 rounded-xl">
              {placeSuggestions.map((dest, i) => (
                <button
                  key={i}
                  onClick={() => handleSelectSuggestionForPlace(dest)}
                  className="w-full px-4 py-3 text-left hover:bg-neutral-900 active:bg-neutral-800 flex items-center gap-3 transition-colors"
                >
                  <div className="w-8 h-8 rounded-lg bg-neutral-900 border border-neutral-800 flex items-center justify-center flex-shrink-0">
                    <MapPin className="w-4 h-4 text-neutral-400" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="text-sm font-bold text-white truncate">{dest.name}</div>
                    <div className="text-xs text-neutral-400 truncate mt-0.5">{dest.label}</div>
                  </div>
                </button>
              ))}
            </div>

            {/* Annuler */}
            <button
              onClick={() => setPlaceModal(null)}
              className="w-full py-3 bg-neutral-900 hover:bg-neutral-800 text-neutral-300 hover:text-white font-bold text-sm rounded-xl border border-neutral-800 transition-colors"
            >
              Annuler
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
