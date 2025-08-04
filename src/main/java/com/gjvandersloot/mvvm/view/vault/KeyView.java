package com.gjvandersloot.mvvm.view.vault;

import com.gjvandersloot.data.KeySecret;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.mvvm.view.IVaultView;
import com.gjvandersloot.mvvm.viewmodel.vault.KeyViewModel;
import com.gjvandersloot.utils.DialogUtils;
import com.gjvandersloot.utils.FxExtensions;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import static com.gjvandersloot.utils.FxExtensions.copyToClipBoard;

@Component
@Scope("prototype")
public class KeyView implements IVaultView {
    @FXML private Button copyBannerMessage;
    @FXML private HBox warningBanner;
    @FXML private Label warningMessage;

    @FXML private Button delete;
    @FXML private Button copy;
    @FXML private Button show;
    @FXML private TextField filterField;
    @FXML private TableView<KeySecret> keysTable;
    @FXML private TableColumn<KeySecret, String> nameColumn;
    @FXML private TableColumn<KeySecret, Boolean> statusColumn;
    @FXML private TableColumn<KeySecret, OffsetDateTime> expirationColumn;

    @Autowired KeyViewModel vm;

    private Vault vault;

    @Autowired private DialogUtils dialogUtils;

    public void download() {
    }

    @FXML
    public void initialize() {

        var selection = keysTable.getSelectionModel().selectedItemProperty();

//        downloadBtn.disableProperty().bind(selection.isNull());

        setupVaultFilter();
    }

    private void setupVaultFilter() {
        FxExtensions.clearOnEscape(filterField);
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
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

        FilteredList<KeySecret> filteredData = new FilteredList<>(vm.getKeys(), p -> true);

        SortedList<KeySecret> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(keysTable.comparatorProperty());

        keysTable.setItems(sortedData);

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

        keysTable.sceneProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            Platform.runLater(() -> keysTable.refresh());
        });
    }

    @Override
    public void init(Vault vault) {
        this.vault = vault;
        warningBanner.setManaged(false);
        warningBanner.setVisible(false);

        CompletableFuture.runAsync(() -> {
            try {
                vm.setKeyClient(vault);
                var keys = vm.loadKeys();

                Platform.runLater(() -> vm.getKeys().setAll(keys));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    vault.setLoadFailed(true);
                    notifyLoadFailed(e.getMessage());
                });
            }
        });
    }

    private void notifyLoadFailed(String message) {
        warningMessage.setText(message);
        warningBanner.setManaged(true);
        warningBanner.setVisible(true);
        copyBannerMessage.setOnAction(event -> copyToClipBoard(message));
    }

    public void copyPublicKey() {
        var key = keysTable.getSelectionModel().getSelectedItem();
        if (key == null)
            return;

        vm.downloadKeyAsync(key.getName())
                .thenAccept((publicKey -> Platform.runLater(() -> copyToClipBoard(publicKey))))
                .exceptionally(e -> {
                    dialogUtils.showError(e.getMessage());
                    return null;
                });
    }

    public void downloadPublicKey() {
        var key = keysTable.getSelectionModel().getSelectedItem();
        if (key == null)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save public key");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PEM Files", "*.pem")
        );

        File file = fileChooser.showSaveDialog(keysTable.getScene().getWindow());

        if (file != null) {
            vm.downloadKeyAsync(key.getName()).thenAccept(publicKey -> {
                try {
                    Files.writeString(file.toPath(), publicKey);
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Public key downloaded to:\n" + file.getAbsolutePath());
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
