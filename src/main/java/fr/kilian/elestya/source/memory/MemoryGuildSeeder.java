package fr.kilian.elestya.source.memory;

import fr.kilian.elestya.api.domain.Guild;
import fr.kilian.elestya.api.domain.GuildMember;
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

        source.addGuild(createGuild(
                "aurora",
                "Aurora",
                "Aelys",
                15_000,
                "Nerion",
                "Kael",
                "Lyria",
                "Thane"
        ));

        source.addGuild(createGuild(
                "valoria",
                "Valoria",
                "Eryndor",
                8_500,
                "Mirael",
                "Soren",
                "Nyra"
        ));

        source.addGuild(createGuild(
                "eclipse",
                "Eclipse",
                "Noctis",
                23_000,
                "Selene",
                "Orion",
                "Veyra",
                "Kian"
        ));

        source.addGuild(createGuild(
                "ember",
                "Ember",
                "Kaelis",
                4_200,
                "Riven",
                "Ashen",
                "Fiora"
        ));

        source.addGuild(createGuild(
                "celestia",
                "Celestia",
                "Astra",
                17_800,
                "Lyanna",
                "Elios",
                "Seraph"
        ));

        source.addGuild(createGuild(
                "ironclad",
                "Ironclad",
                "Draven",
                10_500,
                "Brom",
                "Garrick",
                "Torin"
        ));

        source.addGuild(createGuild(
                "verdant",
                "Verdant",
                "Sylva",
                6_700,
                "Rowan",
                "Ivy",
                "Flora"
        ));

        source.addGuild(createGuild(
                "obsidian",
                "Obsidian",
                "Mordren",
                30_000,
                "Varek",
                "Darius",
                "Nyx"
        ));

        source.addGuild(createGuild(
                "stormborn",
                "Stormborn",
                "Raegar",
                12_400,
                "Tempest",
                "Zephyr",
                "Ardyn"
        ));

        source.addGuild(createGuild(
                "lunaris",
                "Lunaris",
                "Selena",
                9_900,
                "Luna",
                "Cerys",
                "Elara"
        ));

        source.addGuild(createGuild(
                "phoenix",
                "Phoenix",
                "Ignis",
                19_500,
                "Cinder",
                "Blaze",
                "Emberlyn"
        ));

        source.addGuild(createGuild(
                "frostborn",
                "Frostborn",
                "Skadi",
                7_300,
                "Bjorn",
                "Ylva",
                "Freya"
        ));

        source.addGuild(createGuild(
                "arcadia",
                "Arcadia",
                "Cassian",
                11_200,
                "Adriel",
                "Maeve",
                "Elric"
        ));

        source.addGuild(createGuild(
                "ravenfall",
                "Ravenfall",
                "Corvin",
                14_600,
                "Raven",
                "Silas",
                "Morrigan"
        ));

        source.addGuild(createGuild(
                "solaris",
                "Solaris",
                "Helios",
                21_000,
                "Sol",
                "Lucian",
                "Aurelia"
        ));

        source.addGuild(createGuild(
                "wildhunt",
                "Wild Hunt",
                "Fenrir",
                5_800,
                "Hati",
                "Skoll",
                "Ulf"
        ));

        source.addGuild(createGuild(
                "evernight",
                "Evernight",
                "Vesper",
                16_250,
                "Shade",
                "Umbra",
                "Nox"
        ));

        source.addGuild(createGuild(
                "silverwing",
                "Silverwing",
                "Alaric",
                13_750,
                "Aerion",
                "Gale",
                "Ciel"
        ));

        source.addGuild(createGuild(
                "dragonspire",
                "Dragonspire",
                "Vaelor",
                27_000,
                "Rhaen",
                "Drake",
                "Syrax"
        ));

        source.addGuild(createGuild(
                "horizon",
                "Horizon",
                "Atlas",
                18_400,
                "Nova",
                "Aster",
                "Skye"
        ));

        seedJoinRequests();
    }

    private Guild createGuild(
            String id,
            String name,
            String ownerName,
            double balance,
            String officerName,
            String... memberNames
    ) {

        UUID ownerId = uuid(name + ":" + ownerName);

        List<GuildMember> members = new ArrayList<>();

        members.add(new GuildMember(
                ownerId,
                ownerName,
                GuildRank.OWNER
        ));

        if (officerName != null) {
            members.add(new GuildMember(
                    uuid(name + ":" + officerName),
                    officerName,
                    GuildRank.OFFICER
            ));
        }

        for (String memberName : memberNames) {
            members.add(new GuildMember(
                    uuid(name + ":" + memberName),
                    memberName,
                    GuildRank.MEMBER
            ));
        }

        return new Guild(
                id,
                name,
                ownerId,
                members,
                balance,
                new ArrayList<>()
        );
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