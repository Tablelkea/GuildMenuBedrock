package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildMember;
import fr.kilian.elestya.api.domain.GuildRank;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GuildMembersForm {

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildMembersForm(GuildSource guildSource, FormService formService) {
        this.guildSource = Objects.requireNonNull(guildSource, "guildSource cannot be null");
        this.formService = Objects.requireNonNull(formService, "formService cannot be null");
    }

    public void open(Player player) {

        Objects.requireNonNull(player, "player cannot be null");

        UUID uniqueId = player.getUniqueId();
        Optional<Guild> optionalGuild = guildSource.findGuildByPlayer(uniqueId);

        if (optionalGuild.isEmpty()) {
            return;
        }

        Guild guild = optionalGuild.get();

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("Membres - " + guild.name());

        for (GuildMember member : guild.members()) {
            builder.button(member.name() + "\n" + getRankName(member.rank()));
        }

        builder.button("Retour");

        builder.validResultHandler(response -> {
            int buttonId = response.clickedButtonId();

            if (buttonId < guild.members().size()) {
                GuildMember selected = guild.members().get(buttonId);

                new GuildMemberForm(guildSource, formService)
                        .open(player, selected);
            } else {
                new GuildMainForm(guildSource, formService).open(player);
            }
        });
        builder.closedOrInvalidResultHandler(() -> {
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
}
