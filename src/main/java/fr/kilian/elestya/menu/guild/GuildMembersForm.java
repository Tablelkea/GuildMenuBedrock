package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.Guild;
import fr.kilian.elestya.api.domain.guild.GuildMember;
import fr.kilian.elestya.api.domain.guild.GuildRank;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GuildMembersForm {

    private static final String BACK_BUTTON = "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildMembersForm(
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

    public void open(Player player) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        UUID playerId =
                player.getUniqueId();

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(
                        playerId
                );

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild =
                optionalGuild.get();

        /*
         * Snapshot de la liste au moment de l'ouverture.
         *
         * On utilise exactement cette même liste pour :
         * - créer les boutons
         * - retrouver le membre au clic
         *
         * Cela évite qu'un changement dans guild.members()
         * décale les index pendant que le formulaire est ouvert.
         */
        List<GuildMember> members =
                List.copyOf(
                        guild.members()
                );

        SimpleForm.Builder builder =
                SimpleForm.builder()
                        .title(
                                "Membres - "
                                        + guild.name()
                        );

        for (GuildMember member : members) {

            builder.button(
                    member.name()
                            + "\n"
                            + getRankName(
                            member.rank()
                    )
            );
        }

        builder.button(
                BACK_BUTTON
        );

        builder.validResultHandler(
                formService.sync(player, response -> {

                    int buttonId =
                            response.clickedButtonId();

                    if (buttonId < members.size()) {

                        GuildMember selected =
                                members.get(buttonId);

                        /*
                         * On vérifie que le membre existe toujours
                         * avant d'ouvrir sa fiche.
                         */
                        Optional<Guild> currentGuild =
                                guildSource.findGuildByPlayer(
                                        player.getUniqueId()
                                );

                        if (currentGuild.isEmpty()) {
                            return;
                        }

                        Optional<GuildMember> currentMember =
                                currentGuild.get()
                                        .findMember(
                                                selected.playerId()
                                        );

                        if (currentMember.isEmpty()) {

                            player.sendMessage(
                                    "Ce joueur n'est plus membre de la guilde."
                            );

                            open(player);
                            return;
                        }

                        new GuildMemberForm(
                                guildSource,
                                formService
                        ).open(
                                player,
                                currentMember.get()
                        );

                        return;
                    }

                    new GuildMainForm(
                            guildSource,
                            formService
                    ).open(player);
                })
        );

        formService.sendForm(
                player,
                builder.build()
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