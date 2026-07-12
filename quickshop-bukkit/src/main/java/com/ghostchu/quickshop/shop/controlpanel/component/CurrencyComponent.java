package com.ghostchu.quickshop.shop.controlpanel.component;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.shop.ControlComponent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CurrencyComponent implements ControlComponent {

  @Override
  public String identifier() {
    return "currency";
  }

  @Override
  public boolean applies(final @NotNull QuickShopAPI plugin, final @NotNull Player sender, final @NotNull Shop shop) {
    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_CURRENCY)
       && !((QuickShop)plugin).perm().hasPermission(sender, "quickshop.other.currency")) {
      return false;
    }
    for(final EconomyProvider provider : plugin.getEconomyManager().providers().values()) {
      if(provider != plugin.getEconomyManager().provider() && provider.valid()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Component generate(final @NotNull QuickShopAPI plugin, final @NotNull Player sender, final @NotNull Shop shop) {
    final String current = shop.getCurrency();
    final String displayName = (current != null)? current : "";
    final Component text = ((QuickShop)plugin).text().of(sender, "controlpanel.currency", displayName).forLocale();
    final Component hoverText = ((QuickShop)plugin).text().of(sender, "controlpanel.currency-hover").forLocale();
    final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", ((QuickShop)plugin).getMainCommand(), ((QuickShop)plugin).getCommandPrefix("silentcurrency"), shop.getRuntimeRandomUniqueId().toString());
    return text.hoverEvent(HoverEvent.showText(hoverText))
            .clickEvent(ClickEvent.runCommand(clickCommand));
  }
}
