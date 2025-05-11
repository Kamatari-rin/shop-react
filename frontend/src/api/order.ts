import axios from 'axios';
import { OrderDetailDTO, OrderListDTO, OrderItemListDTO, CreateOrderRequestDTO } from '../types';

const api = axios.create({
    baseURL: 'http://localhost:8080',
});

const TEST_USER_ID = '550e8400-e29b-41d4-a716-446655440000';

interface OrderQueryParams {
    status?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
}

export const getOrders = async (params: OrderQueryParams = {}) => {
    const { startDate, endDate, ...restParams } = params;

    const formattedParams = {
        ...restParams,
        startDate: startDate ? new Date(startDate).toISOString() : undefined,
        endDate: endDate ? new Date(endDate).toISOString() : undefined,
        page: params.page ?? 0,
        size: params.size ?? 10,
    };

    const response = await api.get<OrderListDTO>(`/api/orders/${TEST_USER_ID}`, {
        params: formattedParams,
    });
    return response.data;
};

export const getOrderDetail = async (orderId: number) => {
    const response = await api.get<OrderDetailDTO>(
        `/api/orders/${TEST_USER_ID}/${orderId}`
    );
    return response.data;
};

export const getOrderItems = async (orderId: number, page?: number, size?: number) => {
    const response = await api.get<OrderItemListDTO>(
        `/api/orders/${TEST_USER_ID}/${orderId}/items`,
        {
            params: {
                page: page ?? 0,
                size: size ?? 10,
            },
        }
    );
    return response.data;
};

export const createOrder = async (request: CreateOrderRequestDTO) => {
    const response = await api.post<OrderDetailDTO>(
        `/api/orders/${TEST_USER_ID}`,
        request
    );
    return response.data;
};

export const deleteOrder = async (orderId: number) => {
    await api.delete(`/api/orders/${TEST_USER_ID}/${orderId}`);
};