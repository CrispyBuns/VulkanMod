package net.vulkanmod.config;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.widget.CustomButtonWidget;
import net.vulkanmod.config.widget.OptionWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OptionScreenV extends Screen {
    private final List<OptionList2> optionLists;
    private Option<?>[] videoOpts;
    private Option<?>[] graphicsOpts;
    private Option<?>[] otherOpts;
    private final Screen parent;
    private OptionList2 currentList;

    private CustomButtonWidget videoButton;
    private CustomButtonWidget graphicsButton;
    private CustomButtonWidget otherButton;

    private Button doneButton;
    private Button applyButton;

    public OptionScreenV(Component title, Screen parent) {
        super(title);
        this.parent = parent;

        this.optionLists = new ArrayList<>();
        this.videoOpts = Options.getVideoOpts();
        this.graphicsOpts = Options.getGraphicsOpts();
        this.otherOpts = Options.getOtherOpts();

        this.videoButton = new CustomButtonWidget(20, 6, 60, 20, Component.literal("Video"), button -> this.setOptionList(button, optionLists.get(0)));
        this.graphicsButton = new CustomButtonWidget(81, 6, 60, 20, Component.literal("Graphics"), button -> this.setOptionList(button, optionLists.get(1)));
        this.otherButton = new CustomButtonWidget(142, 6, 60, 20, Component.literal("Other"), button -> this.setOptionList(button, optionLists.get(2)));

        this.videoButton.setSelected(true);

    }

    @Override
    protected void init() {

//        this.list = new OptionList2(this.minecraft, this.width, this.height, 32, this.height - 40, 25);
////        this.list.addSingleOptionEntry(new FullscreenOption(this.minecraft.getWindow()));
////        this.list.addSingleOptionEntry(Option.BIOME_BLEND_RADIUS);
////        this.list.addAll(OPTIONS);
//        this.list.addAll(this.options);
//
//        this.addSelectableChild(this.list);

        this.currentList = null;

        int top = 28;
        int bottom = 32;

        this.optionLists.clear();
        OptionList2 optionList = new OptionList2(this.minecraft, this.width, this.height, top, this.height - bottom, 25);
        optionList.addAll(this.videoOpts);
        this.optionLists.add(optionList);
        optionList = new OptionList2(this.minecraft, this.width, this.height, top, this.height - bottom, 25);
        optionList.addAll(this.graphicsOpts);
        this.optionLists.add(optionList);
        optionList = new OptionList2(this.minecraft, this.width, this.height, top, this.height - bottom, 25);
        optionList.addAll(this.otherOpts);
        this.optionLists.add(optionList);

        buildPage();

//        OptionWidget widget = new OptionWidget(this.width / 8, this.height / 2, 100, 20, Component.of("TEST")){};
//
//        this.addSelectableChild(widget);
//        this.addDrawableChild(widget);

        this.applyButton.active = false;
    }

    private void buildPage() {
        this.clearWidgets();

        if(this.currentList == null) this.currentList = optionLists.get(0);

        this.addWidget(currentList);

        this.buildHeader();

        this.addButtons();
    }

    private void buildHeader() {
        this.addRenderableWidget(this.videoButton);
        this.addRenderableWidget(this.graphicsButton);
        this.addRenderableWidget(this.otherButton);
    }

    private void addButtons() {
        int buttonX = (int) (this.width - 150);
        int buttonGap = 55;
        this.applyButton = new CustomButtonWidget(buttonX, this.height - 27, 50, 20, Component.literal("Apply"), button -> {
            Options.applyOptions(Initializer.CONFIG, new Option[][]{this.videoOpts, this.graphicsOpts, this.otherOpts});
        });
        this.doneButton = new CustomButtonWidget(buttonX + buttonGap, this.height - 27, 50, 20, CommonComponents.GUI_DONE, button -> {
            this.minecraft.setScreen(this.parent);
        });

        this.addRenderableWidget(this.applyButton);
        this.addRenderableWidget(this.doneButton);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener element : this.children()) {
            if (!element.mouseClicked(mouseX, mouseY, button)) continue;
            this.setFocused(element);
            if (button == 0) {
                this.setDragging(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.updateStatus();

        this.renderBackground(matrices);
        this.currentList.render(matrices, mouseX, mouseY, delta);
//        fill(matrices, 0, 0, width, height, VUtil.packColor(0.6f,0.2f, 0.2f, 0.5f));

//        VideoOptionsScreen.drawCenteredComponent(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
        List<FormattedCharSequence> list = getHoveredButtonTooltip(this.currentList, mouseX, mouseY);
        if (list != null) {
            this.renderTooltip(matrices, list, mouseX, mouseY);
        }
    }

    private List<FormattedCharSequence> getHoveredButtonTooltip(OptionList2 buttonList, int mouseX, int mouseY) {
        Optional<OptionWidget> optional = buttonList.getHoveredButton(mouseX, mouseY);
        if (optional.isPresent()) {
            if(optional.get().getTooltip() == null) return null;
            return this.minecraft.font.split(optional.get().getTooltip(), 200);
        }
        return ImmutableList.of();
    }

    public void updateStatus() {

        boolean modified = false;
        for(Option<?> option: this.videoOpts) {
            if(option.isModified()) modified = true;
        }
        for(Option<?> option: this.graphicsOpts) {
            if(option.isModified()) modified = true;
        }
        for(Option<?> option: this.otherOpts) {
            if(option.isModified()) modified = true;
        }

        this.applyButton.active = modified;
    }

    public void setOptionList(Button button, OptionList2 optionList) {
        this.currentList = optionList;

        this.buildPage();

        this.videoButton.setSelected(false);
        this.graphicsButton.setSelected(false);
        this.otherButton.setSelected(false);

        ((CustomButtonWidget)button).setSelected(true);
    }
}
