# Testing Guide: Meal Vouchers (Buoni Pasto) v1.7.4

## Obiettivo
Verificare che:
1. La **differenza pagata** sia salvata e recuperata quando si modifica una transazione
2. Il **valore dei buoni pasto** dalle impostazioni sia utilizzato nei calcoli
3. Il **totale della transazione** sia calcolato correttamente

---

## Scenario 1: Creare Transazione con Buoni Pasto + Differenza

### Setup
- Andare a **Impostazioni → Dati → Valore Buono Pasto**
- Verificare il valore: **5.29 €** (default)
- Tornare alla home

### Test
1. Tap **"+ Aggiungi Transazione"**
2. Selezionare categoria: **Alimentari** (o simile)
3. Tipo: **Spesa** (EXPENSE)
4. Titolo: **"Pranzo ufficio"**
5. Metodo pagamento: **BUONI PASTO**
6. Numero buoni: **3**
7. Differenza pagata: **2.50**
8. **Tap Salva**

### Verifica
- ✅ Screen mostra: **Totale = 18.37 €**
  - Calcolo: (3 × 5.29) + 2.50 = 15.87 + 2.50 = 18.37
- ✅ La transazione appare in Home
- ✅ L'importo visualizzato è **-18.37 €** (negativo = spesa)

---

## Scenario 2: Modificare la Transazione e Verificare Differenza Salvata

### Test
1. Tornare alla Home
2. Tap sulla transazione **"Pranzo ufficio"**
3. **Tap per modificare** (pencil icon)

### Verifica CRITICA
- ✅ Campo **"Differenza pagata"** mostra: **2.50** ✓ SALVATO!
- ✅ Campo **"Numero buoni"** mostra: **3**
- ✅ **Totale** ancora mostra: **18.37 €**
- ✅ **Subtotale** (solo buoni): **15.87 €**
- ✅ **Differenza calcolata**: **2.50 €**

### Azione
- Cambiar differenza: **3.50** (era 2.50)
- Nuovo totale dovrebbe diventare: **19.37 €**
- **Tap Salva**

### Verifica
- ✅ Totale aggiornato a **-19.37 €**

---

## Scenario 3: Cambiare Valore Buoni Pasto nelle Impostazioni

### Setup iniziale
- Andare a **Impostazioni → Dati → Valore Buono Pasto**
- Valore attuale: **5.29 €**

### Test
1. Tap sul valore **5.29**
2. Cambiar valore: **6.00** (aumentato)
3. **Tap Salva/OK**

### Verifica impostazione salvata
- ✅ Ritornare a Impostazioni
- ✅ Valore mostra: **6.00 €** ✓ SALVATO!

### Verifica applicazione nel calcolo
1. Tornare a Home
2. Tap **"+ Aggiungi Transazione"**
3. Selezionare **BUONI PASTO**
4. Numero buoni: **2**
5. Differenza pagata: **1.00**

### Verifica
- ✅ **Subtotale**: (2 × 6.00) = **12.00 €** ✓ NUOVO VALORE USATO!
- ✅ **Totale**: 12.00 + 1.00 = **13.00 €**
- **Tap Salva**

### Verifica storico
1. Tornare alla transazione **"Pranzo ufficio"** (creata prima)
2. **Tap per modificare**
   
### Verifica CRITICA - Vecchie transazioni mantengono calcoli corretti
- ✅ Questa transazione ANCORA mostra: **19.37 €**
   - Perché? Ha 3 buoni × 5.29 (VECCHIO valore) + 3.50 = 18.87 + 3.50 = 22.37
   - NO ASPETTA - il totale è già salvato in DB come -19.37
   - Quando si apre per modifica, il valore è preservato ✓
- ✅ La nuova transazione (2 buoni) mostra: **13.00 €**
   - Usa il nuovo valore 6.00 ✓

---

## Scenario 4: Backup e Restore

### Setup
1. Andare a **Impostazioni → Backup**
2. **Tap "Esegui Backup Ora"**
3. Selezionare cartella backup
4. **Attendere** "Backup completato"

### Test Restore
1. Andare a **Impostazioni → Ripristina da Backup**
2. Selezionare il file backup creato
3. **Confermare** ripristino

### Verifica CRITICA - Differenza Recuperata
1. Tornare a Home
2. Tap sulla transazione **"Pranzo ufficio"**
3. **Tap per modificare**

### Verifica
- ✅ **Numero buoni**: 3
- ✅ **Differenza pagata**: 3.50 ✓ RECUPERATA DAL BACKUP!
- ✅ **Totale**: 19.37 €
- ✅ Valore buono pasto: 5.29 €

---

## Scenario 5: Transazioni di Tipo INCOME con Buoni Pasto

### Test (Raro ma possibile)
1. **+ Aggiungi Transazione**
2. Tipo: **Entrata** (INCOME)
3. Metodo: **BUONI PASTO**
4. Numero: **5**

### Verifica
- ✅ Nessun campo **"Differenza pagata"** (solo per EXPENSE)
- ✅ Totale: (5 × 5.29) = **26.45 €** (positivo)

---

## Test di Regressione - Campo Differenza Nascosto per Non-Buoni

### Test
1. **+ Aggiungi Transazione**
2. Metodo pagamento: **CONTANTI** (o CARTA)
3. Verificare: **Campo "Differenza pagata" NON VISIBILE** ✓
4. Questo campo appare SOLO per BUONI PASTO

---

## ✅ Checklist Finale

| Verifica | Status |
|----------|--------|
| Differenza pagata **salvata** e **recuperata** ✓ | ✅ |
| Valore buoni pasto dalle impostazioni **usato** ✓ | ✅ |
| Totale calcolato: (count × value) + difference ✓ | ✅ |
| Backup/restore **preserva differenza** ✓ | ✅ |
| Vecchie transazioni mantengono valori originali ✓ | ✅ |
| Campo differenza nascosto per non-buoni ✓ | ✅ |

---

## Segnalazione Bug

Se uno dei test fallisce:
1. Screenshot della transazione
2. Note il valore atteso vs valore ricevuto
3. Indicare step esatto dove fallisce
