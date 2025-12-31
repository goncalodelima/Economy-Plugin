/*
 *
 *  * This file is part of Economy-Plugin - https://github.com/goncalodelima/Economy-Plugin
 *  * Copyright (c) 2025 goncalodelima and contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package pt.gongas.economy.shared.messaging;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TransactionMessage {

    @Nullable
    private UUID sender;

    @Nullable
    private UUID target;

    public TransactionMessage() {}

    public TransactionMessage(@Nullable UUID sender, @Nullable UUID target) {
        this.sender = sender;
        this.target = target;
    }

    public @Nullable UUID getSender() {
        return sender;
    }

    public @Nullable UUID getTarget() {
        return target;
    }

}
