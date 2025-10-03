package com.gjvandersloot.view;

import com.gjvandersloot.utils.DialogUtils;
import com.gjvandersloot.viewmodel.WizardViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Scope("prototype")
public class WizardView {
    @Autowired WizardViewModel viewModel;
    @Autowired DialogUtils dialogUtils;

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
    @FXML private TextField vaultUriField;
    @FXML private PasswordField certPasswordField;

    @FXML
    public void initialize() {
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
        vaultUriField.textProperty().bindBidirectional(viewModel.vaultUriProperty());
        secretField.textProperty().bindBidirectional(viewModel.secretProperty());
        certPathField.textProperty().bindBidirectional(viewModel.certificatePathProperty());
        certPasswordField.textProperty().bindBidirectional(viewModel.certPasswordProperty());

        certPasswordField.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> {
                            String path = certPathField.textProperty().get();
                            return path != null && path.toLowerCase().endsWith(".pem");
                        },
                        viewModel.certificatePathProperty()
                )
        );

        BooleanBinding basicsFilled =
                clientIdField.textProperty().isNotEmpty()
                        .and(tenantIdField.textProperty().isNotEmpty())
                        .and(vaultUriField.textProperty().isNotEmpty());

        BooleanBinding secretOk =
                secretRadio.selectedProperty()
                        .and(secretField.textProperty().isNotEmpty());

        BooleanBinding certOk =
                certRadio.selectedProperty()
                        .and(certPathField.textProperty().isNotEmpty());

        BooleanBinding allValid =
                basicsFilled.and(secretOk.or(certOk));

        attachButton.disableProperty().bind(allValid.not());
    }

    public void onBrowseCertificate() {
        File initialDir = new File(System.getProperty("user.home"));
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(initialDir);
        chooser.setTitle("Select certificate file");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PKCS#12 / PFX files", "*.pfx", "*.p12"),
                new FileChooser.ExtensionFilter("PEM files", "*.pem"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );

        // get owner window from the button:
        Window owner = browseButton.getScene().getWindow();
        File file = chooser.showOpenDialog(owner);
        if (file != null) {
            String path = file.getAbsolutePath();
            certPathField.setText(path);
        }
    }

    public void onAttach() {
        if (viewModel.createVault())
            Platform.runLater(() -> ((Stage) attachButton.getScene().getWindow()).close());
        else
            Platform.runLater(() -> dialogUtils.showError(viewModel.errorProperty().getValue()));
    }
}
