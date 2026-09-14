package tools.subathon.timer.ui.view.points;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "points/")
@AnonymousAllowed
public class PointsView extends Div implements HasUrlParameter<String> {

    private final PointsPresenter pointsPresenter;

    public PointsView(PointsPresenter pointsPresenter) {
        this.pointsPresenter = pointsPresenter;
        setClassName("points");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        pointsPresenter.onAttach();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        pointsPresenter.onDetach();
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        pointsPresenter.initForChannel(parameter);
        bindText(pointsPresenter.getPointSignal().map(String::valueOf));
    }
}
