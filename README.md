# Projet final Scala (Programmation Fonctionnelle) — Pipeline ETL (Football Players)

**Réalisé par Gibril KHARFALLAH & Ward KHALIFE**

Pipeline ETL complet en Scala 3 pour l'analyse de données de joueurs de football.

## 📊 Dataset choisi

**Football Players** (`2-football-players`)

Le projet implémente un pipeline ETL complet :

- **Extract** : Lecture de fichier et parsing JSON avec Circe
- **Transform** : Validation, nettoyage, suppression des doublons, calcul de statistiques (HOF)
- **Load** : Export des résultats en JSON et génération d'un rapport texte

---

## 🔧 Prérequis

- Java 17+ (testé avec Java 21)
- SBT 1.9+
- Scala 3.3.x

---

## 🚀 Installation et exécution

### Compiler le projet

```bash
sbt compile
```

### Exécuter sur les 3 fichiers (par défaut)

```bash
sbt run
```

Par défaut, le programme traite :

- `data/data_clean.json`
- `data/data_dirty.json`
- `data/data_large.json`

### Exécuter sur un fichier spécifique

```bash
sbt "run data/data_dirty.json"
```

---

## 📁 Fichiers de sortie

Le programme génère un dossier par dataset traité :

```bash
output/
├── clean/
│   ├── results.json
│   └── report.txt
├── dirty/
│   ├── results.json
│   └── report.txt
└── large/
    ├── results.json
    └── report.txt
```

### Contenu de `results.json`

Le JSON exporté respecte la structure attendue et contient :

- **statistics** : Compteurs globaux (entrées, valides, erreurs de parsing, doublons)
- **top_10_scorers** : Les 10 meilleurs buteurs
- **top_10_assisters** : Les 10 meilleurs passeurs
- **most_valuable_players** : Joueurs ayant la plus grande valeur marchande
- **highest_paid_players** : Joueurs les mieux payés
- **players_by_league** : Répartition des joueurs par championnat
- **players_by_position** : Répartition des joueurs par poste
- **average_age_by_position** : Âge moyen par poste
- **average_goals_by_position** : Moyenne de buts par poste
- **discipline_statistics** : Statistiques disciplinaires

**Stats bonus :**

- **top_goal_contribution_per_match** : Meilleure contribution offensive par match
- **top_discipline_risk_per_match** : Risque disciplinaire par match
- **best_value_for_money** : Meilleur rapport qualité/prix

### Contenu de `report.txt`

Le rapport texte est une version lisible des résultats. En plus des tableaux (Top 10, répartitions, moyennes, discipline, etc.), il affiche les compteurs de qualité de données :

- Total d'entrées (JSON)
- Nombre d'erreurs de parsing
- Nombre d'objets invalides (validation)
- Nombre de doublons supprimés
- Total d'entrées valides

Le rapport affiche également :

- Le temps de traitement
- Le débit (entrées/seconde)

---

## 🏗️ Architecture du projet

Le projet est organisé en modules simples et testables :

### `Main.scala`

Point d'entrée. Exécute le pipeline sur un ou plusieurs fichiers et route la sortie dans `output/<label>/`.

### `Parsing.scala`

Lecture de fichier et parsing JSON. Deux modes :

- Parsing "strict" (tout ou rien)
- Parsing robuste entrée-par-entrée (`parsePlayersWithErrors`) qui conserve les entrées décodables et compte les erreurs

### `Validation.scala`

Validation métier et conversion `PlayerRaw` → `Player`. Comptage des invalides et dédoublonnage.

### `Stats.scala`

Calcul des statistiques via HOF (`map`, `filter`, `groupBy`, `foldLeft`, `sortBy`, etc.). Contient aussi les stats bonus.

### `ResultsModels.scala`

Modèles de sortie du JSON (contrat d'export).

### `ResultsBuilder.scala`

Construit l'objet `Results` à partir de `CleanData` et des stats.

### `JsonCodecs.scala`

Encodage JSON via Circe (generic derivation).

### `ResultsWriter.scala`

Écriture de `results.json` (création des dossiers si nécessaire).

### `ReportWriter.scala`

Génération de `report.txt` (format lisible + compteurs + performance).

---

## 💡 Choix techniques importants

### Parsing robuste pour `data_dirty.json`

Pour éviter qu'une entrée JSON invalide fasse échouer tout le parsing, le projet :

- Parse le JSON racine en tableau
- Tente le décodage Circe élément par élément
- Incrémente un compteur `parsingErrors` en cas d'échec

Cela permet de séparer clairement :

- **Parsing errors** : Entrées non décodables
- **Invalid objects** : Entrées décodées mais rejetées par la validation métier

### Validation (`Either` + for-comprehension)

La validation utilise `Either[String, Player]` pour :

- Rendre les échecs explicites
- Composer les règles avec `for` (approche fonctionnelle)

### Scalabilité clean/dirty/large

La case class `PlayerRaw` est adaptée au schéma clean et reste stable :

- Les champs susceptibles d'être manquants sont en `Option`
- Le dirty est géré via :
  - Parsing robuste
  - Validation
  - Dédoublonnage
- Sans modifier la case class selon le fichier d'entrée

### Performance

Le programme affiche le temps de traitement pour chaque fichier, ainsi que le débit (entrées/seconde). L'objectif est d'être performant sur `data_large.json` en évitant les opérations inutiles et en gardant les traitements en mémoire simples.

---

## 🚧 Difficultés rencontrées et solutions

### 1. Dirty dataset : échec complet du parsing

**Problème** : Un seul item invalide peut faire échouer le décodage d'une liste complète.

**Solution** : Parsing robuste élément-par-élément avec compteur d'erreurs (`parsePlayersWithErrors`).

### 2. Comptage cohérent des indicateurs

**Problème** : Distinguer parsing errors / invalid data / duplicates.

**Solution** :

- **parsing errors** : Erreurs de décodage
- **invalidCount** : Validation métier
- **duplicatesRemoved** : Suppression après validation

### 3. Sorties par dataset

**Problème** : Écraser les outputs à chaque exécution.

**Solution** : Routing vers `output/clean`, `output/dirty`, `output/large`.

---

## 🎯 Bonus : Statistiques créatives

Pour se différencier, trois statistiques avancées ont été implémentées :

- **Goal contribution / match** : `(goals + assists) / matches`
- **Discipline risk / match** : `(yellow + 3 × red) / matches`
- **Value for money** : `(goals + assists) / salary`

Ces stats sont exportées dans `results.json` et affichées dans `report.txt`.

---

## 📝 Commandes utiles

### Compiler

```bash
sbt compile
```

### Lancer sur tous les datasets

```bash
sbt run
```

### Lancer sur le dataset dirty uniquement

```bash
sbt "run data/data_dirty.json"
```

### Nettoyer et relancer (utile après modifications structurelles)

```bash
sbt clean run
```

---
