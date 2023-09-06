package com.flux.discordbot.frontend.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("/pending")
public class PendingCommissionsView extends VerticalLayout {
    public PendingCommissionsView() {
        add(new H1("uhm commissions but pending!!!"));
    }
}
