package dev.revere.commission.frontend.views;

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
import dev.revere.commission.Constants;
import dev.revere.commission.entities.Department;
import dev.revere.commission.entities.Freelancer;
import dev.revere.commission.frontend.MainLayout;
import dev.revere.commission.repository.FreelancerRepository;
import org.springframework.beans.factory.annotation.Autowired;

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
    private final Grid<Freelancer> m_freelancerGrid;

    public FreelancersView(@Autowired FreelancerRepository p_freelancerRepository) {
        m_freelancerRepository = p_freelancerRepository;

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
        m_freelancerGrid.addColumn(Freelancer::getBio).setHeader("Bio");

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
}
