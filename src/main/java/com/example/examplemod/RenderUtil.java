package com.example.examplemod;

import net.minecraft.client.renderer.Tessellator;

public class RenderUtil {

   public static void drawImage(int xPos, int yPos, int width, int height) {
      Tessellator tessellator = Tessellator.instance;
      tessellator.startDrawingQuads();
      tessellator.addVertexWithUV((double)xPos, (double)(yPos + height), 0.0D, 0.0D, 1.0D);
      tessellator.addVertexWithUV((double)(xPos + width), (double)(yPos + height), 0.0D, 1.0D, 1.0D);
      tessellator.addVertexWithUV((double)(xPos + width), (double)yPos, 0.0D, 1.0D, 0.0D);
      tessellator.addVertexWithUV((double)xPos, (double)yPos, 0.0D, 0.0D, 0.0D);
      tessellator.draw();
   }
}
