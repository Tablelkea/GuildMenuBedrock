package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.Guild;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GuildListForm {

    private static final String CREATE_BUTTON = "Créer une guilde";
    private static final String JOIN_BUTTON = "Demander à rejoindre";
    private static final String BACK_BUTTON = "Retour";
    private static final String SENT_REQUESTS_BUTTON = "Demandes envoyées";
    private static final String INVITATIONS_BUTTON = "Invitations reçues";
    private static final String RANKING_BUTTON = "Classement";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildListForm(
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

        boolean hasInvitations =
                !guildSource
                        .getReceivedInvitations(
                                playerId
                        )
                        .isEmpty();

        if (guildSource.findGuildByPlayer(playerId).isPresent()) {

            new GuildMainForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        List<Guild> guilds = guildSource.getGuilds()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Guild::name,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("Guildes")
                .content(
                        "Choisissez une guilde à consulter ou créez la vôtre."
                );

        for (Guild guild : guilds) {

            builder.button(
                    guild.name()
                            + "\n"
                            + guild.members().size()
                            + " membre(s)"
            );
        }

        boolean hasPendingRequests =
                !guildSource
                        .getPendingJoinRequestGuilds(
                                playerId
                        )
                        .isEmpty();

        builder.button(
                RANKING_BUTTON
        );

        if (hasInvitations) {
            builder.button(
                    INVITATIONS_BUTTON
            );
        }

        if (hasPendingRequests) {
            builder.button(
                    SENT_REQUESTS_BUTTON
            );
        }

        builder.button(
                CREATE_BUTTON
        );

        builder.validResultHandler(
                formService.sync(player, response -> {

                    int buttonId =
                            response.clickedButtonId();

                    if (buttonId < guilds.size()) {

                        Guild selectedGuild =
                                guilds.get(buttonId);

                        openGuildDetails(
                                player,
                                selectedGuild
                        );

                        return;
                    }

                    String clicked =
                            response.clickedButton().text();

                    switch (clicked) {

                        case INVITATIONS_BUTTON ->
                                new GuildInvitationsForm(
                                        guildSource,
                                        formService
                                ).open(player);

                        case SENT_REQUESTS_BUTTON ->
                                new GuildSentRequestsForm(
                                        guildSource,
                                        formService
                                ).open(player);

                        case CREATE_BUTTON ->
                                openCreateGuildForm(
                                        player
                                );
                        case RANKING_BUTTON ->
                                new GuildRankingForm(
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

    private void openGuildDetails(
            Player player,
            Guild guild
    ) {

        Guild currentGuild = guildSource
                .findGuildById(guild.id())
                .orElse(null);

        if (currentGuild == null) {

            player.sendMessage(
                    "Cette guilde n'existe plus."
            );

            open(player);
            return;
        }

        SimpleForm form = SimpleForm.builder()
                .title(currentGuild.name())
                .content(
                        "Nom : "
                                + currentGuild.name()
                                + "\nMembres : "
                                + currentGuild.members().size()
                                + "\nBanque : "
                                + currentGuild.balance()
                )
                .button(JOIN_BUTTON)
                .button(BACK_BUTTON)
                .validResultHandler(
                        formService.sync(player, response -> {

                            String clicked =
                                    response.clickedButton().text();

                            switch (clicked) {

                                case JOIN_BUTTON -> {

                                    ActionResult result =
                                            guildSource.requestToJoin(
                                                    player.getUniqueId(),
                                                    currentGuild.id()
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

    private void openCreateGuildForm(
            Player player
    ) {

        openCreateGuildForm(
                player,
                "",
                "",
                null
        );
    }

    private void openCreateGuildForm(
            Player player,
            String previousName,
            String previousEntryMessage,
            String errorMessage
    ) {

        CustomForm.Builder builder =
                CustomForm.builder()
                        .title(
                                "Créer une guilde"
                        );

        if (errorMessage != null
                && !errorMessage.isBlank()) {

            builder.label(
                    "Erreur : "
                            + errorMessage
            );
        }

        builder.label(
                "Règles du nom :\n"
                        + "• 3 à 16 caractères\n"
                        + "• Lettres et chiffres\n"
                        + "• Tiret : -\n"
                        + "• Tiret bas : _"
        );

        builder.input(
                "Nom de la guilde",
                "Exemple : Aurora",
                previousName
        );

        builder.input(
                "Message d'entrée",
                "Exemple : Bienvenue dans notre guilde !",
                previousEntryMessage
        );

        builder.closedOrInvalidResultHandler(
                formService.sync(
                        player,
                        () -> open(player)
                )
        );

        builder.validResultHandler(
                formService.sync(player, response -> {

                    /*
                     * Index 0 = label des règles si aucune erreur.
                     *
                     * Pour éviter les index variables à cause du label
                     * d'erreur, on construit les index ci-dessous.
                     */
                    int nameIndex =
                            errorMessage == null
                                    || errorMessage.isBlank()
                                    ? 1
                                    : 2;

                    int messageIndex =
                            nameIndex + 1;

                    String nameInput =
                            response.asInput(
                                    nameIndex
                            );

                    String messageInput =
                            response.asInput(
                                    messageIndex
                            );

                    String name =
                            nameInput == null
                                    ? ""
                                    : nameInput.trim();

                    String entryMessage =
                            messageInput == null
                                    ? ""
                                    : messageInput.trim();

                    ActionResult result =
                            guildSource.createGuild(
                                    player.getUniqueId(),
                                    name,
                                    entryMessage
                            );

                    if (!result.success()) {

                        openCreateGuildForm(
                                player,
                                name,
                                entryMessage,
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
        );

        formService.sendForm(
                player,
                builder.build()
        );
    }


}