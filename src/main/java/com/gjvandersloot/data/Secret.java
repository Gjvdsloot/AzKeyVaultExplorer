package com.gjvandersloot.data;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableStringValue;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Secret {
    public Secret() {
        hidden.set(true);
    }

    private final StringProperty secretName = new SimpleStringProperty(this, "secretName");
    public StringProperty secretNameProperty() { return secretName; }
    public String getSecretName() { return secretName.get(); }
    public void setSecretName(String secretName) { this.secretName.set(secretName); }


    private String vaultUrl;
    private String accountName = null;

    private final StringProperty value = new SimpleStringProperty(this, "value");
    public String getValue() {
        return value.get();
    }
    public StringProperty valueProperty() { return value; }

    private final BooleanProperty hidden = new SimpleBooleanProperty(this, "hidden");
    public boolean isHidden() {
        return hidden.get();
    }
    public void setHidden(boolean hidden) { this.hidden.set(hidden); }
    public BooleanProperty hiddenProperty() { return hidden; }

    // Computed property using a binding
    private final StringBinding display = new StringBinding() {
        {
            bind(value, hidden);
        }

        @Override
        protected String computeValue() {
            String v = value.get();
            if (v == null) return null;
            return hidden.get() ? "*".repeat(v.length()) : v;
        }
    };

    public String getDisplay() {
        return display.get();
    }

    public ObservableStringValue displayProperty() {
        return display;
    }
}
