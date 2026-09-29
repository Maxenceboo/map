import urllib.request
import json
import os

req = urllib.request.Request('https://tiles.openfreemap.org/styles/dark', headers={'User-Agent': 'Mozilla/5.0'})
base = json.loads(urllib.request.urlopen(req).read())

# Classes complètes pour qu'AUCUNE route / bretelle / rond-point ne disparaisse
motorway_classes = ['motorway', 'motorway_link', 'trunk', 'trunk_link']
major_classes = ['primary', 'primary_link', 'secondary', 'secondary_link', 'tertiary', 'tertiary_link']
minor_classes = ['minor', 'street', 'residential', 'living_street', 'service', 'track', 'unclassified', 'road']

def clean_layers(layers, theme):
    out = []
    for l in layers:
        lid = l.get('id', '')
        ltype = l.get('type', '')
        
        # Conserver les labels de rues principales en mode Minecraft
        if ltype == 'symbol':
            if theme == 'minecraft' and ('highway_name' in lid or 'place_town' in lid or 'place_suburb' in lid):
                l_copy = json.loads(json.dumps(l))
                paint = l_copy.get('paint', {})
                layout = l_copy.get('layout', {})
                paint['text-color'] = '#ffffff'
                paint['text-halo-color'] = '#000000'
                paint['text-halo-width'] = 2.5
                layout['text-transform'] = 'uppercase'
                layout['text-size'] = 11
                out.append(l_copy)
            continue

        if 'rail' in lid or 'bound' in lid:
            continue
            
        l_copy = json.loads(json.dumps(l))
        paint = l_copy.get('paint', {})

        if 'fill-pattern' in paint:
            del paint['fill-pattern']
        
        # Mettre à jour les filtres des routes pour inclure TOUTES les bretelles, échangeurs et rues
        if 'highway_motorway' in lid:
            l_copy['filter'] = ['all', ['match', ['geometry-type'], ['LineString', 'MultiLineString'], True, False], ['match', ['get', 'class'], motorway_classes, True, False]]
            if 'minzoom' in l_copy: l_copy['minzoom'] = 4
        elif 'highway_major' in lid:
            l_copy['filter'] = ['all', ['match', ['geometry-type'], ['LineString', 'MultiLineString'], True, False], ['match', ['get', 'class'], major_classes, True, False]]
            if 'minzoom' in l_copy and l_copy['minzoom'] > 8: l_copy['minzoom'] = 8
        elif 'highway_minor' in lid:
            l_copy['filter'] = ['all', ['match', ['geometry-type'], ['LineString', 'MultiLineString'], True, False], ['match', ['get', 'class'], minor_classes, True, False]]
            if 'minzoom' in l_copy and l_copy['minzoom'] > 10: l_copy['minzoom'] = 10

        if theme == 'gta':
            if lid == 'background':
                paint['background-color'] = '#10131a'
            elif 'water' in lid:
                if 'fill-color' in paint: paint['fill-color'] = '#121b27'
                if 'line-color' in paint: paint['line-color'] = '#1d2d40'
            elif 'landcover_wood' in lid or 'park' in lid:
                paint['fill-color'] = '#141e1a'
            elif 'residential' in lid:
                if 'fill-color' in paint: paint['fill-color'] = '#151922'
            elif 'building' in lid:
                if 'fill-color' in paint: paint['fill-color'] = '#1f2430'
            elif 'highway_minor' in lid:
                if 'line-color' in paint: paint['line-color'] = '#2a3242'
            elif 'highway_major_casing' in lid:
                if 'line-color' in paint: paint['line-color'] = '#1a202c'
            elif 'highway_major_inner' in lid:
                if 'line-color' in paint: paint['line-color'] = '#64748b'
            elif 'highway_motorway_casing' in lid:
                if 'line-color' in paint: paint['line-color'] = '#451a03'
            elif 'highway_motorway_inner' in lid:
                if 'line-color' in paint: paint['line-color'] = '#f59e0b'
                
        elif theme == 'minecraft':
            if lid == 'background':
                paint['background-color'] = '#528330'
                paint['background-pattern'] = 'minecraft-grass'
            elif 'water' in lid:
                if 'fill-color' in paint: 
                    paint['fill-color'] = '#3c63cc'
                    paint['fill-pattern'] = 'minecraft-water'
                if 'line-color' in paint: 
                    paint['line-color'] = '#2c4ea8'
            elif 'landcover_wood' in lid or 'park' in lid:
                paint['fill-color'] = '#385e1e'
            elif 'residential' in lid:
                paint['fill-color'] = '#528330'
                paint['fill-pattern'] = 'minecraft-grass'
            elif 'building' in lid:
                if 'fill-color' in paint: paint['fill-color'] = '#71717a'
            elif 'highway_minor' in lid:
                # Routes grises pixelisées continues
                if 'line-color' in paint: paint['line-color'] = '#6b7280'
                paint['line-width'] = ['interpolate', ['linear'], ['zoom'], 10, 2.5, 16, 7]
            elif 'highway_major_casing' in lid:
                if 'line-color' in paint: paint['line-color'] = '#374151'
            elif 'highway_major_inner' in lid:
                if 'line-color' in paint: paint['line-color'] = '#9ca3af'
                paint['line-width'] = ['interpolate', ['linear'], ['zoom'], 8, 3, 16, 8]
            elif 'highway_motorway_casing' in lid:
                if 'line-color' in paint: paint['line-color'] = '#1f2937'
            elif 'highway_motorway_inner' in lid:
                if 'line-color' in paint: paint['line-color'] = '#d1d5db'
                paint['line-width'] = ['interpolate', ['linear'], ['zoom'], 6, 3.5, 16, 9]

        elif theme == 'waze':
            if lid == 'background':
                paint['background-color'] = '#181b22'
            elif 'water' in lid:
                if 'fill-color' in paint: paint['fill-color'] = '#0f243a'
            elif 'landcover_wood' in lid or 'park' in lid:
                paint['fill-color'] = '#16281e'
            elif 'residential' in lid:
                if 'fill-color' in paint: paint['fill-color'] = '#1c202a'
            elif 'building' in lid:
                if 'fill-color' in paint: paint['fill-color'] = '#252a37'
            elif 'highway_minor' in lid:
                if 'line-color' in paint: paint['line-color'] = '#333b4d'
            elif 'highway_major_inner' in lid:
                if 'line-color' in paint: paint['line-color'] = '#475569'
            elif 'highway_motorway_inner' in lid:
                if 'line-color' in paint: paint['line-color'] = '#0284c7'
                
        out.append(l_copy)
    return out

os.makedirs('src/styles/generated', exist_ok=True)
for th in ['gta', 'minecraft', 'waze']:
    s = json.loads(json.dumps(base))
    s['layers'] = clean_layers(base['layers'], th)
    with open(f'src/styles/generated/{th}.json', 'w', encoding='utf-8') as f:
        json.dump(s, f, indent=2)
    print(f"Generated {th}.json with complete continuous road network")
