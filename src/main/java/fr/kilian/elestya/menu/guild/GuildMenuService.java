package fr.kilian.elestya.menu.guild;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.menu.FormService;
import org.bukkit.entity.Player;

import java.util.Objects;

public class GuildMenuService {

    private final GuildSource guildSource;
    private final FormService formService;

    public GuildMenuService(
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

    public void ouvrirMenuGuilde(Player player) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        if (!formService.isBedrockPlayer(player)) {
            return;
        }

        if (guildSource.findGuildByPlayer(
                player.getUniqueId()
        ).isPresent()) {

            new GuildMainForm(
                    guildSource,
                    formService
            ).open(player);

            return;
        }

        new GuildListForm(
                guildSource,
                formService
        ).open(player);
    }
}