# H1 — Android source, tests JVM & APK CI evidence (2026-10-09)

> Snapshot historique du 9 octobre. Il est remplacé pour l'état actuel par [H1/H2 Android validation du 10 octobre](H1_H2_ANDROID_VALIDATION_2026-10-10.md) : 6 tests instrumentation Android SUCCESS. Les états `NO` ci-dessous décrivent uniquement le snapshot historique du 9 octobre.

## Factuel
- Repo: `zdmooc/maya-health-nutrition-intelligence`, branche `main`.
- Fonction H1 : saisie du nom, calories manuelles, grammes facultatifs, total par date locale, historique, suppression d'une entrée.
- Sécurité : chiffrement AES-256/GCM via AndroidKeyStore, AtomicFile, `noBackupFilesDir`, `allowBackup=false`, pas de permission INTERNET, appareil et crash tests à valider.
- Code initial H1 : [8bb021818746b1bc064041e3ad903a970457dda6](https://github.com/zdmooc/maya-health-nutrition-intelligence/commit/8bb021818746b1bc064041e3ad903a970457dda6).
- Correctifs CI : `setup-android@v4`, SDK platform package `android-37.0`.
- Preuve CI : **SUCCESS** [GitHub Actions run 37970764222](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/37970764222).
- Job `android` : `:app:testDebugUnitTest :app:assembleDebug` — SUCCESS ; log indique `BUILD SUCCESSFUL in 2m 29s`.
- Artifact : `maya-health-android-h1-debug-apk` (ID 11636500134), expires 2026-10-23 ; contient `app-debug.apk` et `app-debug.apk.sha256`.
- SHA-256 APK vérifié indépendamment après extraction de l'artefact : `31256828c62a1562e773242b8060bc5f21ac526c544ce7687b6146ef60342520`.
- APK de démonstration `debug`, non signé pour la distribution Play ; ne pas présenter comme release.
- Tests automatisés JVM des nombres, entrées invalides, découpage date Europe/Paris : PASS au niveau CI.
- Test d'instrumentation Android Keystore créé, **non exécuté** par ce workflow (nécessite émulateur/appareil).
- Aucun scan, caméra, PubMed, analyse médicale, micronutriments calculés à ce stade.

## Statuts de preuve
| Gate | État |
|---|---|
| H1_SOURCE_READY | YES |
| H1_CI_BUILD_PROVEN | YES |
| H1_DEBUG_APK_ARTIFACT | YES |
| H1_DEVICE_RUNTIME_PROVEN | NO |
| H1_ENCRYPTION_INSTRUMENTATION_PROVEN | NO |
| H1_DATA_PORTABILITY_READY | NO |
| H1_RELEASE_SIGNED_PLAYSTORE | NO |

## Recommandation de validation humaine
1. Télécharger l'artefact ZIP depuis le lien CI, extraire `app-debug.apk`, comparer SHA-256.
2. Installer sur téléphone Android compatible en autorisant temporairement l'installation depuis la source choisie.
3. Ajouter deux entrées **fictives**, fermer l'application, la rouvrir : les données doivent persister.
4. Vérifier le total du jour, ajouter une entrée après minuit, supprimer une entrée, déconnecter le réseau.
5. Vérifier exceptions/erreurs (écriture impossible, corruption fichier, migration clé) et l'absence de permissions Internet.
6. Rejouer sur émulateur le test d'instrumentation AndroidKeyStore.
7. Ajouter la fonction export chiffré/import/suppression intégrale avant de qualifier H1 totalement terminé.

Ne jamais publier des données médicales personnelles, journaux ou photos de repas réels dans GitHub.
