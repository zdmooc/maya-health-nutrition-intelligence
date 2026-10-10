# H1 — Signature Android et stratégie de mise à jour

État 2026-10-10 : uniquement APK debug CI. Aucune release Play Store ni mise à jour signée durablement.

## Risque de signature
Les artefacts de développement GitHub Actions sont signés par une clé debug créée sur le runner. Une future compilation avec une clé différente ne pourra normalement pas se mettre à jour par-dessus l'APK installé. Une désinstallation préalable peut effacer le journal local.

## Procédure de livraison durable
1. Conserver la même identité applicative Android : com.maya.health.
2. Créer une clé release et sa copie de secours localement avec Android Studio/keytool, hors dépôt GitHub.
3. Stocker la clé et les mots de passe dans des secrets dédiés, avec contrôle d'accès strict ; ne jamais les écrire dans le code, les journaux CI, les issues ni des fichiers publics.
4. Implémenter une tâche de signature release, contrôles apksigner verify, SHA-256, permissions, versionCode et dépendances.
5. Tester mise à jour v1 vers v2 sans désinstallation, sur Android de test, avec journal fictif persistant.
6. Tester export chiffré, restauration sur une installation de test, puis procédure de migration depuis l'APK debug.
7. En cas de changement de signature, ne pas promettre de mise à jour transparente. Exiger une sauvegarde vérifiée avant toute réinstallation.

## Limites
- Aucune clé release ni secret ne sera créé à la place de l'utilisateur sans procédure de conservation approuvée.
- Aucun APK debug ne doit être présenté comme une release de production.
- La sécurité du stockage, les sauvegardes et la conformité exigent des tests sur appareil et une revue indépendante avant données médicales réelles.
