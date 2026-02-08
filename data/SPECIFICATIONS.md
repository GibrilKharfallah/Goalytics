# ⚽ Dataset 2 : Football Players - Joueurs de football

## 📋 Description

Statistiques de joueurs de football professionnels des principales ligues européennes.

## 📊 Structure des données

### Fichiers fournis

- **data_clean.json** : 100 joueurs, données parfaites
- **data_dirty.json** : 500 joueurs avec :
  - Champs manquants (marketValue, salary, club)
  - Valeurs négatives (age, goalsScored)
  - Positions invalides ("Attacker" au lieu de "Forward")
  - Âges impossibles (< 16 ou > 45)
  - Doublons (même ID)
- **data_large.json** : 10 000 entrées (joueurs + variations historiques)

## 🎯 Outputs attendus

### 1. Statistiques générales

**Fichier** : `results.json`

```json
{
  "statistics": {
    "total_players_parsed": ...,
    "total_players_valid": ...,
    "parsing_errors": ...,
    "duplicates_removed": ...
  },
  "top_10_scorers": [
    {
      "name": "...",
      "club": "...",
      "goals": ...,
      "matches": ...
    }
  ],
  "top_10_assisters": [...],
  "most_valuable_players": [...],
  "highest_paid_players": [...],
  "players_by_league": {
    "Premier League": ...,
    "La Liga": ...,
    "Serie A": ...,
    "Bundesliga": ...,
    "Ligue 1": ...
  },
  "players_by_position": {
    "Goalkeeper": ...,
    "Defender": ...,
    "Midfielder": ...,
    "Forward": ...
  },
  "average_age_by_position": {
    "Goalkeeper": ...,
    "Defender": ...,
    "Midfielder": ...,
    "Forward": ...
  },
  "average_goals_by_position": {
    "Forward": ...,
    "Midfielder": ...,
    "Defender": ...,
    "Goalkeeper": ...
  },
  "discipline_statistics": {
    "total_yellow_cards": ...,
    "total_red_cards": ...,
    "most_disciplined_position": "...",
    "least_disciplined_position": "..."
  }
}
```

### 2. Rapport texte

**Fichier** : `report.txt`

```
===============================================
   RAPPORT D'ANALYSE - JOUEURS DE FOOTBALL
===============================================

📊 STATISTIQUES DE PARSING
---------------------------
- Entrées totales lues      : ...
- Entrées valides           : ...
- Errées de parsing        : ...
- Doublons supprimés        : ...

⚽ TOP 10 - BUTEURS
-------------------
1. ...                      : ... buts en ... matchs
2. ...                      : ... buts en ... matchs
...

🎯 TOP 10 - PASSEURS
---------------------
1. ...                      : ... passes en ... matchs
2. ...                      : ... passes en ... matchs
...

💰 TOP 10 - VALEUR MARCHANDE
-----------------------------
1. ...                      : ... M€
2. ...                      : ... M€
...

💵 TOP 10 - SALAIRES
--------------------
1. ...                      : ... M€/an
2. ...                      : ... M€/an
...

🏆 RÉPARTITION PAR LIGUE
-------------------------
- Premier League            : ... joueurs
- La Liga                   : ... joueurs
- Serie A                   : ... joueurs
- Bundesliga                : ... joueurs
- Ligue 1                   : ... joueurs

⚽ RÉPARTITION PAR POSTE
------------------------
- Gardiens (Goalkeeper)     : ... joueurs
- Défenseurs (Defender)     : ... joueurs
- Milieux (Midfielder)      : ... joueurs
- Attaquants (Forward)      : ... joueurs

📊 MOYENNES PAR POSTE
----------------------
ÂGE MOYEN :
- Gardiens                  : ... ans
- Défenseurs                : ... ans
- Milieux                   : ... ans
- Attaquants                : ... ans

BUTS PAR MATCH (moyenne) :
- Attaquants                : ... buts
- Milieux                   : ... buts
- Défenseurs                : ... buts
- Gardiens                  : ... buts

🟨 DISCIPLINE
--------------
- Total cartons jaunes      : ...
- Total cartons rouges      : ...
- Poste le plus discipliné  : ...
- Poste le moins discipliné : ...

⏱️  PERFORMANCE
---------------
- Temps de traitement       : ... secondes
- Entrées/seconde           : ...

===============================================
```

## 🔧 Transformations à implémenter

### Obligatoires

1. **Parsing et validation**
   - Parser les 3 fichiers
   - Valider : age entre 16 et 45, goals >= 0, matches > 0
   - Positions valides : "Goalkeeper", "Defender", "Midfielder", "Forward"
   - Supprimer les doublons (par ID)

2. **Top 10**
   - Top 10 buteurs
   - Top 10 passeurs
   - Top 10 valeurs marchandes
   - Top 10 salaires

3. **Agrégations**
   - Compter joueurs par ligue
   - Compter joueurs par poste
   - Âge moyen par poste
   - Buts moyens par poste

4. **Statistiques de discipline**
   - Total de cartons jaunes et rouges
   - Poste le plus/moins discipliné

### Bonus (optionnel)

5. **Efficacité offensive**
   - Calculer ratio buts/matchs
   - Top 10 joueurs les plus efficaces (qui ont joué au moins 10 matchs)

6. **Meilleur rapport qualité/prix**
   - Calculer ratio buts/salaire (pour ceux qui ont un salaire)
   - Top 10 "bonnes affaires"

7. **Statistiques par ligue**
   - Âge moyen par ligue
   - Moyenne de buts par ligue
   - Ligue la plus productive

## 💡 Conseil - Mesure de performance

```scala
val start = System.currentTimeMillis()
// ... traitement
val duration = (System.currentTimeMillis() - start) / 1000.0
println(f"Traitement effectué en $duration%.3f secondes")
```

## ✅ Critères de réussite

- ✅ Les 3 fichiers sont parsés sans erreur fatale
- ✅ Les positions invalides sont normalisées ou rejetées
- ✅ Les données aberrantes sont filtrées (âges impossibles, stats négatives)
- ✅ Les 10 statistiques obligatoires sont présentes
- ✅ `results.json` est valide et bien formaté
- ✅ `report.txt` est lisible et structuré
- ✅ Le traitement de `data_large.json` prend < 10 secondes



