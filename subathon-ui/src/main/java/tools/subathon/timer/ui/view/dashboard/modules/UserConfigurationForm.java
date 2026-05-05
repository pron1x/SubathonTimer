package tools.subathon.timer.ui.view.dashboard.modules;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.theme.lumo.LumoUtility;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.util.TemplateParser;

import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class UserConfigurationForm extends VerticalLayout {

    private final Binder<UserConfigurationDto> binder;
    private SaveHandler saveHandler;

    private final TextField id;
    private final TextField channelId;
    private final PasswordField seJwt;
    private final IntegerField followerSeconds;
    private final IntegerField raiderSeconds;
    private final IntegerField tier1Seconds;
    private final IntegerField tier2Seconds;
    private final IntegerField tier3Seconds;
    private final IntegerField tier1GiftSeconds;
    private final IntegerField tier2GiftSeconds;
    private final IntegerField tier3GiftSeconds;
    private final IntegerField bitsSeconds;
    private final IntegerField currencySeconds;
    private final IntegerField initialSeconds;
    private final TextField donationTemplatePattern;
    private final TextField donationTemplateUser;
    private final Button saveButton;

    public UserConfigurationForm() {
        binder = new BeanValidationBinder<>(UserConfigurationDto.class);
        id = new TextField();
        channelId = new TextField();
        seJwt = new PasswordField("StreamElements JWT Token");
        followerSeconds = new IntegerField("Follower");
        raiderSeconds = new IntegerField("per Raider");
        tier1Seconds = new IntegerField("Tier 1");
        tier2Seconds = new IntegerField("Tier 2");
        tier3Seconds = new IntegerField("Tier 3");
        tier1GiftSeconds = new IntegerField("Tier 1 Gift");
        tier2GiftSeconds = new IntegerField("Tier 2 Gift");
        tier3GiftSeconds = new IntegerField("Tier 3 Gift");
        bitsSeconds = new IntegerField("per 100 bits");
        currencySeconds = new IntegerField("per EUR/USD");
        initialSeconds = new IntegerField("Starting Time (seconds)");
        donationTemplatePattern = new TextField("Donation message pattern");
        donationTemplateUser = new TextField("Donation message bot name");

        donationTemplatePattern.setHelperText("Placeholders: {user} and {amount}\nExample: {user} just tipped {amount} EUR!");
        donationTemplatePattern.setTooltipText("You can also copy the message template from the donation provider and adjust the placeholders as needed.");
        saveButton = new Button("Save");
        initInternal();
    }

    public void setModel(UserConfigurationDto model) {
        binder.readRecord(model);
    }

    public void setSaveHandler(SaveHandler saveHandler) {
        this.saveHandler = saveHandler;
    }

    private void initInternal() {
        setAlignItems(Alignment.CENTER);
        id.setVisible(false);
        id.setEnabled(false);

        channelId.setVisible(false);
        channelId.setEnabled(false);

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
        configForm.setAutoResponsive(true);
        configForm.setExpandFields(true);
        configForm.setColumnWidth("15em");

        configForm.addFormRow(id);
        FormLayout.FormRow row1 = new FormLayout.FormRow();
        row1.add(seJwt, 2);

        Span eventsHeader = new Span("Configure seconds for each twitch event. 0 ignores events");
        eventsHeader.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextAlignment.CENTER);
        FormLayout.FormRow subheader1 = new FormLayout.FormRow();
        subheader1.add(eventsHeader, 2);

        FormLayout.FormRow row2 = new FormLayout.FormRow();
        row2.add(followerSeconds, raiderSeconds);

        FormLayout.FormRow row3 = new FormLayout.FormRow();
        row3.add(tier1Seconds, tier1GiftSeconds);

        FormLayout.FormRow row4 = new FormLayout.FormRow();
        row4.add(tier2Seconds, tier2GiftSeconds);

        FormLayout.FormRow row5 = new FormLayout.FormRow();
        row5.add(tier3Seconds, tier3GiftSeconds);

        FormLayout.FormRow row6 = new FormLayout.FormRow();
        row6.add(bitsSeconds, currencySeconds);

        Span startingHeader = new Span("Configure start time of the timer in seconds");
        startingHeader.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextAlignment.CENTER);
        FormLayout.FormRow subheader2 = new FormLayout.FormRow();
        subheader2.add(startingHeader, 2);

        FormLayout.FormRow row7 = new FormLayout.FormRow();
        row7.add(initialSeconds, 2);

        Span donationMessageHeader = new Span("Configure the donation message template");
        donationMessageHeader.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextAlignment.CENTER);
        FormLayout.FormRow subheader3 = new FormLayout.FormRow();
        subheader3.add(donationMessageHeader, 2);

        FormLayout.FormRow row8 = new FormLayout.FormRow();
        row8.add(donationTemplatePattern, donationTemplateUser);

        configForm.add(row1, subheader1, row2, row3, row4, row5, row6, subheader2, row7, subheader3, row8);
        Div wrapper = new Div(configForm);
        wrapper.setSizeFull();
        wrapper.getStyle()
                .set("display", "flex")
                .set("justify-content", "center");

        saveButton.addClickListener(event -> {
            if (binder.validate().isOk()) {
                try {
                    saveHandler.save(binder.writeRecord());
                } catch (ValidationException e) {
                    Notification.show("Could not save configuration, check values and try again!", 5000, Notification.Position.MIDDLE);
                }
            } else {
                Notification.show("Could not save configuration, check values and try again!", 5000, Notification.Position.MIDDLE);
            }
        });
        bindFields();

        H3 header = new H3("Timer Configuration");
        add(header, wrapper, saveButton);
    }

    private void bindFields() {
        // Need to bind the empty text field to allow writing record.
        binder.bind(channelId, "channelId");
        binder.forField(id).withConverter(
                stringValue -> !Objects.equals(stringValue, id.getEmptyValue()) ? Long.valueOf(stringValue) : null,
                longValue -> longValue != null ? longValue.toString() : id.getEmptyValue()).bind("id");
        binder.bind(seJwt, "seJwt");
        binder.bind(followerSeconds, "followerSeconds");
        binder.bind(raiderSeconds, "raiderSeconds");
        binder.bind(tier1Seconds, "tier1Seconds");
        binder.bind(tier2Seconds, "tier2Seconds");
        binder.bind(tier3Seconds, "tier3Seconds");
        binder.bind(tier1GiftSeconds, "tier1GiftSeconds");
        binder.bind(tier2GiftSeconds, "tier2GiftSeconds");
        binder.bind(tier3GiftSeconds, "tier3GiftSeconds");
        binder.bind(bitsSeconds, "bitsSeconds");
        binder.bind(currencySeconds, "currencySeconds");
        binder.bind(initialSeconds, "initialSeconds");
        binder.forField(donationTemplatePattern)
                        .withValidator(pattern -> {
                            try {
                                TemplateParser.builder().withTemplate(pattern).build();
                                return true;
                            } catch (IllegalArgumentException ex) {
                                return false;
                            }
                        }, "Invalid template! Only {amount} and {user} placeholders are allowed, with at least one character between them.")
                .bind("donationTemplatePattern");
        binder.bind(donationTemplateUser, "donationTemplateUser");
    }

    private <T, E extends HasValue.ValueChangeEvent<T>> HasValue.ValueChangeListener<HasValue.ValueChangeEvent<T>> createValueCopier(HasValue<E, T> other) {
        return e -> {
            if (e.getValue() != null && other.isEmpty()) {
                other.setValue(e.getValue());
            }
        };
    }

    public interface SaveHandler {
        void save(UserConfigurationDto configModel);
    }
}
