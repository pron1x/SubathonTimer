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

    private PasswordField seJwt;
    private IntegerField followerSeconds;
    private IntegerField raiderSeconds;
    private IntegerField tier1Seconds;
    private IntegerField tier2Seconds;
    private IntegerField tier3Seconds;
    private IntegerField tier1GiftSeconds;
    private IntegerField tier2GiftSeconds;
    private IntegerField tier3GiftSeconds;
    private IntegerField bitsSeconds;
    private IntegerField currencySeconds;
    private IntegerField initialSeconds;
    private Button saveButton;

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
        seJwt = new PasswordField("StreamElements JWT Token");
        followerSeconds = new IntegerField("Follower");
        raiderSeconds = new IntegerField("Raider");
        tier1Seconds = new IntegerField("Tier 1");
        tier2Seconds = new IntegerField("Tier 2");
        tier3Seconds = new IntegerField("Tier 3");
        tier1GiftSeconds = new IntegerField("Tier 1 Gift");
        tier2GiftSeconds = new IntegerField("Tier 2 Gift");
        tier3GiftSeconds = new IntegerField("Tier 3 Gift");
        bitsSeconds = new IntegerField("per 100 bits");
        currencySeconds = new IntegerField("per EUR/USD");
        initialSeconds = new IntegerField("Starting Timer (seconds)");
        saveButton = new Button("Save");

        seJwt.setRequired(true);
        followerSeconds.setRequired(true);
        raiderSeconds.setRequired(true);
        tier1Seconds.setRequired(true);
        tier2Seconds.setRequired(true);
        tier3Seconds.setRequired(true);
        tier1GiftSeconds.setRequired(true);
        tier2GiftSeconds.setRequired(true);
        tier3GiftSeconds.setRequired(true);
        bitsSeconds.setRequired(true);
        currencySeconds.setRequired(true);
        initialSeconds.setRequired(true);

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
            if (binder.validate().isOk()) {
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
