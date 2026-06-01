# UI Design Tokens

This document is the single source of truth for the Axono ReWire UI
vocabulary. Every colour, spacing value, font size, and shared style
class used by the app is listed here. When you add a new view, reach for
these first — and only invent a new token when nothing here fits.

The three layers, in order of preference:

| Layer | Where | Use for |
|---|---|---|
| 1. Shared CSS classes | `src/main/resources/styles.css` + `UIStyles` constants | Anything that has hover / focus / pressed states, or appears in 3+ views (buttons, cards, badges, nav, form fields). |
| 2. Design tokens (Java constants) | `UITheme` (colours, shared snippets), `UIConstants` (spacing / sizes / fonts) | Anywhere you need to compose an inline style or layout value. Always reference the constant rather than hardcoding. |
| 3. One-off inline `-fx-...` strings | Inside the view that needs them | True one-offs — text label tweaks, view-specific accents. Avoid when one of the above already covers the case. |

---

## Colour tokens (`com.axono.ui.UITheme`)

| Constant | Hex | Purpose |
|---|---|---|
| `PRIMARY` | `#59BE8B` | Brand green. Primary actions, focus rings, resource badges, recommended-topic accents. |
| `SECONDARY` | `#207282` | Teal accent. Quiz badges, secondary headings, the dashboard topic colour. |
| `TERTIARY` | `#399386` | Used sparingly for tertiary accents. |
| `BG` | `#F4F6F9` | App background colour. Every scrollable surface. |
| `WHITE` | `#FFFFFF` | Card / nav-bar surfaces. |
| `TEXT_DARK` | `#212529` | Primary text. |
| `TEXT_MUTED` | `#6C757D` | Secondary text — captions, subtitles, hints. |
| `BORDER` | `#DEE2E6` | All borders + dividers. |
| `SECONDARY_OPTION` | `#cccccc` | Legacy. Avoid in new code. |

The CSS file mirrors the same hex values; if you change a token here,
update `styles.css` to match.

---

## Spacing scale (`UIConstants.SPACING_*`)

A consistent vertical / horizontal rhythm. Pick the closest token rather
than introducing a new pixel value.

| Token | px | Typical use |
|---|---|---|
| `SPACING_XS` | 6 | Checkbox ↔ label, icon ↔ label |
| `SPACING_SM` | 8 | Nav-item gaps, step-indicator circles |
| `SPACING_MD` | 10 | Button rows, entry rows, summary row margins |
| `SPACING_LG` | 12 | Compact banners, module card lists, progress rows |
| `SPACING_XL` | 16 | Section heading ↔ card gaps |
| `SPACING_2XL` | 20 | Banner VBox, large section gaps, subject lists |
| `SPACING_3XL` | 40 | Top-level section spacing in scroll-view content VBoxes |

Special-case spacers (`WELCOME_CARD_SPACING`, `FORM_ROW_SPACING`) exist
when a unique value is required.

## Padding scale (`UIConstants.PADDING_*`)

| Token | px | Typical use |
|---|---|---|
| `PADDING_SM` | 12 | Nav bar vertical padding, button-bar padding |
| `PADDING_MD` | 20 | Card padding, scroll-view side padding |
| `PADDING_LG` | 24 | Nav bar horizontal padding |
| `VIEW_PADDING` | 30 | Full-screen StackPane views |
| `CONTENT_PADDING_V` | 60 | Top/bottom padding of scrollable content VBoxes |

---

## Width tokens (`UIConstants`)

| Token | px | Use |
|---|---|---|
| `WINDOW_WIDTH` | 960 | Default stage width |
| `WINDOW_HEIGHT` | 800 | Default stage height |
| `CONTENT_MAX_WIDTH` | 800 | Max width of the central scroll column |
| `SECTION_MAX_WIDTH` | 700 | Max width of section cards |
| `SUBJECT_MAX_WIDTH` | 620 | Onboarding subject picker |
| `SUMMARY_CARD_MAX_WIDTH` | 520 | Onboarding summary card |
| `FORM_MAX_WIDTH` | 500 | Login / sign-up form |
| `WELCOME_CARD_MAX_WIDTH` | 460 | Welcome card |
| `FIELD_PREF_WIDTH` | 400 | Text inputs |
| `FIELD_PREF_HEIGHT` | 36 | Text inputs, wizard buttons |

