package fr.kilian.elestya.source.memory;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.*;
import fr.kilian.elestya.api.result.ActionResult;
import org.bukkit.Bukkit;

import java.util.*;

public class MemoryGuildSource implements GuildSource {

    private final Map<String, Guild> guilds = new HashMap<>();

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
    public ActionResult createGuild(UUID playerId, String name) {

        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        if (findGuildByPlayer(playerId).isPresent()) {
            return ActionResult.failure(
                    "Vous êtes déjà membre d'une guilde."
            );
        }

        if (name.isBlank()) {
            return ActionResult.failure(
                    "Le nom de la guilde ne peut pas être vide."
            );
        }

        if (guilds.containsKey(name)) {
            return ActionResult.failure(
                    "Une guilde avec ce nom existe déjà."
            );
        }

        String playerName = getPlayerName(playerId);

        List<GuildMember> members = new ArrayList<>();

        members.add(
                new GuildMember(
                        playerId,
                        playerName,
                        GuildRank.OWNER
                )
        );

        Guild guild = new Guild(
                name,
                name,
                playerId,
                members,
                0,
                new ArrayList<>()
        );

        guilds.put(guild.id(), guild);

        return ActionResult.success(
                "La guilde a été créée avec succès."
        );
    }

    @Override
    public ActionResult requestToJoin(UUID playerId, String guildId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(guildId, "guildId cannot be null");

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Cette guilde n'existe pas."
            );
        }

        if (findGuildByPlayer(playerId).isPresent()) {
            return ActionResult.failure(
                    "Vous êtes déjà membre d'une guilde."
            );
        }

        Guild guild = optionalGuild.get();

        if (guild.hasJoinRequest(playerId)) {
            return ActionResult.failure(
                    "Vous avez déjà envoyé une demande à cette guilde."
            );
        }

        int requests = 0;

        for (Guild existingGuild : guilds.values()) {
            if (existingGuild.hasJoinRequest(playerId)) {
                requests++;
            }
        }

        if (requests >= 5) {
            return ActionResult.failure(
                    "Vous avez déjà 5 demandes d'adhésion en attente."
            );
        }

        guild.joinRequests().add(playerId);

        return ActionResult.success(
                "Votre demande pour rejoindre " + guild.name() + " a été envoyée."
        );
    }

    @Override
    public ActionResult acceptJoinRequest(UUID actorId, UUID playerId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(playerId, "playerId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasPermission(actorId, GuildPermission.RECRUIT)) {
            return ActionResult.failure(
                    "Vous n'avez pas la permission de gérer les recrutements."
            );
        }

        if (!guild.hasJoinRequest(playerId)) {
            return ActionResult.failure(
                    "Ce joueur n'a pas demandé à rejoindre votre guilde."
            );
        }

        if (findGuildByPlayer(playerId).isPresent()) {

            guild.joinRequests().remove(playerId);

            return ActionResult.failure(
                    "Ce joueur appartient déjà à une autre guilde."
            );
        }

        String playerName = getPlayerName(playerId);

        guild.members().add(
                new GuildMember(
                        playerId,
                        playerName,
                        GuildRank.RECRUIT
                )
        );

        /*
         * Une fois le joueur accepté dans une guilde,
         * toutes ses autres demandes sont supprimées.
         */
        for (Guild existingGuild : guilds.values()) {
            existingGuild.joinRequests().remove(playerId);
        }

        return ActionResult.success(
                playerName + " a rejoint votre guilde en tant que recrue."
        );
    }

    @Override
    public ActionResult rejectJoinRequest(UUID actorId, UUID playerId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(playerId, "playerId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasPermission(actorId, GuildPermission.RECRUIT)) {
            return ActionResult.failure(
                    "Vous n'avez pas la permission de gérer les recrutements."
            );
        }

        if (!guild.hasJoinRequest(playerId)) {
            return ActionResult.failure(
                    "Ce joueur n'a pas demandé à rejoindre votre guilde."
            );
        }

        String playerName = getPlayerName(playerId);

        guild.joinRequests().remove(playerId);

        return ActionResult.success(
                "La demande de " + playerName + " a été refusée."
        );
    }

    @Override
    public ActionResult leaveGuild(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild = optionalGuild.get();

        if (guild.ownerId().equals(playerId)) {
            return ActionResult.failure(
                    "Le chef ne peut pas quitter sa guilde."
            );
        }

        Optional<GuildMember> optionalMember = guild.findMember(playerId);

        if (optionalMember.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes pas membre de cette guilde."
            );
        }

        guild.members().remove(optionalMember.get());

        return ActionResult.success(
                "Vous avez quitté la guilde " + guild.name() + "."
        );
    }

    @Override
    public ActionResult deposit(UUID playerId, double amount) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        if (amount <= 0) {
            return ActionResult.failure(
                    "Le montant doit être supérieur à 0."
            );
        }

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild = optionalGuild.get();

        guild.setBalance(
                guild.balance() + amount
        );

        return ActionResult.success(
                "Vous avez déposé " + amount + " dans la banque de la guilde."
        );
    }

    @Override
    public ActionResult withdraw(UUID playerId, double amount) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        if (amount <= 0) {
            return ActionResult.failure(
                    "Le montant doit être supérieur à 0."
            );
        }

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasPermission(
                playerId,
                GuildPermission.BANK_AND_UPGRADES
        )) {
            return ActionResult.failure(
                    "Vous n'avez pas la permission de retirer de l'argent."
            );
        }

        if (guild.balance() < amount) {
            return ActionResult.failure(
                    "La banque de la guilde ne contient pas assez d'argent."
            );
        }

        guild.setBalance(
                guild.balance() - amount
        );

        return ActionResult.success(
                "Vous avez retiré " + amount + " de la banque de la guilde."
        );
    }

    @Override
    public ActionResult promoteMember(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild = optionalGuild.get();

        /*
         * Aucune permission spécifique pour les rangs n'a été
         * indiquée dans les règles reçues.
         * Pour le moment, seul le chef peut les modifier.
         */
        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure(
                    "Seul le chef peut modifier le rang des membres."
            );
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure(
                    "Ce joueur n'est pas membre de votre guilde."
            );
        }

        GuildMember target = optionalTarget.get();

        if (target.rank() == GuildRank.OWNER) {
            return ActionResult.failure(
                    "Le chef ne peut pas être promu."
            );
        }

        GuildRank nextRank;

        if (target.rank() == GuildRank.RECRUIT) {
            nextRank = GuildRank.MEMBER;
        } else if (target.rank() == GuildRank.MEMBER) {
            nextRank = GuildRank.DEPUTY;
        } else {
            return ActionResult.failure(
                    "Ce membre possède déjà le rang maximum disponible."
            );
        }

        replaceMemberRank(
                guild,
                target,
                nextRank
        );

        return ActionResult.success(
                target.name() + " a été promu au rang " + getRankName(nextRank) + "."
        );
    }

    @Override
    public ActionResult demoteMember(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure(
                    "Seul le chef peut modifier le rang des membres."
            );
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure(
                    "Ce joueur n'est pas membre de votre guilde."
            );
        }

        GuildMember target = optionalTarget.get();

        if (target.rank() == GuildRank.OWNER) {
            return ActionResult.failure(
                    "Le chef ne peut pas être rétrogradé."
            );
        }

        GuildRank previousRank;

        if (target.rank() == GuildRank.DEPUTY) {
            previousRank = GuildRank.MEMBER;
        } else if (target.rank() == GuildRank.MEMBER) {
            previousRank = GuildRank.RECRUIT;
        } else {
            return ActionResult.failure(
                    "Ce membre possède déjà le rang le plus bas."
            );
        }

        replaceMemberRank(
                guild,
                target,
                previousRank
        );

        return ActionResult.success(
                target.name() + " a été rétrogradé au rang "
                        + getRankName(previousRank) + "."
        );
    }

    @Override
    public ActionResult kickMember(UUID actorId, UUID targetId) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(targetId, "targetId cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild = optionalGuild.get();

        if (!guild.hasPermission(actorId, GuildPermission.KICK)) {
            return ActionResult.failure(
                    "Vous n'avez pas la permission d'expulser des membres."
            );
        }

        if (actorId.equals(targetId)) {
            return ActionResult.failure(
                    "Vous ne pouvez pas vous expulser vous-même."
            );
        }

        Optional<GuildMember> optionalTarget = guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {
            return ActionResult.failure(
                    "Ce joueur n'est pas membre de votre guilde."
            );
        }

        GuildMember target = optionalTarget.get();

        if (target.rank() == GuildRank.OWNER) {
            return ActionResult.failure(
                    "Le chef ne peut pas être expulsé."
            );
        }

        guild.members().remove(target);

        return ActionResult.success(
                target.name() + " a été expulsé de la guilde."
        );
    }

    @Override
    public Set<GuildPermission> getPermissions(
            String guildId,
            GuildRank rank
    ) {

        Objects.requireNonNull(guildId, "guildId cannot be null");
        Objects.requireNonNull(rank, "rank cannot be null");

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return Set.of();
        }

        return optionalGuild.get().getPermissions(rank);
    }

    @Override
    public boolean hasPermission(
            UUID playerId,
            GuildPermission permission
    ) {

        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(permission, "permission cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return false;
        }

        return optionalGuild.get().hasPermission(
                playerId,
                permission
        );
    }

    @Override
    public ActionResult setPermission(
            UUID actorId,
            GuildRank rank,
            GuildPermission permission,
            boolean enabled
    ) {

        Objects.requireNonNull(actorId, "actorId cannot be null");
        Objects.requireNonNull(rank, "rank cannot be null");
        Objects.requireNonNull(permission, "permission cannot be null");

        Optional<Guild> optionalGuild = findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return ActionResult.failure(
                    "Vous n'êtes membre d'aucune guilde."
            );
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(actorId)) {
            return ActionResult.failure(
                    "Seul le chef peut modifier les permissions des rangs."
            );
        }

        if (rank == GuildRank.OWNER) {
            return ActionResult.failure(
                    "Les permissions du chef ne peuvent pas être modifiées."
            );
        }

        guild.setPermission(
                rank,
                permission,
                enabled
        );

        String state = enabled
                ? "activée"
                : "désactivée";

        return ActionResult.success(
                "Permission " + state
                        + " pour le rang "
                        + getRankName(rank) + "."
        );
    }

    private void replaceMemberRank(
            Guild guild,
            GuildMember member,
            GuildRank newRank
    ) {

        int index = guild.members().indexOf(member);

        GuildMember updatedMember = new GuildMember(
                member.playerId(),
                member.name(),
                newRank
        );

        guild.members().set(
                index,
                updatedMember
        );
    }

    @Override
    public List<JoinRequest> getJoinRequests(String guildId) {

        Objects.requireNonNull(guildId, "guildId cannot be null");

        Optional<Guild> optionalGuild = findGuildById(guildId);

        if (optionalGuild.isEmpty()) {
            return List.of();
        }

        return optionalGuild.get()
                .joinRequests()
                .stream()
                .map(playerId -> new JoinRequest(
                        playerId,
                        getPlayerName(playerId)
                ))
                .toList();
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
}