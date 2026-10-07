# Elestya Bedrock Menus

Plugin Paper permettant de proposer aux joueurs **Minecraft Bedrock** des menus de guilde natifs via **Floodgate / Cumulus**.

Le projet a été réalisé dans le cadre du test technique Elestya.

L'objectif est de reproduire les principales fonctionnalités des menus de guilde Java sous forme de formulaires Bedrock, tout en gardant une séparation complète entre l'interface utilisateur et la source de données.

## Prérequis

- Java 21
- Maven
- Paper 1.21.11
- Geyser-Spigot
- Floodgate

## Compilation

```bash
mvn clean package
```

Le fichier `.jar` généré se trouve ensuite dans :

```text
target/
```

## Installation

Placer le plugin compilé dans le dossier :

```text
plugins/
```

Le serveur doit également disposer de :

```text
Geyser-Spigot
Floodgate
```

Floodgate est déclaré comme dépendance du plugin afin de garantir que son API soit disponible avant le chargement du plugin.

## Architecture

Le projet est séparé en plusieurs parties :

```text
api/
├── GuildSource
├── domain/
│   ├── Guild
│   ├── GuildMember
│   ├── GuildRank
│   ├── GuildPermission
│   └── JoinRequest
└── result/
    └── ActionResult

source/
└── memory/
    ├── MemoryGuildSource
    └── MemoryGuildSeeder

menu/
├── FormService
└── guild/
    ├── GuildMenuService
    ├── GuildMainForm
    ├── GuildListForm
    ├── GuildMembersForm
    ├── GuildMemberForm
    ├── GuildBankForm
    ├── GuildJoinRequestsForm
    ├── GuildPermissionsForm
    └── GuildLeaveConfirmForm
```

## `GuildSource`

`GuildSource` représente le contrat entre les menus Bedrock et le système de guildes.

Les formulaires ne connaissent pas l'implémentation utilisée pour stocker ou modifier les données.

Ils utilisent uniquement cette interface pour :

- récupérer les guildes ;
- trouver la guilde d'un joueur ;
- créer une guilde ;
- demander à rejoindre une guilde ;
- accepter ou refuser une demande ;
- quitter une guilde ;
- déposer ou retirer de l'argent ;
- promouvoir ou rétrograder un membre ;
- expulser un membre ;
- consulter les permissions d'un rang ;
- vérifier si un joueur possède une permission ;
- modifier les permissions d'un rang.

Cette séparation permet de remplacer facilement l'implémentation de test par le système de données réel d'Elestya sans modifier les formulaires.

Exemple :

```text
Guild forms
     │
     ▼
GuildSource
     │
     ├── MemoryGuildSource
     │
     └── Future Elestya implementation
```

## Source mémoire

`MemoryGuildSource` est une implémentation en mémoire de `GuildSource`.

Elle ne nécessite aucune base de données.

`MemoryGuildSeeder` initialise environ vingt guildes fictives afin de pouvoir tester les différents parcours directement en jeu.

Les données de démonstration contiennent notamment :

- plusieurs guildes ;
- des chefs ;
- des adjoints ;
- des membres ;
- des recrues ;
- différents soldes ;
- des demandes d'adhésion ;
- différentes configurations de permissions.

Les données sont perdues au redémarrage du serveur, ce qui est volontaire pour cette implémentation de test.

## Rangs

Quatre rangs sont disponibles :

```text
Chef
Adjoint
Membre
Recrue
```

### Chef

Il ne peut y avoir qu'un seul chef par guilde.

Le chef possède automatiquement tous les droits et peut modifier les permissions des autres rangs.

### Adjoint

Ses permissions sont configurables par le chef.

### Membre

Ses permissions sont configurables par le chef.

### Recrue

Une recrue correspond au rang attribué à un joueur lorsqu'une demande d'adhésion est acceptée.

Ses permissions sont également configurables par le chef.

## Permissions

Chaque guilde possède sa propre configuration de permissions.

Les permissions ne sont donc pas codées en dur par rang.

Les huit permissions disponibles sont :

- Construire
- Ouvrir les conteneurs
- Inviter / recruter
- Revendiquer des chunks
- Expulser
- Gérer le warp
- Banque et améliorations
- Réserve

Par exemple, deux guildes peuvent donner des droits complètement différents à leurs membres :

```text
Guilde A
Membre
├── Construire
└── Ouvrir les conteneurs

Guilde B
Membre
├── Construire
├── Ouvrir les conteneurs
├── Inviter / recruter
└── Gérer le warp
```

Le chef possède toujours toutes les permissions.

## Gestion des permissions

Le chef peut ouvrir un menu dédié et sélectionner :

```text
Adjoint
Membre
Recrue
```

Chaque permission est ensuite représentée par un toggle Bedrock natif.

Les modifications sont effectuées via `GuildSource`.

Le formulaire ne modifie jamais directement les données de la guilde.

## Menus Bedrock

Les menus utilisent les formulaires natifs fournis par Cumulus.

Trois types de formulaires sont utilisés :

- `SimpleForm`
- `ModalForm`
- `CustomForm`

## Menu principal

Le menu principal d'une guilde permet d'accéder à :

```text
Membres
Banque
Demandes d'adhésion
Permissions
Quitter la guilde
```

Les boutons disponibles dépendent du rang et des permissions du joueur.

