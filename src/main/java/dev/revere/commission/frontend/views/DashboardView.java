package dev.revere.commission.frontend.views;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import dev.revere.commission.Constants;
import dev.revere.commission.data.StripeInvoice;
import dev.revere.commission.entities.Commission;
import dev.revere.commission.frontend.MainLayout;
import dev.revere.commission.frontend.components.Card;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.repository.ReviewRepository;
import dev.revere.commission.services.AuthService;
import dev.revere.commission.services.PaymentService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle(Constants.TITLE_DASHBOARD)
@Route(value = Constants.PATH_DASHBOARD, layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(DashboardView.class);
    private final AuthService m_authService;
    private final PaymentService m_paymentService;

    public DashboardView(@Autowired FreelancerRepository p_freelancerRepository,
                         @Autowired CommissionRepository p_commissionRepository,
                         @Autowired ReviewRepository p_reviewRepository, AuthService p_authService, PaymentService p_paymentService) {
        m_authService = p_authService;
        m_paymentService = p_paymentService;

        addClassName("dashboard-view");
        setPadding(true);
        setSpacing(true);

        H2 welcomeText = new H2("Welcome back, " + m_authService.getCurrentUsername() + "!");
        welcomeText.addClassName("welcome-text");

        Paragraph overviewText = new Paragraph("Here's your commission management overview for today");
        overviewText.addClassName("overview-text");

        HorizontalLayout statsContainer = new HorizontalLayout();
        statsContainer.setWidthFull();
        statsContainer.addClassName("stats-container");

        double totalRevenue = calculateTotalRevenue(p_commissionRepository);
        H1 totalRevenueH1 = new H1(String.format("$%.2f", totalRevenue));
        H1 totalFreelancers = new H1(String.valueOf(p_freelancerRepository.count()));
        H1 totalCommissions = new H1(String.valueOf(p_commissionRepository.count()));
        H1 totalReviews = new H1(String.valueOf(p_reviewRepository.count()));

        statsContainer.add(
                createStatCard("Total Revenue", totalRevenueH1),
                createStatCard("Total Freelancers", totalFreelancers),
                createStatCard("Total Commissions", totalCommissions),
                createStatCard("Total Reviews", totalReviews)
        );

        H3 recentActivityTitle = new H3("Recent Activity");
        VerticalLayout recentActivity = createRecentActivitySection(p_commissionRepository);
        HorizontalLayout analyticsContainer = new HorizontalLayout();
        analyticsContainer.setWidthFull();
        analyticsContainer.addClassName("analytics-container");

        add(welcomeText, overviewText, statsContainer, analyticsContainer,
                recentActivityTitle, recentActivity);
    }

    private double calculateTotalRevenue(CommissionRepository commissionRepository) {
        return commissionRepository.findAll().stream()
                .mapToDouble(commission -> {
                    if (commission.hasInvoice()) {
                        return commission.getInvoice().getAmountPaid();
                    }
                    return 0.0;
                })
                .sum();
    }

    private Card createStatCard(final String p_title, final H1 p_value) {
        final Card card = new Card(p_title);
        card.addClassName("stat-card");

        p_value.setWidthFull();
        p_value.getStyle()
                .set("text-align", "right")
                .set("margin", "0")
                .set("color", "var(--lumo-primary-color)");

        card.addItemToContainer(p_value);

        return card;
    }

    private VerticalLayout createRecentActivitySection(CommissionRepository commissionRepository) {
        VerticalLayout activity = new VerticalLayout();
        activity.addClassName("recent-activity");
        activity.setSpacing(false);
        activity.setPadding(true);

        PageRequest pageRequest = PageRequest.of(0, 5);
        commissionRepository.findByOrderByCreatedAtDesc(pageRequest).forEach(commission -> {
            HorizontalLayout item = new HorizontalLayout();
            item.setAlignItems(Alignment.CENTER);

            Icon icon = getIcon(commission);
            Span text = new Span(commission.getClient());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
            Span date = new Span(commission.getCreatedAt().format(formatter));

            item.add(icon, text, date);
            activity.add(item);
        });

        return activity;
    }

    private static @NotNull Icon getIcon(Commission commission) {
        Icon icon = new Icon(VaadinIcon.CIRCLE);

        if (commission.getState() == Commission.State.COMPLETED) {
            icon.setColor("var(--lumo-success-color)");
        } else if (commission.getState() == Commission.State.IN_PROGRESS) {
            icon.setColor("var(--lumo-contrast-10pct)");
        } else if (commission.getState() == Commission.State.CANCELLED) {
            icon.setColor("var(--lumo-error-color)");
        } else {
            icon.setColor("var(--lumo-primary-color)");
        }
        return icon;
    }
}