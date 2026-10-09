package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.Guild;
import fr.kilian.elestya.api.domain.guild.GuildPermission;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GuildBankForm {

    private static final String DEPOSIT_BUTTON = "Déposer";
    private static final String WITHDRAW_BUTTON = "Retirer";
    private static final String BACK_BUTTON = "Retour";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildBankForm(GuildSource guildSource, FormService formService) {
        this.guildSource = Objects.requireNonNull(guildSource, "guildSource cannot be null");

        this.formService = Objects.requireNonNull(formService, "formService cannot be null");
    }

    public void open(Player player) {

        Objects.requireNonNull(player, "player cannot be null");

        UUID playerId = player.getUniqueId();

        Optional<Guild> optionalGuild = guildSource.findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild = optionalGuild.get();

        String balance = String.format(Locale.FRANCE, "%.2f", guild.balance());

        SimpleForm.Builder builder = SimpleForm.builder().title("Banque - " + guild.name()).content("Solde de la guilde : " + balance).button(DEPOSIT_BUTTON);

        if (guildSource.hasPermission(playerId, GuildPermission.BANK_AND_UPGRADES)) {
            builder.button(WITHDRAW_BUTTON);
        }

        builder.button(BACK_BUTTON);

        builder.validResultHandler(formService.sync(player, response -> {

            String clicked = response.clickedButton().text();

            switch (clicked) {

                case DEPOSIT_BUTTON -> openDepositForm(player);

                case WITHDRAW_BUTTON -> openWithdrawForm(player);

                case BACK_BUTTON -> new GuildTreasuryForm(guildSource, formService).open(player);

                default -> {
                }
            }
        }));

        SimpleForm form = builder.build();

        formService.sendForm(player, form);
    }

    private void openDepositForm(Player player) {

        UUID playerId = player.getUniqueId();

        CustomForm form = CustomForm.builder().title("Déposer de l'argent").input("Montant à déposer", "Exemple : 500").closedOrInvalidResultHandler(formService.sync(player, () -> open(player))).validResultHandler(formService.sync(player, response -> {

            String input = response.asInput(0);

            Double amount = parseAmount(input);

            if (amount == null) {

                player.sendMessage("Veuillez saisir un montant valide.");

                openDepositForm(player);
                return;
            }

            ActionResult result = guildSource.deposit(playerId, amount);

            player.sendMessage(result.message());

            open(player);
        })).build();

        formService.sendForm(player, form);
    }

    private void openWithdrawForm(Player player) {

        UUID playerId = player.getUniqueId();

        CustomForm form = CustomForm.builder().title("Retirer de l'argent").input("Montant à retirer", "Exemple : 500").closedOrInvalidResultHandler(formService.sync(player, () -> open(player))).validResultHandler(formService.sync(player, response -> {

            String input = response.asInput(0);

            Double amount = parseAmount(input);

            if (amount == null) {

                player.sendMessage("Veuillez saisir un montant valide.");

                openWithdrawForm(player);
                return;
            }

            ActionResult result = guildSource.withdraw(playerId, amount);

            player.sendMessage(result.message());

            open(player);
        })).build();

        formService.sendForm(player, form);
    }

    private Double parseAmount(String input) {

        if (input == null || input.isBlank()) {
            return null;
        }

        try {

            double amount = Double.parseDouble(input.trim().replace(',', '.'));

            if (!Double.isFinite(amount)) {
                return null;
            }

            if (amount <= 0) {
                return null;
            }

            return amount;

        } catch (NumberFormatException exception) {
            return null;
        }
    }
}