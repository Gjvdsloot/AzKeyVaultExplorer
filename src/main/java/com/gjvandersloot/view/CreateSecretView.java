package com.gjvandersloot.view;

import com.gjvandersloot.utils.DialogUtils;
import com.gjvandersloot.data.Secret;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.viewmodel.CreateSecretViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateSecretView {
    @FXML private TextField secretNameField;
    @FXML private PasswordField secretValueField;
    @FXML private Button createBtn;

    @Autowired CreateSecretViewModel vm;
    @Autowired DialogUtils dialogUtils;

    @Getter
    private Secret result;


    public void setVault(Vault vault) {
        vm.setVault(vault);
    }

    @FXML
    private void initialize() {
        createBtn.disableProperty().bind(secretNameField.textProperty().isEmpty().or(secretValueField.textProperty().isEmpty()));
    }

    public void onCreate() {
        var stage = (Stage) createBtn.getScene().getWindow();
        try {
            var r = vm.createSecret(secretNameField.getText(), secretValueField.getText());

            var result = new Secret();
            result.setSecretName(r.getName());
            result.setValue(r.getValue());
            this.result = result;

            stage.close();
        } catch (Exception e) {

            dialogUtils.showError(e.getMessage(), stage);
        }
    }
}
