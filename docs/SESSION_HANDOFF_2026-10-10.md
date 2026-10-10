# Point de reprise — Maya Health — 10 octobre 2026

**Décision utilisateur : suspendre la session aujourd'hui ; reprendre ultérieurement.** Aucun travail futur n'est exécuté automatiquement. Ce fichier et la roadmap conservent l'état et les prochaines actions.

## Source de vérité
- Produit L4 autonome : https://github.com/zdmooc/maya-health-nutrition-intelligence
- Gouvernance : https://github.com/zdmooc/cadrage_202682030/blob/main/portfolio/HEALTH_NUTRITION_ANDROID_PRODUCT_2026-10-09.md
- Documentation H1/H2 : [evidence/H1_H2_ANDROID_VALIDATION_2026-10-10.md](../evidence/H1_H2_ANDROID_VALIDATION_2026-10-10.md)
- Baseline de code H1/H2 : `885508d6c741ba95e7ff9382afbfea1a73221096`.
- Dernier commit documentaire constaté avant clôture : `d49d062cc1140ba192afa84a35b39006deb712c3`.
- CI sur ce dernier commit : [build et tests JVM SUCCESS, run 38042616607](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/38042616607) ; [tests Android émulateur SUCCESS, run 38042616561](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/38042616561).
- CI baseline H1/H2 : [APK construit SUCCESS, run 38042295457](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/38042295457) ; [6/6 tests instrumentation sur Android API 35, run 38042295500](https://github.com/zdmooc/maya-health-nutrition-intelligence/actions/runs/38042295500).
- Baseline APK debug 0.3.0-h2-dev : `f0eae58b284424433bb11189d6228d07efb5c67a4f8710df41568a3e4da4f159` (SHA-256 ; artefact GitHub Actions à durée limitée). Aucun release APK signé durablement.

## Matrice des gates au moment de la suspension
| Gate | État prouvé | Restant |
|---|---|---|
| H0 | DOCUMENTED | Rien de bloquant pour continuer |
| H1 | EMULATOR_RUNTIME_PROVEN : journal manuel, AES-GCM Keystore, sauvegarde chiffrée, import/purge (tests des fonctions crypto et repository sur émulateur) | PHONE_PENDING : installation, parcours UI de sélection SAF et restauration réelle, test sauvegarde avant migration d'APK, signature release durable |
| H2 | BUILD_PROVEN / PARSER_EMULATOR_PROVEN : Google Code Scanner intégré, recherche Open Food Facts v3, vérification GTIN, données manquantes affichées comme inconnues | PHONE_SCAN_PENDING : test vraie caméra + Google Play Services, vrai appel OFF et erreurs réseau ; conservation durable de la provenance dans FoodEntry |
| H3 | OPEN | Reconnaissance photo repas, évaluation de précision vs poids vérifiés, consentement |
| H4 | OPEN | Tables CIQUAL/USDA, moteur déterministe micro/macro, unités, incertitudes, couverture et licences |
| H5 | OPEN | Import bilans PDF/image, normalisation unités, intervalles labo, confirmation humaine, historique, sécurité |
| H6 | OPEN | PubMed E-utilities, métadonnées PMID/DOI, veille, rétractations, qualité scientifique et RAG sourcé |
| H7 | OPEN | Dashboard personnalisé, habitudes, évaluation complète, distribution et audit sécurité |

## Plan de reprise
1. **Première action utilisateur nécessaire :** tester l'APK sur un téléphone Android compatible ; n'utiliser que des aliments fictifs et **ne pas désinstaller un APK avec des données sans sauvegarde restaurée et vérifiée**. Tester reprise après fermeture, ajout/suppression, export SAF, import sur installation de test, effacement, puis EAN valide avec et sans réseau.
2. Corriger tout bug issu de ces tests ; conserver une trace uniquement **synthétique** dans `evidence/`.
3. Mettre en place une **signature de release stable** et la validation upgrade `vN -> vN+1` sans perte des données ; aucun secret dans GitHub. Guide : [docs/SECURE_SIGNING_AND_UPDATE.md](SECURE_SIGNING_AND_UPDATE.md).
4. Finaliser H2 en stockant code-barres, source, date, portions et couverture/qualité de la composition dans des contrats versionnés sans inventer les micronutriments.
5. Ensuite attaquer **H4 en priorité métier**, puisque l'objectif central est de suivre vitamines, minéraux, calories, fibres et incertitude, en partant de CIQUAL et OFF/USDA dans le respect des licences.
6. Revenir ensuite à H3 photos, H5 bilans et H6 base scientifique, puis H7.

## Frontières impératives
- Dépôt GitHub public = **code, documentation et données synthétiques uniquement**. Aucun PDF biologique, photo alimentaire personnelle, résultat de laboratoire, mot de passe, secret, clé Android ou donnée personnelle dans GitHub, CI et issues.
- L'app ne remplace pas un professionnel de santé et n'émet pas de diagnostic automatique ; toutes les valeurs extraites de laboratoire doivent être revues par une personne.
- Aucun backend cloud ni infrastructure OpenShift requis pour reprendre H1/H2.
- Ne jamais confondre BUILD_PROVEN, EMULATOR_RUNTIME_PROVEN et PHONE_RUNTIME_PROVEN. Ne pas déclarer H1 ou H2 CLOSE avant preuve réelle.

## Instruction de reprise
Ouvrir ce fichier, relire `docs/ROADMAP.md`, vérifier `main` et les derniers runs CI ; reprendre exactement au **test Android physique H1/H2**, puis au **moteur micronutritionnel H4**, sans recommencer H0 ni créer un dépôt générique.
