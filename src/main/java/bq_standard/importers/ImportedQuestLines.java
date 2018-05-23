package bq_standard.importers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;

public class ImportedQuestLines extends SimpleDatabase<IQuestLine> implements IQuestLineDatabase
{
	private final List<Integer> lineOrder = new ArrayList<>();
	private final IQuestLineDatabase parent;
	
	public ImportedQuestLines(IQuestLineDatabase parent)
	{
		this.parent = parent;
	}
	
	@Override
	public int getOrderIndex(int lineID)
	{
		if(getValue(lineID) == null)
		{
			return -1;
		} else if(!lineOrder.contains(lineID))
		{
			lineOrder.add(lineID);
		}
		
		return lineOrder.indexOf(lineID);
	}
	
	@Override
	public void setOrderIndex(int lineID, int index)
	{
		lineOrder.remove((Integer)lineID);
		lineOrder.add(index, lineID);
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(DBEntry<IQuestLine> entry : getEntries())
		{
			if(entry.getValue() == null)
			{
				continue;
			}
			
			int id = entry.getID();
			
			NBTTagCompound jObj = entry.getValue().writeToNBT(new NBTTagCompound(), saveType);
			jObj.setInteger("lineID", id);
			jObj.setInteger("order", getOrderIndex(id));
			json.appendTag(jObj);
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		reset();
		
		HashMap<Integer,Integer> orderMap = new HashMap<>();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jql = (NBTTagCompound)entry;
			
			int id = jql.hasKey("lineID", 99) ? jql.getInteger("lineID") : -1;
			int order = jql.hasKey("order", 99) ? jql.getInteger("order") : -1;
			
			if(id < 0)
			{
				continue;
			}
			
			IQuestLine line = this.createNew(id);
			line.readFromNBT(jql, saveType);
			
			if(order >= 0)
			{
				orderMap.put(order, id);
			}
		}
		
		List<Integer> orderKeys = new ArrayList<>(orderMap.keySet());
		Collections.sort(orderKeys);
		
		lineOrder.clear();
		for(int o : orderKeys)
		{
			lineOrder.add(orderMap.get(o));
		}
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		return null;
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
	}
	
	@Override
	public void removeQuest(int questID)
	{
		for(DBEntry<IQuestLine> ql : getEntries())
		{
			ql.getValue().removeID(questID);
		}
	}
	
	@Override
	public IQuestLine createNew(int id)
	{
		IQuestLine ql = parent.createNew(id);
		parent.removeID(id);
		add(id, ql);
		ql.setParentDatabase(this);
		return ql;
	}
}
