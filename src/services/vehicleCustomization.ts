export type VehicleType =
  | 'arrow_gta'
  | 'arrow_waze'
  | 'car_sport'
  | 'car_muscle'
  | 'car_suv'
  | 'car_f1'
  | 'car_moto'
  | 'car_cyber'
  | 'minecraft_arrow';

export interface VehicleConfig {
  id: VehicleType;
  name: string;
  category: 'Sport' | 'Course' | 'Muscle' | '4x4' | 'Deux-roues' | 'Futuriste' | 'Flèche' | 'Rétro';
  description: string;
  defaultColor: string;
  allowsColor: boolean;
  size: number;
}

export interface VehicleColorOption {
  id: string;
  name: string;
  hex: string;
}

export const VEHICLE_CONFIGS: VehicleConfig[] = [
  {
    id: 'car_sport',
    name: 'Supercar GT 3D',
    category: 'Sport',
    description: 'Voiture de sport en relief avec aileron surélevé, diffuseur et phares LED.',
    defaultColor: '#ef4444',
    allowsColor: true,
    size: 58,
  },
  {
    id: 'car_muscle',
    name: 'Muscle Car V8 3D',
    category: 'Muscle',
    description: 'Silhouette musclée avec double bande racing, prise d’air capot et feux triples.',
    defaultColor: '#3b82f6',
    allowsColor: true,
    size: 58,
  },
  {
    id: 'car_f1',
    name: 'Monoplace F1 3D',
    category: 'Course',
    description: 'Monoplace de circuit avec pneus larges, ailerons 3D, halo et feu de pluie.',
    defaultColor: '#f97316',
    allowsColor: true,
    size: 60,
  },
  {
    id: 'car_suv',
    name: 'SUV 4x4 Offroad 3D',
    category: '4x4',
    description: 'Gros baroudeur surélevé avec roue de secours, barres de toit et rampe LED.',
    defaultColor: '#10b981',
    allowsColor: true,
    size: 58,
  },
  {
    id: 'car_moto',
    name: 'Superbike GP 3D',
    category: 'Deux-roues',
    description: 'Moto sportive inclinée avec pilote profilé, échappements sous selle et bulle.',
    defaultColor: '#a855f7',
    allowsColor: true,
    size: 52,
  },
  {
    id: 'car_cyber',
    name: 'Hovercar Cyberpunk 3D',
    category: 'Futuriste',
    description: 'Vaisseau antigravité avec réacteurs à plasma, halo d’énergie et lignes néon.',
    defaultColor: '#06b6d4',
    allowsColor: true,
    size: 58,
  },
  {
    id: 'arrow_gta',
    name: 'Flèche GTA V 3D',
    category: 'Flèche',
    description: 'Le curseur radar iconique avec biseau 3D et ombre portée sur la route.',
    defaultColor: '#facc15',
    allowsColor: true,
    size: 50,
  },
  {
    id: 'arrow_waze',
    name: 'Flèche Waze Aéro 3D',
    category: 'Flèche',
    description: 'Curseur aérodynamique cyan en relief avec dégradé et bordure nette.',
    defaultColor: '#38bdf8',
    allowsColor: true,
    size: 50,
  },
  {
    id: 'minecraft_arrow',
    name: 'Curseur Pixel 3D',
    category: 'Rétro',
    description: 'Curseur officiel Minecraft avec ombre 8-bit surélevée.',
    defaultColor: '#ffffff',
    allowsColor: false,
    size: 52,
  },
];

export const VEHICLE_COLORS: VehicleColorOption[] = [
  { id: 'yellow', name: 'Jaune GTA', hex: '#facc15' },
  { id: 'red', name: 'Rouge Racing', hex: '#ef4444' },
  { id: 'blue', name: 'Bleu Électrique', hex: '#3b82f6' },
  { id: 'cyan', name: 'Cyan Waze', hex: '#06b6d4' },
  { id: 'emerald', name: 'Vert Viper', hex: '#10b981' },
  { id: 'orange', name: 'Orange McLaren', hex: '#f97316' },
  { id: 'purple', name: 'Violet Synth', hex: '#a855f7' },
  { id: 'white', name: 'Blanc Pur', hex: '#f8fafc' },
  { id: 'dark', name: 'Noir Carbone', hex: '#262626' },
];

