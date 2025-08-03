package com.gjvandersloot.data;

import javafx.beans.property.*;

import java.time.OffsetDateTime;

public class Certificate {
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    private final StringProperty name = new SimpleStringProperty();

    public String getThumbPrint() {
        return thumbPrint.get();
    }

    public void setThumbPrint(String thumbPrint) {
        this.thumbPrint.set(thumbPrint);
    }

    public StringProperty thumbPrintProperty() {
        return thumbPrint;
    }

    private final StringProperty thumbPrint = new SimpleStringProperty();

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
