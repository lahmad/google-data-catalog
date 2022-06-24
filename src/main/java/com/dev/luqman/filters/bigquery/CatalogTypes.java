package com.dev.luqman.filters.bigquery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.codec.binary.StringUtils;
@AllArgsConstructor
public enum CatalogTypes {
    TABLE("table"),
    DATASET("dataset"),
    UNKNOWN("unknown");

    public static CatalogTypes getType(final String type) {
        for (CatalogTypes enumType : CatalogTypes.values()) {
            if (StringUtils.equals(enumType.getType(), type)) {
                return enumType;
            }
        }
        return UNKNOWN;
    }
    private @Getter String type;
}
