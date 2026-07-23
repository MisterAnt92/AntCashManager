# Google Play Store Listing - Multi-Language Guide

**For**: AntCashManager v1.4.6 (Open Source)

---

## 📁 Structure

```
store-assets/
├── en/
│   └── STORE_LISTING.md          # English listing
├── it/
│   └── STORE_LISTING.md          # Italian (Italiano)
├── de/
│   └── STORE_LISTING.md          # German (Deutsch)
├── fr/
│   └── STORE_LISTING.md          # French (Français)
├── es/
│   └── STORE_LISTING.md          # Spanish (Español)
├── STORE_LISTING_GUIDE.md         # This file
├── feature-graphics/
│   └── feature-graphic.svg        # Feature graphic (1024x500 px)
├── promo-graphics/
│   └── promo-180x120.svg          # Promo graphic (180x120 px)
├── screenshots/
│   └── en/
│       ├── 1_home_screen.png
│       ├── 2_transactions.png
│       ├── 3_budgets.png
│       ├── ...
└── SCREENSHOTS_MULTILINGUAL.md    # Screenshot guide

```

---

## 🌍 Languages Supported

| Language | File                  | Code | Target Market                             |
|----------|-----------------------|------|-------------------------------------------|
| English  | `en/STORE_LISTING.md` | `en` | USA, UK, Australia, Canada                |
| Italian  | `it/STORE_LISTING.md` | `it` | Italy                                     |
| German   | `de/STORE_LISTING.md` | `de` | Germany, Austria, Switzerland             |
| French   | `fr/STORE_LISTING.md` | `fr` | France, Belgium, Switzerland, Canada (FR) |
| Spanish  | `es/STORE_LISTING.md` | `es` | Spain, Latin America                      |

---

## 🚀 How to Use

### Step 1: Choose Your Language

Navigate to the folder matching your target language:

- English → `en/STORE_LISTING.md`
- Italiano → `it/STORE_LISTING.md`
- Deutsch → `de/STORE_LISTING.md`
- Français → `fr/STORE_LISTING.md`
- Español → `es/STORE_LISTING.md`

### Step 2: Copy Content to Google Play Console

