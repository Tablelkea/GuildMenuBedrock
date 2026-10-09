package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.guild.Guild;
import fr.kilian.elestya.api.domain.guild.GuildPermission;
import fr.kilian.elestya.api.domain.guild.GuildRank;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GuildPermissionsForm {

    private static final String DEPUTY_BUTTON = "Adjoint";
    private static final String MEMBER_BUTTON = "Membre";
    private static final String RECRUIT_BUTTON = "Recrue";
    private static final String BACK_BUTTON = "Retour";

    private static final GuildPermission[] PERMISSIONS = {
            GuildPermission.BUILD,
            GuildPermission.OPEN_CONTAINERS,
            GuildPermission.RECRUIT,
            GuildPermission.CLAIM_CHUNKS,
            GuildPermission.KICK,
            GuildPermission.MANAGE_WARP,
            GuildPermission.BANK_AND_UPGRADES,
            GuildPermission.RESERVE
    };

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildPermissionsForm(
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

        if (!guild.ownerId().equals(playerId)) {

            player.sendMessage(
                    "Seul le chef peut modifier les permissions."
            );

            return;
        }

        SimpleForm form = SimpleForm.builder()
                .title(
                        "Permissions - " + guild.name()
                )
                .content(
                        "Choisissez le rang dont vous souhaitez modifier les permissions."
                )
                .button(DEPUTY_BUTTON)
                .button(MEMBER_BUTTON)
                .button(RECRUIT_BUTTON)
                .button(BACK_BUTTON)
                .validResultHandler(
                        formService.sync(player, response -> {

                            String clicked =
                                    response.clickedButton().text();

                            switch (clicked) {

                                case DEPUTY_BUTTON -> openRankPermissions(
                                        player,
                                        GuildRank.DEPUTY
                                );

                                case MEMBER_BUTTON -> openRankPermissions(
                                        player,
                                        GuildRank.MEMBER
                                );

                                case RECRUIT_BUTTON -> openRankPermissions(
                                        player,
                                        GuildRank.RECRUIT
                                );

                                case BACK_BUTTON -> new GuildMainForm(
                                        guildSource,
                                        formService
                                ).open(player);
                            }
                        })
                )
                .build();

        formService.sendForm(
                player,
                form
        );
    }

    private void openRankPermissions(
            Player player,
            GuildRank rank
    ) {

        UUID playerId = player.getUniqueId();

        Optional<Guild> optionalGuild =
                guildSource.findGuildByPlayer(playerId);

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild = optionalGuild.get();

        if (!guild.ownerId().equals(playerId)) {

            player.sendMessage(
                    "Seul le chef peut modifier les permissions."
            );

            return;
        }

        Set<GuildPermission> currentPermissions =
                guildSource.getPermissions(
                        guild.id(),
                        rank
                );

        CustomForm.Builder builder = CustomForm.builder()
                .title(
                        "Permissions - "
                                + getRankName(rank)
                );

        for (GuildPermission permission : PERMISSIONS) {

            builder.toggle(
                    getPermissionName(permission),
                    currentPermissions.contains(permission)
            );
        }

        builder.closedOrInvalidResultHandler(
                formService.sync(
                        player,
                        () -> open(player)
                )
        );

        builder.validResultHandler(
                formService.sync(player, response -> {

                    boolean changed = false;

                    for (int i = 0; i < PERMISSIONS.length; i++) {

                        GuildPermission permission =
                                PERMISSIONS[i];

                        boolean oldValue =
                                currentPermissions.contains(
                                        permission
                                );

                        boolean newValue =
                                response.asToggle(i);

                        if (oldValue == newValue) {
                            continue;
                        }

                        ActionResult result =
                                guildSource.setPermission(
                                        playerId,
                                        rank,
                                        permission,
                                        newValue
                                );

                        if (!result.success()) {

                            player.sendMessage(
                                    result.message()
                            );

                            openRankPermissions(
                                    player,
                                    rank
                            );

                            return;
                        }

                        changed = true;
                    }

                    if (changed) {

                        player.sendMessage(
                                "Les permissions du rang "
                                        + getRankName(rank)
                                        + " ont été mises à jour."
                        );

                    } else {

                        player.sendMessage(
                                "Aucune permission n'a été modifiée."
                        );
                    }

                    open(player);
                })
        );

        formService.sendForm(
                player,
                builder.build()
        );
    }

    private String getPermissionName(
            GuildPermission permission
    ) {

        return switch (permission) {
            case BUILD -> "Construire";
            case OPEN_CONTAINERS -> "Ouvrir les conteneurs";
            case RECRUIT -> "Inviter / recruter";
            case CLAIM_CHUNKS -> "Revendiquer des chunks";
            case KICK -> "Expulser";
            case MANAGE_WARP -> "Gérer le warp";
            case BANK_AND_UPGRADES -> "Banque et améliorations";
            case RESERVE -> "Réserve";
        };
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