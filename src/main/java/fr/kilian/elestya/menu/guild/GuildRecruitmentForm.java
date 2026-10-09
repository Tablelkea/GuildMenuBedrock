package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.GuildPermission;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Objects;

public class GuildRecruitmentForm {

    private static final String INVITE_BUTTON =
            "Inviter un joueur";

    private static final String REQUESTS_BUTTON =
            "Demandes d'adhésion";

    private static final String BACK_BUTTON =
            "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildRecruitmentForm(
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

        if (!guildSource.hasPermission(
                player.getUniqueId(),
                GuildPermission.RECRUIT
        )) {

            player.sendMessage(
                    "Vous n'avez pas la permission de gérer le recrutement."
            );

            new GuildMainForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "Recrutement"
                        )
                        .content(
                                "Gérez les invitations et les demandes d'adhésion."
                        )
                        .button(
                                INVITE_BUTTON
                        )
                        .button(
                                REQUESTS_BUTTON
                        )
                        .button(
                                BACK_BUTTON
                        )
                        .validResultHandler(
                                formService.sync(player, response -> {

                                    String clicked =
                                            response.clickedButton().text();

                                    switch (clicked) {

                                        case INVITE_BUTTON ->
                                                new GuildInvitePlayerForm(
                                                        guildSource,
                                                        formService
                                                ).open(player);

                                        case REQUESTS_BUTTON ->
                                                new GuildJoinRequestsForm(
                                                        guildSource,
                                                        formService
                                                ).open(player);

                                        case BACK_BUTTON ->
                                                new GuildMainForm(
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
}