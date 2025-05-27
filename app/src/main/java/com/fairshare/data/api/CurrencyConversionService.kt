package com.fairshare.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

interface CurrencyConversionService {
    suspend fun getExchangeRate(fromCurrency: String, toCurrency: String): Double
    suspend fun convertAmount(amount: Double, fromCurrency: String, toCurrency: String): Double
    fun clearCache()
}

class ExchangeRateApiService : CurrencyConversionService {
    private val baseUrl = "https://open.er-api.com/v6/latest"
    private val cacheTimeout = TimeUnit.HOURS.toMillis(1) // Cache for 1 hour
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private data class CacheEntry(
        val rates: Map<String, Double>,
        val timestamp: Long
    )
    
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    
    override suspend fun getExchangeRate(fromCurrency: String, toCurrency: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                if (fromCurrency == toCurrency) return@withContext 1.0
                
                val rates = getRatesForCurrency(fromCurrency)
                rates[toCurrency] ?: throw IllegalArgumentException("Exchange rate not found for $toCurrency")
            } catch (e: Exception) {
                throw CurrencyConversionException("Failed to get exchange rate", e)
            }
        }
    }
    
    override suspend fun convertAmount(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val rate = getExchangeRate(fromCurrency, toCurrency)
        return amount * rate
    }
    
    override fun clearCache() {
        cache.clear()
    }
    
    private suspend fun getRatesForCurrency(currency: String): Map<String, Double> {
        val cachedRates = cache[currency]
        if (cachedRates != null && !isCacheExpired(cachedRates.timestamp)) {
            return cachedRates.rates
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/$currency")
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw CurrencyConversionException("API call failed with status ${response.code}")
                    }
                    
                    val jsonResponse = JSONObject(response.body!!.string())
                    if (!jsonResponse.getBoolean("result")) {
                        throw CurrencyConversionException(jsonResponse.getString("error-type"))
                    }
                    
                    val rates = jsonResponse.getJSONObject("rates")
                    val ratesMap = mutableMapOf<String, Double>()
                    rates.keys().forEach { key ->
                        ratesMap[key] = rates.getDouble(key)
                    }
                    
                    cache[currency] = CacheEntry(ratesMap, System.currentTimeMillis())
                    ratesMap
                }
            } catch (e: Exception) {
                throw CurrencyConversionException("Failed to fetch exchange rates", e)
            }
        }
    }
    
    private fun isCacheExpired(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp > cacheTimeout
    }
}

class CurrencyConversionException(message: String, cause: Throwable? = null) : Exception(message, cause) 