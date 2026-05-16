package com.forestrybanking;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Forestry Banking Helper",
	description = "Auto-deposits logs from your inventory and log basket when banking",
	tags = {"forestry", "woodcutting", "logs", "basket", "banking"}
)
public class ForestryBankingPlugin extends Plugin
{
	// -----------------------------------------------------------------------
	// TODO: Verify these item IDs in-game.
	// Hover over the item with the "Item ID" RuneLite plugin enabled,
	// or look them up on the OSRS Wiki.
	// -----------------------------------------------------------------------
	private static final int LOG_BASKET_ITEM_ID   = 28113; // Log basket (in inventory)
	private static final int FORESTRY_KIT_ITEM_ID = 28908; // Forestry kit (worn)

	// All standard log types. Add any missing IDs here.
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

	// -----------------------------------------------------------------------
	// TODO: If depositing or emptying does nothing, these op codes may need
	// adjusting. Right-click the item in-game and count which position the
	// option appears (1 = first option).
	// -----------------------------------------------------------------------
	private static final int DEPOSIT_ALL_OP      = 8; // "Deposit-All" on a bank inventory item
	private static final int BASKET_EMPTY_OP     = 2; // "Empty" on Log basket in inventory
	private static final int KIT_EMPTY_BASKET_OP = 3; // "Empty log basket" on worn Forestry kit

	private static final String DEPOSIT_ALL_OPTION = "Deposit all logs";

	private enum BankState
	{
		IDLE,
		EMPTY_BASKET,  // send the empty action this tick
		DEPOSIT_LOGS   // deposit all logs in inventory this tick
	}

	@Inject
	private Client client;

	@Inject
	private ForestryBankingConfig config;

	private boolean bankOpen = false;
	private BankState bankState = BankState.IDLE;

	@Override
	protected void startUp()
	{
		log.debug("Forestry Banking Helper started");
	}

	@Override
	protected void shutDown()
	{
		bankOpen = false;
		bankState = BankState.IDLE;
		log.debug("Forestry Banking Helper stopped");
	}

	// Fires once the real bank inventory widget is loaded (i.e. after any PIN entry).
	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != InterfaceID.BANKMAIN)
		{
			return;
		}

		bankOpen = true;

		boolean basketEnabled    = config.autoDepositBasket();
		boolean inventoryEnabled = config.autoDepositInventory();

		if (basketEnabled && findBasketSlot() >= 0)
		{
			bankState = BankState.EMPTY_BASKET;
		}
		else if (inventoryEnabled && hasLogsInInventory())
		{
			bankState = BankState.DEPOSIT_LOGS;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Detect bank closed by checking whether the widget is still present.
		if (bankOpen && client.getWidget(InterfaceID.Bankside.ITEMS) == null)
		{
			bankOpen = false;
			bankState = BankState.IDLE;
			return;
		}

		if (!bankOpen)
		{
			return;
		}

		switch (bankState)
		{
			case EMPTY_BASKET:
				emptyBasket();
				// Wait one tick for logs to land in inventory before depositing.
				bankState = BankState.DEPOSIT_LOGS;
				break;

			case DEPOSIT_LOGS:
				depositAllLogs();
				bankState = BankState.IDLE;
				break;

			default:
				break;
		}
	}

	// Adds a "Deposit all logs" right-click option on logs in the bank inventory panel.
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!bankOpen || !config.clickToDepositAll())
		{
			return;
		}

		if (event.getActionParam1() != InterfaceID.Bankside.ITEMS)
		{
			return;
		}

		if (!LOG_ITEM_IDS.contains(event.getIdentifier()))
		{
			return;
		}

		client.createMenuEntry(-1)
			.setOption(DEPOSIT_ALL_OPTION)
			.setTarget(event.getTarget())
			.setType(MenuAction.RUNELITE)
			.setParam0(event.getActionParam0())
			.setParam1(event.getActionParam1())
			.setIdentifier(event.getIdentifier());
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() != MenuAction.RUNELITE
			|| !event.getMenuOption().equals(DEPOSIT_ALL_OPTION))
		{
			return;
		}

		event.consume();

		if (config.autoDepositBasket() && findBasketSlot() >= 0)
		{
			bankState = BankState.EMPTY_BASKET;
		}
		else
		{
			depositAllLogs();
		}
	}

	// -----------------------------------------------------------------------
	// Actions
	// -----------------------------------------------------------------------

	private void emptyBasket()
	{
		int inventorySlot = findBasketInInventory();
		if (inventorySlot >= 0)
		{
			log.debug("Emptying log basket from inventory slot {}", inventorySlot);
			client.menuAction(
				inventorySlot, InterfaceID.Bankside.ITEMS,
				MenuAction.CC_OP, BASKET_EMPTY_OP, LOG_BASKET_ITEM_ID,
				"Empty", "<col=ff9040>Log basket</col>"
			);
			return;
		}

		int equipSlot = findBasketInEquipment();
		if (equipSlot >= 0)
		{
			log.debug("Emptying log basket from worn Forestry kit (equip slot {})", equipSlot);
			client.menuAction(
				equipSlot, InterfaceID.Equipment.CONTENTS,
				MenuAction.CC_OP, KIT_EMPTY_BASKET_OP, FORESTRY_KIT_ITEM_ID,
				"Empty log basket", "<col=ff9040>Forestry kit</col>"
			);
		}
	}

	private void depositAllLogs()
	{
		Widget bankInventory = client.getWidget(InterfaceID.Bankside.ITEMS);
		if (bankInventory == null)
		{
			return;
		}

		Widget[] items = bankInventory.getDynamicChildren();
		if (items == null)
		{
			return;
		}

		for (Widget item : items)
		{
			if (LOG_ITEM_IDS.contains(item.getItemId()))
			{
				log.debug("Depositing log item {} at slot {}", item.getItemId(), item.getIndex());
				client.menuAction(
					item.getIndex(), InterfaceID.Bankside.ITEMS,
					MenuAction.CC_OP, DEPOSIT_ALL_OP, item.getItemId(),
					"Deposit-All", ""
				);
			}
		}
	}

	// -----------------------------------------------------------------------
	// Helpers
	// -----------------------------------------------------------------------

	/** Returns the first slot index where a basket is found, or -1. */
	private int findBasketSlot()
	{
		int slot = findBasketInInventory();
		return slot >= 0 ? slot : findBasketInEquipment();
	}

	private int findBasketInInventory()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			return -1;
		}

		Item[] items = inventory.getItems();
		for (int i = 0; i < items.length; i++)
		{
			if (items[i].getId() == LOG_BASKET_ITEM_ID)
			{
				return i;
			}
		}
		return -1;
	}

	private int findBasketInEquipment()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			return -1;
		}

		Item[] items = equipment.getItems();
		for (int i = 0; i < items.length; i++)
		{
			if (items[i].getId() == FORESTRY_KIT_ITEM_ID)
			{
				return i;
			}
		}
		return -1;
	}

	private boolean hasLogsInInventory()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			return false;
		}

		for (Item item : inventory.getItems())
		{
			if (LOG_ITEM_IDS.contains(item.getId()))
			{
				return true;
			}
		}
		return false;
	}

	@Provides
	ForestryBankingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ForestryBankingConfig.class);
	}
}
