package dev.revere.commission.frontend.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;

@CssImport("./styles/components/cards.css")
public class Card extends Div {
    private final Div m_container;

    public Card(final String p_title) {
        m_container = new Div();

        H3 h3 = new H3(p_title);
        h3.getStyle().set("margin-bottom", "8px");

        m_container.add(h3);
        m_container.addClassName("container");

        add(m_container);
        addClassName("stat-card");
        setWidthFull();
    }

    public void addItemToContainer(final Component... components) {
        m_container.add(components);
    }
}