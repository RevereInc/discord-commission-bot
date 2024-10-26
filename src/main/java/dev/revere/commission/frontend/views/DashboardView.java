package dev.revere.commission.frontend.views;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import dev.revere.commission.Constants;
import dev.revere.commission.frontend.MainLayout;
import dev.revere.commission.frontend.components.Card;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle(Constants.TITLE_DASHBOARD)
@Route(value = Constants.PATH_DASHBOARD, layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class DashboardView extends FormLayout {
    public DashboardView(@Autowired FreelancerRepository p_freelancerRepository, @Autowired CommissionRepository p_commissionRepository, @Autowired ReviewRepository p_reviewRepository) {
        setResponsiveSteps(new ResponsiveStep("0", 4));

        H1 totalFreelancers = new H1(String.valueOf(p_freelancerRepository.count()));
        H1 totalCommissions = new H1(String.valueOf(p_commissionRepository.count()));
        H1 totalReviews = new H1(String.valueOf(p_reviewRepository.count()));

        add(
                createStatCard("Total Freelancers", totalFreelancers),
                createStatCard("Total Commissions", totalCommissions),
                createStatCard("Total Reviews", totalReviews)
        );

        getStyle().set("margin-left", "2rem");
    }

    private Card createStatCard(final String p_title, final H1 p_value) {
        final Card usersCard = new Card(p_title);
        p_value.setWidthFull();
        p_value.getStyle().set("text-align", "right");
        usersCard.addItemToContainer(p_value);

        return usersCard;
    }
}
