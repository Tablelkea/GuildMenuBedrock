package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildWeeklyObjective;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Objects;
import java.util.Optional;

public class GuildWeeklyObjectiveForm {

    private static final String BACK_BUTTON =
            "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildWeeklyObjectiveForm(
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

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(
                        player.getUniqueId()
                );

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild =
                optionalGuild.get();

        Optional<GuildWeeklyObjective> optionalObjective =
                guildSource.getWeeklyObjective(
                        guild.id()
                );

        if (optionalObjective.isEmpty()) {

            player.sendMessage(
                    "L'objectif de la semaine est indisponible."
            );

            new GuildChestForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        GuildWeeklyObjective objective =
                optionalObjective.get();

        String status =
                objective.completed()
                        ? "Terminé"
                        : "En cours";

        String content =
                objective.description()
                        + "\n\nStatut : "
                        + status
                        + "\nProgression : "
                        + formatNumber(
                        objective.currentProgress()
                )
                        + " / "
                        + formatNumber(
                        objective.targetProgress()
                )
                        + "\nRécompense : "
                        + formatNumber(
                        objective.rewardPoints()
                )
                        + " points";

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                objective.name()
                        )
                        .content(
                                content
                        )
                        .button(
                                BACK_BUTTON
                        )
                        .validResultHandler(
                                formService.sync(
                                        player,
                                        response ->
                                                new GuildChestForm(
                                                        guildSource,
                                                        formService
                                                ).open(player)
                                )
                        )
                        .closedOrInvalidResultHandler(
                                formService.sync(
                                        player,
                                        () -> new GuildChestForm(
                                                guildSource,
                                                formService
                                        ).open(player)
                                )
                        )
                        .build();

        formService.sendForm(
                player,
                form
        );
    }

    private String formatNumber(
            int number
    ) {

        return String.format(
                "%,d",
                number
        ).replace(
                ',',
                ' '
        );
    }
}