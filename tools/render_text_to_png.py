from PIL import Image, ImageDraw, ImageFont
import sys

if len(sys.argv) < 3:
    print('Usage: render_text_to_png.py <textfile> <output.png>')
    sys.exit(2)

textfile = sys.argv[1]
outfile = sys.argv[2]

with open(textfile, 'r', encoding='utf-8', errors='replace') as f:
    text = f.read()

# choose a monospaced font if available
try:
    font = ImageFont.truetype('Consola.ttf', 14)
except Exception:
    try:
        font = ImageFont.truetype('Courier New.ttf', 14)
    except Exception:
        font = ImageFont.load_default()

lines = text.splitlines()
# use getbbox for newer PIL font API
line_heights = [font.getbbox('Ay')[3] - font.getbbox('Ay')[1]]
lineh = line_heights[0] if line_heights else 14
maxw = 400
for line in lines:
    try:
        bbox = font.getbbox(line)
        w = bbox[2] - bbox[0]
        if w > maxw: maxw = w
    except Exception:
        pass
imgw = int(maxw) + 20
imgh = lineh * (len(lines) + 1) + 20
if imgh > 4000:
    imgh = 4000
im = Image.new('RGB', (imgw, imgh), color=(255,255,255))
draw = ImageDraw.Draw(im)

y = 10
for line in lines:
    draw.text((10, y), line, fill=(0,0,0), font=font)
    y += lineh

im.save(outfile)
print('Wrote', outfile)
