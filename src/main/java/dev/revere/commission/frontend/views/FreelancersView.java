package dev.revere.commission.frontend.views;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.frontend.MainLayout;
import dev.revere.commission.repository.DepartmentRepository;
import dev.revere.commission.repository.FreelancerRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/25/2024
 */
@PageTitle(Constants.TITLE_FREELANCERS)
@Route(value = Constants.PATH_FREELANCERS, layout = MainLayout.class)
public class FreelancersView extends VerticalLayout {
    private final FreelancerRepository m_freelancerRepository;
    private final DepartmentRepository m_departmentRepository;
    private final Grid<Freelancer> m_freelancerGrid;

    public FreelancersView(@Autowired FreelancerRepository p_freelancerRepository,
                           @Autowired DepartmentRepository p_departmentRepository) {
        m_freelancerRepository = p_freelancerRepository;
        m_departmentRepository = p_departmentRepository;

        m_freelancerGrid = new Grid<>(Freelancer.class, false);
        m_freelancerGrid.appendHeaderRow();

        final GridListDataView<Freelancer> dataView = m_freelancerGrid.setItems(m_freelancerRepository.findAll());

        m_freelancerGrid.setHeight(80F, Unit.VH);
        m_freelancerGrid.addColumn(Freelancer::getName).setHeader("Freelancer");
        m_freelancerGrid.addColumn(Freelancer::getUserId).setHeader("User ID");
        m_freelancerGrid.addColumn(freelancer ->
                freelancer.getDepartments().stream()
                        .map(Department::getName)
                        .collect(Collectors.joining(", "))
        ).setHeader("Departments");
        m_freelancerGrid.addColumn(Freelancer::getPortfolio).setHeader("Portfolio");
        m_freelancerGrid.addComponentColumn(this::createEditButton).setHeader("Actions");

        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(p_freelancer -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            return p_freelancer.getName().toLowerCase().contains(searchTerm.toLowerCase());
        });

        add(searchField, m_freelancerGrid);
    }

    private Button createEditButton(Freelancer freelancer) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addClickListener(e -> openEditDialog(freelancer));
        return editButton;
    }

    private void openEditDialog(Freelancer freelancer) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Freelancer");

        TextField nameField = new TextField("Name");
        nameField.setValue(freelancer.getName());

        TextField userIdField = new TextField("User ID");
        userIdField.setValue(String.valueOf(freelancer.getUserId()));

        TextField portfolioField = new TextField("Portfolio");
        portfolioField.setValue(freelancer.getPortfolio() != null ? freelancer.getPortfolio() : "");

        MultiSelectComboBox<Department> departmentSelect = new MultiSelectComboBox<>("Departments");
        List<Department> allDepartments = m_departmentRepository.findAll();
        departmentSelect.setItems(allDepartments);
        departmentSelect.setItemLabelGenerator(Department::getName);

        if (freelancer.getDepartments() != null) {
            Set<Department> selectedDepartments = allDepartments.stream()
                    .filter(dept -> freelancer.getDepartments().stream()
                            .anyMatch(freeDept -> freeDept.getId().equals(dept.getId())))
                    .collect(Collectors.toSet());
            departmentSelect.select(selectedDepartments);
        }

        Button saveButton = new Button("Save", e -> {
            freelancer.setName(nameField.getValue());
            freelancer.setUserId(Long.parseLong(userIdField.getValue()));

            String portfolio = portfolioField.getValue();
            freelancer.setPortfolio(portfolio.isEmpty() ? null : portfolio);

            freelancer.setDepartments(new ArrayList<>(departmentSelect.getSelectedItems()));

            m_freelancerRepository.save(freelancer);
            m_freelancerGrid.getDataProvider().refreshItem(freelancer);
            dialog.close();
            Notification.show("Freelancer updated successfully");
        });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        VerticalLayout dialogLayout = new VerticalLayout(nameField, userIdField, portfolioField, departmentSelect);
        dialog.add(dialogLayout);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        dialog.getFooter().add(buttonLayout);

        dialog.open();
    }
}