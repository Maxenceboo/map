import React, { useEffect, useRef, useCallback } from 'react';
import * as maplibregl from 'maplibre-gl';
// @ts-ignore
import workerUrl from 'maplibre-gl/dist/maplibre-gl-worker.mjs?worker&url';
import { Coordinates, RouteInfo } from '../types';
import { CarMapTheme, getTheme } from '../styles/mapStyles';
import { VehicleType, getVehicleMarkerHtml } from '../services/vehicleCustomization';
import { Vehicle3DLayer } from '../services/vehicle3DLayer';
import { detectSpeedLimitFromFeature, getDistanceMeters } from '../services/speedLimits';
import { getRadarsInBBox } from '../services/radarService';

try {
  if (workerUrl) {
    maplibregl.setWorkerUrl(workerUrl);
  } else {
    maplibregl.setWorkerUrl('https://unpkg.com/maplibre-gl@4.7.1/dist/maplibre-gl-worker.js');
  }
} catch (e) {
  console.warn('Erreur worker MapLibre:', e);
}

export const DEFAULT_NAV_ZOOM = 16.5;

interface MapProps {
  currentPosition: Coordinates;
  bearing: number;
  route: RouteInfo | null;
  theme: CarMapTheme;
  followUser: boolean;
  is3D: boolean;
  recenterTrigger?: number;
  vehicleType?: VehicleType;
  vehicleColor?: string;
  showHeadlights?: boolean;
  onMapLongPress?: (coords: Coordinates) => void;
  onUserMove?: () => void;
  onRoadSpeedLimitDetected?: (speedLimit: number) => void;
  trafficEnabled?: boolean;
  tomtomApiKey?: string;
  radarAlertsEnabled?: boolean;
}