When introducing a new full-width container, prefer one of these widths
over a new constant so the layout stays in proportion.

---

## Font sizes (`UIConstants.FONT_*`)

| Token | px | Use |
|---|---|---|
| `FONT_XS` | 12 | Hint text, tiny metadata |
| `FONT_SM` | 13 | Muted sub-labels, summary keys |
| `FONT_BODY` | 14 | Body text, button labels |
| `FONT_MD` | 15 | Card row labels |
| `FONT_LG` | 16 | Topic buttons, module names |
| `FONT_NAV` | 18 | Nav bar logo, view subtitles |
| `FONT_SECTION` | 20 | Card headings |
| `FONT_SUBSECTION` | 22 | Dashboard sub-sections |
| `FONT_CARD_HEADING` | 24 | "Score" / "Summary" headings |
| `FONT_PAGE_TITLE` | 28 | Page titles |
| `FONT_BANNER` | 32 | Main view banners |
| `FONT_ICON` | 40 | Large emoji / icon labels |

Always set sizes in pixels using these tokens. `-fx-font-size` values
elsewhere in the codebase are being migrated to use them.

---

## Shared CSS classes (`com.axono.ui.UIStyles`)

The stylesheet at `src/main/resources/styles.css` defines hover, focus,
and pressed states declaratively. Apply by adding to `getStyleClass()`:

```java
Button open = new Button("Open");
open.getStyleClass().add(UIStyles.CLASS_BTN_PRIMARY);
```

| Constant | Class | Purpose |
|---|---|---|
| `CLASS_CARD` | `.card` | Standard white card with rounded border + 20 px padding |
| `CLASS_BTN_PRIMARY` | `.btn-primary` | Brand-green primary action button (with `:hover`, `:armed`, `:focused`) |
| `CLASS_BTN_OUTLINE` | `.btn-outline` | Outline button that fills on hover |
| `CLASS_NAV_BTN` | `.nav-btn` | Top-bar nav button — base state |
| `CLASS_NAV_BTN_ACTIVE` | `.nav-btn-active` | Modifier added by `setActive` |
| `CLASS_BADGE` | `.badge` | Pill-shaped category tag (base) |
| `CLASS_BADGE_QUIZ` | `.badge-quiz` | Teal modifier — quiz items |
| `CLASS_BADGE_RESOURCE` | `.badge-resource` | Green modifier — resource items |

`UIStyles.apply(scene)` attaches the stylesheet to a `Scene`. Every
Scene the app creates does this on construction so the rules are always
in effect.

---

## Iconography (Ikonli FontAwesome 5)

Replace emoji icons with FontIcon nodes. The FA5-Solid pack is on the
classpath via `ikonli-fontawesome5-pack`.

```java
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

FontIcon icon = new FontIcon(FontAwesomeSolid.BOOK_OPEN);
icon.setIconSize(20);
icon.setIconColor(Color.web(UITheme.SECONDARY));
```

Conventions:

- 18 px for inline row icons.
- 24 px for section / topic icons.
- 40 px+ only for hero / banner illustrations.
- Colour with `UITheme.PRIMARY` / `SECONDARY` / `TEXT_MUTED` — do not
  introduce new icon palettes.

---

## Adding a new token

If you really do need a new colour, spacing, or style class:

1. **Justify it.** Skim this doc and `UITheme` / `UIConstants` first.
2. Add the constant to the right home (Java constant or CSS class).
3. Document it here with the same row format.
4. Apply it to all existing places it replaces — don't leave dual code
   paths.
