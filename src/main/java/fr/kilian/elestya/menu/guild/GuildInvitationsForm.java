package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.Guild;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GuildInvitationsForm {

    private static final String ACCEPT_BUTTON =
            "Accepter";

    private static final String REJECT_BUTTON =
            "Refuser";

    private static final String BACK_BUTTON =
            "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildInvitationsForm(
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

        if (guildSource.findGuildByPlayer(
                player.getUniqueId()
        ).isPresent()) {

            new GuildMainForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        List<Guild> invitations =
                guildSource
                        .getReceivedInvitations(
                                player.getUniqueId()
                        )
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        Guild::name,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        SimpleForm.Builder builder =
                SimpleForm.builder()
                        .title(
                                "Invitations reçues"
                        );

        if (invitations.isEmpty()) {

            builder.content(
                    "Vous n'avez aucune invitation en attente."
            );

        } else {

            builder.content(
                    invitations.size()
                            + " invitation(s) en attente."
            );

            for (Guild guild : invitations) {

                builder.button(
                        guild.name()
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

                    if (buttonId < invitations.size()) {

                        openInvitation(
                                player,
                                invitations.get(
                                        buttonId
                                )
                        );

                        return;
                    }

                    new GuildListForm(
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

    private void openInvitation(
            Player player,
            Guild guild
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
                                "Invitation - "
                                        + currentGuild.name()
                        )
                        .content(
                                "La guilde "
                                        + currentGuild.name()
                                        + " vous invite à la rejoindre.\n\n"
                                        + "Vous rejoindrez la guilde avec le rang Recrue."
                        )
                        .button(
                                ACCEPT_BUTTON
                        )
                        .button(
                                REJECT_BUTTON
                        )
                        .button(
                                BACK_BUTTON
                        )
                        .validResultHandler(
                                formService.sync(player, response -> {

                                    String clicked =
                                            response.clickedButton().text();

                                    switch (clicked) {

                                        case ACCEPT_BUTTON -> {

                                            ActionResult result =
                                                    guildSource.acceptInvitation(
                                                            player.getUniqueId(),
                                                            currentGuild.id()
                                                    );

                                            player.sendMessage(
                                                    result.message()
                                            );

                                            if (result.success()) {

                                                Guild joinedGuild =
                                                        guildSource.findGuildByPlayer(
                                                                player.getUniqueId()
                                                        ).orElse(null);

                                                if (joinedGuild != null
                                                        && !joinedGuild.entryMessage().isBlank()) {

                                                    player.sendMessage(
                                                            joinedGuild.entryMessage()
                                                    );
                                                }

                                                new GuildMainForm(
                                                        guildSource,
                                                        formService
                                                ).open(player);

                                                return;
                                            }

                                            open(player);
                                        }

                                        case REJECT_BUTTON -> {

                                            ActionResult result =
                                                    guildSource.rejectInvitation(
                                                            player.getUniqueId(),
                                                            currentGuild.id()
                                                    );

                                            player.sendMessage(
                                                    result.message()
                                            );

                                            open(player);
                                        }

                                        case BACK_BUTTON ->
                                                open(player);
                                    }
                                })
                        )
                        .build();

        formService.sendForm(
                player,
                form
        );
    }
}