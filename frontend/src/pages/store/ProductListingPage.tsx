import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productService } from '@/services/productService';
import { ProductCard } from '@/components/product/ProductCard';
import type { Product, Category, CatalogPageResponse } from '@/types';
import { Filter, SlidersHorizontal, X, ChevronLeft, ChevronRight } from 'lucide-react';

export const ProductListingPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [categories, setCategories] = useState<Category[]>([]);
  const [data, setData] = useState<CatalogPageResponse<Product> | null>(null);
  const [loading, setLoading] = useState(true);
  const [mobileFilterOpen, setMobileFilterOpen] = useState(false);

  // Active Filter state from URL search params
  const q = searchParams.get('q') || '';
  const categorySlug = searchParams.get('categorySlug') || '';
  const sort = searchParams.get('sort') || 'created_at';
  const minPrice = searchParams.get('minPrice') || '';
  const maxPrice = searchParams.get('maxPrice') || '';
  const brand = searchParams.get('brand') || '';
  const minDiscount = searchParams.get('minDiscount') || '';
  const inStockOnly = searchParams.get('inStockOnly') === 'true';
  const page = parseInt(searchParams.get('page') || '1', 10);

  useEffect(() => {
    productService.getCategories().then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    productService
      .searchProducts({
        q: q || undefined,
        categorySlug: categorySlug || undefined,
        minPrice: minPrice ? Number(minPrice) : undefined,
        maxPrice: maxPrice ? Number(maxPrice) : undefined,
        brand: brand || undefined,
        minDiscount: minDiscount ? Number(minDiscount) : undefined,
        inStockOnly: inStockOnly || undefined,
        sort: sort || undefined,
        page,
        pageSize: 16,
      })
      .then(setData)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [searchParams]);

  const updateParam = (key: string, value: string | null) => {
    const newParams = new URLSearchParams(searchParams);
    if (value) {
      newParams.set(key, value);
    } else {
      newParams.delete(key);
    }
    newParams.set('page', '1'); // reset page on filter change
    setSearchParams(newParams);
  };

  const handlePageChange = (newPage: number) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set('page', newPage.toString());
    setSearchParams(newParams);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const clearAllFilters = () => {
    setSearchParams(new URLSearchParams());
  };

  const FilterSidebar = () => (
    <div className="space-y-6 text-sm">
      <div className="flex items-center justify-between pb-4 border-b border-gray-200">
        <h3 className="font-bold text-gray-900 text-base flex items-center gap-2">
          <SlidersHorizontal className="w-4 h-4 text-blue-600" /> Filters
        </h3>
        <button
          onClick={clearAllFilters}
          className="text-xs font-bold text-blue-600 hover:underline"
        >
          Reset All
        </button>
      </div>

      {/* Categories */}
      <div className="space-y-2">
        <h4 className="font-bold text-xs text-gray-400 uppercase tracking-wider">Categories</h4>
        <div className="space-y-1 max-h-48 overflow-y-auto pr-2">
          <button
            onClick={() => updateParam('categorySlug', null)}
            className={`block w-full text-left px-2 py-1.5 rounded-lg text-xs font-medium transition ${
              !categorySlug ? 'bg-blue-50 text-blue-700 font-bold' : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            All Categories
          </button>
          {categories.map((cat) => (
            <button
              key={cat.categoryId}
              onClick={() => updateParam('categorySlug', cat.slug)}
              className={`block w-full text-left px-2 py-1.5 rounded-lg text-xs font-medium transition ${
                categorySlug === cat.slug
                  ? 'bg-blue-50 text-blue-700 font-bold'
                  : 'text-gray-600 hover:bg-gray-50'
              }`}
            >
              {cat.categoryName}
            </button>
          ))}
        </div>
      </div>

      {/* Price Range */}
      <div className="space-y-2 pt-2 border-t border-gray-100">
        <h4 className="font-bold text-xs text-gray-400 uppercase tracking-wider">Price (₹)</h4>
        <div className="flex items-center gap-2">
          <input
            type="number"
            placeholder="Min"
            value={minPrice}
            onChange={(e) => updateParam('minPrice', e.target.value || null)}
            className="w-1/2 p-2 border border-gray-300 rounded-lg text-xs"
          />
          <span className="text-gray-400">-</span>
          <input
            type="number"
            placeholder="Max"
            value={maxPrice}
            onChange={(e) => updateParam('maxPrice', e.target.value || null)}
            className="w-1/2 p-2 border border-gray-300 rounded-lg text-xs"
          />
        </div>
      </div>

      {/* Minimum Discount */}
      <div className="space-y-2 pt-2 border-t border-gray-100">
        <h4 className="font-bold text-xs text-gray-400 uppercase tracking-wider">Min Discount</h4>
        <div className="space-y-1.5 text-xs text-gray-700">
          {[10, 20, 30, 50].map((d) => (
            <label key={d} className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="minDiscount"
                checked={minDiscount === d.toString()}
                onChange={() => updateParam('minDiscount', d.toString())}
                className="text-blue-600 focus:ring-blue-500"
              />
              <span>{d}% and above</span>
            </label>
          ))}
        </div>
      </div>

      {/* Availability */}
      <div className="space-y-2 pt-2 border-t border-gray-100">
        <label className="flex items-center gap-2 cursor-pointer text-xs font-bold text-gray-800">
          <input
            type="checkbox"
            checked={inStockOnly}
            onChange={(e) => updateParam('inStockOnly', e.target.checked ? 'true' : null)}
            className="rounded text-blue-600 focus:ring-blue-500"
          />
          <span>Exclude Out of Stock</span>
        </label>
      </div>
    </div>
  );

  const products = data?.items || [];
  const totalPages = data?.totalPages || 1;

  return (
    <div className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 py-4 sm:py-8">
      {/* Top Header Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 sm:pb-6 border-b border-gray-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-black text-gray-900">
            {q ? `Search results for "${q}"` : categorySlug ? `Category: ${categorySlug}` : 'Explore Products'}
          </h1>
          <p className="text-xs text-gray-500 mt-0.5 sm:mt-1">
            Showing {data?.totalItems || 0} results
          </p>
        </div>

        <div className="flex items-center gap-2 sm:gap-3">
          <button
            onClick={() => setMobileFilterOpen(true)}
            className="lg:hidden flex items-center gap-1.5 px-3 py-1.5 border border-gray-300 rounded-xl text-xs font-bold text-gray-700 bg-white shadow-xs"
          >
            <Filter className="w-3.5 h-3.5" /> Filters
          </button>

          {/* Sort Selector */}
          <div className="flex items-center gap-1.5 sm:gap-2 text-xs">
            <span className="text-gray-400 font-medium hidden sm:inline">Sort by:</span>
            <select
              value={sort}
              onChange={(e) => updateParam('sort', e.target.value)}
              className="px-2.5 py-1.5 border border-gray-300 rounded-xl text-xs font-bold text-gray-700 bg-white focus:outline-none focus:border-blue-600"
            >
              <option value="created_at">Newest First</option>
              <option value="price_asc">Price: Low to High</option>
              <option value="price_desc">Price: High to Low</option>
              <option value="deals">Discount: High to Low</option>
              <option value="rating">Highest Rated</option>
            </select>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-4 sm:gap-8 pt-4 sm:pt-8">
        {/* Desktop Filter Sidebar */}
        <div className="hidden lg:block bg-white p-6 rounded-2xl border border-gray-200 h-fit sticky top-24">
          <FilterSidebar />
        </div>

        {/* Mobile Filter Drawer */}
        {mobileFilterOpen && (
          <div className="fixed inset-0 z-50 lg:hidden">
            <div
              className="absolute inset-0 bg-gray-900/60 backdrop-blur-sm"
              onClick={() => setMobileFilterOpen(false)}
            />
            <div className="absolute inset-y-0 right-0 max-w-xs w-full bg-white p-6 shadow-2xl overflow-y-auto">
              <div className="flex justify-end mb-4">
                <button
                  onClick={() => setMobileFilterOpen(false)}
                  className="p-1.5 text-gray-400 hover:text-gray-600 rounded-full hover:bg-gray-100"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              <FilterSidebar />
            </div>
          </div>
        )}

        {/* Products Grid */}
        <div className="lg:col-span-3">
          {loading ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5 sm:gap-4 lg:gap-6 animate-pulse">
              {[1, 2, 3, 4, 5, 6].map((n) => (
                <div key={n} className="h-80 bg-gray-200 rounded-2xl"></div>
              ))}
            </div>
          ) : products.length === 0 ? (
            <div className="text-center py-16 space-y-4 bg-white rounded-2xl border border-gray-200 p-8">
              <p className="text-lg font-bold text-gray-800">No matching products found</p>
              <p className="text-xs text-gray-500 max-w-sm mx-auto">
                We couldn't find any products matching your current filters. Try changing keywords or resetting filters.
              </p>
              <button
                onClick={clearAllFilters}
                className="px-6 py-2 bg-blue-600 text-white text-xs font-bold rounded-full hover:bg-blue-700 transition"
              >
                Clear All Filters
              </button>
            </div>
          ) : (
            <div className="space-y-6 sm:space-y-8">
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5 sm:gap-4 lg:gap-6">
                {products.map((product) => (
                  <ProductCard key={product.productId} product={product} />
                ))}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 pt-6 border-t border-gray-200">
                  <button
                    onClick={() => handlePageChange(page - 1)}
                    disabled={page <= 1}
                    className="p-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>

                  <span className="text-xs font-bold text-gray-700 px-4">
                    Page {page} of {totalPages}
                  </span>

                  <button
                    onClick={() => handlePageChange(page + 1)}
                    disabled={page >= totalPages}
                    className="p-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
