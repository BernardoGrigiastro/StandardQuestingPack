package bq_standard.rewards;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import bq_standard.client.gui2.rewards.PanelRewardScoreboard;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.factory.FactoryRewardScoreboard;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

public class RewardScoreboard implements IReward
{
	public String score = "Reputation";
	public String type = "dummy";
	public boolean relative = true;
	public int value = 1;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardScoreboard.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.scoreboard";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, IQuest quest)
	{
		return true;
	}
	
	@Override
	public void claimReward(EntityPlayer player, IQuest quest)
	{
		Scoreboard board = player.getWorldScoreboard();
		
		if(board == null)
		{
			return;
		}
		
		ScoreObjective scoreObj = board.getObjective(score);
		
		if(scoreObj == null)
		{
			try
			{
		        IScoreCriteria criteria = IScoreCriteria.INSTANCES.get(type);
		        criteria = criteria != null? criteria : new ScoreCriteria(score);
				scoreObj = board.addScoreObjective(score, criteria);
				scoreObj.setDisplayName(score);
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to create score '" + score + "' for reward!", e);
			}
		}
		
		if(scoreObj == null || scoreObj.getCriteria().isReadOnly())
		{
			return;
		}
		
		Score s = board.getOrCreateScore(player.getName(), scoreObj);
		
		if(relative)
		{
			s.increaseScore(value);
		} else
		{
			s.setScorePoints(value);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		score = json.getString("score");
		type = json.getString("type");
		value = json.getInteger("value");
		relative = json.getBoolean("relative");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		json.setString("score", score);
		json.setString("type", "dummy");
		json.setInteger("value", value);
		json.setBoolean("relative", relative);
		return json;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getRewardGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelRewardScoreboard(rect, quest, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getRewardEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
