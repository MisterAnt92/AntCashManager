# Card Components

## Overview

Card containers following Material Design 3 with semantic content grouping.

## Files

- **AppCard.kt** - Generic card wrapper for any content
- **AppCategoryCard.kt** - Category-specific card with icon and label

## Usage

### AppCard (Generic)

```kotlin
AppCard(
    modifier = Modifier.fillMaxWidth(),
    onClick = { navigateToDetails() }
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Card Title", style = MaterialTheme.typography.headlineSmall)
        Text("Card content", style = MaterialTheme.typography.bodyMedium)
    }
}
```

### AppCategoryCard (Semantic)

```kotlin
AppCategoryCard(
    category = category,
    isSelected = selectedCategoryId == category.id,
    onClick = { onSelectCategory(category.id) }
)
```

## Styling

Cards automatically use:
- Background: `MaterialTheme.colorScheme.surface`
- Elevation: Material 3 default
- Rounded corners: 12dp
- Padding: 16dp (configurable)

## Guidelines

- ✅ Use AppCard for grouping related content
- ✅ Use AppCategoryCard for category selection
- ✅ Keep card content concise
- ❌ Don't nest cards too deeply
- ❌ Don't use cards for simple text

## Responsive

- Phone: Full width minus padding
- Tablet: 2-3 columns grid
- Foldable: Adapt to fold line

## Testing

- Light & Dark themes
- Various content sizes
- Tablet grid layouts

---

**See:** ../README.md for full component documentation
