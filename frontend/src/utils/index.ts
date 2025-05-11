export const formatPrice = (price: number): string => {
    return Math.floor(price).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ') + ' ₽';
};