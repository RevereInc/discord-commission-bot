package com.flux.discordbot.frontend.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("/finished")
public class FinishedCommissionsView extends VerticalLayout {
    FinishedCommissionsView() {
        add(new H1("uhm commissions!!! but finished"));
    }
}