const STORAGE_KEY_TYPE = 'car_gps_vehicle_type';
const STORAGE_KEY_COLOR = 'car_gps_vehicle_color';
const STORAGE_KEY_HEADLIGHTS = 'car_gps_vehicle_headlights';

export function shadeColor(color: string, percent: number): string {
  if (!color || !color.startsWith('#')) return color;
  let clean = color.slice(1);
  if (clean.length === 3) {
    clean = clean[0] + clean[0] + clean[1] + clean[1] + clean[2] + clean[2];
  }
  const num = parseInt(clean, 16);
  let r = (num >> 16) + Math.round(255 * (percent / 100));
  let g = ((num >> 8) & 0x00ff) + Math.round(255 * (percent / 100));
  let b = (num & 0x0000ff) + Math.round(255 * (percent / 100));
  r = Math.min(255, Math.max(0, r));
  g = Math.min(255, Math.max(0, g));
  b = Math.min(255, Math.max(0, b));
  return `#${((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)}`;
}

export function getSavedVehicleCustomization(): {
  vehicleType: VehicleType;
  vehicleColor: string;
  showHeadlights: boolean;
} {
  try {
    const savedType = localStorage.getItem(STORAGE_KEY_TYPE) as VehicleType;
    const savedColor = localStorage.getItem(STORAGE_KEY_COLOR);
    const savedHl = localStorage.getItem(STORAGE_KEY_HEADLIGHTS);

    const vehicleType: VehicleType =
      savedType && VEHICLE_CONFIGS.some((v) => v.id === savedType)
        ? savedType
        : 'car_sport';

    const config = VEHICLE_CONFIGS.find((v) => v.id === vehicleType);
    const vehicleColor = savedColor || config?.defaultColor || '#ef4444';
    const showHeadlights = savedHl !== null ? savedHl === 'true' : true;

    return { vehicleType, vehicleColor, showHeadlights };
  } catch (e) {
    return { vehicleType: 'car_sport', vehicleColor: '#ef4444', showHeadlights: true };
  }
}

export function saveVehicleCustomization(
  vehicleType: VehicleType,
  vehicleColor: string,
  showHeadlights: boolean
) {
  try {
    localStorage.setItem(STORAGE_KEY_TYPE, vehicleType);
    localStorage.setItem(STORAGE_KEY_COLOR, vehicleColor);
    localStorage.setItem(STORAGE_KEY_HEADLIGHTS, String(showHeadlights));
  } catch (e) {
    console.error('Failed to save vehicle customization', e);
  }
}

/**
 * Génère le code SVG d’un véhicule avec perspective 3D, volume, ombre au sol et éclairage.
 * Pivot centré à (32, 34) et orienté vers le HAUT (direction de la marche).
 */
