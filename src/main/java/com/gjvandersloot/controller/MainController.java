package com.gjvandersloot.controller;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.gjvandersloot.AppDataService;
import com.gjvandersloot.data.*;
import com.gjvandersloot.service.AccountService;
import com.gjvandersloot.service.MainStageProvider;
import com.gjvandersloot.service.SecretClientService;
import com.gjvandersloot.ui.SecretItem;
import com.gjvandersloot.ui.SubscriptionItem;
import com.gjvandersloot.ui.VaultItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static javafx.beans.binding.Bindings.selectBoolean;
import static javafx.beans.binding.Bindings.when;

@Component
public class MainController {

    @FXML
    public TreeView<Object> treeView;

    @FXML
    public TableView<SecretItem> secretsTable;

    @FXML
    public TableColumn<SecretItem, String> secretsColumn;
    @FXML
    public TableColumn<SecretItem, String> secretValueColumn;

    @FXML
    public Button show;

    @FXML
    public TextField filterField;

    @FXML
    public TextField treeFilter;

    @Autowired
    AccountService accountService;

    @Autowired
    Store store;

    @Autowired
    SecretClientService secretClientService;

    @Autowired
    MainStageProvider mainStageProvider;

    @Autowired
    AppDataService appDataService;
    private TreeItem<Object> root;

    private final ObservableList<SecretItem> secrets = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.root = new TreeItem<>();
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        loadTreeListeners();
        loadTree();

        setupVaultFilter();
        setupTreeFilter();

        secretValueColumn.setCellValueFactory(cell -> cell.getValue().displayProperty());

        var selection = secretsTable.getSelectionModel().selectedItemProperty();

