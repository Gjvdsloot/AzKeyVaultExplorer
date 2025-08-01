package com.gjvandersloot.controller;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.gjvandersloot.AppDataService;
import com.gjvandersloot.FxmlViewLoader;
import com.gjvandersloot.data.*;
import com.gjvandersloot.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class MainController {

    @FXML
    public TreeView<Object> treeView;

    @FXML
    public TableView<Secret> secretsTable;

    @FXML
    public TableColumn<Secret, String> secretsColumn;
    @FXML
    public TableColumn<Secret, String> secretValueColumn;

    @FXML
    public Button show;

    @FXML
    public TextField filterField;

    @FXML
    public TextField treeFilter;
    public Button copy;
    public TabPane TabManager;

    @Autowired
    private ApplicationContext context;

    @Autowired
    AccountService accountService;

    @Autowired
    Store store;

    @Autowired
    SecretClientService secretClientService;

    @Autowired
    MainStageProvider mainStageProvider;

    @Autowired
    FxmlViewLoader loader;

    @Autowired
    AppDataService appDataService;

    private TreeItem<Object> root;
    private TreeItem<Object> attachedRoot;

    private final ObservableList<Secret> secrets = FXCollections.observableArrayList();

    @Autowired
    private TabManagerService tabManagerService;

    @FXML
    public void initialize() {
        this.root = new TreeItem<>();
        treeView.setRoot(root);
        treeView.setShowRoot(false);

        loadTreeListeners();
        loadTree();
        setupTreeFilter();


        treeView.setCellFactory(tv -> {
            var cell = new TreeCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);

                    setText(null);
                    setGraphic(null);
                    setStyle("");

                    if (empty || item == null) {
                        return;
                    }

                    setText(item.toString());

                    if (item instanceof ILoadable loadable && loadable.getLoadFailed()) {
                        setStyle("-fx-text-fill: gray; -fx-opacity: 0.6;");
                    }
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty()) {
                    TreeItem<Object> treeItem = cell.getTreeItem();

                    // Only toggle if the item has children (i.e., is not a leaf)
                    if (!treeItem.isLeaf()) {
                        treeItem.setExpanded(!treeItem.isExpanded());
                        event.consume();
                        treeView.getSelectionModel().select(null);
                    }
                }
            });

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty() && cell.getItem() instanceof Vault vault) {
                    tabManagerService.openVault(vault);
                }
            });

            return cell;
        });
    }

    private void loadTreeListeners() {
        store.getAccounts().addListener((MapChangeListener<String, Account>) change -> {
            Account account = change.wasAdded() ? change.getValueAdded() : change.getValueRemoved();

            Set<String> subsToHandle = account.getTenants().values().stream()
                    .flatMap(t -> t.getSubscriptions().keySet().stream())
                    .collect(Collectors.toSet());

            root.getChildren().removeIf(node -> {
                Object value = node.getValue();
                return value instanceof Subscription s && subsToHandle.contains(s.getId());
            });

            if (change.wasAdded()) {
                Set<Subscription> subsToAdd = change.getValueAdded().getTenants().values().stream()
                        .flatMap(t -> t.getSubscriptions().values().stream())
                        .collect(Collectors.toSet());

                subsToAdd.forEach(s -> addSubscriptionItem(account, s, root));
            }
        });

        store.getAttachedVaults().addListener((MapChangeListener<String, Vault>) change -> {
            if (change.wasRemoved()) {
                var uriToRemove = change.getValueRemoved().getVaultUri();

                attachedRoot.getChildren().removeIf(node ->
                        ((Vault) node.getValue()).getVaultUri().equals(uriToRemove)
                );
            }
            if (change.wasAdded()) {
                addVaultItem(change.getValueAdded());
            }
        });
    }

    private void addVaultItem(Vault vault) {

        var ti = new TreeItem<Object>(vault);
        attachedRoot.getChildren().add(ti);
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

        FilteredList<Secret> filteredData = new FilteredList<>(secrets, p -> true);

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
    }

    public void addSubscription() {

        var dialog = createCancelDialog();

        var future = CompletableFuture.runAsync(() -> {
                    Account account;
                    try {
                        account = accountService.addAccount();
                        store.getAccounts().put(account.getUsername(), account);
                    } catch (Exception e) {
                        showError(e.getMessage());
                    }
                })
                .whenComplete((r, e) -> Platform.runLater((dialog.getStage())::close));

        dialog.setOnCancel(() -> {
            future.cancel(true);
            Platform.runLater((dialog.getStage())::close);
        });

        dialog.getStage().showAndWait();
    }

    private CancelDialogController createCancelDialog() {
        var loader = new FXMLLoader(getClass().getResource("/CancelDialog.fxml"));
        Parent root;
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
        cancelController.setStage(dialog);
        return cancelController;
    }

    public void loadTree() {
        var root = this.root;
        root.getChildren().clear();
        attachedRoot = new TreeItem<>("Attached");
        root.getChildren().add(attachedRoot);

        var accounts = store.getAccounts();

        for (var account : accounts.values()) {
            for (var tenant : account.getTenants().values()) {
                for (var subscription : tenant.getSubscriptions().values()) {
                    addSubscriptionItem(account, subscription, root);
                }
            }
        }

        store.getAttachedVaults().forEach((s, vault) -> addVaultItem(vault));
    }

    // Maybe remove root as it's equal to this.root. Might change later though when attached resources are added.
    private void addSubscriptionItem(Account account, Subscription subscription, TreeItem<Object> root) {
        var subscriptionItem = new Subscription();
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
        var subscription = (Subscription) treeItem.getValue();

        CompletableFuture.supplyAsync(() -> {
                    try {
                        AtomicReference<CancelDialogController> dlg = new AtomicReference<>();

                        return accountService.addKeyVaults(
                                subscription.getId(),
                                subscription.getAccountName(),
                                () -> Platform.runLater(() -> {
                                    dlg.set(createCancelDialog());
                                    dlg.get().getStage().showAndWait();
                                }),
                                () -> Platform.runLater((dlg.get().getStage())::close)
                        );
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
        }).thenAccept(vaults -> Platform.runLater(() -> {
            treeItem.getChildren().clear();

            var vaultItems = new ArrayList<Vault>();
            for (var vault : vaults) {
                var vaultItem = new Vault(vault.getVaultUri(), subscription.getAccountName());

                var child = new TreeItem<>();
                child.setValue(vaultItem);

                treeItem.getChildren().add(child);
                vaultItems.add(vaultItem);
            }

            subscription.setVaults(vaultItems);
        })).exceptionally((e) -> {
            Platform.runLater(() -> treeItem.getChildren().clear());
            showError(e.getMessage());
            return null;
        });
    }

    public static void copyToClipBoard(String value) {
        Clipboard clipboard = Clipboard.getSystemClipboard();

        ClipboardContent content = new ClipboardContent();
        content.putString(value);

        clipboard.setContent(content);
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

    // Button bar, with default account
    public void addAttached() throws IOException {
        Parent root = loader.load("/WizardView.fxml");

        var stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Add attached key vault");
        stage.initOwner(mainStageProvider.getPrimaryStage());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
}
