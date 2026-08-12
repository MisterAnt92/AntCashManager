# Deployment & Debug Documentation

Guide per rilascio, debugging e deployment della app.

## 📄 File in questa cartella

### DEPLOY_NOW.md
**Deployment checklist e procedure**

Contenuto:
- Pre-deployment checklist (version, changelog, notes)
- Build commands (debug APK, release APK)
- Crash reporting and error tracking setup
- Release notes e versioning
- Procedure per deploy su Play Store
- Post-deployment verification

**Usa quando**:
- Devi fare un deploy della app
- Vuoi una checklist per assicurarti di non dimenticare nulla
- Devi configurare crash reporting

### DEBUG_SYMBOLS_GUIDE.md
**Debug symbols per leggere stack trace**

Contenuto:
- Come configurare simboli di debug
- Come mappare offuscamento
- Come leggere stack trace
- Setup Crashlytics
- Configurazione debugging device

**Usa quando**:
- Hai un crash da debuggare
- Stack trace è offuscato e non legibile
- Devi configurare debug symbols per la prima volta

## 🎯 Quick Checklist

**Pre-Deploy**:
- [ ] Version bump (MAJOR.MINOR.PATCH)
- [ ] Changelog aggiornato
- [ ] All tests passing
- [ ] APK build successful
- [ ] Crash reporting configured

**Deploy**:
- [ ] Upload APK/Bundle a Play Store
- [ ] Release notes visibili
- [ ] Rollout percentage (staggered se primo deploy)

**Post-Deploy**:
- [ ] Monitor crash reports
- [ ] Check analytics
- [ ] Respond to early feedback

## 📚 Additional Resources

- Vedi [AGENTS.md](../../../AGENTS.md) per procedure di codice prima di deploy
- Vedi [testing/](../testing/) per assicurarsi che tutti i test passino

**Last Updated**: 2026-08-12