        show.disableProperty().bind(selection.isNull());
        var hidden = selectBoolean(selection, "hidden");
        show.textProperty().bind(when(selection.isNull().or(hidden))
                .then("Show").otherwise("Hide"));
    }

    private void loadTreeListeners() {
        store.getAccounts().addListener((MapChangeListener<String, Account>) change -> {
            if (change.wasAdded() && !change.wasRemoved()) {
                var account = change.getValueAdded();
                var subs = change.getValueAdded().getTenants().values().stream().flatMap(t ->
                        t.getSubscriptions().values().stream()).collect(Collectors.toSet());

                subs.forEach(s -> addSubscriptionItem(account, s, root));
                return;
            }
            if (!change.wasAdded() && change.wasRemoved()) {
                var subs = change.getValueRemoved().getTenants().values().stream().flatMap(t ->
                        t.getSubscriptions().keySet().stream()).collect(Collectors.toSet());

                var subItems = root.getChildren().stream()
                        .filter(t -> {
                            if (!(t.getValue() instanceof SubscriptionItem s))
                                return false;

                            return subs.contains(s.getId());
                        }).toList();

                root.getChildren().removeAll(subItems);
            }
        });
    }

    private void setupTreeFilter() {
        treeView.setRoot(this.root);

        treeFilter.textProperty().addListener((obs, old, nw) -> {
            if (nw == null || nw.isBlank()) {
                treeView.setRoot(this.root);
            } else {
                treeView.setRoot(filterTree(root, nw.toLowerCase().trim()));
            }
        });
    }

    private TreeItem<Object> filterTree(TreeItem<Object> root, String filter) {
        if (root.getValue() instanceof String)
            return null;

        String text = Optional.ofNullable(root.getValue())
                .map(Object::toString)
                .orElse("")
                .toLowerCase();

        var copy = new TreeItem<>(root.getValue());
        copy.setExpanded(true);

        for (TreeItem<Object> child : root.getChildren()) {
            TreeItem<Object> filteredChild = filterTree(child, filter);
            if (filteredChild != null) {
                copy.getChildren().add(filteredChild);
                filteredChild.setExpanded(true);
            }
        }

        // Include this node if (a) it matches the filter itself, or (b) any child matched:
        boolean matchesSelf = text.contains(filter.toLowerCase());
        return (matchesSelf || !copy.getChildren().isEmpty())
                ? copy
                : null;
    }

    private void setupVaultFilter() {
        secretsColumn.setCellValueFactory(cell -> cell.getValue().secretNameProperty());

        FilteredList<SecretItem> filteredData = new FilteredList<>(secrets, p -> true);

        SortedList<SecretItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(secretsTable.comparatorProperty());

        secretsTable.setItems(sortedData);

        filterField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = (newVal == null ? "" : newVal.toLowerCase().trim());
            filteredData.setPredicate(item -> {
                // show all if filter is empty
                if (lower.isEmpty()) {
                    return true;
                }
                // otherwise compare against whatever fields you like:
                boolean matchesName = item.getSecretName() != null
                        && item.getSecretName().toLowerCase().contains(lower);

                return matchesName;
            });
        });
    }

    public void addSubscription() throws IOException {

        var dialog = createCancelDialog();

        var future = CompletableFuture.runAsync(() -> {
                    Account account;
                    try {
                        account = accountService.addAccount();
                        store.getAccounts().put(account.getUsername(), account);
                        appDataService.saveStore();
                    } catch (Exception e) {
                        showError(e.getMessage());
                    }
                })
                .whenComplete((r, e) -> Platform.runLater(dialog::close));

//        cancelController.setOnCancel(() -> {
//            future.cancel(true);
//            Platform.runLater(dialog::close);
//        });

        dialog.showAndWait();
    }

    private Stage createCancelDialog() {
        var loader = new FXMLLoader(getClass().getResource("/CancelDialog.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CancelDialogController cancelController = loader.getController();
        var dialog = new Stage(StageStyle.UNDECORATED);
        dialog.initOwner(mainStageProvider.getPrimaryStage());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(root));
        cancelController.setOnCancel(dialog::close);
        cancelController.setDialogStage(dialog);
        return dialog;
    }

    public void loadTree() {
        var root = this.root;
        root.getChildren().clear();

        var accounts = store.getAccounts();

        for (var account : accounts.values()) {
            for (var tenant : account.getTenants().values()) {
                for (var subscription : tenant.getSubscriptions().values()) {
                    addSubscriptionItem(account, subscription, root);
                }
            }
        }
    }

    // Maybe remove root as it's equal to this.root. Might change later though when attached resources are added.
    private void addSubscriptionItem(Account account, Subscription subscription, TreeItem<Object> root) {
        var subscriptionItem = new SubscriptionItem();
        subscriptionItem.setId(subscription.getId());
        subscriptionItem.setName(subscription.getName());
        subscriptionItem.setAccountName(account.getUsername());

        var treeItem = new TreeItem<>();
        treeItem.setValue(subscriptionItem);

        var loadingItem = new TreeItem<>();
        loadingItem.setValue("Loading");

        treeItem.getChildren().add(loadingItem);

        if (subscription.isVisible())
            root.getChildren().add(treeItem);

        treeItem.expandedProperty().addListener((obs, o, n) -> {
            if (!n) return;

            if (subscriptionItem.getVaults() == null) {
                loadVaults(treeItem);
            }
        });

        subscription.visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (isNowVisible) {
                root.getChildren().add(treeItem);
            } else {
                root.getChildren().remove(treeItem);
            }
        });
    }

    private void loadVaults(TreeItem<Object> treeItem) {
        var subscriptionItem = (SubscriptionItem) treeItem.getValue();

        CompletableFuture.<ArrayList<Vault>>supplyAsync(() -> {
                    try {
                        AtomicReference<Stage> dlg = new AtomicReference<>();

                        return accountService.addKeyVaults(
                                subscriptionItem.getId(),
                                subscriptionItem.getAccountName(),
                                () -> Platform.runLater(() -> {
                                    dlg.set(createCancelDialog());
                                    dlg.get().showAndWait();
                                }),
                                () -> Platform.runLater(() -> dlg.get().close())
                        );
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
        }).thenAccept(vaults -> Platform.runLater(() -> {
            treeItem.getChildren().clear();

            var vaultItems = new ArrayList<VaultItem>();
            for (var vault : vaults) {
                var vaultItem = new VaultItem();
                vaultItem.setVaultUri(vault.getVaultUri());
                vaultItem.setName(vault.getName());
                vaultItem.setAccountName(subscriptionItem.getAccountName());

                var child = new TreeItem<>();
                child.setValue(vaultItem);


                treeItem.getChildren().add(child);
                vaultItems.add(vaultItem);
            }

            subscriptionItem.setVaults(vaultItems);
        })).exceptionally(e -> {
            Platform.runLater(() -> treeItem.getChildren().clear());
            showError(e.getMessage());
            return null;
        });
    }

    private CompletableFuture<List<SecretProperties>> listPropertySecretsFuture = null;
    public void treeViewClicked() {
        TreeItem<Object> clickedItem = treeView.getSelectionModel().getSelectedItem();
        if (clickedItem == null) return;

        var obj = clickedItem.getValue();

        if (!(obj instanceof VaultItem vaultItem)) return;

        var url = vaultItem.getVaultUri();
        var accountName = vaultItem.getAccountName();

        if (listPropertySecretsFuture != null && !listPropertySecretsFuture.isDone() && !listPropertySecretsFuture.isCancelled())
            listPropertySecretsFuture.cancel(true);

        CompletableFuture<List<SecretProperties>> fetchFuture = CompletableFuture.supplyAsync(() -> {
            var secretClient = secretClientService.getOrCreateClient(url, accountName);

            try {
                return secretClient.listPropertiesOfSecrets()
                        .stream()
                        .toList();
            } catch(Exception e) {
                showError(e.getMessage());
                throw e;
            }
        });

        listPropertySecretsFuture = fetchFuture;

        fetchFuture.thenAccept(secretProperties -> Platform.runLater(() -> {
            if (fetchFuture.isCancelled())
                return;

            var secretItems = secretProperties.stream().map(s -> {
                var secretItem = new SecretItem();
                secretItem.setSecretName(s.getName());
                secretItem.setAccountName(accountName);
                secretItem.setVaultUri(vaultItem.getVaultUri());
                return secretItem;
            }).toList();

            Platform.runLater(() -> {
                secrets.clear();
                secrets.addAll(secretItems);
            });
        })).exceptionally((e) -> {
            Platform.runLater(() -> {
                secrets.clear();
                treeView.getSelectionModel().clearSelection();
            });
            return null;
        });
    }

    public void showSecret() {
        var secret = secretsTable.getSelectionModel().getSelectedItem();
        if (secret == null)
            return;

        if (secret.getValue() == null)
            CompletableFuture
                    .runAsync(() -> lazyLoadSecret(secret))
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
            CompletableFuture.runAsync(() -> lazyLoadSecret(secret))
                    .thenAccept(v -> Platform.runLater(() -> copyToClipBoard(secret.getValue())))
                    .exceptionally(e -> {
                        showError(e.getMessage());
                        return null;
                    });
        else
            copyToClipBoard(secret.getValue());
    }

    public static void copyToClipBoard(String value) {
        Clipboard clipboard = Clipboard.getSystemClipboard();

        ClipboardContent content = new ClipboardContent();
        content.putString(value);

        clipboard.setContent(content);
    }

    private void lazyLoadSecret(SecretItem secret) {
        SecretClient client;
        client = secretClientService.getOrCreateClient(secret.getVaultUri(), secret.getAccountName());
        var val = client.getSecret(secret.getSecretName());
        Platform.runLater(() -> {
            secret.valueProperty().setValue((val.getValue()));
            secret.hiddenProperty().set(true);
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
            dialog.initOwner(mainStageProvider.getPrimaryStage());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));

            errorCtr.setDialogStage(dialog);
            errorCtr.setMessage(e);
            dialog.showAndWait();
        });
    }

    public void addSecret(ActionEvent actionEvent) {
        showError("OhOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO\nNo\nOh\nNo\nNo\nOh\nNo\nNo\nOh\nNo\nNo\nOh\nNo\nNo\nOh\nNo\nNo\nOh\nNo");
    }

    public void delete(ActionEvent actionEvent) {
        var y = treeView.getSelectionModel().getSelectedItem();

        if (y.getValue() instanceof SubscriptionItem si) {
            store.getAccounts().remove(si.getAccountName());
        }
    }
}
