package net.p3pp3rf1y.sophisticatedcore.util;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class TransactionCallback {
	private TransactionCallback() {
	}

	public static void onSuccess(TransactionContext transaction, Runnable callback) {
		transaction.addOuterCloseCallback(result -> {
			if (result.wasCommitted()) {
				callback.run();
			}
		});
	}

	public static void onFail(TransactionContext transaction, Runnable callback) {
		transaction.addOuterCloseCallback(result -> {
			if (result.wasAborted()) {
				callback.run();
			}
		});
	}
}
