package com.forestrybanking;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("forestrybanking")
public interface ForestryBankingConfig extends Config
{
	@ConfigSection(
		name = "Auto-Deposit",
		description = "Automatically deposit when the bank is opened",
		position = 0
	)
	String autoDepositSection = "autoDeposit";

	@ConfigItem(
		keyName = "autoDepositInventory",
		name = "Auto-deposit inventory logs",
		description = "When the bank opens, automatically deposit all logs sitting in your inventory",
		section = autoDepositSection,
		position = 1
	)
	default boolean autoDepositInventory()
	{
		return true;
	}

	@ConfigItem(
		keyName = "autoDepositBasket",
		name = "Auto-deposit basket logs",
		description = "When the bank opens, automatically empty your log basket (worn or in inventory) and deposit the logs",
		section = autoDepositSection,
		position = 2
	)
	default boolean autoDepositBasket()
	{
		return true;
	}

	@ConfigSection(
		name = "Manual",
		description = "Manual deposit options",
		position = 3
	)
	String manualSection = "manual";

	@ConfigItem(
		keyName = "clickToDepositAll",
		name = "Click logs to deposit all",
		description = "Adds a 'Deposit all logs' right-click option on logs while the bank is open — empties basket and deposits everything in one click",
		section = manualSection,
		position = 4
	)
	default boolean clickToDepositAll()
	{
		return false;
	}
}
