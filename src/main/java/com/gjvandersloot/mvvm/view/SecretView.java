package com.gjvandersloot.mvvm.view;

import com.gjvandersloot.controller.ErrorDialogController;
import com.gjvandersloot.data.Secret;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.mvvm.viewmodel.SecretViewModel;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.gjvandersloot.controller.MainController.copyToClipBoard;
import static javafx.beans.binding.Bindings.selectBoolean;
import static javafx.beans.binding.Bindings.when;

@Component
@Scope("prototype")
public class SecretView implements Initializable {
    @FXML private Button copy;
    @FXML private Button show;
    @FXML private TextField filterField;
    @FXML private TableView<Secret> secretsTable;
    @FXML private TableColumn<Secret, String> secretsColumn;
    @FXML private TableColumn<Secret, String> secretValueColumn;

    @Autowired
    private SecretViewModel vm;

    @FXML
    public void initialize() {
        secretValueColumn.setCellValueFactory(cell -> cell.getValue().displayProperty());

        var selection = secretsTable.getSelectionModel().selectedItemProperty();

        copy.disableProperty().bind(selection.isNull());
        show.disableProperty().bind(selection.isNull());
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
        secretsColumn.setCellValueFactory(cell -> cell.getValue().secretNameProperty());

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
                        showError(e.getMessage());
                        return null;
                    });
        else
            copyToClipBoard(secret.getValue());
    }

    public void addSecret(ActionEvent actionEvent) {
        CompletableFuture.runAsync(() -> {
            var s = new Secret();
            s.setSecretName("Hello");
            Platform.runLater(() -> vm.getSecrets().add(s));

        });
    }

    public void delete(ActionEvent actionEvent) {
    }

    @Override
    public void init(Vault vault) {
        CompletableFuture.runAsync(() -> {
            try {
                vm.setSecretClient(vault);
                var secrets = vm.loadSecrets(); // sync or async, either is fine

                Platform.runLater(() -> {
                    vm.getSecrets().setAll(secrets);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    vault.setLoadFailed(true);
                    showError(e.getMessage());
                });
            }
        });
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
            dialog.initOwner(secretsTable.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));

            errorCtr.setDialogStage(dialog);
            errorCtr.setMessage(e);
            dialog.showAndWait();
        });
    }
}
