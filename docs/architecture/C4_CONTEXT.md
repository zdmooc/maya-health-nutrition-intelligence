# Vue de contexte / frontières

```mermaid
flowchart TB
  U[Utilisateur Android] --> APP[App Kotlin Compose]
  APP --> DB[(Stockage local chiffré - H1)]
  APP --> DIARY[Journal alimentaire - H1]
  APP --> PHOTO[Photo repas + validation - H3]
  APP --> BAR[Scanner code barre - H2]
  APP --> LAB[Validation bilans - H5]
  APP --> VIEW[Tableaux tendances - H7]
  BAR --> OFF[Open Food Facts v3]
  PHOTO --> EST[IA visuelle optionnelle consentie]
  DIARY --> NUTRI[Moteur composition - H4]
  NUTRI --> CIQ[CIQUAL / licence à vérifier]
  NUTRI --> USDA[USDA FoodData Central / conditions API]
  LAB --> DB
  APP --> SCI[Moteur recherche sources - H6]
  SCI --> PUB[NCBI PubMed E-utilities]
  SCI --> KNOW[(Registre PMID/DOI et provenance)]
```

Toutes les flèches externes sont **DESIGN ONLY** à H0. Données personnelles isolées du registre de publications. L'analyse de photo ne vaut ni mesure précise du grammage ni dosage micronutritionnel.

## Backend futur (option)
API sécurisée/BFF, contrôle de consentement, worker ingestion scientifique, stockage privé isolé, vecteur RAG avec provenance, audit et rate limits. Pas de déploiement cloud ni Kubernetes tant que local-first satisfait le MVP.
