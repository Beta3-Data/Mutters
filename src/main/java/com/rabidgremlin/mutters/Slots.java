package com.rabidgremlin.mutters;

import java.util.HashMap;

public class Slots {

	private HashMap<String, Slot> slots = new HashMap<String, Slot>();
	
	public void add(Slot slot)
	{
		slots.put(slot.getName().toLowerCase(), slot);
	}
	
	public Slot getSlot(String name)
	{
		return slots.get(name.toLowerCase());
	}
	
}