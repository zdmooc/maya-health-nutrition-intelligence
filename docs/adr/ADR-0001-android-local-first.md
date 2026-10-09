# ADR-0001 — Produit Android local-first, non médical

- Date : 2026-10-09
- Statut : ACCEPTED FOR INITIAL DESIGN
- Classification : L4 PRODUCT, indépendant de MayaBank Payments

## Contexte
Le périmètre combine nutrition, photos de repas, codes-barres, micronutriments, bilans biologiques et littérature scientifique. Les données de santé sont hautement sensibles. Le prototype peut évoluer sans infrastructure cloud initiale.

## Décision
1. Android natif Kotlin + Jetpack Compose pour la première interface.
2. Journal de saisie manuelle d'abord ; caméra/scan et PDF importés de façon incrémentale.
3. Base locale chiffrée, chiffrement des sauvegardes et clé gérée par Android Keystore **à implémenter avant persistance de données réelles**.
4. Moteur de nutrition déterministe, provenance systématique, inconnus explicitement affichés.
5. Interface optionnelle backend pour OFF, sources de composition, PubMed et éventuellement modèles visuels. Pas d'upload de photo/donnée biologique par défaut.
6. Qualifier chaque modèle/estimation par jeux de tests et incertitudes ; pas de diagnostic, prescription ou objectif de santé calculé automatiquement.
7. Dépôt peut rester public pour le **code uniquement** ; envisager GitHub privé si souhaité, mais ne jamais y stocker de données réelles.

## Alternatives écartées pour la V0
- Un backend Kubernetes/OpenShift dès le départ (coût/surface d'attaque inutiles).
- Une application RAG générale distincte de l'owner produit (doublon de la plateforme IA).
- Estimer les micro-nutriments uniquement depuis les pixels.
- Synchronisation de bilans ou de photos dans le dépôt GitHub/Drive à l'insu de l'utilisateur.

## Conséquences
- Offline mode minimal mais import scientific/update nécessite connexion.
- Compilations et tests Android doivent être prouvés avec un SDK/émulateur/téléphone ; aucun runtime déclaré à H0.
- Stockage local actuel du prototype = uniquement données de démonstration en mémoire, détruites au redémarrage.
