package com.gjvandersloot.data;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

public class Subscription {
    @JsonIgnore
    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    public BooleanProperty visibleProperty() { return visible; }

    @JsonSetter("visible")
    public void setVisible(boolean visible) { this.visible.set(visible); }

    @JsonGetter("visible")
    public boolean isVisible() { return visible.get(); }

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String id;

    @Getter @Setter
    private String accountName;

    @Override
    public String toString() {
        return name;
    }

    @Getter @Setter
    @JsonIgnore
    private ArrayList<Vault> vaults;
}
