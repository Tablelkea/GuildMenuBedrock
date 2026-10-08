package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildShopInfo;
import fr.kilian.elestya.api.domain.GuildShopItem;
import fr.kilian.elestya.api.domain.GuildTreasuryInfo;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class GuildTreasuryForm {

    private static final String BANK_BUTTON =
            "Banque";

    private static final String RESERVE_BUTTON =
            "Réserve";

    private static final String SHOP_BUTTON =
            "Boutique";

    private static final String UPGRADES_BUTTON =
            "Améliorations";

    private static final String BACK_BUTTON =
            "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildTreasuryForm(
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

        String balance =
                String.format(
                        Locale.FRANCE,
                        "%,.2f",
                        guild.balance()
                );

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "Trésor - "
                                        + guild.name()
                        )
                        .content(
                                "Banque de guilde : "
                                        + balance
                        )
                        .button(
                                BANK_BUTTON
                        )
                        .button(
                                RESERVE_BUTTON
                        )
                        .button(
                                SHOP_BUTTON
                        )
                        .button(
                                UPGRADES_BUTTON
                        )
                        .button(
                                BACK_BUTTON
                        )
                        .validResultHandler(
                                formService.sync(player, response -> {

                                    String clicked =
                                            response.clickedButton().text();

                                    switch (clicked) {

                                        case BANK_BUTTON ->
                                                new GuildBankForm(
                                                        guildSource,
                                                        formService
                                                ).open(player);

                                        case RESERVE_BUTTON ->
                                                openReserve(
                                                        player
                                                );

                                        case SHOP_BUTTON ->
                                                openShop(
                                                        player
                                                );

                                        case UPGRADES_BUTTON ->
                                                new GuildUpgradesForm(
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

    private void openReserve(
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

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                "Réserve commune"
                        )
                        .content(
                                "Rangées : "
                                        + info.reserveRows()
                                        + " / "
                                        + info.maxReserveRows()
                                        + "\nCases disponibles : "
                                        + info.reserveSlots()
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

    private void openShop(
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

        Optional<GuildShopInfo> optionalShop =
                guildSource.getGuildShopInfo(
                        guild.id()
                );

        if (optionalShop.isEmpty()) {

            player.sendMessage(
                    "Les informations de la boutique sont indisponibles."
            );

            open(player);
            return;
        }

        GuildShopInfo shop =
                optionalShop.get();

        if (!shop.hasAccess()) {
            SimpleForm form =
                    SimpleForm.builder()
                            .title(
                                    "Boutique de guilde"
                            )
                            .content(
                                    "Votre guilde a accès à la boutique.\n\n"
                                            + "Les articles affichés par la source mémoire "
                                            + "sont des données de démonstration."
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

            return;
        }

        SimpleForm.Builder builder =
                SimpleForm.builder()
                        .title(
                                "Boutique de guilde"
                        )
                        .content(
                                "Votre guilde a accès à la boutique."
                        );

        for (GuildShopItem item : shop.items()) {

            builder.button(
                    item.name()
                            + "\n"
                            + formatMoney(item.price())
                            + " pièces"
            );
        }

        builder.button(
                BACK_BUTTON
        );

        builder.validResultHandler(
                formService.sync(player, response -> {

                    int buttonId =
                            response.clickedButtonId();

                    if (buttonId < shop.items().size()) {

                        GuildShopItem selected =
                                shop.items().get(
                                        buttonId
                                );

                        openShopItem(
                                player,
                                selected
                        );

                        return;
                    }

                    open(player);
                })
        );

        builder.closedOrInvalidResultHandler(
                formService.sync(
                        player,
                        () -> open(player)
                )
        );

        formService.sendForm(
                player,
                builder.build()
        );
    }

    private void openShopItem(
            Player player,
            GuildShopItem item
    ) {

        SimpleForm form =
                SimpleForm.builder()
                        .title(
                                item.name()
                        )
                        .content(
                                "Catégorie : "
                                        + item.category()
                                        + "\n\nPrix : "
                                        + formatMoney(
                                        item.price()
                                )
                                        + " pièces"
                                        + "\n\n"
                                        + item.description()
                        )
                        .button(
                                BACK_BUTTON
                        )
                        .validResultHandler(
                                formService.sync(
                                        player,
                                        response -> openShop(player)
                                )
                        )
                        .closedOrInvalidResultHandler(
                                formService.sync(
                                        player,
                                        () -> openShop(player)
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
                "%,.0f",
                amount
        );
    }
}