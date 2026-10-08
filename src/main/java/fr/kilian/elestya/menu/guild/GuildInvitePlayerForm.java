package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.GuildPermission;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;

import java.util.Objects;

public class GuildInvitePlayerForm {

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildInvitePlayerForm(
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

        open(
                player,
                "",
                null
        );
    }

    private void open(
            Player player,
            String previousName,
            String errorMessage
    ) {

        if (!guildSource.hasPermission(
                player.getUniqueId(),
                GuildPermission.RECRUIT
        )) {

            player.sendMessage(
                    "Vous n'avez pas la permission d'inviter des joueurs."
            );

            new GuildMainForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        StringBuilder label =
                new StringBuilder();

        if (errorMessage != null
                && !errorMessage.isBlank()) {

            label.append(errorMessage)
                    .append("\n\n");
        }

        label.append(
                "Entrez le pseudo du joueur à inviter."
        );

        CustomForm form =
                CustomForm.builder()
                        .title(
                                "Inviter un joueur"
                        )
                        .input(
                                label.toString(),
                                "Pseudo du joueur",
                                previousName
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
                        .validResultHandler(
                                formService.sync(player, response -> {

                                    String input =
                                            response.asInput(0);

                                    String playerName =
                                            input == null
                                                    ? ""
                                                    : input.trim();

                                    ActionResult result =
                                            guildSource.invitePlayer(
                                                    player.getUniqueId(),
                                                    playerName
                                            );

                                    if (!result.success()) {

                                        open(
                                                player,
                                                playerName,
                                                result.message()
                                        );

                                        return;
                                    }

                                    player.sendMessage(
                                            result.message()
                                    );

                                    new GuildMainForm(
                                            guildSource,
                                            formService
                                    ).open(player);
                                })
                        )
                        .build();

        formService.sendForm(
                player,
                form
        );
    }
}