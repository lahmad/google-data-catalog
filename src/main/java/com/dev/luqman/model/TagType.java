package com.dev.luqman.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TagType {
    STRING("string"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    ENUMERATED("enumerated"),
    DATETIME("datetime"),
    RICHTEXT("richtext");

    private String type;
}
