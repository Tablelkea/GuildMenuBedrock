package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.Guild;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.List;
import java.util.Objects;

public class GuildRankingForm {

    private static final String BACK_BUTTON =
            "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildRankingForm(
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
            Player player
    ) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        List<Guild> ranking =
                guildSource.getGuildRanking();

        SimpleForm.Builder builder =
                SimpleForm.builder()
                        .title(
                                "Classement des guildes"
                        );

        if (ranking.isEmpty()) {

            builder.content(
                    "Aucune guilde n'a encore marqué de point."
            );

        } else {

            builder.content(
                    "Classement actuel des guildes."
            );

            for (int i = 0; i < ranking.size(); i++) {

                Guild guild =
                        ranking.get(i);

                int position =
                        i + 1;

                builder.button(
                        "#"
                                + position
                                + " - "
                                + guild.name()
                                + "\n"
                                + formatPoints(
                                guild.points()
                        )
                                + " points"
                );
            }
        }

        builder.button(
                BACK_BUTTON
        );

        builder.validResultHandler(
                formService.sync(player, response -> {

                    int buttonId =
                            response.clickedButtonId();

                    /*
                     * Une guilde du classement a été sélectionnée.
                     */
                    if (buttonId < ranking.size()) {

                        Guild selectedGuild =
                                ranking.get(buttonId);

                        openGuildDetails(
                                player,
                                selectedGuild,
                                buttonId + 1
                        );

                        return;
                    }

                    returnToPreviousMenu(
                            player
                    );
                })
        );

        formService.sendForm(
                player,
                builder.build()
        );
    }

    private void openGuildDetails(
            Player player,
            Guild guild,
            int position
    ) {

        Guild currentGuild =
                guildSource.findGuildById(
                        guild.id()
                ).orElse(null);

        if (currentGuild == null) {

            player.sendMessage(
                    "Cette guilde n'existe plus."
            );

            open(player);
            return;
        }

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "#" + position
                                        + " - "
                                        + currentGuild.name()
                        )
                        .content(
                                "Classement : #"
                                        + position
                                        + "\nPoints : "
                                        + formatPoints(
                                        currentGuild.points()
                                )
                                        + "\nMembres : "
                                        + currentGuild.members().size()
                        )
                        .button(
                                BACK_BUTTON
                        )
                        .validResultHandler(
                                formService.sync(
                                        player,
                                        response -> open(player)
                                )
                        )
                        .closedOrInvalidResultHandler(
                                formService.sync(
                                        player,
                                        () -> open(player)
                                )
                        )
                        .build();

        formService.sendForm(
                player,
                form
        );
    }

    private void returnToPreviousMenu(
            Player player
    ) {

        if (guildSource.findGuildByPlayer(
                player.getUniqueId()
        ).isPresent()) {

            new GuildMainForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        new GuildListForm(
                guildSource,
                formService
        ).open(player);
    }

    private String formatPoints(
            int points
    ) {

        return String.format(
                "%,d",
                points
        ).replace(
                ',',
                ' '
        );
    }
}