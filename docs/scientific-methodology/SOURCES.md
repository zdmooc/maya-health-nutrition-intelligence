# Sources scientifiques et alimentaires — registry initial

| Source | Rôle | Limites | Référence |
|---|---|---|---|
| Open Food Facts | Catalogue de produits et nutri-table étiquetée | base collaborative, completeness variable, licence ODbL, rate limits, User-Agent, API v3 recommandée | https://github.com/openfoodfacts/openfoodfacts-server/blob/main/docs/api/index.md |
| CIQUAL (ANSES) | Tables françaises de composition des aliments | vérifier licence, millésime, disponibilité de chaque micronutriment, qualité | https://ciqual.anses.fr/ |
| USDA FoodData Central | Composition complémentaire, surtout hors produits français | définitions des aliments, millésimes, API key et conditions d'usage à contrôler | https://fdc.nal.usda.gov/api-guide |
| PubMed NCBI E-utilities | Métadonnées scientifiques avec PMID, DOI, dates et types d'études | limite sans clé 3 requêtes/s, articles ≠ consensus, pas de copie massive d'abstracts | https://www.ncbi.nlm.nih.gov/books/NBK25497/ |

## Pipeline bibliographique prévu
`query -> ESearch -> ESummary/EFetch -> normalize PMID/DOI -> deduplicate -> store metadata -> assess methodology -> update/retraction checks -> provenance-cited RAG -> human review`.

- Stocker date de consultation, requête, exclusions et versions.
- En cas d'incohérence, indiquer le désaccord des sources et un niveau de certitude plutôt que combler les lacunes par une réponse générée.
- Réévaluer périodiquement et ne pas promouvoir automatiquement une nouvelle étude isolée en conseil médical.
- Vérifier la réutilisation des textes/figures sous droit d'auteur avant indexation intégrale.
- Distinguer données nutritionnelles étiquetées, composition analytique et estimation photographique.
