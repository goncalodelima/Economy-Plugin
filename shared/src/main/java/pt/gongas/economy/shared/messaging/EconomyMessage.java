/*
 *
 *  * This file is part of Economy-Plugin - https://github.com/goncalodelima/Economy-Plugin
 *  * Copyright (c) 2026 goncalodelima and contributors
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

// Records are not supported by this Redis codec because the library does not depend on the shared project.
// Only classes registered in the codec can be deserialized without a no-arg constructor or @JsonCreator.
public class EconomyMessage {

    @NotNull
    private final String target;

    @NotNull
    private final String message;

    @JsonCreator
    public EconomyMessage(
            @JsonProperty("target") @NotNull String target,
            @JsonProperty("message") @NotNull String message
    ) {
        this.target = target;
        this.message = message;
    }

    public @NotNull String getTarget() {
        return target;
    }

    public @NotNull String getMessage() {
        return message;
    }

}