export function getVehicleSvgString(
  type: VehicleType,
  color: string,
  showHeadlights = false
): string {
  const darkColor = shadeColor(color, -30);
  const midColor = shadeColor(color, -12);
  const lightColor = shadeColor(color, 25);

  // 1. Supercar GT 3D (Volume profilé, aileron surélevé, feux LED 3D)
  if (type === 'car_sport') {
    const headlightCone = showHeadlights
      ? `<polygon points="26,14 6,-32 58,-32 38,14" fill="url(#hlConeSport)" opacity="0.45" />`
      : '';

    return `
      <svg viewBox="0 0 64 64" width="100%" height="100%" style="overflow: visible;">
        <defs>
          <linearGradient id="hlConeSport" x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" stop-color="#fef08a" stop-opacity="0.5" />
            <stop offset="100%" stop-color="#fef08a" stop-opacity="0" />
          </linearGradient>
          <linearGradient id="sportRoof" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#1e293b" />
            <stop offset="100%" stop-color="#090d16" />
          </linearGradient>
        </defs>
        ${headlightCone}

        <!-- Ombre portée 3D au sol -->
        <ellipse cx="32" cy="40" rx="19" ry="17" fill="rgba(0,0,0,0.6)" />

        <!-- Pneus avec épaisseur 3D -->
        <rect x="15" y="42" width="7" height="12" rx="2" fill="#111" stroke="#000" stroke-width="0.8" />
        <rect x="42" y="42" width="7" height="12" rx="2" fill="#111" stroke="#000" stroke-width="0.8" />
        <rect x="17" y="20" width="5.5" height="10" rx="2" fill="#111" stroke="#000" stroke-width="0.8" />
        <rect x="41.5" y="20" width="5.5" height="10" rx="2" fill="#111" stroke="#000" stroke-width="0.8" />

        <!-- Diffuseur arrière en carbone -->
        <rect x="23" y="53" width="18" height="3" rx="1" fill="#09090b" />
        <line x1="28" y1="53" x2="28" y2="56" stroke="#27272a" stroke-width="1" />
        <line x1="36" y1="53" x2="36" y2="56" stroke="#27272a" stroke-width="1" />

        <!-- Bas de caisse / Flancs 3D ombrés -->
        <path d="M20 54 L44 54 L46 38 L45 22 L39 13 L25 13 L19 22 L18 38 Z" fill="${darkColor}" stroke="#000" stroke-width="1.2" />

        <!-- Face supérieure / Carrosserie sculptée -->
        <path d="M26 13 C29 11, 35 11, 38 13 C43 17, 44 24, 44 34 C44 44, 43 51, 41 53 C38 54, 26 54, 23 53 C21 51, 20 44, 20 34 C20 24, 21 17, 26 13 Z" 
              fill="${color}" stroke="${darkColor}" stroke-width="0.8" />

        <!-- Arêtes de relief sur le capot avant -->
        <path d="M28 14 L30 22 M36 14 L34 22" stroke="${lightColor}" stroke-width="1.2" stroke-linecap="round" />

        <!-- Pare-brise avant incliné 3D -->
        <polygon points="25,23 39,23 38,31 26,31" fill="#0f172a" stroke="#334155" stroke-width="0.8" />

        <!-- Toit surélevé (point culminant de la 3D) -->
        <rect x="25.5" y="31" width="13" height="8.5" rx="1" fill="url(#sportRoof)" stroke="#1e293b" stroke-width="0.6" />
        <line x1="27" y1="32" x2="37" y2="32" stroke="#64748b" stroke-width="0.8" opacity="0.6" />

        <!-- Lunette arrière plongeante -->
        <polygon points="26,39.5 38,39.5 39,47 25,47" fill="#0f172a" stroke="#334155" stroke-width="0.8" />

        <!-- Rétroviseurs extérieurs 3D -->
        <ellipse cx="17.5" cy="24" rx="2" ry="1.2" fill="${lightColor}" stroke="#000" stroke-width="0.5" />
        <ellipse cx="46.5" cy="24" rx="2" ry="1.2" fill="${lightColor}" stroke="#000" stroke-width="0.5" />

        <!-- Bandeau de feux arrière LED 3D (visible de dos) -->
        <rect x="22" y="52" width="20" height="2" rx="1" fill="#ef4444" />
        <rect x="24" y="52" width="16" height="0.8" fill="#fca5a5" />

        <!-- Aileron arrière 3D surélevé (flottant au-dessus du coffre) -->
        <line x1="24" y1="48" x2="24" y2="52" stroke="#000" stroke-width="1.5" />
        <line x1="40" y1="48" x2="40" y2="52" stroke="#000" stroke-width="1.5" />
        <rect x="18" y="47" width="28" height="3" rx="1" fill="#09090b" stroke="#3f3f46" stroke-width="0.8" />

        <!-- Phares avant LED xénon -->
        <ellipse cx="25" cy="14" rx="2" ry="1.2" fill="#fef08a" />
        <ellipse cx="39" cy="14" rx="2" ry="1.2" fill="#fef08a" />
      </svg>
    `;
  }

  // 2. Muscle Car V8 3D (Gros gabarit, double bande, nez agressif et feux horizontaux)
  if (type === 'car_muscle') {
    const headlightCone = showHeadlights
      ? `<polygon points="25,12 6,-32 58,-32 39,12" fill="url(#hlConeMuscle)" opacity="0.45" />`
      : '';

    return `
      <svg viewBox="0 0 64 64" width="100%" height="100%" style="overflow: visible;">
        <defs>
          <linearGradient id="hlConeMuscle" x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" stop-color="#fef08a" stop-opacity="0.5" />
            <stop offset="100%" stop-color="#fef08a" stop-opacity="0" />
          </linearGradient>
        </defs>
        ${headlightCone}

        <!-- Ombre portée 3D -->
        <ellipse cx="32" cy="40" rx="20" ry="18" fill="rgba(0,0,0,0.6)" />

        <!-- Pneus arrière extra-larges -->
        <rect x="14" y="41" width="8" height="13" rx="2" fill="#111" stroke="#000" stroke-width="1" />
        <rect x="42" y="41" width="8" height="13" rx="2" fill="#111" stroke="#000" stroke-width="1" />
        <rect x="15" y="19" width="7" height="11" rx="2" fill="#111" stroke="#000" stroke-width="1" />
        <rect x="42" y="19" width="7" height="11" rx="2" fill="#111" stroke="#000" stroke-width="1" />

        <!-- Châssis & flancs musclés 3D -->
        <path d="M20 54 L44 54 L46 22 L43 12 L21 12 L18 22 Z" fill="${darkColor}" stroke="#000" stroke-width="1.2" />

        <!-- Capot et corps principal -->
        <path d="M21 12 L43 12 L44 24 L45 52 L19 52 L20 24 Z" fill="${color}" stroke="${darkColor}" stroke-width="0.8" />

        <!-- Doubles bandes racing blanches traversantes -->
        <rect x="28.5" y="12" width="2.5" height="40" fill="#ffffff" opacity="0.9" />
        <rect x="33" y="12" width="2.5" height="40" fill="#ffffff" opacity="0.9" />

        <!-- Blower / Prise d'air moteur sur le capot -->
        <rect x="27.5" y="16" width="9" height="5.5" rx="1.5" fill="#09090b" stroke="#71717a" stroke-width="0.8" />
        <ellipse cx="32" cy="18" rx="2.5" ry="1.2" fill="#e4e4e7" />

        <!-- Pare-brise teinté -->
        <polygon points="22,23 42,23 41,31 23,31" fill="#0f172a" stroke="#334155" stroke-width="0.8" />

        <!-- Toit surélevé avec volume -->
        <rect x="23" y="31" width="18" height="10" rx="1" fill="${midColor}" stroke="#1e293b" stroke-width="0.6" />

        <!-- Lunette arrière inclinée -->
        <polygon points="23,41 41,41 42,48 22,48" fill="#0f172a" stroke="#334155" stroke-width="0.8" />

        <!-- Becquet arrière Ducktail -->
        <path d="M20 50 L44 50 L44 52.5 L20 52.5 Z" fill="#09090b" />

        <!-- Face arrière verticale 3D avec 6 feux stop classiques -->
        <rect x="20" y="52" width="24" height="3" fill="#18181b" />
        <rect x="22" y="52.5" width="2" height="1.8" fill="#ef4444" />
        <rect x="25" y="52.5" width="2" height="1.8" fill="#ef4444" />
        <rect x="28" y="52.5" width="2" height="1.8" fill="#ef4444" />
        <rect x="34" y="52.5" width="2" height="1.8" fill="#ef4444" />
        <rect x="37" y="52.5" width="2" height="1.8" fill="#ef4444" />
        <rect x="40" y="52.5" width="2" height="1.8" fill="#ef4444" />

        <!-- Quad phares ronds avant -->
        <circle cx="23" cy="13" r="1.6" fill="#fef08a" />
        <circle cx="26.5" cy="13" r="1.6" fill="#fef08a" />
        <circle cx="37.5" cy="13" r="1.6" fill="#fef08a" />
        <circle cx="41" cy="13" r="1.6" fill="#fef08a" />
      </svg>
    `;
  }

  // 3. Monoplace F1 3D (Aérodynamique, roues slicks, aileron biplan 3D, halo et casque)
  if (type === 'car_f1') {
    const headlightCone = showHeadlights
      ? `<polygon points="30,8 10,-32 54,-32 34,8" fill="url(#hlConeF1)" opacity="0.4" />`
      : '';

    return `
      <svg viewBox="0 0 64 64" width="100%" height="100%" style="overflow: visible;">
        <defs>
          <linearGradient id="hlConeF1" x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" stop-color="#fef08a" stop-opacity="0.45" />
            <stop offset="100%" stop-color="#fef08a" stop-opacity="0" />
          </linearGradient>
        </defs>
        ${headlightCone}

        <!-- Ombre de la monoplace -->
        <ellipse cx="32" cy="38" rx="21" ry="18" fill="rgba(0,0,0,0.55)" />

        <!-- Énormes pneus slicks arrière F1 -->
        <rect x="10" y="39" width="9" height="14" rx="2" fill="#18181b" stroke="#000" stroke-width="1" />
        <rect x="45" y="39" width="9" height="14" rx="2" fill="#18181b" stroke="#000" stroke-width="1" />
        <!-- Pneus avant -->
        <rect x="12" y="14" width="8" height="11" rx="2" fill="#18181b" stroke="#000" stroke-width="1" />
        <rect x="44" y="14" width="8" height="11" rx="2" fill="#18181b" stroke="#000" stroke-width="1" />

        <!-- Triangles de suspension en carbone -->
        <line x1="18" y1="18" x2="28" y2="20" stroke="#52525b" stroke-width="2" />
        <line x1="46" y1="18" x2="36" y2="20" stroke="#52525b" stroke-width="2" />
        <line x1="18" y1="46" x2="27" y2="47" stroke="#52525b" stroke-width="2.2" />
        <line x1="46" y1="46" x2="37" y2="47" stroke="#52525b" stroke-width="2.2" />

        <!-- Aileron avant large F1 -->
        <rect x="14" y="10" width="36" height="3.5" rx="1" fill="#09090b" stroke="#27272a" stroke-width="0.8" />

        <!-- Fuselage / Pontons latéraux 3D -->
        <path d="M29 8 L35 8 L37 24 C40 27, 43 31, 43 45 L21 45 C21 31, 24 27, 27 24 Z" fill="${color}" stroke="#000" stroke-width="1.2" />

        <!-- Pontons avec dégradé d'ombre -->
        <path d="M22 33 L27 28 L27 45 L22 45 Z" fill="${darkColor}" />
        <path d="M42 33 L37 28 L37 45 L42 45 Z" fill="${darkColor}" />

        <!-- Cockpit ouvert et arceau Halo titane -->
        <circle cx="32" cy="30" r="3.8" fill="#0f172a" />
        <!-- Casque du pilote -->
        <circle cx="32" cy="30" r="2.4" fill="#facc15" stroke="#000" stroke-width="0.6" />
        <path d="M29 28 C30 26, 34 26, 35 28 L34 34 L30 34 Z" fill="none" stroke="#27272a" stroke-width="1.8" />

        <!-- Boîte à air moteur surélevée (Airbox) -->
        <rect x="30" y="34.5" width="4" height="4.5" rx="1" fill="#09090b" />

        <!-- Aileron de requin (Shark Fin) 3D -->
        <line x1="32" y1="36" x2="32" y2="48" stroke="${lightColor}" stroke-width="1.8" stroke-linecap="round" />

        <!-- Aileron arrière 3D surélevé F1 avec plaques d'extrémité -->
        <rect x="13" y="49" width="38" height="4" rx="1" fill="#09090b" stroke="#3f3f46" stroke-width="1" />
        <rect x="13" y="47" width="2" height="8" rx="0.5" fill="#27272a" />
        <rect x="49" y="47" width="2" height="8" rx="0.5" fill="#27272a" />

        <!-- Feu de pluie F1 rouge clignotant au centre -->
        <rect x="30.5" y="52" width="3" height="2" rx="0.5" fill="#ef4444" />
      </svg>
    `;
  }

  // 4. SUV 4x4 Offroad 3D (Garde au sol haute, barres de toit, roue de secours arrière)
  if (type === 'car_suv') {
    const headlightCone = showHeadlights
      ? `<polygon points="24,10 4,-32 60,-32 40,10" fill="url(#hlConeSuv)" opacity="0.45" />`
      : '';

    return `
      <svg viewBox="0 0 64 64" width="100%" height="100%" style="overflow: visible;">
        <defs>
          <linearGradient id="hlConeSuv" x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" stop-color="#fef08a" stop-opacity="0.5" />
            <stop offset="100%" stop-color="#fef08a" stop-opacity="0" />
          </linearGradient>
        </defs>
        ${headlightCone}

        <!-- Ombre portée surélevée (garde au sol) -->
        <ellipse cx="32" cy="41" rx="21" ry="18" fill="rgba(0,0,0,0.6)" />

        <!-- Pneus tout-terrain crantés larges -->
        <rect x="14" y="16" width="7" height="12" rx="1.5" fill="#18181b" stroke="#000" stroke-width="1" />
        <rect x="43" y="16" width="7" height="12" rx="1.5" fill="#18181b" stroke="#000" stroke-width="1" />
        <rect x="14" y="40" width="7" height="12" rx="1.5" fill="#18181b" stroke="#000" stroke-width="1" />
        <rect x="43" y="40" width="7" height="12" rx="1.5" fill="#18181b" stroke="#000" stroke-width="1" />

        <!-- Pare-chocs avant tout-terrain -->
        <rect x="22" y="9" width="20" height="3" rx="1" fill="#27272a" stroke="#000" stroke-width="1" />

        <!-- Caisse carrée SUV 3D -->
        <path d="M21 11 L43 11 L45 22 L46 52 L18 52 L19 22 Z" fill="${darkColor}" stroke="#000" stroke-width="1.2" />
        <path d="M22 12 L42 12 L43 23 L44 51 L20 51 L21 23 Z" fill="${color}" />

        <!-- Pare-brise -->
        <polygon points="22,22 42,22 41,30 23,30" fill="#0f172a" stroke="#334155" stroke-width="0.8" />

        <!-- Toit surélevé avec galerie de toit (Roof Rack) -->
        <rect x="22" y="30" width="20" height="18" fill="${midColor}" />
        <line x1="23" y1="31" x2="23" y2="47" stroke="#71717a" stroke-width="1.8" />
        <line x1="41" y1="31" x2="41" y2="47" stroke="#71717a" stroke-width="1.8" />
        <line x1="23" y1="36" x2="41" y2="36" stroke="#52525b" stroke-width="1.2" />
        <line x1="23" y1="42" x2="41" y2="42" stroke="#52525b" stroke-width="1.2" />

        <!-- Rampe de spots LED de toit -->
        <rect x="24" y="30" width="16" height="2" rx="0.5" fill="#fef08a" />

        <!-- Roue de secours 3D sur le hayon arrière -->
        <circle cx="32" cy="54" r="5" fill="#18181b" stroke="#3f3f46" stroke-width="1.2" />
        <circle cx="32" cy="54" r="2.2" fill="#52525b" />

        <!-- Feux stop verticaux sur les montants arrière -->
        <rect x="19" y="47" width="2.5" height="4" rx="0.5" fill="#ef4444" />
        <rect x="42.5" y="47" width="2.5" height="4" rx="0.5" fill="#ef4444" />

        <!-- Phares avant -->
        <rect x="23" y="11.5" width="4" height="2.5" rx="0.5" fill="#fef08a" />
        <rect x="37" y="11.5" width="4" height="2.5" rx="0.5" fill="#fef08a" />
      </svg>
    `;
  }

  // 5. Superbike GP 3D (Pneu arrière incliné, pilote en position profilée, casque 3D)
  if (type === 'car_moto') {
    const headlightCone = showHeadlights
      ? `<polygon points="30,11 14,-30 50,-30 34,11" fill="url(#hlConeMoto)" opacity="0.45" />`
      : '';

    return `
      <svg viewBox="0 0 64 64" width="100%" height="100%" style="overflow: visible;">
        <defs>
          <linearGradient id="hlConeMoto" x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" stop-color="#fef08a" stop-opacity="0.5" />
            <stop offset="100%" stop-color="#fef08a" stop-opacity="0" />
          </linearGradient>
        </defs>
        ${headlightCone}

        <!-- Ombre portée sous la moto -->
        <ellipse cx="32" cy="38" rx="10" ry="17" fill="rgba(0,0,0,0.55)" />

        <!-- Gros pneu arrière 3D moto -->
        <rect x="28.5" y="42" width="7" height="13" rx="2.5" fill="#18181b" stroke="#000" stroke-width="1" />
        <!-- Pneu avant -->
        <rect x="29.5" y="10" width="5" height="10" rx="2" fill="#18181b" stroke="#000" stroke-width="0.8" />

        <!-- Double sortie d'échappement sous la selle -->
        <circle cx="28" cy="42" r="1.5" fill="#71717a" stroke="#000" stroke-width="0.5" />
        <circle cx="36" cy="42" r="1.5" fill="#71717a" stroke="#000" stroke-width="0.5" />

        <!-- Carénage de la moto -->
        <path d="M28 16 C28 14, 36 14, 36 16 L38 27 L26 27 Z" fill="${color}" stroke="#000" stroke-width="1.2" />

        <!-- Guidon sport bracelets -->
        <line x1="21" y1="21" x2="43" y2="21" stroke="#3f3f46" stroke-width="2.5" stroke-linecap="round" />

        <!-- Corps du motard en position aérodynamique (3D) -->
        <ellipse cx="32" cy="31" rx="6.5" ry="4.5" fill="#18181b" stroke="#000" stroke-width="0.8" />

        <!-- Casque motard 3D avec reflet de visière -->
        <circle cx="32" cy="27" r="4" fill="${lightColor}" stroke="#000" stroke-width="1" />
        <path d="M29.5" y="25" C31 24, 33 24, 34.5 25 L34.5 26.5 L29.5 26.5 Z" fill="#0f172a" />

        <!-- Coque arrière et feu stop -->
        <path d="M28 35 L36 35 L34 44 L30 44 Z" fill="${color}" stroke="#000" stroke-width="1" />
        <rect x="30" y="44" width="4" height="2" rx="0.5" fill="#ef4444" />
      </svg>
    `;
  }

  // 6. Hovercar Cyberpunk 3D (Halo antigravité, réacteurs à plasma, arêtes néon)
  if (type === 'car_cyber') {
    const headlightCone = showHeadlights
      ? `<polygon points="26,10 4,-32 60,-32 38,10" fill="url(#hlConeCyber)" opacity="0.5" />`
      : '';

    return `
      <svg viewBox="0 0 64 64" width="100%" height="100%" style="overflow: visible;">
        <defs>
          <linearGradient id="hlConeCyber" x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" stop-color="#38bdf8" stop-opacity="0.55" />
            <stop offset="100%" stop-color="#38bdf8" stop-opacity="0" />
          </linearGradient>
          <linearGradient id="thrusterPlasma" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#38bdf8" />
            <stop offset="100%" stop-color="#ec4899" />
          </linearGradient>
        </defs>
        ${headlightCone}

        <!-- Halo d'énergie antigravité au sol -->
        <ellipse cx="32" cy="40" rx="22" ry="17" fill="rgba(6,182,212,0.3)" />

        <!-- Flammes de plasma réacteurs arrière -->
        <polygon points="19,49 25,49 22,62" fill="url(#thrusterPlasma)" opacity="0.9" />
        <polygon points="39,49 45,49 42,62" fill="url(#thrusterPlasma)" opacity="0.9" />

        <!-- Châssis biseauté 3D futuriste -->
        <polygon points="32,7 46,20 48,34 46,50 38,47 32,49 26,47 18,50 16,34 18,20" 
                 fill="#09090b" stroke="${color}" stroke-width="2.2" stroke-linejoin="round" />

        <!-- Panneaux supérieurs facettés 3D -->
        <polygon points="32,10 42,21 40,36 32,38 24,36 22,21" fill="${darkColor}" />
        <polygon points="32,12 39,21 37,34 32,36 27,34 25,21" fill="${color}" />

        <!-- Verrière holographique 3D -->
        <polygon points="32,17 36,24 35,31 29,31 28,24" fill="#06b6d4" opacity="0.9" stroke="#ffffff" stroke-width="1" />

        <!-- Arêtes néon magenta haute visibilité -->
        <line x1="18" y1="22" x2="16" y2="45" stroke="#ec4899" stroke-width="1.8" />
        <line x1="46" y1="22" x2="48" y2="45" stroke="#ec4899" stroke-width="1.8" />

        <!-- Tuyères ioniques -->
        <rect x="18" y="47" width="8" height="3" rx="1" fill="#06b6d4" />
        <rect x="38" y="47" width="8" height="3" rx="1" fill="#06b6d4" />
      </svg>
    `;
  }

  // 7. Flèche GTA V 3D (Biseau 3D gauche/droite et ombre portée)
  if (type === 'arrow_gta') {
    return `
      <svg viewBox="0 0 64 64" width="100%" height="100%" style="overflow: visible;">
        <!-- Ombre portée 3D -->
        <polygon points="34,10 14,58 34,48 54,58" fill="rgba(0,0,0,0.5)" />
        <!-- Moitié gauche ombrée 3D -->
        <polygon points="32,6 12,54 32,44" fill="${darkColor}" stroke="#000" stroke-width="2.5" stroke-linejoin="round" />
        <!-- Moitié droite en pleine lumière -->
        <polygon points="32,6 52,54 32,44" fill="${color}" stroke="#000" stroke-width="2.5" stroke-linejoin="round" />
        <!-- Ligne centrale de biseau -->
        <line x1="32" y1="6" x2="32" y2="44" stroke="${lightColor}" stroke-width="1.5" />
      </svg>
    `;
  }

  // 8. Flèche Waze Aéro 3D
  if (type === 'arrow_waze') {
    return `
      <svg viewBox="0 0 64 64" width="100%" height="100%" style="overflow: visible;">
        <polygon points="34,10 16,56 34,46 52,56" fill="rgba(0,0,0,0.45)" />
        <polygon points="32,6 14,52 32,42" fill="${darkColor}" stroke="#ffffff" stroke-width="2.5" stroke-linejoin="round" />
        <polygon points="32,6 50,52 32,42" fill="${color}" stroke="#ffffff" stroke-width="2.5" stroke-linejoin="round" />
        <polygon points="32,12 24,46 32,40 40,46" fill="#ffffff" opacity="0.4" />
      </svg>
    `;
  }

  // 9. Curseur Pixel 3D
  if (type === 'minecraft_arrow') {
    return `
      <div class="w-14 h-14 flex items-center justify-center relative">
        <img src="/textures/minecraft_cursor.png" class="w-12 h-12 object-contain absolute translate-x-1 translate-y-1 opacity-50 brightness-0" style="image-rendering: pixelated;" />
        <img src="/textures/minecraft_cursor.png" class="w-12 h-12 object-contain relative" style="image-rendering: pixelated;" />
      </div>
    `;
  }

  return `
    <svg viewBox="0 0 64 64" width="100%" height="100%">
      <polygon points="32,6 12,56 32,46 52,56" fill="${color}" stroke="#000" stroke-width="3" />
    </svg>
  `;
}

/**
 * Génère le code HTML complet inséré dans le Marker MapLibre.
 * Possède obligatoirement id="player-arrow" pour la rotation automatique.
 */
export function getVehicleMarkerHtml(
  type: VehicleType,
  color: string,
  showHeadlights = true
): string {
  const config = VEHICLE_CONFIGS.find((v) => v.id === type) || VEHICLE_CONFIGS[0];
  const sizePx = config.size;

  const svgContent = getVehicleSvgString(type, color, showHeadlights);

  return `
    <div id="player-arrow" style="width: ${sizePx}px; height: ${sizePx}px;" class="flex items-center justify-center transition-transform duration-100 ease-linear pointer-events-none">
      ${svgContent}
    </div>
  `;
}
