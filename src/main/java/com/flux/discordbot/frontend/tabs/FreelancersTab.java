package com.flux.discordbot.frontend.tabs;

import com.flux.discordbot.entities.Freelancer;
import com.flux.discordbot.frontend.AuthCheck;
import com.flux.discordbot.frontend.MainLayout;
import com.flux.discordbot.repository.FreelancerRepository;
import com.flux.discordbot.services.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Freelancers | Flux Solutions")
@Route(value = "freelancers", layout = MainLayout.class)
public class FreelancersTab extends VerticalLayout implements AfterNavigationObserver {

    private final FreelancerRepository m_freelancerRepository;
    private final Grid<Freelancer> m_freelancerGrid;
    public FreelancersTab(@Autowired FreelancerRepository p_freelancerRepository) {
        m_freelancerRepository = p_freelancerRepository;

        m_freelancerGrid = new Grid<>(Freelancer.class, false);
        m_freelancerGrid.appendHeaderRow();

        final GridListDataView<Freelancer> dataView = m_freelancerGrid.setItems(m_freelancerRepository.findAll());

        // Set height and add columns
        m_freelancerGrid.setHeight(80F, Unit.VH);
        m_freelancerGrid.addColumn(Freelancer::getName).setHeader("Freelancer");
        m_freelancerGrid.addColumn(Freelancer::getUserId).setHeader("User ID");
        m_freelancerGrid.addColumn(Freelancer::getServiceRoleIds).setHeader("Services");
        m_freelancerGrid.addColumn(Freelancer::getBio).setHeader("Bio");

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

            return p_freelancer.getName().toLowerCase().contains(searchTerm.toLowerCase());
        });

        add(m_freelancerGrid, searchField);
    }

    @Override
    public void afterNavigation(final AfterNavigationEvent p_afterNavigationEvent) {
        new AuthCheck().doAuthChecks(FreelancersTab.class);
    }
}
