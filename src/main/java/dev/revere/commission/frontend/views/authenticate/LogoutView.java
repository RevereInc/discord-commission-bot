package dev.revere.commission.frontend.views.authenticate;


import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import dev.revere.commission.Constants;
import dev.revere.commission.frontend.MainLayout;

@PageTitle(Constants.TITLE_LOGOUT)
@Route(value = Constants.PATH_LOGOUT, layout = MainLayout.class)
public class LogoutView extends Composite<VerticalLayout> {
    public LogoutView() {
        VaadinSession.getCurrent().getSession().invalidate();
        UI.getCurrent().getPage().reload();
    }

}
