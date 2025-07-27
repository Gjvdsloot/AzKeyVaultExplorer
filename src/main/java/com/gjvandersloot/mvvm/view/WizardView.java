package com.gjvandersloot.mvvm.view;

import com.gjvandersloot.mvvm.viewmodel.WizardViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WizardView {
    @Autowired
    WizardViewModel viewModel;

    @FXML private RadioButton secretRadio;
    @FXML private RadioButton certRadio;
    @FXML private VBox secretPane;
    @FXML private VBox certPane;

    @FXML
    public void initialize() {
        // Bind selected toggle to viewModel string property
        viewModel.selectedAuthMethodProperty().bind(
                Bindings.createStringBinding(() -> {
                    if (secretRadio.isSelected()) return "Secret";
                    else return "Certificate";
                }, secretRadio.selectedProperty(), certRadio.selectedProperty())
        );

        // Bind visibility
        secretPane.visibleProperty().bind(viewModel.selectedAuthMethodProperty().isEqualTo("Secret"));
        secretPane.managedProperty().bind(secretPane.visibleProperty());

        certPane.visibleProperty().bind(viewModel.selectedAuthMethodProperty().isEqualTo("Certificate"));
        certPane.managedProperty().bind(certPane.visibleProperty());
    }
}
