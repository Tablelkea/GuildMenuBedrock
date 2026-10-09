package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.Guild;
import fr.kilian.elestya.api.domain.guild.GuildMember;
import fr.kilian.elestya.api.domain.guild.GuildPermission;
import fr.kilian.elestya.api.domain.guild.GuildRank;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GuildMemberForm {

    private static final String PROMOTE_BUTTON = "Promouvoir";
    private static final String DEMOTE_BUTTON = "Rétrograder";
    private static final String KICK_BUTTON = "Expulser";
    private static final String BACK_BUTTON = "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildMemberForm(
            GuildSource guildSource,
            FormService formService
    ) {
        this.guildSource = Objects.requireNonNull(
                guildSource,
                "guildSource cannot be null"
        );

        this.formService = Objects.requireNonNull(
                formService,
                "formService cannot be null"
        );
    }

    public void open(
            Player player,
            GuildMember target
    ) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        Objects.requireNonNull(
                target,
                "target cannot be null"
        );

        UUID actorId = player.getUniqueId();
        UUID targetId = target.playerId();

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild = optionalGuild.get();

        Optional<GuildMember> optionalTarget =
                guild.findMember(targetId);

        if (optionalTarget.isEmpty()) {

            player.sendMessage(
                    "Ce joueur n'est plus membre de la guilde."
            );

            new GuildMembersForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        GuildMember currentTarget =
                optionalTarget.get();

        GuildRank rank =
                currentTarget.rank();

        SimpleForm.Builder builder =
                SimpleForm.builder()
                        .title(currentTarget.name())
                        .content(
                                "Rang : "
                                        + getRankName(rank)
                        );

        /*
         * Seul le chef gère les promotions / rétrogradations.
         */
        if (guild.ownerId().equals(actorId)
                && rank != GuildRank.OWNER) {

            if (rank != GuildRank.RECRUIT) {
                builder.button(
                        DEMOTE_BUTTON
                );
            }

            /*
             * RECRUIT -> MEMBER
             * MEMBER -> DEPUTY
             * DEPUTY -> OWNER via cession
             */
            builder.button(
                    PROMOTE_BUTTON
            );
        }

        /*
         * Le bouton peut être affiché si le joueur possède KICK,
         * mais la vraie règle de hiérarchie est vérifiée
         * côté GuildSource.
         */
        if (guildSource.hasPermission(
                actorId,
                GuildPermission.KICK
        )
                && !currentTarget.isOwner()
                && !targetId.equals(actorId)) {

            builder.button(
                    KICK_BUTTON
            );
        }

        builder.button(
                BACK_BUTTON
        );

        builder.validResultHandler(
                formService.sync(player, response -> {

                    String clicked =
                            response.clickedButton().text();

                    switch (clicked) {

                        case DEMOTE_BUTTON -> handleDemotion(
                                player,
                                targetId
                        );

                        case PROMOTE_BUTTON -> {

                            if (currentTarget.rank()
                                    == GuildRank.DEPUTY) {

                                openTransferConfirmation(
                                        player,
                                        currentTarget
                                );

                                return;
                            }

                            handlePromotion(
                                    player,
                                    targetId
                            );
                        }

                        case KICK_BUTTON -> openKickConfirmation(
                                player,
                                currentTarget
                        );

                        case BACK_BUTTON -> new GuildMembersForm(
                                guildSource,
                                formService
                        ).open(player);
                    }
                })
        );

        formService.sendForm(
                player,
                builder.build()
        );
    }

    private void handlePromotion(
            Player player,
            UUID targetId
    ) {

        ActionResult result =
                guildSource.promoteMember(
                        player.getUniqueId(),
                        targetId
                );

        player.sendMessage(
                result.message()
        );

        if (!result.success()) {
            refresh(
                    player,
                    targetId
            );
            return;
        }

        Player targetPlayer =
                Bukkit.getPlayer(
                        targetId
                );

        if (targetPlayer != null
                && targetPlayer.isOnline()) {

            targetPlayer.sendMessage(
                    "Vous avez été promu."
            );
        }

        refresh(
                player,
                targetId
        );
    }

    private void handleDemotion(
            Player player,
            UUID targetId
    ) {

        ActionResult result =
                guildSource.demoteMember(
                        player.getUniqueId(),
                        targetId
                );

        player.sendMessage(
                result.message()
        );

        if (!result.success()) {
            refresh(
                    player,
                    targetId
            );
            return;
        }

        Player targetPlayer =
                Bukkit.getPlayer(
                        targetId
                );

        if (targetPlayer != null
                && targetPlayer.isOnline()) {

            targetPlayer.sendMessage(
                    "Vous avez été rétrogradé."
            );
        }

        refresh(
                player,
                targetId
        );
    }

    private void openTransferConfirmation(
            Player player,
            GuildMember target
    ) {

        ModalForm form =
                ModalForm.builder()
                        .title(
                                "Céder la guilde"
                        )
                        .content(
                                "Voulez-vous vraiment céder la guilde à "
                                        + target.name()
                                        + " ?\n\n"
                                        + "Vous deviendrez Adjoint et "
                                        + target.name()
                                        + " deviendra Chef."
                        )
                        .button1(
                                "Oui"
                        )
                        .button2(
                                "Non"
                        )
                        .validResultHandler(
                                formService.sync(player, response -> {

                                    if (response.clickedButtonId() != 0) {

                                        refresh(
                                                player,
                                                target.playerId()
                                        );

                                        return;
                                    }

                                    ActionResult result =
                                            guildSource.transferOwnership(
                                                    player.getUniqueId(),
                                                    target.playerId()
                                            );

                                    player.sendMessage(
                                            result.message()
                                    );

                                    if (!result.success()) {

                                        refresh(
                                                player,
                                                target.playerId()
                                        );

                                        return;
                                    }

                                    Player targetPlayer =
                                            Bukkit.getPlayer(
                                                    target.playerId()
                                            );

                                    if (targetPlayer != null
                                            && targetPlayer.isOnline()) {

                                        targetPlayer.sendMessage(
                                                "Vous êtes désormais le Chef de la guilde."
                                        );
                                    }

                                    /*
                                     * L'acteur n'est plus Chef,
                                     * donc on retourne au menu principal.
                                     */
                                    new GuildMainForm(
                                            guildSource,
                                            formService
                                    ).open(player);
                                })
                        )
                        .closedOrInvalidResultHandler(
                                formService.sync(
                                        player,
                                        () -> refresh(
                                                player,
                                                target.playerId()
                                        )
                                )
                        )
                        .build();

        formService.sendForm(
                player,
                form
        );
    }

    private void openKickConfirmation(
            Player player,
            GuildMember target
    ) {

        ModalForm form =
                ModalForm.builder()
                        .title(
                                "Expulser un membre"
                        )
                        .content(
                                "Voulez-vous vraiment expulser "
                                        + target.name()
                                        + " ?"
                        )
                        .button1(
                                "Confirmer"
                        )
                        .button2(
                                "Annuler"
                        )
                        .validResultHandler(
                                formService.sync(player, response -> {

                                    if (response.clickedButtonId() != 0) {

                                        refresh(
                                                player,
                                                target.playerId()
                                        );

                                        return;
                                    }

                                    ActionResult result =
                                            guildSource.kickMember(
                                                    player.getUniqueId(),
                                                    target.playerId()
                                            );

                                    player.sendMessage(
                                            result.message()
                                    );

                                    if (!result.success()) {

                                        refresh(
                                                player,
                                                target.playerId()
                                        );

                                        return;
                                    }

                                    Player targetPlayer =
                                            Bukkit.getPlayer(
                                                    target.playerId()
                                            );

                                    if (targetPlayer != null
                                            && targetPlayer.isOnline()) {

                                        targetPlayer.sendMessage(
                                                "Vous avez été expulsé de votre guilde."
                                        );
                                    }

                                    new GuildMembersForm(
                                            guildSource,
                                            formService
                                    ).open(player);
                                })
                        )
                        .closedOrInvalidResultHandler(
                                formService.sync(
                                        player,
                                        () -> refresh(
                                                player,
                                                target.playerId()
                                        )
                                )
                        )
                        .build();

        formService.sendForm(
                player,
                form
        );
    }

    private void refresh(
            Player player,
            UUID targetId
    ) {

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(
                        player.getUniqueId()
                );

        if (optionalGuild.isEmpty()) {
            return;
        }

        Optional<GuildMember> optionalMember =
                optionalGuild.get()
                        .findMember(
                                targetId
                        );

        if (optionalMember.isEmpty()) {

            new GuildMembersForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        open(
                player,
                optionalMember.get()
        );
    }

    private String getRankName(
            GuildRank rank
    ) {

        return switch (rank) {
            case OWNER -> "Chef";
            case DEPUTY -> "Adjoint";
            case MEMBER -> "Membre";
            case RECRUIT -> "Recrue";
        };
    }
}