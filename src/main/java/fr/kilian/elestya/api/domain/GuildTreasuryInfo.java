package fr.kilian.elestya.api.domain;

public record GuildTreasuryInfo(
        int reserveRows,
        int maxReserveRows,
        double reserveUpgradePrice,
        int chestLocksPerMember,
        int maxChestLocksPerMember,
        double chestLockUpgradePrice
) {

    public int reserveSlots() {
        return reserveRows * 9;
    }

    public boolean reserveMaxed() {
        return reserveRows >= maxReserveRows;
    }

    public boolean chestLocksMaxed() {
        return chestLocksPerMember >= maxChestLocksPerMember;
    }
}