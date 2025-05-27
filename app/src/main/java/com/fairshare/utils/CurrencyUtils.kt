package com.fairshare.utils

import com.fairshare.data.api.CurrencyConversionService
import com.fairshare.data.api.ExchangeRateApiService
import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    // Default currency is PHP (Philippine Peso)
    private const val DEFAULT_CURRENCY_CODE = "PHP"
    private val conversionService: CurrencyConversionService = ExchangeRateApiService()
    
    // Common currency codes
    object CurrencyCodes {
        const val PHP = "PHP"
        const val USD = "USD"
        const val EUR = "EUR"
        const val GBP = "GBP"
        const val JPY = "JPY"
        const val AUD = "AUD"
        const val SGD = "SGD"
        const val CAD = "CAD"
        const val HKD = "HKD"
        const val CNY = "CNY"
        const val CHF = "CHF"
        const val INR = "INR"
    }

    // Currency symbols for display
    private val currencySymbols = mapOf(
        CurrencyCodes.PHP to "₱",
        CurrencyCodes.USD to "$",
        CurrencyCodes.EUR to "€",
        CurrencyCodes.GBP to "£",
        CurrencyCodes.JPY to "¥",
        CurrencyCodes.AUD to "A$",
        CurrencyCodes.SGD to "S$",
        CurrencyCodes.CAD to "C$",
        CurrencyCodes.HKD to "HK$",
        CurrencyCodes.CNY to "¥",
        CurrencyCodes.CHF to "Fr",
        CurrencyCodes.INR to "₹"
    )

    private val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("USD") // Default to USD
    }

    // Get currency formatter for a specific currency
    fun getFormatter(currencyCode: String = DEFAULT_CURRENCY_CODE): NumberFormat {
        val locale = when (currencyCode) {
            CurrencyCodes.PHP -> Locale("fil", "PH")
            CurrencyCodes.USD -> Locale.US
            CurrencyCodes.EUR -> Locale.GERMANY
            CurrencyCodes.GBP -> Locale.UK
            CurrencyCodes.JPY -> Locale.JAPAN
            CurrencyCodes.AUD -> Locale("en", "AU")
            CurrencyCodes.SGD -> Locale("en", "SG")
            CurrencyCodes.CAD -> Locale("en", "CA")
            CurrencyCodes.HKD -> Locale("en", "HK")
            CurrencyCodes.CNY -> Locale.CHINA
            CurrencyCodes.CHF -> Locale("de", "CH")
            CurrencyCodes.INR -> Locale("en", "IN")
            else -> Locale.getDefault()
        }
        
        return NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(currencyCode)
        }
    }

    // Format amount with currency symbol
    fun formatAmount(amount: Double, currencyCode: String = DEFAULT_CURRENCY_CODE): String {
        return getFormatter(currencyCode).format(amount)
    }

    // Format amount without currency symbol
    fun formatAmountWithoutSymbol(amount: Double): String {
        return String.format("%.2f", amount)
    }

    // Get currency symbol
    fun getCurrencySymbol(currencyCode: String = DEFAULT_CURRENCY_CODE): String {
        return currencySymbols[currencyCode] ?: currencyCode
    }

    // List of available currencies for selection
    fun getAvailableCurrencies(): List<Currency> {
        return Currency.getAvailableCurrencies().sortedBy { it.currencyCode }
    }

    // Get display name for currency
    fun getCurrencyDisplayName(currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            "${currency.displayName} (${getCurrencySymbol(currencyCode)})"
        } catch (e: IllegalArgumentException) {
            currencyCode
        }
    }

    // Parse amount string to Double
    fun parseAmount(amount: String): Double {
        return amount.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
    }

    // Convert amount between currencies
    suspend fun convertAmount(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Double {
        return conversionService.convertAmount(amount, fromCurrency, toCurrency)
    }

    // Get exchange rate between currencies
    suspend fun getExchangeRate(fromCurrency: String, toCurrency: String): Double {
        return conversionService.getExchangeRate(fromCurrency, toCurrency)
    }

    // Format amount with conversion
    suspend fun formatAmountWithConversion(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): String {
        val convertedAmount = convertAmount(amount, fromCurrency, toCurrency)
        return "${formatAmount(amount, fromCurrency)} (${formatAmount(convertedAmount, toCurrency)})"
    }

    // Clear exchange rate cache
    fun clearExchangeRateCache() {
        conversionService.clearCache()
    }

    fun setCurrency(currencyCode: String) {
        try {
            currencyFormatter.currency = Currency.getInstance(currencyCode)
        } catch (e: IllegalArgumentException) {
            // If invalid currency code, default to USD
            currencyFormatter.currency = Currency.getInstance("USD")
        }
    }

    fun isValidAmount(amount: String): Boolean {
        return try {
            parseAmount(amount)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun formatAmountWithSymbol(amount: Double, currencyCode: String): String {
        val symbol = currencySymbols[currencyCode] ?: currencyCode
        return "$symbol${String.format("%.2f", amount)}"
    }

    fun getAllCurrencies(): List<String> {
        return currencySymbols.keys.toList()
    }

    fun isValidCurrencyCode(currencyCode: String): Boolean {
        return currencySymbols.containsKey(currencyCode)
    }
} 