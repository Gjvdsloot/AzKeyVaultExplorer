package com.gjvandersloot.mvvm.view.vault;

import com.gjvandersloot.DialogUtils;
import com.gjvandersloot.data.Certificate;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.mvvm.view.Initializable;
import com.gjvandersloot.mvvm.viewmodel.vault.CertificateViewModel;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static javafx.beans.binding.Bindings.selectBoolean;
import static javafx.beans.binding.Bindings.when;

@Component
@Scope("prototype")
public class CertificateView implements Initializable {
    @FXML private TableView<Certificate> certsTable;
    @FXML private TableColumn<Certificate, String> nameColumn;
    @FXML private TableColumn<Certificate, String> thumbPrintColumn;
    @FXML private TableColumn<Certificate, String> statusColumn;
    @FXML private TableColumn<Certificate, String> expirationColumn;

    @FXML private Button delete;
    @FXML private Button copy;
    @FXML private Button show;
    @FXML private TextField filterField;

    @Autowired private CertificateViewModel vm;

    @Autowired private ApplicationContext context;

    private Vault vault;

    @Autowired
    private DialogUtils dialogUtils;

    @FXML
    public void initialize() {

        var selection = certsTable.getSelectionModel().selectedItemProperty();

//        copy.disableProperty().bind(selection.isNull());
//        show.disableProperty().bind(selection.isNull());
//        delete.disableProperty().bind(selection.isNull());
//        var hidden = selectBoolean(selection, "hidden");
//        show.textProperty().bind(when(selection.isNull().or(hidden))
//                .then("Show").otherwise("Hide"));

        setupVaultFilter();
    }

    private void setupVaultFilter() {
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());

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

//    public void showCertificate() {
//        var secret = certsTable.getSelectionModel().getSelectedItem();
//        if (secret == null)
//            return;
//
//        if (secret.() == null)
//            CompletableFuture
//                    .runAsync(() -> vm.loadCertificate(secret))
//                    .thenAccept((v) -> Platform.runLater(() -> secret.setHidden(!secret.isHidden())));
//        else {
//            secret.setHidden(!secret.hiddenProperty().getValue());
//        }
//    }

//    public void copyCertificate() {
//        var secret = certsTable.getSelectionModel().getSelectedItem();
//        if (secret == null)
//            return;
//
//        if (secret.getValue() == null)
//            CompletableFuture.runAsync(() -> vm.loadCertificate(secret))
//                    .thenAccept(v -> Platform.runLater(() -> copyToClipBoard(secret.getValue())))
//                    .exceptionally(e -> {
//                        showError(e.getMessage());
//                        return null;
//                    });
//        else
//            copyToClipBoard(secret.getValue());
//    }

//    public void addCertificate(ActionEvent actionEvent) throws IOException {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateCertificateView.fxml"));
//        loader.setControllerFactory(context::getBean);
//        Parent root = loader.load();
//
//        CreateCertificateView ctr = loader.getController();
//        ctr.setVault(vault);
//
//        var stage = new Stage(StageStyle.DECORATED);
//        stage.setTitle("Create new secret");
//        stage.initOwner(mainStageProvider.getPrimaryStage());
//        stage.initModality(Modality.APPLICATION_MODAL);
//        stage.setScene(new Scene(root));
//        stage.showAndWait();
//
//        if (ctr.getResult() != null)
//            vm.addCertificate(ctr.getResult());
//    }

//    public void deleteCertificate() {
//        var selectedCertificate = certsTable.getSelectionModel().getSelectedItem();
//
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Delete secret");
//        alert.setHeaderText("Are you sure you want to delete secret " + selectedCertificate.getCertificateName() + "?");
//        alert.setContentText("This action cannot be undone.");
//
//        Optional<ButtonType> result = alert.showAndWait();
//
//        if (result.isPresent() && result.get() != ButtonType.OK) return;
//
//        CompletableFuture.runAsync(() -> vm.deleteCertificate(selectedCertificate));
//    }

    @Override
    public void init(Vault vault) {
        this.vault = vault;

        CompletableFuture.runAsync(() -> {
            try {
                vm.setCertificateClient(vault);
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
}