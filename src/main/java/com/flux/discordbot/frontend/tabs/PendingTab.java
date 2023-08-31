package com.flux.discordbot.frontend.tabs;

import com.flux.discordbot.frontend.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
@Route(value = "pending", layout = MainLayout.class)
@RouteAlias("")
public class PendingTab extends VerticalLayout {
    public PendingTab() {
        add(new H1("Real"));
    }
}
