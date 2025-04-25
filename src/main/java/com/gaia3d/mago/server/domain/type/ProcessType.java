package com.gaia3d.mago.server.domain.type;

import lombok.Getter;

@Getter
public enum ProcessType {
    NOT_FOUND,
    READY,
    UPLOADING,
    RUNNING,
    SUCCESS,
    FAIL;

    public static ProcessType valueOfSafetyNull(String value) {
        for (ProcessType status : ProcessType.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return NOT_FOUND;
    }
}
