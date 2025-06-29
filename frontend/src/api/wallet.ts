import api from './api';

export const getWallet = async (): Promise<{ balance: number; userId: string }> => {
    const response = await api.get('/api/wallets');
    return {
        balance: response.data.balance, // Просто используем значение как есть
        userId: response.data.userId.toString(),
    };
};

export const creditBalance = async (amount: number): Promise<void> => {
    await api.put('/api/wallets/credit', { amount });
};