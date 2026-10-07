package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildMember;
import fr.kilian.elestya.api.domain.GuildPermission;
import fr.kilian.elestya.api.domain.GuildRank;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GuildMainForm {

    private static final String MEMBERS_BUTTON = "Membres";
    private static final String BANK_BUTTON = "Banque";
    private static final String REQUESTS_BUTTON = "Demandes d'adhésion";
    private static final String PERMISSIONS_BUTTON = "Permissions";
    private static final String LEAVE_BUTTON = "Quitter la guilde";

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildMainForm(
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

        Optional<GuildMember> optionalMember =
                guild.findMember(playerId);

        if (optionalMember.isEmpty()) {
            return;
        }

        GuildMember member = optionalMember.get();

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("Guilde - " + guild.name())
                .content(
                        "Nom : " + guild.name()
                                + "\nMembres : " + guild.members().size()
                                + "\nBanque : " + guild.balance()
                                + "\nRang : " + getRankName(member.rank())
                )
                .button(MEMBERS_BUTTON)
                .button(BANK_BUTTON);

        if (guildSource.hasPermission(
                playerId,
                GuildPermission.RECRUIT
        )) {
            builder.button(REQUESTS_BUTTON);
        }

        if (member.rank() == GuildRank.OWNER) {
            builder.button(PERMISSIONS_BUTTON);
        }

        builder.button(LEAVE_BUTTON);
        builder.closedOrInvalidResultHandler(() -> {
        });
        builder.validResultHandler(response -> {
            String button = response.clickedButton().text();

            switch (button) {
                case MEMBERS_BUTTON -> new GuildMembersForm(guildSource, formService).open(player);
                case BANK_BUTTON -> new GuildBankForm(guildSource, formService).open(player);
                case REQUESTS_BUTTON -> new GuildJoinRequestsForm(guildSource, formService).open(player);
                case PERMISSIONS_BUTTON -> new GuildPermissionsForm(guildSource, formService).open(player);
                case LEAVE_BUTTON -> new GuildLeaveConfirmForm(guildSource, formService).open(player);
                default -> {
                }
            }
        });

        SimpleForm form = builder.build();

        formService.sendForm(
                player,
                form
        );
    }

    private String getRankName(GuildRank rank) {

        return switch (rank) {
            case OWNER -> "Chef";
            case DEPUTY -> "Adjoint";
            case MEMBER -> "Membre";
            case RECRUIT -> "Recrue";
        };
    }
}