package bq_standard;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import bq_standard.network.StandardPacketType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ScoreboardBQ
{
	static ConcurrentHashMap<String, ScoreBQ> objectives = new ConcurrentHashMap<String, ScoreBQ>();
	
	public static int getScore(UUID uuid, String scoreName)
	{
		ScoreBQ score = objectives.get(scoreName);
		
		if(score == null)
		{
			return 0;
		} else
		{
			return score.getScore(uuid);
		}
	}
	
	public static void setScore(EntityPlayer player, String scoreName, int value)
	{
		ScoreBQ score = objectives.get(scoreName);
		
		if(score == null)
		{
			score = new ScoreBQ();
			objectives.put(scoreName, score);
		}
		
		score.setScore(QuestingAPI.getQuestingUUID(player), value);
		
		if(player instanceof EntityPlayerMP)
		{
			SendToClient((EntityPlayerMP)player);
		}
	}
	
	public static void SendToClient(EntityPlayerMP player)
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject json = new JsonObject();
		writeJson(json);
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayer(new QuestingPacket(StandardPacketType.SCORE_SYNC.GetLocation(), tags), player);
	}
	
	public static void readJson(JsonObject json)
	{
		objectives.clear();
		for(JsonElement element : JsonHelper.GetArray(json, "objectives"))
		{
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			JsonObject jObj = element.getAsJsonObject();
			String name = JsonHelper.GetString(jObj, "name", "");
			ScoreBQ score = new ScoreBQ();
			
			if(name.length() <= 0)
			{
				continue;
			}
			
			score.readJson(jObj);
			objectives.put(name, score);
		}
	}
	
	public static void writeJson(JsonObject json)
	{
		JsonArray jAry = new JsonArray();
		for(Entry<String,ScoreBQ> entry : objectives.entrySet())
		{
			JsonObject jObj = new JsonObject();
			jObj.addProperty("name", entry.getKey());
			entry.getValue().writeJson(jObj);
			jAry.add(jObj);
		}
		json.add("objectives", jAry);
	}
}
