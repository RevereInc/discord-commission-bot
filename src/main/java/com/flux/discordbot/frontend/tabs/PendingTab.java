package com.flux.discordbot.frontend.tabs;

import com.flux.discordbot.entities.Commission;
import com.flux.discordbot.frontend.MainLayout;
import com.flux.discordbot.repository.CommissionRepository;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Pending | Flux Solutions")
@Route(value = "pending", layout = MainLayout.class)
public class PendingTab extends VerticalLayout {

    private final CommissionRepository m_commissionRepository;
    private final Grid<Commission> m_commissionGrid;

    public PendingTab(@Autowired CommissionRepository p_commissionRepository) {
        m_commissionRepository = p_commissionRepository;

        m_commissionGrid = new Grid<>(Commission.class, false);
        m_commissionGrid.appendHeaderRow();

        final GridListDataView<Commission> dataView = m_commissionGrid
                .setItems(m_commissionRepository.findAll()); // TODO: filter through commissions that are pending

        // Set height and add columns
        m_commissionGrid.setHeight(80F, Unit.VH);
        m_commissionGrid.addColumn(Commission::getFreelancer).setHeader("Freelancer");
        m_commissionGrid.addColumn(Commission::getQuote).setHeader("Quote");
        m_commissionGrid.addColumn(Commission::getDescription).setHeader("Description");

        // Add search bar
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

            return p_freelancer.getFreelancer().toLowerCase().contains(searchTerm.toLowerCase());
        });

        add(m_commissionGrid, searchField);
    }
}
