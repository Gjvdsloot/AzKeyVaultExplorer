package com.gjvandersloot.mvvm.view.vault;

import com.gjvandersloot.utils.DialogUtils;
import com.gjvandersloot.data.Certificate;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.mvvm.view.Initializable;
import com.gjvandersloot.mvvm.viewmodel.vault.CertificateViewModel;
import com.gjvandersloot.utils.FxExtensions;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Component
@Scope("prototype")
public class CertificateView implements Initializable {
    public Button downloadBtn;
    @FXML private TableView<Certificate> certsTable;
    @FXML private TableColumn<Certificate, String> nameColumn;
    @FXML private TableColumn<Certificate, String> thumbPrintColumn;
    @FXML private TableColumn<Certificate, Boolean> statusColumn;
    @FXML private TableColumn<Certificate, OffsetDateTime> expirationColumn;

    @FXML private Button delete;
    @FXML private Button copy;
    @FXML private Button show;
    @FXML private TextField filterField;

    @Autowired private CertificateViewModel vm;

    @Autowired
    private DialogUtils dialogUtils;

    @FXML
    public void initialize() {

        var selection = certsTable.getSelectionModel().selectedItemProperty();

        downloadBtn.disableProperty().bind(selection.isNull());

        setupVaultFilter();
    }

    private void setupVaultFilter() {
        FxExtensions.clearOnEscape(filterField);
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        thumbPrintColumn.setCellValueFactory(cell -> cell.getValue().thumbPrintProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().enabledProperty());
        expirationColumn.setCellValueFactory(cellData -> cellData.getValue().expirationDateProperty());

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "enabled" : "disabled");
                }
            }
        });
        expirationColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            @Override
            protected void updateItem(OffsetDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        FilteredList<Certificate> filteredData = new FilteredList<>(vm.getCertificates(), p -> true);

        SortedList<Certificate> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(certsTable.comparatorProperty());

        certsTable.setItems(sortedData);

        filterField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = (newVal == null ? "" : newVal.toLowerCase().trim());
            filteredData.setPredicate(item -> {
                if (lower.isEmpty()) {
                    return true;
                }

                return item.getName() != null
                        && item.getName().toLowerCase().contains(lower);
            });
        });

        certsTable.sceneProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            Platform.runLater(() -> certsTable.refresh());
        });
    }

    @Override
    public void init(Vault vault) {

        CompletableFuture.runAsync(() -> {
            try {
                vm.setCertificateClient(vault);
                vm.setSecretClient(vault);
                var certificates = vm.loadCertificates();

                Platform.runLater(() -> vm.getCertificates().setAll(certificates));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    vault.setLoadFailed(true);
                    dialogUtils.showError(e.getMessage());
                });
            }
        });
    }

    public void download() {
        var cert = certsTable.getSelectionModel().getSelectedItem();
        if (cert == null)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Certificate");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PFX Files", "*.pfx"),
                new FileChooser.ExtensionFilter("CER Files", "*.cer")
        );

        File file = fileChooser.showSaveDialog(downloadBtn.getScene().getWindow());

        if (file != null) {
            boolean isPfx = file.getName().toLowerCase().endsWith(".pfx");

            vm.downloadCertificateAsync(cert.getName(), isPfx).thenAccept(bytes -> {
                try {
                    Files.write(file.toPath(), bytes);
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Certificate downloaded to:\n" + file.getAbsolutePath());
                        alert.setHeaderText(null);
                        alert.setTitle("Download complete");
                        alert.show();
                    });
                } catch (IOException e) {
                    dialogUtils.showError(e.getMessage());
                }
            }).exceptionally(e -> {
                dialogUtils.showError(e.getMessage());
                return null;
            });
        }
    }
}