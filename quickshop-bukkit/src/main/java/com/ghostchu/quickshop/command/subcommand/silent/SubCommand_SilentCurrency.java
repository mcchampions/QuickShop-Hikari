package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import com.ghostchu.quickshop.api.economy.EconomyProvider;

public class SubCommand_SilentCurrency extends SubCommand_SilentBase {

  public SubCommand_SilentCurrency(final QuickShop plugin) {
    super(plugin);
  }

  @Override
  protected void doSilentCommand(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {
    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_CURRENCY)
       && !plugin.perm().hasPermission(sender, "quickshop.other.currency")) {
      plugin.text().of(sender, "not-permission").send();
      return;
    }

    final String current = shop.getCurrency();
    final List<String> options = new ArrayList<>();
    for(final EconomyProvider p : plugin.getEconomyManager().providers().values()) {
      if(!p.name().equalsIgnoreCase(plugin.getEconomyManager().provider().name())) {
        options.add(p.providerName());
      }
    }

    if(options.isEmpty()) {
      return;
    }

    int index = -1;
    if(current != null) {
      index = options.indexOf(current);
    }
    final String next = (index + 1 < options.size())? options.get(index + 1) : null;

    if(next == null) {
      shop.setCurrency(null);
      plugin.text().of(sender, "currency-unset").send();
    } else {
      shop.setCurrency(next);
      plugin.text().of(sender, "currency-set", next).send();
    }

    MsgUtil.sendControlPanelInfo(sender, shop);
    shop.setSignText(plugin.text().findRelativeLanguages(sender));
  }
}
