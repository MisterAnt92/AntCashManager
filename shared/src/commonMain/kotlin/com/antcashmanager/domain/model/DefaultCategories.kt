package com.antcashmanager.domain.model

public val DEFAULT_EXPENSE_CATEGORIES: List<Category> = listOf(
    Category(name = "Non categorizzato", icon = "more_horiz",    color = 0xFF90A4AE, type = "EXPENSE", isDefault = true, sortOrder = 0),
    Category(name = "Casa",              icon = "home",           color = 0xFF4FC3F7, type = "EXPENSE", isDefault = true, sortOrder = 1),
    Category(name = "Trasporti",         icon = "directions_car", color = 0xFF64B5F6, type = "EXPENSE", isDefault = true, sortOrder = 2),
    Category(name = "Cibo",              icon = "restaurant",     color = 0xFFE57373, type = "EXPENSE", isDefault = true, sortOrder = 3),
    Category(name = "Bollette",          icon = "receipt_long",   color = 0xFFFFB74D, type = "EXPENSE", isDefault = true, sortOrder = 4),
    Category(name = "Pranzi/Cene fuori", icon = "local_dining",   color = 0xFFF06292, type = "EXPENSE", isDefault = true, sortOrder = 5),
    Category(name = "Divertimento",      icon = "theater_comedy", color = 0xFFBA68C8, type = "EXPENSE", isDefault = true, sortOrder = 6),
    Category(name = "Salute",            icon = "local_hospital", color = 0xFF81C784, type = "EXPENSE", isDefault = true, sortOrder = 7),
    Category(name = "Shopping",          icon = "shopping_bag",   color = 0xFFDCE775, type = "EXPENSE", isDefault = true, sortOrder = 8),
    Category(name = "Istruzione",        icon = "school",         color = 0xFF7986CB, type = "EXPENSE", isDefault = true, sortOrder = 9),
    Category(name = "Altro",             icon = "more_horiz",     color = 0xFF90A4AE, type = "EXPENSE", isDefault = true, sortOrder = 10),
    Category(name = "Regali",            icon = "redeem",         color = 0xFFFF8A65, type = "EXPENSE", isDefault = true, sortOrder = 11),
    Category(name = "Abbonamenti",       icon = "subscriptions",  color = 0xFFFFD54F, type = "EXPENSE", isDefault = true, sortOrder = 12),
    Category(name = "Cura personale",    icon = "spa",            color = 0xFFA1887F, type = "EXPENSE", isDefault = true, sortOrder = 13),
)

public val DEFAULT_INCOME_CATEGORIES: List<Category> = listOf(
    Category(name = "Non categorizzato", icon = "more_horiz",        color = 0xFF90A4AE, type = "INCOME", isDefault = true, sortOrder = 0),
    Category(name = "Stipendio",         icon = "payments",           color = 0xFF81C784, type = "INCOME", isDefault = true, sortOrder = 1),
    Category(name = "Paghetta",          icon = "savings",            color = 0xFF4DB6AC, type = "INCOME", isDefault = true, sortOrder = 2),
    Category(name = "Rimborso",          icon = "currency_exchange",  color = 0xFF64B5F6, type = "INCOME", isDefault = true, sortOrder = 3),
    Category(name = "Investimenti",      icon = "trending_up",        color = 0xFFFFD54F, type = "INCOME", isDefault = true, sortOrder = 4),
    Category(name = "Freelance",         icon = "work",               color = 0xFFA1887F, type = "INCOME", isDefault = true, sortOrder = 5),
    Category(name = "Altro",             icon = "more_horiz",         color = 0xFF90A4AE, type = "INCOME", isDefault = true, sortOrder = 6),
    Category(name = "Regalo",            icon = "redeem",             color = 0xFFFF8A65, type = "INCOME", isDefault = true, sortOrder = 7),
    Category(name = "Buoni pasto",       icon = "card_giftcard",      color = 0xFF66BB6A, type = "INCOME", isDefault = true, sortOrder = 8),
)

public val ALL_DEFAULT_CATEGORIES: List<Category> = DEFAULT_EXPENSE_CATEGORIES + DEFAULT_INCOME_CATEGORIES
