package dev.revere.commission.frontend.views;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
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
import dev.revere.commission.discord.JDAInitializer;
import dev.revere.commission.entities.Account;
import dev.revere.commission.entities.Department;
import dev.revere.commission.frontend.MainLayout;
import dev.revere.commission.repository.DepartmentRepository;
import dev.revere.commission.services.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 11/1/2024
 * <p>
 * View for managing departments in the application.
 * Provides CRUD functionality to manage Department entities.
 */
@PageTitle(Constants.TITLE_DEPARTMENTS)
@Route(value = Constants.PATH_DEPARTMENTS, layout = MainLayout.class)
public class DepartmentsView extends VerticalLayout {
    private final DepartmentRepository m_departmentRepository;
    private final DepartmentService m_departmentService;
    private final Grid<Department> m_departmentGrid;

    public DepartmentsView(@Autowired DepartmentRepository p_departmentRepository,
                           @Autowired DepartmentService p_departmentService) {
        m_departmentRepository = p_departmentRepository;
        m_departmentService = p_departmentService;

        m_departmentGrid = new Grid<>(Department.class, false);
        m_departmentGrid.appendHeaderRow();

        GridListDataView<Department> dataView = m_departmentGrid.setItems(m_departmentRepository.findAll());

        m_departmentGrid.setHeight(80F, Unit.VH);
        m_departmentGrid.addColumn(Department::getName).setHeader("Department Name");
        m_departmentGrid.addColumn(Department::getMainGuildRoleId).setHeader("Main Guild Role ID");
        m_departmentGrid.addColumn(Department::getMainGuildCategoryID).setHeader("Main Guild Category ID");
        m_departmentGrid.addColumn(Department::getCommissionGuildRoleId).setHeader("Commission Guild Role ID");
        m_departmentGrid.addColumn(Department::getCommissionGuildCategoryID).setHeader("Commission Guild Category ID");

        m_departmentGrid.addComponentColumn(account -> {
            HorizontalLayout layout = new HorizontalLayout();

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addClickListener(e -> openEditDialog(account));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addClickListener(e -> openDeleteDialog(account));

            layout.add(editButton, deleteButton);
            return layout;
        }).setHeader("Actions");
        ;

        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(department -> {
            String searchTerm = searchField.getValue().trim();
            if (searchTerm.isEmpty()) return true;
            return department.getName().toLowerCase().contains(searchTerm.toLowerCase());
        });

        Button createButton = new Button("Create Department", new Icon(VaadinIcon.PLUS));
        createButton.addClickListener(e -> openCreateDialog());

        add(new HorizontalLayout(searchField, createButton), m_departmentGrid);
    }

    private void openEditDialog(Department department) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Department");

        TextField nameField = new TextField("Name");
        nameField.setValue(department.getName());

        TextField mainGuildRoleIdField = new TextField("Main Guild Role ID");
        mainGuildRoleIdField.setValue(String.valueOf(department.getMainGuildRoleId()));

        TextField mainGuildCategoryIDField = new TextField("Main Guild Category ID");
        mainGuildCategoryIDField.setValue(String.valueOf(department.getMainGuildCategoryID()));

        TextField commissionGuildRoleIdField = new TextField("Commission Guild Role ID");
        commissionGuildRoleIdField.setValue(String.valueOf(department.getCommissionGuildRoleId()));

        TextField commissionGuildCategoryIDField = new TextField("Commission Guild Category ID");
        commissionGuildCategoryIDField.setValue(String.valueOf(department.getCommissionGuildCategoryID()));

        Button saveButton = new Button("Save", e -> {
            department.setName(nameField.getValue());
            department.setMainGuildRoleId(Long.parseLong(mainGuildRoleIdField.getValue()));
            department.setCommissionGuildRoleId(Long.parseLong(commissionGuildRoleIdField.getValue()));
            m_departmentRepository.save(department);
            m_departmentGrid.getDataProvider().refreshItem(department);
            dialog.close();
            Notification.show("Department updated successfully");
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        VerticalLayout dialogLayout = new VerticalLayout(nameField, mainGuildRoleIdField, mainGuildCategoryIDField, commissionGuildRoleIdField, commissionGuildCategoryIDField);
        dialog.add(dialogLayout);
        dialog.getFooter().add(new HorizontalLayout(saveButton, cancelButton));

        dialog.open();
    }

    private void openDeleteDialog(Department department) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");

        confirmDialog.add("Are you sure you want to delete the department \"" + department.getName() + "\"?");


        Button confirmButton = new Button("Delete", e -> {
            m_departmentService.deleteDepartmentRoles(department.getName(), JDAInitializer.getShardManager());
            m_departmentService.deleteDepartment(department.getId());
            m_departmentGrid.setItems(m_departmentRepository.findAll());
            confirmDialog.close();
            Notification.show("Department deleted successfully");
        });

        Button cancelButton = new Button("Cancel", e -> confirmDialog.close());
        confirmDialog.getFooter().add(new HorizontalLayout(confirmButton, cancelButton));

        confirmDialog.open();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create Department");

        TextField nameField = new TextField("Name");

        Button createButton = new Button("Create", e -> {
            m_departmentService.createDepartmentInGuild(nameField.getValue(), JDAInitializer.getShardManager());
            m_departmentGrid.setItems(m_departmentRepository.findAll());
            dialog.close();
            Notification.show("Department created successfully");
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        VerticalLayout dialogLayout = new VerticalLayout(nameField);
        dialog.add(dialogLayout);
        dialog.getFooter().add(new HorizontalLayout(createButton, cancelButton));

        dialog.open();
    }
}