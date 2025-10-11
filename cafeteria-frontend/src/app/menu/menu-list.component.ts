import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ProductService } from './product.service';
import { Product } from './product.model';
import { CategoryService } from './category.service';
import { Category } from './category.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-menu-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './menu-list.component.html',
  styleUrls: ['./menu-list.component.scss']
})
export class MenuListComponent implements OnInit {
  readonly loading = signal(true);
  readonly search = signal('');
  readonly categoriaFilter = signal<string>('');
  readonly estadoFilter = signal<string>('');
  readonly products = signal<Product[]>([]);
  readonly creating = signal(false);
  readonly isSaving = signal(false);
  readonly editing = signal<Product | null>(null);
  readonly isUpdating = signal(false);
  readonly deletingId = signal<number | null>(null);
  readonly activatingId = signal<number | null>(null);
  readonly sortStock = signal<'asc' | 'desc'>('asc');
  readonly categoriasApi = signal<Category[]>([]);
  // Fallback: si aún no carga la API, derivar de los productos
  readonly categorias = computed(() => this.categoriasApi().length
    ? this.categoriasApi().map(c => c.nombre)
    : Array.from(new Set(this.products().map(p => p.categoria))));
  readonly estados = computed(() => Array.from(new Set(this.products().map(p => p.estado))));

  readonly filtered = computed(() => {
    const term = this.search().trim().toLowerCase();
    const cat = this.categoriaFilter();
    const est = this.estadoFilter();
    const list = this.products().filter(p => {
      const byName = !term || p.nombre.toLowerCase().includes(term);
      const byDesc = !term || (p.descripcion || '').toLowerCase().includes(term);
      const byQuery = byName || byDesc;
      const byCat = !cat || p.categoria === cat;
      const byEst = !est || p.estado === est;
      return byQuery && byCat && byEst;
    });

    const order = this.sortStock();
    const sorted = list.slice().sort((a, b) => {
      const av = Number(a.stock ?? 0);
      const bv = Number(b.stock ?? 0);
      return order === 'asc' ? av - bv : bv - av;
    });
    return sorted;
  });

  constructor(
    private readonly productService: ProductService,
    private readonly categoryService: CategoryService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.productService.getProducts().subscribe(list => {
      this.products.set(list);
      this.loading.set(false);
    });
    this.categoryService.getCategories().subscribe({
      next: cats => this.categoriasApi.set(cats),
      error: err => console.error('Error cargando categorías', err)
    });
  }

  onCreate(): void { this.creating.set(true); }

  onEdit(p: Product): void {
    this.editing.set(p);
    this.editForm.reset();
    this.editForm.patchValue({
      nombre: p.nombre,
      categoriaId: p.categoriaId ?? (this.categoriasApi().find(c => c.nombre === p.categoria)?.id ?? null),
      precio: p.precio,
      stock: p.stock,
      imagenUrl: '',
      descripcion: p.descripcion ?? ''
    });
  }

  onDelete(p: Product): void {
    if (!p.id) return;
    const ok = confirm(`¿Desactivar el producto "${p.nombre}"?`);
    if (!ok) return;
    this.deletingId.set(p.id);
    // Usamos PUT para marcar activo=false sin cambiar otros campos
    this.productService.updateProduct(p.id, {
      nombre: p.nombre,
      categoriaId: p.categoriaId ?? (this.categoriasApi().find(c => c.nombre === p.categoria)?.id ?? 0),
      precio: Number(p.precio ?? 0),
      stock: Number(p.stock ?? 0),
      descripcion: p.descripcion || undefined,
      activo: false
    }).subscribe({
      next: () => {
        // Reflejar cambio inmediato y sincronizar con backend
        this.products.update(list => list.map(x => x.id === p.id ? { ...x, estado: 'Inactivo' } : x));
        this.deletingId.set(null);
        this.productService.getProducts().subscribe(list => this.products.set(list));
      },
      error: err => {
        console.error('Error desactivando producto', err);
        this.deletingId.set(null);
        alert('No se pudo desactivar el producto');
      }
    });
  }

  onActivate(p: Product): void {
    if (!p.id) return;
    const ok = confirm(`¿Activar el producto "${p.nombre}"?`);
    if (!ok) return;
    this.activatingId.set(p.id);
    this.productService.updateProduct(p.id, {
      nombre: p.nombre,
      categoriaId: p.categoriaId ?? (this.categoriasApi().find(c => c.nombre === p.categoria)?.id ?? 0),
      precio: Number(p.precio ?? 0),
      stock: Number(p.stock ?? 0),
      descripcion: p.descripcion || undefined,
      activo: true
    }).subscribe({
      next: () => {
        this.products.update(list => list.map(x => x.id === p.id ? { ...x, estado: 'Activo' } : x));
        this.activatingId.set(null);
        this.productService.getProducts().subscribe(list => this.products.set(list));
      },
      error: err => {
        console.error('Error activando producto', err);
        this.activatingId.set(null);
        alert('No se pudo activar el producto');
      }
    });
  }

  // Formulario de creación de producto
  readonly createForm = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    categoriaId: [null as number | null, Validators.required],
    precio: [null as number | null, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    imagenUrl: [''],
    descripcion: ['']
  });

  // Formulario de edición de producto
  readonly editForm = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    categoriaId: [null as number | null, Validators.required],
    precio: [null as number | null, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    imagenUrl: [''],
    descripcion: ['']
  });

  cancelCreate(): void {
    this.creating.set(false);
    this.isSaving.set(false);
    this.createForm.reset();
  }

  cancelEdit(): void {
    this.editing.set(null);
    this.isUpdating.set(false);
    this.editForm.reset();
  }

  saveCreate(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    const v = this.createForm.getRawValue();
    this.isSaving.set(true);
    this.productService.createProduct({
      nombre: v.nombre,
      categoriaId: Number(v.categoriaId),
      precio: Number(v.precio ?? 0),
      stock: Number(v.stock ?? 0),
      imagenUrl: v.imagenUrl || undefined,
      descripcion: v.descripcion || undefined,
      activo: true
    }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.creating.set(false);
        this.createForm.reset();
        this.loading.set(true);
        this.productService.getProducts().subscribe(list => {
          this.products.set(list);
          this.loading.set(false);
        });
      },
      error: err => {
        console.error('Error creando producto', err);
        this.isSaving.set(false);
        alert('No se pudo crear el producto');
      }
    });
  }

  toggleSortStock(): void {
    this.sortStock.set(this.sortStock() === 'asc' ? 'desc' : 'asc');
  }

  saveEdit(): void {
    const current = this.editing();
    if (!current) return;
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }
    const v = this.editForm.getRawValue();
    this.isUpdating.set(true);
    this.productService.updateProduct(current.id!, {
      nombre: v.nombre,
      categoriaId: Number(v.categoriaId),
      precio: Number(v.precio ?? 0),
      stock: Number(v.stock ?? 0),
      imagenUrl: v.imagenUrl || undefined,
      descripcion: v.descripcion || undefined
    }).subscribe({
      next: () => {
        this.isUpdating.set(false);
        this.editing.set(null);
        this.editForm.reset();
        this.loading.set(true);
        this.productService.getProducts().subscribe(list => {
          this.products.set(list);
          this.loading.set(false);
        });
      },
      error: err => {
        console.error('Error actualizando producto', err);
        this.isUpdating.set(false);
        alert('No se pudo actualizar el producto');
      }
    });
  }
}
