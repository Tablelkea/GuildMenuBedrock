package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildMember;
import fr.kilian.elestya.api.domain.GuildPermission;
import fr.kilian.elestya.api.domain.GuildRank;
import fr.kilian.elestya.api.result.ActionResult;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GuildMemberForm {

    private static final String PROMOTE_BUTTON = "Promouvoir";
    private static final String DEMOTE_BUTTON = "Rétrograder";
    private static final String KICK_BUTTON = "Expulser";
    private static final String BACK_BUTTON = "Retour";
    private final GuildSource guildSource;
    private final FormService formService;

    public GuildMemberForm(GuildSource guildSource, FormService formService) {
        this.guildSource = Objects.requireNonNull(guildSource, "guildSource cannot be null");
        this.formService = Objects.requireNonNull(formService, "formService cannot be null");
    }

    public void open(Player player, GuildMember target) {

        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(target, "target cannot be null");

        UUID actorId = player.getUniqueId();
        UUID targetId = target.playerId();

        Optional<Guild> optionalGuild = guildSource.findGuildByPlayer(actorId);

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild = optionalGuild.get();

        GuildRank rank = target.rank();
        SimpleForm.Builder builder = SimpleForm.builder().title(target.name()).content("Rang: " + getRankName(rank));

        if (guild.ownerId().equals(actorId) && !rank.equals(GuildRank.OWNER)) {
            if (!rank.equals(GuildRank.RECRUIT)) {
                builder.button(DEMOTE_BUTTON);
            }
            if (!target.isDeputy()) {
                builder.button(PROMOTE_BUTTON);
            }
        }

        if (guildSource.hasPermission(actorId, GuildPermission.KICK) && !target.isOwner() && !targetId.equals(actorId)) {
            builder.button(KICK_BUTTON);
        }

        builder.button(BACK_BUTTON);

        builder.validResultHandler(response -> {
            String clicked = response.clickedButton().text();
            switch (clicked) {

                case DEMOTE_BUTTON -> {
                    ActionResult result = guildSource.demoteMember(actorId, targetId);

                    player.sendMessage(result.message());

                    if (result.success()) {
                        Player targetPlayer = Bukkit.getPlayer(targetId);

                        if (targetPlayer != null) {
                            targetPlayer.sendMessage("Vous avez été rétrogradé.");
                        }

                        refresh(player, targetId);

                    }
                }
                case PROMOTE_BUTTON -> {
                    ActionResult result = guildSource.promoteMember(actorId, targetId);

                    player.sendMessage(result.message());

                    if (result.success()) {
                        Player targetPlayer = Bukkit.getPlayer(targetId);

                        if (targetPlayer != null) {
                            targetPlayer.sendMessage("Vous avez été promu.");
                        }

                        refresh(player, targetId);
                    }
                }
                case KICK_BUTTON -> openKickConfirmation(player, target);
                case BACK_BUTTON -> new GuildMembersForm(guildSource, formService).open(player);
                default -> {
                }
            }
        });

        SimpleForm form = builder.build();
        formService.sendForm(player, form);

    }

    private String getRankName(GuildRank rank) {
        switch (rank) {
            case RECRUIT -> {
                return "Recrue";
            }
            case MEMBER -> {
                return "Membre";
            }
            case DEPUTY -> {
                return "Adjoint";
            }
            case OWNER -> {
                return "Chef";
            }
        }

        return "Inconnue";
    }

    private void openKickConfirmation(Player player, GuildMember target) {

        ModalForm form = ModalForm.builder().title("Expulser un membre").content("Voulez-vous vraiment expulser " + target.name() + " ?").button1("Confirmer").button2("Annuler").validResultHandler(confirmation -> {
            if (confirmation.clickedButtonId() == 0) {
                ActionResult result = guildSource.kickMember(player.getUniqueId(), target.playerId());
                player.sendMessage(result.message());

                if (result.success()) {
                    Player targetPlayer = Bukkit.getPlayer(target.playerId());
                    if (targetPlayer != null) {
                        targetPlayer.sendMessage("Vous avez été expulsé de votre guilde.");
                    }
                    new GuildMembersForm(guildSource, formService).open(player);
                } else {
                    open(player, target);
                }
            } else {
                open(player, target);
            }
        }).build();
        formService.sendForm(player, form);

    }

    private void refresh(Player player, UUID targetId) {
        Optional<Guild> targetGuild = guildSource.findGuildByPlayer(targetId);

        if (targetGuild.isEmpty()) {
            return;
        }

        Optional<GuildMember> member = targetGuild.get().findMember(targetId);

        if (member.isEmpty()) {
            return;
        }

        open(player, member.get());
    }

}
