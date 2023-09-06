package com.flux.discordbot.frontend.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("/active")
public class ActiveCommissionsView extends VerticalLayout {
    ActiveCommissionsView() {
        add(new H1("uhm commissions!!! but active"));
    }
}
