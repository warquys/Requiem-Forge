/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.pandemonium.common.impl.anchor;

import com.mojang.datafixers.util.Function5;
import com.mojang.serialization.DataResult;
import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.api.anchor.FractureAnchorFactory;
import ladysnake.pandemonium.api.anchor.GlobalEntityPos;
import ladysnake.pandemonium.api.anchor.GlobalEntityTracker;
import ladysnake.requiem.core.util.DataResults;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

import java.util.UUID;
import java.util.function.Function;

public final class AnchorFactories {
    public static FractureAnchorFactory fromEntity(Entity entity, boolean synced) {
        return (manager, id) -> new EntityFractureAnchor(manager, entity.getUuid(), id, new GlobalEntityPos(entity), synced);
    }

    public static DataResult<FractureAnchorFactory> fromTag(NbtCompound anchorTag) {
        // Point-free style at its finest
        return GlobalEntityPos.CODEC.parse(NbtOps.INSTANCE, anchorTag.getCompound("pos")).<UUID, Boolean, Function<Function5<GlobalEntityTracker, UUID, Integer, GlobalEntityPos, Boolean, FractureAnchor>, FractureAnchorFactory>>apply3(
            (pos, uuid, synced) -> factory -> (manager, networkId) -> factory.apply(manager, uuid, networkId, pos, synced),
            DataResults.tryGet(() -> anchorTag.getUuid(InertFractureAnchor.ANCHOR_UUID_NBT)),
            DataResult.success(anchorTag.getBoolean(InertFractureAnchor.SYNCED_NBT))
        ).flatMap(preprocessor -> switch (anchorTag.getString(InertFractureAnchor.ANCHOR_TYPE_NBT)) {
            case InertFractureAnchor.ENTITY_TYPE_ID -> DataResult.success(preprocessor.apply(EntityFractureAnchor::new));
            case InertFractureAnchor.INERT_TYPE_ID -> DataResult.success(preprocessor.apply(InertFractureAnchor::new));
            default -> DataResult.error("Unknown anchor type");
        });
    }
}
