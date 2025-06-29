import api from './api';
import { CartDTO, CartItemRequestDTO } from '../types';

export const getCart = async (): Promise<CartDTO> => {
    const response = await api.get<CartDTO>('/api/cart');
    return response.data;
};

export const getOrCreateAnonymousCart = async (id: string): Promise<CartDTO> => {
    const response = await api.get<CartDTO>(`/api/cart/anonymous/${id}`);
    // Примечание: Если корзина не найдена, сервер создаёт новую с новым id.
    // Проверку возвращённого id и обновление localStorage нужно делать в cartStore.ts.
    return response.data;
};

export const addItemToCart = async (item: CartItemRequestDTO): Promise<CartDTO> => {
    const response = await api.post<CartDTO>('/api/cart/items', item);
    return response.data;
};

export const addItemToAnonymousCart = async (id: string, item: CartItemRequestDTO): Promise<CartDTO> => {
    const response = await api.post<CartDTO>(`/api/cart/anonymous/${id}/items`, item);
    return response.data;
};

export const removeItemFromCart = async (productId: number): Promise<CartDTO> => {
    const response = await api.delete<CartDTO>(`/api/cart/items/${productId}`);
    return response.data;
};

export const removeItemFromAnonymousCart = async (id: string, productId: number): Promise<CartDTO> => {
    const response = await api.delete<CartDTO>(`/api/cart/anonymous/${id}/items/${productId}`);
    return response.data;
};

export const updateItemQuantity = async (item: CartItemRequestDTO): Promise<CartDTO> => {
    const response = await api.put<CartDTO>('/api/cart/items', item);
    return response.data;
};

export const updateItemQuantityInAnonymousCart = async (id: string, item: CartItemRequestDTO): Promise<CartDTO> => {
    const response = await api.put<CartDTO>(`/api/cart/anonymous/${id}/items`, item);
    return response.data;
};

export const clearCart = async (): Promise<void> => {
    await api.delete('/api/cart');
};

export const clearAnonymousCart = async (id: string): Promise<void> => {
    await api.delete(`/api/cart/anonymous/${id}`);
};

export const mergeCarts = async (id: string): Promise<CartDTO> => {
    const response = await api.post<CartDTO>(`/api/cart/merge/${id}`);
    return response.data;
};