import axios from 'axios';
import { CartDTO, CartItemRequestDTO } from '../types';

const api = axios.create({
    baseURL: 'http://localhost:8080',
});

export const getCart = async (userId: string): Promise<CartDTO> => {
    const response = await api.get<CartDTO>(`/api/cart/${userId}`);
    return response.data;
};

export const addItemToCart = async (userId: string, item: CartItemRequestDTO): Promise<CartDTO> => {
    const response = await api.post<CartDTO>(`/api/cart/${userId}/items`, item);
    return response.data;
};

export const removeItemFromCart = async (userId: string, productId: number): Promise<CartDTO> => {
    const response = await api.delete<CartDTO>(`/api/cart/${userId}/items/${productId}`);
    return response.data;
};

export const updateItemQuantity = async (userId: string, item: CartItemRequestDTO): Promise<CartDTO> => {
    const response = await api.put<CartDTO>(`/api/cart/${userId}/items`, item);
    return response.data;
};

export const clearCart = async (userId: string): Promise<void> => {
    await api.delete(`/api/cart/${userId}`);
};