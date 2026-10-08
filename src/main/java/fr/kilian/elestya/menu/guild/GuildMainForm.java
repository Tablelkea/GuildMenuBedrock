package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildMember;
import fr.kilian.elestya.api.domain.GuildPermission;
import fr.kilian.elestya.api.domain.GuildRank;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GuildMainForm {

    private static final String MEMBERS_BUTTON =
            "Membres";

    private static final String CHEST_BUTTON =
            "Coffre";

    private static final String TREASURE_BUTTON =
            "Trésor";

    private static final String RECRUITMENT_BUTTON =
            "Recrutement";

    private static final String PERMISSIONS_BUTTON =
            "Permissions";

    private static final String LEAVE_BUTTON =
            "Quitter la guilde";

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

    public void open(
            Player player
    ) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        UUID playerId =
                player.getUniqueId();

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(
                        playerId
                );

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild =
                optionalGuild.get();

        Optional<GuildMember> optionalMember =
                guild.findMember(
                        playerId
                );

        if (optionalMember.isEmpty()) {
            return;
        }

        GuildMember member =
                optionalMember.get();

        String balance =
                String.format(
                        Locale.FRANCE,
                        "%,.2f",
                        guild.balance()
                );

        SimpleForm.Builder builder =
                SimpleForm.builder()
                        .title(
                                "Guilde - "
                                        + guild.name()
                        )
                        .content(
                                "Nom : "
                                        + guild.name()
                                        + "\nMembres : "
                                        + guild.members().size()
                                        + "\nTrésor : "
                                        + balance
                                        + "\nPoints : "
                                        + guild.points()
                                        + "\nRang : "
                                        + getRankName(
                                        member.rank()
                                )
                        )
                        .button(
                                MEMBERS_BUTTON
                        )
                        .button(
                                CHEST_BUTTON
                        )
                        .button(
                                TREASURE_BUTTON
                        );

        if (guildSource.hasPermission(
                playerId,
                GuildPermission.RECRUIT
        )) {

            builder.button(
                    RECRUITMENT_BUTTON
            );
        }

        if (member.rank()
                == GuildRank.OWNER) {

            builder.button(
                    PERMISSIONS_BUTTON
            );
        }

        builder.button(
                LEAVE_BUTTON
        );

        builder.validResultHandler(
                formService.sync(player, response -> {

                    String clicked =
                            response.clickedButton().text();

                    switch (clicked) {

                        case MEMBERS_BUTTON ->
                                new GuildMembersForm(
                                        guildSource,
                                        formService
                                ).open(player);

                        case CHEST_BUTTON ->
                                new GuildChestForm(
                                        guildSource,
                                        formService
                                ).open(player);

                        case TREASURE_BUTTON ->
                                new GuildTreasuryForm(
                                        guildSource,
                                        formService
                                ).open(player);

                        case RECRUITMENT_BUTTON ->
                                new GuildRecruitmentForm(
                                        guildSource,
                                        formService
                                ).open(player);

                        case PERMISSIONS_BUTTON ->
                                new GuildPermissionsForm(
                                        guildSource,
                                        formService
                                ).open(player);

                        case LEAVE_BUTTON ->
                                new GuildLeaveConfirmForm(
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

    private String getRankName(
            GuildRank rank
    ) {

        return switch (rank) {
            case OWNER -> "Chef";
            case DEPUTY -> "Adjoint";
            case MEMBER -> "Membre";
            case RECRUIT -> "Recrue";
        };
    }
}