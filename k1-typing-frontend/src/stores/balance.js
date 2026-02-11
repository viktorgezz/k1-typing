import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { balanceAPI } from '@/api/balance'

/**
 * Store для управления балансом пользователя
 */
export const useBalanceStore = defineStore('balance', () => {
    // State
    const balance = ref(0)
    const loading = ref(false)
    const error = ref(null)

    // Getters
    const userBalance = computed(() => balance.value)

    // Actions
    /**
     * Загрузить баланс пользователя
     */
    async function fetchBalance() {
        loading.value = true
        error.value = null

        try {
            const data = await balanceAPI.viewOwnBalance()
            balance.value = data.balance || 0
            return { success: true }
        } catch (err) {
            console.error('Failed to fetch balance:', err)
            error.value = err.response?.data?.message || 'Ошибка загрузки баланса'
            balance.value = 0
            return { success: false, error: error.value }
        } finally {
            loading.value = false
        }
    }

    /**
     * Сбросить баланс
     */
    function resetBalance() {
        balance.value = 0
        error.value = null
    }

    /**
     * Очистить ошибку
     */
    function clearError() {
        error.value = null
    }

    return {
        // State
        balance,
        loading,
        error,
        // Getters
        userBalance,
        // Actions
        fetchBalance,
        resetBalance,
        clearError,
    }
})
