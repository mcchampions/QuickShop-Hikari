package com.ghostchu.quickshop.economy;
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

import com.ghostchu.quickshop.api.economy.EconomyManager;
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * QSEconomyManager
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class QSEconomyManager implements EconomyManager {

  final Map<String, EconomyProvider> providers = new HashMap<>();
  final Map<String, String> currencyProviderMap = new HashMap<>();

  String currentProvider = "VAULT";

  /**
   * Retrieves a map of all economies associated with their unique identifier.
   *
   * @return A map containing integers as keys and EconomyProvider objects as values.
   */
  @Override
  public Map<String, EconomyProvider> providers() {

    return providers;
  }

  /**
   * Set the EconomyProvider for this manager.
   *
   * @param provider The EconomyProvider to be set for this manager.
   */
  @Override
  public void provider(final @NotNull EconomyProvider provider) {

    providers.put(provider.name().toUpperCase(Locale.ROOT), provider);
    currencyProviderMap.put(provider.providerName().toUpperCase(Locale.ROOT), provider.name());
  }

  /**
   * Retrieves an Optional instance of EconomyProvider associated with the given id.
   *
   * @param id The unique identifier of the EconomyProvider to retrieve.
   *
   * @return An Optional instance containing the EconomyProvider if found, or an empty Optional if
   * not found.
   */
  @Override
  public Optional<EconomyProvider> provider(final @NotNull String id) {

    return Optional.ofNullable(providers.get(id.toUpperCase(Locale.ROOT)));
  }

  /**
   * Uses the provided EconomyProvider associated with the specified provider ID.
   *
   * @param id The unique identifier of the EconomyProvider to be used.
   */
  @Override
  public void useProvider(final @NotNull String id) {

    this.currentProvider = id;
  }

  /**
   * Retrieves the current EconomyProvider.
   *
   * @return The current EconomyProvider, or null if not set.
   */
  @Override
  public @Nullable EconomyProvider provider() {

    return providers.get(currentProvider.toUpperCase(Locale.ROOT));
  }

  /**
   * Retrieves the EconomyProvider that should handle the given currency.
   * If the currency matches a registered provider name, that provider is returned.
   * Otherwise falls back to the default provider.
   *
   * @param currency the currency name, or null for default provider
   * @return The EconomyProvider for the given currency, or the default provider
   */
  @Override
  public @Nullable EconomyProvider providerForCurrency(@Nullable final String currency) {

    if (currency == null || currency.isEmpty()) {
      return provider();
    }
    final String providerName = currencyProviderMap.get(currency.toUpperCase(Locale.ROOT));
    if (providerName != null) {
      final EconomyProvider provider = providers.get(providerName.toUpperCase(Locale.ROOT));
      if (provider != null && provider.valid()) {
        return provider;
      }
    }
    return provider();
  }
}