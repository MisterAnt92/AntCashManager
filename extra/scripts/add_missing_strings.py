#!/usr/bin/env python3
"""Script per aggiungere automaticamente le stringhe mancanti nei file di traduzione"""

import xml.etree.ElementTree as ET
import os

# Traduzioni mancanti
translations = {
    'it': {
        'categories_selection_hint': 'Tocca una categoria per selezionarla (avanzamento automatico nell\'app reale)',
        'charts_legend_item_subtitle': '%1$s • %2$s',
        'settings_third_party_author': 'di %s',
        'transactions_search_query_preview': '"%s"',
    },
    'fr': {
        'common_dashboard': 'Tableau de bord',
        'common_home': 'Accueil',
        'common_charts': 'Graphiques',
        'common_transactions': 'Transactions',
        'common_categories': 'Catégories',
        'common_settings': 'Paramètres',
        'common_income': 'Revenus',
        'common_expenses': 'Dépenses',
        'common_back': 'Retour',
        'common_add': 'Ajouter',
        'add_transaction_tags_suggestions': 'Suggestions :',
        'add_transaction_delete_confirm_msg': 'Êtes-vous sûr de vouloir supprimer cette transaction ? Cette action ne peut pas être annulée.',
        'categories_selection_hint': 'Appuyez sur une catégorie pour la sélectionner (avancement automatique dans l\'application réelle)',
        'transactions_search_query_preview': '"%s"',
        'settings_third_party_author': 'par %s',
        'error_opening_link': 'Impossible d\'ouvrir le lien. Veuillez vérifier vos applications.',
        'settings_easter_egg_message': '🐜 Salut ! Merci d\'utiliser AntCash !',
        'common_close': 'Fermer',
        'common_click_here': 'Cliquez ici',
        'preview_app_text_title': 'Titre d\'exemple',
        'preview_app_text_dark': 'Titre sombre',
        'preview_app_text_clickable': 'Texte cliquable',
        'preview_app_text_all_caps': 'ce texte est en majuscules',
    },
    'es': {
        'common_dashboard': 'Panel',
        'common_home': 'Inicio',
        'common_charts': 'Gráficos',
        'common_transactions': 'Transacciones',
        'common_categories': 'Categorías',
        'common_settings': 'Ajustes',
        'common_income': 'Ingresos',
        'common_expenses': 'Gastos',
        'common_back': 'Atrás',
        'common_add': 'Añadir',
        'add_transaction_tags_suggestions': 'Sugerencias:',
        'add_transaction_delete_confirm_msg': '¿Estás seguro de que quieres eliminar esta transacción? Esta acción no se puede deshacer.',
        'categories_selection_hint': 'Toca una categoría para seleccionarla (avance automático en la aplicación real)',
        'charts_legend_item_subtitle': '%1$s • %2$s',
        'transactions_search_query_preview': '"%s"',
        'settings_third_party_author': 'por %s',
        'error_opening_link': 'No se pudo abrir el enlace. Por favor, compruebe sus aplicaciones.',
        'settings_easter_egg_message': '🐜 ¡Hola! ¡Gracias por usar AntCash!',
        'common_close': 'Cerrar',
        'common_click_here': 'Haz clic aquí',
        'preview_app_text_title': 'Título de ejemplo',
        'preview_app_text_dark': 'Título oscuro',
        'preview_app_text_clickable': 'Texto clicable',
        'preview_app_text_all_caps': 'este texto está en mayúsculas',
        'home_transaction_item_subtitle': '%1$s • %2$s',
    },
}

def add_strings_to_file(lang_code, translations_dict):
    """Aggiunge le stringhe mancanti al file di lingua specificato"""
    file_path = f'androidApp/src/main/res/values-{lang_code}/strings.xml'

    try:
        # Leggi il file esistente
        tree = ET.parse(file_path)
        root = tree.getroot()

        # Trova le stringhe esistenti
        existing_keys = {elem.get('name') for elem in root.findall('string')}

        # Aggiungi le stringhe mancanti
        added_count = 0
        for key, value in translations_dict.items():
            if key not in existing_keys:
                # Crea il nuovo elemento
                new_elem = ET.Element('string', {'name': key})
                # Escapa gli apostrofi per Android
                new_elem.text = value.replace("'", "\\'")
                # Aggiungi alla fine
                root.append(new_elem)
                added_count += 1
                print(f"  Aggiunta: {key}")

        if added_count > 0:
            # Scrivi il file aggiornato con formattazione corretta
            ET.indent(tree, space='    ')
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            print(f"✅ {lang_code.upper()}: {added_count} stringhe aggiunte")
        else:
            print(f"ℹ️  {lang_code.upper()}: Nessuna stringa da aggiungere")

    except Exception as e:
        print(f"❌ Errore per {lang_code}: {e}")

def main():
    os.chdir('/opt/src/GIT/app/AntCashManager')

    print("🔄 Aggiunta stringhe mancanti...\n")

    for lang_code, trans_dict in translations.items():
        print(f"\n=== {lang_code.upper()} ===")
        add_strings_to_file(lang_code, trans_dict)

    print("\n✨ Operazione completata!")

if __name__ == '__main__':
    main()

