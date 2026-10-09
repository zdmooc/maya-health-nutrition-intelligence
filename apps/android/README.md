# Android H0 — développement local

Code Kotlin + Jetpack Compose, sans permission internet ni caméra. Ce prototype **n'a pas encore été compilé ni installé**.

- Android Gradle Plugin **9.3.0**, Gradle **9.5.0 minimum**, JDK **17**, SDK **37**. Versions vérifiées contre la documentation officielle Android Developers (octobre 2026).
- Compose BOM **2026.08.00**, Activity Compose **1.13.0**.
- Android Studio : ouvrir `apps/android/`, configurer Gradle 9.5.0 et SDK 37, exécuter `:app:assembleDebug`, puis installer sur un appareil de test.
- Le dépôt ne contient pas encore le binaire Gradle Wrapper ; le build exige une installation de Gradle/Android Studio configurée.
- La seule fonction H0 est l'ajout temporaire d'un aliment fictif et du nombre de calories donné par l'utilisateur. Les entrées disparaissent lorsque le processus redémarre.
- Caméra, code-barres, calcul fiable, persistance, chiffrement, PDF, analyse biologique, PubMed et RAG : **non implémentés**.

Références : https://developer.android.com/build/releases/agp-9-3-0-release-notes et https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
