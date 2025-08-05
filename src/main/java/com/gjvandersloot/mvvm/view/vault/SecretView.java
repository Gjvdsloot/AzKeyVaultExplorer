package com.gjvandersloot.mvvm.view.vault;

import com.gjvandersloot.utils.DialogUtils;
import com.gjvandersloot.data.Secret;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.mvvm.view.CreateSecretView;
import com.gjvandersloot.mvvm.view.IVaultView;
import com.gjvandersloot.mvvm.viewmodel.vault.SecretViewModel;
import com.gjvandersloot.service.MainStageProvider;
import com.gjvandersloot.utils.FxExtensions;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.gjvandersloot.utils.FxExtensions.copyToClipBoard;
import static javafx.beans.binding.Bindings.selectBoolean;
import static javafx.beans.binding.Bindings.when;

@Component
@Scope("prototype")
public class SecretView implements IVaultView {
    @FXML private ProgressIndicator progressIndicator;

    @FXML private Button copyBannerMessage;
    @FXML private HBox warningBanner;
    @FXML private Label warningMessage;

    @FXML private Button delete;
    @FXML private Button copy;
    @FXML private Button show;
    @FXML private TextField filterField;
    @FXML private TableView<Secret> secretsTable;
    @FXML private TableColumn<Secret, String> secretsColumn;
    @FXML private TableColumn<Secret, String> secretValueColumn;
    @FXML private TableColumn<Secret, Boolean> statusColumn;
    @FXML private TableColumn<Secret, OffsetDateTime> expirationColumn;

    @Autowired private SecretViewModel vm;

    @Autowired private MainStageProvider mainStageProvider;
    private Vault vault;

    @Autowired
    private DialogUtils dialogUtils;

    @FXML
    public void initialize() {
        secretValueColumn.setCellValueFactory(cell -> cell.getValue().displayProperty());

        var selection = secretsTable.getSelectionModel().selectedItemProperty();

        copy.disableProperty().bind(selection.isNull());
        show.disableProperty().bind(selection.isNull());
        delete.disableProperty().bind(selection.isNull());
        var hidden = selectBoolean(selection, "hidden");
        show.textProperty().bind(when(selection.isNull().or(hidden))
                .then("Show").otherwise("Hide"));

// DON'T REMOVE THIS. USE IT WHENEVER DATA DOES NOT LOAD PROPERLY.
//        vm.errorProperty().addListener((obs, o, n) -> {
//            if (!n.isEmpty()) showError(n);
//        });

        setupVaultFilter();
    }

    private void setupVaultFilter() {
        FxExtensions.clearOnEscape(filterField);
        secretsColumn.setCellValueFactory(cell -> cell.getValue().secretNameProperty());
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

        FilteredList<Secret> filteredData = new FilteredList<>(vm.getSecrets(), p -> true);

        SortedList<Secret> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(secretsTable.comparatorProperty());

        secretsTable.setItems(sortedData);

        filterField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = (newVal == null ? "" : newVal.toLowerCase().trim());
            filteredData.setPredicate(item -> {
                if (lower.isEmpty()) {
                    return true;
                }

                return item.getSecretName() != null
                        && item.getSecretName().toLowerCase().contains(lower);
            });
        });

        secretsTable.sceneProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            Platform.runLater(() -> secretsTable.refresh());
        });
    }

    public void showSecret() {
        var secret = secretsTable.getSelectionModel().getSelectedItem();
        if (secret == null)
            return;

        if (secret.getValue() == null)
            CompletableFuture
                    .runAsync(() -> vm.loadSecret(secret))
                    .thenAccept((v) -> Platform.runLater(() -> secret.setHidden(!secret.isHidden())));
        else {
            secret.setHidden(!secret.hiddenProperty().getValue());
        }
    }

    public void copySecret() {
        var secret = secretsTable.getSelectionModel().getSelectedItem();
        if (secret == null)
            return;

        if (secret.getValue() == null)
            CompletableFuture.runAsync(() -> vm.loadSecret(secret))
                    .thenAccept(v -> Platform.runLater(() -> copyToClipBoard(secret.getValue())))
                    .exceptionally(e -> {
                        dialogUtils.showError(e.getMessage());
                        return null;
                    });
        else
            copyToClipBoard(secret.getValue());
    }

    @Autowired private ApplicationContext context;

    public void addSecret(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateSecretView.fxml"));
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();

        CreateSecretView ctr = loader.getController();
        ctr.setVault(vault);

        var stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Create new secret");
        stage.initOwner(mainStageProvider.getPrimaryStage());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();

        if (ctr.getResult() != null)
            vm.addSecret(ctr.getResult());
    }

    public void deleteSecret() {
        var selectedSecret = secretsTable.getSelectionModel().getSelectedItem();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete secret");
        alert.setHeaderText("Are you sure you want to delete secret " + selectedSecret.getSecretName() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() != ButtonType.OK) return;

        CompletableFuture.runAsync(() -> vm.deleteSecret(selectedSecret));
    }

    @Override
    public void init(Vault vault) {
        this.vault = vault;

        warningBanner.setManaged(false);
        warningBanner.setVisible(false);
        progressIndicator.setVisible(true);
        secretsTable.setVisible(false);
        vm.getSecrets().clear();

        CompletableFuture.runAsync(() -> {
            try {
                vm.setSecretClient(vault);
                var secrets = vm.loadSecrets();

                Platform.runLater(() -> vm.getSecrets().setAll(secrets));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    vault.setLoadFailed(true);
                    notifyLoadFailed(e.getMessage());
                });
            } finally {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    secretsTable.setVisible(true);
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

    public void reload() {
        init(vault);
    }
}
