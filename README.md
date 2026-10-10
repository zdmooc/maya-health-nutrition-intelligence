# Maya Health & Nutrition Intelligence

Application Android **personnelle** de suivi nutritionnel, d'analyse de bilans biologiques et de veille scientifique, basée sur des **preuves traçables**.

> **État au 2026-10-10 : H1_EMULATOR_RUNTIME_PROVEN / PHONE_PENDING ; H2_CI_AND_PARSER_EMULATOR_PROVEN / SCANNER_PHONE_PENDING.** Ajout de l'export/import chiffré et de la purge ; preuves de fonctionnement sur appareil à valider avant clôture.

## Reprise de session — 10 octobre 2026

Travail suspendu à la demande de l'utilisateur. Reprendre par [le checkpoint H1/H2 et la liste des actions restantes](docs/SESSION_HANDOFF_2026-10-10.md). **Tests CI et émulateur verts ; test Android physique non effectué ; H4 micronutriments sera le prochain axe métier.**

## Objectifs fonctionnels
1. Journal alimentaire local et cumul quotidien, unités contrôlées.
2. Scan de code-barres produit (Open Food Facts) et enrichissement nutritionnel (CIQUAL, USDA si droits et données disponibles).
3. Photographie des repas, détection des aliments et estimation des portions **avec confirmation humaine obligatoire**.
4. Bilan macro + micro-nutriments (valeurs disponibles, manquantes, incertaines ; ne jamais confondre inconnu et zéro).
5. Import de bilans biologiques PDF/photo avec contrôle manuel des valeurs, des unités et des références.
6. Recherche PubMed et base de connaissances scientifiques avec PMID/DOI, provenance, date, qualité des preuves, licence et réévaluation.
7. Suivi optionnel des habitudes (activité, tabac, café, poids) avec consentement.

## Principes
- **Privacy-by-design / local-first**. Aucune donnée de santé réelle, pièce jointe médicale, photographie personnelle ou secret dans Git/GitHub.
- Les propositions IA sont des **estimations** et ne constituent ni diagnostic ni prescription.
- Traçabilité : `source`, `observed_at`, `estimated_at`, `unit`, `confidence`, `reference_range` lorsque applicable.
- Distinguer `PRODUCT_DATA`, `FOOD_COMPOSITION_REFERENCE`, `VISUAL_ESTIMATE`, `LAB_RESULT_HUMAN_VERIFIED` et `SCIENTIFIC_EVIDENCE`.
- Mesures manquantes = `null / UNKNOWN`, jamais automatiquement 0.
- Aucun échange de données médicales avec un modèle externe sans consentement explicite et DPIA/évaluation des risques selon l'usage.

## Dépôt et frontières de responsabilité
- **L4 product owner** : Android, logique métier santé/nutrition, APIs, scientific evidence, tests et UX.
- `cadrage_202682030` : roadmap, statut et ownership.
- `maya-ai-agentic-architecture-reference` : patterns RAG, évaluations, gouvernance réutilisés en référence (pas de duplication).
- `shared-platform-services-openshift` : infrastructure IAM/OTel/GitOps **optionnelle**, non prérequis pour la version Android locale.

## Ordre de construction
| Gate | Fonctionnalité | Statut |
|---|---|---|
| H0 | Cadrage, ADR, contrats de données, squelette Android | DOCUMENTED |
| H1 | Journal Android, sauvegarde portable chiffrée, restauration et purge | EMULATOR_RUNTIME_PROVEN / PHONE_PENDING |
| H2 | Lecture Google Code Scanner et consultation Open Food Facts v3 ; préremplissage manuel du journal | CI_BUILD_PROVEN / PARSER_EMULATOR_PROVEN / SCAN_DEVICE_PENDING |
| H3 | Photos repas et vérification des portions, benchmark d'erreur | OPEN |
| H4 | Micronutriments et composition, références et couverture | OPEN |
| H5 | Import biologique et suivi longitudinal, validation humaine | OPEN |
| H6 | PubMed, mise à jour scientifique, déduplication, provenance | OPEN |
| H7 | Synthèse personnelle, tests sécurité/fiabilité, APK de démonstration | OPEN |

Consulter [ROADMAP](docs/ROADMAP.md), [ADR-0001](docs/adr/ADR-0001-android-local-first.md), [modèle de données](docs/architecture/DATA_CONTRACTS.md), [sources](docs/scientific-methodology/SOURCES.md) et [sécurité](docs/privacy-security/THREAT_MODEL.md).

## Extension H1 — 10 octobre 2026
- Export local chiffré à phrase secrète ; import avec confirmation d'écrasement ; purge du journal et de la clé Keystore.
- Tests Android d'intégration (Keystore, archive altérée, mauvais mot de passe, purge) ajoutés ; [workflow émulateur](.github/workflows/android-h1-runtime.yml).
- Aucun APK de production ni test sur ton téléphone n'est revendiqué. Pour éviter une perte de données à la mise à jour, consulte [la stratégie de signature](docs/SECURE_SIGNING_AND_UPDATE.md).

## Extension H2 — 10 octobre 2026
- Scanner EAN/UPC par Google Play Services (sans permission caméra directe) ; saisie manuelle alternative.
- Recherche explicite de produits Open Food Facts API v3 avec envoi du **code-barres uniquement**, contrôle du chiffre de vérification GTIN, provenance et date de consultation.
- Calories et macros manquantes restent `UNKNOWN`, et la portion est confirmée avant enregistrement ; la liaison de provenance persistante reste à développer.
- Ce mode utilise INTERNET lors de la recherche produit ; il n'envoie pas le journal alimentaire ni des bilans biologiques.
- Compilation H2 et six tests Android sur émulateur SUCCESS ; lecture réelle par caméra, requête réseau OFF en conditions réelles et provenance persistée à qualifier.

## Démarrage
Le module Android `apps/android/` offre un journal manuel avec stockage local AES-256-GCM/AndroidKeyStore, journal des aliments et suppression individuelle. Le build GitHub Actions du 09/10/2026 est **SUCCESS**, artefact APK debug généré. Les tests d'instrumentation Android Keystore sur émulateur sont SUCCESS (6/6 avec H2). Aucun test sur le téléphone réel de l'utilisateur ni parcours complet SAF UI n'est encore attesté.

## Limites
Ce dépôt est **public**. Il ne doit contenir que des jeux de données fictifs ou explicitement publics/licenciés. Pour toute expérimentation sur les bilans de l'utilisateur : privilégier un stockage privé chiffré en dehors de Git et une application installée localement.

## Preuves actuelles
- [H1/H2 : build et tests Android du 10 octobre 2026](evidence/H1_H2_ANDROID_VALIDATION_2026-10-10.md).
- APK debug Android 0.3.0 disponible en artifact GitHub CI, non signé comme release.
