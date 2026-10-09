package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.Guild;
import fr.kilian.elestya.api.domain.guild.GuildPermission;
import fr.kilian.elestya.api.domain.guild.JoinRequest;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GuildJoinRequestsForm {

    private static final String ACCEPT_BUTTON = "Accepter";
    private static final String REJECT_BUTTON = "Refuser";
    private static final String BACK_BUTTON = "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildJoinRequestsForm(
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

        UUID playerId = player.getUniqueId();

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild = optionalGuild.get();

        if (!guildSource.hasPermission(
                playerId,
                GuildPermission.RECRUIT
        )) {
            player.sendMessage(
                    "Vous n'avez pas la permission de gérer les recrutements."
            );
            return;
        }

        List<JoinRequest> requests =
                guildSource.getJoinRequests(
                        guild.id()
                );

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("Demandes d'adhésion");

        if (requests.isEmpty()) {

            builder.content(
                    "Aucune demande d'adhésion en attente."
            );

        } else {

            builder.content(
                    requests.size()
                            + " demande(s) en attente."
            );

            for (JoinRequest request : requests) {
                builder.button(
                        request.playerName()
                );
            }
        }

        builder.button(BACK_BUTTON);

        builder.validResultHandler(
                formService.sync(player, response -> {

                    int buttonId =
                            response.clickedButtonId();

                    /*
                     * Les demandes correspondent aux premiers boutons.
                     * Le dernier bouton correspond toujours à Retour.
                     */
                    if (buttonId < requests.size()) {

                        JoinRequest request =
                                requests.get(buttonId);

                        openRequest(
                                player,
                                request
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

    private void openRequest(
            Player player,
            JoinRequest request
    ) {

        SimpleForm form = SimpleForm.builder()
                .title(
                        "Demande de "
                                + request.playerName()
                )
                .content(
                        request.playerName()
                                + " souhaite rejoindre votre guilde."
                )
                .button(ACCEPT_BUTTON)
                .button(REJECT_BUTTON)
                .button(BACK_BUTTON)
                .validResultHandler(
                        formService.sync(player, response -> {

                            String clicked =
                                    response.clickedButton().text();

                            switch (clicked) {

                                case ACCEPT_BUTTON -> {

                                    ActionResult result =
                                            guildSource.acceptJoinRequest(
                                                    player.getUniqueId(),
                                                    request.playerId()
                                            );

                                    player.sendMessage(
                                            result.message()
                                    );

                                    if (result.success()) {

                                        Player joinedPlayer =
                                                Bukkit.getPlayer(
                                                        request.playerId()
                                                );

                                        if (joinedPlayer != null
                                                && joinedPlayer.isOnline()) {

                                            Guild joinedGuild =
                                                    guildSource.findGuildByPlayer(
                                                            request.playerId()
                                                    ).orElse(null);

                                            if (joinedGuild != null) {

                                                joinedPlayer.sendMessage(
                                                        "Vous avez rejoint la guilde "
                                                                + joinedGuild.name()
                                                                + "."
                                                );

                                                if (!joinedGuild.entryMessage().isBlank()) {

                                                    joinedPlayer.sendMessage(
                                                            joinedGuild.entryMessage()
                                                    );
                                                }
                                            }
                                        }
                                    }

                                    open(player);
                                }

                                case REJECT_BUTTON -> {

                                    ActionResult result =
                                            guildSource.rejectJoinRequest(
                                                    player.getUniqueId(),
                                                    request.playerId()
                                            );

                                    player.sendMessage(
                                            result.message()
                                    );

                                    open(player);
                                }

                                case BACK_BUTTON -> open(player);
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