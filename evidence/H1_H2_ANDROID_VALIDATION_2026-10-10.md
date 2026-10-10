# Maya Health — H1/H2 Android validation — 2026-10-10

**Source de vérité pour la maturité technique de la version 0.3.0-dev.**

## Release candidate de test
- Repo : https://github.com/zdmooc/maya-health-nutrition-intelligence
- Commit de code : [885508d6](https://github.com/zdmooc/maya-health-nutrition-intelligence/commit/885508d6c741ba95e7ff9382afbfea1a73221096).
- Build GitHub Actions : [38042295457](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/38042295457) — **SUCCESS** (Gradle testDebugUnitTest + assembleDebug).
- Emulateur GitHub Actions : [38042295500](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/38042295500) — **SUCCESS**, Android API 35, **6 tests / 6 PASS**, 0 erreur.
- APK debug GitHub artifact : `maya-health-android-h1-debug-apk`, artifact ID `11665863455`, expiration annoncée le 24 octobre 2026.
- SHA-256 de `app-debug.apk` extrait de l'artifact et vérifié : `f0eae58b284424433bb11189d6228d07efb5c67a4f8710df41568a3e4da4f159`.
- Taille APK brut : 12 678 457 octets.
- Identité Android : `com.maya.health` ; version `0.3.0-h2-dev`, code 3.
- Tests exercés : sérialisation stricte JSON, Android Keystore AES-GCM (persistance, refus des fichiers modifiés), sauvegarde portable chiffrée PBKDF2+AES-GCM (mot de passe incorrect/corruption), purge locale et re-création d'une clé, parser Open Food Facts avec données nutritionnelles manquantes, rejet de codes GTIN incorrects.

## Matrice de vérité
| Fonctionnalité | Verdict | Limites |
|---|---|---|
| Journal alimentaire manuel + stockage AES-GCM | **EMULATOR_RUNTIME_PROVEN** | pas de test smartphone utilisateur |
| Export/import chiffré par fichier + purge | **EMULATOR_CRYPTO_AND_REPOSITORY_PROVEN** | flux complet de sélection SAF UI et stockage externe non instrumenté |
| API Open Food Facts v3 / résultat nutritionnel | **PARSER_EMULATOR_PROVEN** | aucune requête réelle testée en CI, base collaborative |
| Scanner Google Code Scanner | **BUILD_PROVEN / DEVICE_PENDING** | caméra + services Google Play à tester sur Android réel |
| Identité/clé stable release, migration APK vN->vN+1 | **NOT_PROVEN** | clé de release et essais de mise à jour manquants |
| Micronutriments CIQUAL/USDA | **NOT_IMPLEMENTED** | H4 OPEN |
| Photo, bilan sanguin, PubMed/RAG | **NOT_IMPLEMENTED** | H3, H5, H6 OPEN |

## Contrôles de confidentialité
- Pas de données médicales personnelles ou aliments réels dans le dépôt, CI et tests : fixtures synthétiques uniquement.
- L'export chiffré se produit via Storage Access Framework ; le journal n'est pas uploadé par l'application.
- H2 requiert maintenant `android.permission.INTERNET`, exclusivement pour la consultation par code produit ; le scanner via Google Play Services ne requiert pas de permission CAMERA dans l'application.
- Les champs nutritionnels absents restent inconnus, non remplacés par zéro.
- Les permissions et comportements réseau sur l'appareil, gestion des captures d'écran, cloud choisi par l'utilisateur et l'absence de données de santé dans les logs doivent être vérifiés avant usage réel.

## À exécuter sur téléphone Android
1. Installer le nouvel APK sur appareil **de test**, sans désinstaller une précédente version contenant des données à préserver.
2. Ajouter deux aliments fictifs, fermer/redémarrer, vérifier la conservation des données.
3. Exporter un backup avec phrase secrète forte, valider lecture après réinstallation de test, tester mot de passe erroné.
4. Tester le scanner sur un vrai EAN, confirmation du GTIN, produit OFF trouvé/introuvable et absence de réseau.
5. Contrôler calories, portion, données inconnues ; rien n'est ajouté avant confirmation.
6. Vérifier purge + alias Keystore supprimé et absence de sauvegarde interne ; contrôler séparément exports externes.
7. Mettre en place une signature release stable et tester migration avec conservation du journal. Les clés debug CI sont éphémères.

**Ne pas annoncer H1 complètement CLOSE ou H2 pleinement opérationnel avant les essais sur téléphone.**
