# Roadmap produit — H0 à H7

État au 2026-10-09 : **H0 DOCUMENTED / ANDROID_SCAFFOLD_UNBUILT**. Aucune preuve d'APK, de scanning réel, de données d'aliments intégrées ni de suivi biologique exécuté.

| Gate | Scope | Critère d'acceptation | Statut |
|---|---|---|---|
| H0 | Contrats, ADR, privacy, Android minimal | Sources, frontières, Android sources disponibles | DOCUMENTED |
| H1 | Journal Android personnel | CRUD local chiffré, journée/fuseau, tests et APK installé sur téléphone | OPEN |
| H2 | Codes-barres | Scan en direct, EAN vérifié, OFF API v3, gestion produit absent et provenance | OPEN |
| H3 | Photos repas | Identification multi-aliments, grammages ajustés humainement, test d'erreur vs pesée | OPEN |
| H4 | Nutrition complète | Ciqual/USDA, unités macro/micronutriments, couverture par aliment, incertitudes, licence | OPEN |
| H5 | Biologie | PDF+photo, unité, intervalle de référence, validation humaine, courbes longitudinales | OPEN |
| H6 | Recherche scientifique | PubMed via E-utilities, PMID/DOI, licence, date, synthèse sourcée, veille, correction et retrait | OPEN |
| H7 | Synthèse & hardening | UX accessible, suivi optionnel des habitudes, audit sécurité, performance, distribution APK | OPEN |

## Definition of Done commune
- [ ] Sources exactes, licences, dates et provenance associées aux mesures.
- [ ] Données sensibles jamais exportées en logs ou à un fournisseur externe sans autorisation.
- [ ] Tests automatisés + revue humaine des extraits scientifiques/biologiques.
- [ ] Capacité à supprimer/exporter localement ses données.
- [ ] Documentation reproductible et preuve d'exécution, distinguer `STATIC_VALIDATED`/`ANDROID_BUILD_PROVEN`/`DEVICE_RUNTIME_PROVEN`.
- [ ] Les fonctions déployées uniquement quand vérifiées ; pas de promesse de diagnostic ni thérapeutique.

## Exécution prioritaire
1. H1 : installation Android réelle, persistance chiffrée, journal et navigation.
2. H2 : barcode + OFF, gestion des données manquantes et cache.
3. H4 : moteur de composition reproductible avant toute recommandation personnalisée.
4. H3 : computer vision avec estimation vs pesée et consentement d'upload.
5. H5 : biologie, unit tests, bornes valeurs biologiques, revue systématique.
6. H6 : article ingestion, sources, preuve RAG et alertes de retrait.
7. H7 : tableaux de bord et validation transverse.

L'ordre H4/H3 peut être inversé uniquement sur preuve ou besoin explicite. Aucun OpenShift/Cloud obligatoire pour MVP Android local.
