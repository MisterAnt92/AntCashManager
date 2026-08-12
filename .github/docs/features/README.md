# Feature Documentation

Documentazione di feature specifiche della app.

## 📄 File in questa cartella

### RECEIPT_SCAN_FEATURE.md
**Receipt Scanning Feature (ML Kit OCR)**

Contenuto:
- Feature overview (cosa fa, perché è importante)
- Architecture (Flow del receipt scan)
- ML Kit integration (Text recognition, preprocessing)
- Data flow (ML Kit → entity extraction → transaction creation)
- Error handling (invalid receipts, incomplete data)
- Future improvements (currency detection, merchant mapping)
- Testing strategy

**Usa quando**:
- Devi lavorare sul feature di receipt scanning
- Devi capire come è implementato il receipt scan
- Devi debuggare problemi di OCR

## 🎯 Feature List (AntCashManager)

Altre feature implementate (vedi AGENTS.md e copilot-instructions.md):
- Transaction management (CRUD)
- Category management
- Charts and analytics
- Widgets (Glance API)
- Backup/Restore
- Multi-language support (EN, IT, FR, DE, ES)
- DataStore encryption (optional)

## 📚 How to Document New Features

Quando aggiungi una nuova feature, crea un markdown file con:
1. Feature overview (descrizione, benefits)
2. Architecture (layer breakdown, components)
3. Key flows (user interaction flow)
4. Data models (entities, DTOs)
5. Error handling (edge cases, error messages)
6. Testing strategy (unit, UI, integration tests)
7. Future improvements (known limitations, next steps)

## 📚 Additional Resources

- Vedi [architecture/](../architecture/) per Clean Architecture details
- Vedi [development/](../development/) per development guidelines
- Vedi [testing/](../testing/) per testing patterns

**Last Updated**: 2026-08-12
