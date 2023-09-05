package com.flux.discordbot.frontend.tabs;

import com.flux.discordbot.frontend.MainLayout;
import com.flux.discordbot.frontend.components.Card;
import com.flux.discordbot.repository.CommissionRepository;
import com.flux.discordbot.repository.FreelancerRepository;
import com.flux.discordbot.repository.ReviewRepository;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Stats | Flux Solutions")
@Route(value = "/stats", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class StatsTab extends FormLayout {

    private final FreelancerRepository m_freelancerRepository;
    private final CommissionRepository m_commissionRepository;
    private final ReviewRepository m_reviewRepository;

    public StatsTab(@Autowired FreelancerRepository p_freelancerRepository, @Autowired CommissionRepository p_commissionRepository, @Autowired ReviewRepository p_reviewRepository) {
        this.m_freelancerRepository = p_freelancerRepository;
        m_commissionRepository = p_commissionRepository;
        m_reviewRepository = p_reviewRepository;

        setResponsiveSteps(new ResponsiveStep("0", 4));

        H1 totalFreelancers = new H1(String.valueOf(m_freelancerRepository.count()));
        H1 totalCommissions = new H1(String.valueOf(m_commissionRepository.count()));
        H1 totalReviews = new H1(String.valueOf(m_reviewRepository.count()));

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
