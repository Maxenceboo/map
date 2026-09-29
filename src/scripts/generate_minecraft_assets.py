from PIL import Image, ImageDraw
import os

os.makedirs('public/textures', exist_ok=True)

# 1. Texture Herbe Minecraft (16x16 pixels répétée)
# Palette officielle Grass Block Top Minecraft
grass_palette = [
    (91, 135, 49),   # #5b8731 base
    (76, 120, 40),   # #4c7828 sombre
    (82, 125, 44),   # #527d2c moyen
    (94, 141, 53),   # #5e8d35 clair
    (71, 111, 37),   # #476f25 ombre
]
# Matrice 16x16 du bloc d'herbe
grass_pattern = [
    [0, 1, 2, 0, 3, 2, 0, 1, 0, 2, 3, 0, 1, 2, 0, 3],
    [2, 0, 4, 1, 0, 2, 4, 0, 3, 1, 0, 2, 4, 0, 1, 2],
    [3, 2, 0, 3, 2, 0, 1, 3, 0, 4, 2, 0, 3, 1, 0, 4],
    [1, 0, 3, 2, 4, 1, 0, 2, 1, 0, 3, 4, 2, 0, 2, 1],
    [0, 4, 1, 0, 2, 3, 2, 0, 4, 2, 0, 1, 0, 3, 4, 0],
    [2, 3, 0, 2, 1, 0, 4, 3, 2, 0, 3, 2, 1, 0, 2, 3],
    [1, 0, 4, 3, 0, 2, 1, 0, 1, 4, 0, 1, 3, 4, 0, 1],
    [0, 2, 1, 0, 4, 1, 3, 2, 0, 2, 3, 0, 2, 1, 3, 0],
    [3, 1, 0, 3, 2, 0, 4, 1, 3, 0, 1, 4, 0, 2, 0, 2],
    [2, 0, 2, 1, 0, 4, 2, 0, 2, 4, 0, 2, 3, 1, 4, 0],
    [0, 3, 4, 0, 3, 1, 0, 3, 1, 0, 3, 1, 0, 4, 2, 1],
    [1, 2, 0, 2, 1, 0, 2, 4, 0, 2, 4, 0, 2, 1, 0, 3],
    [4, 0, 3, 1, 0, 3, 1, 0, 3, 1, 0, 3, 1, 0, 3, 2],
    [2, 1, 0, 4, 2, 0, 4, 2, 0, 4, 2, 0, 4, 2, 0, 4],
    [0, 3, 2, 0, 3, 1, 0, 3, 1, 0, 3, 1, 0, 3, 1, 0],
    [1, 0, 1, 3, 0, 4, 2, 1, 0, 2, 1, 0, 2, 0, 4, 2],
]

img_grass = Image.new('RGB', (16, 16))
for y in range(16):
    for x in range(16):
        img_grass.putpixel((x, y), grass_palette[grass_pattern[y][x]])
img_grass = img_grass.resize((64, 64), Image.NEAREST)
img_grass.save('public/textures/minecraft_grass.png')

# 2. Texture Eau Minecraft (16x16 pixels bleus animés)
water_palette = [
    (41, 85, 196),   # #2955c4 base
    (51, 94, 208),   # #335ed0 moyen
    (60, 102, 217),  # #3c66d9 clair
    (66, 108, 224),  # #426ce0 reflet
    (35, 75, 181),   # #234bb5 ombre
]
img_water = Image.new('RGB', (16, 16))
for y in range(16):
    for x in range(16):
        idx = (x * 3 + y * 5 + ((x ^ y) % 3)) % len(water_palette)
        img_water.putpixel((x, y), water_palette[idx])
img_water = img_water.resize((64, 64), Image.NEAREST)
img_water.save('public/textures/minecraft_water.png')

# 3. Sprite Cochon Minecraft (16x16 transparent comme sur la photo près de l'eau !)
img_pig = Image.new('RGBA', (24, 24), (0, 0, 0, 0))
draw_pig = ImageDraw.Draw(img_pig)
# Corps rose
draw_pig.rectangle([4, 6, 20, 16], fill=(240, 142, 142), outline=(0, 0, 0))
# Tête
draw_pig.rectangle([14, 4, 22, 12], fill=(244, 164, 164), outline=(0, 0, 0))
# Groin
draw_pig.rectangle([18, 7, 22, 10], fill=(219, 110, 110), outline=(0, 0, 0))
# Yeux
draw_pig.point([17, 6], fill=(0, 0, 0))
draw_pig.point([16, 6], fill=(255, 255, 255))
# Pattes
draw_pig.rectangle([6, 16, 9, 20], fill=(219, 110, 110), outline=(0, 0, 0))
draw_pig.rectangle([15, 16, 18, 20], fill=(219, 110, 110), outline=(0, 0, 0))
img_pig.save('public/textures/minecraft_pig.png')

# 4. Sprite Arbre Chêne Minecraft (24x32)
img_tree = Image.new('RGBA', (32, 40), (0, 0, 0, 0))
draw_tree = ImageDraw.Draw(img_tree)
# Tronc bois
draw_tree.rectangle([13, 22, 19, 38], fill=(103, 77, 46), outline=(45, 33, 17))
# Feuillage bloc 1
draw_tree.rectangle([4, 10, 28, 24], fill=(59, 102, 34), outline=(32, 59, 17))
# Feuillage bloc 2
draw_tree.rectangle([8, 2, 24, 14], fill=(74, 128, 43), outline=(32, 59, 17))
img_tree.save('public/textures/minecraft_tree.png')

# 5. Curseur Joueur Pixel Minecraft (Exactement celui de la photo !)
# Flèche blanche avec contour noir pixelisé
img_cursor = Image.new('RGBA', (48, 48), (0, 0, 0, 0))
draw_cur = ImageDraw.Draw(img_cursor)
# Polygon blanc pixelisé
pts = [(24, 4), (8, 42), (24, 34), (40, 42)]
draw_cur.polygon(pts, fill=(255, 255, 255), outline=(0, 0, 0))
# Contour plus épais noir 2px
draw_cur.line([(24, 4), (8, 42)], fill=(0, 0, 0), width=3)
draw_cur.line([(8, 42), (24, 34)], fill=(0, 0, 0), width=3)
draw_cur.line([(24, 34), (40, 42)], fill=(0, 0, 0), width=3)
draw_cur.line([(40, 42), (24, 4)], fill=(0, 0, 0), width=3)
img_cursor.save('public/textures/minecraft_cursor.png')

print('Textures Minecraft générées avec succès dans public/textures/')
