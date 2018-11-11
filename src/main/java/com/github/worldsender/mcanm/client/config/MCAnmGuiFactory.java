package com.github.worldsender.mcanm.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class MCAnmGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {
        // UNUSED
    }


    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        // UNUSED
        return null;
    }

    protected Class<? extends GuiConfig> mainConfigGuiClass() {
        return MCAnmGUI.class;
    }


    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        try {
            return this.mainConfigGuiClass().getDeclaredConstructor(GuiScreen.class).newInstance(parentScreen);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
