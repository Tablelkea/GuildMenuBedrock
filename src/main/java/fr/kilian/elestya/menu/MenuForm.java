package fr.kilian.elestya.menu;

import fr.kilian.elestya.api.GuildSource;
import fr.kilian.elestya.api.ShopSource;
import fr.kilian.elestya.menu.buy.BuyMainForm;
import fr.kilian.elestya.menu.guild.GuildMainForm;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.Objects;

public class MenuForm {

    private final ShopSource shopSource;
    private final GuildSource guildSource;
    private final FormService formService;

    public MenuForm(ShopSource shopSource, GuildSource guildSource, FormService formService){
        this.shopSource = Objects.requireNonNull(shopSource, "shopSource cannot be null");
        this.guildSource = Objects.requireNonNull(guildSource, "guildSource cannot be null");
        this.formService = Objects.requireNonNull(formService, "formService cannot be null");
    }

    private final static String SHOP = "Boutique";
    private final static String SELL = "Vendre";
    private final static String HDV = "HDV";
    private final static String PROFIL = "Profile";
    private final static String QUESTS = "Quetes";
    private final static String GIFT = "Cadeaux";
    private final static String RANK = "Grade";
    private final static String GUILD = "Guilde";
    private final static String TOP = "Top";
    private final static String GEMS = "Gems";
    private final static String PASS = "Pass";
    private final static String STYLE = "Style";
    private final static String TROPHEE = "Trophées";
    private final static String JOBS = "Metiers";
    private final static String LOTERIE = "Loterie";
    private final static String WORLDS = "Mondes";
    private final static String BOOSTS = "Boosts";
    private final static String WIKI = "Wiki";


    public void open(Player player){

        Objects.requireNonNull(player, "player cannot be null");

        SimpleForm form = SimpleForm.builder()
                .button(SHOP)
                .button(SELL)
                .button(HDV)
                .button(PROFIL)
                .button(QUESTS)
                .button(GIFT)
                .button(RANK)
                .button(GUILD)
                .button(TOP)
                .button(GEMS)
                .button(PASS)
                .button(STYLE)
                .button(TROPHEE)
                .button(JOBS)
                .button(LOTERIE)
                .button(WORLDS)
                .button(BOOSTS)
                .button(WIKI)
                .closedOrInvalidResultHandler(() -> {})
                .validResultHandler(formService.sync(player, response -> {
                    String button = response.clickedButton().text();

                    switch (button){

                        case SHOP -> new BuyMainForm(shopSource, formService).open(player);
                        case GUILD -> new GuildMainForm(guildSource, formService).open(player);

                    }
                }))
                .build();

        formService.sendForm(player, form);

    }

}
