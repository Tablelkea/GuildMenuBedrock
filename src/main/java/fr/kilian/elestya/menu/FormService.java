package fr.kilian.elestya.menu;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.Form;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Objects;
import java.util.UUID;

public class FormService {

    private final FloodgateApi api;

    public FormService(FloodgateApi api) {
        this.api = Objects.requireNonNull(api, "api cannot be null");
    }

    public boolean isBedrockPlayer(Player player) {
        Objects.requireNonNull(player, "player cannot be null");

        UUID playerId = player.getUniqueId();
        return api.isFloodgatePlayer(playerId);
    }

    public boolean sendForm(Player player, Form form) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(form, "form cannot be null");

        if (!api.isFloodgatePlayer(player.getUniqueId())) {
            return false;
        }

        FloodgatePlayer fplayer = api.getPlayer(player.getUniqueId());

        return fplayer.sendForm(form);
    }

}
