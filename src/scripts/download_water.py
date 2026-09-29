import urllib.request
import urllib.parse
import json
import os

# Gironde water bodies: ocean coastline, estuary, garonne, dordogne, lakes
query = """
[out:json][timeout:60];
(
  way["natural"="water"](44.20,-1.25,45.55,0.30);
  way["waterway"~"river|canal"](44.20,-1.25,45.55,0.30);
);
out geom;
"""

url = 'https://overpass-api.de/api/interpreter'
data = urllib.parse.urlencode({'data': query}).encode('utf-8')
print('Téléchargement des cours d’eau de la Gironde (Garonne, Estuaire, Bassin d’Arcachon, Lacs)...')

try:
    req = urllib.request.Request(url, data=data, headers={'User-Agent': 'GameMapsIRL/1.0'})
    res = urllib.request.urlopen(req, timeout=70)
    osm_data = json.loads(res.read())
    elements = osm_data.get('elements', [])
    print(f'Plans et cours d’eau trouvés : {len(elements)}')

    features = []
    for el in elements:
        geom = el.get('geometry', [])
        if len(geom) >= 2:
            coords = [[pt['lon'], pt['lat']] for pt in geom]
            features.append({
                'type': 'Feature',
                'properties': {},
                'geometry': {
                    'type': 'Polygon' if (coords[0] == coords[-1] and len(coords) >= 4) else 'LineString',
                    'coordinates': [coords] if (coords[0] == coords[-1] and len(coords) >= 4) else coords,
                }
            })

    geojson = {
        'type': 'FeatureCollection',
        'features': features
    }

    out_path = 'public/data/gironde_water.geojson'
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(geojson, f)

    size_mb = os.path.getsize(out_path) / (1024 * 1024)
    print(f'GeoJSON Eau Gironde sauvegardé dans {out_path} ({size_mb:.2f} MB, {len(features)} éléments)')
except Exception as e:
    print('Erreur Overpass eau:', e)
