package fr.kilian.elestya.source.memory;

import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildMember;
import fr.kilian.elestya.api.domain.GuildPermission;
import fr.kilian.elestya.api.domain.GuildRank;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemoryGuildSeeder {

    private final MemoryGuildSource source;

    public MemoryGuildSeeder(MemoryGuildSource source) {
        this.source = source;
    }

    public void seed() {

        Guild aurora = createGuild(
                "aurora",
                "Aurora",
                "Aelys",
                15_000,
                "Nerion",
                "Kael",
                "Lyria",
                "Thane"
        );

        configureBalancedPermissions(aurora);
        source.addGuild(aurora);


        Guild valoria = createGuild(
                "valoria",
                "Valoria",
                "Eryndor",
                8_500,
                "Mirael",
                "Soren",
                "Nyra"
        );

        configureStrictPermissions(valoria);
        source.addGuild(valoria);


        Guild eclipse = createGuild(
                "eclipse",
                "Eclipse",
                "Noctis",
                23_000,
                "Selene",
                "Orion",
                "Veyra",
                "Kian"
        );

        configurePermissivePermissions(eclipse);
        source.addGuild(eclipse);


        Guild ember = createGuild(
                "ember",
                "Ember",
                "Kaelis",
                4_200,
                "Riven",
                "Ashen",
                "Fiora"
        );

        configureBalancedPermissions(ember);
        source.addGuild(ember);


        Guild celestia = createGuild(
                "celestia",
                "Celestia",
                "Astra",
                17_800,
                "Lyanna",
                "Elios",
                "Seraph"
        );

        configurePermissivePermissions(celestia);
        source.addGuild(celestia);


        Guild ironclad = createGuild(
                "ironclad",
                "Ironclad",
                "Draven",
                10_500,
                "Brom",
                "Garrick",
                "Torin"
        );

        configureStrictPermissions(ironclad);
        source.addGuild(ironclad);


        Guild verdant = createGuild(
                "verdant",
                "Verdant",
                "Sylva",
                6_700,
                "Rowan",
                "Ivy",
                "Flora"
        );

        configureBalancedPermissions(verdant);
        source.addGuild(verdant);


        Guild obsidian = createGuild(
                "obsidian",
                "Obsidian",
                "Mordren",
                30_000,
                "Varek",
                "Darius",
                "Nyx"
        );

        configureStrictPermissions(obsidian);
        source.addGuild(obsidian);


        Guild stormborn = createGuild(
                "stormborn",
                "Stormborn",
                "Raegar",
                12_400,
                "Tempest",
                "Zephyr",
                "Ardyn"
        );

        configurePermissivePermissions(stormborn);
        source.addGuild(stormborn);


        Guild lunaris = createGuild(
                "lunaris",
                "Lunaris",
                "Selena",
                9_900,
                "Luna",
                "Cerys",
                "Elara"
        );

        configureBalancedPermissions(lunaris);
        source.addGuild(lunaris);


        Guild phoenix = createGuild(
                "phoenix",
                "Phoenix",
                "Ignis",
                19_500,
                "Cinder",
                "Blaze",
                "Emberlyn"
        );

        configurePermissivePermissions(phoenix);
        source.addGuild(phoenix);


        Guild frostborn = createGuild(
                "frostborn",
                "Frostborn",
                "Skadi",
                7_300,
                "Bjorn",
                "Ylva",
                "Freya"
        );

        configureStrictPermissions(frostborn);
        source.addGuild(frostborn);


        Guild arcadia = createGuild(
                "arcadia",
                "Arcadia",
                "Cassian",
                11_200,
                "Adriel",
                "Maeve",
                "Elric"
        );

        configureBalancedPermissions(arcadia);
        source.addGuild(arcadia);


        Guild ravenfall = createGuild(
                "ravenfall",
                "Ravenfall",
                "Corvin",
                14_600,
                "Raven",
                "Silas",
                "Morrigan"
        );

        configureStrictPermissions(ravenfall);
        source.addGuild(ravenfall);


        Guild solaris = createGuild(
                "solaris",
                "Solaris",
                "Helios",
                21_000,
                "Sol",
                "Lucian",
                "Aurelia"
        );

        configurePermissivePermissions(solaris);
        source.addGuild(solaris);


        Guild wildHunt = createGuild(
                "wildhunt",
                "Wild Hunt",
                "Fenrir",
                5_800,
                "Hati",
                "Skoll",
                "Ulf"
        );

        configureBalancedPermissions(wildHunt);
        source.addGuild(wildHunt);


        Guild evernight = createGuild(
                "evernight",
                "Evernight",
                "Vesper",
                16_250,
                "Shade",
                "Umbra",
                "Nox"
        );

        configureStrictPermissions(evernight);
        source.addGuild(evernight);


        Guild silverwing = createGuild(
                "silverwing",
                "Silverwing",
                "Alaric",
                13_750,
                "Aerion",
                "Gale",
                "Ciel"
        );

        configureBalancedPermissions(silverwing);
        source.addGuild(silverwing);


        Guild dragonspire = createGuild(
                "dragonspire",
                "Dragonspire",
                "Vaelor",
                27_000,
                "Rhaen",
                "Drake",
                "Syrax"
        );

        configurePermissivePermissions(dragonspire);
        source.addGuild(dragonspire);


        Guild horizon = createGuild(
                "horizon",
                "Horizon",
                "Atlas",
                18_400,
                "Nova",
                "Aster",
                "Skye"
        );

        configureBalancedPermissions(horizon);
        source.addGuild(horizon);


        seedJoinRequests();
    }

    private Guild createGuild(
            String id,
            String name,
            String ownerName,
            double balance,
            String deputyName,
            String... memberNames
    ) {

        UUID ownerId = uuid(name + ":" + ownerName);

        List<GuildMember> members = new ArrayList<>();

        members.add(
                new GuildMember(
                        ownerId,
                        ownerName,
                        GuildRank.OWNER
                )
        );

        members.add(
                new GuildMember(
                        uuid(name + ":" + deputyName),
                        deputyName,
                        GuildRank.DEPUTY
                )
        );

        for (String memberName : memberNames) {

            members.add(
                    new GuildMember(
                            uuid(name + ":" + memberName),
                            memberName,
                            GuildRank.MEMBER
                    )
            );
        }

        /*
         * Ajoute également une recrue fictive
         * pour pouvoir tester les différents rangs.
         */
        String recruitName = name + "Recruit";

        members.add(
                new GuildMember(
                        uuid(name + ":" + recruitName),
                        recruitName,
                        GuildRank.RECRUIT
                )
        );

        return new Guild(
                id,
                name,
                ownerId,
                members,
                balance,
                new ArrayList<>()
        );
    }

    /*
     * Configuration "équilibrée".
     *
     * Adjoint :
     * presque tous les droits.
     *
     * Membre :
     * droits classiques.
     *
     * Recrue :
     * très peu de droits.
     */
    private void configureBalancedPermissions(Guild guild) {

        enable(
                guild,
                GuildRank.DEPUTY,
                GuildPermission.BUILD,
                GuildPermission.OPEN_CONTAINERS,
                GuildPermission.RECRUIT,
                GuildPermission.CLAIM_CHUNKS,
                GuildPermission.KICK,
                GuildPermission.MANAGE_WARP,
                GuildPermission.BANK_AND_UPGRADES,
                GuildPermission.RESERVE
        );

        enable(
                guild,
                GuildRank.MEMBER,
                GuildPermission.BUILD,
                GuildPermission.OPEN_CONTAINERS,
                GuildPermission.RESERVE
        );

        enable(
                guild,
                GuildRank.RECRUIT,
                GuildPermission.BUILD
        );
    }

    /*
     * Configuration stricte.
     *
     * Le chef conserve beaucoup de contrôle.
     */
    private void configureStrictPermissions(Guild guild) {

        enable(
                guild,
                GuildRank.DEPUTY,
                GuildPermission.BUILD,
                GuildPermission.OPEN_CONTAINERS,
                GuildPermission.RECRUIT,
                GuildPermission.KICK,
                GuildPermission.RESERVE
        );

        enable(
                guild,
                GuildRank.MEMBER,
                GuildPermission.BUILD,
                GuildPermission.OPEN_CONTAINERS
        );

        enable(
                guild,
                GuildRank.RECRUIT,
                GuildPermission.BUILD
        );
    }

    /*
     * Configuration permissive.
     *
     * Les membres ont davantage de possibilités.
     */
    private void configurePermissivePermissions(Guild guild) {

        enable(
                guild,
                GuildRank.DEPUTY,
                GuildPermission.BUILD,
                GuildPermission.OPEN_CONTAINERS,
                GuildPermission.RECRUIT,
                GuildPermission.CLAIM_CHUNKS,
                GuildPermission.KICK,
                GuildPermission.MANAGE_WARP,
                GuildPermission.BANK_AND_UPGRADES,
                GuildPermission.RESERVE
        );

        enable(
                guild,
                GuildRank.MEMBER,
                GuildPermission.BUILD,
                GuildPermission.OPEN_CONTAINERS,
                GuildPermission.RECRUIT,
                GuildPermission.CLAIM_CHUNKS,
                GuildPermission.MANAGE_WARP,
                GuildPermission.RESERVE
        );

        enable(
                guild,
                GuildRank.RECRUIT,
                GuildPermission.BUILD,
                GuildPermission.OPEN_CONTAINERS
        );
    }

    private void enable(
            Guild guild,
            GuildRank rank,
            GuildPermission... permissions
    ) {

        for (GuildPermission permission : permissions) {
            guild.setPermission(
                    rank,
                    permission,
                    true
            );
        }
    }

    private void seedJoinRequests() {

        source.findGuildById("aurora").ifPresent(guild -> {
            guild.joinRequests().add(uuid("candidate:Aria"));
            guild.joinRequests().add(uuid("candidate:Leo"));
        });

        source.findGuildById("eclipse").ifPresent(guild -> {
            guild.joinRequests().add(uuid("candidate:Ezra"));
        });

        source.findGuildById("obsidian").ifPresent(guild -> {
            guild.joinRequests().add(uuid("candidate:Kira"));
            guild.joinRequests().add(uuid("candidate:Zane"));
            guild.joinRequests().add(uuid("candidate:Milo"));
        });

        source.findGuildById("solaris").ifPresent(guild -> {
            guild.joinRequests().add(uuid("candidate:Ayla"));
        });

        source.findGuildById("dragonspire").ifPresent(guild -> {
            guild.joinRequests().add(uuid("candidate:Theo"));
            guild.joinRequests().add(uuid("candidate:Lena"));
        });
    }

    private UUID uuid(String value) {
        return UUID.nameUUIDFromBytes(
                value.getBytes(StandardCharsets.UTF_8)
        );
    }
}