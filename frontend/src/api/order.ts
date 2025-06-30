import api from './api';
import { OrderDetailDTO, OrderListDTO, OrderItemListDTO } from '../types';

interface OrderQueryParams {
    status?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
}

export const getOrders = async (params: OrderQueryParams = {}): Promise<OrderListDTO> => {
    const { startDate, endDate, ...restParams } = params;

    const formattedParams = {
        ...restParams,
        startDate: startDate ? new Date(startDate).toISOString() : undefined,
        endDate: endDate ? new Date(endDate).toISOString() : undefined,
        page: params.page ?? 0,
        size: params.size ?? 10,
    };

    const response = await api.get<OrderListDTO>('/api/orders', {
        params: formattedParams,
    });
    return response.data;
};

export const getOrderDetail = async (orderId: number): Promise<OrderDetailDTO> => {
    const response = await api.get<OrderDetailDTO>(`/api/orders/${orderId}`);
    return response.data;
};

export const getOrderItems = async (orderId: number, page?: number, size?: number): Promise<OrderItemListDTO> => {
    const response = await api.get<OrderItemListDTO>(`/api/orders/${orderId}/items`, {
        params: {
            page: page ?? 0,
            size: size ?? 10,
        },
    });
    return response.data;
};


export const deleteOrder = async (orderId: number): Promise<void> => {
    await api.delete(`/api/orders/${orderId}`);
};