package com.gjvandersloot.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
}
