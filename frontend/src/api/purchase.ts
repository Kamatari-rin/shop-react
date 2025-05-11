import axios from 'axios';
import { PurchaseResponseDTO } from '../types';

const api = axios.create({
    baseURL: 'http://localhost:8080',
});

export const createPurchase = async (userId: string): Promise<PurchaseResponseDTO> => {
    try {
        const response = await api.post<PurchaseResponseDTO>(`/api/purchases/${userId}`);
        return response.data;
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            console.error('Error response:', error.response.data);
            throw error;
        }
        throw new Error('Failed to create purchase');
    }
};