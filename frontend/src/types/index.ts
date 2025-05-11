export interface ProductDTO {
    id: number;
    name: string;
    price: number;
    imageUrl: string;
    categoryId: number;
}

export interface ProductPage {
    content: ProductDTO[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
}

export interface ProductDetailDTO {
    id: number;
    name: string;
    description: string;
    price: number;
    imageUrl: string;
    categoryId: number;
    categoryName: string;
    createdAt: string;
    updatedAt: string;
}

export interface CartDTO {
    id: number;
    userId: string;
    items: CartItemDTO[];
    totalAmount: number;
    createdAt: string;
}

export interface CartItemDTO {
    id: number;
    productId: number;
    productName: string;
    priceAtTime: number;
    quantity: number;
    imageUrl: string;
}

export interface OrderDetailDTO {
    id: number;
    userId: string;
    orderDate: string;
    status: OrderStatus;
    totalAmount: number;
    items: OrderItemDTO[];
}

export interface OrderListDTO {
    orders: OrderDTO[];
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
}

export interface OrderDTO {
    id: number;
    userId: string;
    orderDate: string;
    status: OrderStatus;
    totalAmount: number;
}

export enum OrderStatus {
    PENDING = 'PENDING',
    COMPLETED = 'COMPLETED',
    CANCELLED = 'CANCELLED',
}

export interface OrderItemListDTO {
    items: OrderItemDTO[];
    page?: number;
    size?: number;
    totalPages?: number;
    totalElements?: number;
}

export interface OrderItemDTO {
    id: number;
    productId: number;
    productName: string;
    quantity: number;
    price: number;
    imageUrl: string;
}

export interface CreateOrderRequestDTO {
    userId: string;
    items: CartItemRequestDTO[];
}

export interface CartItemRequestDTO {
    productId: number;
    quantity: number;
}

export interface PurchaseResponseDTO {
    orderId: number;
    userId: string;
    totalAmount: number;
    paymentStatus: PaymentStatus;
    transactionDate: string;
}

export enum PaymentStatus {
    PENDING = 'PENDING',
    COMPLETED = 'COMPLETED',
    FAILED = 'FAILED',
    REFUNDED = 'REFUNDED',
}