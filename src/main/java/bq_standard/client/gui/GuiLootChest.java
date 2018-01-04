package bq_standard.client.gui;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;

public class GuiLootChest extends GuiScreen
{
	static ResourceLocation guiChest = new ResourceLocation("bq_standard","textures/gui/gui_loot_chest.png");
	ArrayList<BigItemStack> rewards = new ArrayList<BigItemStack>();
	String title;
	
	public GuiLootChest(ArrayList<BigItemStack> rewards2, String title)
	{
		this.rewards = rewards2;
		this.title = title;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(new SoundEvent(new ResourceLocation("random.chestopen")), 1.0F));
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		this.drawDefaultBackground();
		
		mc.renderEngine.bindTexture(guiChest);
		
		int cw = 128;
		int ch = 68;
		
		this.drawTexturedModalRect(width/2 - cw/2, height/2, 0, 0, cw, ch);
		
		String txt = TextFormatting.BOLD + "" + TextFormatting.UNDERLINE + I18n.format(title);
		mc.fontRenderer.drawString(txt, width/2 - mc.fontRenderer.getStringWidth(txt)/2, height/2 + ch + 8, Color.WHITE.getRGB(), false);
		
		// Auto balance row size
		int rowL = MathHelper.ceil(rewards.size()/8F);
		rowL = MathHelper.ceil(rewards.size()/rowL);
		
		BigItemStack ttStack = null;
		
		GlStateManager.pushMatrix();
		
		for(int i = 0; i < rewards.size(); i++)
		{
			mc.renderEngine.bindTexture(guiChest);
			
			int n1 = i%rowL;
			int n2 = i/rowL;
			int n3 = Math.min(rewards.size() - n2*rowL, rowL);
			
			int rx = (width/2) - (36 * n3)/2 + (36 * n1);
			int ry = height/2 - 36 - (n2 * 36);
			
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			
			this.drawTexturedModalRect(rx, ry, 128, 0, 32, 32);
			
			BigItemStack stack = rewards.get(i);
			RenderUtils.RenderItemStack(mc, stack.getBaseStack(), rx + 8, ry + 8, stack == null || stack.stackSize <= 1? "" : "" + stack.stackSize);
			
			if(mx >= rx + 8 && mx < rx + 24 && my >= ry + 8 && my < ry + 24)
			{
				ttStack = stack;
			}
		}
		
		GlStateManager.popMatrix();
		
		if(ttStack != null)
		{
			this.drawHoveringText(ttStack.getBaseStack().getTooltip(mc.player, mc.gameSettings.advancedItemTooltips? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL), mx, my, fontRenderer);
		}
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}
