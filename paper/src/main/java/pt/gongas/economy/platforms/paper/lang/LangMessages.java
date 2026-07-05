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

package pt.gongas.economy.platforms.paper.lang;

import pt.gongas.economy.platforms.paper.util.config.Configuration;

public final class LangMessages {

    public final String error;
    public final String error1;
    public final String error2;
    public final String databaseNotFound;
    public final String paymentError;
    public final String notEnoughBalance;

    public final String viewOwnBalance;
    public final String viewOtherBalance;
    public final String transactionSuccessful;
    public final String transactionReceivedSuccessful;
    public final String setTransactionSuccessful;
    public final String setTransactionReceivedSuccessful;
    public final String addTransactionSuccessful;
    public final String addTransactionReceivedSuccessful;
    public final String removeTransactionSuccessful;
    public final String removeTransactionReceivedSuccessful;

    public final String payInvalidAmount;
    public final String cannotSendToSelf;
    public final String setInvalidAmount;
    public final String addInvalidAmount;
    public final String removeInvalidAmount;

    private LangMessages(String error,
            String error1,
            String error2,
            String databaseNotFound,
            String paymentError,
            String notEnoughBalance,
            String viewOwnBalance,
            String viewOtherBalance,
            String transactionSuccessful,
            String transactionReceivedSuccessful,
            String setTransactionSuccessful,
            String setTransactionReceivedSuccessful,
            String addTransactionSuccessful,
            String addTransactionReceivedSuccessful,
            String removeTransactionSuccessful,
            String removeTransactionReceivedSuccessful,
            String payInvalidAmount,
            String cannotSendToSelf,
            String setInvalidAmount,
            String addInvalidAmount,
            String removeInvalidAmount
    ) {
        this.error = error;
        this.error1 = error1;
        this.error2 = error2;
        this.databaseNotFound = databaseNotFound;
        this.paymentError = paymentError;
        this.notEnoughBalance = notEnoughBalance;
        this.viewOwnBalance = viewOwnBalance;
        this.viewOtherBalance = viewOtherBalance;
        this.transactionSuccessful = transactionSuccessful;
        this.transactionReceivedSuccessful = transactionReceivedSuccessful;
        this.setTransactionSuccessful = setTransactionSuccessful;
        this.setTransactionReceivedSuccessful = setTransactionReceivedSuccessful;
        this.addTransactionSuccessful = addTransactionSuccessful;
        this.addTransactionReceivedSuccessful = addTransactionReceivedSuccessful;
        this.removeTransactionSuccessful = removeTransactionSuccessful;
        this.removeTransactionReceivedSuccessful = removeTransactionReceivedSuccessful;
        this.payInvalidAmount = payInvalidAmount;
        this.cannotSendToSelf = cannotSendToSelf;
        this.setInvalidAmount = setInvalidAmount;
        this.addInvalidAmount = addInvalidAmount;
        this.removeInvalidAmount = removeInvalidAmount;
    }

    public static LangMessages from(Configuration lang) {
        return new LangMessages(
                lang.getString("error", "<red>Something unexpected happened. Please re-log into the server."),
                lang.getString("error1", "<red>Something unexpected happened. Contact an administrator."),
                lang.getString("error2", "<red>An error occurred in the database while searching for this player's balance. Please contact an administrator."),
                lang.getString("database-not-found", "<red>No player with the entered name was found in the database."),
                lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."),
                lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"),
                lang.getString("view-own-balance", "<green>You have <white><balance><icon></bold><green>."),
                lang.getString("view-other-balance", "<green>The player <white><target> <green>has <white><balance><icon></bold><green>."),
                lang.getString("transaction-successful", "<green>You have successfully executed a transaction of <white><amount><icon></bold> <green>to the player <white><target><green>!"),
                lang.getString("transaction-received-successful", "<green>You have successfully received <white><amount><icon></bold> <green>from the player <white><sender><green>!"),
                lang.getString("set-transaction-successful", "<green>You have successfully set <white><amount><icon></bold> <green>for <white><target><green>."),
                lang.getString("set-transaction-received-successful", "<green>Your balance was set to <white><amount><icon></bold> <green>by <white><sender><green>."),
                lang.getString("add-transaction-successful", "<green>You have successfully added <white><amount><icon></bold> <green>to <white><target><green>."),
                lang.getString("add-transaction-received-successful", "<green>You have successfully received <white><amount><icon></bold> <green>from <white><sender><green>."),
                lang.getString("remove-transaction-successful", "<green>You have successfully removed <white><amount><icon></bold> <green>from <white><target><green>."),
                lang.getString("remove-transaction-received-successful", "<green><white><amount><icon></bold> <green>was successfully removed from your balance by <white><sender><green>."),
                lang.getString("pay-invalid-amount", "<red>The amount must be greater than zero."),
                lang.getString("cannot-send-to-self", "<red>You cannot send money to yourself!"),
                lang.getString("set-invalid-amount", "<red>The value must be greater than or equal to zero."),
                lang.getString("add-invalid-amount", "<red>The amount must be greater than zero."),
                lang.getString("remove-invalid-amount", "<red>The amount must be greater than zero.")
        );
    }
}
