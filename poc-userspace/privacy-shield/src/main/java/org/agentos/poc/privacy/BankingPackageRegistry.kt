package org.agentos.poc.privacy

/**
 * Registry of known financial, banking, cryptocurrency, and password manager apps.
 * In Phase 1 & 2, any context collection is immediately suspended when these packages are in the foreground.
 */
object BankingPackageRegistry {

    private val THAI_BANKING_PACKAGES = setOf(
        "com.kasikorn.retail.mbanking.wap",   // K PLUS
        "com.scb.phone",                      // SCB EASY
        "ktbcs.netbank",                      // Krungthai NEXT
        "com.bbl.mobilephone",                // Bangkok Bank Mobile Banking
        "com.krungsri.kma",                   // KMA (Krungsri)
        "com.ttbbank.oneapp",                 // ttb touch
        "com.uob.mightyth",                   // UOB TMRW Thailand
        "th.co.cimbthai.moct",                // CIMB THAI Digital Banking
        "th.co.lhbank.mchoice",               // LHB You
        "com.baac.baacbaac",                  // BAAC Mobile
        "com.gsb.mymo",                       // MyMo by GSB
        "th.co.truemoney.wallet"              // TrueMoney Wallet
    )

    private val CRYPTO_WALLETS = setOf(
        "io.metamask",                        // MetaMask
        "com.wallet.crypto.trustapp",         // Trust Wallet
        "com.binance.dev",                    // Binance
        "com.bitkub.app",                     // Bitkub
        "co.coinbase.android",                // Coinbase
        "com.okex.mobile"                     // OKX
    )

    private val PASSWORD_MANAGERS = setOf(
        "com.x8bit.bitwarden",                // Bitwarden
        "com.onepassword.android",            // 1Password
        "com.lastpass.lpandroid",             // LastPass
        "com.dashlane",                       // Dashlane
        "org.keepassdroid",                   // KeePassDroid
        "keepass2android.keepass2android"     // KeePass2Android
    )

    private val CUSTOM_USER_BLACKLIST = mutableSetOf<String>()

    /**
     * Checks if a package is strictly blacklisted from any AI observation.
     */
    fun isPackageBlacklisted(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return true
        return THAI_BANKING_PACKAGES.contains(packageName) ||
                CRYPTO_WALLETS.contains(packageName) ||
                PASSWORD_MANAGERS.contains(packageName) ||
                CUSTOM_USER_BLACKLIST.contains(packageName)
    }

    /**
     * Add a package to user-defined blacklist.
     */
    fun addCustomBlacklist(packageName: String) {
        CUSTOM_USER_BLACKLIST.add(packageName)
    }

    /**
     * Remove a package from user-defined blacklist.
     */
    fun removeCustomBlacklist(packageName: String) {
        CUSTOM_USER_BLACKLIST.remove(packageName)
    }

    fun getBlacklist(): Set<String> {
        return THAI_BANKING_PACKAGES + CRYPTO_WALLETS + PASSWORD_MANAGERS + CUSTOM_USER_BLACKLIST
    }
}

