import { useQuery } from '@tanstack/react-query';
import { getProducts } from '../api/catalog';
import { useProductStore } from '../store';
import ProductCard from '../components/common/ProductCard';
import { useEffect, useRef } from 'react';
import debounce from 'lodash.debounce';

export default function ShopPage() {
    const {
        search,
        minPrice,
        maxPrice,
        page,
        size,
        sort,
        direction,
        setSearch,
        setPriceRange,
        setPage,
        setSize,
        setSort,
    } = useProductStore();

    const { data, isLoading, error } = useQuery({
        queryKey: ['products', { search, minPrice, maxPrice, page, size, sort, direction }],
        queryFn: () => getProducts({ search, minPrice, maxPrice, page, size, sort, direction }),
    });

    const debouncedSearch = useRef(
        debounce((value: string) => {
            setSearch(value);
        }, 300)
    ).current;

    const debouncedPriceFilter = useRef(
        debounce((min: number | undefined, max: number | undefined) => {
            setPriceRange(min, max);
        }, 300)
    ).current;

    useEffect(() => {
        return () => {
            debouncedSearch.cancel();
            debouncedPriceFilter.cancel();
        };
    }, [debouncedSearch, debouncedPriceFilter]);

    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        debouncedSearch(e.target.value);
    };

    const handlePriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        const numericValue = value ? Number(value.replace(/[^0-9]/g, '')) : undefined;
        if (name === 'minPrice') {
            debouncedPriceFilter(numericValue, maxPrice);
        } else if (name === 'maxPrice') {
            debouncedPriceFilter(minPrice, numericValue);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row gap-4">
                <input
                    type="text"
                    placeholder="Найти товар"
                    defaultValue={search}
                    onChange={handleSearchChange}
                    className="border p-2 rounded w-full md:w-1/3 text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <div className="flex gap-4">
                    <input
                        type="text"
                        name="minPrice"
                        placeholder="Мин. цена"
                        pattern="[0-9]*"
                        inputMode="numeric"
                        defaultValue={minPrice}
                        onChange={handlePriceChange}
                        className="border p-2 rounded w-24 text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        onInput={(e) => {
                            e.currentTarget.value = e.currentTarget.value.replace(/[^0-9]/g, '');
                        }}
                    />
                    <input
                        type="text"
                        name="maxPrice"
                        placeholder="Макс. цена"
                        pattern="[0-9]*"
                        inputMode="numeric"
                        defaultValue={maxPrice}
                        onChange={handlePriceChange}
                        className="border p-2 rounded w-24 text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        onInput={(e) => {
                            e.currentTarget.value = e.currentTarget.value.replace(/[^0-9]/g, '');
                        }}
                    />
                </div>
                <div className="flex gap-4">
                    <button
                        onClick={() => setSort('name', direction === 'asc' ? 'desc' : 'asc')}
                        className="flex items-center gap-2 text-gray-700 hover:text-blue-600"
                    >
                        По названию
                        <svg
                            className={`w-4 h-4 transform ${direction === 'asc' && sort === 'name' ? 'rotate-0' : 'rotate-180'}`}
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                        </svg>
                    </button>
                    <button
                        onClick={() => setSort('price', direction === 'asc' ? 'desc' : 'asc')}
                        className="flex items-center gap-2 text-gray-700 hover:text-blue-600"
                    >
                        По цене
                        <svg
                            className={`w-4 h-4 transform ${direction === 'asc' && sort === 'price' ? 'rotate-0' : 'rotate-180'}`}
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                        </svg>
                    </button>
                </div>
            </div>

            {isLoading && <div>Загрузка...</div>}
            {error && <div>Ошибка: {(error as Error).message}</div>}
            {data && data.content.length === 0 && <div>Товары не найдены</div>}
            {data && data.content.length > 0 && (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    {data.content.map((product) => (
                        <ProductCard key={product.id} product={product} />
                    ))}
                </div>
            )}

            {data && (
                <div className="flex justify-center gap-4 items-center">
                    <button
                        disabled={page === 0}
                        onClick={() => setPage(page - 1)}
                        className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50 hover:bg-gray-300 transition"
                    >
                        Назад
                    </button>
                    <span>
            Страница {page + 1} из {data.totalPages}
          </span>
                    <button
                        disabled={page >= data.totalPages - 1}
                        onClick={() => setPage(page + 1)}
                        className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50 hover:bg-gray-300 transition"
                    >
                        Вперед
                    </button>
                    <select
                        value={size}
                        onChange={(e) => setSize(Number(e.target.value))}
                        className="border p-2 rounded text-gray-900"
                    >
                        <option value={10}>10</option>
                        <option value={20}>20</option>
                        <option value={50}>50</option>
                        <option value={100}>100</option>
                    </select>
                </div>
            )}
        </div>
    );
}