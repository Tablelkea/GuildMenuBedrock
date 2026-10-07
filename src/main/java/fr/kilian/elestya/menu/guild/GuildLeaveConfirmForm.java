package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.ModalForm;

import java.util.Objects;

public class GuildLeaveConfirmForm {

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildLeaveConfirmForm(
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

        ModalForm form = ModalForm.builder()
                .title("Quitter la guilde")
                .content(
                        "Voulez-vous vraiment quitter votre guilde ?"
                )
                .button1("Confirmer")
                .button2("Annuler")
                .validResultHandler(response -> {

                    if (response.clickedButtonId() == 0) {

                        ActionResult result =
                                guildSource.leaveGuild(
                                        player.getUniqueId()
                                );

                        player.sendMessage(
                                result.message()
                        );

                        if (result.success()) {

                            new GuildListForm(
                                    guildSource,
                                    formService
                            ).open(player);

                        } else {

                            new GuildMainForm(
                                    guildSource,
                                    formService
                            ).open(player);
                        }

                        return;
                    }

                    new GuildMainForm(
                            guildSource,
                            formService
                    ).open(player);
                })
                .closedOrInvalidResultHandler(
                        () -> new GuildMainForm(
                                guildSource,
                                formService
                        ).open(player)
                )
                .build();

        formService.sendForm(
                player,
                form
        );
    }
}