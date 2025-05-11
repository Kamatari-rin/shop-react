import { create } from 'zustand';
import { CartDTO } from '../types';

interface ProductFilters {
    search: string;
    minPrice?: number;
    maxPrice?: number;
    page: number;
    size: number;
    sort: 'name' | 'price';
    direction: 'asc' | 'desc';
    setSearch: (search: string) => void;
    setPriceRange: (min?: number, max?: number) => void;
    setPage: (page: number) => void;
    setSize: (size: number) => void;
    setSort: (sort: 'name' | 'price', direction: 'asc' | 'desc') => void;
}

interface CartState {
    cart: CartDTO | null;
    setCart: (cart: CartDTO) => void;
    clearCartState: () => void;
}

export const useProductStore = create<ProductFilters>((set) => ({
    search: '',
    minPrice: undefined,
    maxPrice: undefined,
    page: 0,
    size: 10,
    sort: 'name',
    direction: 'asc',
    setSearch: (search) => set({ search, page: 0 }),
    setPriceRange: (minPrice, maxPrice) => set({ minPrice, maxPrice, page: 0 }),
    setPage: (page) => set({ page }),
    setSize: (size) => set({ size, page: 0 }),
    setSort: (sort, direction) => set({ sort, direction, page: 0 }),
}));

export const useCartStore = create<CartState>((set) => ({
    cart: null,
    setCart: (cart) => set({ cart }),
    clearCartState: () => set({ cart: null }),
}));