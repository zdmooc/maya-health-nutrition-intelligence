# Maya Health & Nutrition Intelligence

Application Android **personnelle** de suivi nutritionnel, d'analyse de bilans biologiques et de veille scientifique, basée sur des **preuves traçables**.

> **État au 2026-10-09 : H0 INITIALIZED / ARCHITECTURE_BASELINE — aucune application APK ni fonctionnalité de reconnaissance photo/code-barres/biologie testée.**

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
| H0 | Cadrage, ADR, contrats de données, squelette Android | IN PROGRESS |
| H1 | Journal manuel quotidien Android, stockage local sécurisé et tests | OPEN |
| H2 | Recherche/scan code-barres, traçabilité produit | OPEN |
| H3 | Photos repas et vérification des portions, benchmark d'erreur | OPEN |
| H4 | Micronutriments et composition, références et couverture | OPEN |
| H5 | Import biologique et suivi longitudinal, validation humaine | OPEN |
| H6 | PubMed, mise à jour scientifique, déduplication, provenance | OPEN |
| H7 | Synthèse personnelle, tests sécurité/fiabilité, APK de démonstration | OPEN |

Consulter [ROADMAP](docs/ROADMAP.md), [ADR-0001](docs/adr/ADR-0001-android-local-first.md), [modèle de données](docs/architecture/DATA_CONTRACTS.md), [sources](docs/scientific-methodology/SOURCES.md) et [sécurité](docs/privacy-security/THREAT_MODEL.md).

## Démarrage
Le module Android `apps/android/` est un **prototype compilable à qualifier** : journal manuel d'aliments en mémoire, sans sauvegarde persistante. Ouvrir le dossier dans Android Studio disposant de JDK 17, SDK Android 37 et Gradle compatible. Aucun APK n'a encore été produit ou testé sur un téléphone réel.

## Limites
Ce dépôt peut être **public**. Il ne doit contenir que des jeux de données fictifs ou explicitement publics/licenciés. Pour toute expérimentation sur les bilans de l'utilisateur : privilégier un stockage privé chiffré en dehors de Git et une application installée localement.
