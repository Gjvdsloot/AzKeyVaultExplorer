package com.gjvandersloot.data;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableStringValue;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class Secret {
    public Secret() {
        hidden.set(true);
    }

    private final StringProperty secretName = new SimpleStringProperty();
    public StringProperty secretNameProperty() { return secretName; }
    public String getSecretName() { return secretName.get(); }
    public void setSecretName(String secretName) { this.secretName.set(secretName); }


    private String vaultUri;
    private String accountName = null;

    private final StringProperty value = new SimpleStringProperty();
    public String getValue() {
        return value.get();
    }
    public void setValue(String val) { value.set(val); }
    public StringProperty valueProperty() { return value; }

    private final BooleanProperty hidden = new SimpleBooleanProperty();
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

    public void setEnabled(Boolean enabled) {
        this.enabled.set(enabled);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    private final BooleanProperty enabled = new SimpleBooleanProperty(false);

    public void setExpirationDate(OffsetDateTime expiresOn) {
        this.expirationDate.set(expiresOn);
    }

    public OffsetDateTime getExpirationDate() {
        return expirationDate.get();
    }

    public ObjectProperty<OffsetDateTime> expirationDateProperty() {
        return expirationDate;
    }

    private final ObjectProperty<OffsetDateTime> expirationDate = new SimpleObjectProperty<>();
}
