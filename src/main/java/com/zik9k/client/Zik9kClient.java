package com.zik9k.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class Zik9kClient implements ClientModInitializer {
    private static final int PANEL_WIDTH = 680;
    private static final int PANEL_HEIGHT = 400;
    private static final int SIDEBAR_WIDTH = 168;
    private static final String[] TABS = {"Combat", "Render", "Movement", "Player", "Misc"};
    private static final String[] TAB_MARKS = {"+", "o", ">", "*", "#"};
    private static KeyBinding openGuiKey;

    @Override public void onInitializeClient() {
        ClientConfig.load();
        ModuleManager.init();
        ESPWorldRenderer.register();
        BlockOverlayWorldRenderer.register();
        TracersWorldRenderer.register();
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.zik9k.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.zik9k"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.tick(client);
            while (openGuiKey.wasPressed()) if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
        });
    }

    private static final class ClickGuiScreen extends Screen {
        private int selectedTab, avatarIndex;
        private String searchQuery = "";
        private boolean searchFocused;
        private long lastBlinkTime;
        private boolean cursorVisible = true;
        private ClickGuiScreen() { super(Text.literal("TelikinesDLC")); avatarIndex = ClientConfig.avatarIndex(); }
        @Override protected void init() { searchFocused=false; lastBlinkTime=System.currentTimeMillis(); }
        private int panelLeft(){return (width-PANEL_WIDTH)/2;} private int panelTop(){return (height-PANEL_HEIGHT)/2;}
        private int searchTop(){return panelTop()+13;} private int searchBottom(){return panelTop()+33;}
        private int accentColor(){return switch(ClientConfig.accent()){case 1->0xFF8D67FF;case 2->0xFFE26BFF;default->0xFFB15CFF;};}
        private boolean matches(Module m){if(searchQuery.isBlank())return true;String q=searchQuery.toLowerCase();return m.getName().toLowerCase().contains(q)||m.getDescription().toLowerCase().contains(q);}
        @Override public void render(DrawContext c,int mx,int my,float delta){
            MinecraftClient client=MinecraftClient.getInstance();int oa=Math.max(0,Math.min(255,Math.round(ClientConfig.overlayOpacity()*255f/100f)));c.fill(0,0,width,height,(oa<<24)|0x08060D);
            int a=accentColor(),l=panelLeft(),t=panelTop(),r=l+PANEL_WIDTH,b=t+PANEL_HEIGHT;c.fill(l+5,t+7,r+5,b+7,0x42000000);c.fill(l+2,t,r-2,b,0xFF17131F);c.fill(l,t+2,r,b-2,0xFF17131F);c.fill(l+2,t+2,l+SIDEBAR_WIDTH,b-2,0xFF17121E);c.fill(l+SIDEBAR_WIDTH,t+46,r-2,b-2,0xFF120F18);
            c.drawText(textRenderer,Text.literal("T"),l+20,t+16,a,false);c.drawText(textRenderer,Text.literal("TELIKINESDLC"),l+40,t+16,0xFFF4EEF9,false);AvatarTextures.draw(c,client,avatarIndex,l+16,t+58,32);
            String u=client.getSession().getUsername();if(u.length()>13)u=u.substring(0,13);c.drawText(textRenderer,Text.literal(u),l+58,t+62,0xFFEFE8F3,false);c.drawText(textRenderer,Text.literal("Click avatar to change"),l+58,t+76,0xFF817787,false);
            for(int i=0;i<TABS.length;i++){int tt=t+112+i*48;boolean sel=i==selectedTab;if(sel){c.fill(l+10,tt-8,l+SIDEBAR_WIDTH-10,tt+28,0xFF2C2038);c.fill(l+10,tt-8,l+13,tt+28,a);}c.drawText(textRenderer,Text.literal(TAB_MARKS[i]),l+25,tt+3,sel?a:0xFF776D7D,false);c.drawText(textRenderer,Text.literal(TABS[i]),l+49,tt+3,sel?0xFFF7F1FA:0xFF8F8794,false);}
            int st=b-47;boolean sh=mx>=l+10&&mx<=l+SIDEBAR_WIDTH-10&&my>=st-6&&my<=st+24;if(sh)c.fill(l+10,st-6,l+SIDEBAR_WIDTH-10,st+24,0xFF28202F);c.drawText(textRenderer,Text.literal("S"),l+25,st+1,sh?a:0xFF776D7D,false);c.drawText(textRenderer,Text.literal("Settings"),l+49,st+1,sh?0xFFF7F1FA:0xFF8F8794,false);
            int cl=l+SIDEBAR_WIDTH+1;c.fill(cl,t+2,r-2,t+46,0xFF1A1622);c.fill(cl+18,t+13,cl+104,t+33,0xFF24202B);c.drawText(textRenderer,Text.literal("1.21.11"),cl+27,t+18,0xFFBBB2C2,false);c.drawText(textRenderer,Text.literal("v"),cl+88,t+18,0xFF766C7B,false);
            int sr=r-20,sl=sr-190;boolean hov=mx>=sl&&mx<=sr&&my>=searchTop()&&my<=searchBottom();c.fill(sl,searchTop(),sr,searchBottom(),searchFocused?0xFF2B2634:hov?0xFF292430:0xFF24202B);String vis=searchQuery.length()>24?searchQuery.substring(0,24):searchQuery;c.drawText(textRenderer,Text.literal(vis.isEmpty()?"Search":vis),sl+11,t+18,vis.isEmpty()?0xFF706775:0xFFD9D0DD,false);c.drawText(textRenderer,Text.literal("/"),sr-18,t+18,0xFF706775,false);
            if(searchFocused){if(System.currentTimeMillis()-lastBlinkTime>500){cursorVisible=!cursorVisible;lastBlinkTime=System.currentTimeMillis();}if(cursorVisible){int cx=sl+11+textRenderer.getWidth(vis)+1;c.fill(cx,t+17,cx+1,t+30,0xFFD9D0DD);}}
            c.drawText(textRenderer,Text.literal(TABS[selectedTab]),cl+24,t+68,0xFFF4EDF8,false);List<Module> mods=ModuleManager.getModules(ModuleCategory.values()[selectedTab]).stream().filter(this::matches).toList();if(mods.isEmpty())c.drawText(textRenderer,Text.literal(searchQuery.isBlank()?"No modules in this category":"No matching modules"),cl+24,t+91,0xFF6F6673,false);else{int cy=t+104;for(Module m:mods){if(cy>b-28)break;drawModuleCard(c,m,cl+22,cy,r-20,a,mx,my);cy+=58;}}super.render(c,mx,my,delta);
        }
        private void drawModuleCard(DrawContext c,Module m,int x,int y,int rr,int a,int mx,int my){boolean h=mx>=x&&mx<=rr&&my>=y&&my<=y+48;c.fill(x,y,rr,y+48,h?0xFF211B28:0xFF1B1720);if(m.isEnabled())c.fill(x,y,x+3,y+48,a);c.drawText(textRenderer,Text.literal(m.getName()),x+12,y+8,m.isEnabled()?0xFFF5EDF9:0xFFD8CFDD,false);String d=m.getDescription();if(d.length()>43)d=d.substring(0,43)+"...";c.drawText(textRenderer,Text.literal(d),x+12,y+25,0xFF817987,false);c.fill(rr-58,y+13,rr-12,y+34,m.isEnabled()?0xFF49305B:0xFF25202A);c.drawText(textRenderer,Text.literal(m.isEnabled()?"ON":"OFF"),rr-46,y+18,m.isEnabled()?0xFFE5D2EF:0xFF7E7584,false);}
        @Override public boolean mouseClicked(double mx,double my,int button){int l=panelLeft(),t=panelTop(),r=l+PANEL_WIDTH,b=t+PANEL_HEIGHT;if(button==1){List<Module> mods=ModuleManager.getModules(ModuleCategory.values()[selectedTab]).stream().filter(this::matches).toList();int cy=t+104;int cx=l+SIDEBAR_WIDTH+23;for(Module m:mods){if(cy>b-28)break;if(mx>=cx&&mx<=r-20&&my>=cy&&my<=cy+48){if(m instanceof TriggerBotModule tb){client.setScreen(new TriggerBotSettingsScreen(tb,this));return true;}if(m instanceof ESPModule e){client.setScreen(new ESPSettingsScreen(e,this));return true;}if(m instanceof BlockOverlayModule bo){client.setScreen(new BlockOverlaySettingsScreen(bo,this));return true;}if(m instanceof TracersModule tr){client.setScreen(new TracersSettingsScreen(tr,this));return true;}if(m instanceof KillAuraModule ka){client.setScreen(new KillAuraSettingsScreen(ka,this));return true;}return true;}cy+=58;}return super.mouseClicked(mx,my,button);}if(button!=0)return super.mouseClicked(mx,my,button);if(mx>=l+10&&mx<=l+SIDEBAR_WIDTH-10){for(int i=0;i<TABS.length;i++){int tt=t+112+i*48;if(my>=tt-8&&my<=tt+28){selectedTab=i;searchFocused=false;return true;}}int st=b-47;if(my>=st-6&&my<=st+24){client.setScreen(new SettingsScreen());return true;}}if(mx>=l+12&&mx<=l+52&&my>=t+54&&my<=t+94){avatarIndex=(avatarIndex+1)%AvatarTextures.count();ClientConfig.setAvatarIndex(avatarIndex);return true;}int sl=r-210,sr=r-20;if(mx>=sl&&mx<=sr&&my>=t+13&&my<=t+33){searchFocused=true;lastBlinkTime=System.currentTimeMillis();cursorVisible=true;return true;}List<Module> mods=ModuleManager.getModules(ModuleCategory.values()[selectedTab]).stream().filter(this::matches).toList();int cy=t+104,cx=l+SIDEBAR_WIDTH+23;for(Module m:mods){if(cy>b-28)break;if(mx>=cx&&mx<=r-20&&my>=cy&&my<=cy+48){m.toggle();return true;}cy+=58;}searchFocused=false;return super.mouseClicked(mx,my,button);}
        @Override public boolean charTyped(char ch,int mods){if(searchFocused&&ch>=32&&ch!=127&&searchQuery.length()<48){searchQuery+=ch;lastBlinkTime=System.currentTimeMillis();cursorVisible=true;return true;}return super.charTyped(ch,mods);}
        @Override public boolean keyPressed(int key,int scan,int mods){if(searchFocused){if(key==GLFW.GLFW_KEY_ESCAPE){searchFocused=false;return true;}if(key==GLFW.GLFW_KEY_BACKSPACE){if(!searchQuery.isEmpty())searchQuery=searchQuery.substring(0,searchQuery.length()-1);return true;}if(key==GLFW.GLFW_KEY_DELETE||key==GLFW.GLFW_KEY_A&&(mods&GLFW.GLFW_MOD_CONTROL)!=0){searchQuery="";return true;}}return super.keyPressed(key,scan,mods);}
        @Override public boolean shouldPause(){return false;}
    }
}
