package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildChestInfo;
import fr.kilian.elestya.api.domain.GuildContribution;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GuildChestForm {

    private static final String SEASON_BUTTON =
            "Saison";

    private static final String TIMER_BUTTON =
            "Chrono";

    private static final String PODIUM_BUTTON =
            "Podium";

    private static final String TOP_TEN_BUTTON =
            "Top 10";

    private static final String DEPOSIT_ALL_BUTTON =
            "Tout déposer";

    private static final String CONTRIBUTIONS_BUTTON =
            "Apports";

    private static final String BACK_BUTTON =
            "Retour";
    private static final String WEEKLY_OBJECTIVE_BUTTON =
            "Objectif de la semaine";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildChestForm(
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

        Optional<GuildChestInfo> optionalChest =
                guildSource.getGuildChestInfo(
                        guild.id()
                );

        if (optionalChest.isEmpty()) {

            player.sendMessage(
                    "Les informations du coffre sont indisponibles."
            );

            new GuildMainForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        GuildChestInfo chest =
                optionalChest.get();

        String content =
                "À déposer : "
                        + chest.targetItemName()
                        + "\nProgression : "
                        + chest.depositedAmount()
                        + " / "
                        + chest.targetAmount();

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "Coffre - "
                                        + guild.name()
                        )
                        .content(
                                content
                        )
                        .button(
                                SEASON_BUTTON
                        )
                        .button(
                                TIMER_BUTTON
                        )
                        .button(
                                WEEKLY_OBJECTIVE_BUTTON

                        ).button(
                                PODIUM_BUTTON
                        )
                        .button(
                                TOP_TEN_BUTTON
                        )
                        .button(
                                DEPOSIT_ALL_BUTTON
                        )
                        .button(
                                CONTRIBUTIONS_BUTTON
                        )
                        .button(
                                BACK_BUTTON
                        )
                        .validResultHandler(
                                formService.sync(player, response -> {

                                    String clicked =
                                            response.clickedButton().text();

                                    switch (clicked) {

                                        case SEASON_BUTTON ->
                                                openSeason(
                                                        player
                                                );

                                        case TIMER_BUTTON ->
                                                openTimer(
                                                        player
                                                );

                                        case PODIUM_BUTTON ->
                                                openPodium(
                                                        player
                                                );

                                        case TOP_TEN_BUTTON ->
                                                openTopTen(
                                                        player
                                                );

                                        case DEPOSIT_ALL_BUTTON ->
                                                depositAll(
                                                        player
                                                );

                                        case CONTRIBUTIONS_BUTTON ->
                                                openContributions(
                                                        player
                                                );

                                        case BACK_BUTTON ->
                                                new GuildMainForm(
                                                        guildSource,
                                                        formService
                                                ).open(player);
                                        case WEEKLY_OBJECTIVE_BUTTON ->
                                                new GuildWeeklyObjectiveForm(
                                                        guildSource,
                                                        formService
                                                ).open(player);
                                    }
                                })
                        )
                        .closedOrInvalidResultHandler(
                                formService.sync(
                                        player,
                                        () -> new GuildMainForm(
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

    private void openSeason(
            Player player
    ) {

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(
                        player.getUniqueId()
                );

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild =
                optionalGuild.get();

        Optional<GuildChestInfo> optionalChest =
                guildSource.getGuildChestInfo(
                        guild.id()
                );

        if (optionalChest.isEmpty()) {
            open(player);
            return;
        }

        GuildChestInfo chest =
                optionalChest.get();

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "Saison"
                        )
                        .content(
                                chest.seasonName()
                                        + "\n\nPoints de la guilde : "
                                        + formatPoints(
                                        chest.points()
                                )
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

    private void openTimer(
            Player player
    ) {

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(
                        player.getUniqueId()
                );

        if (optionalGuild.isEmpty()) {
            return;
        }

        Optional<GuildChestInfo> optionalChest =
                guildSource.getGuildChestInfo(
                        optionalGuild.get().id()
                );

        if (optionalChest.isEmpty()) {
            open(player);
            return;
        }

        GuildChestInfo chest =
                optionalChest.get();

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "Chrono"
                        )
                        .content(
                                "Temps restant : "
                                        + chest.timeRemaining()
                                        + "\n\nObjectif actuel : "
                                        + chest.targetItemName()
                                        + "\nProgression : "
                                        + chest.depositedAmount()
                                        + " / "
                                        + chest.targetAmount()
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

    private void openPodium(
            Player player
    ) {

        List<Guild> ranking =
                guildSource.getGuildRanking();

        StringBuilder content =
                new StringBuilder();

        if (ranking.isEmpty()) {

            content.append(
                    "Aucune guilde classée."
            );

        } else {

            int amount =
                    Math.min(
                            3,
                            ranking.size()
                    );

            for (int i = 0; i < amount; i++) {

                Guild guild =
                        ranking.get(i);

                content.append(
                        "#"
                );

                content.append(
                        i + 1
                );

                content.append(
                        " "
                );

                content.append(
                        guild.name()
                );

                content.append(
                        " - "
                );

                content.append(
                        formatPoints(
                                guild.points()
                        )
                );

                content.append(
                        " points"
                );

                if (i + 1 < amount) {
                    content.append("\n");
                }
            }
        }

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "Podium"
                        )
                        .content(
                                content.toString()
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

    private void openTopTen(
            Player player
    ) {

        List<Guild> ranking =
                guildSource.getGuildRanking();

        StringBuilder content =
                new StringBuilder();

        int amount =
                Math.min(
                        10,
                        ranking.size()
                );

        if (amount == 0) {

            content.append(
                    "Aucune guilde classée."
            );

        } else {

            for (int i = 0; i < amount; i++) {

                Guild guild =
                        ranking.get(i);

                content.append(
                        "#"
                );

                content.append(
                        i + 1
                );

                content.append(
                        " "
                );

                content.append(
                        guild.name()
                );

                content.append(
                        " - "
                );

                content.append(
                        formatPoints(
                                guild.points()
                        )
                );

                content.append(
                        " points"
                );

                if (i + 1 < amount) {
                    content.append("\n");
                }
            }
        }

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "Top 10"
                        )
                        .content(
                                content.toString()
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

    private void depositAll(
            Player player
    ) {

        ActionResult result =
                guildSource.depositAllChestItems(
                        player.getUniqueId()
                );

        player.sendMessage(
                result.message()
        );

        open(player);
    }

    private void openContributions(
            Player player
    ) {

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(
                        player.getUniqueId()
                );

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild =
                optionalGuild.get();

        List<GuildContribution> contributions =
                guildSource.getGuildContributions(
                        guild.id()
                );

        StringBuilder content =
                new StringBuilder();

        if (contributions.isEmpty()) {

            content.append(
                    "Aucun apport enregistré."
            );

        } else {

            for (int i = 0;
                 i < contributions.size();
                 i++) {

                GuildContribution contribution =
                        contributions.get(i);

                content.append(
                        "#"
                );

                content.append(
                        i + 1
                );

                content.append(
                        " "
                );

                content.append(
                        contribution.playerName()
                );

                content.append(
                        " - "
                );

                content.append(
                        contribution.amount()
                );

                content.append(
                        " apport(s)"
                );

                if (i + 1
                        < contributions.size()) {

                    content.append(
                            "\n"
                    );
                }
            }
        }

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "Apports"
                        )
                        .content(
                                content.toString()
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