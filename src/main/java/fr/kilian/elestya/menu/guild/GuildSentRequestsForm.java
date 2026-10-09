package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.Guild;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GuildSentRequestsForm {

    private static final String BACK_BUTTON = "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildSentRequestsForm(GuildSource guildSource, FormService formService) {
        this.guildSource = Objects.requireNonNull(guildSource, "guildSource cannot be null");

        this.formService = Objects.requireNonNull(formService, "formService cannot be null");
    }

    public void open(Player player) {

        Objects.requireNonNull(player, "player cannot be null");

        /*
         * Dès que le joueur possède une guilde,
         * ses demandes ne sont plus pertinentes.
         */
        if (guildSource.findGuildByPlayer(player.getUniqueId()).isPresent()) {

            new GuildMainForm(guildSource, formService).open(player);

            return;
        }

        List<Guild> requestedGuilds = guildSource.getPendingJoinRequestGuilds(player.getUniqueId()).stream().sorted(Comparator.comparing(Guild::name, String.CASE_INSENSITIVE_ORDER)).toList();

        SimpleForm.Builder builder = SimpleForm.builder().title("Demandes envoyées");

        if (requestedGuilds.isEmpty()) {

            builder.content("Vous n'avez aucune demande d'adhésion en attente.");

        } else {

            builder.content(requestedGuilds.size() + " demande(s) d'adhésion en attente.");

            for (Guild guild : requestedGuilds) {

                builder.button(guild.name() + "\nEn attente");
            }
        }

        builder.button(BACK_BUTTON);

        builder.validResultHandler(formService.sync(player, response -> {

            int buttonId = response.clickedButtonId();

            if (buttonId < requestedGuilds.size()) {

                Guild selectedGuild = requestedGuilds.get(buttonId);

                openRequest(player, selectedGuild);

                return;
            }

            new GuildListForm(guildSource, formService).open(player);
        }));

        formService.sendForm(player, builder.build());
    }

    private void openRequest(Player player, Guild guild) {

        /*
         * On vérifie que la guilde existe toujours.
         */
        Guild currentGuild = guildSource.findGuildById(guild.id()).orElse(null);

        if (currentGuild == null) {

            player.sendMessage("Cette guilde n'existe plus.");

            open(player);
            return;
        }

        ModalForm form = ModalForm.builder().title("Demande - " + currentGuild.name()).content("Votre demande pour rejoindre " + currentGuild.name() + " est toujours en attente.\n\n" + "Voulez-vous l'annuler ?").button1("Annuler la demande").button2("Retour").validResultHandler(formService.sync(player, response -> {

            if (response.clickedButtonId() != 0) {

                open(player);
                return;
            }

            ActionResult result = guildSource.cancelJoinRequest(player.getUniqueId(), currentGuild.id());

            player.sendMessage(result.message());

            open(player);
        })).closedOrInvalidResultHandler(formService.sync(player, () -> open(player))).build();

        formService.sendForm(player, form);
    }
}