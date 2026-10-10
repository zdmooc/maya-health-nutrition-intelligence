# Android H1 — local-first nutrition diary

La branche `main` contient les sources de l'application Android native Kotlin/Jetpack Compose. Fonction prévue : journal de repas manuel (nom, calories, grammes optionnels), sommes quotidiennes, historique et suppression, avec stockage **AES-256/GCM chiffré** par Android Keystore.

**Aucune donnée personnelle dans Git.** H1 tient le journal chiffré localement. H2 utilise Google Code Scanner (sans permission caméra de notre APK) et la permission INTERNET pour une requête explicite Open Food Facts. Aucun bilan, image du repas ou journal n'est envoyé. Les micronutriments absents restent inconnus.

## Build CI
GitHub Actions [android-h1.yml](../../.github/workflows/android-h1.yml) exécute :
- JDK 17, SDK API 37, Gradle 9.5, AGP 9.3 ;
- tests unitaires des valeurs, formats et dates locales ;
- compilation `:app:assembleDebug` puis APK en artifact temporaire avec SHA-256.

Baseline 2026-10-09 : build CI **SUCCESS**, [run #37970764222](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/37970764222), build + tests JVM + artifact debug APK et SHA-256 publiés. Test sur téléphone réel/émulateur **non effectué**.

## Fonctions de sauvegarde chiffrée (sources ajoutées 10/10)
- Export SAF dans une archive AES-GCM protégée par une phrase secrète dérivée PBKDF2-HMAC-SHA256 ; import contrôlé, confirmation d'écrasement du journal, purge des données et de la clé.
- Tests Android d'intégration ajoutés ; **aucune preuve sur téléphone utilisateur**.
- Pour les mises à jour et les clés de signature de release, voir [le guide de distribution](../../docs/SECURE_SIGNING_AND_UPDATE.md).

## Installation sur Android
1. Ouvrir GitHub Actions > **Android H1 — build and unit tests** > dernier run réussi.
2. Télécharger l'artefact `maya-health-android-h1-debug-apk` (ZIP) puis extraire `app-debug.apk`.
3. Transférer vers un téléphone Android de test et installer après vérification du SHA-256.
4. Ajouter deux aliments fictifs, fermer complètement l'application, la rouvrir, vérifier la persistance et la suppression ; tester absence de connexion réseau.
5. N'utiliser que des données fictives jusqu'à validation sur téléphone de la sauvegarde et de la restauration chiffrées.

Option locale : Gradle 9.5 installé, Android SDK 37, build-tools 36, JDK 17 ; depuis `apps/android` : `gradle :app:testDebugUnitTest :app:assembleDebug`.

## Gates
- `H1_SOURCE_READY` : Kotlin + tests source + sécurité at-rest.
- `H1_CI_BUILD_PROVEN` : un run GitHub Actions SUCCESS et APK artifact.
- `H1_DEVICE_RUNTIME_PROVEN` : un vrai téléphone/émulateur + test Android Keystore et redémarrage.
- `H1_DATA_PORTABILITY_READY` : export/import chiffré et purge codés ; validation d'exécution à confirmer.

La présence d'une interface et du chiffrement ne démontre pas une conformité réglementaire ou un dispositif médical.

## H2 — Scanner et Open Food Facts (développement en cours)
- Scanner Google Play Services 16.1.0 sur téléphone compatible, ou saisir un EAN/UPC manuellement.
- Contrôle du chiffre GTIN, consultation HTTPS Open Food Facts v3 uniquement sur bouton utilisateur.
- Les valeurs nutritionnelles sont issues d'une base collaborative et doivent être vérifiées ; si l'énergie est inconnue, pas de conversion automatique à zéro.
- Confirmer les grammes et cliquer sur Préremplir, puis vérifier la saisie dans le journal.
- H2 ne stocke pas encore dans l'entrée persistée le code-barres, la provenance ni les micronutriments ; ces extensions restent planifiées.
- Validation scanner réel et réseau indisponible sur téléphone : encore requise.

## Dernières preuves (10 octobre 2026)
- [Build Android H1/H2 SUCCESS](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/38042295457) sur commit `885508d6`, artefact APK debug (ID 11665863455).
- [Instrumentation Android API 35 SUCCESS](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/38042295500) : 6 tests, 0 échec.
- Ces résultats **ne** testent **pas** le scanner de caméra ni le GET réel sur Open Food Facts ; essai sur téléphone et stratégie de signature restent requis.
- [Evidence complète](../../evidence/H1_H2_ANDROID_VALIDATION_2026-10-10.md).
