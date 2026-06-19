---
name: Silver Guardian / 银发守护者
description: A quiet, elder-friendly Android companion for health, safety, and family connection.
colors:
  jade-primary: "#2F8F6B"
  jade-deep: "#176B50"
  jade-soft: "#EDF3EC"
  paper-page: "#FBFBFA"
  surface: "#FFFFFF"
  editorial-ink: "#202622"
  secondary-ink: "#6F7772"
  divider: "#EAEAEA"
  reminder: "#956400"
  reminder-soft: "#FBF3DB"
  danger: "#B42318"
  danger-soft: "#FDEBEC"
typography:
  display:
    fontFamily: "sans-serif"
    fontSize: "32sp"
    fontWeight: 700
  headline:
    fontFamily: "sans-serif"
    fontSize: "26sp"
    fontWeight: 700
  title:
    fontFamily: "sans-serif"
    fontSize: "21sp"
    fontWeight: 700
  body:
    fontFamily: "sans-serif"
    fontSize: "17sp"
    fontWeight: 400
  label:
    fontFamily: "sans-serif"
    fontSize: "14sp"
    fontWeight: 600
rounded:
  grouped-surface: "12dp"
  button: "12dp"
  chip: "12dp"
  bottom-navigation: "24dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "12dp"
  section: "16dp"
  screen: "18dp"
  lg: "24dp"
components:
  button-primary:
    backgroundColor: "{colors.jade-primary}"
    textColor: "{colors.surface}"
    typography: "{typography.body}"
    rounded: "{rounded.button}"
    height: "56dp"
  grouped-surface:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.editorial-ink}"
    border: "1dp {colors.divider}"
    rounded: "{rounded.grouped-surface}"
    padding: "18dp"
---

# Design System: Silver Guardian / 银发守护者

Silver Guardian now uses a quiet editorial product language: paper-like backgrounds, crisp white surfaces, strong readable type, and sparse jade accents. The interface should feel like a well-kept health notebook rather than a colorful dashboard.

The brand keeps jade as its identity color, but color is used sparingly. Most hierarchy comes from typography, spacing, dividers, and clear grouping. Shadows are nearly absent; ordinary surfaces use a 1dp divider border.

Core rules:

- Use Android system sans-serif for reliable Chinese rendering.
- Keep body text at 17sp or larger.
- Use 12dp card/button/chip radius for a restrained document feel.
- Use 24dp only for the floating bottom navigation shell.
- Keep the 5 bottom tabs and existing app flow unchanged.
- Avoid gradients, heavy shadows, nested cards, emoji, decorative color blocks, and purely color-based status.
