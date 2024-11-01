package dev.revere.commission.frontend.views;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.revere.commission.Constants;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.frontend.MainLayout;
import dev.revere.commission.repository.CommissionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/25/2024
 */
@PageTitle(Constants.TITLE_COMMISSIONS)
@Route(value = Constants.PATH_COMMISSIONS, layout = MainLayout.class)
public class CommissionsView extends VerticalLayout {

    private final CommissionRepository m_commissionRepository;
    private final Grid<Commission> m_commissionGrid;
    private final ComboBox<String> m_stateSelector;

    public CommissionsView(@Autowired CommissionRepository p_commissionRepository) {
        m_commissionRepository = p_commissionRepository;

        m_commissionGrid = new Grid<>(Commission.class, false);
        m_commissionGrid.appendHeaderRow();

        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        m_stateSelector = new ComboBox<>();
        m_stateSelector.setItems("All", "Completed", "In Progress", "Pending", "Cancelled");
        m_stateSelector.addValueChangeListener(event -> updateGrid());
        m_stateSelector.setValue("All");

        configureGrid();

        searchField.addValueChangeListener(e -> updateGrid());

        add(new HorizontalLayout(m_stateSelector, searchField), m_commissionGrid);
        setHeightFull();
    }

    private void configureGrid() {
        m_commissionGrid.setHeight(80F, Unit.VH);
        m_commissionGrid.addColumn(Commission::getClient).setHeader("Client");
        m_commissionGrid.addColumn(Commission::getFreelancer).setHeader("Freelancer");
        m_commissionGrid.addColumn(Commission::getCategory).setHeader("Department");
        m_commissionGrid.addColumn(Commission::getDescription).setHeader("Description");
        m_commissionGrid.addColumn(Commission::getFormattedQuote).setHeader("Quote");
        m_commissionGrid.addColumn(Commission::getState).setHeader("State");
        m_commissionGrid.addColumn(commission -> {
            if (commission.getState() == Commission.State.CANCELLED) {
                return "Cancelled";
            } else if (commission.getInvoice() == null) {
                return "No Invoice";
            } else {
                return commission.getInvoice().isPaid() ? "Paid" : "Unpaid";
            }
        }).setHeader("Payment");

        updateGrid();
    }

    private void updateGrid() {
        String selectedState = m_stateSelector.getValue();
        List<Commission> filteredCommissions = getFilteredCommissions(selectedState);
        m_commissionGrid.setItems(filteredCommissions);
    }

    private List<Commission> getFilteredCommissions(String state) {
        return m_commissionRepository.findAll().stream()
                .filter(commission -> {
                    return switch (state) {
                        case "Cancelled" -> commission.getState() == Commission.State.CANCELLED;
                        case "Pending" -> commission.getState() == Commission.State.PENDING;
                        case "In Progress" -> commission.getState() == Commission.State.IN_PROGRESS;
                        case "Completed" -> commission.getState() == Commission.State.COMPLETED;
                        default -> true;
                    };
                })
                .collect(Collectors.toList());
    }
}
