package dev.revere.commission.frontend;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinSession;
import dev.revere.commission.Constants;
import dev.revere.commission.services.AuthService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainLayout extends AppLayout {
    private final AuthService m_authService;
    private final H2 m_title;
    private final Map<String, SideNav> m_categoryNavs = new HashMap<>();

    public MainLayout(AuthService p_authService) {
        m_authService = p_authService;
        m_title = new H2();

        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        addToDrawer(createDrawerContent());
    }

    private Component createHeaderContent() {
        final FlexLayout flexLayout = new FlexLayout();
        flexLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        flexLayout.setAlignContent(FlexLayout.ContentAlignment.CENTER);
        flexLayout.setWidthFull();
        flexLayout.setHeightFull();

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        layout.add(m_title);

        final Button logoutButton = new Button("Logout");
        logoutButton.addClickListener(p_buttonClickEvent -> m_authService.logout());
        logoutButton.getStyle().set("margin-right", "1rem");

        flexLayout.add(layout, logoutButton);

        return flexLayout;
    }

    private Component createDrawerContent() {
        VerticalLayout layout = new VerticalLayout();
        H2 logoLayout = new H2(Constants.PROJECT_NAME);

        getAuthorizedRoutes().forEach(route -> {
            SideNav categoryNav = m_categoryNavs.computeIfAbsent(route.category(), category -> {
                SideNav nav = new SideNav();
                nav.setLabel(category);
                return nav;
            });

            String path = RouteConfiguration.forSessionScope().getUrl(route.view());
            SideNavItem navItem = new SideNavItem(route.name(), path);
            navItem.setPrefixComponent(getIconForRoute(route.route()));
            categoryNav.addItem(navItem);
        });

        layout.add(logoLayout);
        m_categoryNavs.values().forEach(layout::add);
        return layout;
    }

    private Component getIconForRoute(String route) {
        return switch (route) {
            case "dashboard" -> VaadinIcon.DASHBOARD.create();
            case "commissions" -> VaadinIcon.CASH.create();
            case "freelancers" -> VaadinIcon.GROUP.create();
            case "accounts" -> VaadinIcon.USER.create();
            default -> VaadinIcon.CHEVRON_RIGHT.create();
        };
    }

    private List<AuthService.AuthorizedRoute> getAuthorizedRoutes() {
        final AuthService.Role role = VaadinSession.getCurrent().getAttribute(AuthService.Role.class);
        return role != null ? role.getAuthorizedRoutes() : List.of();
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        m_title.setText(getCurrentPageTitle());

        Class<? extends Component> currentViewClass = getContent().getClass().asSubclass(Component.class);
        String currentPath = RouteConfiguration.forSessionScope().getUrl(currentViewClass);

        m_categoryNavs.values().forEach(sideNav ->
                sideNav.getItems().forEach(item -> {
                    String itemPath = item.getPath();
                    String originalLabel = getAuthorizedRoutes()
                            .stream()
                            .filter(route -> RouteConfiguration.forSessionScope().getUrl(route.view()).equals(itemPath))
                            .map(AuthService.AuthorizedRoute::name)
                            .findFirst()
                            .orElse("");

                    if (itemPath.equals(currentPath)) {
                        item.setLabel(originalLabel);
                    } else {
                        item.setLabel(originalLabel);
                    }
                })
        );
    }

    private String getCurrentPageTitle() {
        return getContent().getClass().getAnnotation(Route.class).value();
    }
}