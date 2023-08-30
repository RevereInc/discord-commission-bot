package com.flux.discordbot.frontend;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;

@PreserveOnRefresh
public class MainLayout extends AppLayout {

    public MainLayout() {
        final String js = "document.documentElement.setAttribute('theme', $0)";
        getElement().executeJs(js, Lumo.DARK);

        final H1 title = new H1("Flux Solutions");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        final Tabs tabs = getTabs();

        addToDrawer(tabs);

        addToNavbar(title);
    }

    private Tabs getTabs() {
        final Tabs tabs = new Tabs();
        tabs.add();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        return tabs;
    }

    private Tab createTab(final VaadinIcon viewIcon, final String viewName, final Class<? extends Component> viewClass) {
        final Icon icon = viewIcon.create();
        icon.getStyle().set("box-sizing", "border-box")
                .set("margin-inline-end", "var(--lumo-space-m)")
                .set("margin-inline-start", "var(--lumo-space-xs)");

        final RouterLink link = new RouterLink();
        link.add(icon, new Span(viewName));
        link.setRoute(viewClass);
        link.setTabIndex(-1);

        return new Tab(link);
    }
}