1. **Go to**: [Google Play Console](https://play.google.com/console/)
2. **Select**: Your app → **Store Listing**
3. **Select Language** dropdown: Choose the language (e.g., "English", "Italiano", etc.)
4. **Fill in fields** by copying from corresponding `STORE_LISTING.md`:

| Field Name            | Source in Markdown           |
|-----------------------|------------------------------|
| **Title**             | "App Name (Display Name)"    |
| **Short description** | "Short Description"          |
| **Full description**  | "Full Description"           |
| **What's new**        | "Release Notes / What's New" |
| **Keywords**          | "Keywords / Tags"            |

### Step 3: Add Graphics & Screenshots

**Feature Graphic** (1024×500 px):

- Source: `feature-graphics/feature-graphic.svg`
- Upload to: **Feature graphic** field

**Promo Graphic** (180×120 px, optional):

- Source: `promo-graphics/promo-180x120.svg`
- Upload to: **Promo graphic** field (optional)

**Screenshots** (up to 8 per language):

- Source: `screenshots/{language}/*.png`
- Upload to: **Screenshots** section
- ⚠️ **Note**: Generate screenshots using `capture_screenshots.sh` (see
  `SCREENSHOTS_MULTILINGUAL.md`)

---

## ✍️ Customization Tips

### Update App Version

If releasing a new version (e.g., 1.4.7), update:

1. All `STORE_LISTING.md` files: Change `"Version": 1.4.6` → `"Version": 1.4.7`
2. Release Notes section with new features/fixes
3. Last Updated date

### Update Features List

Edit the **KEY FEATURES** section in each `STORE_LISTING.md`:

- Add/remove feature bullets (🎯 marker)
- Keep 5-8 main features for clarity
- Ensure consistency across all languages

### Update Keywords

Modify the **Keywords / Tags** section:

- Keep 5-7 relevant keywords
- Separate by commas
- Stay under 80 characters

### Translate Customizations

If you modify English content, ensure it's translated to all 4 other languages for consistency.

---

## 📊 Content Length Limits

| Field                 | Max Characters | Tips                                        |
|-----------------------|----------------|---------------------------------------------|
| **Title**             | 50             | Keep short, include app name + main feature |
| **Short Description** | 80             | Hook users in 1-2 sentences                 |
| **Full Description**  | 4000           | Use emojis, bullet points, clear sections   |
| **Release Notes**     | 500            | Focus on new features + bug fixes           |
| **Keywords**          | 80 total       | Comma-separated, no spaces around commas    |

---

## 🎨 Graphics Best Practices

### Feature Graphic (1024×500 px)

- **What it is**: Banner displayed at top of store listing
- **Design tips**:
    - Include app name clearly
    - Show key visual element (piggy bank, money, etc.)
    - Use brand colors (AntCashManager green)
    - Minimal text (max 10 words)
    - Include app version number

### Screenshots (1080×1920 px, 9:16 aspect ratio)

- **Recommended count**: 4-8 per language
- **Best practice flow**:
    1. Home screen (transactions overview)
    2. Add/Edit transaction
    3. Budget tracking
    4. Charts & analytics
    5. Settings
    6. Data management
    7. Features highlight
    8. Call-to-action (star/review prompt)

- **Design tips**:
    - Use actual app UI (not mockups)
    - Add captions/highlights for key features
    - Show dark mode variant (if space allows)
    - Include localized text in screenshots (strings from app resources)

---

## ✅ Pre-Launch Checklist

- [ ] All 5 languages filled in (EN, IT, DE, FR, ES)
- [ ] Title is accurate and ≤50 chars
- [ ] Short description is compelling and ≤80 chars
- [ ] Full description includes all key features
- [ ] Feature graphic uploaded (1024×500 px)
- [ ] At least 4 screenshots per language
- [ ] Screenshots are properly localized (language matches listing)
- [ ] Release notes are up-to-date
- [ ] Keywords are relevant and ≤80 chars
- [ ] No hardcoded URLs or personal info in listings
- [ ] Privacy policy link is present in description
- [ ] App version matches listings (1.4.6)
- [ ] No spelling or grammar errors
- [ ] Open source license mentioned
- [ ] Store listing complies with Google Play policies

---

## 🔗 References

### Google Play Console

- **Listing Docs**: https://support.google.com/googleplay/android-developer/answer/6334261
- **Graphics Specs**: https://support.google.com/googleplay/android-developer/answer/9866151
- **Review Guidelines**: https://play.google.com/about/developer-content-policy/

### AntCashManager Resources

- **Project README**: `../../README.md`
- **Privacy Policy**: `../../wiki/privacy-policy.html`
- **Multi-Language Support**: See `SCREENSHOTS_MULTILINGUAL.md`

---

## 💡 Troubleshooting

### Listing not updating after upload

- **Check**: Did you click "Save" at the bottom of each section?
- **Wait**: Changes may take 15-30 minutes to appear in store
- **Verify**: Switch languages in Play Console to confirm changes were saved

### Screenshot dimensions incorrect

- **Correct size**: 1080×1920 (9:16 aspect ratio)
- **Tool**: Use `optimize_screenshots.sh` to auto-resize

### Character limits exceeded

- **Solution**: Use `STORE_LISTING.md` as guide - all content already optimized
- **Check**: Copy exactly from markdown (don't add extra text)

### Language not appearing in dropdown

- **Reason**: Must have at least one complete listing in that language first
- **Fix**: Fill all required fields (Title, Short desc, Full desc, Graphics, Screenshots)
- **Wait**: May take 24-48 hours to show up

---

## 📞 Support

For questions or translations:

1. **Check `STORE_LISTING.md`** for your language
2. **Review `SCREENSHOTS_MULTILINGUAL.md`** for screenshot guide
3. **Consult `README.md`** for app context
4. **File issue** on GitHub: https://github.com/sformica/AntCashManager

---

**Last Updated**: May 2026  
**App Version**: 1.4.6  
**Format**: Markdown → Google Play Console  
**Languages**: 5 (EN, IT, DE, FR, ES)

