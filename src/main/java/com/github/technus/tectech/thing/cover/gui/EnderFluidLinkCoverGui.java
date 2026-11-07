package com.github.technus.tectech.thing.cover.gui;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.github.technus.tectech.thing.cover.GT_Cover_TM_EnderFluidLink;
import gregtech.api.modularui2.CoverGuiData;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.util.GT_LanguageManager;
import gregtech.common.gui.modularui.cover.base.CoverBaseGui;
import gregtech.common.modularui2.sync.LinkedBoolValue;
import gregtech.common.modularui2.widget.SelectButton;

public class EnderFluidLinkCoverGui extends CoverBaseGui<GT_Cover_TM_EnderFluidLink> {

    public EnderFluidLinkCoverGui(GT_Cover_TM_EnderFluidLink cover) {
        super(cover);
    }

    @Override
    protected String getGuiId() {
        return "cover.ender_fluid_link";
    }

    @Override
    public void addUIWidgets(PanelSyncManager syncManager, Flow column, CoverGuiData data) {
        StringSyncValue frequency = new StringSyncValue(cover::getFrequency, cover::setFrequency).allowC2S();
        BooleanSyncValue privateChannel = new BooleanSyncValue(
            cover::isPrivateChannel, value -> cover.setPrivateChannel(value, data.getPlayer())).allowC2S();
        BooleanSyncValue exporting = new BooleanSyncValue(cover::isExporting, cover::setExporting).allowC2S();
        syncManager.syncValue("frequency", frequency);
        syncManager.syncValue("private_channel", privateChannel);
        syncManager.syncValue("exporting", exporting);

        column.child(Flow.row().coverChildren().childPadding(8).marginBottom(8)
            .child(new TextFieldWidget().value(frequency).setMaxLength(9).size(82, 12))
            .child(label("328", "Channel").asWidget().verticalCenter()));
        column.child(Flow.row().coverChildren().childPadding(2).marginBottom(2)
            .child(new SelectButton().value(LinkedBoolValue.of(privateChannel, false)).size(16)
                .overlay(GTGuiTextures.OVERLAY_BUTTON_WHITELIST).addTooltipLine(label("326", "Public")))
            .child(new SelectButton().value(LinkedBoolValue.of(privateChannel, true)).size(16)
                .overlay(GTGuiTextures.OVERLAY_BUTTON_BLACKLIST).addTooltipLine(label("327", "Private")))
            .child(label("329", "Public/Private").asWidget().marginLeft(2).verticalCenter()));
        column.child(Flow.row().coverChildren().childPadding(2)
            .child(new SelectButton().value(LinkedBoolValue.of(exporting, false)).size(16)
                .overlay(GTGuiTextures.OVERLAY_BUTTON_IMPORT).addTooltipLine(label("007", "Import")))
            .child(new SelectButton().value(LinkedBoolValue.of(exporting, true)).size(16)
                .overlay(GTGuiTextures.OVERLAY_BUTTON_EXPORT).addTooltipLine(label("006", "Export")))
            .child(label("229", "Import/Export").asWidget().marginLeft(2).marginRight(TICK_RATE_BUTTON_SIZE)
                .verticalCenter()));
    }

    private static IKey label(String index, String english) {
        String key = "Interaction_DESCRIPTION_Index_" + index;
        return IKey.str(GT_LanguageManager.addStringLocalization(key, english, false));
    }
}
