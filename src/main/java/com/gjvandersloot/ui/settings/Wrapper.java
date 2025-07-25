package com.gjvandersloot.ui.settings;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Wrapper<T> {
    public Wrapper(T value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    private String displayName;

    private T value;

    @Override
    public String toString() {
        return displayName;
    }
}
