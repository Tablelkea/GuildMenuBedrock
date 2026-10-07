package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
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

        /*
         * Si le joueur possède déjà une guilde,
         * il n'a plus rien à faire dans ce menu.
         */
        if (guildSource.findGuildByPlayer(playerId).isPresent()) {
            new GuildMainForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        /*
         * On trie simplement par nom afin que la liste reste
         * stable même si MemoryGuildSource utilise une HashMap.
         */
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

        builder.button(CREATE_BUTTON);

        builder.validResultHandler(response -> {

            int buttonId = response.clickedButtonId();

            /*
             * Tous les premiers boutons correspondent aux guildes.
             */
            if (buttonId < guilds.size()) {

                Guild selectedGuild =
                        guilds.get(buttonId);

                openGuildDetails(
                        player,
                        selectedGuild
                );

                return;
            }

            /*
             * Le dernier bouton est "Créer une guilde".
             */
            openCreateGuildForm(player);
        });

        formService.sendForm(
                player,
                builder.build()
        );
    }

    private void openGuildDetails(
            Player player,
            Guild guild
    ) {

        /*
         * On récupère une version récente de la guilde.
         * Elle a pu changer depuis l'ouverture de la liste.
         */
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
                        "Nom : " + currentGuild.name()
                                + "\nMembres : " + currentGuild.members().size()
                                + "\nBanque : " + currentGuild.balance()
                )
                .button(JOIN_BUTTON)
                .button(BACK_BUTTON)
                .validResultHandler(response -> {

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

                        default -> {
                        }
                    }
                })
                .build();

        formService.sendForm(
                player,
                form
        );
    }

    private void openCreateGuildForm(Player player) {

        CustomForm form = CustomForm.builder()
                .title("Créer une guilde")
                .input(
                        "Nom de la guilde",
                        "Exemple : Aurora"
                )
                .closedOrInvalidResultHandler(
                        () -> open(player)
                )
                .validResultHandler(response -> {

                    String name =
                            response.asInput(0);

                    if (name == null || name.isBlank()) {

                        player.sendMessage(
                                "Veuillez saisir un nom de guilde."
                        );

                        openCreateGuildForm(player);
                        return;
                    }

                    ActionResult result =
                            guildSource.createGuild(
                                    player.getUniqueId(),
                                    name.trim()
                            );

                    player.sendMessage(
                            result.message()
                    );

                    if (result.success()) {

                        /*
                         * Le joueur est maintenant chef de sa guilde.
                         */
                        new GuildMainForm(
                                guildSource,
                                formService
                        ).open(player);

                        return;
                    }

                    openCreateGuildForm(player);
                })
                .build();

        formService.sendForm(
                player,
                form
        );
    }
}