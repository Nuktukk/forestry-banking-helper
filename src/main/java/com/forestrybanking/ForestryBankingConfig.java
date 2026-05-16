package com.forestrybanking;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("forestrybanking")
public interface ForestryBankingConfig extends Config
{
	@ConfigSection(
		name = "Logs",
		description = "Menu swap options for logs in your bank inventory",
		position = 0
	)
	String logsSection = "logs";

	@ConfigItem(
		keyName = "logsLeftClick",
		name = "Left-click to deposit all",
		description = "Makes 'Deposit-All' the left-click action on logs while the bank is open",
		section = logsSection,
		position = 1
	)
	default boolean logsLeftClick()
	{
		return false;
	}

	@ConfigItem(
		keyName = "logsRightClick",
		name = "Promote to top of right-click",
		description = "Moves 'Deposit-All' to the top of the right-click menu on logs while the bank is open",
		section = logsSection,
		position = 2
	)
	default boolean logsRightClick()
	{
		return false;
	}

	@ConfigSection(
		name = "Log Basket",
		description = "Menu swap options for your log basket (inventory or worn Forestry kit)",
		position = 3
	)
	String basketSection = "basket";

	@ConfigItem(
		keyName = "basketLeftClick",
		name = "Left-click to empty",
		description = "Makes 'Empty' the left-click action on your log basket (or 'Empty log basket' on a worn Forestry kit) while the bank is open",
		section = basketSection,
		position = 4
	)
	default boolean basketLeftClick()
	{
		return false;
	}

	@ConfigItem(
		keyName = "basketRightClick",
		name = "Promote to top of right-click",
		description = "Moves 'Empty' to the top of the right-click menu on your log basket (or 'Empty log basket' on a worn Forestry kit) while the bank is open",
		section = basketSection,
		position = 5
	)
	default boolean basketRightClick()
	{
		return false;
	}
}
