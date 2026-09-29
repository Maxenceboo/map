import urllib.request
import urllib.parse
import json
import os

# Gironde bounding box
# S: 44.20, W: -1.25, N: 45.55, E: 0.30
query = """
[out:json][timeout:60];
(
  way["highway"~"motorway|trunk|primary|secondary|tertiary"](44.20,-1.25,45.55,0.30);
);
out geom;
"""

url = 'https://overpass-api.de/api/interpreter'
data = urllib.parse.urlencode({'data': query}).encode('utf-8')
print('Téléchargement du réseau routier de la Gironde (autoroutes, départementales, voies principales)...')

try:
    req = urllib.request.Request(url, data=data, headers={'User-Agent': 'GameMapsIRL/1.0'})
    res = urllib.request.urlopen(req, timeout=70)
    osm_data = json.loads(res.read())
    elements = osm_data.get('elements', [])
    print(f'Tronçons trouvés : {len(elements)}')

    features = []
    for el in elements:
        geom = el.get('geometry', [])
        if len(geom) >= 2:
            coords = [[pt['lon'], pt['lat']] for pt in geom]
            tags = el.get('tags', {})
            features.append({
                'type': 'Feature',
                'properties': {
                    'name': tags.get('name', ''),
                    'ref': tags.get('ref', ''),
                    'highway': tags.get('highway', 'road'),
                },
                'geometry': {
                    'type': 'LineString',
                    'coordinates': coords,
                }
            })

    geojson = {
        'type': 'FeatureCollection',
        'features': features
    }

    os.makedirs('public/data', exist_ok=True)
    out_path = 'public/data/gironde_roads.geojson'
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(geojson, f)

    size_mb = os.path.getsize(out_path) / (1024 * 1024)
    print(f'GeoJSON Gironde sauvegardé dans {out_path} ({size_mb:.2f} MB, {len(features)} routes)')
except Exception as e:
    print('Erreur Overpass:', e)
