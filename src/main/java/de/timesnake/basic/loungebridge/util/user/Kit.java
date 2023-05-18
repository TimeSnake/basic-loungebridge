/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.database.util.game.DbKit;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class Kit extends de.timesnake.basic.game.util.game.Kit {

  private final List<Consumer<GameUser>> applier;

  public Kit(Builder builder) {
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

  public static class Builder extends de.timesnake.basic.game.util.game.Kit.Builder {

    private final List<Consumer<GameUser>> applier = new LinkedList<>();

    @Override
    public Builder id(int id) {
      return (Builder) super.id(id);
    }

    @Override
    public Builder name(String name) {
      return (Builder) super.name(name);
    }

    @Override
    public Builder addDescription(String... lines) {
      return (Builder) super.addDescription(lines);
    }

    @Override
    public Builder material(Material material) {
      return (Builder) super.material(material);
    }

    public Builder addApplier(Consumer<GameUser> applier) {
      this.applier.add(applier);
      return this;
    }

    public Builder addItems(ItemStack... items) {
      this.addApplier(u -> {
        for (ItemStack item : items) {
          if (item instanceof ExItemStack exItem) {
            u.setItem(exItem.cloneWithId());
          } else {
            u.addItem(item);
          }
        }
      });
      return this;
    }

    public Builder addEffect(PotionEffectType effectType, int amplifier) {
      this.addApplier(u -> u.addPotionEffect(effectType, amplifier));
      return this;
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