export const Map: React.FC<MapProps> = ({
  currentPosition,
  bearing,
  route,
  theme,
  followUser,
  is3D,
  recenterTrigger,
  vehicleType = 'arrow_gta',
  vehicleColor = '#facc15',
  showHeadlights = true,
  onMapLongPress,
  onUserMove,
  onRoadSpeedLimitDetected,
  trafficEnabled = true,
  tomtomApiKey = '',
  radarAlertsEnabled = true,
}) => {

  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<maplibregl.Map | null>(null);
  const playerMarkerRef = useRef<maplibregl.Marker | null>(null);
  const vehicle3DLayerRef = useRef<Vehicle3DLayer | null>(null);
  const destMarkerRef = useRef<maplibregl.Marker | null>(null);
  const routeRef = useRef<RouteInfo | null>(route);
  routeRef.current = route;

  const routeSourceId = 'active-route-source';
  const routeGlowLayerId = 'active-route-glow';
  const routeCoreLayerId = 'active-route-core';
  const routeTrafficSourceId = 'route-traffic-sections-source';
  const routeTrafficGlowLayerId = 'route-traffic-sections-glow';
  const routeTrafficCoreLayerId = 'route-traffic-sections-core';

  const radarSourceId = 'radars-source';
  const radarGlowLayerId = 'radars-glow';
  const radarCoreLayerId = 'radars-core';
  const radarLabelLayerId = 'radars-label';

  const trafficSourceId = 'tomtom-traffic-flow';
  const trafficLayerId = 'tomtom-traffic-flow-layer';

  const themeConfig = getTheme(theme);




  // Chargement des textures Minecraft dans le moteur de rendu
  const loadMinecraftTextures = async (map: maplibregl.Map) => {
    const textures = [
      { name: 'minecraft-grass', url: '/textures/minecraft_grass.png' },
      { name: 'minecraft-water', url: '/textures/minecraft_water.png' },
      { name: 'minecraft-pig', url: '/textures/minecraft_pig.png' },
      { name: 'minecraft-tree', url: '/textures/minecraft_tree.png' },
    ];

    for (const { name, url } of textures) {
      if (!map.hasImage(name)) {
        try {
          const res = await map.loadImage(url);
          if (res && res.data && !map.hasImage(name)) {
            map.addImage(name, res.data);
          }
        } catch (e) {
          console.warn('Texture notice:', name, e);
        }
      }
    }
  };

  const setup3DVehicleLayer = useCallback((mapInstance?: maplibregl.Map | null) => {
    const map = mapInstance || mapRef.current;
    if (!map) return;

    const layerId = 'vehicle-3d-model-layer';
    if (map.getLayer(layerId)) {
      try {
        map.removeLayer(layerId);
      } catch (e) {
        // ignore
      }
    }

    const layer = new Vehicle3DLayer(
      currentPosition,
      bearing,
      vehicleType,
      vehicleColor,
      showHeadlights
    );
    vehicle3DLayerRef.current = layer;

    try {
      map.addLayer(layer);
      if (playerMarkerRef.current) {
        playerMarkerRef.current.getElement().style.display = 'none';
      }
    } catch (err) {
      console.error('Erreur chargement layer 3D véhicule:', err);
      if (playerMarkerRef.current) {
        playerMarkerRef.current.getElement().style.display = 'block';
      }
    }
  }, [currentPosition, bearing, vehicleType, vehicleColor, showHeadlights]);

  // Configuration et rafraîchissement de la couche Trafic TomTom
  const setupTomTomTraffic = useCallback((mapInstance?: maplibregl.Map | null) => {
    const map = mapInstance || mapRef.current;
    if (!map || !map.isStyleLoaded()) return;

    try {
      if (map.getLayer(trafficLayerId)) map.removeLayer(trafficLayerId);
      if (map.getSource(trafficSourceId)) map.removeSource(trafficSourceId);
    } catch (_) {}

    if (trafficEnabled && tomtomApiKey && tomtomApiKey.trim().length > 5) {
      const key = encodeURIComponent(tomtomApiKey.trim());
      // Placer sous les pastilles de radars si elles existent
      const beforeRadar = map.getLayer(radarGlowLayerId) ? radarGlowLayerId : undefined;

      try {
        map.addSource(trafficSourceId, {
          type: 'raster',
          tiles: [`https://api.tomtom.com/traffic/map/4/tile/flow/relative0/{z}/{x}/{y}.png?key=${key}`],
          tileSize: 256,
        });

        map.addLayer(
          {
            id: trafficLayerId,
            type: 'raster',
            source: trafficSourceId,
            paint: {
              'raster-opacity': 0.85,
            },
          },
          beforeRadar
        );
      } catch (err) {
        console.warn('Erreur chargement couche TomTom Traffic:', err);
      }
    }
  }, [trafficEnabled, tomtomApiKey]);

  // Configuration et rafraîchissement des pastilles radars sur la carte
  const setupRadarLayers = useCallback((mapInstance?: maplibregl.Map | null) => {
    const map = mapInstance || mapRef.current;
    if (!map || !map.isStyleLoaded()) return;

    try {
      if (map.getLayer(radarLabelLayerId)) map.removeLayer(radarLabelLayerId);
      if (map.getLayer(radarCoreLayerId)) map.removeLayer(radarCoreLayerId);
      if (map.getLayer(radarGlowLayerId)) map.removeLayer(radarGlowLayerId);
      if (map.getSource(radarSourceId)) map.removeSource(radarSourceId);
    } catch (_) {}

    if (!radarAlertsEnabled) return;

    try {
      map.addSource(radarSourceId, {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] },
      });

      // Halo lumineux du radar
      map.addLayer({
        id: radarGlowLayerId,
        type: 'circle',
        source: radarSourceId,
        minzoom: 11,
        paint: {
          'circle-radius': ['interpolate', ['linear'], ['zoom'], 11, 7, 16, 12],
          'circle-color': ['get', 'color'],
          'circle-opacity': 0.75,
          'circle-blur': 0.35,
        },
      });

      // Point central
      map.addLayer({
        id: radarCoreLayerId,
        type: 'circle',
        source: radarSourceId,
        minzoom: 11,
        paint: {
          'circle-radius': ['interpolate', ['linear'], ['zoom'], 11, 4, 16, 6],
          'circle-color': '#ffffff',
          'circle-stroke-width': 2,
          'circle-stroke-color': '#000000',
        },
      });

      // Étiquette vitesse ou FEU
      map.addLayer({
        id: radarLabelLayerId,
        type: 'symbol',
        source: radarSourceId,
        minzoom: 13,
        layout: {
          'text-field': ['get', 'label'],
          'text-size': 11,
          'text-offset': [0, -1.3],
          'text-font': ['Open Sans Bold', 'Arial Unicode MS Bold'],
          'text-allow-overlap': false,
        },
        paint: {
          'text-color': '#ffffff',
          'text-halo-color': '#000000',
          'text-halo-width': 2,
        },
      });
    } catch (err) {
      console.warn('Erreur initialisation couches radars:', err);
    }
  }, [radarAlertsEnabled]);

  // Mise à jour des points radars selon l'emprise visible
  const updateRadars = useCallback(() => {
    const map = mapRef.current;
    if (!map || !map.isStyleLoaded() || !radarAlertsEnabled) return;

    const source = map.getSource(radarSourceId) as maplibregl.GeoJSONSource | undefined;
    if (!source) return;

    const zoom = map.getZoom();
    if (zoom < 10.5) {
      source.setData({ type: 'FeatureCollection', features: [] });
      return;
    }

    const bounds = map.getBounds();
    const items = getRadarsInBBox(
      bounds.getWest(),
      bounds.getSouth(),
      bounds.getEast(),
      bounds.getNorth()
    );

    source.setData({
      type: 'FeatureCollection',
      features: items.map((r) => ({
        type: 'Feature',
        geometry: {
          type: 'Point',
          coordinates: r.coordinates,
        },
        properties: {
          id: r.id,
          type: r.type,
          speedLimit: r.speedLimit,
          label: r.type === 'red_light' ? 'FEU' : String(r.speedLimit),
          color: r.type === 'red_light' ? '#ef4444' : '#f59e0b',
        },
      })),
    });
  }, [radarAlertsEnabled]);


  // Fonction de dessin de l'itinéraire garantie permanente
  const drawRoute = useCallback(() => {
    const map = mapRef.current;
    if (!map) return;

    if (!map.isStyleLoaded()) {
      map.once('styledata', drawRoute);
      return;
    }

    const currentRoute = routeRef.current;

    // Nettoyage sécurisé
    try {
      if (map.getLayer(routeTrafficCoreLayerId)) map.removeLayer(routeTrafficCoreLayerId);
    } catch (_) {}
    try {
      if (map.getLayer(routeTrafficGlowLayerId)) map.removeLayer(routeTrafficGlowLayerId);
    } catch (_) {}
    try {
      if (map.getSource(routeTrafficSourceId)) map.removeSource(routeTrafficSourceId);
    } catch (_) {}
    try {
      if (map.getLayer(routeCoreLayerId)) map.removeLayer(routeCoreLayerId);
    } catch (_) {}
    try {
      if (map.getLayer(routeGlowLayerId)) map.removeLayer(routeGlowLayerId);
    } catch (_) {}
    try {
      if (map.getSource(routeSourceId)) map.removeSource(routeSourceId);
    } catch (_) {}

    if (destMarkerRef.current) {
      destMarkerRef.current.remove();
      destMarkerRef.current = null;
    }
    // Nettoyage de tout balisage de destination résiduel
    if (typeof document !== 'undefined') {
      document.querySelectorAll('.dest-beacon').forEach((el) => el.remove());
    }

    if (!currentRoute || !currentRoute.coordinates || currentRoute.coordinates.length < 2) {
      map.triggerRepaint();
      return;
    }

    const geojsonData = {
      type: 'Feature' as const,
      properties: {},
      geometry: {
        type: 'LineString' as const,
        coordinates: currentRoute.coordinates,
      },
    };

    map.addSource(routeSourceId, {
      type: 'geojson',
      data: geojsonData,
    });

    // 1. Bordure / Halo néon du tracé
    map.addLayer({
      id: routeGlowLayerId,
      type: 'line',
      source: routeSourceId,
      layout: {
        'line-join': 'round',
        'line-cap': 'round',
      },
      paint: {
        'line-color': themeConfig.routeGlowColor,
        'line-width': ['interpolate', ['linear'], ['zoom'], 10, 8, 16, 18],
        'line-opacity': 0.85,
        'line-blur': 2,
      },
    });

    // 2. Cœur lumineux du tracé
    map.addLayer({
      id: routeCoreLayerId,
      type: 'line',
      source: routeSourceId,
      layout: {
        'line-join': 'round',
        'line-cap': 'round',
      },
      paint: {
        'line-color': themeConfig.routeColor,
        'line-width': ['interpolate', ['linear'], ['zoom'], 10, 4, 16, 8],
        'line-opacity': 0.95,
      },
    });

    // 3. Surbrillance des sections de bouchons / ralentissements directement sur le tracé
    if (currentRoute.trafficSections && currentRoute.trafficSections.length > 0) {
      const trafficFeatures = currentRoute.trafficSections
        .map((sec, idx) => {
          const coords = currentRoute.coordinates.slice(sec.startIndex, sec.endIndex + 1);
          if (coords.length < 2) return null;
          const isJam = sec.severity === 'jam';
          return {
            type: 'Feature' as const,
            properties: {
              id: idx,
              severity: sec.severity,
              color: isJam ? '#ef4444' : '#f97316', // Rouge vif pour bouchon, Orange vif pour ralenti
              glowColor: isJam ? '#dc2626' : '#ea580c',
            },
            geometry: {
              type: 'LineString' as const,
              coordinates: coords,
            },
          };
        })
        .filter(Boolean);

      if (trafficFeatures.length > 0) {
        map.addSource(routeTrafficSourceId, {
          type: 'geojson',
          data: {
            type: 'FeatureCollection',
            features: trafficFeatures as any,
          },
        });

        // Halo lumineux du bouchon sur le tracé
        map.addLayer({
          id: routeTrafficGlowLayerId,
          type: 'line',
          source: routeTrafficSourceId,
          layout: {
            'line-join': 'round',
            'line-cap': 'round',
          },
          paint: {
            'line-color': ['get', 'glowColor'],
            'line-width': ['interpolate', ['linear'], ['zoom'], 10, 8, 16, 20],
            'line-opacity': 0.95,
            'line-blur': 2,
          },
        });

        // Ligne vive de congestion (Orange ou Rouge)
        map.addLayer({
          id: routeTrafficCoreLayerId,
          type: 'line',
          source: routeTrafficSourceId,
          layout: {
            'line-join': 'round',
            'line-cap': 'round',
          },
          paint: {
            'line-color': ['get', 'color'],
            'line-width': ['interpolate', ['linear'], ['zoom'], 10, 4, 16, 8],
            'line-opacity': 1.0,
          },
        });
      }
    }

    // 3. Marqueur de destination au bout du chemin
    const destCoords = currentRoute.coordinates[currentRoute.coordinates.length - 1];
    const destEl = document.createElement('div');
    destEl.className = 'dest-beacon';

    if (theme === 'minecraft') {
      destEl.innerHTML = `
        <div class="flex flex-col items-center">
          <div class="px-2 py-1 bg-red-600 border-2 border-black text-white text-[10px] font-pixel shadow-2xl">
            CIBLE
          </div>
          <div class="w-3 h-3 bg-red-600 border-2 border-black rotate-45 -mt-1 shadow-lg"></div>
        </div>
      `;
    } else {
      destEl.innerHTML = `
        <div class="flex flex-col items-center">
          <div class="px-2.5 py-1 bg-purple-600 text-white font-bold text-xs rounded-md shadow-2xl border border-white/50">
            ARRIVÉE
          </div>
          <div class="w-3.5 h-3.5 bg-purple-600 border-2 border-white rotate-45 -mt-1.5 shadow-lg"></div>
        </div>
      `;
    }

    destMarkerRef.current = new maplibregl.Marker({
      element: destEl,
      anchor: 'bottom',
    })
      .setLngLat(destCoords)
      .addTo(map);
  }, [route, theme, themeConfig]);

  // 1. Initialisation de la carte
  useEffect(() => {
    if (!mapContainerRef.current) return;

    const map = new maplibregl.Map({
      container: mapContainerRef.current,
      style: themeConfig.mapStyle,
      center: currentPosition,
      zoom: DEFAULT_NAV_ZOOM,
      pitch: is3D ? 55 : 0,
      bearing: bearing,
      attributionControl: false,
    });

    const markerEl = document.createElement('div');
    markerEl.className = 'car-player-marker';
    markerEl.innerHTML = getVehicleMarkerHtml(vehicleType, vehicleColor, showHeadlights);

    const marker = new maplibregl.Marker({
      element: markerEl,
      rotationAlignment: 'map',
      pitchAlignment: 'viewport',
    })
      .setLngLat(currentPosition)
      .addTo(map);

    playerMarkerRef.current = marker;

    // Détection de clic maintenu (Long Press > 550ms) pour poser un point de destination
    let longPressTimer: any = null;
    let startPoint: { x: number; y: number } | null = null;
    let targetCoords: Coordinates | null = null;

    const cancelLongPress = () => {
      if (longPressTimer) {
        clearTimeout(longPressTimer);
        longPressTimer = null;
      }
      startPoint = null;
      targetCoords = null;
    };

    const onPointerDown = (e: any) => {
      cancelLongPress();
      const pt = e.point || { x: e.originalEvent?.clientX || 0, y: e.originalEvent?.clientY || 0 };
      startPoint = { x: pt.x, y: pt.y };
      targetCoords = [e.lngLat.lng, e.lngLat.lat];

      longPressTimer = setTimeout(() => {
        if (targetCoords && onMapLongPress) {
          if ('vibrate' in navigator) {
            try { navigator.vibrate(50); } catch (_) {}
          }
          onMapLongPress(targetCoords);
        }
        cancelLongPress();
      }, 550);
    };

    const onPointerMove = (e: any) => {
      if (!startPoint || !longPressTimer) return;
      const pt = e.point || { x: e.originalEvent?.clientX || 0, y: e.originalEvent?.clientY || 0 };
      const dist = Math.hypot(pt.x - startPoint.x, pt.y - startPoint.y);
      if (dist > 25) {
        cancelLongPress();
      }
    };

    map.on('contextmenu', (e: any) => {
      if (e?.lngLat && onMapLongPress) {
        cancelLongPress();
        if ('vibrate' in navigator) {
          try { navigator.vibrate(50); } catch (_) {}
        }
        onMapLongPress([e.lngLat.lng, e.lngLat.lat]);
      }
    });

    map.on('mousedown', onPointerDown);
    map.on('touchstart', onPointerDown);

    map.on('mousemove', onPointerMove);
    map.on('touchmove', onPointerMove);

    map.on('mouseup', cancelLongPress);
    map.on('touchend', cancelLongPress);

    map.on('dragstart', (e: any) => {
      if (startPoint) {
        const pt = e.point || { x: e.originalEvent?.clientX || 0, y: e.originalEvent?.clientY || 0 };
        const dist = Math.hypot(pt.x - startPoint.x, pt.y - startPoint.y);
        if (dist > 25) cancelLongPress();
      }
      if (e?.originalEvent && onUserMove) onUserMove();
    });

    map.on('zoomstart', (e: any) => {
      cancelLongPress();
      if (e?.originalEvent && onUserMove) onUserMove();
    });

    map.on('boxzoomstart', (e: any) => {
      cancelLongPress();
      if (e?.originalEvent && onUserMove) onUserMove();
    });

    map.on('rotatestart', (e: any) => {
      cancelLongPress();
      if (e?.originalEvent && onUserMove) onUserMove();
    });

    map.on('pitchstart', (e: any) => {
      cancelLongPress();
      if (e?.originalEvent && onUserMove) onUserMove();
    });

    map.on('load', () => {
      map.resize();
      loadMinecraftTextures(map);
      drawRoute();
      setupTomTomTraffic(map);
      setupRadarLayers(map);
      updateRadars();
      setup3DVehicleLayer(map);
    });

    map.on('moveend', () => {
      updateRadars();
    });

    map.on('error', (e) => {
      console.warn('MapLibre notice:', e);
    });

    const handleResize = () => map.resize();
    window.addEventListener('resize', handleResize);

    mapRef.current = map;
    (window as any).carMap = map;
    map.on('zoomend', () => {
      console.log('[CAR_GPS] Zoom level:', map.getZoom().toFixed(2));
      updateRadars();
    });

    return () => {
      window.removeEventListener('resize', handleResize);
      (window as any).carMap = null;
      map.remove();
      mapRef.current = null;
    };
  }, []);

  // 2. Changement de thème (avec re-dessin immédiat de la route, radars et trafic)
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;

    map.setStyle(themeConfig.mapStyle);
    map.once('styledata', () => {
      loadMinecraftTextures(map);
      drawRoute(); // Garantit que l'itinéraire ne disparaît JAMAIS au changement de thème
      setupTomTomTraffic(map);
      setupRadarLayers(map);
      updateRadars();
      setup3DVehicleLayer(map);
    });

    if (playerMarkerRef.current) {
      const el = playerMarkerRef.current.getElement();
      el.innerHTML = getVehicleMarkerHtml(vehicleType, vehicleColor, showHeadlights);
      const arrowEl = document.getElementById('player-arrow');
      if (arrowEl) {
        arrowEl.style.transform = `rotate(${bearing}deg)`;
      }
    }
  }, [theme, drawRoute, vehicleType, vehicleColor, showHeadlights, setupTomTomTraffic, setupRadarLayers, updateRadars]);

  // 2b. Mise à jour dynamique du trafic TomTom quand la clé ou l'option change
  useEffect(() => {
    setupTomTomTraffic();
  }, [setupTomTomTraffic]);

  // 2c. Mise à jour dynamique des radars
  useEffect(() => {
    setupRadarLayers();
    updateRadars();
  }, [setupRadarLayers, updateRadars]);


  const lastPosTimeRef = useRef<number>(Date.now());

  // Mise à jour instantanée du véhicule, couleur ou phares
  useEffect(() => {
    const now = Date.now();
    const timeDelta = Math.max(400, Math.min(2500, now - lastPosTimeRef.current));
    lastPosTimeRef.current = now;

    if (vehicle3DLayerRef.current) {
      vehicle3DLayerRef.current.update(
        currentPosition,
        bearing,
        vehicleType,
        vehicleColor,
        showHeadlights,
        timeDelta
      );
    }

    if (playerMarkerRef.current) {
      const el = playerMarkerRef.current.getElement();
      el.innerHTML = getVehicleMarkerHtml(vehicleType, vehicleColor, showHeadlights);
      const arrowEl = document.getElementById('player-arrow');
      if (arrowEl) {
        arrowEl.style.transform = `rotate(${bearing}deg)`;
      }
    }
  }, [vehicleType, vehicleColor, showHeadlights, bearing, currentPosition]);

  const lastQueriedPosRef = useRef<Coordinates | null>(null);

  // Détection en direct de la route sous le véhicule pour la limitation de vitesse
  useEffect(() => {
    const map = mapRef.current;
    if (!map || !map.isStyleLoaded() || !onRoadSpeedLimitDetected) return;

    if (
      lastQueriedPosRef.current &&
      getDistanceMeters(lastQueriedPosRef.current, currentPosition) < 6
    ) {
      return;
    }
    lastQueriedPosRef.current = currentPosition;

    try {
      const point = map.project(currentPosition);
      const bbox: [maplibregl.PointLike, maplibregl.PointLike] = [
        [point.x - 8, point.y - 8],
        [point.x + 8, point.y + 8],
      ];
      const features = map.queryRenderedFeatures(bbox, {
        layers: [
          'highway_motorway_inner',
          'highway_motorway_casing',
          'highway_major_inner',
          'highway_major_casing',
          'highway_minor',
        ],
      });

      if (features && features.length > 0) {
        const topRoad = features[0];
        const detectedLimit = detectSpeedLimitFromFeature(topRoad.properties);
        onRoadSpeedLimitDetected(detectedLimit);
      }
    } catch (_) {}
  }, [currentPosition, onRoadSpeedLimitDetected]);

  const prevFollowUserRef = useRef<boolean>(followUser);

  // 3. Mise à jour position & cap (caméra de suivi avec zoom classique garanti et transition linéaire)
  useEffect(() => {
    if (!mapRef.current || !playerMarkerRef.current) return;

    playerMarkerRef.current.setLngLat(currentPosition);

    const arrowEl = document.getElementById('player-arrow');
    if (arrowEl) {
      arrowEl.style.transform = `rotate(${bearing}deg)`;
    }

    if (followUser) {
      const justRecentered = !prevFollowUserRef.current;
      mapRef.current.easeTo({
        center: currentPosition,
        zoom: DEFAULT_NAV_ZOOM,
        bearing: is3D ? bearing : 0,
        pitch: is3D ? 55 : 0,
        padding: { top: 120, bottom: 200, left: 0, right: 0 },
        duration: justRecentered ? 750 : 900,
        easing: (t) => t, // Transition linéaire pure sans à-coups ni téléportation
      });
    }

    prevFollowUserRef.current = followUser;
  }, [currentPosition, bearing, followUser, is3D]);

  // Recentrage explicite (clic sur le bouton recentrer, démarrage de trajet ou bouton GPS)
  useEffect(() => {
    if (!mapRef.current || recenterTrigger === undefined || recenterTrigger === 0) return;

    mapRef.current.easeTo({
      center: currentPosition,
      zoom: DEFAULT_NAV_ZOOM,
      bearing: is3D ? bearing : 0,
      pitch: is3D ? 55 : 0,
      padding: { top: 120, bottom: 200, left: 0, right: 0 },
      duration: 750,
    });
  }, [recenterTrigger]);

  // 4. Mise à jour de l'itinéraire quand route change
  useEffect(() => {
    drawRoute();
  }, [route, drawRoute]);

  return (
    <div className="absolute inset-0 w-full h-full overflow-hidden bg-black">
      <div ref={mapContainerRef} className="absolute inset-0 w-full h-full" />
    </div>
  );
};
