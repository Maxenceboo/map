import { StyleSpecification } from 'maplibre-gl';
import gtaStyleJson from './generated/gta.json';
import minecraftStyleJson from './generated/minecraft.json';
import wazeStyleJson from './generated/waze.json';

export type CarMapTheme = 'gta' | 'minecraft' | 'waze';

export interface ThemeConfig {
  id: CarMapTheme;
  name: string;
  badge: string;
  routeColor: string;
  routeGlowColor: string;
  markerType: 'gta' | 'minecraft' | 'waze';
  mapStyle: StyleSpecification;
}

export const CAR_THEMES: Record<CarMapTheme, ThemeConfig> = {
  gta: {
    id: 'gta',
    name: 'GTA V Radar',
    badge: '🎮 GTA V',
    routeColor: '#c084fc', // Violet GPS GTA Online
    routeGlowColor: '#9333ea',
    markerType: 'gta',
    mapStyle: gtaStyleJson as unknown as StyleSpecification,
  },
  minecraft: {
    id: 'minecraft',
    name: 'Carte Minecraft',
    badge: '🟩 Minecraft',
    routeColor: '#ef4444', // Rouge Redstone
    routeGlowColor: '#b91c1c',
    markerType: 'minecraft',
    mapStyle: minecraftStyleJson as unknown as StyleSpecification,
  },
  waze: {
    id: 'waze',
    name: 'Waze Nocturne',
    badge: '🚗 Waze Pro',
    routeColor: '#38bdf8', // Bleu ciel Waze
    routeGlowColor: '#0284c7',
    markerType: 'waze',
    mapStyle: wazeStyleJson as unknown as StyleSpecification,
  },
};

export function getTheme(theme: CarMapTheme): ThemeConfig {
  return CAR_THEMES[theme] || CAR_THEMES.gta;
}
