package com.eggheadgames.inapppayments

import com.billing.BillingService
import com.billing.splitMessages
import org.junit.Assert
import org.junit.Test

class AmazonSkuLimitTest {

    @Test
    fun checkSkuListSize_ShouldSplitIfMoreThen100() {
        val iapKeys = mutableListOf<String>()
        for (i in 0 until 230) {
            iapKeys.add("sku_$i")
        }

        val splitMessages = iapKeys.splitMessages(BillingService.MAX_SKU_LIMIT)

        Assert.assertEquals(3, splitMessages.size)

        Assert.assertEquals(100, splitMessages[0].size)
        Assert.assertEquals(100, splitMessages[1].size)
        Assert.assertEquals(30, splitMessages[2].size)
    }

    @Test
    fun checkSkuListSize_ShouldReturnSizeIfLessThen100() {
        val iapKeys = mutableListOf<String>()
        for (i in 0 until 40) {
            iapKeys.add("sku_$i")
        }

        val splitMessages = iapKeys.splitMessages(BillingService.MAX_SKU_LIMIT)

        Assert.assertEquals(1, splitMessages.size)

        Assert.assertEquals(40, splitMessages[0].size)
    }


    @Test
    fun checkSkuListSize_ShouldReturn0() {
        val iapKeys = mutableListOf<String>()

        val splitMessages = iapKeys.splitMessages(BillingService.MAX_SKU_LIMIT)

        Assert.assertEquals(0, splitMessages.size)
    }
}