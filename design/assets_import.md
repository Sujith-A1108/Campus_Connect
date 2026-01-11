Icon import and density guide

1. Preferred source: provide icons as vector SVG (recommended) or high-resolution PNG (>= 1024px).
2. For SVGs: Use Android Studio's "New > Vector Asset" to import; this generates XML drawables usable with tinting.
3. For PNGs: use ImageMagick to generate density versions:
   - mdpi (baseline): convert input.png -resize 48x48 mdpi/icon.png
   - hdpi: 1.5x mdpi
   - xhdpi: 2x mdpi
   - xxhdpi: 3x mdpi
   - xxxhdpi: 4x mdpi
4. Place density images under `app/src/main/res/drawable-mdpi/`, `drawable-hdpi/`, etc.
5. Update `design/assets_manifest.json` with a logical key and drawable path.
6. Use `android:tint` on ImageView or `app:itemIconTint` on BottomNavigationView to apply brand colors.

Tip: When producing icons from Figma, export as SVG and keep an `icons/` folder in the repo root for source files.