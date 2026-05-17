package com.forestrybanking;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Forestry Banking Helper",
	description = "Streamlines banking logs by swapping menu options on logs and your log basket",
	tags = {"forestry", "woodcutting", "logs", "basket", "banking"}
)
public class ForestryBankingPlugin extends Plugin
{
	private static final int LOG_BASKET_ITEM_ID   = 28113; // Log basket (in inventory)
	private static final int FORESTRY_KIT_ITEM_ID = 28908; // Forestry kit (worn)

	private static final Set<Integer> LOG_ITEM_IDS = ImmutableSet.of(
		1511,  // Logs
		1521,  // Oak logs
		1519,  // Willow logs
		6333,  // Teak logs
		1517,  // Maple logs
		6332,  // Mahogany logs
		1515,  // Yew logs
		1513,  // Magic logs
		19669  // Redwood logs
	);

	@Inject
	private Client client;

	@Inject
	private ForestryBankingConfig config;

	private boolean bankOpen = false;

	@Override
	protected void startUp()
	{
		log.debug("Forestry Banking Helper started");
	}

	@Override
	protected void shutDown()
	{
		bankOpen = false;
		log.debug("Forestry Banking Helper stopped");
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN)
		{
			bankOpen = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (bankOpen && client.getWidget(InterfaceID.Bankside.ITEMS) == null)
		{
			bankOpen = false;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!bankOpen)
		{
			return;
		}

		int itemId = event.getIdentifier();

		// ---- Logs in inventory -----------------------------------------------
		if (LOG_ITEM_IDS.contains(itemId))
		{
			if (config.logsLeftClick())
			{
				swapToLeftClick("Deposit-All");
			}
			else if (config.logsRightClick())
			{
				promoteToTopRightClick("Deposit-All");
			}
		}

		// ---- Log basket in inventory -----------------------------------------
		if (itemId == LOG_BASKET_ITEM_ID)
		{
			if (config.basketLeftClick())
			{
				swapToLeftClick("Empty");
			}
			else if (config.basketRightClick())
			{
				promoteToTopRightClick("Empty");
			}
		}

		// ---- Forestry kit worn -----------------------------------------------
		if (itemId == FORESTRY_KIT_ITEM_ID)
		{
			if (config.basketLeftClick())
			{
				swapToLeftClick("Empty log basket");
			}
			else if (config.basketRightClick())
			{
				promoteToTopRightClick("Empty log basket");
			}
		}
	}

	// -------------------------------------------------------------------------
	// Menu helpers
	// -------------------------------------------------------------------------

	/**
	 * Moves the entry whose option matches {@code option} to the last position
	 * in the array, making it the left-click action.
	 */
	private void swapToLeftClick(String option)
	{
		MenuEntry[] entries = client.getMenuEntries();
		int last = entries.length - 1;

		for (int i = last; i >= 0; i--)
		{
			if (entries[i].getOption().equalsIgnoreCase(option))
			{
				if (i == last)
				{
					return; // already left-click
				}
				MenuEntry temp  = entries[last];
				entries[last]   = entries[i];
				entries[i]      = temp;
				client.setMenuEntries(entries);
				return;
			}
		}
	}

	/**
	 * Moves the entry whose option matches {@code option} to the
	 * second-to-last position, making it the first (top) right-click option.
	 */
	private void promoteToTopRightClick(String option)
	{
		MenuEntry[] entries = client.getMenuEntries();
		int count = entries.length;

		if (count < 2)
		{
			return;
		}

		int target = count - 2; // just below left-click

		for (int i = count - 1; i >= 0; i--)
		{
			if (entries[i].getOption().equalsIgnoreCase(option))
			{
				if (i == target)
				{
					return; // already top right-click
				}
				MenuEntry entry = entries[i];

				if (i < target)
				{
					System.arraycopy(entries, i + 1, entries, i, target - i);
				}
				else
				{
					System.arraycopy(entries, target, entries, target + 1, i - target);
				}

				entries[target] = entry;
				client.setMenuEntries(entries);
				return;
			}
		}
	}

	@Provides
	ForestryBankingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ForestryBankingConfig.class);
	}
}