Par exemple, le menu des demandes d'adhésion n'est accessible qu'aux joueurs disposant du droit de recrutement.

La gestion des permissions est réservée au chef.

## Gestion des membres

Le menu des membres affiche :

- le nom du joueur ;
- son rang.

Une fiche individuelle peut ensuite proposer, selon les droits du joueur :

- promouvoir ;
- rétrograder ;
- expulser.

La progression des rangs est actuellement :

```text
Recrue
  ↓
Membre
  ↓
Adjoint
```

Le rang de Chef n'est pas attribuable par promotion.

La promotion et la rétrogradation sont réservées au chef.

L'expulsion utilise la permission dédiée `KICK`.

Une confirmation est demandée avant toute expulsion.

## Banque

Le menu banque affiche le solde actuel de la guilde.

Tous les membres peuvent déposer de l'argent.

Le retrait nécessite la permission :

```text
Banque et améliorations
```

Les montants sont saisis dans un `CustomForm`.

Les entrées invalides sont gérées avant l'envoi de l'action à `GuildSource`.

Les règles métier restent néanmoins vérifiées une seconde fois par la source de données.

## Demandes d'adhésion

Un joueur sans guilde peut consulter la liste des guildes et envoyer une demande d'adhésion.

Un joueur possédant la permission :

```text
Inviter / recruter
```

peut consulter les demandes de sa guilde et :

- les accepter ;
- les refuser.

Lorsqu'une demande est acceptée, le joueur rejoint la guilde avec le rang :

```text
Recrue
```

Les autres demandes en attente du joueur sont supprimées après son entrée dans une guilde.

Un joueur peut avoir au maximum cinq demandes d'adhésion simultanées.

## Joueur sans guilde

Un joueur Bedrock ne possédant aucune guilde arrive sur la liste des guildes disponibles.

Il peut :

- consulter une guilde ;
- consulter son nombre de membres ;
- envoyer une demande d'adhésion ;
- créer sa propre guilde.

Lorsqu'une guilde est créée, le joueur devient automatiquement son Chef.

## Quitter une guilde

Un membre peut quitter sa guilde depuis le menu principal.

Une confirmation est demandée avec un `ModalForm`.

Le Chef ne peut actuellement pas quitter sa guilde.

## Résultats des actions

Toutes les actions passent par `GuildSource` et retournent un :

```text
ActionResult
```

Un résultat contient :

```text
success
message
```

Cela permet au backend de décider si une action est autorisée ou refusée et de fournir directement le message à afficher au joueur.

Les formulaires ne reproduisent donc pas les règles métier.

Exemple de flux :

```text
Bedrock form
    ↓
GuildSource.withdraw(...)
    ↓
permission valide ?
montant valide ?
solde suffisant ?
    ↓
ActionResult
    ↓
message affiché au joueur
```

Tous les messages visibles par les joueurs sont en français.

## `FormService`

`FormService` centralise les interactions avec Floodgate.

Il permet notamment de :

- vérifier qu'un joueur est un joueur Bedrock ;
- envoyer un formulaire Cumulus.

Cela évite que chaque menu utilise directement `FloodgateApi`.

## Point d'entrée

Le point d'entrée public demandé pour l'intégration est :

```java
ouvrirMenuGuilde(Player player)
```

Il détermine automatiquement quel menu afficher.

```text
ouvrirMenuGuilde(player)
        ↓
joueur Bedrock ?
        ↓
possède une guilde ?
   ├── oui → menu principal
   └── non → liste des guildes
```

L'intégration n'a donc pas besoin de connaître les différentes classes de formulaires.

## Commande de test

Une commande temporaire est disponible pour faciliter les tests :

```text
/guildmenu
```

Elle appelle directement le point d'entrée :

```text
ouvrirMenuGuilde(Player)
```

Elle permet de tester les menus sans dépendre d'un autre plugin.

## Tests

Les différents parcours ont été testés directement depuis un client Minecraft Bedrock connecté au serveur via Geyser et Floodgate.

Ont notamment été testés :

- ouverture des formulaires Bedrock ;
- création de guilde ;
- consultation des guildes ;
- demandes d'adhésion ;
- acceptation et refus des demandes ;
- navigation entre les menus ;
- consultation des membres ;
- promotion ;
- rétrogradation ;
- expulsion ;
- confirmation d'expulsion ;
- dépôt bancaire ;
- retrait bancaire ;
- vérification des permissions ;
- modification des permissions des rangs ;
- sortie d'une guilde ;
- messages de réussite et d'erreur.

## Remplacement de la source de données

L'implémentation de démonstration est actuellement :

```java
GuildSource source = new MemoryGuildSource();
```

Pour brancher les données réelles d'Elestya, il suffit d'implémenter :

```java
GuildSource
```

puis de remplacer l'instance utilisée au démarrage.

Les formulaires Bedrock n'ont pas besoin d'être modifiés.

## Technologies

- Java 21
- Maven
- Paper 1.21.11
- Geyser
- Floodgate
- Cumulus

## Note

`MemoryGuildSource` est uniquement destiné à fournir un environnement autonome pour le test technique.

La logique des formulaires a volontairement été construite autour de l'interface `GuildSource` afin que l'intégration avec le système de guildes réel puisse être réalisée sans dépendre de cette implémentation en mémoire.