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

package pt.gongas.economy.shared.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class Formatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    private final List<String> suffixes;

    public Formatter() {
        suffixes = Arrays.asList(
                "", "K", "M", "B", "T", "Q", "QQ", "S", "SS", "OC",
                "N", "D", "UN", "DD", "TR", "QT", "QN", "SD", "SPD",
                "OD", "ND", "VG", "UVG", "DVG", "TVG", "QTV", "QNV", "SEV",
                "SPV", "OVG", "NVG", "TG"
        );
    }

    public String formatNumber(double value) {

        if (value < 0.01 && value > -0.01) {
            return "0";
        }

        boolean negative = value < 0;
        value = Math.abs(value);

        int index = 0;

        if (value >= 1000) {
            index = (int) (Math.log10(value) / Math.log10(1000));
            if (index >= suffixes.size()) index = suffixes.size() - 1;
            value /= Math.pow(1000, index);
        }

        return (negative ? "-" : "") + DECIMAL_FORMAT.format(value) + suffixes.get(index);
    }

    public double parseFormattedNumber(String formattedNumber) {

        formattedNumber = formattedNumber.toUpperCase();
        boolean negative = formattedNumber.startsWith("-");

        if (negative) {
            formattedNumber = formattedNumber.substring(1);
        }

        int suffixIndex = -1;

        for (int i = suffixes.size() - 1; i > 0; i--) {

            String suffix = suffixes.get(i);

            if (formattedNumber.endsWith(suffix)) {
                suffixIndex = i;
                formattedNumber = formattedNumber.replace(suffix, "");
                break;
            }

        }

        double value;

        try {

            value = Double.parseDouble(formattedNumber);

            if (Double.isInfinite(value) || Double.isNaN(value)) {
                return -1;
            }

            if (value < 0.01 && value > -0.01) {
                return 0;
            }

        } catch (NumberFormatException e) {
            return -1;
        }

        if (suffixIndex != -1) {
            value *= Math.pow(1000, suffixIndex);
        }

        return negative ? -value : value;
    }

}
