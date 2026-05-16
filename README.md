# Forestry Banking Helper

A RuneLite plugin that streamlines the tedious process of banking logs during Forestry woodcutting sessions. Instead of manually emptying your log basket and depositing multiple times, this plugin handles the whole sequence in one go.

## The Problem

When banking logs with a Forestry log basket, the normal process is:
1. Deposit logs from inventory
2. Empty log basket into inventory
3. Deposit those logs too

This plugin automates all of that.

## Features

### Auto-Deposit Inventory Logs
Automatically deposits all logs from your inventory the moment you open the bank. No clicking required.

### Auto-Deposit Basket Logs
Automatically empties your log basket and deposits the logs when you open the bank. Works whether your basket is:
- **In your inventory** as a standalone Log basket
- **Worn** as part of your Forestry kit

The plugin handles the one-tick delay between emptying the basket and the logs landing in your inventory.

### Click to Deposit All *(optional)*
Adds a **"Deposit all logs"** right-click option directly on logs in your bank inventory panel. Clicking it empties your basket and deposits everything in one click — useful if you prefer to trigger it manually rather than automatically on bank open.

## Configuration

| Option | Default | Description |
|---|---|---|
| Auto-deposit inventory logs | On | Deposits logs from inventory when bank opens |
| Auto-deposit basket logs | On | Empties basket and deposits logs when bank opens |
| Click logs to deposit all | Off | Adds right-click option on logs for manual trigger |

All three options can be toggled independently — for example you can turn off auto-deposit and rely solely on the right-click option.

## Bank PIN Support

The plugin detects the bank only after the PIN screen is cleared, so it works correctly whether or not you have a bank PIN set.

## Supported Log Types

- Logs
- Oak logs
- Willow logs
- Teak logs
- Maple logs
- Mahogany logs
- Yew logs
- Magic logs
- Redwood logs

## Installation

Install via the RuneLite Plugin Hub — search for **Forestry Banking Helper**.
