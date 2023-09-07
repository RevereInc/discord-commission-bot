package com.flux.discordbot.frontend;

import com.flux.discordbot.frontend.tabs.CommissionsTab;
import com.flux.discordbot.frontend.tabs.FreelancersTab;
import com.flux.discordbot.frontend.tabs.PendingTab;
import com.flux.discordbot.frontend.tabs.StatsTab;
import com.flux.discordbot.services.AuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Optional;

@PreserveOnRefresh
public class MainLayout extends AppLayout {
    private final Tabs m_menu;
    private final H1 m_title;
    private final AuthService m_authService;

    public MainLayout(final AuthService p_authService) {
        m_authService = p_authService;
        m_title = new H1();
        m_title.getStyle().setMargin("1rem");

        final String js = "document.documentElement.setAttribute('theme', $0)";
        getElement().executeJs(js, Lumo.DARK);

        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());

        m_menu = createMenu();
        addToDrawer(m_menu);
    }

    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        layout.add(m_title);
        return layout;
    }

    private Component createDrawerContent(final Tabs p_menu) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        final HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(logoLayout, p_menu);
        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {
        final AuthService.Role role = VaadinSession.getCurrent().getAttribute(AuthService.Role.class);

        if (role == null) {
            return new Component[]{};
        }

        return role
                .getAuthorizedRoutes()
                .stream()
                .map(p_route -> createTab(p_route.name(), p_route.view()))
                .toArray(Component[]::new);
    }

    private static Tab createTab(final String p_text, final Class<? extends Component> p_navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(p_text, p_navigationTarget));
        ComponentUtil.setData(tab, Class.class, p_navigationTarget);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent()).ifPresent(m_menu::setSelectedTab);
        m_title.setText(getCurrentPageTitle());
    }

    private Optional<Tab> getTabForComponent(final Component p_component) {
        return m_menu.getChildren()
                .filter(p_tab -> ComponentUtil.getData(p_tab, Class.class)
                        .equals(p_component.getClass()))
                .findFirst().map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        return getContent().getClass().getAnnotation(PageTitle.class).value();
    }
}
