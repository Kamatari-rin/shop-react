import axios from 'axios';
import {ProductDetailDTO, ProductPage} from '../types';

const api = axios.create({
    baseURL: 'http://localhost:8080',
});

export interface ProductFilters {
    categoryId?: number;
    minPrice?: number;
    maxPrice?: number;
    search?: string;
    page?: number;
    size?: number;
    sort?: string;
    direction?: 'asc' | 'desc';
}

export const getProducts = async (filters: ProductFilters): Promise<ProductPage> => {
    const response = await api.get<ProductPage>('/api/products', {
        params: {
            categoryId: filters.categoryId,
            minPrice: filters.minPrice,
            maxPrice: filters.maxPrice,
            search: filters.search,
            page: filters.page ?? 0,
            size: filters.size ?? 10,
            sort: filters.sort ?? 'name',
            direction: filters.direction ?? 'asc',
        },
    });
    return response.data;
};

export const getProductById = async (id: number): Promise<ProductDetailDTO> => {
    const response = await api.get<ProductDetailDTO>(`/api/products/${id}`);
    return response.data;
};