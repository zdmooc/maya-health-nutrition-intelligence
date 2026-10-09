# Android H1 — local-first nutrition diary

La branche `main` contient les sources de l'application Android native Kotlin/Jetpack Compose. Fonction prévue : journal de repas manuel (nom, calories, grammes optionnels), sommes quotidiennes, historique et suppression, avec stockage **AES-256/GCM chiffré** par Android Keystore.

**Aucune donnée personnelle dans Git.** La version debug n'intègre ni caméra, ni Internet, ni cloud, ni PDF, ni IA. Les calories doivent être saisies manuellement. Les micronutriments absents restent inconnus.

## Build CI
GitHub Actions [android-h1.yml](../../.github/workflows/android-h1.yml) exécute :
- JDK 17, SDK API 37, Gradle 9.5, AGP 9.3 ;
- tests unitaires des valeurs, formats et dates locales ;
- compilation `:app:assembleDebug` puis APK en artifact temporaire avec SHA-256.

Au 2026-10-09 : build CI **SUCCESS**, [run #37970764222](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/37970764222), build + tests JVM + artifact debug APK et SHA-256 publiés. Test sur téléphone réel/émulateur **non effectué**.

## Installation sur Android
1. Ouvrir GitHub Actions > **Android H1 — build and unit tests** > dernier run réussi.
2. Télécharger l'artefact `maya-health-android-h1-debug-apk` (ZIP) puis extraire `app-debug.apk`.
3. Transférer vers un téléphone Android de test et installer après vérification du SHA-256.
4. Ajouter deux aliments fictifs, fermer complètement l'application, la rouvrir, vérifier la persistance et la suppression ; tester absence de connexion réseau.
5. Ne passer à des données personnelles qu'après qualification sur appareil et stratégie de sauvegarde/export (pas encore disponible).

Option locale : Gradle 9.5 installé, Android SDK 37, build-tools 36, JDK 17 ; depuis `apps/android` : `gradle :app:testDebugUnitTest :app:assembleDebug`.

## Gates
- `H1_SOURCE_READY` : Kotlin + tests source + sécurité at-rest.
- `H1_CI_BUILD_PROVEN` : un run GitHub Actions SUCCESS et APK artifact.
- `H1_DEVICE_RUNTIME_PROVEN` : un vrai téléphone/émulateur + test Android Keystore et redémarrage.
- `H1_DATA_PORTABILITY_READY` : export/import chiffré et suppression vérifiables (non implémentés).

La présence d'une interface et du chiffrement ne démontre pas une conformité réglementaire ou un dispositif médical.
