package com.gjvandersloot.mvvm.view;

import com.gjvandersloot.controller.ErrorDialogController;
import com.gjvandersloot.mvvm.viewmodel.WizardViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class WizardView {
    public TextField vaultUrlField;
    @Autowired
    WizardViewModel viewModel;

    @FXML private RadioButton secretRadio;
    @FXML private RadioButton certRadio;
    @FXML private VBox secretPane;
    @FXML private VBox certPane;
    @FXML private Button browseButton;
    @FXML private TextField certPathField;
    @FXML private TextField clientIdField;
    @FXML private TextField tenantIdField;
    @FXML private Button attachButton;
    @FXML private PasswordField secretField;

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

        clientIdField.textProperty().bindBidirectional(viewModel.clientIdProperty());
        tenantIdField.textProperty().bindBidirectional(viewModel.tenantIdProperty());
        vaultUrlField.textProperty().bindBidirectional(viewModel.vaultUrlProperty());
        secretField.textProperty().bindBidirectional(viewModel.secretProperty());

        // 2) build a BooleanBinding that's true when *all* required fields are non‑empty:
        BooleanBinding basicsFilled =
                clientIdField.textProperty().isNotEmpty()
                        .and(tenantIdField.textProperty().isNotEmpty())
                        .and(vaultUrlField.textProperty().isNotEmpty());

        // when Secret is selected, require secretField:
        BooleanBinding secretOk =
                secretRadio.selectedProperty()
                        .and(secretField.textProperty().isNotEmpty());

        // when Cert is selected, require certPathField:
        BooleanBinding certOk =
                certRadio.selectedProperty()
                        .and(certPathField.textProperty().isNotEmpty());

        // 3) final binding: basics AND (secretOk OR certOk)
        BooleanBinding allValid =
                basicsFilled.and(secretOk.or(certOk));

        // 4) bind the button’s disabled‑property to the inverse of that:
        attachButton.disableProperty().bind(allValid.not());

        viewModel.successProperty().addListener((obs, o, n) -> {
            if (n) ((Stage) attachButton.getScene().getWindow()).close();
        });

        viewModel.errorProperty().addListener((obs, o, n) -> {
            if (!n.isEmpty()) showError(n);
        });
    }

    public void onBrowseCertificate() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select certificate file");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PKCS#12 / PFX files", "*.pfx", "*.p12"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );

        // get owner window from the button:
        Window owner = browseButton.getScene().getWindow();
        File file = chooser.showOpenDialog(owner);
        if (file != null) {
            String path = file.getAbsolutePath();
            certPathField.setText(path);

            // if you want to push it into your VM:
            viewModel.setCertificatePath(path);
        }
    }

    private void showError(String e) {
        Platform.runLater(() -> {
            var loader = new FXMLLoader(getClass().getResource("/ErrorDialog.fxml"));

            Parent root;
            try {
                root = loader.load();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            ErrorDialogController errorCtr = loader.getController();

            var dialog = new Stage(StageStyle.DECORATED);
            dialog.initOwner(attachButton.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));

            errorCtr.setDialogStage(dialog);
            errorCtr.setMessage(e);
            dialog.showAndWait();
        });
    }

    public void onAttach() {
        viewModel.createAttachedVault();
    }
}
