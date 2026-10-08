package fr.kilian.elestya.source.memory;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.*;
import fr.kilian.elestya.api.result.ActionResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class MemoryGuildSource implements GuildSource {

    private final Map<String, Guild> guilds = new HashMap<>();
    private final Map<UUID, Set<String>> invitations = new HashMap<>();
    private final Map<String, Integer> chestProgress = new HashMap<>();
    private final Map<String, Integer> reserveRows = new HashMap<>();
    private final Map<String, Integer> chestLocks = new HashMap<>();

    private final Map<String, Map<UUID, Integer>> contributions = new HashMap<>();

    public MemoryGuildSource() {
        new MemoryGuildSeeder(this).seed();
    }

    void addGuild(Guild guild) {
        Objects.requireNonNull(guild, "guild cannot be null");

        guilds.put(guild.id(), guild);
    }

    @Override
    public List<Guild> getGuilds() {
        return new ArrayList<>(guilds.values());
    }

    @Override
    public Optional<Guild> findGuildByPlayer(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        for (Guild guild : guilds.values()) {
            if (guild.isMember(playerId)) {
                return Optional.of(guild);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Guild> findGuildById(String guildId) {

        Objects.requireNonNull(guildId, "guildId cannot be null");

        return Optional.ofNullable(guilds.get(guildId));
    }

    @Override
    public ActionResult createGuild(
            UUID playerId,
            String name,
            String entryMessage
    ) {

        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        Objects.requireNonNull(
                entryMessage,
                "entryMessage cannot be null"
        );

        if (findGuildByPlayer(playerId).isPresent()) {
            return ActionResult.failure(
                    "Vous êtes déjà membre d'une guilde."
            );
        }

        String normalizedName =
                name.trim();

        if (!normalizedName.matches(
                "^[A-Za-z0-9_-]{3,16}$"
        )) {

            return ActionResult.failure(
                    "Le nom doit contenir entre 3 et 16 caractères et uniquement des lettres, chiffres, - ou _."
            );
        }

        boolean alreadyExists =
                guilds.values()
                        .stream()
                        .anyMatch(guild ->
                                guild.name().equalsIgnoreCase(
                                        normalizedName
                                )
                        );

        if (alreadyExists) {
            return ActionResult.failure(
                    "Une guilde avec ce nom existe déjà."
            );
        }

        String guildId =
                normalizedName.toLowerCase(
                        Locale.ROOT
                );

        String playerName =
                getPlayerName(playerId);

        List<GuildMember> members =
                new ArrayList<>();

        members.add(
                new GuildMember(
                        playerId,
                        playerName,
                        GuildRank.OWNER
                )
        );

        Guild guild =
                new Guild(
                        guildId,
                        normalizedName,
                        playerId,
                        members,
                        0,
                        0,
                        new ArrayList<>()
                );

        String normalizedEntryMessage =
                entryMessage.trim();

        if (normalizedEntryMessage.isBlank()) {

            normalizedEntryMessage =
                    "Bienvenue dans la guilde "
                            + normalizedName
                            + " !";
        }

        guild.setEntryMessage(
                normalizedEntryMessage
        );

        guilds.put(
                guild.id(),
                guild
        );

        return ActionResult.success(
                "La guilde "
                        + normalizedName
                        + " a été créée avec succès."
        );
    }
    @Override
    public ActionResult requestToJoin(UUID playerId, String guildId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(guildId, "guildId cannot be null");

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Cette guilde n'existe pas.");
        }

        if (findGuildByPlayer(playerId).isPresent()) {
            return ActionResult.failure("Vous êtes déjà membre d'une guilde.");
        }

        Guild guild = optionalGuild.get();

        if (guild.hasJoinRequest(playerId)) {
            return ActionResult.failure("Vous avez déjà envoyé une demande à cette guilde.");
        }

        int requests = 0;

        for (Guild existingGuild : guilds.values()) {
            if (existingGuild.hasJoinRequest(playerId)) {
                requests++;
            }
        }

        if (requests >= 5) {
            return ActionResult.failure("Vous avez déjà 5 demandes d'adhésion en attente.");
        }

        guild.joinRequests().add(playerId);

        return ActionResult.success("Votre demande pour rejoindre " + guild.name() + " a été envoyée.");
    }

    @Override
    public ActionResult acceptJoinRequest(UUID actorId, UUID playerId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(playerId, "playerId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasPermission(actorId, GuildPermission.RECRUIT)) {
            return ActionResult.failure("Vous n'avez pas la permission de gérer les recrutements.");
        }

        if (!guild.hasJoinRequest(playerId)) {
            return ActionResult.failure("Ce joueur n'a pas demandé à rejoindre votre guilde.");
        }

        if (findGuildByPlayer(playerId).isPresent()) {

            guild.joinRequests().remove(playerId);

            return ActionResult.failure("Ce joueur appartient déjà à une autre guilde.");
        }

        String playerName = getPlayerName(playerId);

        guild.members().add(new GuildMember(playerId, playerName, GuildRank.RECRUIT));

        /*
         * Une fois le joueur accepté dans une guilde,
         * toutes ses autres demandes sont supprimées.
         */
        for (Guild existingGuild : guilds.values()) {
            existingGuild.joinRequests().remove(playerId);
        }

        return ActionResult.success(playerName + " a rejoint votre guilde en tant que recrue.");
    }

    @Override
    public ActionResult rejectJoinRequest(UUID actorId, UUID playerId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(playerId, "playerId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasPermission(actorId, GuildPermission.RECRUIT)) {
            return ActionResult.failure("Vous n'avez pas la permission de gérer les recrutements.");
        }

        if (!guild.hasJoinRequest(playerId)) {
            return ActionResult.failure("Ce joueur n'a pas demandé à rejoindre votre guilde.");
        }

        String playerName = getPlayerName(playerId);

        guild.joinRequests().remove(playerId);

        return ActionResult.success("La demande de " + playerName + " a été refusée.");
    }

    @Override
    public ActionResult leaveGuild(UUID playerId) {

        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        Optional<Guild> optionalGuild =
                findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild =
                optionalGuild.get();

        Optional<GuildMember> optionalMember =
                guild.findMember(playerId);

        if (optionalMember.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes pas membre de cette guilde."
            );
        }

        /*
         * Le chef ne peut quitter la guilde que s'il est
         * le dernier membre.
         *
         * Sinon il doit d'abord céder la guilde à un adjoint.
         */
        if (guild.ownerId().equals(playerId)) {

            if (guild.members().size() > 1) {
                return ActionResult.failure(
                        "Vous devez céder la guilde avant de pouvoir la quitter."
                );
            }

            /*
             * L'implémentation mémoire ne possède pas de système
             * d'économie joueur.
             *
             * On récupère donc ici le montant restant avant la
             * suppression afin de pouvoir l'afficher dans le résultat.
             *
             * Dans l'implémentation réelle d'Elestya, ce montant
             * pourra être reversé au compte du chef.
             */
            double recoveredBalance =
                    guild.balance();

            String guildName =
                    guild.name();

            deleteGuild(
                    guild
            );

            return ActionResult.success(
                    "La guilde "
                            + guildName
                            + " a été supprimée. "
                            + formatMoney(recoveredBalance)
                            + " ont été récupérés."
            );
        }

        guild.members().remove(
                optionalMember.get()
        );

        return ActionResult.success(
                "Vous avez quitté la guilde "
                        + guild.name()
                        + "."
        );
    }

    @Override
    public ActionResult deposit(UUID playerId, double amount) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        if (amount <= 0) {
            return ActionResult.failure("Le montant doit être supérieur à 0.");
        }

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        guild.setBalance(guild.balance() + amount);

        return ActionResult.success("Vous avez déposé " + amount + " dans la banque de la guilde.");
    }

    @Override
    public ActionResult withdraw(UUID playerId, double amount) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        if (amount <= 0) {
            return ActionResult.failure("Le montant doit être supérieur à 0.");
        }

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasPermission(playerId, GuildPermission.BANK_AND_UPGRADES)) {
            return ActionResult.failure("Vous n'avez pas la permission de retirer de l'argent.");
        }

        if (guild.balance() < amount) {
            return ActionResult.failure("La banque de la guilde ne contient pas assez d'argent.");
        }

        guild.setBalance(guild.balance() - amount);

        return ActionResult.success("Vous avez retiré " + amount + " de la banque de la guilde.");
    }

    @Override
    public ActionResult promoteMember(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        /*
         * Aucune permission spécifique pour les rangs n'a été
         * indiquée dans les règles reçues.
         * Pour le moment, seul le chef peut les modifier.
         */
        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure("Seul le chef peut modifier le rang des membres.");
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure("Ce joueur n'est pas membre de votre guilde.");
        }

        GuildMember target = optionalTarget.get();

        if (target.rank() == GuildRank.OWNER) {
            return ActionResult.failure("Le chef ne peut pas être promu.");
        }

        GuildRank nextRank;

        if (target.rank() == GuildRank.RECRUIT) {
            nextRank = GuildRank.MEMBER;
        } else if (target.rank() == GuildRank.MEMBER) {
            nextRank = GuildRank.DEPUTY;
        } else {
            return ActionResult.failure("Ce membre possède déjà le rang maximum disponible.");
        }

        replaceMemberRank(guild, target, nextRank);

        return ActionResult.success(target.name() + " a été promu au rang " + getRankName(nextRank) + ".");
    }

    @Override
    public ActionResult demoteMember(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure("Seul le chef peut modifier le rang des membres.");
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure("Ce joueur n'est pas membre de votre guilde.");
        }

        GuildMember target = optionalTarget.get();

        if (target.rank() == GuildRank.OWNER) {
            return ActionResult.failure("Le chef ne peut pas être rétrogradé.");
        }

        GuildRank previousRank;

        if (target.rank() == GuildRank.DEPUTY) {
            previousRank = GuildRank.MEMBER;
        } else if (target.rank() == GuildRank.MEMBER) {
            previousRank = GuildRank.RECRUIT;
        } else {
            return ActionResult.failure("Ce membre possède déjà le rang le plus bas.");
        }

        replaceMemberRank(guild, target, previousRank);

        return ActionResult.success(target.name() + " a été rétrogradé au rang " + getRankName(previousRank) + ".");
    }

    @Override
    public ActionResult kickMember(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");

        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasPermission(actorId, GuildPermission.KICK)) {
            return ActionResult.failure("Vous n'avez pas la permission d'expulser des membres.");
        }

        if (actorId.equals(targetId)) {
            return ActionResult.failure("Vous ne pouvez pas vous expulser vous-même.");
        }

        Optional<GuildMember> optionalActor = guild.findMember(actorId);

        if (optionalActor.isEmpty()) {
            return ActionResult.failure("Impossible de trouver votre profil dans la guilde.");
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure("Ce joueur n'est pas membre de votre guilde.");
        }

        GuildMember actor = optionalActor.get();

        GuildMember target = optionalTarget.get();

        if (target.rank() == GuildRank.OWNER) {
            return ActionResult.failure("Le chef ne peut pas être expulsé.");
        }

        /*
         * Un joueur ne peut expulser qu'un rang
         * strictement inférieur au sien.
         *
         * Chef    > Adjoint > Membre > Recrue
         */
        if (getRankLevel(actor.rank()) <= getRankLevel(target.rank())) {

            return ActionResult.failure("Vous ne pouvez expulser qu'un membre d'un rang inférieur au vôtre.");
        }

        guild.members().remove(target);

        return ActionResult.success(target.name() + " a été expulsé de la guilde.");
    }

    @Override
    public Set<GuildPermission> getPermissions(String guildId, GuildRank rank) {

        Objects.requireNonNull(guildId, "guildId cannot be null");
        Objects.requireNonNull(rank, "rank cannot be null");

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return Set.of();
        }

        return optionalGuild.get().getPermissions(rank);
    }

    @Override
    public boolean hasPermission(UUID playerId, GuildPermission permission) {

        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(permission, "permission cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return false;
        }

        return optionalGuild.get().hasPermission(playerId, permission);
    }

    @Override
    public ActionResult setPermission(UUID actorId, GuildRank rank, GuildPermission permission, boolean enabled) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(rank, "rank cannot be null");
        Objects.requireNonNull(permission, "permission cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure("Seul le chef peut modifier les permissions des rangs.");
        }

        if (rank == GuildRank.OWNER) {
            return ActionResult.failure("Les permissions du chef ne peuvent pas être modifiées.");
        }

        guild.setPermission(rank, permission, enabled);

        String state = enabled ? "activée" : "désactivée";

        return ActionResult.success("Permission " + state + " pour le rang " + getRankName(rank) + ".");
    }

    private void replaceMemberRank(Guild guild, GuildMember member, GuildRank newRank) {

        int index = guild.members().indexOf(member);

        GuildMember updatedMember = new GuildMember(member.playerId(), member.name(), newRank);

        guild.members().set(index, updatedMember);
    }

    @Override
    public List<JoinRequest> getJoinRequests(String guildId) {

        Objects.requireNonNull(guildId, "guildId cannot be null");

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return List.of();
        }

        return optionalGuild.get().joinRequests().stream().map(playerId -> new JoinRequest(playerId, getPlayerName(playerId))).toList();
    }

    private String getPlayerName(UUID playerId) {

        String name = Bukkit.getOfflinePlayer(playerId).getName();

        if (name != null) {
            return name;
        }

        return playerId.toString();
    }

    private String getRankName(GuildRank rank) {

        return switch (rank) {
            case OWNER -> "Chef";
            case DEPUTY -> "Adjoint";
            case MEMBER -> "Membre";
            case RECRUIT -> "Recrue";
        };
    }

    @Override
    public ActionResult transferOwnership(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");

        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure("Seul le chef peut céder la guilde.");
        }

        if (actorId.equals(targetId)) {
            return ActionResult.failure("Vous êtes déjà le chef de cette guilde.");
        }

        Optional<GuildMember> optionalActor = guild.findMember(actorId);

        if (optionalActor.isEmpty()) {
            return ActionResult.failure("Impossible de trouver votre profil dans la guilde.");
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure("Ce joueur n'est pas membre de votre guilde.");
        }

        GuildMember actor = optionalActor.get();

        GuildMember target = optionalTarget.get();

        /*
         * Elestya demande qu'une cession puisse uniquement
         * être effectuée vers un Adjoint.
         */
        if (target.rank() != GuildRank.DEPUTY) {
            return ActionResult.failure("La guilde ne peut être cédée qu'à un Adjoint.");
        }

        /*
         * L'ancien chef devient Adjoint.
         */
        replaceMemberRank(guild, actor, GuildRank.DEPUTY);

        /*
         * L'Adjoint devient Chef.
         */
        replaceMemberRank(guild, target, GuildRank.OWNER);

        guild.setOwnerId(targetId);

        return ActionResult.success("Vous avez cédé la guilde à " + target.name() + ".");
    }

    private int getRankLevel(GuildRank rank) {

        return switch (rank) {
            case OWNER -> 4;
            case DEPUTY -> 3;
            case MEMBER -> 2;
            case RECRUIT -> 1;
        };
    }

    @Override
    public List<Guild> getPendingJoinRequestGuilds(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        return guilds.values().stream().filter(guild -> guild.hasJoinRequest(playerId)).toList();
    }

    @Override
    public ActionResult cancelJoinRequest(UUID playerId, String guildId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        Objects.requireNonNull(guildId, "guildId cannot be null");

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Cette guilde n'existe plus.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasJoinRequest(playerId)) {
            return ActionResult.failure("Vous n'avez aucune demande en attente pour cette guilde.");
        }

        guild.joinRequests().remove(playerId);

        return ActionResult.success("Votre demande pour rejoindre " + guild.name() + " a été annulée.");
    }

    @Override
    public List<Guild> getReceivedInvitations(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        Set<String> guildIds = invitations.getOrDefault(playerId, Set.of());

        return guildIds.stream().map(this::findGuildById).flatMap(Optional::stream).toList();
    }

    @Override
    public ActionResult invitePlayer(UUID actorId, String playerName) {

        Objects.requireNonNull(actorId, "actorId cannot be null");

        Objects.requireNonNull(playerName, "playerName cannot be null");

        if (playerName.isBlank()) {
            return ActionResult.failure("Veuillez saisir le nom d'un joueur.");
        }

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasPermission(actorId, GuildPermission.RECRUIT)) {
            return ActionResult.failure("Vous n'avez pas la permission d'inviter des joueurs.");
        }

        Player targetPlayer = Bukkit.getPlayerExact(playerName.trim());

        if (targetPlayer == null || !targetPlayer.isOnline()) {

            return ActionResult.failure("Ce joueur doit être connecté pour être invité.");
        }

        UUID targetId = targetPlayer.getUniqueId();

        if (actorId.equals(targetId)) {
            return ActionResult.failure("Vous ne pouvez pas vous inviter vous-même.");
        }

        Optional<Guild> targetGuild = findGuildByPlayer(targetId);

        if (targetGuild.isPresent()) {

            if (targetGuild.get().id().equals(guild.id())) {

                return ActionResult.failure("Ce joueur est déjà membre de votre guilde.");
            }

            return ActionResult.failure("Ce joueur appartient déjà à une autre guilde.");
        }

        Set<String> playerInvitations = invitations.computeIfAbsent(targetId, ignored -> new HashSet<>());

        if (!playerInvitations.add(guild.id())) {
            return ActionResult.failure("Ce joueur possède déjà une invitation de votre guilde.");
        }

        return ActionResult.success(targetPlayer.getName() + " a été invité dans votre guilde.");
    }

    @Override
    public ActionResult acceptInvitation(UUID playerId, String guildId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        Objects.requireNonNull(guildId, "guildId cannot be null");

        if (findGuildByPlayer(playerId).isPresent()) {
            return ActionResult.failure("Vous êtes déjà membre d'une guilde.");
        }

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Cette guilde n'existe plus.");
        }

        Set<String> playerInvitations = invitations.get(playerId);

        if (playerInvitations == null || !playerInvitations.contains(guildId)) {

            return ActionResult.failure("Vous n'avez pas été invité dans cette guilde.");
        }

        Guild guild = optionalGuild.get();

        guild.members().add(new GuildMember(playerId, getPlayerName(playerId), GuildRank.RECRUIT));

        /*
         * Le joueur appartient maintenant à une guilde :
         * toutes ses invitations deviennent inutiles.
         */
        invitations.remove(playerId);

        /*
         * Même chose pour ses demandes d'adhésion.
         */
        for (Guild existingGuild : guilds.values()) {
            existingGuild.joinRequests().remove(playerId);
        }

        return ActionResult.success("Vous avez rejoint la guilde " + guild.name() + " en tant que Recrue.");
    }

    @Override
    public ActionResult rejectInvitation(UUID playerId, String guildId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        Objects.requireNonNull(guildId, "guildId cannot be null");

        Set<String> playerInvitations = invitations.get(playerId);

        if (playerInvitations == null || !playerInvitations.remove(guildId)) {

            return ActionResult.failure("Cette invitation n'existe plus.");
        }

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (playerInvitations.isEmpty()) {
            invitations.remove(playerId);
        }

        if (optionalGuild.isEmpty()) {
            return ActionResult.success("L'invitation a été supprimée.");
        }

        return ActionResult.success("Vous avez refusé l'invitation de " + optionalGuild.get().name() + ".");
    }

    @Override
    public Optional<GuildChestInfo> getGuildChestInfo(String guildId) {

        Objects.requireNonNull(guildId, "guildId cannot be null");

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return Optional.empty();
        }

        Guild guild = optionalGuild.get();

        int targetAmount = 64;

        int depositedAmount = chestProgress.getOrDefault(guildId, 0);

        return Optional.of(new GuildChestInfo("Saison actuelle", "3 jours et 12 heures", guild.points(), "Bûche de chêne", targetAmount, depositedAmount));
    }

    @Override
    public List<GuildContribution> getGuildContributions(String guildId) {

        Objects.requireNonNull(guildId, "guildId cannot be null");

        Map<UUID, Integer> guildContributions = contributions.getOrDefault(guildId, Map.of());

        return guildContributions.entrySet().stream().map(entry -> new GuildContribution(entry.getKey(), getPlayerName(entry.getKey()), entry.getValue())).sorted(Comparator.comparingInt(GuildContribution::amount).reversed()).toList();
    }

    @Override
    public ActionResult depositAllChestItems(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure("Vous n'êtes membre d'aucune guilde.");
        }

        Guild guild = optionalGuild.get();

        int targetAmount = 64;

        int currentAmount = chestProgress.getOrDefault(guild.id(), 0);

        if (currentAmount >= targetAmount) {
            return ActionResult.failure("L'objectif actuel du coffre est déjà complété.");
        }

        /*
         * Implémentation mémoire :
         * on simule un dépôt de 16 objets.
         *
         * L'implémentation réelle d'Elestya pourra ici
         * lire l'inventaire du joueur.
         */
        int deposited = Math.min(16, targetAmount - currentAmount);

        chestProgress.put(guild.id(), currentAmount + deposited);

        contributions.computeIfAbsent(guild.id(), ignored -> new HashMap<>()).merge(playerId, deposited, Integer::sum);

        /*
         * Les apports font progresser les points
         * de saison dans notre source fictive.
         */
        guild.setPoints(guild.points() + deposited);

        return ActionResult.success("Vous avez déposé " + deposited + " élément(s) dans le coffre.");
    }

    @Override
    public Optional<GuildTreasuryInfo> getTreasuryInfo(
            String guildId
    ) {

        Objects.requireNonNull(
                guildId,
                "guildId cannot be null"
        );

        Optional<Guild> optionalGuild =
                findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return Optional.empty();
        }

        int currentReserveRows =
                reserveRows.getOrDefault(
                        guildId,
                        1
                );

        int currentChestLocks =
                chestLocks.getOrDefault(
                        guildId,
                        1
                );

        return Optional.of(
                new GuildTreasuryInfo(
                        currentReserveRows,
                        6,
                        getUpgradePrice(currentReserveRows),
                        currentChestLocks,
                        10,
                        getUpgradePrice(currentChestLocks)
                )
        );
    }

    @Override
    public ActionResult upgradeReserve(
            UUID actorId
    ) {

        Objects.requireNonNull(
                actorId,
                "actorId cannot be null"
        );

        Optional<Guild> optionalGuild =
                findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild =
                optionalGuild.get();

        if (!guild.hasPermission(
                actorId,
                GuildPermission.BANK_AND_UPGRADES
        )) {
            return ActionResult.failure(
                    "Vous n'avez pas la permission de gérer les améliorations."
            );
        }

        int currentLevel =
                reserveRows.getOrDefault(
                        guild.id(),
                        1
                );

        int maxLevel = 6;

        if (currentLevel >= maxLevel) {
            return ActionResult.failure(
                    "La réserve commune est déjà au niveau maximum."
            );
        }

        double price =
                getUpgradePrice(
                        currentLevel
                );

        if (guild.balance() < price) {
            return ActionResult.failure(
                    "La banque de la guilde ne contient pas assez d'argent."
            );
        }

        guild.setBalance(
                guild.balance() - price
        );

        int newLevel =
                currentLevel + 1;

        reserveRows.put(
                guild.id(),
                newLevel
        );

        return ActionResult.success(
                "La réserve commune possède désormais "
                        + newLevel
                        + " rangée(s), soit "
                        + (newLevel * 9)
                        + " cases."
        );
    }

    @Override
    public ActionResult upgradeChestLocks(
            UUID actorId
    ) {

        Objects.requireNonNull(
                actorId,
                "actorId cannot be null"
        );

        Optional<Guild> optionalGuild =
                findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild =
                optionalGuild.get();

        if (!guild.hasPermission(
                actorId,
                GuildPermission.BANK_AND_UPGRADES
        )) {
            return ActionResult.failure(
                    "Vous n'avez pas la permission de gérer les améliorations."
            );
        }

        int currentLevel =
                chestLocks.getOrDefault(
                        guild.id(),
                        1
                );

        int maxLevel = 10;

        if (currentLevel >= maxLevel) {
            return ActionResult.failure(
                    "Le nombre de cadenas est déjà au maximum."
            );
        }

        double price =
                getUpgradePrice(
                        currentLevel
                );

        if (guild.balance() < price) {
            return ActionResult.failure(
                    "La banque de la guilde ne contient pas assez d'argent."
            );
        }

        guild.setBalance(
                guild.balance() - price
        );

        int newLevel =
                currentLevel + 1;

        chestLocks.put(
                guild.id(),
                newLevel
        );

        return ActionResult.success(
                "Chaque membre peut désormais verrouiller "
                        + newLevel
                        + " coffre(s)."
        );
    }

    private double getUpgradePrice(
            int currentLevel
    ) {

        return 25_000.0 * currentLevel;
    }

    @Override
    public Optional<GuildShopInfo> getGuildShopInfo(
            String guildId
    ) {

        Objects.requireNonNull(
                guildId,
                "guildId cannot be null"
        );

        Optional<Guild> optionalGuild =
                findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return Optional.empty();
        }

        /*
         * Données fictives pour l'implémentation mémoire.
         *
         * Le serveur de test ne me permet pas d'accéder à la boutique
         * déverrouillée avec ma guilde actuelle, je ne peux donc pas
         * connaître précisément le contenu complet ni son fonctionnement.
         *
         * Ces valeurs servent uniquement à démontrer le comportement
         * du menu Bedrock. L'implémentation réelle d'Elestya pourra
         * fournir les vraies conditions et les vrais articles.
         */

        int seed =
                Math.abs(
                        guildId.hashCode()
                );

        int novaMembers =
                seed % 5;

        int constellationMembers =
                (seed / 5) % 2;

        List<GuildShopItem> items =
                List.of(
                        /*
                         * Cet article est visible dans le menu Java,
                         * son nom et son prix sont donc connus.
                         */
                        new GuildShopItem(
                                "spider_spawner",
                                "Spawner à araignées",
                                "Spawners",
                                170_000,
                                "Engendre des araignées une fois posé."
                        ),

                        /*
                         * Articles fictifs uniquement pour pouvoir
                         * démontrer une boutique contenant plusieurs entrées.
                         */
                        new GuildShopItem(
                                "zombie_spawner",
                                "Spawner à zombies",
                                "Spawners",
                                185_000,
                                "Article fictif de démonstration."
                        ),

                        new GuildShopItem(
                                "skeleton_spawner",
                                "Spawner à squelettes",
                                "Spawners",
                                200_000,
                                "Article fictif de démonstration."
                        )
                );

        return Optional.of(
                new GuildShopInfo(
                        novaMembers,
                        3,
                        constellationMembers,
                        1,
                        items
                )
        );
    }

    @Override
    public Optional<GuildWeeklyObjective> getWeeklyObjective(
            String guildId
    ) {

        Objects.requireNonNull(
                guildId,
                "guildId cannot be null"
        );

        Optional<Guild> optionalGuild =
                findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return Optional.empty();
        }

        /*
         * Données fictives.
         *
         * Je n'ai pas accès au contenu exact de l'objectif
         * hebdomadaire sur le serveur Java avec ma guilde actuelle.
         *
         * Cette implémentation sert uniquement à démontrer
         * le fonctionnement du menu Bedrock.
         * L'implémentation réelle d'Elestya fournira
         * l'objectif, la progression et la récompense réels.
         */
        int seed =
                Math.abs(
                        guildId.hashCode()
                );

        int target =
                1_000;

        int progress =
                seed % (target + 1);

        return Optional.of(
                new GuildWeeklyObjective(
                        "Objectif de la semaine",
                        "Contribuer à la progression de la guilde.",
                        progress,
                        target,
                        500
                )
        );
    }

    @Override
    public List<Guild> getGuildRanking() {

        return guilds.values()
                .stream()
                .filter(guild -> guild.points() > 0)
                .sorted(
                        Comparator.comparingInt(
                                Guild::points
                        ).reversed()
                )
                .toList();
    }

    private void deleteGuild(
            Guild guild
    ) {

        String guildId =
                guild.id();

        /*
         * Supprime la guilde principale.
         */
        guilds.remove(
                guildId
        );

        /*
         * Supprime toutes les invitations liées
         * à cette guilde.
         */
        invitations.values()
                .forEach(guildIds ->
                        guildIds.remove(guildId)
                );

        /*
         * Supprime les éventuelles entrées devenues vides.
         */
        invitations.entrySet()
                .removeIf(entry ->
                        entry.getValue().isEmpty()
                );

        /*
         * Nettoyage des données fictives liées au coffre.
         */
        chestProgress.remove(
                guildId
        );

        contributions.remove(
                guildId
        );

        /*
         * Nettoyage des améliorations.
         */
        reserveRows.remove(
                guildId
        );

        chestLocks.remove(
                guildId
        );
    }

    private String formatMoney(
            double amount
    ) {

        return String.format(
                Locale.FRANCE,
                "%,.2f",
                amount
        );
    }
}