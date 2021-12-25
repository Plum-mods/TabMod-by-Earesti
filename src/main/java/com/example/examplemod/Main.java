package com.example.examplemod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

@Mod(
        modid = "CyprymTab",
        name = "CyprymTab",
        version = "1.1"
)
public class Main {

   protected final Minecraft mc = Minecraft.getMinecraft();
   private ConcurrentHashMap cachedImages = new ConcurrentHashMap();
   private ConcurrentHashMap cachedTextures = new ConcurrentHashMap();
   private List fetchingSkins = Collections.synchronizedList(new ArrayList());


   @EventHandler
   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(this);
   }

   @EventHandler
   public void postInit(FMLPostInitializationEvent event) {}

   @SubscribeEvent(
           priority = EventPriority.HIGH
   )
   public void eventHandler(Pre event) {
      if(event.type == ElementType.PLAYER_LIST) {
         event.setCanceled(true);
      }

      ScoreObjective scoreobjective = this.mc.theWorld.getScoreboard().func_96539_a(0);
      NetHandlerPlayClient handler = this.mc.thePlayer.sendQueue;
      int width = event.resolution.getScaledWidth();
      int height = event.resolution.getScaledHeight();
      if(this.mc.gameSettings.keyBindPlayerList.getIsKeyPressed() && (!this.mc.isIntegratedServerRunning() || handler.playerInfoList.size() > 1 || scoreobjective != null) && event.type == ElementType.TEXT) {
         if(event.type == ElementType.PLAYER_LIST) {
            event.setCanceled(true);
         }

         this.mc.mcProfiler.startSection("playerList");
         ArrayList players = new ArrayList(handler.playerInfoList);
         int columns = 3;
         boolean playersPerPage = false;
         int columnWidth = 160;
         boolean columnHeight = true;
         int left = (width - columns * columnWidth) / 2;
         boolean border = true;
         byte currentPage = 0;
         GL11.glPushMatrix();
         GL11.glDisable(2929);
         GuiIngame var10000 = Minecraft.getMinecraft().ingameGUI;
         GuiIngame.drawRect(left, 0, left + columnWidth * columns - 1, 12, Integer.MIN_VALUE);

         int yPosOnline;
         for(yPosOnline = 0; yPosOnline < 105; ++yPosOnline) {
            int cellPtr = yPosOnline - currentPage * 120;
            int xPos = left + cellPtr % 3 * columnWidth;
            int yPos = 12 + cellPtr / 3 * 10;
            GuiIngame.drawRect(xPos, yPos, xPos + columnWidth - 1, yPos + 10 - 1, (new Color(158, 152, 152, 50)).getRGB());
         }
         for(yPosOnline = 0; yPosOnline < players.size(); ++yPosOnline) {
            int cellPtr = yPosOnline - currentPage * 120;
            int xPos = left + cellPtr % columns * columnWidth;
            int yPos = 13 + cellPtr / 3 * 10;

            if(yPosOnline < players.size()) {
              var10000 = Minecraft.getMinecraft().ingameGUI;
//               var10000.drawVerticalLine(15, 12, 200,(new Color(0, 152, 152, 100)).getRGB());
               //GuiIngame.drawRect(xPos, yPos, xPos + columnWidth - 2, yPos + 10 - 1, (new Color(158, 152, 152, 50)).getRGB());
               GuiPlayerInfo player = (GuiPlayerInfo)players.get(yPosOnline);
               String playerName = player.name;

               ScorePlayerTeam team = this.mc.theWorld.getScoreboard().getPlayersTeam(playerName);
               String[] displayName = ScorePlayerTeam.formatPlayerName(team, playerName).replaceAll("&", "§").split(" ");
//               String name = displayName[0];
               String name = displayName.length == 1?displayName[0]:displayName[1];
               String prefix = displayName.length > 1?displayName[0]:"";
               int strWidthPrefix = this.mc.fontRenderer.getStringWidth(prefix.replaceAll("§.", "")) / 2;
               this.mc.fontRenderer.drawString(name, xPos + 11, yPos, (new Color(255, 255, 255, 100)).getRGB());
               if(this.bindFace(StringUtils.stripControlCodes(playerName))) {
                  RenderUtil.drawImage(xPos, yPos-1, 9, 9);
               }
            }
         }

         yPosOnline = (int)Math.ceil((double)players.size() / (double)columns) * 195 + 167;
         var10000 = Minecraft.getMinecraft().ingameGUI;
         GuiIngame.drawRect(left, yPosOnline, left + columnWidth * columns - 1, yPosOnline + 11, Integer.MIN_VALUE);
         this.mc.ingameGUI.drawCenteredString(this.mc.fontRenderer, "§2Игроки онлайн", width / 2, 2, -1);
         this.mc.ingameGUI.drawCenteredString(this.mc.fontRenderer, "§a"+players.size()+"§r игроков из §a"+handler.currentServerMaxPlayers+"§r", width / 2, yPosOnline + 2, -1);
         GL11.glPopMatrix();
         GL11.glEnable(2929);
      }

   }

   protected void renderPlayerList(int width, int height) {}

   private boolean bindFace(String username) {
      if(this.fetchingSkins.contains(username)) {
         if(this.cachedImages.containsKey(username)) {
            DynamicTexture faceTex = new DynamicTexture((BufferedImage)this.cachedImages.get(username));
            this.cachedTextures.put(username, faceTex);
            this.cachedImages.remove(username);
         }

         return false;
      } else if(!this.cachedTextures.containsKey(username)) {
         this.fetchingSkins.add(username);
         (new Thread(new SkinLoader(username, this.cachedImages, this.fetchingSkins))).start();
         return false;
      } else {
         GL11.glBindTexture(3553, ((DynamicTexture)this.cachedTextures.get(username)).getGlTextureId());
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         return true;
      }
   }
}
