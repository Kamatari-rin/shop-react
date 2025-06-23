import axios from 'axios';
import api, { getUserId } from './api';
import { PurchaseResponseDTO } from '../types';

export const createPurchase = async (): Promise<PurchaseResponseDTO> => {
    try {
        const userId = getUserId();
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