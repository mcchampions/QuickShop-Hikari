package com.ghostchu.quickshop.economy.provider;
/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import me.qscbm.plugins.slimecoins.api.EconomyResult;
import me.qscbm.plugins.slimecoins.api.SlimeCoinsAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.logging.Level;

public class SlimeCoinsProvider implements EconomyProvider, Listener {

  private final QuickShop plugin;
  private String lastError = "No transaction error logged";

  public SlimeCoinsProvider(@NotNull final QuickShop plugin) {
    this.plugin = plugin;
    Bukkit.getPluginManager().registerEvents(this, plugin.getJavaPlugin());
    Log.debug("SlimeCoins economy provider registered.");
  }

  @Override
  public @NotNull String name() {
    return "BuiltIn-SlimeCoins";
  }

  @Override
  public String providerName() {
    return "SlimeCoins";
  }

  @Override
  public @NotNull String lastError() {
    return lastError;
  }

  @Override
  public boolean valid() {
    return CommonUtil.isClassAvailable("me.qscbm.plugins.slimecoins.api.SlimeCoinsAPI")
           && SlimeCoinsAPI.getInstance() != null;
  }

  @Override
  public boolean multiCurrency() {
    return false;
  }

  @Override
  public boolean supportsCurrency(final @NotNull String world, final @Nullable String currency) {
    return false;
  }

  @Override
  public @NotNull String format(final @NotNull BigDecimal amount, final @NotNull String world, final @Nullable String currency) {
    if (!valid()) {
      return amount.toPlainString();
    }
    final SlimeCoinsAPI api = SlimeCoinsAPI.getInstance();
    final String name = amount.compareTo(BigDecimal.ONE) == 0
            ? api.getCurrencyNameSingular()
            : api.getCurrencyNamePlural();
    final DecimalFormat df = new DecimalFormat("#,###.##");
    return df.format(amount) + " " + name;
  }

  @Override
  public @NotNull BigDecimal balance(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency) {
    if (!valid() || user.getUniqueId() == null) {
      return BigDecimal.ZERO;
    }
    try {
      return SlimeCoinsAPI.getInstance().getBalance(user.getUniqueId());
    } catch (final Exception e) {
      if (QuickShop.getInstance().getSentryErrorReporter() != null) {
        QuickShop.getInstance().getSentryErrorReporter().ignoreThrow();
      }
      QuickShop.getInstance().logger().warn("Failure - getBalance - " + user + " - " + world);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), e);
    }
    return BigDecimal.ZERO;
  }

  @Override
  public boolean deposit(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency, final @NotNull BigDecimal amount) {
    if (!valid() || user.getUniqueId() == null) {
      return false;
    }
    try {
      final EconomyResult result = SlimeCoinsAPI.getInstance().deposit(user.getUniqueId(), amount, "QuickShop-Hikari");
      if (result.isSuccess()) {
        return true;
      }
      this.lastError = providerName() + ": " + result.getMessage();
      Log.transaction(Level.WARNING, "Deposit player " + user.getUniqueId() + " failed, SlimeCoins response: " + result.getMessage());
    } catch (final Exception e) {
      QuickShop.getInstance().logger().warn("Failure - deposit - " + user + " - " + amount + " - " + world);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), e);
    }
    return false;
  }

  @Override
  public boolean withdraw(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency, final @NotNull BigDecimal amount) {
    if (!valid() || user.getUniqueId() == null) {
      return false;
    }
    try {
      final EconomyResult result = SlimeCoinsAPI.getInstance().withdraw(user.getUniqueId(), amount, "QuickShop-Hikari");
      if (result.isSuccess()) {
        return true;
      }
      this.lastError = providerName() + ": " + result.getMessage();
      Log.transaction(Level.WARNING, "Withdraw player " + user.getUniqueId() + " failed, SlimeCoins response: " + result.getMessage());
    } catch (final Exception e) {
      QuickShop.getInstance().logger().warn("Failure - withdraw - " + user + " - " + amount + " - " + world);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), e);
    }
    return false;
  }

  @EventHandler
  public void onPluginEnable(final PluginEnableEvent event) {
    if (!event.getPlugin().getName().equals("SlimeCoins")) {
      return;
    }
    Log.debug("SlimeCoins plugin enabled, provider ready.");
  }

  @EventHandler
  public void onPluginDisable(final PluginDisableEvent event) {
    if (!event.getPlugin().getName().equals("SlimeCoins")) {
      return;
    }
    Log.debug("SlimeCoins plugin disabled, provider invalidated.");
  }
}
