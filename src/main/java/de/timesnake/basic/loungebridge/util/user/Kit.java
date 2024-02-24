/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.database.util.game.DbKit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class Kit extends de.timesnake.basic.game.util.game.Kit {

  private final List<Consumer<GameUser>> applier;

  public Kit(Builder<?> builder) {
    super(builder);
    this.applier = builder.applier;
  }

  public Kit(DbKit dbKit, List<Consumer<GameUser>> applier) {
    super(dbKit);
    this.applier = applier;
  }

  public List<Consumer<GameUser>> getApplier() {
    return applier;
  }

  public static class Builder<B extends Builder<B>> extends de.timesnake.basic.game.util.game.Kit.Builder<B> {

    private final List<Consumer<GameUser>> applier = new LinkedList<>();

    public B addApplier(Consumer<GameUser> applier) {
      this.applier.add(applier);
      return (B) this;
    }

    public B addItems(ItemStack... items) {
      this.addApplier(u -> {
        for (ItemStack item : items) {
          if (item instanceof ExItemStack exItem) {
            u.setItem(exItem.cloneWithId());
          } else {
            u.addItem(item);
          }
        }
      });
      return (B) this;
    }

    public B addEffect(PotionEffectType effectType, int amplifier) {
      this.addApplier(u -> u.addPotionEffect(effectType, amplifier));
      return (B) this;
    }

    @Override
    public void checkBuild() {
      super.checkBuild();
    }

    @Override
    public Kit build() {
      this.checkBuild();
      return new Kit(this);
    }
  }
}
