package de.hysky.skyblocker.skyblock;

import java.util.Optional;

public class PetDetails {
    private final String type;
    private final double exp;
    private final String tier;
    private final Optional<String> uuid;
    private final Optional<String> item;
    private final Optional<String> skin;

    public PetDetails(String type, double exp, String tier, Optional<String> uuid, Optional<String> item, Optional<String> skin) {
        this.type = type;
        this.exp = exp;
        this.tier = tier;
        this.uuid = uuid;
        this.item = item;
        this.skin = skin;
    }

    public String getType() {
        return type;
    }

    public double getExp() {
        return exp;
    }

    public String getTier() {
        return tier;
    }

    public Optional<String> getUuid() {
        return uuid;
    }

    public Optional<String> getItem() {
        return item;
    }

    public Optional<String> getSkin() {
        return skin;
    }
}
