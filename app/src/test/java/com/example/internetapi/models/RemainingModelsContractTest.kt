package com.example.internetapi.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.Serializable
import java.math.BigDecimal

class RemainingModelsContractTest {

    @Test
    fun budget_usesExpectedDefaultValues() {
        val budget = Budget(
            totalSpend = BigDecimal("120.00"),
            totalPlanned = BigDecimal("300.00"),
            totalEarned = BigDecimal("500.00")
        )

        assertEquals("", budget.date)
        assertTrue(budget.budgets.isEmpty())
    }

    @Test
    fun monthBudget_andUpdateResponses_areSerializable() {
        assertTrue(Serializable::class.java.isAssignableFrom(MonthBudget::class.java))
        assertTrue(Serializable::class.java.isAssignableFrom(UpdateBudgetResponse::class.java))
        assertTrue(Serializable::class.java.isAssignableFrom(UpdateAccountRequest::class.java))
        assertTrue(Serializable::class.java.isAssignableFrom(UpdateAccountResponse::class.java))
    }

    @Test
    fun updateBudgetRequest_copy_replacesOnlyUpdatedField() {
        val request = UpdateBudgetRequest(
            budgetId = 9,
            planned = BigDecimal("100.00")
        )

        val copied = request.copy(planned = BigDecimal("150.00"))

        assertEquals(9, copied.budgetId)
        assertEquals(0, copied.planned.compareTo(BigDecimal("150.00")))
    }

    @Test
    fun media_andTransferModels_keepPassedValues() {
        val mediaTypeRequest = MediaTypeRequest(mediaName = "Water")
        val mediaRegisterRequest = MediaRegisterRequest(
            mediaType = 3,
            meterRead = BigDecimal("321.5"),
            year = 2026,
            month = 2
        )
        val transferRequest = TransferMoneyRequest(
            accountId = 1,
            value = BigDecimal("20.00"),
            targetAccount = 2
        )

        assertEquals("Water", mediaTypeRequest.mediaName)
        assertEquals(3, mediaRegisterRequest.mediaType)
        assertEquals(0, mediaRegisterRequest.meterRead.compareTo(BigDecimal("321.5")))
        assertEquals(2026, mediaRegisterRequest.year)
        assertEquals(2, mediaRegisterRequest.month)
        assertEquals(1, transferRequest.accountId)
        assertEquals(0, transferRequest.value.compareTo(BigDecimal("20.00")))
        assertEquals(2, transferRequest.targetAccount)
    }

    @Test
    fun invoiceAndOperationModels_keepCoreValues() {
        val updateInvoiceAccountRequest = UpdateInvoiceAccountRequest(
            invoiceId = 101,
            oldAccount = 1,
            newAccount = 4
        )
        val accountOperation = AccountOperation(
            id = 5,
            date = "2026-02-20",
            value = BigDecimal("17.50"),
            account = 1,
            type = "EXPENSE"
        )
        val invoiceDetails = InvoiceDetails(
            invoiceItemId = 1,
            productName = "Milk",
            quantity = BigDecimal("2.000"),
            price = BigDecimal("10.00"),
            discount = BigDecimal("1.00"),
            totalPrice = BigDecimal("19.00")
        )

        assertEquals(101, updateInvoiceAccountRequest.invoiceId)
        assertEquals(1, updateInvoiceAccountRequest.oldAccount)
        assertEquals(4, updateInvoiceAccountRequest.newAccount)
        assertEquals("EXPENSE", accountOperation.type)
        assertEquals(0, accountOperation.value.compareTo(BigDecimal("17.50")))
        assertEquals("Milk", invoiceDetails.productName)
        assertEquals(0, invoiceDetails.totalPrice.compareTo(BigDecimal("19.00")))
    }

    @Test
    fun status_enum_containsExpectedValuesInOrder() {
        assertEquals(listOf(Status.SUCCESS, Status.ERROR, Status.LOADING), Status.values().toList())
    }
}
