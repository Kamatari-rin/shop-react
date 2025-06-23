import { create } from 'zustand';
import { v4 as uuid } from 'uuid';
import keycloak from '../keycloak';
import {
    getCart,
    getOrCreateAnonymousCart,
    addItemToCart,
    addItemToAnonymousCart,
    removeItemFromCart,
    removeItemFromAnonymousCart,
    updateItemQuantity,
    updateItemQuantityInAnonymousCart,
    clearCart,
    clearAnonymousCart,
    mergeCarts,
} from '../api/cart';
import { CartDTO, CartItemRequestDTO } from '../types';

interface CartState {
    cartId: string | null;
    cart: CartDTO | null;
    addItem: (productId: number, quantity: number) => Promise<void>;
    removeItem: (productId: number) => Promise<void>;
    updateQuantity: (productId: number, quantity: number) => Promise<void>;
    clearCart: () => Promise<void>;
    syncWithServer: () => Promise<void>;
    fetchCart: () => Promise<void>;
}

export const useCartStore = create<CartState>((set, get) => ({
    cartId: localStorage.getItem('cart_id'),
    cart: null,
    addItem: async (productId: number, quantity: number) => {
        const { cartId } = get();
        const item: CartItemRequestDTO = { productId, quantity };

        console.debug('Adding item, authenticated:', keycloak.authenticated, 'cartId:', cartId);

        if (keycloak.authenticated) {
            try {
                const updatedCart = await addItemToCart(item);
                set({ cart: updatedCart });
            } catch (error) {
                console.error('Failed to add item to server cart:', error);
                throw error; // Пробрасываем ошибку для обработки в компоненте
            }
        } else {
            try {
                let currentCartId = cartId || uuid();
                const updatedCart = await addItemToAnonymousCart(currentCartId, item);
                if (updatedCart.id !== currentCartId) {
                    console.debug('New cart ID received:', updatedCart.id);
                    currentCartId = updatedCart.id;
                    set({ cartId: currentCartId });
                    localStorage.setItem('cart_id', currentCartId);
                }
                set({ cart: updatedCart });
            } catch (error) {
                console.error('Failed to add item to anonymous cart:', error);
                throw error; // Пробрасываем ошибку для обработки в компоненте
            }
        }
    },
    removeItem: async (productId: number) => {
        const { cartId } = get();

        console.debug('Removing item, authenticated:', keycloak.authenticated, 'cartId:', cartId);

        if (keycloak.authenticated) {
            try {
                const updatedCart = await removeItemFromCart(productId);
                set({ cart: updatedCart });
            } catch (error) {
                console.error('Failed to remove item from server cart:', error);
                throw error;
            }
        } else if (cartId) {
            try {
                const updatedCart = await removeItemFromAnonymousCart(cartId, productId);
                set({ cart: updatedCart });
            } catch (error) {
                console.error('Failed to remove item from anonymous cart:', error);
                throw error;
            }
        }
    },
    updateQuantity: async (productId: number, quantity: number) => {
        if (quantity < 1) return;
        const { cartId } = get();
        const item: CartItemRequestDTO = { productId, quantity };

        console.debug('Updating quantity, authenticated:', keycloak.authenticated, 'cartId:', cartId);

        if (keycloak.authenticated) {
            try {
                const updatedCart = await updateItemQuantity(item);
                set({ cart: updatedCart });
            } catch (error) {
                console.error('Failed to update item quantity on server:', error);
                throw error;
            }
        } else if (cartId) {
            try {
                const updatedCart = await updateItemQuantityInAnonymousCart(cartId, item);
                set({ cart: updatedCart });
            } catch (error) {
                console.error('Failed to update item quantity in anonymous cart:', error);
                throw error;
            }
        }
    },
    clearCart: async () => {
        const { cartId } = get();

        console.debug('Clearing cart, authenticated:', keycloak.authenticated, 'cartId:', cartId);

        set({ cart: null, cartId: null });
        localStorage.removeItem('cart_id');

        if (keycloak.authenticated) {
            try {
                await clearCart();
            } catch (error) {
                console.error('Failed to clear server cart:', error);
                throw error;
            }
        } else if (cartId) {
            try {
                await clearAnonymousCart(cartId);
            } catch (error) {
                console.error('Failed to clear anonymous cart:', error);
                throw error;
            }
        }
    },
    syncWithServer: async () => {
        if (!keycloak.authenticated) return;
        const { cartId } = get();

        console.debug('Syncing with server, cartId:', cartId);

        if (cartId) {
            try {
                const updatedCart = await mergeCarts(cartId);
                set({ cart: updatedCart, cartId: null });
                localStorage.removeItem('cart_id');
            } catch (error) {
                console.error('Failed to merge carts:', error);
                await get().fetchCart();
            }
        } else {
            await get().fetchCart();
        }
    },
    fetchCart: async () => {
        const { cartId } = get();

        console.debug('Fetching cart, authenticated:', keycloak.authenticated, 'cartId:', cartId);

        if (!keycloak.authenticated) {
            if (!cartId) {
                try {
                    const newCartId = uuid();
                    const cart = await getOrCreateAnonymousCart(newCartId);
                    set({ cart, cartId: cart.id });
                    localStorage.setItem('cart_id', cart.id);
                } catch (error) {
                    console.error('Failed to create anonymous cart:', error);
                    set({ cartId: null });
                    localStorage.removeItem('cart_id');
                }
            } else {
                try {
                    const cart = await getOrCreateAnonymousCart(cartId);
                    if (cart.id !== cartId) {
                        console.debug('New cart ID received:', cart.id);
                        set({ cart, cartId: cart.id });
                        localStorage.setItem('cart_id', cart.id);
                    } else {
                        set({ cart });
                    }
                } catch (error) {
                    console.error('Failed to fetch anonymous cart:', error);
                    set({ cartId: null });
                    localStorage.removeItem('cart_id');
                }
            }
        } else {
            try {
                const cart = await getCart();
                set({ cart });
            } catch (error) {
                console.error('Failed to fetch server cart:', error);
            }
        }
    },
}));