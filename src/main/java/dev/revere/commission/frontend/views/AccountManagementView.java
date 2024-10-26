package dev.revere.commission.frontend.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.revere.commission.Constants;
import dev.revere.commission.entities.Account;
import dev.revere.commission.frontend.MainLayout;
import dev.revere.commission.repository.AccountRepository;
import dev.revere.commission.services.AuthService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/25/2024
 */
@PermitAll
@PageTitle(Constants.TITLE_ACCOUNTS)
@Route(value = Constants.PATH_ACCOUNTS, layout = MainLayout.class)
public class AccountManagementView extends VerticalLayout {
    private final AccountRepository m_accountRepository;
    private final AuthService m_authService;
    private final Grid<Account> m_accountGrid;
    private final Dialog m_dialog;
    private Account m_currentAccount;

    public AccountManagementView(@Autowired AccountRepository p_accountRepository, @Autowired AuthService p_authService) {
        m_accountRepository = p_accountRepository;
        m_authService = p_authService;

        m_accountGrid = new Grid<>(Account.class, false);
        m_accountGrid.appendHeaderRow();

        final GridListDataView<Account> dataView = m_accountGrid.setItems(m_accountRepository.findAll());

        m_accountGrid.setHeight(80F, Unit.VH);
        m_accountGrid.addColumn(Account::getUsername).setHeader("Username");
        m_accountGrid.addColumn(Account::getRole).setHeader("Role");

        m_accountGrid.addComponentColumn(account -> {
            HorizontalLayout layout = new HorizontalLayout();

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addClickListener(e -> openAccountDialog(account));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addClickListener(e -> deleteAccount(account));

            layout.add(editButton, deleteButton);
            return layout;
        }).setHeader("Actions");

        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.addValueChangeListener(e -> dataView.refreshAll());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        dataView.addFilter(account -> {
            String filter = searchField.getValue().trim();
            if (filter.isEmpty()) return true;

            return account.getUsername().toLowerCase().contains(filter);
        });

        Button createButton = new Button("Create Account", new Icon(VaadinIcon.PLUS));
        createButton.addClickListener(e -> openAccountDialog(null));

        m_dialog = createAccountDialog();

        add(new HorizontalLayout(searchField, createButton), m_accountGrid);
    }

    private Dialog createAccountDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Account Details");

        TextField usernameField = new TextField("Username");
        TextField passwordField = new TextField("Password");
        ComboBox<AuthService.Role> roleSelector = new ComboBox<>("Role");
        roleSelector.setItems(AuthService.Role.values());

        Button saveButton = new Button("Save", e -> {
            try {
                if (m_currentAccount == null) {
                    m_authService.createAccount(usernameField.getValue(), passwordField.getValue(), roleSelector.getValue());
                } else {
                    m_currentAccount.setUsername(usernameField.getValue());
                    if (!passwordField.isEmpty()) {
                        m_currentAccount.setPassword(new BCryptPasswordEncoder().encode(passwordField.getValue()));
                    }
                    m_currentAccount.setRole(roleSelector.getValue());
                    m_accountRepository.save(m_currentAccount);
                }
                m_accountGrid.setItems(m_accountRepository.findAll());
                dialog.close();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });

        Button cancelButton = new Button("Cancel", e -> {
            m_currentAccount = null;
            dialog.close();
        });

        FormLayout formLayout = new FormLayout(usernameField, passwordField, roleSelector);
        dialog.add(formLayout);
        dialog.getFooter().add(saveButton, cancelButton);

        return dialog;
    }

    private void openAccountDialog(Account p_account) {
        TextField usernameField = null;
        ComboBox<AuthService.Role> roleSelector = null;

        for (Component component : m_dialog.getChildren()
                .filter(c -> c instanceof FormLayout)
                .findFirst()
                .orElse(new FormLayout()).getChildren().toList()) {

            if (component instanceof TextField) {
                usernameField = (TextField) component;
            } else if (component instanceof ComboBox) {
                roleSelector = (ComboBox<AuthService.Role>) component;
            }
        }

        if (p_account != null) {
            m_dialog.setHeaderTitle("Edit Account");
            m_currentAccount = p_account;

            usernameField.setValue(p_account.getUsername());
            roleSelector.setValue(p_account.getRole());
        } else {
            m_dialog.setHeaderTitle("Create Account");
            m_currentAccount = null;
            usernameField.clear();
            roleSelector.clear();
        }

        m_dialog.open();
    }

    private void deleteAccount(Account p_account) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Account");
        dialog.setText("Are you sure you want to delete this account?");

        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            m_accountRepository.delete(p_account);
            m_accountGrid.setItems(m_accountRepository.findAll());
            Notification.show("Account deleted", 3000, Notification.Position.TOP_CENTER);
        });

        dialog.open();
    }
}
