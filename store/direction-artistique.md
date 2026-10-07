# Direction artistique — Vektor GPS

## L'idée

Un GPS qui ressemble à l'écran d'un jeu vidéo : une carte de nuit, un itinéraire lumineux,
et **la flèche jaune du joueur**. Le logo reprend exactement ce qu'on voit en conduisant.

## Logo

- **Symbole** : la flèche jaune en relief (une moitié éclairée, une moitié dans l'ombre),
  au bout d'un itinéraire violet, sur fond bleu nuit traversé de quelques routes.
- **Fichiers** :
  - `icone-play-store-512.png` — icône de la fiche Play Store (512 × 512, carrée : Google arrondit lui-même).
  - `banniere-1024x500.png` — image de présentation en haut de la fiche.
  - Icône de l'app : `app/src/main/res/drawable/ic_launcher_*.xml` (adaptative, avec version une couleur).
- **À respecter** : garder la flèche jaune sur fond sombre ; ne pas la déformer ni la tourner ;
  laisser autour d'elle une marge d'au moins un quart de sa largeur.

## Couleurs

| Rôle | Couleur | Code |
| :--- | :--- | :--- |
| Accent (flèche, boutons, valeurs) | Jaune objectif | `#FFC533` |
| Ombre de l'accent | Jaune foncé | `#DB9E14` |
| Itinéraire | Violet | `#C084FC` |
| Liseré de l'itinéraire | Violet nuit | `#3B0764` |
| Fond | Noir bleuté | `#0D1014` |
| Surfaces | Bleu nuit | `#1F2630` |
| Surfaces relevées | Ardoise | `#2E3744` |
| Texte | Blanc cassé | `#F4F6F8` |
| Texte secondaire | Gris bleu | `#98A2AE` |
| Danger (excès, radar, arrêter) | Rouge | `#E5484D` |
| Avertissement (ralentissement, estimation) | Orange | `#FF9F1C` |

Un seul accent : le jaune. Le violet est réservé à l'itinéraire, le rouge au danger.

## Typographie

- **Dans l'app** : la police du système (Roboto sur Android), en gras pour les valeurs,
  avec des chiffres de largeur fixe pour la vitesse et les distances.
- **Sur les visuels de la fiche** : Bahnschrift gras, qui rappelle la signalisation routière.
- Pas de majuscules partout : une majuscule en début de phrase suffit.

## Formes

- Cartes arrondies (rayon 20), boutons arrondis (rayon 16), pastilles et boutons ronds.
- Fonds opaques avec une ombre, pas de bordures fines ni de flou.
- Icônes vectorielles pleines, jamais d'émojis.
- Véhicules en volumes simples, façon low-poly.

## Ton

- Tutoiement, phrases courtes, vocabulaire du jeu utilisé avec parcimonie (« mission », « objectif »).
- On dit ce que fait l'app, sans superlatifs.

## Captures d'écran

Format 1080 × 1920, un titre court au-dessus de chaque écran, même fond que la bannière.
Ordre conseillé : guidage, carte, aperçu du trajet, recherche, véhicules, thèmes, éditeur de véhicule
(`capture-1-guidage.png` à `capture-7-editeur-vehicule.png`).

Pour en refaire : Paramètres > À propos > « Position de démonstration » place le véhicule
sur les Champs-Élysées, ce qui évite de montrer où l'on se trouve.
