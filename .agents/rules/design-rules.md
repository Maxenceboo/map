---
description: "User design rules: flat minimalist HUD, no rounded cards, clean navigation lists"
globs: ["src/components/**/*.tsx", "src/**/*.css"]
always_on: true
---

# UI Design Rules

- **No Rounded Cards**: Avoid bubble/card aesthetics (`rounded-2xl`, `rounded-3xl`, large dropshadows). The user specifically requested a clean, sober, flat UI inspired by high-end gaming HUDs (GTA V, Cyberpunk).
- **Dark & Flat Theme**: Use `bg-neutral-900`, `bg-neutral-950`, `bg-black` with subtle dividers (`border-neutral-800`).
- **Hierarchy & Sub-menus**: Use dedicated full sub-views with `< Précédent` header and bottom action button instead of expanding cards.
- **Search Bar**: Keep the search input wide, comfortable, and clutter-free.
- **Status Dots**: Discrete indicators (e.g. green GPS dot, mute icon) placed on the top right.
