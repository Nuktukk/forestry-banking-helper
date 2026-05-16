package com.forestrybanking;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ForestryBankingPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ForestryBankingPlugin.class);
		RuneLite.main(args);
	}
}
