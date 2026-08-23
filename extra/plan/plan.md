Specifiche inserimento transazioni:
Tipologia USCITE:
- Quando inserisci pagamento buoni pasto ci deve essere la possibilità opzionale di inserire l'importo più un multiplo dei buoni pasto;
attualmente non è così. Analizza e sistema questo tipo di inserimento e vedi se è possibile implementare migliorie.
-  Quando si sceglie il metodo di pagamento buoni pasto sull'inserimento della Transazione e si inserisce la Differenza pagata questo valore delle essere salvato e sommato
al multiplo dei buoni pasto nella Transazione per avere il totale della Transazione. Analizza,verifica e sistema perchè non è così.
- Quando si inserisce il Valore Buono Pasto sulle impostazioni, questo valore salvato poi andrà utilizzato quando si sceglie
il metodo di pagamento buoni pasto come multiplo sull'inserimento della Transazione. Analizza,verifica e sistema perchè non è così.


Quando inserisci pagamento buoni pasto ci deve essere la possibilità opzionale di inserire l'importo più un multiplo dei buoni pasto;
attualmente non è così. Analizza e sistema questo tipo di inserimento e vedi se è possibile implementare migliorie.


- TODO: Alla sezione di Impostazioni, sotto Visualizzazione aggiungi una nuova sezione "Pagamenti", sposta dentro la parte relativa al Valore Buono Pasto e poi aggiungi
il pulsante "Tipo di pagamento predefinito". Al click si cambierà il tipo di pagamento predefinito che attualmente è il tipo Elettronico quando vai ad 
aggiungere/modificare una Transazione. Questa opzione andrà salvata come configurazione e poi andrà visualizzato/impostato il predefinito sulle nuove Transazioni
che si andranno ad aggiungere.  Aggiorna poi gli unit test e quelli che falliranno con questa modifica.



- Unit Test da sistemare:
- Caused by: java.io.IOException: Error while instrumenting sun/util/resources/cldr/provider/CLDRLocaleDataMetaInfo with JaCoCo 0.8.12.202403310830/dbfb6f2.
- Caused by: java.lang.IllegalArgumentException: Unsupported class file major version 69
- Caused by: java.security.NoSuchAlgorithmException at BackupServiceTest.kt:491
- Caused by: java.security.NoSuchAlgorithmException at BackupPayloadCipherTest.kt:127
- Caused by: java.lang.ClassNotFoundException at SandboxClassLoader.java:164

- 

