package com.gaia3d.mago.server.domain.type;

import lombok.Getter;

@Getter
public enum AssetType {
    NOT_FOUND,
    READY,
    UPLOADING,
    RUNNING,
    SUCCESS,
    FAIL;

    public static AssetType valueOfSafetyNull(String value) {
        for (AssetType status : AssetType.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return NOT_FOUND;
    }
}
