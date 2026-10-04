"""
Convertit la base des radars français de la branche webgl-version en ressource Android.

Usage (depuis la racine du dépôt) :
    git show webgl-version:src/data/radarsFrance.json > /tmp/radarsFrance.json
    python tools/convert_radars.py /tmp/radarsFrance.json app/src/main/assets/radars_france.json

- Répare les accents encodés deux fois en UTF-8 ("ValliÃ¨res" -> "Vallières").
- Garde le format compact : id, t (type), c ([lng, lat]), s (vitesse), r (route), p (lieu), d (sens).
"""
import json
import sys


def fix_text(value):
    if not isinstance(value, str) or "Ã" not in value:
        return value
    try:
        return value.encode("latin-1").decode("utf-8")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return value


def main(source, target):
    with open(source, encoding="utf-8") as f:
        radars = json.load(f)
    for radar in radars:
        for key in ("r", "p", "d"):
            if key in radar:
                radar[key] = fix_text(radar[key])
    with open(target, "w", encoding="utf-8") as f:
        json.dump(radars, f, ensure_ascii=False, separators=(",", ":"))
    remaining = sum(1 for r in radars for k in ("r", "p", "d") if "Ã" in str(r.get(k, "")))
    print(f"{len(radars)} radars écrits dans {target} ({remaining} textes encore suspects)")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
