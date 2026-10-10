# Data contracts v0.1 — valeurs, unités, provenance

## Invariants
- Toute valeur est un couple nombre+unité explicite, jamais valeur non typée.
- `null` = inconnue/non publiée ; `0` = mesure ou composition réellement égale à zéro.
- `measured` vs `estimated` vs `user_entered` vs `derived`.
- Horodatage ISO-8601, fuseau et date locale pour la journée nutritionnelle.
- Ne jamais interpoler les valeurs biologiques entre deux prélèvements.

## FoodEntry
```json
{
  "id": "synthetic-uuid",
  "meal_time": "2026-10-09T12:00:00+02:00",
  "food": "repas fictif",
  "portion_g": 100,
  "portion_source": "USER_CONFIRMED",
  "source": {"kind": "FOOD_COMPOSITION_REFERENCE", "provider": "TEST_ONLY", "reference": "synthetic-fixture"},
  "nutrients": [
    {"code": "energy", "value": 100, "unit": "kcal", "basis": "per_portion", "confidence": "USER_ENTERED"},
    {"code": "magnesium", "value": null, "unit": "mg", "basis": "per_portion", "confidence": "UNKNOWN"}
  ],
  "is_synthetic": true
}
```

## ProductObservation
```json
{"barcode": "TEST-EAN", "product_name": null, "source": "OPEN_FOOD_FACTS", "api_version": "v3", "retrieved_at": null, "nutrition_completeness": "UNKNOWN"}
```

## LabObservation
```json
{"analyte": "ldl_cholesterol", "value": null, "unit": "g/L", "sampled_at": null, "laboratory_reference": null, "human_verified": false, "source_document_id": null}
```
- Rien à utiliser pour un tableau médical avant `human_verified=true`.
- L'intervalle de référence appartient au document et au contexte du prélèvement, pas à un seuil codé en dur.
- Le moteur ne peut poser un diagnostic en automatique.

## ScientificEvidence
```json
{"pmid": null, "doi": null, "title": "", "publication_date": null, "source_type": "journal_article", "retrieved_at": null, "license": "UNKNOWN", "evidence_grade": "UNASSESSED", "retraction_status": "UNKNOWN", "human_reviewed": false}
```
- Sans contenu sous licence de réutilisation explicite, stocker uniquement métadonnées, liens et notes originales.
- Versionner index, date de dernière revue, conflits entre études et niveau de preuve.

## Agrégats et couverture
Somme journalière : uniquement pour éléments mesurés/estimés disponibles. Afficher à côté `coverage_ratio` (fraction des aliments avec ce nutriment renseigné) et `is_partial=true` si manque de données. Un total partiel est **au minimum des apports connus**, pas une mesure complète.

## H2 — Produit scanné, état de conception
La consultation Open Food Facts v3 est séparée de la persistance du journal. `ProductSnapshot` porte `code`, `name`, `brand`, `energyKcal100g`, `proteinG100g`, `fatG100g`, `carbsG100g`, `capturedAt`, `source`. Toute valeur absente est `null` : pas de zéro fictif. Le nom et les calories pour une portion préremplissent la saisie manuelle uniquement après confirmation des grammes. Le schéma de la base H1 ne conserve pas encore la provenance OFF du produit ; **H2 n'est pas clos** sans cette évolution et des tests sur appareil Android.
