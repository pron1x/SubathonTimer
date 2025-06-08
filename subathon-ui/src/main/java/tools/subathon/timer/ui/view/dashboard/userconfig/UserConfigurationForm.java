package tools.subathon.timer.ui.view.dashboard.userconfig;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;

public class UserConfigurationForm extends VerticalLayout {

    private final Binder<UserConfigurationModel> binder;
    private SaveHandler saveHandler;

    private final PasswordField seJwt = new PasswordField("StreamElements JWT Token");
    private final IntegerField followerSeconds = new IntegerField("Follower");
    private final IntegerField raiderSeconds = new IntegerField("Raider");
    private final IntegerField tier1Seconds = new IntegerField("Tier 1");
    private final IntegerField tier2Seconds = new IntegerField("Tier 2");
    private final IntegerField tier3Seconds = new IntegerField("Tier 3");
    private final IntegerField tier1GiftSeconds = new IntegerField("Tier 1 Gift");
    private final IntegerField tier2GiftSeconds = new IntegerField("Tier 2 Gift");
    private final IntegerField tier3GiftSeconds = new IntegerField("Tier 3 Gift");
    private final IntegerField bitsSeconds = new IntegerField("per 100 bits");
    private final IntegerField currencySeconds = new IntegerField("per EUR/USD");
    private final IntegerField initialSeconds = new IntegerField("Starting Timer (seconds)");
    private final Button saveButton = new Button("Save");

    public UserConfigurationForm() {
        binder = new BeanValidationBinder<>(UserConfigurationModel.class);
        initInternal();
    }

    public void setModel(UserConfigurationModel model) {
        binder.setBean(model);
    }

    public void setSaveHandler(SaveHandler saveHandler) {
        this.saveHandler = saveHandler;
    }

    private void initInternal() {
        tier1Seconds.addValueChangeListener(createValueCopier(tier1GiftSeconds));
        tier2Seconds.addValueChangeListener(createValueCopier(tier2GiftSeconds));
        tier3Seconds.addValueChangeListener(createValueCopier(tier3GiftSeconds));
        bitsSeconds.addValueChangeListener(createValueCopier(currencySeconds));

        FormLayout configForm = new FormLayout();
        configForm.add(seJwt, followerSeconds, raiderSeconds, tier1Seconds, tier1GiftSeconds, tier2Seconds, tier2GiftSeconds,
                tier3Seconds, tier3GiftSeconds, bitsSeconds, currencySeconds, initialSeconds);
        configForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("350px", 2));
        configForm.setWidth("400px");

        configForm.setColspan(seJwt, 2);
        configForm.setColspan(initialSeconds, 2);

        saveButton.addClickListener(event -> {
            if(binder.validate().isOk()) {
                saveHandler.save();
            }
        });
        binder.bindInstanceFields(this);

        add(new H3("Configure seconds to add for events"), configForm, saveButton);
    }

    private <T, E extends HasValue.ValueChangeEvent<T>> HasValue.ValueChangeListener<HasValue.ValueChangeEvent<T>> createValueCopier(HasValue<E, T> other) {
        return e -> {
            if (e.getValue() != null && other.isEmpty()) {
                other.setValue(e.getValue());
            }
        };
    }

    public interface SaveHandler {
        void save();
    }
}
