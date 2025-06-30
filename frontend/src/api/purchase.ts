import axios from 'axios';
import api from './api';
import {PurchaseResponseDTO} from '../types';

export const createPurchase = async (): Promise<PurchaseResponseDTO> => {
    try {
        const response = await api.post<PurchaseResponseDTO>('/api/purchases');
        return response.data;
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            console.error('Error response:', error.response.data);
            throw error;
        }
        throw new Error('Failed to create purchase');
    }
};