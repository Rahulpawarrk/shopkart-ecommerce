import React, { useState, useEffect } from 'react';
import { adminService } from '@/services/adminService';
import { useAppDispatch } from '@/store';
import { showToast } from '@/store/slices/uiSlice';
import type { Category } from '@/types';
import { Plus, Edit, Layers, X, FolderTree } from 'lucide-react';

export const AdminCategoriesPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [saving, setSaving] = useState(false);

  const [formData, setFormData] = useState<{
    categoryName: string;
    slug: string;
    description: string;
    parentCategoryId?: number | null;
    active: boolean;
  }>({
    categoryName: '',
    slug: '',
    description: '',
    parentCategoryId: undefined,
    active: true,
  });

  const loadCategories = () => {
    setLoading(true);
    adminService
      .getCategories()
      .then(setCategories)
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadCategories();
  }, []);

  const handleOpenAdd = () => {
    setEditingCategory(null);
    setFormData({
      categoryName: '',
      slug: '',
      description: '',
      parentCategoryId: undefined,
      active: true,
    });
    setModalOpen(true);
  };

  const handleOpenEdit = (c: Category) => {
    setEditingCategory(c);
    setFormData({
      categoryName: c.categoryName,
      slug: c.slug,
      description: c.description || '',
      parentCategoryId: c.parentCategoryId ?? undefined,
      active: c.active,
    });
    setModalOpen(true);
  };

  const handleNameChange = (categoryName: string) => {
    const slug = categoryName
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)+/g, '');
    setFormData((prev) => ({
      ...prev,
      categoryName,
      slug: editingCategory ? prev.slug : slug,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (editingCategory) {
        await adminService.updateCategory(editingCategory.categoryId, formData);
        dispatch(showToast({ message: 'Category updated successfully', type: 'success' }));
      } else {
        await adminService.createCategory(formData);
        dispatch(showToast({ message: 'Category created successfully', type: 'success' }));
      }
      setModalOpen(false);
      loadCategories();
    } catch (err: any) {
      dispatch(showToast({ message: err.message || 'Failed to save category', type: 'error' }));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900">Category Catalog</h1>
          <p className="text-xs text-slate-500">Organize merchandise hierarchy and taxonomy</p>
        </div>
        <button
          onClick={handleOpenAdd}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-primary text-white text-xs font-bold rounded-xl hover:bg-primary/90 transition shadow-sm self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          Add Category
        </button>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="py-16 text-center">
            <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-xs text-slate-500">Loading categories...</p>
          </div>
        ) : categories.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-xs italic">No categories created yet.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Category Name</th>
                  <th className="px-6 py-3">URL Slug</th>
                  <th className="px-6 py-3">Parent</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {categories.map((cat) => {
                  const parent = categories.find((c) => c.categoryId === cat.parentCategoryId);
                  return (
                    <tr key={cat.categoryId} className="hover:bg-slate-50/50 transition">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <Layers className="w-4 h-4 text-slate-400" />
                          <span className="font-bold text-slate-900">{cat.categoryName}</span>
                        </div>
                        {cat.description && (
                          <p className="text-[11px] text-slate-400 pl-6 mt-0.5">{cat.description}</p>
                        )}
                      </td>
                      <td className="px-6 py-4 font-mono text-slate-500">{cat.slug}</td>
                      <td className="px-6 py-4 text-slate-700">
                        {parent ? (
                          <span className="inline-flex items-center gap-1 bg-slate-100 px-2 py-0.5 rounded text-[10px]">
                            <FolderTree className="w-3 h-3 text-slate-400" />
                            {parent.categoryName}
                          </span>
                        ) : (
                          <span className="text-slate-400 italic text-[11px]">Root Category</span>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <span
                          className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                            cat.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-600'
                          }`}
                        >
                          {cat.active ? 'Active' : 'Hidden'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <button
                          onClick={() => handleOpenEdit(cat)}
                          className="p-1.5 text-slate-500 hover:text-primary rounded hover:bg-slate-100 transition"
                          title="Edit category"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl relative">
            <button
              onClick={() => setModalOpen(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-lg font-bold text-slate-900 mb-1">
              {editingCategory ? 'Edit Category' : 'Create Category'}
            </h3>
            <p className="text-xs text-slate-500 mb-4">Manage catalog navigation and sorting</p>

            <form onSubmit={handleSubmit} className="space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Category Name *</label>
                <input
                  type="text"
                  required
                  value={formData.categoryName}
                  onChange={(e) => handleNameChange(e.target.value)}
                  className="w-full p-2.5 rounded-lg border border-slate-200 focus:ring-2 focus:ring-primary"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">URL Slug *</label>
                <input
                  type="text"
                  required
                  value={formData.slug}
                  onChange={(e) => setFormData({ ...formData, slug: e.target.value })}
                  className="w-full p-2.5 rounded-lg border border-slate-200 font-mono"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Parent Category</label>
                <select
                  value={formData.parentCategoryId ?? ''}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      parentCategoryId: e.target.value ? Number(e.target.value) : undefined,
                    })
                  }
                  className="w-full p-2.5 rounded-lg border border-slate-200 bg-white"
                >
                  <option value="">None (Top Level)</option>
                  {categories
                    .filter((c) => !editingCategory || c.categoryId !== editingCategory.categoryId)
                    .map((c) => (
                      <option key={c.categoryId} value={c.categoryId}>
                        {c.categoryName}
                      </option>
                    ))}
                </select>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Description</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full p-2.5 rounded-lg border border-slate-200 min-h-[60px]"
                />
              </div>

              <div className="pt-1">
                <label className="flex items-center gap-2 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={formData.active}
                    onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                    className="w-4 h-4 text-primary rounded"
                  />
                  <span className="font-semibold text-slate-700">Active / Visible in navigation</span>
                </label>
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 font-semibold text-slate-600 hover:bg-slate-100 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-5 py-2 font-bold text-white bg-primary hover:bg-primary/90 rounded-lg shadow disabled:opacity-50"
                >
                  {saving ? 'Saving...' : editingCategory ? 'Save Changes' : 'Create Category'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
