# Scénarios d'acceptation H1–H7

| ID | Scénario | Assertion |
|---|---|---|
| T01 | Ajouter repas manuel fictif 100 kcal + 200 kcal | total = 300 kcal, source manuelle |
| T02 | Aliments sans magnésium renseigné | magnésium = INCONNU, pas 0 mg |
| T03 | Produit code-barres introuvable | alerte « produit absent », saisie manuelle possible |
| T04 | Photo de repas mélange 3 aliments | proposition modifiable ; pas de quantité imposée |
| T05 | Café/soupe liquide | unité g/mL contrôlée, pas de conversion sans densité |
| T06 | Bilan mmol/L vs g/L et bornes différentes | ne compare pas avant normalisation validée |
| T07 | PDF médical OCR 1,57 -> 157 | erreur détectée par revue explicite ; pas de sauvegarde approuvée |
| T08 | Article PubMed retiré | marque RETRACTED, ne cite pas comme preuve active |
| T09 | Aliment incomplet | couverture nutritive visible dans tous les totaux |
| T10 | Déconnexion réseau | journal existant utilisable ; données externes en attente |
| T11 | Demande suppression données | suppression vérifiable (base, cache, exports contrôlés) |
| T12 | Réinstallation / appareil volé | pas de secrets persistants ni exports non chiffrés par défaut |

À H1 : tests JVM exécutés dans la CI ; tests Android d'intégration ajoutés pour Keystore, mot de passe de sauvegarde, corruption et purge ; leur résultat sur émulateur et téléphone doit être vérifié. Les tests H2–H7 restent prospectifs.

## Tests supplémentaires H1
- T13 : archive protégée par mot de passe, aucune donnée fictive en clair.
- T14 : mauvais mot de passe/fichier altéré => import rejeté et aucune modification des données locales.
- T15 : suppression intégrale => fichier chiffré et alias Keystore effacés, journal neuf utilisable.
- T16 : signature release stable et migration de version sans désinstallation => test Android réel restant.
