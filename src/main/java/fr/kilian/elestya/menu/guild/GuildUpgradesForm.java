package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildTreasuryInfo;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class GuildUpgradesForm {

    private static final String RESERVE_BUTTON =
            "Réserve commune";

    private static final String LOCKS_BUTTON =
            "Cadenas";

    private static final String BACK_BUTTON =
            "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildUpgradesForm(
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

        Optional<GuildTreasuryInfo> optionalInfo =
                guildSource.getTreasuryInfo(
                        guild.id()
                );

        if (optionalInfo.isEmpty()) {

            player.sendMessage(
                    "Les informations des améliorations sont indisponibles."
            );

            new GuildTreasuryForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        GuildTreasuryInfo info =
                optionalInfo.get();

        SimpleForm.Builder builder =
                SimpleForm.builder()
                        .title(
                                "Améliorations"
                        )
                        .content(
                                "Améliorations du trésor de "
                                        + guild.name()
                                        + "."
                        );

        builder.button(
                RESERVE_BUTTON
                        + "\n"
                        + info.reserveRows()
                        + " / "
                        + info.maxReserveRows()
                        + " rangées"
        );

        builder.button(
                LOCKS_BUTTON
                        + "\n"
                        + info.chestLocksPerMember()
                        + " / "
                        + info.maxChestLocksPerMember()
        );

        builder.button(
                BACK_BUTTON
        );

        builder.validResultHandler(
                formService.sync(player, response -> {

                    String clicked =
                            response.clickedButton().text();

                    /*
                     * Comme les boutons ont une seconde ligne,
                     * on teste le début du texte.
                     */
                    if (clicked.startsWith(
                            RESERVE_BUTTON
                    )) {

                        openReserveUpgrade(
                                player
                        );

                        return;
                    }

                    if (clicked.startsWith(
                            LOCKS_BUTTON
                    )) {

                        openChestLockUpgrade(
                                player
                        );

                        return;
                    }

                    new GuildTreasuryForm(
                            guildSource,
                            formService
                    ).open(player);
                })
        );

        builder.closedOrInvalidResultHandler(
                formService.sync(
                        player,
                        () -> new GuildTreasuryForm(
                                guildSource,
                                formService
                        ).open(player)
                )
        );

        formService.sendForm(
                player,
                builder.build()
        );
    }

    private void openReserveUpgrade(
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

        Optional<GuildTreasuryInfo> optionalInfo =
                guildSource.getTreasuryInfo(
                        guild.id()
                );

        if (optionalInfo.isEmpty()) {
            open(player);
            return;
        }

        GuildTreasuryInfo info =
                optionalInfo.get();

        if (info.reserveMaxed()) {

            player.sendMessage(
                    "La réserve commune est déjà au niveau maximum."
            );

            open(player);
            return;
        }

        String price =
                formatMoney(
                        info.reserveUpgradePrice()
                );

        ModalForm form =
                ModalForm.builder()
                        .title(
                                "Réserve commune"
                        )
                        .content(
                                "Rangée actuelle : "
                                        + info.reserveRows()
                                        + " / "
                                        + info.maxReserveRows()
                                        + "\nCases actuelles : "
                                        + info.reserveSlots()
                                        + "\n\nProchaine rangée : "
                                        + price
                                        + "\n\nAcheter cette amélioration ?"
                        )
                        .button1(
                                "Acheter"
                        )
                        .button2(
                                "Annuler"
                        )
                        .validResultHandler(
                                formService.sync(player, response -> {

                                    if (response.clickedButtonId() != 0) {
                                        open(player);
                                        return;
                                    }

                                    ActionResult result =
                                            guildSource.upgradeReserve(
                                                    player.getUniqueId()
                                            );

                                    player.sendMessage(
                                            result.message()
                                    );

                                    open(player);
                                })
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

    private void openChestLockUpgrade(
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

        Optional<GuildTreasuryInfo> optionalInfo =
                guildSource.getTreasuryInfo(
                        guild.id()
                );

        if (optionalInfo.isEmpty()) {
            open(player);
            return;
        }

        GuildTreasuryInfo info =
                optionalInfo.get();

        if (info.chestLocksMaxed()) {

            player.sendMessage(
                    "Le nombre de cadenas est déjà au maximum."
            );

            open(player);
            return;
        }

        String price =
                formatMoney(
                        info.chestLockUpgradePrice()
                );

        ModalForm form =
                ModalForm.builder()
                        .title(
                                "Cadenas"
                        )
                        .content(
                                info.chestLocksPerMember()
                                        + " coffre(s) verrouillable(s) par membre, sur "
                                        + info.maxChestLocksPerMember()
                                        + ".\n\n"
                                        + "Niveau suivant : "
                                        + price
                                        + "\n\n"
                                        + "Un cadenas ferme un coffre à tous sauf à toi, "
                                        + "à qui tu invites, et au chef de guilde.\n\n"
                                        + "Acheter cette amélioration ?"
                        )
                        .button1(
                                "Acheter"
                        )
                        .button2(
                                "Annuler"
                        )
                        .validResultHandler(
                                formService.sync(player, response -> {

                                    if (response.clickedButtonId() != 0) {
                                        open(player);
                                        return;
                                    }

                                    ActionResult result =
                                            guildSource.upgradeChestLocks(
                                                    player.getUniqueId()
                                            );

                                    player.sendMessage(
                                            result.message()
                                    );

                                    open(player);
                                })
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

    private String formatMoney(
            double amount
    ) {

        return String.format(
                Locale.FRANCE,
                "%,.2f",
                amount
        );
    }
}