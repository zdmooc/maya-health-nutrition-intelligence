# Sécurité et conformité — points bloquants avant données réelles

## Périmètre
Les habitudes alimentaires, photos de repas et bilans biologiques peuvent révéler des informations de santé personnelles. La présence sur un dépôt public est exclue pour les données réelles, captures, secrets, jetons, et jeux de tests dérivés d'un patient.

## Menaces / contrôles
| Menace | Contrôle requis | État |
|---|---|---|
| Commit de données patient | .gitignore + revue des commits + secret scanning + données synthétiques seules | PARTIAL |
| Vol du téléphone | Keystore + chiffrement et verrouillage application, sauvegarde maîtrisée | TODO H1 |
| Exfiltration vers modèle vision | Opt-in granulaire, minimisation, information sur destinataire, suppression, journal | TODO H3 |
| Mauvaise lecture bilan biologique | OCR comme brouillon, revue manuelle obligatoire, unité/référence/horodatage | TODO H5 |
| Recommandation hallucination | Citation, niveau de preuve, évaluations, limites et escalade vers professionnel | TODO H6 |
| Confusion micro-nutriment manquant/0 | `UNKNOWN`, couverture & qualité de données visibles | DESIGN |
| Fuite par logs/telemetry | redaction, désactivation collecte médicale, minimisation des traces | TODO |
| Sources scientifiques obsolètes | dates de révision, identification errata/retractions | TODO H6 |

## Règles
1. Dépôt public = code/documentation uniquement.
2. Aucun export de bilans/diagnostic dans des outils tiers par défaut.
3. Pas de traitement automatique prescriptif, ni conseil d'arrêt/changement de traitement.
4. Si l'usage se généralise ou devient commercial, analyser RGPD, AIPD/DPIA, contrats de sous-traitance, obligations de dispositif médical selon les finalités effectives et éventuel hébergement HDS.
5. Le prototype actuel ne persiste aucune donnée. **Ne pas saisir de données de santé réelles** tant que H1 sécurité n'est pas fermé.

## H1 — Sauvegarde portable (10/10)
Le journal est exporté sous forme de fichier chiffré, après choix de destination par l'utilisateur via Android Storage Access Framework. Secret dérivé PBKDF2-HMAC-SHA256 (210 000 itérations), sel aléatoire et AES-256-GCM. L'application ne détient pas de clé de récupération : mot de passe oublié = backup perdu. Export cloud choisi manuellement hors contrôle de l'application. Les fichiers externes exportés ne sont PAS supprimés par la purge locale. Revue du format, des comportements en cas d'interruption, des IME/claviers, des captures d'écran, des erreurs de stockage et des modèles Android requise avant usage de données réelles.
