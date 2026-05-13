import { createContext, useContext, useEffect, useMemo, useState, ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { apiRequest } from '../lib/api';

export interface Product {
  id: number;
  name: string;
  category: string;
  categorySlug: string;
  price: number;
  image?: string;
  modifiers: ExtraIngredient[];
}

export interface Category {
  id: number;
  label: string;
  slug: string;
}

export interface ExtraIngredient {
  id: number;
  name: string;
  price: number;
}

export interface CartItem {
  product: Product;
  quantity: number;
  extras: ExtraIngredient[];
}

interface POSContextType {
  cart: CartItem[];
  addToCart: (product: Product, extras?: ExtraIngredient[]) => void;
  removeFromCart: (index: number) => void;
  updateCartItem: (index: number, quantity: number, extras: ExtraIngredient[]) => void;
  clearCart: () => void;
  categories: Category[];
  products: Product[];
  loadingProducts: boolean;
  reloadCatalog: () => Promise<void>;
  extraIngredients: ExtraIngredient[];
}

const POSContext = createContext<POSContextType | undefined>(undefined);

interface CategoriaApi {
  idCategoria: number;
  nombre: string;
  slug: string;
}

interface ModificadorApi {
  idModificador: number;
  nombre: string;
  precioExtra: number;
}

interface ProductoApi {
  idProducto: number;
  nombre: string;
  precio: number;
  precioPromocional?: number | null;
  imagenUrl?: string | null;
  categoria: string;
  modificadores: ModificadorApi[];
}

export function POSProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [cart, setCart] = useState<CartItem[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loadingProducts, setLoadingProducts] = useState(false);

  const loadCatalog = async () => {
    if (!user) {
      setCategories([]);
      setProducts([]);
      return;
    }

    setLoadingProducts(true);
    try {
      const categorias = await apiRequest<CategoriaApi[]>('/categorias');
      const mappedCategories: Category[] = categorias.map((categoria) => ({
        id: categoria.idCategoria,
        label: categoria.nombre,
        slug: categoria.slug,
      }));

      const productGroups = await Promise.all(
        mappedCategories.map(async (categoria) => {
          const productos = await apiRequest<ProductoApi[]>(
            `/categorias/${categoria.slug}/productos`
          );

          return productos.map((producto) => ({
            id: producto.idProducto,
            name: producto.nombre,
            category: categoria.label,
            categorySlug: categoria.slug,
            price: Number(producto.precioPromocional ?? producto.precio ?? 0),
            image: producto.imagenUrl ?? undefined,
            modifiers: (producto.modificadores ?? []).map((modificador) => ({
              id: modificador.idModificador,
              name: modificador.nombre,
              price: Number(modificador.precioExtra ?? 0),
            })),
          }));
        })
      );

      setCategories(mappedCategories);
      setProducts(productGroups.flat());
    } finally {
      setLoadingProducts(false);
    }
  };

  useEffect(() => {
    void loadCatalog();
  }, [user?.idUsuario]);

  const addToCart = (product: Product, extras: ExtraIngredient[] = []) => {
    setCart([...cart, { product, quantity: 1, extras }]);
  };

  const removeFromCart = (index: number) => {
    setCart(cart.filter((_, i) => i !== index));
  };

  const updateCartItem = (index: number, quantity: number, extras: ExtraIngredient[]) => {
    const newCart = [...cart];
    newCart[index] = { ...newCart[index], quantity, extras };
    setCart(newCart);
  };

  const clearCart = () => {
    setCart([]);
  };

  const extraIngredients = useMemo(
    () =>
      Array.from(
        new Map(
          products
            .flatMap((product) => product.modifiers)
            .map((modifier) => [modifier.id, modifier])
        ).values()
      ),
    [products]
  );

  return (
    <POSContext.Provider
      value={{
        cart,
        addToCart,
        removeFromCart,
        updateCartItem,
        clearCart,
        categories,
        products,
        loadingProducts,
        reloadCatalog: loadCatalog,
        extraIngredients,
      }}
    >
      {children}
    </POSContext.Provider>
  );
}

export function usePOS() {
  const context = useContext(POSContext);
  if (context === undefined) {
    throw new Error('usePOS must be used within a POSProvider');
  }
  return context;
}
